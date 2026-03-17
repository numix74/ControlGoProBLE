package com.actioncam.airbuble.gps

import android.content.ContentValues
import android.content.Context
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Écrit un fichier GPX en temps réel, waypoint par waypoint.
 *
 * Android 10+ : MediaStore → Documents/AirBuble/GPX/
 * Android 8-9  : écriture directe (WRITE_EXTERNAL_STORAGE requis)
 *
 * Le fichier est supprimé automatiquement à la fermeture si aucun waypoint
 * n'avait de coordonnées GPS valides.
 */
class GpxWriter(private val context: Context) {

    companion object {
        private const val TAG = "GpxWriter"
        private const val FOLDER_NAME = "AirBuble/GPX"
    }

    private var outputStream: OutputStream? = null
    private var writer: BufferedWriter? = null
    private var sessionFileName: String = ""
    private var waypointCount = 0
    private var gpsWaypointCount = 0
    private var mediaStoreUri: Uri? = null
    private var lastDirectFile: File? = null

    val isOpen: Boolean get() = writer != null

    fun openSession(sessionStartTime: Long): Boolean {
        if (isOpen) {
            Log.w(TAG, "openSession sur session déjà ouverte — fermeture")
            closeSession()
        }
        return try {
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            sessionFileName = "${sdf.format(Date(sessionStartTime))}.gpx"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) openViaMediaStore()
            else openViaDirectFile()
        } catch (e: Exception) {
            Log.e(TAG, "openSession: ${e.message}")
            false
        }
    }

    private fun openViaMediaStore(): Boolean {
        val cv = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, sessionFileName)
            put(MediaStore.Files.FileColumns.MIME_TYPE, "application/gpx+xml")
            put(MediaStore.Files.FileColumns.RELATIVE_PATH, "Documents/$FOLDER_NAME")
            put(MediaStore.Files.FileColumns.IS_PENDING, 1)
        }
        val uri = context.contentResolver.insert(
            MediaStore.Files.getContentUri("external"), cv
        ) ?: return false.also { Log.e(TAG, "MediaStore insert failed") }
        mediaStoreUri = uri
        outputStream = context.contentResolver.openOutputStream(uri)
            ?: return false.also { Log.e(TAG, "Cannot open OutputStream") }
        initWriter()
        return true
    }

    @Suppress("DEPRECATION")
    private fun openViaDirectFile(): Boolean {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            FOLDER_NAME
        )
        dir.mkdirs()
        val file = File(dir, sessionFileName)
        lastDirectFile = file
        outputStream = file.outputStream()
        initWriter()
        return true
    }

    private fun initWriter() {
        waypointCount = 0; gpsWaypointCount = 0
        writer = BufferedWriter(OutputStreamWriter(outputStream!!, Charsets.UTF_8))
        writeHeader()
        Log.d(TAG, "GPX ouvert: $sessionFileName")
    }

    private fun writeHeader() {
        writer!!.write(
            """<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.1" creator="AirBuble"
     xmlns="http://www.topografix.com/GPX/1/1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
"""
        )
        writer!!.flush()
    }

    fun addWaypoint(name: String, location: Location?, timestamp: Long, recordingStartMs: Long?) {
        val w = writer ?: return
        try {
            val hasGps = location != null && (location.latitude != 0.0 || location.longitude != 0.0)
            val lat = location?.latitude ?: 0.0
            val lon = location?.longitude ?: 0.0
            val alt = location?.altitude ?: 0.0
            val speedKmh = if (location?.hasSpeed() == true) location.speed * 3.6f else 0f
            val acc = if (location?.hasAccuracy() == true) location.accuracy else -1f

            val sdfLocal = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getDefault()
            }
            val descParts = mutableListOf(sdfLocal.format(Date(timestamp)))
            if (recordingStartMs != null) {
                val elapsed = ((timestamp - recordingStartMs) / 1000).coerceAtLeast(0)
                descParts.add("T+%02d:%02d".format(elapsed / 60, elapsed % 60))
            }
            if (speedKmh > 0) descParts.add(String.format(Locale.US, "%.0f km/h", speedKmh))
            if (acc >= 0) descParts.add(String.format(Locale.US, "acc: %.0fm", acc))

            val wptName = if (hasGps) name else "$name (no GPS)"
            w.write(
                """  <wpt lat="${String.format(Locale.US, "%.8f", lat)}" lon="${String.format(Locale.US, "%.8f", lon)}">
    <ele>${String.format(Locale.US, "%.1f", alt)}</ele>
    <time>${isoUtc(timestamp)}</time>
    <name>$wptName</name>
    <desc>${descParts.joinToString(" | ")}</desc>
  </wpt>
"""
            )
            w.flush()
            waypointCount++
            if (hasGps) gpsWaypointCount++
            Log.d(TAG, "Waypoint: $wptName")
        } catch (e: Exception) {
            Log.e(TAG, "addWaypoint: ${e.message}")
        }
    }

    fun closeSession() {
        val count = waypointCount
        val gpsCount = gpsWaypointCount
        val uri = mediaStoreUri
        val file = lastDirectFile
        try {
            writer?.write("</gpx>\n")
            writer?.flush()
            writer?.close()
            outputStream?.close()
            Log.d(TAG, "GPX fermé: $sessionFileName ($count waypoints, $gpsCount avec GPS)")
        } catch (e: Exception) {
            Log.e(TAG, "closeSession: ${e.message}")
        } finally {
            writer = null; outputStream = null
            mediaStoreUri = null; lastDirectFile = null
            sessionFileName = ""; waypointCount = 0; gpsWaypointCount = 0
        }
        // Supprimer le fichier si aucun waypoint GPS valide
        if (gpsCount == 0) {
            if (uri != null) {
                context.contentResolver.delete(uri, null, null)
                Log.d(TAG, "GPX supprimé (0 GPS)")
            } else {
                file?.delete()
            }
        } else if (uri != null) {
            val cv = ContentValues().apply { put(MediaStore.Files.FileColumns.IS_PENDING, 0) }
            context.contentResolver.update(uri, cv, null, null)
        }
    }

    private fun isoUtc(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(millis))
}
