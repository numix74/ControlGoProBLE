package com.actioncam.airbuble.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.actioncam.airbuble.camera.StorageInfo
import com.actioncam.airbuble.ui.theme.LocalAppColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    cameraModel: String,
    batteryLevel: Int = 0,
    isCharging: Boolean = false,
    storageInfo: StorageInfo = StorageInfo(),
    isRecording: Boolean,
    isCountdownActive: Boolean,
    isTimerModeEnabled: Boolean,
    displayTime: String,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onTakePhoto: () -> Unit,
    onMarkHilight: () -> Unit,
    onDisconnect: () -> Unit,
    onShutdownCamera: () -> Unit,
    onToggleTimerMode: () -> Unit,
    onAdjustTimer: (Int) -> Unit,
    onSnapTimer: () -> Unit
) {
    val c = LocalAppColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "rec")
    val recScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
        label = "scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(c.background, c.card)))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Header
        Text(cameraModel, color = c.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        val statusLabel = when {
            isCountdownActive -> "● REBOURS"
            isRecording       -> "● REC"
            else              -> "PRÊT"
        }
        val statusColor = when {
            isCountdownActive -> c.timerYellow
            isRecording       -> c.recordRed
            else              -> c.accent
        }
        Text(statusLabel, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)

        Spacer(modifier = Modifier.height(8.dp))
        MiniStatusBar(batteryLevel = batteryLevel, isCharging = isCharging, storageInfo = storageInfo)

        Spacer(modifier = Modifier.weight(1f))

        // Timer section
        TimerSection(
            displayTime = displayTime,
            isTimerModeEnabled = isTimerModeEnabled,
            isRecording = isRecording,
            isCountdownActive = isCountdownActive,
            onToggleTimerMode = onToggleTimerMode,
            onAdjustTimer = onAdjustTimer,
            onSnapTimer = onSnapTimer
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Record button
        val btnScale = if (isRecording || isCountdownActive) recScale else 1f
        val btnColor = when {
            isCountdownActive -> c.timerYellow
            isRecording       -> c.recordRed
            else              -> c.recordRed.copy(alpha = 0.85f)
        }
        Surface(
            modifier = Modifier.size(100.dp).scale(btnScale),
            shape = CircleShape,
            color = btnColor,
            shadowElevation = if (isRecording || isCountdownActive) 12.dp else 4.dp,
            onClick = { if (isRecording || isCountdownActive) onStopRecording() else onStartRecording() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isRecording || isCountdownActive) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White, modifier = Modifier.size(48.dp))
                } else {
                    Box(modifier = Modifier.size(52.dp).background(Color.White, CircleShape))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            when { isCountdownActive -> "Annuler"; isRecording -> "Arrêter"; else -> "Enregistrer" },
            color = c.textDim, fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Secondary buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(56.dp), shape = CircleShape,
                    color = c.textPrimary.copy(alpha = 0.08f), onClick = onTakePhoto
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CameraAlt, null, tint = c.accent, modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Photo", color = c.textDim, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(56.dp), shape = CircleShape,
                    color = c.textPrimary.copy(alpha = 0.08f), onClick = onMarkHilight
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.LocationOn, null, tint = c.accent, modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Waypoint", color = c.textDim, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(56.dp), shape = CircleShape,
                    color = c.textPrimary.copy(alpha = 0.08f), onClick = onShutdownCamera
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PowerSettingsNew, null, tint = c.textDim, modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Éteindre", color = c.textDim, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = onDisconnect,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = c.textDim),
            border = BorderStroke(1.dp, c.textPrimary.copy(alpha = 0.1f))
        ) { Text("Déconnecter", fontSize = 13.sp) }

        Spacer(modifier = Modifier.height(8.dp))
        Text("AirBuble v0.1 — Insta360", color = c.textPrimary.copy(alpha = 0.15f), fontSize = 9.sp, letterSpacing = 2.sp)
    }
}

