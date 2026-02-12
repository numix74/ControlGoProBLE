package com.ximun.gopropro

/**
 * Mappings des Settings GoPro - Basé sur la documentation officielle OpenGoPro
 * Source: https://gopro.github.io/OpenGoPro/ble/features/settings.html
 * Dernière mise à jour depuis la doc officielle complète
 */
object GoProSettingsMappings {

    // Resolution (Setting ID 2)
    private val RESOLUTION_LABELS = mapOf(
        1 to "4K",
        4 to "2.7K",
        6 to "2.7K 4:3",
        7 to "1440",
        9 to "1080",
        12 to "720",
        18 to "4K 4:3",
        21 to "5.6K",
        24 to "5K",
        25 to "5K 4:3",
        26 to "5.3K 8:7",
        27 to "5.3K 4:3",
        28 to "4K 8:7",
        31 to "8K",
        35 to "5.3K 21:9",
        36 to "4K 21:9",
        37 to "4K 1:1",
        38 to "900",
        39 to "4K SPH",
        100 to "5.3K",
        107 to "5.3K 8:7 V2",
        108 to "4K 8:7 V2",
        109 to "4K 9:16 V2",
        110 to "1080 9:16 V2",
        111 to "2.7K 4:3 V2",
        112 to "4K 4:3 V2",
        113 to "5.3K 4:3 V2"
    )

    // FPS (Setting ID 3)
    private val FPS_LABELS = mapOf(
        0 to "240",
        1 to "120",
        2 to "100",
        3 to "90",
        5 to "60",
        6 to "50",
        8 to "30",
        9 to "25",
        10 to "24",
        13 to "200",
        15 to "400",
        16 to "360",
        17 to "300"
    )

    // Video Timelapse Rate (Setting ID 5)
    private val TIMELAPSE_RATE_LABELS = mapOf(
        0 to "0.5s",
        1 to "1s",
        2 to "2s",
        3 to "5s",
        4 to "10s",
        5 to "30s",
        6 to "60s",
        7 to "2 min",
        8 to "5 min",
        9 to "30 min",
        10 to "60 min",
        11 to "3s"
    )

    // Photo Timelapse Rate (Setting ID 30)
    private val PHOTO_TIMELAPSE_RATE_LABELS = mapOf(
        11 to "3s",
        100 to "60 min",
        101 to "30 min",
        102 to "5 min",
        103 to "2 min",
        104 to "60s",
        105 to "30s",
        106 to "10s",
        107 to "5s",
        108 to "2s",
        109 to "1s",
        110 to "0.5s"
    )

    // Nightlapse Rate (Setting ID 32)
    private val NIGHT_LAPSE_RATE_LABELS = mapOf(
        4 to "4s",
        5 to "5s",
        10 to "10s",
        15 to "15s",
        20 to "20s",
        30 to "30s",
        100 to "60s",
        120 to "2 min",
        300 to "5 min",
        1800 to "30 min",
        3600 to "60 min",
        3601 to "Auto"
    )

    // Auto Power Down (Setting ID 59)
    private val AUTO_POWER_DOWN_LABELS = mapOf(
        0 to "Jamais",
        1 to "1 Min",
        4 to "5 Min",
        6 to "15 Min",
        7 to "30 Min",
        11 to "8 Sec",
        12 to "30 Sec"
    )

    // GPS (Setting ID 83)
    private val GPS_LABELS = mapOf(
        0 to "Désactivé",
        1 to "Activé"
    )

    // LCD Brightness (Setting ID 88)
    private val LCD_BRIGHTNESS_LABELS = mapOf(
        10 to "10%",
        20 to "20%",
        30 to "30%",
        40 to "40%",
        50 to "50%",
        60 to "60%",
        70 to "70%",
        80 to "80%",
        90 to "90%",
        100 to "100%"
    )

