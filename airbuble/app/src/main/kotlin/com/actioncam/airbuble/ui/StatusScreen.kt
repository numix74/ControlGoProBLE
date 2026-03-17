package com.actioncam.airbuble.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.actioncam.airbuble.camera.StorageInfo
import com.actioncam.airbuble.ui.theme.LocalAppColors

@Composable
fun StatusScreen(
    modifier: Modifier = Modifier,
    cameraModel: String,
    cameraSerial: String,
    firmwareVersion: String,
    batteryLevel: Int,
    isCharging: Boolean,
    storageInfo: StorageInfo,
    isOverheating: Boolean,
    isRecording: Boolean,
    waypointCount: Int,
    captureMode: String
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LocalAppColors.current.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Text("Statut caméra", color = LocalAppColors.current.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))

        BatteryCard(batteryLevel, isCharging)
        Spacer(Modifier.height(12.dp))

        StorageCard(storageInfo)
        Spacer(Modifier.height(12.dp))

        InfoCard(
            cameraModel = cameraModel,
            cameraSerial = cameraSerial,
            firmwareVersion = firmwareVersion,
            isOverheating = isOverheating,
            isRecording = isRecording,
            waypointCount = waypointCount,
            captureMode = captureMode
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun BatteryCard(batteryLevel: Int, isCharging: Boolean) {
    val batteryColor = when {
        batteryLevel < 20 -> LocalAppColors.current.error
        batteryLevel < 50 -> Color(0xFFF59E0B)
        else -> LocalAppColors.current.accent
    }
    StatusCard(title = "BATTERIE") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = if (batteryLevel > 0) "$batteryLevel%" else "—",
                color = LocalAppColors.current.textPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                    null,
                    tint = batteryColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = when {
                        isCharging    -> "EN CHARGE"
                        batteryLevel < 20 -> "CRITIQUE"
                        else          -> "NOMINAL"
                    },
                    color = batteryColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(LocalAppColors.current.border)
        ) {
            if (batteryLevel > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((batteryLevel / 100f).coerceIn(0f, 1f))
                        .background(batteryColor)
                )
            }
        }
    }
}

@Composable
private fun StorageCard(storageInfo: StorageInfo) {
    val totalGb = storageInfo.totalSpaceBytes / 1_000_000_000.0
    val freeGb  = storageInfo.freeSpaceBytes  / 1_000_000_000.0
    val usedFraction = if (storageInfo.totalSpaceBytes > 0)
        1f - (storageInfo.freeSpaceBytes.toFloat() / storageInfo.totalSpaceBytes)
    else 0f
    val storageColor = when {
        usedFraction > 0.9f -> LocalAppColors.current.error
        usedFraction > 0.75f -> Color(0xFFF59E0B)
        else -> LocalAppColors.current.accent
    }

    StatusCard(title = "STOCKAGE") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = if (storageInfo.sdCardPresent) "%.1f Go libres".format(freeGb) else "—",
                color = LocalAppColors.current.textPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = if (storageInfo.sdCardPresent) "/ %.0f Go".format(totalGb) else "PAS DE CARTE",
                color = LocalAppColors.current.textDim,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(LocalAppColors.current.border)
        ) {
            if (storageInfo.sdCardPresent) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(usedFraction.coerceIn(0f, 1f))
                        .background(storageColor)
                )
            }
        }
        if (storageInfo.sdCardPresent && storageInfo.totalSpaceBytes > 0) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "%.0f%% utilisé".format(usedFraction * 100),
                color = LocalAppColors.current.textDim,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun InfoCard(
    cameraModel: String,
    cameraSerial: String,
    firmwareVersion: String,
    isOverheating: Boolean,
    isRecording: Boolean,
    waypointCount: Int,
    captureMode: String
) {
    StatusCard(title = "INFORMATIONS") {
        val rows = buildList {
            add(Triple(Icons.Default.Videocam,       "MODÈLE",    cameraModel.ifEmpty { "—" }))
            if (cameraSerial.isNotEmpty())
                add(Triple(Icons.Default.Badge,          "N° SÉRIE",  cameraSerial))
            if (firmwareVersion.isNotEmpty())
                add(Triple(Icons.Default.SystemUpdate,   "FIRMWARE",  firmwareVersion))
            add(Triple(Icons.Default.DeviceThermostat, "TEMPÉRATURE",
                if (isOverheating) "SURCHAUFFE" else "NORMALE"))
            add(Triple(
                if (isRecording) Icons.Default.RadioButtonChecked else Icons.Default.CheckCircle,
                "ÉTAT",
                if (isRecording) "ENREGISTREMENT" else "PRÊT"
            ))
            if (captureMode.isNotEmpty())
                add(Triple(Icons.Default.Camera,         "MODE",      captureMode))
            add(Triple(Icons.Default.LocationOn,         "WAYPOINTS", "$waypointCount"))
        }

        rows.forEachIndexed { i, (icon, label, value) ->
            if (i > 0) HorizontalDivider(color = LocalAppColors.current.border, thickness = 0.5.dp)
            InfoRow(icon, label, value, if (label == "TEMPÉRATURE" && isOverheating) LocalAppColors.current.error else Color.White)
        }
    }
}

@Composable
private fun StatusCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = LocalAppColors.current.card,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, LocalAppColors.current.border)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, color = LocalAppColors.current.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String, valueColor: Color = Color.White) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = LocalAppColors.current.accent, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(10.dp))
            Text(label, color = LocalAppColors.current.textDim, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Text(value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}