@Composable
private fun TimerSection(
    displayTime: String,
    isTimerModeEnabled: Boolean,
    isRecording: Boolean,
    isCountdownActive: Boolean,
    onToggleTimerMode: () -> Unit,
    onAdjustTimer: (Int) -> Unit,
    onSnapTimer: () -> Unit
) {
    val c = LocalAppColors.current
    val showAdjust = isTimerModeEnabled && !isRecording && !isCountdownActive
    val displayColor = when {
        isCountdownActive  -> c.timerYellow
        isRecording        -> c.recordRed
        isTimerModeEnabled -> c.textPrimary
        else               -> c.textPrimary.copy(alpha = 0.3f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(c.card)
            .padding(vertical = 20.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (isTimerModeEnabled || isCountdownActive) {
                Text(
                    text = if (isCountdownActive) "REBOURS" else "DURÉE",
                    color = if (isCountdownActive) c.timerYellow else c.accent,
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (showAdjust) {
                    RepeatableIconButton(
                        icon = Icons.Default.Remove, tint = c.textDim,
                        onSingleClick = { onAdjustTimer(-5) },
                        onRepeatTick = { onAdjustTimer(-1) },
                        onSnapRelease = onSnapTimer
                    )
                } else {
                    Spacer(modifier = Modifier.size(44.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = displayTime,
                    color = displayColor,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (showAdjust) {
                    RepeatableIconButton(
                        icon = Icons.Default.Add, tint = c.textDim,
                        onSingleClick = { onAdjustTimer(5) },
                        onRepeatTick = { onAdjustTimer(1) },
                        onSnapRelease = onSnapTimer
                    )
                } else {
                    Spacer(modifier = Modifier.size(44.dp))
                }
            }
        }

        // Timer toggle (top-right corner)
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 4.dp, end = 4.dp)
                .size(40.dp)
                .clickable { onToggleTimerMode() },
            color = if (isTimerModeEnabled) c.timerYellow.copy(alpha = 0.15f) else Color.Transparent,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, if (isTimerModeEnabled) c.timerYellow else c.textPrimary.copy(alpha = 0.15f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Timer, null,
                    tint = if (isTimerModeEnabled) c.timerYellow else c.textDim,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun MiniStatusBar(batteryLevel: Int, isCharging: Boolean, storageInfo: StorageInfo) {
    if (batteryLevel == 0 && !storageInfo.sdCardPresent) return
    val c = LocalAppColors.current
    val batteryColor = when {
        batteryLevel in 1..19 -> c.error
        else -> c.textDim
    }
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (batteryLevel > 0) {
            Icon(
                if (isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                null, tint = batteryColor, modifier = Modifier.size(13.dp)
            )
            Spacer(Modifier.width(3.dp))
            Text("$batteryLevel%", color = batteryColor, fontSize = 11.sp)
        }
        if (batteryLevel > 0 && storageInfo.sdCardPresent) {
            Spacer(Modifier.width(14.dp))
            Text("·", color = c.textDim.copy(alpha = 0.4f), fontSize = 11.sp)
            Spacer(Modifier.width(14.dp))
        }
        if (storageInfo.sdCardPresent) {
            val freeGb = storageInfo.freeSpaceBytes / 1_000_000_000.0
            Icon(Icons.Default.SdStorage, null, tint = c.textDim, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(3.dp))
            Text("%.0f Go libres".format(freeGb), color = c.textDim, fontSize = 11.sp)
        }
    }
}

@Composable
private fun RepeatableIconButton(
    icon: ImageVector,
    tint: Color,
    onSingleClick: () -> Unit,
    onRepeatTick: () -> Unit,
    onSnapRelease: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var longPressJob by remember { mutableStateOf<Job?>(null) }

    Surface(
        modifier = Modifier
            .size(44.dp)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitPointerEvent()
                        if (down.changes.any { it.pressed }) {
                            var wasLongPress = false
                            longPressJob = scope.launch {
                                delay(400)
                                wasLongPress = true
                                while (true) {
                                    onRepeatTick()
                                    delay(120)
                                }
                            }
                            do {
                                val event = awaitPointerEvent()
                            } while (event.changes.any { it.pressed })

                            longPressJob?.cancel()
                            longPressJob = null
                            if (wasLongPress) onSnapRelease() else onSingleClick()
                        }
                    }
                }
            },
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(32.dp))
        }
    }
}
