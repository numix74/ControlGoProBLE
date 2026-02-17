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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Switch
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
import com.ximun.gopropro.ui.theme.LocalAppColors
import com.ximun.gopropro.ui.theme.PrimaryTeal
import com.ximun.gopropro.viewmodel.CameraUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun SettingsScreen(
    state: CameraUiState,
    isLandscape: Boolean = false,
    onUpdateSetting: (Int, Int) -> Unit,
    onSyncTime: () -> Unit = {},
    onReboot: () -> Unit = {},
    onToggleDarkMode: () -> Unit = {},
    onToggleBubble: () -> Unit = {}
) {
    val settings = state.settings
    val capabilities = state.capabilities
    val appColors = LocalAppColors.current

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

        // Section: Paramètres système (seulement ceux disponibles sur le modèle)
        val systemSettingCandidates = listOf(
            GoProConstants.SETTING_ID_AUTO_POWER_DOWN,
            GoProConstants.SETTING_ID_LED,
            GoProConstants.SETTING_ID_BEEP_VOLUME,
            GoProConstants.SETTING_ID_LCD_BRIGHTNESS,
            GoProConstants.SETTING_ID_SCREEN_SAVER,
            GoProConstants.SETTING_ID_GPS,
            GoProConstants.SETTING_ID_ANTI_FLICKER,
            GoProConstants.SETTING_ID_WIRELESS_BAND,
            GoProConstants.SETTING_ID_LANGUAGE
        )
        val availableSystemSettings = systemSettingCandidates.filter { settings.containsKey(it) }

        SectionHeader(icon = Icons.Default.Settings, title = "PARAMÈTRES SYSTÈME")
        Spacer(modifier = Modifier.height(12.dp))

        // Settings système de la caméra
        availableSystemSettings.forEach { settingId ->
            RenderSetting(settingId, capabilities, settings, onUpdateSetting)
        }

        // Toggle Mode Sombre/Clair (app)
        DarkModeToggle(isDarkMode = state.isDarkMode, onToggle = onToggleDarkMode)

        // Toggle Bulle Flottante (app)
        BubbleToggle(isBubbleEnabled = state.isBubbleEnabled, onToggle = onToggleBubble)

        // Bouton Sync Horloge (toujours visible)
        ActionSettingRow(
            label = "Sync Horloge",
            icon = Icons.Default.Sync,
            actionLabel = "Synchroniser",
            feedbackLabel = "✓ Synchronisé",
            feedbackColor = Color(0xFF4CAF50),
            feedbackDurationMs = 2000,
            onClick = onSyncTime
        )

        // Bouton Redémarrer (conditionnel — masqué si non supporté par la caméra)
        // CMD_REBOOT (0x11) n'est pas supporté sur tous les modèles (ex: HERO11 Mini)
        // On vérifie via la présence d'un setting connu qui confirme le support
        val isRebootSupported = settings.containsKey(GoProConstants.SETTING_ID_LCD_BRIGHTNESS)
                && !state.cameraName.contains("Mini", ignoreCase = true)
        if (isRebootSupported) {
            ActionSettingRow(
                label = "Redémarrer",
                icon = Icons.Default.RestartAlt,
                actionLabel = "Redémarrer",
                feedbackLabel = "Redémarrage...",
                feedbackColor = Color(0xFFF59E0B),
                feedbackDurationMs = 3000,
                onClick = onReboot
            )
        }

        Spacer(modifier = Modifier.height(if (isLandscape) 24.dp else 80.dp))
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
 * Part des settings du preset (settingArray) puis enrichit avec des settings
 * supplémentaires pertinents pour le mode, s'ils existent sur la caméra.
 */
private fun buildModeSettings(
    activePreset: com.ximun.gopropro.proto.GoProProtos.Preset?,
    availableSettings: Set<Int>
): ModeSettings {
    if (activePreset == null) {
        return ModeSettings(
            settingIds = defaultVideoSettings().filter { it in availableSettings }
                .ifEmpty { defaultVideoSettings() }
        )
    }

    // IDs du settingArray du preset (typiquement les captions: résolution, fps, objectif...)
    val presetSettingIds = activePreset.settingArrayList.map { it.id }.toSet()

    // Détecter le type de mode via le titleId (valeurs du proto EnumPresetTitle)
    val titleId = if (activePreset.hasTitleId()) activePreset.titleId.number else -1
    val isTimelapse = titleId in setOf(
        7,         // TIME_WARP
        8, 9,      // TIME_LAPSE, NIGHT_LAPSE
        16,        // TIME_WARP_2
        69,        // SIMPLE_TIME_WARP
        76, 77, 78, // STAR_TRAIL, LIGHT_PAINTING, LIGHT_TRAIL
        81         // MAX_TIMEWARP
    ) || presetSettingIds.any { it in setOf(
        GoProConstants.SETTING_ID_TIMEWARP_SPEED,
        GoProConstants.SETTING_ID_TIMELAPSE_RATE,
        GoProConstants.SETTING_ID_NIGHT_LAPSE_RATE,
        GoProConstants.SETTING_ID_STAR_TRAILS_LENGTH,
        GoProConstants.SETTING_ID_LAPSE_MODE
    )}

    // Ordre d'affichage complet (preset + enrichissements)
    // On inclut à la fois les settings du preset ET des settings supplémentaires
    // pertinents pour le mode, filtrés par ce qui est réellement disponible
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
        150,  // Video Horizon Leveling
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
        GoProConstants.SETTING_ID_HINDSIGHT,
        GoProConstants.SETTING_ID_MAX_LENS_MOD_ENABLE
    )

    // Settings enrichis pour le mode vidéo (ajoutés même s'ils ne sont pas dans le settingArray)
    val videoExtraSettings = setOf(
        GoProConstants.SETTING_ID_HYPERSMOOTH,
        150,  // Video Horizon Leveling
        GoProConstants.SETTING_ID_BIT_RATE,
        GoProConstants.SETTING_ID_BIT_DEPTH,
        GoProConstants.SETTING_ID_VIDEO_PROFILE,
        GoProConstants.SETTING_ID_HINDSIGHT
    )

    // Settings enrichis pour timelapse
    val timelapseExtraSettings = setOf(
        GoProConstants.SETTING_ID_LAPSE_MODE,
        GoProConstants.SETTING_ID_MEDIA_FORMAT
    )

    val extraSettings = if (isTimelapse) timelapseExtraSettings else videoExtraSettings

    // Filtre : on garde un setting s'il est dans le preset OU dans les extras ET disponible sur la caméra
    val resultIds = orderedSettingIds.filter { id ->
        id in presetSettingIds || (id in extraSettings && id in availableSettings)
    }

    return ModeSettings(
        settingIds = resultIds.ifEmpty {
            defaultVideoSettings().filter { it in availableSettings }
                .ifEmpty { defaultVideoSettings() }
        },
        isTimelapse = isTimelapse
    )
}

