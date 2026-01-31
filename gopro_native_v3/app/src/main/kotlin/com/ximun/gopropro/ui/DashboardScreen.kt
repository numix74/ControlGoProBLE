package com.ximun.gopropro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ximun.gopropro.viewmodel.CameraUiState

@Composable
fun DashboardScreen(
    state: CameraUiState,
    onRecordToggle: () -> Unit,
    onHilight: () -> Unit
) {
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
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GoPro HERO V3",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = state.statusText,
                        color = if (state.isConnected) Color(0xFF1DE9B6) else Color.Gray,
                        fontSize = 14.sp
                    )
                }
                
                // Indicateur Batterie
                Surface(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${state.batteryLevel}%",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(10.dp)
                                .background(Color.White.copy(alpha = 0.3f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Bouton REC Central (Pulse)
            Box(contentAlignment = Alignment.Center) {
                // Glow effect if recording
                if (state.isRecording) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(Color.Red.copy(alpha = 0.2f))
                    )
                }

                Button(
                    onClick = onRecordToggle,
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isRecording) Color.Red else Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Text(
                        text = if (state.isRecording) "STOP" else "REC",
                        color = if (state.isRecording) Color.White else Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Cartes d'infos (Glassmorphism)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                GlassCard(
                    modifier = Modifier.weight(1f),
                    title = "SD Card",
                    value = state.storageSpace
                )
                GlassCard(
                    modifier = Modifier.weight(1f),
                    title = "Preset",
                    value = state.currentPreset
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bouton HiLight
            OutlinedButton(
                onClick = onHilight,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
            ) {
                Text("ADD HILIGHT", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GlassCard(modifier: Modifier, title: String, value: String) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(24.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(Color.White.copy(0.2f), Color.Transparent)))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = title, color = Color.White.copy(0.5f), fontSize = 12.sp)
            Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}
