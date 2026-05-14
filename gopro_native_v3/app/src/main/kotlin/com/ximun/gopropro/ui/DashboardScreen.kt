@file:OptIn(androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi::class)

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
import androidx.compose.material3.windowsizeclass.WindowSizeClass
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
import androidx.compose.ui.res.stringResource
import com.ximun.gopropro.R
import com.ximun.gopropro.ui.theme.HilightYellow
import com.ximun.gopropro.ui.theme.LocalAppColors
import com.ximun.gopropro.viewmodel.CameraUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun DashboardLayout(
    windowSizeClass: WindowSizeClass,
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
    onToggleBubble: () -> Unit = {},
    onToggleAutoSync: () -> Unit = {},
    onLanguageChange: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val appColors = LocalAppColors.current
    val isLandscape = windowSizeClass.isLandscape

    @Composable
    fun TabContent() {
        when (state.selectedTab) {
            0 -> DashboardScreen(state, isLandscape, onRecordToggle, onHilight, onDisconnect, onSleep, onToggleTimerMode, onAdjustTimer, onSnapTimer)
            1 -> SettingsScreen(state, isLandscape, onUpdateSetting, onSyncTime, onReboot, onToggleDarkMode, onToggleBubble, onToggleAutoSync, onLanguageChange)
            2 -> PresetsScreen(state, isLandscape, onLoadPreset)
            3 -> StatusScreen(state, isLandscape)
        }
    }

    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors.background)
        ) {
            DashboardNavRail(selectedTab = state.selectedTab, onTabSelected = onTabSelected)
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                TabContent()
            }
        }
    } else {
        Scaffold(
            bottomBar = {
                DashboardNavBar(selectedTab = state.selectedTab, onTabSelected = onTabSelected)
            },
            containerColor = appColors.background
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                Box(modifier = Modifier.weight(1f)) {
                    TabContent()
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    state: CameraUiState,
    isLandscape: Boolean,
    onRecordToggle: () -> Unit,
    onHilight: () -> Unit,
    onDisconnect: () -> Unit,
    onSleep: () -> Unit,
    onToggleTimerMode: () -> Unit,
    onAdjustTimer: (Int) -> Unit,
    onSnapTimer: () -> Unit
) {
    val bottomSpacer = if (isLandscape) 24.dp else 80.dp

    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Colonne gauche : header + stats + timer
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(end = 12.dp)
            ) {
                HeaderSection(
                    title = stringResource(R.string.dashboard_title),
                    subtitle = stringResource(R.string.dashboard_subtitle),
                    actions = {
                        Row {
                            IconButton(onClick = onDisconnect) {
                                Icon(Icons.Default.LinkOff, stringResource(R.string.dashboard_btn_disconnect), tint = LocalAppColors.current.textSecondary)
                            }
                            IconButton(onClick = onSleep) {
                                Icon(Icons.Default.PowerSettingsNew, stringResource(R.string.dashboard_btn_sleep), tint = LocalAppColors.current.textPrimary)
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                StatsSection(state)
                Spacer(modifier = Modifier.height(16.dp))
                TimerSection(state, timerFontSize = 56, onToggleTimerMode, onAdjustTimer, onSnapTimer)
                Spacer(modifier = Modifier.height(bottomSpacer))
            }
            // Colonne droite : contrôles d'enregistrement centrés
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                RecordingControls(state, onRecordToggle, onHilight)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            HeaderSection(
                title = stringResource(R.string.dashboard_title),
                subtitle = stringResource(R.string.dashboard_subtitle),
                actions = {
                    Row {
                        IconButton(onClick = onDisconnect) {
                            Icon(Icons.Default.LinkOff, stringResource(R.string.dashboard_btn_disconnect), tint = LocalAppColors.current.textSecondary)
                        }
                        IconButton(onClick = onSleep) {
                            Icon(Icons.Default.PowerSettingsNew, stringResource(R.string.dashboard_btn_sleep), tint = LocalAppColors.current.textPrimary)
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
            StatsSection(state)
            Spacer(modifier = Modifier.height(32.dp))
            TimerSection(state, timerFontSize = 72, onToggleTimerMode, onAdjustTimer, onSnapTimer)
            Spacer(modifier = Modifier.height(40.dp))
            RecordingControls(state, onRecordToggle, onHilight)
            Spacer(modifier = Modifier.height(bottomSpacer))
        }
    }
}

@Composable
private fun StatsSection(state: CameraUiState) {
    val appColors = LocalAppColors.current
    val batteryIcon = when {
        state.isCharging -> Icons.Default.BatteryChargingFull
        state.batteryLevel <= 15 -> Icons.Default.Battery1Bar
        state.batteryLevel <= 35 -> Icons.Default.Battery3Bar
        state.batteryLevel <= 55 -> Icons.Default.Battery4Bar
        state.batteryLevel <= 80 -> Icons.Default.Battery5Bar
        else -> Icons.Default.BatteryFull
    }
    val batteryColor = when {
        state.isCharging -> appColors.accent
        state.batteryLevel < 20 -> Color(0xFFDC2626)
        else -> appColors.accent
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatusCard(Modifier.weight(1f), stringResource(R.string.dashboard_stat_battery), "${state.batteryLevel}%", batteryIcon, batteryColor)
        StatusCard(Modifier.weight(1f), stringResource(R.string.dashboard_stat_storage), state.storageSpace, Icons.Default.SdCard, appColors.accent)
    }
}

@Composable
private fun TimerSection(
    state: CameraUiState,
    timerFontSize: Int,
    onToggleTimerMode: () -> Unit,
    onAdjustTimer: (Int) -> Unit,
    onSnapTimer: () -> Unit
) {
    val appColors = LocalAppColors.current
    val timerLabel = if (state.isCountdownActive) stringResource(R.string.dashboard_timer_countdown) else stringResource(R.string.dashboard_timer_duration)
    val timerColor = if (state.isCountdownActive) HilightYellow else appColors.accent
    val showAdjust = state.isTimerModeEnabled && !state.isRecording && !state.isCountdownActive
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
            Text(
                text = timerLabel,
                color = timerColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
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

                Text(
                    text = state.displayTime,
                    color = appColors.textPrimary,
                    fontSize = timerFontSize.sp,
                    fontWeight = FontWeight.Light
                )

                Spacer(modifier = Modifier.width(8.dp))

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

                            if (wasLongPress) {
                                onSnapRelease()
                            } else {
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
            title = if (isActive) stringResource(R.string.dashboard_btn_stop_capture) else stringResource(R.string.dashboard_btn_start_capture),
            icon = if (isActive) Icons.Default.Stop else Icons.Default.RadioButtonChecked,
            iconColor = if (isActive) appColors.textPrimary else Color.Red,
            backgroundColor = if (isActive) appColors.card else Color.Red.copy(alpha = 0.1f),
            borderColor = if (isActive) appColors.textSecondary else Color.Red,
            onClick = onRecordToggle
        )

        ControlCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.dashboard_btn_hilight),
            icon = Icons.Default.AutoAwesome,
            iconColor = if (state.isRecording) HilightYellow else appColors.textSecondary,
            backgroundColor = appColors.card,
            borderColor = if (state.isRecording) HilightYellow else appColors.border,
            enabled = state.isRecording,
            onClick = onHilight
        )
    }
}

@Composable
fun StatusCard(modifier: Modifier, title: String, value: String, icon: ImageVector, iconTint: Color = LocalAppColors.current.accent) {
    val appColors = LocalAppColors.current
    Surface(
        modifier = modifier,
        color = appColors.card,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
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
            NavItem(stringResource(R.string.nav_tab_control), Icons.Default.Videocam, selectedTab == 0) { onTabSelected(0) }
            NavItem(stringResource(R.string.nav_tab_settings), Icons.Default.Settings, selectedTab == 1) { onTabSelected(1) }
            NavItem(stringResource(R.string.nav_tab_presets), Icons.Default.DashboardCustomize, selectedTab == 2) { onTabSelected(2) }
            NavItem(stringResource(R.string.nav_tab_status), Icons.Default.Info, selectedTab == 3) { onTabSelected(3) }
        }
    }
}

@Composable
fun DashboardNavRail(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val appColors = LocalAppColors.current
    NavigationRail(
        containerColor = appColors.card
    ) {
        Spacer(modifier = Modifier.weight(1f))
        NavigationRailItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.Videocam, null, modifier = Modifier.size(24.dp)) },
            label = { Text(stringResource(R.string.nav_tab_control), fontSize = 10.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = appColors.accent,
                selectedTextColor = appColors.textPrimary,
                unselectedIconColor = appColors.textSecondary,
                unselectedTextColor = appColors.textSecondary,
                indicatorColor = appColors.accent.copy(alpha = 0.15f)
            )
        )
        NavigationRailItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Default.Settings, null, modifier = Modifier.size(24.dp)) },
            label = { Text(stringResource(R.string.nav_tab_settings), fontSize = 10.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = appColors.accent,
                selectedTextColor = appColors.textPrimary,
                unselectedIconColor = appColors.textSecondary,
                unselectedTextColor = appColors.textSecondary,
                indicatorColor = appColors.accent.copy(alpha = 0.15f)
            )
        )
        NavigationRailItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(Icons.Default.DashboardCustomize, null, modifier = Modifier.size(24.dp)) },
            label = { Text(stringResource(R.string.nav_tab_presets), fontSize = 10.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = appColors.accent,
                selectedTextColor = appColors.textPrimary,
                unselectedIconColor = appColors.textSecondary,
                unselectedTextColor = appColors.textSecondary,
                indicatorColor = appColors.accent.copy(alpha = 0.15f)
            )
        )
        NavigationRailItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            icon = { Icon(Icons.Default.Info, null, modifier = Modifier.size(24.dp)) },
            label = { Text(stringResource(R.string.nav_tab_status), fontSize = 10.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = appColors.accent,
                selectedTextColor = appColors.textPrimary,
                unselectedIconColor = appColors.textSecondary,
                unselectedTextColor = appColors.textSecondary,
                indicatorColor = appColors.accent.copy(alpha = 0.15f)
            )
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun NavItem(title: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val appColors = LocalAppColors.current
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = if (isSelected) appColors.accent else appColors.textSecondary, modifier = Modifier.size(24.dp))
        Text(title, color = if (isSelected) appColors.textPrimary else appColors.textSecondary, fontSize = 10.sp)
    }
}
