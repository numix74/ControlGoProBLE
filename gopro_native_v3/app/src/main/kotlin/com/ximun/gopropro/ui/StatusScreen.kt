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
        headerSection("SYSTEM STATUS", "DIAGNOSTIC TEMPS RÉEL")

        Spacer(modifier = Modifier.height(24.dp))

        // Grille Batterie & Température
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Carte Batterie
            StatusGridCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.BatteryFull, // Transformé en BatteryHalf dans la logique d'icône si besoin
                iconColor = PrimaryTeal,
                iconRotation = -90f,
                badgeText = if (state.batteryLevel < 20) "CRITIQUE" else "NOMINAL",
                badgeColor = if (state.batteryLevel < 20) Color(0xFFEF4444) else PrimaryTeal, // Red / Teal
                badgeBg = if (state.batteryLevel < 20) Color(0xFFEF4444).copy(alpha = 0.2f) else PrimaryTeal.copy(alpha = 0.2f),
                mainValue = "${state.batteryLevel}%",
                label = "BATTERIE"
            )

            // Carte Température (Microchip icon replacement by Memory/DeveloperBoard default)
            StatusGridCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.DeviceThermostat,
                iconColor = PrimaryTeal,
                badgeText = "HERO 11 Mini", // Hardcoded ou dynamique si on avait le modèle
                badgeColor = Color.LightGray,
                badgeBg = Color.White.copy(alpha = 0.1f),
                mainValue = state.tempStatus,
                label = "TEMPÉRATURE"
            )
        }

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
fun StatusGridCard(
    modifier: Modifier,
    icon: ImageVector,
    iconColor: Color,
    iconRotation: Float = 0f,
    badgeText: String,
    badgeColor: Color,
    badgeBg: Color,
    mainValue: String,
    label: String
) {
    Surface(
        modifier = modifier.height(144.dp),
        color = AppCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    icon, // Pas de rotation simple modifier dans Icon, on utilise Modifier.rotate si besoin mais Icon n'a pas ça direct sans dépendance extra parfois. 
                    // Utilisons un Box pour rotater si besoin.
                    contentDescription = null, 
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
                
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            // Bottom Content
            Column {
                Text(
                    text = mainValue,
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = label,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
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
            InfoRow("État Système", if (state.isRecording) "ENREGISTREMENT" else "PRÊT", if (state.isRecording) Icons.Default.RadioButtonChecked else Icons.Default.CheckCircle)
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            InfoRow("Capacité SD", state.sdCapacityFormatted, Icons.Default.SdStorage)
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            InfoRow("Temps Restant", state.videoRemainingTime, Icons.Default.Timer)
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            InfoRow("Preset Actif", "ID ${state.currentPresetId}", Icons.Default.Tune)
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            InfoRow("Firmware", state.firmwareVersion, Icons.Default.Code)
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            InfoRow("Serial", state.serialNumber, Icons.Default.Fingerprint)
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
                tint = PrimaryTeal.copy(alpha = 0.6f),
                modifier = Modifier.size(12.dp)
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
