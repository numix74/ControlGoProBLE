package com.ximun.gopropro

/**
 * Mappings des Settings GoPro - Basé sur la documentation officielle OpenGoPro
 * Source: https://gopro.github.io/OpenGoPro/ble/features/settings.html
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
        0 to "240 fps",
        1 to "120 fps",
        2 to "100 fps",
        3 to "90 fps",
        5 to "60 fps",
        6 to "50 fps",
        8 to "30 fps",
        9 to "25 fps",
        10 to "24 fps",
        13 to "200 fps",
        15 to "400 fps",
        16 to "360 fps",
        17 to "300 fps"
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

    // Video Lens (Setting ID 121)
    private val LENS_LABELS = mapOf(
        0 to "Wide",
        2 to "Narrow",
        3 to "SuperView",
        4 to "Linear",
        7 to "Max SuperView",
        8 to "Linear + HL",
        9 to "HyperView",
        10 to "Linear + Lock",
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
        1 to "On",
        2 to "High",
        3 to "Boost"
    )

    // Hindsight (Setting ID 167)
    private val HINDSIGHT_LABELS = mapOf(
        0 to "Off",
        1 to "15 Sec",
        2 to "30 Sec"
    )

    // Video Bit Rate (Setting ID 182)
    private val BIT_RATE_LABELS = mapOf(
        0 to "Standard",
        1 to "High",
        2 to "Extended"
    )

    // Bit Depth (Setting ID 183)
    private val BIT_DEPTH_LABELS = mapOf(
        0 to "8-bit",
        1 to "10-bit"
    )

    // Profiles (Setting ID 184)
    private val VIDEO_PROFILE_LABELS = mapOf(
        0 to "Standard",
        1 to "Flat"
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
            121 -> LENS_LABELS
            122 -> PHOTO_LENS_LABELS
            134 -> ANTI_FLICKER_LABELS
            135 -> HYPERSMOOTH_LABELS
            167 -> HINDSIGHT_LABELS
            182 -> BIT_RATE_LABELS
            183 -> BIT_DEPTH_LABELS
            184 -> VIDEO_PROFILE_LABELS
            else -> null
        }

        return map?.get(value) ?: "$value"
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
            5 -> "Timelapse Rate"
            30 -> "Photo Timelapse Rate"
            32 -> "Night Lapse Rate"
            59 -> "Auto Power Down"
            83 -> "GPS"
            88 -> "Luminosité LCD"
            91 -> "LEDs"
            108 -> "Ratio d'aspect"
            121 -> "Objectif"
            122 -> "Photo Lens"
            134 -> "Anti-Flicker"
            135 -> "HyperSmooth"
            167 -> "Hindsight"
            182 -> "Débit"
            183 -> "Profondeur de bits"
            184 -> "Profil vidéo"
            else -> "Setting $settingId"
        }
    }
}