    // LED (Setting ID 91)
    private val LED_LABELS = mapOf(
        0 to "Off",
        2 to "On",
        3 to "All On",
        4 to "All Off",
        5 to "Front Off Only",
        100 to "Back Only"
    )

    // Video Aspect Ratio (Setting ID 108)
    private val ASPECT_RATIO_LABELS = mapOf(
        0 to "4:3",
        1 to "16:9",
        3 to "8:7",
        4 to "9:16",
        5 to "21:9",
        6 to "1:1"
    )

    // TimeWarp Speed (Setting ID 111)
    private val TIMEWARP_SPEED_LABELS = mapOf(
        0 to "15x",
        1 to "30x",
        2 to "60x",
        3 to "150x",
        4 to "300x",
        5 to "900x",
        6 to "1800x",
        7 to "2x",
        8 to "5x",
        9 to "10x",
        10 to "Auto",
        11 to "1x",
        12 to "1/2x"
    )

    // Video Lens (Setting ID 121)
    // Note: Sur HERO11 Mini, l'Horizon Leveling est intégré dans le Lens (pas de setting 150 séparé)
    // Valeurs 8 et 10 = Linear avec stabilisation horizon
    private val LENS_LABELS = mapOf(
        0 to "Wide",
        2 to "Narrow",
        3 to "SuperView",
        4 to "Linear",
        7 to "Max SuperView",
        8 to "Linear + Horizon",
        9 to "HyperView",
        10 to "Linear + Horizon Lock",
        11 to "Max HyperView",
        12 to "Ultra SuperView",
        13 to "Ultra Wide",
        14 to "Ultra Linear",
        104 to "Ultra HyperView"
    )

    // Photo Lens (Setting ID 122)
    private val PHOTO_LENS_LABELS = mapOf(
        0 to "Wide 12 MP",
        10 to "Linear 12 MP",
        15 to "9MP Wide",
        19 to "Narrow",
        27 to "Wide 23 MP",
        28 to "Linear 23 MP",
        31 to "Wide 27 MP",
        32 to "Linear 27 MP",
        37 to "9MP Linear",
        38 to "13MP Linear",
        39 to "13MP Wide",
        40 to "13MP Ultra Wide",
        41 to "Ultra Wide 12 MP",
        44 to "13MP Ultra Linear",
        100 to "Max SuperView",
        101 to "Wide",
        102 to "Linear"
    )

    // Timelapse Lens (Setting ID 123)
    private val TIMELAPSE_LENS_LABELS = mapOf(
        19 to "Narrow",
        31 to "Wide 27 MP",
        32 to "Linear 27 MP",
        100 to "Max SuperView",
        101 to "Wide",
        102 to "Linear"
    )

    // Photo Output (Setting ID 125)
    private val PHOTO_OUTPUT_LABELS = mapOf(
        0 to "Standard",
        1 to "Raw",
        2 to "HDR",
        3 to "SuperPhoto"
    )

    // Media Format (Setting ID 128)
    private val MEDIA_FORMAT_LABELS = mapOf(
        13 to "Vidéo Accéléré",
        20 to "Photo Accéléré",
        21 to "Photo Nuit Accéléré",
        26 to "Vidéo Nuit Accéléré"
    )

    // Anti-Flicker (Setting ID 134)
    private val ANTI_FLICKER_LABELS = mapOf(
        0 to "NTSC",
        1 to "PAL",
        2 to "60 Hz",
        3 to "50 Hz"
    )

    // HyperSmooth (Setting ID 135)
    private val HYPERSMOOTH_LABELS = mapOf(
        0 to "Off",
        1 to "Low",
        2 to "High",
        3 to "Boost",
        4 to "Auto Boost",
        100 to "Standard"
    )

    // Video Horizon Leveling (Setting ID 150)
    private val VIDEO_HORIZON_LEVELING_LABELS = mapOf(
        0 to "Off",
        2 to "Verrouillé"
    )

