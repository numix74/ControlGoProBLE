package com.actioncam.airbuble.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.actioncam.airbuble.BuildConfig
import com.actioncam.airbuble.camera.ConnectionState
import com.actioncam.airbuble.camera.ScannedDevice

// Theme colors
private val BgDark = Color(0xFF0F172A)
private val BgCard = Color(0xFF1E293B)
private val Accent = Color(0xFF4CC4C4)
private val TextDim = Color(0xFF94A3B8)
private val TextDimmer = Color(0xFF64748B)

@Composable
fun ConnectionScreen(
    connectionState: ConnectionState,
    scannedDevices: List<ScannedDevice>,
    wifiSsid: String,
    wifiPassword: String,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnectDevice: (ScannedDevice) -> Unit,
    onDisconnect: () -> Unit,
    onConnectDebugEmulator: (() -> Unit)? = null
) {
    val isScanning = connectionState == ConnectionState.SCANNING
    val isBleConnected = connectionState == ConnectionState.BLE_CONNECTED
    val isBleConnecting = connectionState == ConnectionState.BLE_CONNECTING
    val isWifiConnecting = connectionState == ConnectionState.WIFI_CONNECTING
    val isConnected = connectionState == ConnectionState.CONNECTED

    // Ping animation
    val infiniteTransition = rememberInfiniteTransition(label = "ping")
    val pingScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart),
        label = "scale"
    )
    val pingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart),
        label = "alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDark, BgCard)))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // BLE icon with ping
        Box(contentAlignment = Alignment.Center) {
            if (isScanning) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(pingScale)
                        .alpha(pingAlpha)
                        .border(1.dp, Accent, CircleShape)
                )
            }
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Accent.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isScanning) Icons.Default.BluetoothSearching
                        else Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("AirBuble", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(
            "INSTA360 CAMERA CONTROL",
            color = Accent, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 4.sp,
            modifier = Modifier.alpha(0.8f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Status card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.03f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Status indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (color, label) = when (connectionState) {
                        ConnectionState.DISCONNECTED -> Color(0xFFF59E0B) to "Prêt"
                        ConnectionState.SCANNING -> Accent to "Recherche..."
                        ConnectionState.BLE_CONNECTING -> Accent to "Connexion BLE..."
                        ConnectionState.BLE_CONNECTED -> Color(0xFF22C55E) to "BLE connecté"
                        ConnectionState.WIFI_CONNECTING -> Accent to "Connexion WiFi..."
                        ConnectionState.CONNECTED -> Color(0xFF22C55E) to "Connecté"
                        ConnectionState.ERROR -> Color(0xFFEF4444) to "Erreur"
                    }
                    Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(label, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                // WiFi credentials (shown after BLE connect)
                if (isBleConnected && wifiSsid.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Wifi, null, tint = Accent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SSID: $wifiSsid", color = TextDim, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Mot de passe: $wifiPassword", color = TextDimmer, fontSize = 12.sp,
                        modifier = Modifier.padding(start = 24.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Main action button
                when {
                    isConnected -> {
                        Button(
                            onClick = onDisconnect,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF4444),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Déconnecter", fontWeight = FontWeight.Bold)
                        }
                    }
                    isWifiConnecting -> {
                        Button(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.DarkGray,
                                contentColor = Color.White
                            )
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Accent, strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Connexion WiFi...", fontWeight = FontWeight.Bold)
                        }
                    }
                    isBleConnected -> {
                        Button(
                            onClick = onDisconnect,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF4444),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Déconnecter", fontWeight = FontWeight.Bold)
                        }
                    }
                    isScanning -> {
                        Button(
                            onClick = onStopScan,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.DarkGray,
                                contentColor = Color.White
                            )
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Accent, strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Arrêter le scan", fontWeight = FontWeight.Bold)
                        }
                    }
                    isBleConnecting -> {
                        Button(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.DarkGray,
                                contentColor = Color.White
                            )
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Accent, strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Connexion en cours...", fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {
                        Button(
                            onClick = onStartScan,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Accent,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(Icons.Default.BluetoothSearching, contentDescription = null)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Scanner", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scanned devices list
        if (scannedDevices.isNotEmpty() && !isBleConnected) {
            Text(
                "${scannedDevices.size} appareil(s) trouvé(s)",
                color = TextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(scannedDevices, key = { it.id }) { device ->
                    DeviceCard(
                        device = device,
                        isConnecting = isBleConnecting,
                        onClick = { onConnectDevice(device) }
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        // Debug button — emulator shortcut (DEBUG builds only)
        if (BuildConfig.DEBUG && onConnectDebugEmulator != null &&
            connectionState == ConnectionState.DISCONNECTED) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onConnectDebugEmulator,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
            ) {
                Text(
                    "⚡ Connexion émulateur",
                    color = Color(0xFFF59E0B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Footer
        Text(
            "AirBuble v0.1 — Insta360",
            color = Color.White.copy(alpha = 0.2f),
            fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp
        )
    }
}

@Composable
private fun DeviceCard(
    device: ScannedDevice,
    isConnecting: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isConnecting, onClick = onClick),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Bluetooth,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(device.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(device.id, color = TextDimmer, fontSize = 11.sp)
            }
            // RSSI indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.SignalCellular4Bar,
                    contentDescription = null,
                    tint = when {
                        device.rssi > -60 -> Color(0xFF22C55E)
                        device.rssi > -80 -> Color(0xFFF59E0B)
                        else -> Color(0xFFEF4444)
                    },
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${device.rssi} dBm",
                    color = TextDimmer,
                    fontSize = 11.sp
                )
            }
        }
    }
}
