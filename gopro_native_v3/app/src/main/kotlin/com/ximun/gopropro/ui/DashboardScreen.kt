package com.ximun.gopropro.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ximun.gopropro.ui.theme.AppPrimary
import com.ximun.gopropro.ui.theme.HilightYellow
import com.ximun.gopropro.ui.theme.LocalAppColors
import com.ximun.gopropro.ui.theme.PrimaryTeal
import com.ximun.gopropro.viewmodel.CameraUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun DashboardLayout(
    viewModel: com.ximun.gopropro.viewmodel.GoProViewModel,
    onRecordToggle: () -> Unit,
    onHilight: () -> Unit,
    onDisconnect: () -> Unit,
    onSleep: () -> Unit,
    onReboot: () -> Unit,
    onSyncTime: () -> Unit,
    onToggleTimerMode: () -> Unit,
    onAdjustTimer: (Int) -> Unit,
    onSnapTimer: () -> Unit,
    onTabSelected: (Int) -> Unit,
    onUpdateSetting: (Int, Int) -> Unit,
    onLoadPreset: (Int) -> Unit,
    onToggleDarkMode: () -> Unit = {},
    onToggleBubble: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val appColors = com.ximun.gopropro.ui.theme.LocalAppColors.current
    Scaffold(
        bottomBar = {
            DashboardNavBar(selectedTab = state.selectedTab, onTabSelected = onTabSelected)
        },
        containerColor = appColors.background
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (state.selectedTab) {
                0 -> DashboardScreen(state, onRecordToggle, onHilight, onDisconnect, onSleep, onToggleTimerMode, onAdjustTimer, onSnapTimer)
                1 -> SettingsScreen(state, onUpdateSetting, onSyncTime, onReboot, onToggleDarkMode, onToggleBubble)
                2 -> PresetsScreen(state, onLoadPreset)
                3 -> StatusScreen(state)
            }
        }
    }
}

