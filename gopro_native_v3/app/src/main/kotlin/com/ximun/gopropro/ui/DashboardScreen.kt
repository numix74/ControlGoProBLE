package com.ximun.gopropro.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ximun.gopropro.ui.theme.AppPrimary
import com.ximun.gopropro.ui.theme.HilightYellow
import com.ximun.gopropro.ui.theme.LocalAppColors
import com.ximun.gopropro.ui.theme.PrimaryTeal
import com.ximun.gopropro.viewmodel.CameraUiState


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
                0 -> DashboardScreen(state, onRecordToggle, onHilight, onDisconnect, onSleep, onToggleTimerMode, onAdjustTimer)
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
    onAdjustTimer: (Int) -> Unit
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
        TimerSection(state, onToggleTimerMode, onAdjustTimer)
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
    onAdjustTimer: (Int) -> Unit
) {
    // Optimization: avoid re-calculating strings if values don't change
    val timerLabel = remember(state.isCountdownActive) { if (state.isCountdownActive) "REBOURS" else "DURÉE" }
    val timerColor = remember(state.isCountdownActive) { if (state.isCountdownActive) HilightYellow else PrimaryTeal }

    val appColors = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(appColors.card)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timerLabel,
                color = timerColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = state.displayTime,
                color = appColors.textPrimary,
                fontSize = 72.sp,
                fontWeight = FontWeight.Light
            )
        }
        
        TimerControls(state, onToggleTimerMode, onAdjustTimer, Modifier.align(Alignment.TopEnd))
    }
}

@Composable
private fun TimerControls(
    state: CameraUiState,
    onToggleTimerMode: () -> Unit,
    onAdjustTimer: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
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

        if (state.isTimerModeEnabled && !state.isRecording && !state.isCountdownActive) {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Remove, null, tint = appColors.textSecondary, modifier = Modifier.size(16.dp).clickable { onAdjustTimer(-5) })
                Text(text = "${state.initialTimerValue}s", color = appColors.textPrimary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp))
                Icon(Icons.Default.Add, null, tint = appColors.textSecondary, modifier = Modifier.size(16.dp).clickable { onAdjustTimer(5) })
            }
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
