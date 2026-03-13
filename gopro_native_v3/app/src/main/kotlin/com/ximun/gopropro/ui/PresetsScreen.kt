package com.ximun.gopropro.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ximun.gopropro.GoProPresetMappings
import com.ximun.gopropro.R
import com.ximun.gopropro.proto.GoProProtos
import com.ximun.gopropro.ui.theme.LocalAppColors
import com.ximun.gopropro.viewmodel.CameraUiState

@Composable
fun PresetsScreen(
    state: CameraUiState,
    isLandscape: Boolean = false,
    onLoadPreset: (Int) -> Unit
) {
    val appColors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.background)
            .padding(20.dp)
    ) {
        HeaderSection(
            title = stringResource(R.string.presets_title),
            subtitle = stringResource(R.string.presets_subtitle)
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (state.presetGroups.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = appColors.accent)
            }
        } else {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                state.presetGroups.forEach { group ->
                    PresetGroupSection(group, state.currentPresetId, onLoadPreset)
                }
            }
        }
    }
}

@Composable
fun PresetGroupSection(
    group: GoProProtos.PresetGroup,
    currentPresetId: Int,
    onLoadPreset: (Int) -> Unit
) {
    val appColors = LocalAppColors.current
    val groupId = if (group.hasId()) group.id.number else 0
    val groupTitleRes = GoProPresetMappings.getGroupTitleRes(groupId)
    val groupTitle = if (groupId == 0 || !listOf(1000, 1001, 1002).contains(groupId)) {
        stringResource(R.string.preset_group_unknown, groupId)
    } else {
        stringResource(groupTitleRes)
    }
    val groupIcon = GoProPresetMappings.getGroupIcon(groupId)

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(groupIcon, null, tint = appColors.accent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = groupTitle,
                color = appColors.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            group.presetArrayList.forEachIndexed { index, preset ->
                PresetCard(preset, currentPresetId == preset.id, index + 1, onLoadPreset)
            }
        }
    }
}

@Composable
fun PresetCard(
    preset: GoProProtos.Preset,
    isActive: Boolean,
    index: Int,
    onLoadPreset: (Int) -> Unit
) {
    val appColors = LocalAppColors.current
    val backgroundColor = if (isActive) appColors.accent.copy(alpha = 0.15f) else appColors.card
    val borderColor = if (isActive) appColors.accent else appColors.border
    val nameColor = if (isActive) appColors.accent else appColors.textPrimary

    val iconId = if (preset.hasIcon()) preset.icon.number else 0
    val icon = GoProPresetMappings.getPresetIcon(iconId)

    val presetName = when {
        preset.hasCustomName() -> preset.customName
        preset.hasTitleId() ->
            GoProPresetMappings.getPresetTitle(preset.titleId.number)
                ?.let { stringResource(it) } ?: stringResource(R.string.presets_item_fallback, index)
        else -> stringResource(R.string.presets_item_fallback, index)
    }
    val settingsLine = GoProPresetMappings.formatPresetSettings(preset.settingArrayList)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (settingsLine != null) 72.dp else 64.dp)
            .clickable { onLoadPreset(preset.id) },
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = nameColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = presetName,
                    color = nameColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                if (settingsLine != null) {
                    Text(
                        text = settingsLine,
                        color = appColors.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }
            if (isActive) {
                Icon(Icons.Default.CheckCircle, null, tint = appColors.accent, modifier = Modifier.size(20.dp))
            }
        }
    }
}
