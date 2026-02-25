package com.ximun.gopropro.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ximun.gopropro.R
import kotlinx.coroutines.delay

@Composable
fun ConnectionScreen(
    isBleReady: Boolean,
    isBluetoothEnabled: Boolean,
    windowSizeClass: androidx.compose.material3.windowsizeclass.WindowSizeClass? = null,
    onConnect: () -> Unit
) {
    val isLandscape = windowSizeClass?.isLandscape ?: false
    var isScanning by remember { mutableStateOf(false) }

    // Animation pour le "ping" du logo Bluetooth
    val infiniteTransition = rememberInfiniteTransition(label = "ping")
    val pingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pingScale"
    )
    val pingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pingAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                )
            )
            .padding(24.dp)
    ) {
        // Blur effect background (simulé par un cercle radial)
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = (-50).dp)
                .alpha(0.1f)
                .background(Color(0xFF00B0FF), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isLandscape) Modifier.widthIn(max = 480.dp) else Modifier)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Box(contentAlignment = Alignment.Center) {
                // Ping animation (seulement si BT activé)
                if (isBluetoothEnabled) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .scale(pingScale)
                            .alpha(pingAlpha)
                            .border(1.dp, Color(0xFF00B0FF), CircleShape)
                    )
                }

                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = if (isBluetoothEnabled) Color(0xFF00B0FF).copy(alpha = 0.1f) else Color(0xFFEF4444).copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isBluetoothEnabled) Icons.Default.Bluetooth else Icons.Default.BluetoothDisabled,
                            contentDescription = null,
                            tint = if (isBluetoothEnabled) Color(0xFF00B0FF) else Color(0xFFEF4444),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.connection_title),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )

            Text(
                text = stringResource(R.string.connection_subtitle),
                color = Color(0xFF00B0FF),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                modifier = Modifier.alpha(0.8f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Card Status
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
                color = Color.White.copy(alpha = 0.03f),
                shape = RoundedCornerShape(24.dp),
                border = BoxDefaults.outlinedBorder
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.connection_status_label),
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            val indicatorColor = when {
                                !isBluetoothEnabled -> Color(0xFFEF4444)
                                isBleReady -> Color.Green
                                else -> Color(0xFFF59E0B)
                            }
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(indicatorColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = when {
                                    !isBluetoothEnabled -> stringResource(R.string.connection_status_bt_off)
                                    isBleReady -> stringResource(R.string.connection_status_ready)
                                    else -> stringResource(R.string.connection_status_init)
                                },
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Text(
                        text = when {
                            !isBluetoothEnabled -> stringResource(R.string.connection_msg_bt_off)
                            isBleReady -> stringResource(R.string.connection_msg_bt_ready)
                            else -> stringResource(R.string.connection_msg_bt_init)
                        },
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            isScanning = true
                            onConnect()
                        },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        enabled = isBleReady && isBluetoothEnabled && !isScanning,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isScanning) Color.DarkGray else Color(0xFF00B0FF),
                            contentColor = Color.Black
                        )
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFF00B0FF),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(stringResource(R.string.connection_btn_scan), fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(R.string.connection_btn_connect), fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(R.string.connection_footer),
                color = Color.White.copy(alpha = 0.2f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }

    // Reset scanning flag after connection or timeout (simulation)
    LaunchedEffect(isScanning) {
        if (isScanning) {
            delay(30_000)
            isScanning = false
        }
    }
}

object BoxDefaults {
    val outlinedBorder = BorderStroke(
        width = 1.dp,
        color = Color.White.copy(alpha = 0.05f)
    )
}
