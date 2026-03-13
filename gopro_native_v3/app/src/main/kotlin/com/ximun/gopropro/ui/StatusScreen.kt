package com.ximun.gopropro.ui

import androidx.compose.foundation.background
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ximun.gopropro.GoProPresetMappings
import com.ximun.gopropro.R
import com.ximun.gopropro.ui.theme.LocalAppColors
import com.ximun.gopropro.viewmodel.CameraUiState

@Composable
fun StatusScreen(state: CameraUiState, isLandscape: Boolean = false) {
    val appColors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.background)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection(
            title = stringResource(R.string.status_title),
            subtitle = stringResource(R.string.status_subtitle)
        )
        Spacer(modifier = Modifier.height(24.dp))
        BatterySection(state)
        Spacer(modifier = Modifier.height(24.dp))
        StorageSection(state)
        Spacer(modifier = Modifier.height(24.dp))
        SystemInfoList(state)
        Spacer(modifier = Modifier.height(if (isLandscape) 24.dp else 80.dp))
    }
}

@Composable
fun BatterySection(state: CameraUiState) {
    val appColors = LocalAppColors.current
    val errorColor = Color(0xFFDC2626)
    val batteryColor = if (state.batteryLevel < 20) errorColor else appColors.accent

    Surface(
        color = appColors.card,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, appColors.border)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = stringResource(R.string.status_battery_section),
                color = appColors.textSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${state.batteryLevel}%",
                    color = appColors.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = when {
                        state.isCharging -> stringResource(R.string.status_battery_charging)
                        state.batteryLevel < 20 -> stringResource(R.string.status_battery_critical)
                        else -> stringResource(R.string.status_battery_nominal)
                    },
                    color = batteryColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(appColors.progressTrack)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(state.batteryLevel / 100f)
                        .background(batteryColor)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    if (state.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                    contentDescription = null,
                    tint = appColors.textSecondary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (state.isCharging) stringResource(R.string.status_battery_ac)
                           else stringResource(R.string.status_battery_internal),
                    color = appColors.textSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StorageSection(state: CameraUiState) {
    val appColors = LocalAppColors.current
    val errorColor = Color(0xFFDC2626)

    Surface(
        color = appColors.card,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, appColors.border)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = stringResource(R.string.status_storage_section),
                color = appColors.textSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = state.storageSpace,
                    color = appColors.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = stringResource(R.string.status_storage_remaining),
                    color = appColors.textSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(appColors.progressTrack)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(state.storagePercent / 100f)
                        .background(appColors.accent)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.status_storage_full_pct, state.storagePercent),
                    color = appColors.textSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.status_storage_card_label, state.sdStatusLabel),
                    color = if (state.sdStatusLabel != "OK") errorColor else appColors.textSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.status_storage_sd_type),
                    color = appColors.textSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SystemInfoList(state: CameraUiState) {
    val appColors = LocalAppColors.current
    val errorColor = Color(0xFFDC2626)

    Surface(
        color = appColors.card,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, appColors.border)
    ) {
        Column {
            InfoRow(state.cameraName.ifEmpty { "GoPro" }, "OK", Icons.Default.Videocam)
            HorizontalDivider(color = appColors.divider)

            InfoRow(
                stringResource(R.string.status_info_system_state),
                if (state.isRecording) stringResource(R.string.status_info_recording)
                else stringResource(R.string.status_info_ready),
                if (state.isRecording) Icons.Default.RadioButtonChecked else Icons.Default.CheckCircle
            )
            HorizontalDivider(color = appColors.divider)

            InfoRow(stringResource(R.string.status_info_temperature), state.tempStatus, Icons.Default.DeviceThermostat)
            if (state.isOverheating) {
                HorizontalDivider(color = errorColor)
                InfoRow(stringResource(R.string.status_info_overheat), stringResource(R.string.status_info_danger), Icons.Default.Warning)
            }
            HorizontalDivider(color = appColors.divider)

            if (state.isCharging) {
                InfoRow(stringResource(R.string.status_info_power), stringResource(R.string.status_battery_charging), Icons.Default.Power)
                HorizontalDivider(color = appColors.divider)
            }

            InfoRow(stringResource(R.string.status_info_photos), "${state.photosRemaining}", Icons.Default.PhotoCamera)
            HorizontalDivider(color = appColors.divider)
            InfoRow(stringResource(R.string.status_info_videos), "${state.videosCount}", Icons.Default.VideoLibrary)
            HorizontalDivider(color = appColors.divider)
            InfoRow(stringResource(R.string.status_info_waypoints), "${state.waypointCount}", Icons.Default.LocationOn)
            HorizontalDivider(color = appColors.divider)
            InfoRow(stringResource(R.string.status_info_sd_capacity), state.sdCapacityFormatted, Icons.Default.SdStorage)
            HorizontalDivider(color = appColors.divider)
            InfoRow(stringResource(R.string.status_info_time_remaining), state.videoRemainingTime, Icons.Default.Timer)
            HorizontalDivider(color = appColors.divider)

            val activePreset = state.presetGroups
                .flatMap { it.presetArrayList }
                .firstOrNull { it.id == state.currentPresetId }
            val presetName = when {
                activePreset == null -> "ID ${state.currentPresetId}"
                activePreset.hasCustomName() -> activePreset.customName
                activePreset.hasTitleId() ->
                    GoProPresetMappings.getPresetTitle(activePreset.titleId.number)
                        ?.let { stringResource(it) } ?: "ID ${state.currentPresetId}"
                else -> "ID ${state.currentPresetId}"
            }
            InfoRow(stringResource(R.string.status_info_active_preset), presetName, Icons.Default.DashboardCustomize)

            if (state.firmwareVersion.isNotEmpty()) {
                HorizontalDivider(color = appColors.divider)
                InfoRow(stringResource(R.string.status_info_firmware), state.firmwareVersion, Icons.Default.SystemUpdate)
            }
            if (state.serialNumber.isNotEmpty()) {
                HorizontalDivider(color = appColors.divider)
                InfoRow(stringResource(R.string.status_info_serial), state.serialNumber, Icons.Default.Badge)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, icon: ImageVector) {
    val appColors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = appColors.accent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label.uppercase(),
                color = appColors.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = value,
            color = appColors.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )
    }
}
