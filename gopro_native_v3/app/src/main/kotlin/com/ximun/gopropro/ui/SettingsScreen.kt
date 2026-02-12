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
    val modeSettingIds = buildModeSettings(activePreset)

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
 * Utilise les settingArray du preset pour déterminer quels settings afficher.
 */
@Composable
private fun buildModeSettings(
    activePreset: com.ximun.gopropro.proto.GoProProtos.Preset?
): ModeSettings {
    if (activePreset == null) return ModeSettings(defaultVideoSettings())

    // Récupérer tous les setting IDs du preset (pas seulement les captions)
    val presetSettingIds = activePreset.settingArrayList.map { it.id }.toSet()

    // Si le preset n'a pas de settings (données pas encore reçues), fallback
    if (presetSettingIds.isEmpty()) return ModeSettings(defaultVideoSettings())

    // Détecter le type de mode pour l'icône
    val isTimelapse = presetSettingIds.contains(GoProConstants.SETTING_ID_TIMEWARP_SPEED) ||
            presetSettingIds.contains(GoProConstants.SETTING_ID_TIMELAPSE_RATE) ||
            presetSettingIds.contains(GoProConstants.SETTING_ID_NIGHT_LAPSE_RATE) ||
            presetSettingIds.contains(GoProConstants.SETTING_ID_STAR_TRAILS_LENGTH) ||
            presetSettingIds.contains(GoProConstants.SETTING_ID_LAPSE_MODE)

    // Ordre d'affichage souhaité pour les settings du mode
    val orderedSettingIds = listOf(
        // Résolution et cadrage
        GoProConstants.SETTING_ID_RESOLUTION,
        GoProConstants.SETTING_ID_FPS,
        GoProConstants.SETTING_ID_FRAME_RATE,
        GoProConstants.SETTING_ID_ASPECT_RATIO,
        GoProConstants.SETTING_ID_VIDEO_FRAMING,
        // Objectif
        GoProConstants.SETTING_ID_LENS,
        GoProConstants.SETTING_ID_TIMELAPSE_LENS,
        GoProConstants.SETTING_ID_PHOTO_LENS,
        // Stabilisation
        GoProConstants.SETTING_ID_HYPERSMOOTH,
        // Timelapse / Nuit spécifiques
        GoProConstants.SETTING_ID_TIMEWARP_SPEED,
        GoProConstants.SETTING_ID_TIMELAPSE_RATE,
        GoProConstants.SETTING_ID_NIGHT_LAPSE_RATE,
        GoProConstants.SETTING_ID_STAR_TRAILS_LENGTH,
        GoProConstants.SETTING_ID_LAPSE_MODE,
        GoProConstants.SETTING_ID_MEDIA_FORMAT,
        // Qualité
        GoProConstants.SETTING_ID_BIT_RATE,
        GoProConstants.SETTING_ID_BIT_DEPTH,
        GoProConstants.SETTING_ID_VIDEO_PROFILE,
        GoProConstants.SETTING_ID_SYSTEM_VIDEO_MODE,
        // Divers
        GoProConstants.SETTING_ID_ANTI_FLICKER,
        GoProConstants.SETTING_ID_HINDSIGHT,
        GoProConstants.SETTING_ID_MAX_LENS_MOD_ENABLE
    )

    // Filtrer : ne garder que ceux qui sont dans le preset
    val filteredIds = orderedSettingIds.filter { it in presetSettingIds }

    return ModeSettings(
        settingIds = filteredIds.ifEmpty { defaultVideoSettings() },
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