@Composable
fun DashboardScreen(
    state: CameraUiState,
    onRecordToggle: () -> Unit,
    onHilight: () -> Unit,
    onDisconnect: () -> Unit,
    onSleep: () -> Unit,
    onToggleTimerMode: () -> Unit,
    onAdjustTimer: (Int) -> Unit,
    onSnapTimer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection(
            title = "STUDIO PRO",
            subtitle = "LIAISON DIRECTE",
            actions = {
                Row {
                    IconButton(onClick = onSleep) {
                        Icon(Icons.Default.PowerSettingsNew, "Veille", tint = Color.LightGray)
                    }
                    IconButton(onClick = onDisconnect) {
                        Icon(Icons.Default.LinkOff, "Déconnexion", tint = Color.Gray)
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        StatsSection(state)
        Spacer(modifier = Modifier.height(32.dp))
        TimerSection(state, onToggleTimerMode, onAdjustTimer, onSnapTimer)
        Spacer(modifier = Modifier.height(40.dp))
        RecordingControls(state, onRecordToggle, onHilight)
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun StatsSection(state: CameraUiState) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatusCard(Modifier.weight(1f), "BATTERIE", "${state.batteryLevel}%", Icons.Default.BatteryChargingFull)
        StatusCard(Modifier.weight(1f), "STOCKAGE", state.storageSpace, Icons.Default.SdCard)
    }
}

@Composable
private fun TimerSection(
    state: CameraUiState,
    onToggleTimerMode: () -> Unit,
    onAdjustTimer: (Int) -> Unit,
    onSnapTimer: () -> Unit
) {
    val timerLabel = remember(state.isCountdownActive) { if (state.isCountdownActive) "REBOURS" else "DURÉE" }
    val timerColor = remember(state.isCountdownActive) { if (state.isCountdownActive) HilightYellow else PrimaryTeal }
    val showAdjust = state.isTimerModeEnabled && !state.isRecording && !state.isCountdownActive

    val appColors = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(appColors.card)
            .padding(vertical = 24.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Label DURÉE / REBOURS — en haut, séparé du chrono
            Text(
                text = timerLabel,
                color = timerColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Chrono central avec +/- de chaque côté
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Bouton "-" à gauche du chrono
                if (showAdjust) {
                    RepeatableIconButton(
                        icon = Icons.Default.Remove,
                        tint = appColors.textSecondary,
                        onSingleClick = { onAdjustTimer(-5) },
                        onRepeatTick = { onAdjustTimer(-1) },
                        onSnapRelease = onSnapTimer
                    )
                } else {
                    Spacer(modifier = Modifier.size(44.dp))
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Chrono centré verticalement avec les +/-
                Text(
                    text = state.displayTime,
                    color = appColors.textPrimary,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Light
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Bouton "+" à droite du chrono
                if (showAdjust) {
                    RepeatableIconButton(
                        icon = Icons.Default.Add,
                        tint = appColors.textSecondary,
                        onSingleClick = { onAdjustTimer(5) },
                        onRepeatTick = { onAdjustTimer(1) },
                        onSnapRelease = onSnapTimer
                    )
                } else {
                    Spacer(modifier = Modifier.size(44.dp))
                }
            }
        }

        // Icône Timer — position fixe en haut à droite, 5dp du bord haut et droit
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 5.dp, end = 5.dp)
                .size(48.dp)
                .clickable { onToggleTimerMode() },
            color = if (state.isTimerModeEnabled) HilightYellow.copy(alpha = 0.2f) else Color.Transparent,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (state.isTimerModeEnabled) HilightYellow else Color.DarkGray)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Timer,
                    null,
                    tint = if (state.isTimerModeEnabled) HilightYellow else appColors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Bouton icône avec support appui long : tap simple = delta normal (5s),
 * appui long maintenu = répétition accélérée (1s par tick).
 * Au relâchement d'un appui long, onSnapRelease est appelé pour arrondir à 0/5.
 */
@Composable
private fun RepeatableIconButton(
    icon: ImageVector,
    tint: Color,
    onSingleClick: () -> Unit,
    onRepeatTick: () -> Unit,
    onSnapRelease: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var longPressJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    Surface(
        modifier = Modifier
            .size(44.dp)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitPointerEvent()
                        if (down.changes.any { it.pressed }) {
                            val startTime = System.currentTimeMillis()
                            var wasLongPress = false

                            // Lancer un job de répétition après le délai long press
                            longPressJob = scope.launch {
                                delay(400) // Seuil long press
                                wasLongPress = true
                                while (true) {
                                    onRepeatTick()
                                    delay(120)
                                }
                            }

                            // Attendre le relâchement
                            do {
                                val event = awaitPointerEvent()
                            } while (event.changes.any { it.pressed })

                            longPressJob?.cancel()
                            longPressJob = null

                            if (wasLongPress) {
                                // Fin d'appui long → arrondir à 0/5
                                onSnapRelease()
                            } else {
                                // Tap simple
                                onSingleClick()
                            }
                        }
                    }
                }
            },
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                null,
                tint = tint,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun RecordingControls(
    state: CameraUiState,
    onRecordToggle: () -> Unit,
    onHilight: () -> Unit
) {
    val appColors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val isActive = state.isRecording || state.isCountdownActive
        ControlCard(
            modifier = Modifier.weight(1.2f),
            title = if (isActive) "STOP CAPTURE" else "START CAPTURE",
            icon = if (isActive) Icons.Default.Stop else Icons.Default.RadioButtonChecked,
            iconColor = if (isActive) appColors.textPrimary else Color.Red,
            backgroundColor = if (isActive) appColors.card else Color.Red.copy(alpha = 0.1f),
            borderColor = if (isActive) appColors.textSecondary else Color.Red,
            onClick = onRecordToggle
        )

        ControlCard(
            modifier = Modifier.weight(1f),
            title = "Hilight",
            icon = Icons.Default.AutoAwesome,
            iconColor = if (state.isRecording) HilightYellow else appColors.textSecondary,
            backgroundColor = appColors.card,
            borderColor = if (state.isRecording) HilightYellow else Color.DarkGray,
            enabled = state.isRecording,
            onClick = onHilight
        )
    }
}

@Composable
fun StatusCard(modifier: Modifier, title: String, value: String, icon: ImageVector) {
    val appColors = LocalAppColors.current
    Surface(
        modifier = modifier,
        color = appColors.card,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = AppPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, color = appColors.textSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(value, color = appColors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ControlCard(
    modifier: Modifier,
    title: String,
    icon: ImageVector,
    iconColor: Color,
    backgroundColor: Color,
    borderColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val appColors = LocalAppColors.current
    Surface(
        modifier = modifier
            .height(100.dp)
            .clickable(enabled = enabled) { onClick() },
        color = backgroundColor,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, borderColor.copy(alpha = if (enabled) 1f else 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = iconColor.copy(alpha = if (enabled) 1f else 0.5f), modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = if (enabled) appColors.textPrimary else appColors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DashboardNavBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val appColors = LocalAppColors.current
    Surface(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        color = appColors.card,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem("Controle", Icons.Default.Videocam, selectedTab == 0) { onTabSelected(0) }
            NavItem("Réglages", Icons.Default.Settings, selectedTab == 1) { onTabSelected(1) }
            NavItem("Presets", Icons.Default.DashboardCustomize, selectedTab == 2) { onTabSelected(2) }
            NavItem("Status", Icons.Default.Info, selectedTab == 3) { onTabSelected(3) }
        }
    }
}

@Composable
fun NavItem(title: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val appColors = LocalAppColors.current
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = if (isSelected) AppPrimary else appColors.textSecondary, modifier = Modifier.size(24.dp))
        Text(title, color = if (isSelected) appColors.textPrimary else appColors.textSecondary, fontSize = 10.sp)
    }
}
