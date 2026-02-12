package com.ximun.gopropro

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Mappings des enums Protobuf EnumPresetTitle, EnumPresetIcon et EnumPresetGroup
 * depuis la documentation officielle OpenGoPro.
 */
object GoProPresetMappings {

    // --- EnumPresetTitle → Nom lisible ---

    private val presetTitleMap = mapOf(
        0 to "Activit\u00e9",
        1 to "Standard",
        2 to "Cin\u00e9matique",
        3 to "Photo",
        4 to "Live Burst",
        5 to "Rafale",
        6 to "Nuit",
        7 to "TimeWarp",
        8 to "Acc\u00e9l\u00e9r\u00e9",
        9 to "Nuit en acc\u00e9l\u00e9r\u00e9",
        10 to "Vid\u00e9o",
        11 to "Ralenti",
        13 to "Photo",
        14 to "Panorama",
        15 to "Rafale",
        16 to "TimeWarp",
        17 to "Acc\u00e9l\u00e9r\u00e9",
        18 to "Personnalis\u00e9",
        19 to "A\u00e9rien",
        20 to "V\u00e9lo",
        21 to "\u00c9pique",
        22 to "Int\u00e9rieur",
        23 to "Moto",
        24 to "Mount\u00e9",
        25 to "Ext\u00e9rieur",
        26 to "POV",
        27 to "Selfie",
        28 to "Skate",
        29 to "Neige",
        30 to "Sentier",
        31 to "Voyage",
        32 to "Eau",
        33 to "Boucle",
        34 to "\u00c9toiles",
        35 to "Action",
        36 to "Suivi",
        37 to "Surf",
        38 to "Ville",
        39 to "Mouvement",
        40 to "Harnais",
        41 to "Casque",
        42 to "Bouche",
        43 to "Cin\u00e9matique personnalis\u00e9",
        44 to "Vlog",
        45 to "FPV",
        46 to "HDR",
        47 to "Paysage",
        48 to "Log",
        49 to "Ralenti personnalis\u00e9",
        50 to "Tr\u00e9pied",
        58 to "Basique",
        59 to "Ultra Ralenti",
        60 to "Standard Endurance",
        61 to "Activit\u00e9 Endurance",
        62 to "Cin\u00e9matique Endurance",
        63 to "Ralenti Endurance",
        64 to "Stationnaire 1",
        65 to "Stationnaire 2",
        66 to "Stationnaire 3",
        67 to "Stationnaire 4",
        68 to "Vid\u00e9o Simple",
        69 to "TimeWarp Simple",
        70 to "Super Photo",
        71 to "Photo de Nuit",
        72 to "Vid\u00e9o Endurance",
        73 to "Qualit\u00e9 max",
        74 to "Batterie \u00e9tendue",
        75 to "Batterie longue",
        76 to "Fil\u00e9s d\u2019\u00e9toiles",
        77 to "Light Painting",
        78 to "Feux de v\u00e9hicules",
        79 to "Plein \u00e9cran",
        80 to "Vid\u00e9o avec objectif Max",
        81 to "TimeWarp avec objectif Max",
        82 to "Qualit\u00e9 standard",
        83 to "Qualit\u00e9 basique",
        93 to "Qualit\u00e9 maximale",
        94 to "Personnalis\u00e9",
        99 to "Standard facile",
        100 to "HDR facile",
        106 to "Rafale Ralenti",
        125 to "Vid\u00e9o 4:3",
        126 to "Vid\u00e9o 16:9",
        127 to "Ralenti 16:9",
        131 to "Acc\u00e9l\u00e9r\u00e9 Vid\u00e9o",
        132 to "Acc\u00e9l\u00e9r\u00e9 Photo",
        133 to "Nuit en acc\u00e9l\u00e9r\u00e9 Vid\u00e9o",
        134 to "Nuit en acc\u00e9l\u00e9r\u00e9 Photo"
    )

    fun getPresetTitle(titleId: Int): String? = presetTitleMap[titleId]

    /**
     * Formate les settings "caption" d'un preset en une ligne courte
     * ex: "1080 | 60 | Li" (comme l'app officielle GoPro)
     */
    fun formatPresetSettings(settings: List<com.ximun.gopropro.proto.GoProProtos.PresetSetting>): String? {
        val captions = settings.filter { it.hasIsCaption() && it.isCaption }
        if (captions.isEmpty()) return null
        return captions.mapNotNull { setting ->
            val label = GoProSettingsMappings.getLabel(setting.id, setting.value)
            when {
                // Setting inconnu → on ne l'affiche pas
                !GoProSettingsMappings.isKnownSetting(setting.id) -> null
                // Label Unknown → on ne l'affiche pas
                label.startsWith("Unknown") -> null
                // Setting connu mais valeur brute (ex: FPS "60") → abréger et afficher
                else -> abbreviateLabel(label)
            }
        }.joinToString(" | ").ifEmpty { null }
    }

    /** Abr\u00e8ge les labels longs pour tenir en une ligne */
    private fun abbreviateLabel(label: String): String = when {
        label.contains("Max SuperView") -> "MSV"
        label.contains("Max HyperView") -> "MHV"
        label.contains("Ultra SuperView") -> "USV"
        label.contains("HyperView") -> "HV"
        label.contains("SuperView") -> "SV"
        label.contains("Linear") -> "Li"
        label.contains("Wide") -> "La"
        label.contains("Narrow") -> "Na"
        else -> label
    }

    // --- EnumPresetIcon → ImageVector Material Icon ---

