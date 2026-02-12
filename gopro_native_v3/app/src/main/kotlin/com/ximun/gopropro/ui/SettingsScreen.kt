package com.ximun.gopropro.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ximun.gopropro.GoProPresetMappings
import com.ximun.gopropro.GoProSettingsMappings
import com.ximun.gopropro.ble.GoProConstants
import com.ximun.gopropro.ui.theme.AppCard
import com.ximun.gopropro.ui.theme.PrimaryTeal
import com.ximun.gopropro.viewmodel.CameraUiState


@Composable
fun SettingsScreen(
    state: CameraUiState,
    onUpdateSetting: (Int, Int) -> Unit
) {
    val settings = state.settings
    val capabilities = state.capabilities

    // Trouver le preset actif et ses settings
    val activePreset = state.presetGroups
        .flatMap { it.presetArrayList }
        .firstOrNull { it.id == state.currentPresetId }

    // Déterminer le titre et l'icône du mode actif
    val presetName = when {
        activePreset == null -> "MODE ACTIF"
        activePreset.hasCustomName() -> activePreset.customName.uppercase()
        activePreset.hasTitleId() ->
            GoProPresetMappings.getPresetTitle(activePreset.titleId.number)?.uppercase() ?: "MODE ACTIF"
        else -> "MODE ACTIF"
    }

    // Construire la liste de settings du mode à partir du preset actif
    val modeSettingIds = buildModeSettings(activePreset, settings.keys)

    // Debug temporaire
    android.util.Log.d("SettingsScreen", "🔧 activePreset=${activePreset?.id}, settingArrayCount=${activePreset?.settingArrayCount ?: 0}, presetSettingIds=${activePreset?.settingArrayList?.map { it.id }}")
    android.util.Log.d("SettingsScreen", "🔧 state.settings.keys=${settings.keys.sorted()}")
    android.util.Log.d("SettingsScreen", "🔧 modeSettingIds=${modeSettingIds.settingIds}")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection(title = "RÉGLAGES", subtitle = "PARAMÈTRES DE LA CAMÉRA")
        Spacer(modifier = Modifier.height(24.dp))

        // Section: Paramètres du mode actif
        SectionHeader(
            icon = if (modeSettingIds.isTimelapse) Icons.Default.Timelapse else Icons.Default.Videocam,
            title = presetName
        )
        Spacer(modifier = Modifier.height(12.dp))

        modeSettingIds.settingIds.forEach { settingId ->
            RenderSetting(settingId, capabilities, settings, onUpdateSetting)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Section: Paramètres système
        SectionHeader(icon = Icons.Default.Settings, title = "PARAMÈTRES SYSTÈME")
        Spacer(modifier = Modifier.height(12.dp))

        val systemSettings = listOf(
            GoProConstants.SETTING_ID_AUTO_POWER_DOWN,
            GoProConstants.SETTING_ID_LED,
            GoProConstants.SETTING_ID_LCD_BRIGHTNESS,
            GoProConstants.SETTING_ID_GPS
        )

        systemSettings.forEach { settingId ->
            RenderSetting(settingId, capabilities, settings, onUpdateSetting)
        }
    }
}

/**
 * Résultat du calcul des settings du mode
 */
private data class ModeSettings(
    val settingIds: List<Int>,
    val isTimelapse: Boolean = false
)

/**
 * Construit la liste de settings pertinents en fonction du preset actif.
 * Utilise les settingArray du preset pour déterminer quels settings afficher,
 * avec fallback sur les settings par défaut si pas de données.
 */