    // Photo Horizon Leveling (Setting ID 151)
    private val PHOTO_HORIZON_LEVELING_LABELS = mapOf(
        0 to "Off",
        2 to "Verrouillé"
    )

    // Hindsight (Setting ID 167)
    private val HINDSIGHT_LABELS = mapOf(
        0 to "Off",
        1 to "15 Sec",
        2 to "30 Sec"
    )

    // Photo Single Interval (Setting ID 171)
    private val PHOTO_SINGLE_INTERVAL_LABELS = mapOf(
        0 to "Off",
        2 to "0.5s",
        3 to "1s",
        4 to "2s",
        5 to "5s",
        6 to "10s",
        7 to "30s",
        8 to "60s",
        9 to "120s",
        10 to "3s"
    )

    // Photo Interval Duration (Setting ID 172)
    private val PHOTO_INTERVAL_DURATION_LABELS = mapOf(
        0 to "Off",
        1 to "15 Sec",
        2 to "30 Sec",
        3 to "1 Min",
        4 to "5 Min",
        5 to "15 Min",
        6 to "30 Min",
        7 to "1 Heure",
        8 to "2 Heures",
        9 to "3 Heures"
    )

    // Video Performance Mode (Setting ID 173)
    private val VIDEO_PERFORMANCE_MODE_LABELS = mapOf(
        0 to "Perf. Max",
        1 to "Batterie étendue",
        2 to "Trépied / Stationnaire"
    )

    // Control Mode (Setting ID 175)
    private val CONTROL_MODE_LABELS = mapOf(
        0 to "Easy",
        1 to "Pro"
    )

    // Easy Mode Speed (Setting ID 176) - Simplifié pour les captions
    private val EASY_MODE_SPEED_LABELS = mapOf(
        0 to "8X Ultra Slo-Mo",
        1 to "4X Super Slo-Mo",
        2 to "2X Slo-Mo",
        3 to "1X (Low Light)",
        4 to "4X Super Slo-Mo (Ext.)",
        5 to "2X Slo-Mo (Ext.)",
        6 to "1X (Ext.) (Low Light)",
        7 to "8X Ultra Slo-Mo (50Hz)",
        8 to "4X Super Slo-Mo (50Hz)",
        9 to "2X Slo-Mo (50Hz)",
        10 to "1X (50Hz) (Low Light)",
        11 to "4X Super Slo-Mo (50Hz) (Ext.)",
        12 to "2X Slo-Mo (50Hz) (Ext.)",
        13 to "1X (50Hz) (Ext.) (Low Light)",
        14 to "8X Ultra Slo-Mo (Ext.)",
        15 to "8X Ultra Slo-Mo (50Hz) (Ext.)",
        16 to "8X Ultra Slo-Mo (Long.)",
        17 to "4X Super Slo-Mo (Long.)",
        18 to "2X Slo-Mo (Long.)",
        19 to "1X (Long.) (Low Light)",
        20 to "8X Ultra Slo-Mo (50Hz) (Long.)",
        21 to "4X Super Slo-Mo (50Hz) (Long.)",
        22 to "2X Slo-Mo (50Hz) (Long.)",
        23 to "1X (50Hz) (Long.) (Low Light)",
        24 to "2X Slo-Mo (4K)",
        25 to "4X Super Slo-Mo (2.7K)",
        26 to "2X Slo-Mo (4K) (50Hz)",
        27 to "4X Super Slo-Mo (2.7K) (50Hz)"
    )

    // Enable Night Photo (Setting ID 177)
    private val NIGHT_PHOTO_LABELS = mapOf(
        0 to "Off",
        1 to "On"
    )

    // Wireless Band (Setting ID 178)
    private val WIRELESS_BAND_LABELS = mapOf(
        0 to "2.4GHz",
        1 to "5GHz"
    )