    fun getPresetIcon(iconId: Int): ImageVector = when (iconId) {
        0 -> Icons.Default.Videocam                     // VIDEO
        1 -> Icons.AutoMirrored.Filled.DirectionsRun    // ACTIVITY
        2 -> Icons.Default.Movie                        // CINEMATIC
        3 -> Icons.Default.PhotoCamera                  // PHOTO
        4 -> Icons.Default.BurstMode                    // LIVE_BURST
        5 -> Icons.Default.BurstMode                    // BURST
        6 -> Icons.Default.DarkMode                     // PHOTO_NIGHT
        7 -> Icons.Default.Speed                        // TIMEWARP
        8 -> Icons.Default.Timelapse                    // TIMELAPSE
        9 -> Icons.Default.NightsStay                   // NIGHTLAPSE
        10 -> Icons.Default.SlowMotionVideo             // SNAIL / SLOMO
        11 -> Icons.Default.Videocam                    // VIDEO_2
        13 -> Icons.Default.PhotoCamera                 // PHOTO_2
        14 -> Icons.Default.Panorama                    // PANORAMA
        15 -> Icons.Default.BurstMode                   // BURST_2
        16 -> Icons.Default.Speed                       // TIMEWARP_2
        17 -> Icons.Default.Timelapse                   // TIMELAPSE_2
        18 -> Icons.Default.Tune                        // CUSTOM
        19 -> Icons.Default.Air                         // AIR
        20 -> Icons.AutoMirrored.Filled.DirectionsBike   // BIKE
        21 -> Icons.Default.Landscape                   // EPIC
        22 -> Icons.Default.Home                        // INDOOR
        23 -> Icons.Default.Speed                        // MOTOR
        24 -> Icons.Default.CameraAlt                   // MOUNTED
        25 -> Icons.Default.Forest                      // OUTDOOR
        26 -> Icons.Default.Visibility                  // POV
        27 -> Icons.Default.Face                        // SELFIE
        28 -> Icons.AutoMirrored.Filled.DirectionsRun    // SKATE
        29 -> Icons.Default.AcUnit                      // SNOW
        30 -> Icons.Default.Terrain                     // TRAIL
        31 -> Icons.Default.Flight                      // TRAVEL
        32 -> Icons.Default.Water                       // WATER
        33 -> Icons.Default.Loop                        // LOOPING
        34 -> Icons.Default.Star                        // STARS
        35 -> Icons.Default.FlashOn                     // ACTION
        36 -> Icons.Default.Person                       // FOLLOW_CAM
        37 -> Icons.Default.Water                        // SURF
        38 -> Icons.Default.LocationCity                // CITY
        39 -> Icons.Default.Vibration                   // SHAKY
        40 -> Icons.Default.Accessibility               // CHESTY
        41 -> Icons.Default.FitnessCenter                // HELMET
        42 -> Icons.Default.CameraAlt                   // BITE
        43 -> Icons.Default.Movie                       // CUSTOM_CINEMATIC
        44 -> Icons.Default.Mic                         // VLOG
        45 -> Icons.Default.FlightTakeoff               // FPV
        46 -> Icons.Default.HdrStrong                       // HDR
        47 -> Icons.Default.Landscape                   // LANDSCAPE
        48 -> Icons.Default.DataUsage                   // LOG
        49 -> Icons.Default.SlowMotionVideo             // CUSTOM_SLOMO
        50 -> Icons.Default.CameraAlt                   // TRIPOD
        55 -> Icons.Default.Videocam                    // MAX_VIDEO
        56 -> Icons.Default.PhotoCamera                 // MAX_PHOTO
        57 -> Icons.Default.Speed                       // MAX_TIMEWARP
        58 -> Icons.Default.Videocam                    // BASIC
        59 -> Icons.Default.SlowMotionVideo             // ULTRA_SLO_MO
        60 -> Icons.Default.BatteryFull                 // STANDARD_ENDURANCE
        61 -> Icons.Default.BatteryFull                 // ACTIVITY_ENDURANCE
        62 -> Icons.Default.BatteryFull                 // CINEMATIC_ENDURANCE
        63 -> Icons.Default.BatteryFull                 // SLOMO_ENDURANCE
        70 -> Icons.Default.AutoAwesome                 // SIMPLE_SUPER_PHOTO
        71 -> Icons.Default.DarkMode                    // SIMPLE_NIGHT_PHOTO
        73 -> Icons.Default.Hd                            // HIGHEST_QUALITY_VIDEO
        74 -> Icons.Default.Videocam                    // STANDARD_QUALITY_VIDEO
        75 -> Icons.Default.Videocam                    // BASIC_QUALITY_VIDEO
        76 -> Icons.Default.Star                        // STAR_TRAIL
        77 -> Icons.Default.Brush                       // LIGHT_PAINTING
        78 -> Icons.Default.LinearScale                 // LIGHT_TRAIL
        79 -> Icons.Default.Fullscreen                  // FULL_FRAME
        100 -> Icons.Default.Tune                       // EASY_STANDARD_PROFILE
        101 -> Icons.Default.HdrStrong                      // EASY_HDR_PROFILE
        102 -> Icons.Default.SlowMotionVideo            // BURST_SLOMO
        1000 -> Icons.Default.Timelapse                 // TIMELAPSE_PHOTO
        1001 -> Icons.Default.NightsStay                // NIGHTLAPSE_PHOTO
        else -> Icons.Default.RadioButtonChecked
    }

    // --- EnumPresetGroup → Nom et icône du groupe ---

    fun getGroupTitle(groupId: Int): String = when (groupId) {
        1000 -> "VIDÉO"
        1001 -> "PHOTO"
        1002 -> "TIMELAPSE"
        else -> "GROUPE $groupId"
    }

    fun getGroupIcon(groupId: Int): ImageVector = when (groupId) {
        1000 -> Icons.Default.Videocam
        1001 -> Icons.Default.PhotoCamera
        1002 -> Icons.Default.Timelapse
        else -> Icons.Default.Folder
    }
}