@Composable
private fun buildModeSettings(
    activePreset: com.ximun.gopropro.proto.GoProProtos.Preset?,
    availableSettings: Set<Int>
): ModeSettings {
    if (activePreset == null) return ModeSettings(defaultVideoSettings())

    // Récupérer tous les setting IDs du preset (pas seulement les captions)
    val presetSettingIds = activePreset.settingArrayList.map { it.id }.toSet()

    // Détecter le type de mode via le titleId
    val titleId = if (activePreset.hasTitleId()) activePreset.titleId.number else -1
    val isTimelapse = titleId in setOf(
        8, 9,      // Accéléré, Nuit en accéléré
        10, 11,    // TimeWarp, Max TimeWarp (anciens)
        76, 77, 78, // Filés étoiles, Light Painting, Feux véhicules
        80, 81     // Max Video, Max TimeWarp
    ) || presetSettingIds.any { it in setOf(
        GoProConstants.SETTING_ID_TIMEWARP_SPEED,
        GoProConstants.SETTING_ID_TIMELAPSE_RATE,
        GoProConstants.SETTING_ID_NIGHT_LAPSE_RATE,
        GoProConstants.SETTING_ID_STAR_TRAILS_LENGTH,
        GoProConstants.SETTING_ID_LAPSE_MODE
    )}

    // Si le preset a des settings, on les utilise pour filtrer
    if (presetSettingIds.isNotEmpty()) {
        // Ordre d'affichage souhaité
        val orderedSettingIds = listOf(
            GoProConstants.SETTING_ID_RESOLUTION,
            GoProConstants.SETTING_ID_FPS,
            GoProConstants.SETTING_ID_FRAME_RATE,
            GoProConstants.SETTING_ID_ASPECT_RATIO,
            GoProConstants.SETTING_ID_VIDEO_FRAMING,
            GoProConstants.SETTING_ID_LENS,
            GoProConstants.SETTING_ID_TIMELAPSE_LENS,
            GoProConstants.SETTING_ID_PHOTO_LENS,
            GoProConstants.SETTING_ID_HYPERSMOOTH,
            GoProConstants.SETTING_ID_TIMEWARP_SPEED,
            GoProConstants.SETTING_ID_TIMELAPSE_RATE,
            GoProConstants.SETTING_ID_NIGHT_LAPSE_RATE,
            GoProConstants.SETTING_ID_STAR_TRAILS_LENGTH,
            GoProConstants.SETTING_ID_LAPSE_MODE,
            GoProConstants.SETTING_ID_MEDIA_FORMAT,
            GoProConstants.SETTING_ID_BIT_RATE,
            GoProConstants.SETTING_ID_BIT_DEPTH,
            GoProConstants.SETTING_ID_VIDEO_PROFILE,
            GoProConstants.SETTING_ID_SYSTEM_VIDEO_MODE,
            GoProConstants.SETTING_ID_ANTI_FLICKER,
            GoProConstants.SETTING_ID_HINDSIGHT,
            GoProConstants.SETTING_ID_MAX_LENS_MOD_ENABLE
        )

        val filteredIds = orderedSettingIds.filter { it in presetSettingIds }
        if (filteredIds.isNotEmpty()) {
            return ModeSettings(settingIds = filteredIds, isTimelapse = isTimelapse)
        }
    }

    // Fallback : afficher tous les settings dont on a une valeur
    // (même si le preset n'a pas de settingArray chargé)
    return ModeSettings(
        settingIds = defaultVideoSettings().filter { it in availableSettings }
            .ifEmpty { defaultVideoSettings() },
        isTimelapse = isTimelapse
    )
}

/**
 * Settings vidéo par défaut (fallback si pas de preset data)
 */
private fun defaultVideoSettings() = listOf(
    GoProConstants.SETTING_ID_RESOLUTION,
    GoProConstants.SETTING_ID_FPS,
    GoProConstants.SETTING_ID_ASPECT_RATIO,
    GoProConstants.SETTING_ID_LENS,
    GoProConstants.SETTING_ID_HYPERSMOOTH,
    GoProConstants.SETTING_ID_ANTI_FLICKER,
    GoProConstants.SETTING_ID_BIT_RATE,
    GoProConstants.SETTING_ID_BIT_DEPTH,
    GoProConstants.SETTING_ID_VIDEO_PROFILE
)

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = PrimaryTeal, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun RenderSetting(
    settingId: Int,
    capabilities: Map<Int, List<Int>>,
    settings: Map<Int, Int>,
    onUpdateSetting: (Int, Int) -> Unit
) {
    val currentVal = settings[settingId]
    // Si on a la valeur actuelle, on affiche le réglage, même si on n'a pas encore les toutes les capacités
    if (currentVal != null) {
        val caps = capabilities[settingId] ?: listOf(currentVal)
        SettingDropdown(
            label = GoProSettingsMappings.getSettingName(settingId),
            settingId = settingId,
            currentValue = currentVal,
            capabilities = caps,
            onValueChange = { value -> onUpdateSetting(settingId, value) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingDropdown(
    label: String,
    settingId: Int,
    currentValue: Int?,
    capabilities: List<Int>,
    onValueChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = GoProSettingsMappings.getAvailableOptions(settingId, capabilities)

    if (options.isEmpty()) return

    val currentLabel = currentValue?.let {
        GoProSettingsMappings.getLabel(settingId, it)
    } ?: "..."

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                color = AppCard,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = currentLabel, color = Color.Gray)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
                    }
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { (value, optionLabel) ->
                    DropdownMenuItem(
                        text = { Text(optionLabel) },
                        onClick = {
                            onValueChange(value)
                            expanded = false
                        },
                        leadingIcon = if (value == currentValue) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null
                    )
                }
            }
        }
    }
}
