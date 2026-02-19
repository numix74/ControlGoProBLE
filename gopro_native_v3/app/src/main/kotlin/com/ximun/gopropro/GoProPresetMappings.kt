package com.ximun.gopropro

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Mappings des enums Protobuf EnumPresetTitle, EnumPresetIcon et EnumPresetGroup
 * depuis la documentation officielle OpenGoPro.
 *
 * Les titres de presets retournent désormais un @StringRes Int pour la localisation i18n.
 * Les appelants (composables) résolvent via stringResource(resId).
 */
object GoProPresetMappings {

    // --- EnumPresetTitle → @StringRes Int ---

    private val presetTitleMap = mapOf(
        0 to R.string.preset_title_0,
        1 to R.string.preset_title_1,
        2 to R.string.preset_title_2,
        3 to R.string.preset_title_3,
        4 to R.string.preset_title_4,
        5 to R.string.preset_title_5,
        6 to R.string.preset_title_6,
        7 to R.string.preset_title_7,
        8 to R.string.preset_title_8,
        9 to R.string.preset_title_9,
        10 to R.string.preset_title_10,
        11 to R.string.preset_title_11,
        13 to R.string.preset_title_13,
        14 to R.string.preset_title_14,
        15 to R.string.preset_title_15,
        16 to R.string.preset_title_16,
        17 to R.string.preset_title_17,
        18 to R.string.preset_title_18,
        19 to R.string.preset_title_19,
        20 to R.string.preset_title_20,
        21 to R.string.preset_title_21,
        22 to R.string.preset_title_22,
        23 to R.string.preset_title_23,
        24 to R.string.preset_title_24,
        25 to R.string.preset_title_25,
        26 to R.string.preset_title_26,
        27 to R.string.preset_title_27,
        28 to R.string.preset_title_28,
        29 to R.string.preset_title_29,
        30 to R.string.preset_title_30,
        31 to R.string.preset_title_31,
        32 to R.string.preset_title_32,
        33 to R.string.preset_title_33,
        34 to R.string.preset_title_34,
        35 to R.string.preset_title_35,
        36 to R.string.preset_title_36,
        37 to R.string.preset_title_37,
        38 to R.string.preset_title_38,
        39 to R.string.preset_title_39,
        40 to R.string.preset_title_40,
        41 to R.string.preset_title_41,
        42 to R.string.preset_title_42,
        43 to R.string.preset_title_43,
        44 to R.string.preset_title_44,
        45 to R.string.preset_title_45,
        46 to R.string.preset_title_46,
        47 to R.string.preset_title_47,
        48 to R.string.preset_title_48,
        49 to R.string.preset_title_49,
        50 to R.string.preset_title_50,
        58 to R.string.preset_title_58,
        59 to R.string.preset_title_59,
        60 to R.string.preset_title_60,
        61 to R.string.preset_title_61,
        62 to R.string.preset_title_62,
        63 to R.string.preset_title_63,
        64 to R.string.preset_title_64,
        65 to R.string.preset_title_65,
        66 to R.string.preset_title_66,
        67 to R.string.preset_title_67,
        68 to R.string.preset_title_68,
        69 to R.string.preset_title_69,
        70 to R.string.preset_title_70,
        71 to R.string.preset_title_71,
        72 to R.string.preset_title_72,
        73 to R.string.preset_title_73,
        74 to R.string.preset_title_74,
        75 to R.string.preset_title_75,
        76 to R.string.preset_title_76,
        77 to R.string.preset_title_77,
        78 to R.string.preset_title_78,
        79 to R.string.preset_title_79,
        80 to R.string.preset_title_80,
        81 to R.string.preset_title_81,
        82 to R.string.preset_title_82,
        83 to R.string.preset_title_83,
        93 to R.string.preset_title_93,
        94 to R.string.preset_title_94,
        99 to R.string.preset_title_99,
        100 to R.string.preset_title_100,
        106 to R.string.preset_title_106,
        125 to R.string.preset_title_125,
        126 to R.string.preset_title_126,
        127 to R.string.preset_title_127,
        131 to R.string.preset_title_131,
        132 to R.string.preset_title_132,
        133 to R.string.preset_title_133,
        134 to R.string.preset_title_134
    )

