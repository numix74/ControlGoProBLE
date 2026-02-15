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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ximun.gopropro.GoProPresetMappings
import com.ximun.gopropro.ui.theme.AppCard
import com.ximun.gopropro.ui.theme.PrimaryTeal
import com.ximun.gopropro.viewmodel.CameraUiState

@Composable
fun StatusScreen(state: CameraUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()) // Ajout du scroll si petit écran
    ) {
        // En-tête
        HeaderSection(title = "SYSTEM STATUS", subtitle = "DIAGNOSTIC TEMPS RÉEL")

        Spacer(modifier = Modifier.height(24.dp))

        // Section Batterie (Full Width)
        BatterySection(state)

        Spacer(modifier = Modifier.height(24.dp))

        // Section Stockage
        StorageSection(state)

        Spacer(modifier = Modifier.height(24.dp))

        // Liste Infos Système
        SystemInfoList(state)
        
        Spacer(modifier = Modifier.height(80.dp)) // Spacer pour le bas
    }
}

@Composable
fun BatterySection(state: CameraUiState) {
    Surface(
        color = AppCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "ALIMENTATION",
                color = Color.Gray,
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
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = if (state.isCharging) "EN CHARGE" else if (state.batteryLevel < 20) "CRITIQUE" else "NOMINAL",
                    color = if (state.isCharging) PrimaryTeal else if (state.batteryLevel < 20) Color(0xFFEF4444) else PrimaryTeal,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress Bar Batterie
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.05f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(state.batteryLevel / 100f)
                        .background(if (state.isCharging) PrimaryTeal else if (state.batteryLevel < 20) Color(0xFFEF4444) else PrimaryTeal)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                 Icon(if (state.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                 Spacer(modifier = Modifier.width(4.dp))
                 Text(text = if (state.isCharging) "SECTEUR" else "INTERNE", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StorageSection(state: CameraUiState) {
    Surface(
        color = AppCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "STATISTIQUES STOCKAGE",
                color = Color.Gray,
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
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "RESTANT",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress Bar Custom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.05f))
            ) {
                // Barre de progression (width basé sur le %)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(state.storagePercent / 100f)
                        .background(PrimaryTeal)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${state.storagePercent}% PLEIN",
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "CARTE ${state.sdStatusLabel}",
                    color = if (state.sdStatusLabel != "OK") Color(0xFFEF4444) else Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "SD CARD (V30)",
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SystemInfoList(state: CameraUiState) {
    Surface(
        color = AppCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column {
            // Modèle caméra
            InfoRow(state.cameraName.ifEmpty { "GoPro" }, "OK", Icons.Default.Videocam)
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

            // État système
            InfoRow("État Système", if (state.isRecording) "ENREGISTREMENT" else "PRÊT", if (state.isRecording) Icons.Default.RadioButtonChecked else Icons.Default.CheckCircle)
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

            // Température
            InfoRow("Température", state.tempStatus, Icons.Default.DeviceThermostat)
            if (state.isOverheating) {
                HorizontalDivider(color = Color(0xFFEF4444))
                InfoRow("SURCHAUFFE", "DANGER", Icons.Default.Warning)
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

            // Alimentation
            if (state.isCharging) {
                InfoRow("Alimentation", "EN CHARGE", Icons.Default.Power)
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            }

            InfoRow("Photos Restantes", "${state.photosRemaining}", Icons.Default.PhotoCamera)
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            InfoRow("Vidéos sur Carte", "${state.videosCount}", Icons.Default.VideoLibrary)
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            InfoRow("Capacité SD", state.sdCapacityFormatted, Icons.Default.SdStorage)
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            InfoRow("Temps Restant", state.videoRemainingTime, Icons.Default.Timer)
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

            val activePreset = state.presetGroups
                .flatMap { it.presetArrayList }
                .firstOrNull { it.id == state.currentPresetId }
            val presetName = when {
                activePreset == null -> "ID ${state.currentPresetId}"
                activePreset.hasCustomName() -> activePreset.customName
                activePreset.hasTitleId() -> GoProPresetMappings.getPresetTitle(activePreset.titleId.number) ?: "ID ${state.currentPresetId}"
                else -> "ID ${state.currentPresetId}"
            }
            InfoRow("Preset Actif", presetName, Icons.Default.DashboardCustomize)

            // Firmware & Serial (si disponibles)
            if (state.firmwareVersion.isNotEmpty()) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                InfoRow("Firmware", state.firmwareVersion, Icons.Default.SystemUpdate)
            }
            if (state.serialNumber.isNotEmpty()) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                InfoRow("N° Série", state.serialNumber, Icons.Default.Badge)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, icon: ImageVector) {
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
                tint = PrimaryTeal.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label.uppercase(),
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )
    }
}
