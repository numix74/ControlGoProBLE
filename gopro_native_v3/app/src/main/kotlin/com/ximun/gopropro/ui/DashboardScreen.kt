package com.ximun.gopropro.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ximun.gopropro.viewmodel.CameraUiState

// Design tokens
val AppBackground = Color(0xFF0F172A)
val AppCard = Color(0xFF1E293B)
val AppPrimary = Color(0xFF3B82F6)
val PrimaryTeal = Color(0xFF4CC4C4)
val HilightYellow = Color(0xFFCA8A04)

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
    onLoadPreset: (Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    Scaffold(
        bottomBar = {
            DashboardNavBar(selectedTab = state.selectedTab, onTabSelected = onTabSelected)
        },
        containerColor = AppBackground
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (state.selectedTab) {
                0 -> DashboardScreen(state, onRecordToggle, onHilight, onDisconnect, onSleep, onReboot, onSyncTime, onToggleTimerMode, onAdjustTimer)
                1 -> SettingsScreen(state, onUpdateSetting)
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
    onReboot: () -> Unit,
    onSyncTime: () -> Unit,
    onToggleTimerMode: () -> Unit,
    onAdjustTimer: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            headerSection("STUDIO PRO", "LIAISON DIRECTE")
            Row {
                IconButton(onClick = onSleep) {
                    Icon(Icons.Default.PowerSettingsNew, "Veille", tint = Color.LightGray)
                }
                IconButton(onClick = onDisconnect) {
                    Icon(Icons.Default.LinkOff, "Déconnexion", tint = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Status Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatusCard(Modifier.weight(1f), "BATTERIE", "${state.batteryLevel}%", Icons.Default.BatteryChargingFull)
            StatusCard(Modifier.weight(1f), "STOCKAGE", state.storageSpace, Icons.Default.Storage)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Central Chrono Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(AppCard)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (state.isCountdownActive) "REBOURS" else "DURÉE",
                    color = if (state.isCountdownActive) HilightYellow else PrimaryTeal,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = state.displayTime,
                    color = Color.White,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Light
                )
            }
            
            // Timer Button & Controls
            Column(
                modifier = Modifier.align(Alignment.TopEnd),
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
                            tint = if (state.isTimerModeEnabled) HilightYellow else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                if (state.isTimerModeEnabled && !state.isRecording && !state.isCountdownActive) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Remove, null, tint = Color.Gray, modifier = Modifier.size(16.dp).clickable { onAdjustTimer(-5) })
                        Text(text = "${state.initialTimerValue}s", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp))
                        Icon(Icons.Default.Add, null, tint = Color.Gray, modifier = Modifier.size(16.dp).clickable { onAdjustTimer(5) })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Execution Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ControlCard(
                modifier = Modifier.weight(1.2f),
                title = if (state.isRecording || state.isCountdownActive) "STOP CAPTURE" else "START CAPTURE",
                icon = if (state.isRecording || state.isCountdownActive) Icons.Default.Stop else Icons.Default.RadioButtonChecked,
                iconColor = if (state.isRecording || state.isCountdownActive) Color.White else Color.Red,
                backgroundColor = if (state.isRecording || state.isCountdownActive) AppCard else Color.Red.copy(alpha = 0.1f),
                borderColor = if (state.isRecording || state.isCountdownActive) Color.Gray else Color.Red,
                onClick = onRecordToggle
            )

            ControlCard(
                modifier = Modifier.weight(1f),
                title = "Hilight",
                icon = Icons.Default.AutoAwesome,
                iconColor = if (state.isRecording) HilightYellow else Color.Gray,
                backgroundColor = AppCard,
                borderColor = if (state.isRecording) HilightYellow else Color.DarkGray,
                enabled = state.isRecording,
                onClick = onHilight
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Actions Rapides
        Text("ACTIONS RAPIDES", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = "Sync Horloge",
                icon = Icons.Default.Sync,
                onClick = onSyncTime
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = "Redémarrer",
                icon = Icons.Default.RestartAlt,
                onClick = onReboot
            )
        }
    }
}

@Composable
fun QuickActionButton(
    modifier: Modifier,
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .clickable { onClick() },
        color = AppCard.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = PrimaryTeal, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, icon: ImageVector) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, color = Color.Gray, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("Prochainement disponible", color = Color.DarkGray, fontSize = 12.sp)
        }
    }
}

@Composable
fun StatusCard(modifier: Modifier, title: String, value: String, icon: ImageVector) {
    Surface(
        modifier = modifier,
        color = AppCard,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = AppPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
            Text(title, color = if (enabled) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DashboardNavBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        color = AppCard,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem("Controle", Icons.Default.Dashboard, selectedTab == 0) { onTabSelected(0) }
            NavItem("Réglages", Icons.Default.Settings, selectedTab == 1) { onTabSelected(1) }
            NavItem("Presets", Icons.Default.Tune, selectedTab == 2) { onTabSelected(2) }
            NavItem("Status", Icons.Default.Info, selectedTab == 3) { onTabSelected(3) }
        }
    }
}

@Composable
fun NavItem(title: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = if (isSelected) AppPrimary else Color.Gray, modifier = Modifier.size(24.dp))
        Text(title, color = if (isSelected) Color.White else Color.Gray, fontSize = 10.sp)
    }
}

@Composable
fun headerSection(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = subtitle,
            color = PrimaryTeal,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}
