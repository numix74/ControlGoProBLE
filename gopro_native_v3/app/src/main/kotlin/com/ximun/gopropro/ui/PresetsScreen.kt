package com.ximun.gopropro.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ximun.gopropro.GoProPresetMappings
import com.ximun.gopropro.proto.GoProProtos
import com.ximun.gopropro.ui.theme.AppCard
import com.ximun.gopropro.ui.theme.PrimaryTeal
import com.ximun.gopropro.viewmodel.CameraUiState

@Composable
fun PresetsScreen(
    state: CameraUiState,
    onLoadPreset: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        HeaderSection(title = "PRESETS", subtitle = "MODES RAPIDES")
        Spacer(modifier = Modifier.height(24.dp))

        if (state.presetGroups.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryTeal)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                items(state.presetGroups) { group ->
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
    val groupId = if (group.hasId()) group.id.number else 0
    val groupTitle = GoProPresetMappings.getGroupTitle(groupId)
    val groupIcon = GoProPresetMappings.getGroupIcon(groupId)

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(groupIcon, null, tint = PrimaryTeal, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = groupTitle,
                color = Color.Gray,
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
    val backgroundColor = if (isActive) PrimaryTeal.copy(alpha = 0.2f) else AppCard
    val borderColor = if (isActive) PrimaryTeal else Color.Transparent
    val textColor = if (isActive) PrimaryTeal else Color.White

    val iconId = if (preset.hasIcon()) preset.icon.number else 0
    val icon = GoProPresetMappings.getPresetIcon(iconId)

    val presetName = when {
        preset.hasCustomName() -> preset.customName
        preset.hasTitleId() -> GoProPresetMappings.getPresetTitle(preset.titleId.number) ?: "Preset $index"
        else -> "Preset $index"
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
            Icon(icon, null, tint = textColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = presetName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                if (settingsLine != null) {
                    Text(
                        text = settingsLine,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
            if (isActive) {
                Icon(Icons.Default.CheckCircle, null, tint = PrimaryTeal, modifier = Modifier.size(20.dp))
            }
        }
    }
}
