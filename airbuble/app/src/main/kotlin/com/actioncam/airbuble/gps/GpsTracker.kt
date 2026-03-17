package com.actioncam.airbuble.gps

import android.content.Context
import android.location.Location
import android.os.HandlerThread
import android.util.Log
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

/**
 * Suivi GPS (FusedLocationProviderClient) + session GPX.
 *
 * Cycle de vie :
 *  startSession()       → ouvre le fichier GPX, démarre les mises à jour GPS
 *  onRecordingStarted() → waypoint REC_START #N
 *  onHilight()          → waypoint HILIGHT #N (pendant un enregistrement)
 *  onRecordingStopped() → waypoint REC_STOP #N
 *  endSession()         → ferme le fichier GPX, arrête le GPS
 */
class GpsTracker(
    private val context: Context,
    val onWaypointAdded: () -> Unit
) {
    companion object {
        private const val TAG = "GpsTracker"
    }

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private val gpxWriter = GpxWriter(context)

    @Volatile private var currentLocation: Location? = null
    private var locationThread: HandlerThread? = null

    private var sessionStartMs: Long = 0L
    private var recordingStartMs: Long? = null

    private var hilightCount = 0
    private var recStartCount = 0
    private var recStopCount = 0

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 2000L
    ).apply {
        setMinUpdateIntervalMillis(1000L)
        setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
    }.build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            currentLocation = result.lastLocation
        }
    }

    @Suppress("MissingPermission")
    fun startSession(sessionStartMs: Long) {
        this.sessionStartMs = sessionStartMs
        hilightCount = 0; recStartCount = 0; recStopCount = 0
        recordingStartMs = null

        if (gpxWriter.openSession(sessionStartMs)) {
            try {
                locationThread?.quit()
                locationThread = HandlerThread("GpsTrackerThread").also { it.start() }
                fusedClient.requestLocationUpdates(locationRequest, locationCallback, locationThread!!.looper)
                Log.d(TAG, "Session GPS démarrée")
            } catch (e: Exception) {
                Log.e(TAG, "requestLocationUpdates: ${e.message}")
            }
        } else {
            Log.e(TAG, "Impossible d'ouvrir le fichier GPX")
        }
    }

    fun onRecordingStarted(timestamp: Long) {
        recStartCount++
        recordingStartMs = timestamp
        Log.d(TAG, "onRecordingStarted #$recStartCount")
        gpxWriter.addWaypoint("REC_START #$recStartCount", currentLocation, timestamp, recordingStartMs)
        onWaypointAdded()
    }

    fun onHilight(timestamp: Long) {
        hilightCount++
        Log.d(TAG, "onHilight #$hilightCount")
        gpxWriter.addWaypoint("HILIGHT #$hilightCount", currentLocation, timestamp, recordingStartMs)
        onWaypointAdded()
    }

    fun onRecordingStopped(timestamp: Long) {
        recStopCount++
        Log.d(TAG, "onRecordingStopped #$recStopCount")
        gpxWriter.addWaypoint("REC_STOP #$recStopCount", currentLocation, timestamp, recordingStartMs)
        onWaypointAdded()
        recordingStartMs = null
    }

    fun endSession() {
        fusedClient.removeLocationUpdates(locationCallback)
        locationThread?.quit()
        locationThread = null
        currentLocation = null
        if (gpxWriter.isOpen) gpxWriter.closeSession()
        Log.d(TAG, "Session GPS terminée")
    }
}
