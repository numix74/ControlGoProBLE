package com.actioncam.airbuble.insta360

import com.arashivision.sdkcamera.camera.model.CaptureMode as SdkCaptureMode

/** Display name helpers for SDK enums — never imported in non-Insta360 code. */
object Insta360SettingsMappings {

    fun modeDisplayName(mode: SdkCaptureMode): String = when (mode.name) {
        "RECORD_NORMAL"       -> "Vidéo normale"
        "CAPTURE_NORMAL"      -> "Photo"
        "TIMELAPSE"           -> "Timelapse"
        "HDR_RECORD"          -> "Vidéo HDR"
        "HDR_CAPTURE"         -> "Photo HDR"
        "BULLETTIME"          -> "Bullet Time"
        "TIME_SHIFT"          -> "Time Shift"
        "SLOW_MOTION"         -> "Ralenti"
        "BURST"               -> "Rafale"
        "NIGHT_SCENE"         -> "Nuit"
        "LOOPER_RECORDING"    -> "Boucle"
        "INTERVAL_SHOOTING"   -> "Intervalomètre"
        "STARLAPSE_SHOOTING"  -> "Star Lapse"
        "SUPER_RECORD"        -> "Super Vidéo"
        "SELFIE_RECORD"       -> "Selfie"
        "PURE_RECORD"         -> "Pure Vidéo"
        else -> mode.name.split("_")
            .joinToString(" ") { w -> w.lowercase().replaceFirstChar { it.uppercase() } }
    }

    fun settingLabel(settingId: String): String = when (settingId) {
        "RECORD_RESOLUTION" -> "Résolution vidéo"
        "PHOTO_RESOLUTION"  -> "Résolution photo"
        "ISO"               -> "ISO"
        "EV"                -> "Compensation EV"
        "WB"                -> "Balance des blancs"
        "SHUTTER"           -> "Vitesse d'obturation"
        "GAMMA_MODE"        -> "Mode gamma"
        "INTERVAL"          -> "Intervalle"
        else -> settingId.split("_")
            .joinToString(" ") { w -> w.lowercase().replaceFirstChar { it.uppercase() } }
    }
}