    // Star Trails Length (Setting ID 179)
    private val STAR_TRAILS_LENGTH_LABELS = mapOf(
        1 to "Court",
        2 to "Long",
        3 to "Max"
    )

    // System Video Mode (Setting ID 180)
    private val SYSTEM_VIDEO_MODE_LABELS = mapOf(
        0 to "Qualité max",
        101 to "Batterie étendue",
        102 to "Batterie longue",
        111 to "Qualité standard",
        112 to "Qualité basique"
    )

    // Video Bit Rate (Setting ID 182)
    private val BIT_RATE_LABELS = mapOf(
        0 to "Standard",
        1 to "High"
    )

    // Bit Depth (Setting ID 183)
    private val BIT_DEPTH_LABELS = mapOf(
        0 to "8-bit",
        2 to "10-bit"
    )

    // Profiles (Setting ID 184)
    private val VIDEO_PROFILE_LABELS = mapOf(
        0 to "Standard",
        1 to "HDR",
        2 to "Log",
        101 to "HLG HDR"
    )

    // Video Easy Mode (Setting ID 186)
    private val VIDEO_EASY_MODE_LABELS = mapOf(
        0 to "Qualité max",
        1 to "Qualité standard",
        2 to "Qualité basique",
        3 to "Vidéo standard",
        4 to "Vidéo HDR"
    )

    // Lapse Mode (Setting ID 187)
    private val LAPSE_MODE_LABELS = mapOf(
        0 to "TimeWarp",
        1 to "Filés d'étoiles",
        2 to "Light Painting",
        3 to "Feux de véhicules",
        4 to "Max TimeWarp",
        5 to "Max Filés d'étoiles",
        6 to "Max Light Painting",
        7 to "Max Feux de véhicules",
        8 to "Vidéo Accéléré",
        9 to "Vidéo Nuit Accéléré"
    )

    // Max Lens Mod (Setting ID 189)
    private val MAX_LENS_MOD_LABELS = mapOf(
        0 to "Aucun",
        1 to "Max Lens 1.0",
        2 to "Max Lens 2.0",
        3 to "Max Lens 2.5",
        4 to "Macro",
        5 to "Anamorphique",
        6 to "ND 4",
        7 to "ND 8",
        8 to "ND 16",
        9 to "ND 32",
        10 to "Objectif standard",
        100 to "Détection auto"
    )

    // Max Lens Mod Enable (Setting ID 190)
    private val MAX_LENS_MOD_ENABLE_LABELS = mapOf(
        0 to "Off",
        1 to "On"
    )

    // Easy Night Photo (Setting ID 191)
    private val EASY_NIGHT_PHOTO_LABELS = mapOf(
        0 to "Super Photo",
        1 to "Photo Nuit",
        2 to "Rafale"
    )

    // Multi Shot Aspect Ratio (Setting ID 192)
    private val MULTI_SHOT_ASPECT_RATIO_LABELS = mapOf(
        0 to "4:3",
        1 to "16:9",
        3 to "8:7",
        4 to "9:16"
    )

    // Framing (Setting ID 193)
    private val FRAMING_LABELS = mapOf(
        0 to "Widescreen",
        1 to "Vertical",
        2 to "Full Frame",
        100 to "4:3 Trad.",
        101 to "16:9",
        103 to "8:7 Full",
        104 to "9:16 Vert.",
        105 to "21:9 Ultra Wide",
        106 to "1:1"
    )

    // Camera Mode (Setting ID 194)
    private val CAMERA_MODE_LABELS = mapOf(
        0 to "Objectif simple",
        1 to "360°"
    )

    // Beep Volume (Setting ID 216)
    private val BEEP_VOLUME_LABELS = mapOf(
        70 to "Faible",
        85 to "Moyen",
        100 to "Fort"
    )

    // Setup Screen Saver (Setting ID 219)
    private val SCREEN_SAVER_LABELS = mapOf(
        0 to "Jamais",
        1 to "1 Min",
        2 to "2 Min",
        3 to "3 Min",
        4 to "5 Min"
    )