/**
 * Settings vidéo par défaut (fallback si pas de preset data)
 */
private fun defaultVideoSettings() = listOf(
    GoProConstants.SETTING_ID_RESOLUTION,
    GoProConstants.SETTING_ID_FPS,
    GoProConstants.SETTING_ID_LENS,
    GoProConstants.SETTING_ID_HYPERSMOOTH,
    GoProConstants.SETTING_ID_BIT_RATE,
    GoProConstants.SETTING_ID_BIT_DEPTH,
    GoProConstants.SETTING_ID_VIDEO_PROFILE
)

/**
 * Toggle Mode Sombre/Clair intégré dans les paramètres système.
 * Même style visuel que les dropdowns de réglages.
 */
@Composable
private fun DarkModeToggle(isDarkMode: Boolean, onToggle: () -> Unit) {
    val appColors = LocalAppColors.current
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() },
            color = appColors.card,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, appColors.border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isDarkMode) "Mode Clair" else "Mode Sombre",
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onToggle() }
                    )
                }
            }
        }
    }
}

/**
 * Toggle Bulle Flottante intégré dans les paramètres système.
 * Même style visuel que le toggle Mode Sombre/Clair.
 */
@Composable
private fun BubbleToggle(isBubbleEnabled: Boolean, onToggle: () -> Unit) {
    val appColors = LocalAppColors.current
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() },
            color = appColors.card,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, appColors.border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Bulle Flottante",
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary
                )
                Switch(
                    checked = isBubbleEnabled,
                    onCheckedChange = { onToggle() }
                )
            }
        }
    }
}

/**
 * Bouton d'action intégré dans les paramètres système.
 * Même style visuel que les dropdowns mais avec feedback temporaire.
 */
@Composable
private fun ActionSettingRow(
    label: String,
    icon: ImageVector,
    actionLabel: String,
    feedbackLabel: String,
    feedbackColor: Color,
    feedbackDurationMs: Long,
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var feedback by remember { mutableStateOf(false) }
    val appColors = LocalAppColors.current

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !feedback) {
                    onClick()
                    scope.launch {
                        feedback = true
                        delay(feedbackDurationMs)
                        feedback = false
                    }
                },
            color = appColors.card,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                1.dp,
                if (feedback) feedbackColor.copy(alpha = 0.4f) else appColors.border
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = label, fontWeight = FontWeight.Bold, color = appColors.textPrimary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (feedback) feedbackLabel else actionLabel,
                        color = if (feedback) feedbackColor else PrimaryTeal,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        icon, null,
                        tint = if (feedback) feedbackColor else PrimaryTeal,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    val appColors = LocalAppColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = PrimaryTeal, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = appColors.textSecondary,
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
    val caps = when {
        capabilities.containsKey(settingId) -> capabilities[settingId]!!
        currentVal != null -> listOf(currentVal)
        else -> emptyList()
    }
    SettingDropdown(
        label = GoProSettingsMappings.getSettingName(settingId),
        settingId = settingId,
        currentValue = currentVal,
        capabilities = caps,
        onValueChange = { value -> onUpdateSetting(settingId, value) }
    )
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
    val hasOptions = options.isNotEmpty()
    val appColors = LocalAppColors.current

    val currentLabel = currentValue?.let {
        GoProSettingsMappings.getLabel(settingId, it)
    } ?: "..."
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (hasOptions) Modifier.clickable { expanded = true } else Modifier),
                color = appColors.card,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, appColors.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, fontWeight = FontWeight.Bold, color = appColors.textPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = currentLabel, color = if (currentValue != null) PrimaryTeal else appColors.textSecondary)
                        if (hasOptions) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = appColors.textSecondary)
                        }
                    }
                }
            }

            if (hasOptions) {
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
}
