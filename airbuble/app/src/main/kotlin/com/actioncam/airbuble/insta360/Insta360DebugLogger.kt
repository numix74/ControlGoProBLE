package com.actioncam.airbuble.insta360

import android.util.Log
import com.arashivision.sdkcamera.camera.InstaCameraManager
import com.arashivision.sdkcamera.camera.model.CaptureMode as SdkCaptureMode
import com.arashivision.sdkcamera.camera.model.TemperatureLevel

/**
 * Dump complet des données SDK vers logcat pour calibrage de l'émulateur.
 *
 * Tag logcat : "Insta360Debug"
 * Récupérer avec : adb logcat -s Insta360Debug > x3_capture.log
 *
 * Format des lignes :
 *   [INFO]    cameraType=<str> firmware=<str> serial=<str>
 *   [CONFIG]  modeCount=<int>
 *   [MODE]    name=<str> isVideo=<bool> isPhoto=<bool> isLive=<bool>
 *   [SETTING] mode=<str> key=<str> count=<int> values=<list>
 *   [BATTERY] level=<int> charging=<bool>
 *   [STORAGE] free=<long> total=<long>
 *   [TEMP]    level=<str>
 *
 * Utilisation : appelé automatiquement dès la connexion à la caméra.
 * Désactiver en production en passant enabled=false dans dumpAll().
 */
object Insta360DebugLogger {

    private const val TAG = "Insta360Debug"

    /** Dump infos caméra (firmware, type, n° série) — appeler depuis onCameraStatusChanged CONNECTED. */
    fun dumpCameraInfo(sdk: InstaCameraManager) {
        Log.i(TAG, "[INFO] cameraType=${safeGet { sdk.cameraType }} " +
                "firmware=${safeGet { sdk.cameraVersion }} " +
                "serial=${safeGet { sdk.cameraSerial }}")
    }

    /** Dump liste des modes + settings par mode — appeler depuis initCameraSupportConfig.onComplete(). */
    fun dumpCaptureConfig(sdk: InstaCameraManager) {
        val modes: List<SdkCaptureMode> = try {
            sdk.supportCaptureMode ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
        Log.i(TAG, "[CONFIG] modeCount=${modes.size}")
        for (mode in modes) {
            Log.i(TAG, "[MODE] name=${mode.name} isVideo=${mode.isVideoMode} " +
                    "isPhoto=${mode.isPhotoMode} isLive=${mode.isLiveMode}")
            dumpSettingsForMode(sdk, mode)
        }
    }

    private fun dumpSettingsForMode(sdk: InstaCameraManager, mode: SdkCaptureMode) {
        val settings = try {
            sdk.getSupportCaptureSettingList(mode) ?: return
        } catch (_: Exception) {
            return
        }
        for (cs in settings) {
            val values: List<*> = try {
                when (cs.name) {
                    "RECORD_RESOLUTION" -> sdk.getSupportRecordResolutionList(mode)
                    "PHOTO_RESOLUTION"  -> sdk.getSupportPhotoResolutionList(mode)
                    "ISO"               -> sdk.getSupportISOList(mode)
                    "EV"                -> sdk.getSupportEVList(mode)
                    "WB"                -> sdk.getSupportWBList(mode)
                    "SHUTTER"           -> sdk.getSupportShutterList(mode)
                    "GAMMA_MODE"        -> sdk.getSupportGammaModeList(mode)
                    "INTERVAL"          -> sdk.getSupportIntervalList(mode)
                    else                -> emptyList<Any>()
                }
            } catch (_: Exception) {
                emptyList<Any>()
            }
            Log.i(TAG, "[SETTING] mode=${mode.name} key=${cs.name} count=${values.size} values=$values")
        }
    }

    /** Appeler depuis onCameraBatteryUpdate. */
    fun dumpBattery(level: Int, isCharging: Boolean) {
        Log.i(TAG, "[BATTERY] level=$level charging=$isCharging")
    }

    /** Appeler depuis onCameraStorageChanged. */
    fun dumpStorage(freeSpace: Long, totalSpace: Long) {
        Log.i(TAG, "[STORAGE] free=$freeSpace total=$totalSpace")
    }

    /** Appeler depuis onCameraTemperatureChanged. */
    fun dumpTemperature(tempLevel: TemperatureLevel?) {
        Log.i(TAG, "[TEMP] level=${tempLevel?.name ?: "null"}")
    }

    private inline fun safeGet(block: () -> String?): String =
        try { block() ?: "" } catch (_: Exception) { "" }
}