    // Setup Language (Setting ID 223)
    private val LANGUAGE_LABELS = mapOf(
        0 to "English (US)",
        1 to "English (UK)",
        2 to "English (AUS)",
        3 to "Deutsch",
        4 to "Français",
        5 to "Italiano",
        6 to "Español",
        7 to "Español (NA)",
        8 to "中文",
        9 to "日本語",
        10 to "한국어",
        11 to "Português",
        12 to "Русский",
        13 to "English (IND)",
        14 to "Svenska"
    )

    // Photo Mode (Setting ID 227)
    private val PHOTO_MODE_LABELS = mapOf(
        0 to "SuperPhoto",
        1 to "Photo Nuit",
        2 to "Rafale"
    )

    // Video Framing (Setting ID 232)
    private val VIDEO_FRAMING_LABELS = mapOf(
        0 to "4:3",
        1 to "16:9",
        3 to "8:7",
        4 to "9:16",
        5 to "21:9",
        6 to "1:1"
    )

    // Multi Shot Framing (Setting ID 233)
    private val MULTI_SHOT_FRAMING_LABELS = mapOf(
        0 to "4:3",
        1 to "16:9",
        3 to "8:7",
        4 to "9:16"
    )

    // Frame Rate (Setting ID 234) - Même valeurs que FPS (ID 3)
    private val FRAME_RATE_LABELS = mapOf(
        0 to "240",
        1 to "120",
        2 to "100",
        3 to "90",
        5 to "60",
        6 to "50",
        8 to "30",
        9 to "25",
        10 to "24",
        13 to "200",
        15 to "400",
        16 to "360",
        17 to "300"
    )

    /**
     * Retourne le label pour une valeur donnée d'un setting
     * Si la valeur n'existe pas dans le mapping, retourne la valeur brute
     */
    fun getLabel(settingId: Int, value: Int): String {
        val map = when (settingId) {
            2 -> RESOLUTION_LABELS
            3 -> FPS_LABELS
            5 -> TIMELAPSE_RATE_LABELS
            30 -> PHOTO_TIMELAPSE_RATE_LABELS
            32 -> NIGHT_LAPSE_RATE_LABELS
            59 -> AUTO_POWER_DOWN_LABELS
            83 -> GPS_LABELS
            88 -> LCD_BRIGHTNESS_LABELS
            91 -> LED_LABELS
            108 -> ASPECT_RATIO_LABELS
            111 -> TIMEWARP_SPEED_LABELS
            121 -> LENS_LABELS
            122 -> PHOTO_LENS_LABELS
            123 -> TIMELAPSE_LENS_LABELS
            125 -> PHOTO_OUTPUT_LABELS
            128 -> MEDIA_FORMAT_LABELS
            134 -> ANTI_FLICKER_LABELS
            135 -> HYPERSMOOTH_LABELS
            150 -> VIDEO_HORIZON_LEVELING_LABELS
            151 -> PHOTO_HORIZON_LEVELING_LABELS
            167 -> HINDSIGHT_LABELS
            171 -> PHOTO_SINGLE_INTERVAL_LABELS
            172 -> PHOTO_INTERVAL_DURATION_LABELS
            173 -> VIDEO_PERFORMANCE_MODE_LABELS
            175 -> CONTROL_MODE_LABELS
            176 -> EASY_MODE_SPEED_LABELS
            177 -> NIGHT_PHOTO_LABELS
            178 -> WIRELESS_BAND_LABELS
            179 -> STAR_TRAILS_LENGTH_LABELS
            180 -> SYSTEM_VIDEO_MODE_LABELS
            182 -> BIT_RATE_LABELS
            183 -> BIT_DEPTH_LABELS
            184 -> VIDEO_PROFILE_LABELS
            186 -> VIDEO_EASY_MODE_LABELS
            187 -> LAPSE_MODE_LABELS
            189 -> MAX_LENS_MOD_LABELS
            190 -> MAX_LENS_MOD_ENABLE_LABELS
            191 -> EASY_NIGHT_PHOTO_LABELS
            192 -> MULTI_SHOT_ASPECT_RATIO_LABELS
            193 -> FRAMING_LABELS
            194 -> CAMERA_MODE_LABELS
            216 -> BEEP_VOLUME_LABELS
            219 -> SCREEN_SAVER_LABELS
            223 -> LANGUAGE_LABELS
            227 -> PHOTO_MODE_LABELS
            232 -> VIDEO_FRAMING_LABELS
            233 -> MULTI_SHOT_FRAMING_LABELS
            234 -> FRAME_RATE_LABELS
            else -> null
        }

        return map?.get(value) ?: "$value"
    }

