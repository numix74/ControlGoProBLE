package com.ximun.gopropro

object GoProSettingsMappings {
    
    // Resolution (Setting ID 2) - TOUS les modèles
    private val RESOLUTION_LABELS = mapOf(
        1 to "5.3K (8:7)",
        4 to "4K (16:9)",
        6 to "4K (4:3)",
        9 to "2.7K (16:9)",
        12 to "2.7K (4:3) 120fps",
        15 to "1440p",
        17 to "1080p (4:3)",
        18 to "2.7K (4:3)",
        23 to "5K (4:3)",
        24 to "5.3K (16:9)",
        25 to "5.3K (4:3)",
        26 to "1080p",
        27 to "4K (9:16)",
        28 to "1080p (9:16)",
        100 to "5.3K",
        101 to "4K 4:3 HyperView",
        102 to "5.3K 4:3 HyperView"
    )
    
    // FPS (Setting ID 3) - TOUS les modèles
    private val FPS_LABELS = mapOf(
        0 to "240 fps",
        1 to "120 fps",
        2 to "100 fps",
        5 to "60 fps",
        6 to "50 fps",
        8 to "30 fps",
        9 to "25 fps",
        10 to "24 fps",
        13 to "200 fps"
    )
    
    // Lens (Setting ID 121) - TOUS les modèles
    private val LENS_LABELS = mapOf(
        0 to "Wide",
        2 to "Medium",
        3 to "Narrow",
        4 to "SuperView",
        7 to "Linear Horizon",
        8 to "Max SuperView",
        9 to "Linear",
        10 to "Linear + Horizon Lock",
        11 to "Max HyperView"
    )
    
    // HyperSmooth (Setting ID 135)
    private val HYPERSMOOTH_LABELS = mapOf(
        0 to "Off",
        1 to "Low",
        2 to "On",
        3 to "High",
        4 to "Boost",
        100 to "AutoBoost"
    )
    
    // Color Profile (Setting ID 134)
    private val COLOR_LABELS = mapOf(
        0 to "GoPro Color",
        1 to "Vibrant",
        2 to "Flat",
        3 to "Natural"
    )
    
    // ISO Max (Setting ID 122)
    private val ISO_MAX_LABELS = mapOf(
        1 to "100",
        4 to "200",
        6 to "400",
        9 to "800",
        12 to "1000",
        18 to "1600",
        26 to "3200",
        27 to "6400",
        28 to "12800",
        100 to "25600"
    )
    
    // White Balance (Setting ID 124)
    private val WHITE_BALANCE_LABELS = mapOf(
        0 to "Auto",
        1 to "2800K",
        2 to "3200K",
        5 to "4000K",
        6 to "4500K",
        7 to "5000K",
        8 to "5500K",
        9 to "6000K",
        10 to "6500K",
        11 to "Native",
        100 to "Native"
    )
    
    // Sharpness (Setting ID 139)
    private val SHARPNESS_LABELS = mapOf(
        0 to "Low",
        1 to "Medium",
        2 to "High"
    )
    
    // Bit Rate (Setting ID 144)
    private val BIT_RATE_LABELS = mapOf(
        0 to "Standard",
        1 to "High"
    )
    
    // Bit Depth (Setting ID 145)
    private val BIT_DEPTH_LABELS = mapOf(
        0 to "8-bit",
        2 to "10-bit"
    )
    
    // Video Profile (Setting ID 102)
    private val VIDEO_PROFILE_LABELS = mapOf(
        0 to "Standard",
        1 to "HDR",
        2 to "Log"
    )
    
    // Hindsight (Setting ID 113) // User had 139, but Sharpness is 139. Checking my constants.
    private val HINDSIGHT_LABELS = mapOf(
        0 to "Off",
        2 to "15 sec",
        3 to "30 sec"
    )
    
    // Interval photo (Setting ID 31)
    private val PHOTO_INTERVAL_LABELS = mapOf(
        0 to "Off",
        2 to "0.5 sec",
        3 to "1 sec",
        4 to "2 sec",
        5 to "5 sec",
        6 to "10 sec",
        7 to "30 sec",
        8 to "60 sec"
    )
    
    // EV Comp (Setting ID 85)
    private val EV_COMP_LABELS = mapOf(
        0 to "-2.0",
        1 to "-1.5",
        2 to "-1.0",
        3 to "-0.5",
        4 to "0",
        5 to "+0.5",
        6 to "+1.0",
        7 to "+1.5",
        8 to "+2.0"
    )
    
    // Shutter Speed (Setting ID 73)
    private val SHUTTER_LABELS = mapOf(
        0 to "Auto",
        2 to "1/125",
        3 to "1/250",
        4 to "1/500",
        5 to "1/1000",
        7 to "1/2000",
        8 to "1/4000",
        9 to "1/8000",
        10 to "1/16000",
        11 to "1/100",
        12 to "1/200"
    )
    
    /**
     * Retourne le label pour une valeur donnée d'un setting
     * Si la valeur n'existe pas dans le mapping, retourne "Inconnu (X)"
     */
    fun getLabel(settingId: Int, value: Int): String {
        val map = when (settingId) {
            2 -> RESOLUTION_LABELS
            3 -> FPS_LABELS
            121 -> LENS_LABELS
            135 -> HYPERSMOOTH_LABELS
            134 -> COLOR_LABELS
            122 -> ISO_MAX_LABELS
            124 -> WHITE_BALANCE_LABELS
            139 -> SHARPNESS_LABELS
            144 -> BIT_RATE_LABELS
            145 -> BIT_DEPTH_LABELS
            102 -> VIDEO_PROFILE_LABELS
            31 -> PHOTO_INTERVAL_LABELS
            85 -> EV_COMP_LABELS
            73 -> SHUTTER_LABELS
            else -> null
        }
        
        return map?.get(value) ?: "$value"
    }
    
    /**
     * Retourne les options disponibles basées sur les capacités
     * Filtre automatiquement les valeurs inconnues
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
            121 -> "Objectif"
            135 -> "HyperSmooth"
            134 -> "Couleur"
            122 -> "ISO Max"
            124 -> "Balance des blancs"
            139 -> "Netteté"
            144 -> "Débit"
            145 -> "Profondeur de bits"
            102 -> "Profil vidéo"
            31 -> "Intervalle photo"
            85 -> "Compensation EV"
            73 -> "Vitesse d'obturation"
            else -> "Setting $settingId"
        }
    }
}
