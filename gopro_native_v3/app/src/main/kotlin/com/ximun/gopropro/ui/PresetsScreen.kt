package com.ximun.gopropro.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ximun.gopropro.viewmodel.CameraUiState
import com.ximun.gopropro.proto.GoProProtos

@Composable
fun PresetsScreen(
    state: CameraUiState,
    onLoadPreset: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        headerSection("PRESETS", "MODES RAPIDES")
        Spacer(modifier = Modifier.height(16.dp))

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
    val groupTitle = when (group.id) {
        1000 -> "VIDÉO"
        1001 -> "PHOTO"
        1002 -> "TIMELAPSE"
        else -> "GROUPE ${group.id}"
    }

    val groupIcon = when (group.id) {
        1000 -> Icons.Default.Videocam
        1001 -> Icons.Default.PhotoCamera
        1002 -> Icons.Default.Timelapse
        else -> Icons.Default.Folder
    }

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
            group.presetArrayList.forEach { preset ->
                PresetCard(preset, currentPresetId == preset.id, onLoadPreset)
            }
        }
    }
}

@Composable
fun PresetCard(
    preset: GoProProtos.Preset,
    isActive: Boolean,
    onLoadPreset: (Int) -> Unit
) {
    val backgroundColor = if (isActive) PrimaryTeal.copy(alpha = 0.2f) else AppCard
    val borderColor = if (isActive) PrimaryTeal else Color.Transparent
    val textColor = if (isActive) PrimaryTeal else Color.White

    // Mapping rudimentaire des icônes de preset (peut être amélioré)
    val icon = when (preset.icon) {
        0 -> Icons.Default.Videocam              // VIDEO
        1 -> Icons.Default.PhotoCamera           // PHOTO
        2 -> Icons.Default.Timelapse            // TIMELAPSE
        3 -> Icons.AutoMirrored.Filled.DirectionsRun       // ACTIVITY
        4 -> Icons.Default.Movie                // CINEMATIC
        5 -> Icons.Default.SlowMotionVideo      // SLOMO
        else -> Icons.Default.RadioButtonChecked
    }


    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
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
            Text(
                text = if (preset.hasCustomName()) preset.customName else "Preset ${preset.id}", // Fallback si pas de nom
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            if (isActive) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.CheckCircle, null, tint = PrimaryTeal, modifier = Modifier.size(20.dp))
            }
        }
    }
}