    /**
     * Vérifie si un setting ID est connu (a un mapping défini)
     * Utilisé pour distinguer les valeurs brutes légitimes des settings inconnus
     */
    fun isKnownSetting(settingId: Int): Boolean = when (settingId) {
        2, 3, 5, 30, 32, 59, 83, 88, 91, 108, 111, 121, 122, 123, 125, 128,
        134, 135, 150, 151, 167, 171, 172, 173, 175, 176, 177, 178, 179, 180,
        182, 183, 184, 186, 187, 189, 190, 191, 192, 193, 194, 216, 219, 223,
        227, 232, 233, 234 -> true
        else -> false
    }

    /**
     * Retourne les options disponibles basées sur les capacités
     */
    fun getAvailableOptions(settingId: Int, capabilities: List<Int>): List<Pair<Int, String>> {
        return capabilities.map { value ->
            val label = getLabel(settingId, value)
            value to label
        }
    }

    /**
     * Retourne le nom du setting pour l'affichage
     */
    fun getSettingName(settingId: Int): String {
        return when (settingId) {
            2 -> "Résolution"
            3 -> "FPS"
            5 -> "Intervalle Timelapse"
            30 -> "Intervalle Photo Timelapse"
            32 -> "Intervalle Nuit Accéléré"
            59 -> "Extinction auto"
            83 -> "GPS"
            88 -> "Luminosité LCD"
            91 -> "LEDs"
            108 -> "Ratio d'aspect"
            111 -> "Vitesse TimeWarp"
            121 -> "Objectif"
            122 -> "Objectif Photo"
            123 -> "Objectif Timelapse"
            125 -> "Sortie Photo"
            128 -> "Format Média"
            134 -> "Anti-Flicker"
            135 -> "HyperSmooth"
            150 -> "Maintien de l'horizon"
            151 -> "Horizon (Photo)"
            167 -> "Hindsight"
            171 -> "Intervalle Photo"
            172 -> "Durée Intervalle"
            173 -> "Mode Performance"
            175 -> "Mode de contrôle"
            176 -> "Vitesse Easy Mode"
            177 -> "Photo Nuit"
            178 -> "Bande WiFi"
            179 -> "Longueur Filés"
            180 -> "Mode Vidéo Système"
            182 -> "Débit"
            183 -> "Profondeur de bits"
            184 -> "Profil vidéo"
            186 -> "Mode Vidéo Easy"
            187 -> "Mode Timelapse"
            189 -> "Mod Objectif Max"
            190 -> "Mod Objectif Max (actif)"
            191 -> "Photo Nuit Easy"
            192 -> "Ratio Multi-Shot"
            193 -> "Cadrage"
            194 -> "Mode Caméra"
            216 -> "Volume Bip"
            219 -> "Économiseur d'écran"
            223 -> "Langue"
            227 -> "Mode Photo"
            232 -> "Cadrage Vidéo"
            233 -> "Cadrage Multi-Shot"
            234 -> "Fréquence d'images"
            else -> "Setting $settingId"
        }
    }
}