    /** Retourne le @StringRes du titre, ou null si l'ID est inconnu. */
    @StringRes
    fun getPresetTitle(titleId: Int): Int? = presetTitleMap[titleId]

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

    /** Abrège les labels longs pour tenir en une ligne */
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
        20 -> Icons.Default.TwoWheeler                  // BIKE
        21 -> Icons.Default.Landscape                   // EPIC
        22 -> Icons.Default.Home                        // INDOOR
        23 -> Icons.Default.Speed                       // MOTOR
        24 -> Icons.Default.CameraAlt                   // MOUNTED
        25 -> Icons.Default.Forest                      // OUTDOOR
        26 -> Icons.Default.Visibility                  // POV
        27 -> Icons.Default.Face                        // SELFIE
        28 -> Icons.AutoMirrored.Filled.DirectionsRun   // SKATE
        29 -> Icons.Default.AcUnit                      // SNOW
        30 -> Icons.Default.Terrain                     // TRAIL
        31 -> Icons.Default.Flight                      // TRAVEL
        32 -> Icons.Default.Water                       // WATER
        33 -> Icons.Default.Loop                        // LOOPING
        34 -> Icons.Default.Star                        // STARS
        35 -> Icons.Default.FlashOn                     // ACTION
        36 -> Icons.Default.Person                      // FOLLOW_CAM
        37 -> Icons.Default.Water                       // SURF
        38 -> Icons.Default.LocationCity                // CITY
        39 -> Icons.Default.Vibration                   // SHAKY
        40 -> Icons.Default.Accessibility               // CHESTY
        41 -> Icons.Default.FitnessCenter               // HELMET
        42 -> Icons.Default.CameraAlt                   // BITE
        43 -> Icons.Default.Movie                       // CUSTOM_CINEMATIC
        44 -> Icons.Default.Mic                         // VLOG
        45 -> Icons.Default.FlightTakeoff               // FPV
        46 -> Icons.Default.HdrStrong                   // HDR
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
        73 -> Icons.Default.Hd                          // HIGHEST_QUALITY_VIDEO
        74 -> Icons.Default.Videocam                    // STANDARD_QUALITY_VIDEO
        75 -> Icons.Default.Videocam                    // BASIC_QUALITY_VIDEO
        76 -> Icons.Default.Star                        // STAR_TRAIL
        77 -> Icons.Default.Brush                       // LIGHT_PAINTING
        78 -> Icons.Default.LinearScale                 // LIGHT_TRAIL
        79 -> Icons.Default.Fullscreen                  // FULL_FRAME
        100 -> Icons.Default.Tune                       // EASY_STANDARD_PROFILE
        101 -> Icons.Default.HdrStrong                  // EASY_HDR_PROFILE
        102 -> Icons.Default.SlowMotionVideo            // BURST_SLOMO
        1000 -> Icons.Default.Timelapse                 // TIMELAPSE_PHOTO
        1001 -> Icons.Default.NightsStay                // NIGHTLAPSE_PHOTO
        else -> Icons.Default.RadioButtonChecked
    }

    // --- EnumPresetGroup → @StringRes et icône du groupe ---

    @StringRes
    fun getGroupTitleRes(groupId: Int): Int = when (groupId) {
        1000 -> R.string.preset_group_video
        1001 -> R.string.preset_group_photo
        1002 -> R.string.preset_group_timelapse
        else -> R.string.preset_group_unknown
    }

    fun getGroupIcon(groupId: Int): ImageVector = when (groupId) {
        1000 -> Icons.Default.Videocam
        1001 -> Icons.Default.PhotoCamera
        1002 -> Icons.Default.Timelapse
        else -> Icons.Default.Folder
    }
}
