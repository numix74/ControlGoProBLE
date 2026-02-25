package com.ximun.gopropro.gps

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
 * Gère le suivi GPS (FusedLocationProviderClient) et la session GPX.
 *
 * Cycle de vie :
 *  - startSession()       : ouvre le fichier GPX, démarre les mises à jour GPS
 *  - onRecordingStarted() : waypoint REC_START #N
 *  - onHilight()          : waypoint HILIGHT #N (si en cours d'enregistrement)
 *  - onRecordingStopped() : waypoint REC_STOP #N (si pas de HILIGHT dans le clip)
 *  - endSession()         : ferme le fichier GPX, arrête le GPS
 *
 * Callback [onWaypointAdded] appelé à chaque waypoint → met à jour le compteur UI.
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

    // État GPS
    @Volatile private var currentLocation: Location? = null
    private var locationThread: HandlerThread? = null

    // État de la session
    private var sessionStartMs: Long = 0L
    private var recordingStartMs: Long? = null

    // Compteurs de waypoints
    private var hilightCount = 0
    private var recStartCount = 0
    private var recStopCount = 0

    // Flag : le clip courant a-t-il déjà eu un HILIGHT ?
    private var currentClipHasHilight = false

    // ── Location callback ────────────────────────────────────────────

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

    // ── API publique ─────────────────────────────────────────────────

    /** Démarre la session : ouvre le fichier GPX + active les mises à jour GPS. */
    @Suppress("MissingPermission")
    fun startSession(sessionStartMs: Long) {
        this.sessionStartMs = sessionStartMs
        hilightCount = 0
        recStartCount = 0
        recStopCount = 0
        currentClipHasHilight = false
        recordingStartMs = null

        if (gpxWriter.openSession(sessionStartMs)) {
            try {
                locationThread?.quit()
                locationThread = HandlerThread("GpsTrackerThread").also { it.start() }
                fusedClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    locationThread!!.looper
                )
                Log.d(TAG, "Session GPS démarrée")
            } catch (e: Exception) {
                Log.e(TAG, "requestLocationUpdates error: ${e.message}")
            }
        } else {
            Log.e(TAG, "Impossible d'ouvrir le fichier GPX")
        }
    }

    /** Appelé quand l'enregistrement GoPro démarre. */
    fun onRecordingStarted(timestamp: Long) {
        recStartCount++
        recordingStartMs = timestamp
        currentClipHasHilight = false
        Log.d(TAG, "onRecordingStarted #$recStartCount")

        gpxWriter.addWaypoint(
            name = "REC_START #$recStartCount",
            location = currentLocation,
            timestamp = timestamp,
            recordingStartMs = recordingStartMs
        )
        onWaypointAdded()
    }

    /** Appelé quand le bouton HiLight est pressé (pendant un enregistrement). */
    fun onHilight(timestamp: Long) {
        hilightCount++
        currentClipHasHilight = true
        Log.d(TAG, "onHilight #$hilightCount")

        gpxWriter.addWaypoint(
            name = "HILIGHT #$hilightCount",
            location = currentLocation,
            timestamp = timestamp,
            recordingStartMs = recordingStartMs
        )
        onWaypointAdded()
    }

    /** Appelé quand l'enregistrement GoPro s'arrête. */
    fun onRecordingStopped(timestamp: Long) {
        // REC_STOP uniquement si aucun HILIGHT dans ce clip
        if (!currentClipHasHilight) {
            recStopCount++
            Log.d(TAG, "onRecordingStopped #$recStopCount (pas de HILIGHT)")
            gpxWriter.addWaypoint(
                name = "REC_STOP #$recStopCount",
                location = currentLocation,
                timestamp = timestamp,
                recordingStartMs = recordingStartMs
            )
            onWaypointAdded()
        } else {
            Log.d(TAG, "onRecordingStopped ignoré (clip a déjà un HILIGHT)")
        }
        recordingStartMs = null
        currentClipHasHilight = false
    }

    /** Ferme la session GPX et arrête le GPS. Appelé à la déconnexion GoPro. */
    fun endSession() {
        fusedClient.removeLocationUpdates(locationCallback)
        locationThread?.quit()
        locationThread = null
        currentLocation = null
        if (gpxWriter.isOpen) {
            gpxWriter.closeSession()
        }
        Log.d(TAG, "Session GPS terminée")
    }
}
