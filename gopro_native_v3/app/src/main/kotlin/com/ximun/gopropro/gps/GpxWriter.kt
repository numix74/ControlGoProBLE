package com.ximun.gopropro.gps

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
 * Sur Android 10+ (API 29) : utilise MediaStore → Documents/GoProPro/GPX/
 * Sur Android 8-9 (API 26-28) : écriture directe (nécessite WRITE_EXTERNAL_STORAGE)
 */
class GpxWriter(private val context: Context) {

    companion object {
        private const val TAG = "GpxWriter"
        private const val FOLDER_NAME = "AirBubble/GPX"
    }

    private var outputStream: OutputStream? = null
    private var writer: BufferedWriter? = null
    private var sessionFileName: String = ""
    private var waypointCount = 0
    private var gpsWaypointCount = 0   // waypoints avec coordonnées GPS valides
    private var mediaStoreUri: Uri? = null
    private var lastDirectFile: File? = null

    fun openSession(sessionStartTime: Long): Boolean {
        // Filet de sécurité : fermer la session précédente si elle est encore ouverte
        if (isOpen) {
            Log.w(TAG, "openSession appelé sur session déjà ouverte, fermeture de la précédente")
            closeSession()
        }
        return try {
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            sessionFileName = "gopro_${sdf.format(Date(sessionStartTime))}.gpx"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                openViaMediaStore()
            } else {
                openViaDirectFile()
            }
        } catch (e: Exception) {
            Log.e(TAG, "openSession error: ${e.message}")
            false
        }
    }

    private fun openViaMediaStore(): Boolean {
        val cv = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, sessionFileName)
            put(MediaStore.Files.FileColumns.MIME_TYPE, "application/gpx+xml")
            put(MediaStore.Files.FileColumns.RELATIVE_PATH, "Documents/$FOLDER_NAME")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Files.FileColumns.IS_PENDING, 1)
            }
        }
        val uri = context.contentResolver.insert(
            MediaStore.Files.getContentUri("external"), cv
        ) ?: run {
            Log.e(TAG, "MediaStore insert failed")
            return false
        }
        mediaStoreUri = uri
        outputStream = context.contentResolver.openOutputStream(uri) ?: run {
            Log.e(TAG, "Cannot open OutputStream via MediaStore")
            return false
        }
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
        waypointCount = 0
        gpsWaypointCount = 0
        writer = BufferedWriter(OutputStreamWriter(outputStream!!, Charsets.UTF_8))
        writeHeader()
        Log.d(TAG, "GPX session opened: $sessionFileName")
    }

    private fun writeHeader() {
        writer!!.write(
            """<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.1" creator="GoProPro"
     xmlns="http://www.topografix.com/GPX/1/1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
"""
        )
        writer!!.flush()
    }

    /**
     * Ajoute un waypoint au fichier GPX.
     *
     * @param name              Nom du waypoint, ex. "HILIGHT #1", "REC_START #1"
     * @param location          Position GPS au moment de l'événement (null = pas de fix)
     * @param timestamp         Timestamp de l'événement (ms depuis epoch)
     * @param recordingStartMs  Timestamp de début d'enregistrement pour le T+ (null si hors record)
     */
    fun addWaypoint(
        name: String,
        location: Location?,
        timestamp: Long,
        recordingStartMs: Long?
    ) {
        val w = writer ?: return
        try {
            val hasGps = location != null && (location.latitude != 0.0 || location.longitude != 0.0)
            val lat = location?.latitude ?: 0.0
            val lon = location?.longitude ?: 0.0
            val alt = location?.altitude ?: 0.0
            val speedKmh = if (location?.hasSpeed() == true) location.speed * 3.6f else 0f
            val acc = if (location?.hasAccuracy() == true) location.accuracy else -1f

            // Description : "2024-02-20 14:23:05 | T+02:34 | 145 km/h | acc: 4m"
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
            val desc = descParts.joinToString(" | ")

            val wptName = if (hasGps) name else "$name (no GPS)"
            val isoTime = isoUtc(timestamp)

            w.write(
                """  <wpt lat="${String.format(Locale.US, "%.8f", lat)}" lon="${String.format(Locale.US, "%.8f", lon)}">
    <ele>${String.format(Locale.US, "%.1f", alt)}</ele>
    <time>$isoTime</time>
    <name>$wptName</name>
    <desc>$desc</desc>
  </wpt>
"""
            )
            w.flush()
            waypointCount++
            if (hasGps) gpsWaypointCount++
            Log.d(TAG, "Waypoint: $wptName @ $desc")
        } catch (e: Exception) {
            Log.e(TAG, "addWaypoint error: ${e.message}")
        }
    }

    fun closeSession() {
        val count = waypointCount
        val gpsCount = gpsWaypointCount
        val uriToDelete = mediaStoreUri
        val fileToDelete = lastDirectFile
        try {
            writer?.write("</gpx>\n")
            writer?.flush()
            writer?.close()
            outputStream?.close()
            Log.d(TAG, "GPX session closed: $sessionFileName ($count waypoints)")
        } catch (e: Exception) {
            Log.e(TAG, "closeSession error: ${e.message}")
        } finally {
            writer = null
            outputStream = null
            mediaStoreUri = null
            lastDirectFile = null
            sessionFileName = ""
            waypointCount = 0
            gpsWaypointCount = 0
        }
        // Garder uniquement si au moins 1 waypoint a des coordonnées GPS valides
        val shouldDelete = gpsCount == 0
        if (uriToDelete != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (shouldDelete) {
                val deleted = context.contentResolver.delete(uriToDelete, null, null)
                Log.d(TAG, "GPX supprimé (0 GPS, $count waypoints no-GPS, deleted=$deleted)")
            } else {
                val cv = ContentValues().apply { put(MediaStore.Files.FileColumns.IS_PENDING, 0) }
                context.contentResolver.update(uriToDelete, cv, null, null)
                Log.d(TAG, "GPX finalisé ($gpsCount/$count waypoints avec GPS)")
            }
        } else if (shouldDelete) {
            fileToDelete?.delete()
            Log.d(TAG, "GPX supprimé (0 GPS, direct file)")
        }
    }

    val isOpen: Boolean get() = writer != null

    private fun isoUtc(millis: Long): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(millis))
    }
}
