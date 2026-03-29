package com.actioncam.airbuble

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.actioncam.airbuble.bubble.BubbleController
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.actioncam.airbuble.ui.theme.DarkAppColors
import com.actioncam.airbuble.ui.theme.LightAppColors
import com.actioncam.airbuble.ui.theme.LocalAppColors
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.actioncam.airbuble.ui.ConnectionScreen
import com.actioncam.airbuble.ui.DashboardScreen
import com.actioncam.airbuble.ui.SettingsScreen
import com.actioncam.airbuble.ui.StatusScreen
import com.actioncam.airbuble.viewmodel.AirbubleViewModel

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var viewModel: AirbubleViewModel
    private lateinit var bubbleController: BubbleController

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        Log.d(TAG, "Permissions granted: $allGranted")
        if (allGranted) viewModel.startScan()
    }

    private val overlayLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { bubbleController.onOverlayPermissionResult() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[AirbubleViewModel::class.java]

        bubbleController = BubbleController(this, viewModel, lifecycleScope)
        bubbleController.setupCallbacks(
            onRecordToggle = {
                val state = viewModel.uiState.value
                if (state.isRecording || state.isCountdownActive) viewModel.stopRecording()
                else viewModel.startRecording()
            },
            onHilight = { viewModel.markHilight() }
        )
        bubbleController.startObserving(this, overlayLauncher)

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val appColors = if (uiState.isDarkMode) DarkAppColors else LightAppColors
            CompositionLocalProvider(LocalAppColors provides appColors) {
            MaterialTheme(
                colorScheme = if (uiState.isDarkMode) darkColorScheme() else lightColorScheme()
            ) {
                val devices by viewModel.scannedDevices.collectAsState()
                Surface(modifier = Modifier.fillMaxSize(), color = appColors.background) {
                    if (uiState.isCameraReady) {
                        Scaffold(
                            containerColor = Color.Transparent,
                            bottomBar = {
                                NavigationBar(containerColor = Color(0xFF1E293B)) {
                                    NavigationBarItem(
                                        selected = uiState.selectedTab == 0,
                                        onClick = { viewModel.selectTab(0) },
                                        icon = { Icon(Icons.Default.Videocam, null) },
                                        label = { Text("Contrôles") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color(0xFF4CC4C4),
                                            selectedTextColor = Color(0xFF4CC4C4),
                                            indicatorColor = Color(0xFF4CC4C4).copy(alpha = 0.15f),
                                            unselectedIconColor = Color(0xFF94A3B8),
                                            unselectedTextColor = Color(0xFF94A3B8)
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = uiState.selectedTab == 1,
                                        onClick = { viewModel.selectTab(1) },
                                        icon = { Icon(Icons.Default.Settings, null) },
                                        label = { Text("Réglages") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color(0xFF4CC4C4),
                                            selectedTextColor = Color(0xFF4CC4C4),
                                            indicatorColor = Color(0xFF4CC4C4).copy(alpha = 0.15f),
                                            unselectedIconColor = Color(0xFF94A3B8),
                                            unselectedTextColor = Color(0xFF94A3B8)
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = uiState.selectedTab == 2,
                                        onClick = { viewModel.selectTab(2) },
                                        icon = { Icon(Icons.Default.Info, null) },
                                        label = { Text("Statut") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color(0xFF4CC4C4),
                                            selectedTextColor = Color(0xFF4CC4C4),
                                            indicatorColor = Color(0xFF4CC4C4).copy(alpha = 0.15f),
                                            unselectedIconColor = Color(0xFF94A3B8),
                                            unselectedTextColor = Color(0xFF94A3B8)
                                        )
                                    )
                                }
                            }
                        ) { innerPadding ->
                            when (uiState.selectedTab) {
                                1 -> {
                                    val ctx = LocalContext.current
                                    SettingsScreen(
                                        modifier = Modifier.padding(innerPadding),
                                        availableModes = uiState.availableModes,
                                        availableSettings = uiState.availableSettings,
                                        currentModeId = uiState.captureModeId,
                                        isDarkMode = uiState.isDarkMode,
                                        isBubbleEnabled = uiState.isBubbleEnabled,
                                        isCameraReady = uiState.isCameraReady,
                                        diagnosticLineCount = viewModel.getDiagnosticLineCount(),
                                        onSwitchMode = { viewModel.switchMode(it) },
                                        onChangeSetting = { s, v -> viewModel.changeSetting(s, v) },
                                        onRefresh = { viewModel.loadModesAndSettings() },
                                        onToggleDarkMode = { viewModel.toggleDarkMode() },
                                        onToggleBubble = { viewModel.toggleBubble() },
                                        onSyncTime = { viewModel.syncTime() },
                                        onExportLogs = {
                                            viewModel.buildDiagnosticShareIntent(ctx)?.let { intent ->
                                                ctx.startActivity(Intent.createChooser(intent, "Partager les logs"))
                                            }
                                        }
                                    )
                                }
                                2 -> StatusScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    cameraModel = uiState.cameraModel,
                                    cameraSerial = uiState.cameraSerial,
                                    firmwareVersion = uiState.firmwareVersion,
                                    batteryLevel = uiState.batteryLevel,
                                    isCharging = uiState.isCharging,
                                    storageInfo = uiState.storageInfo,
                                    isOverheating = uiState.isOverheating,
                                    isRecording = uiState.isRecording,
                                    waypointCount = uiState.waypointCount,
                                    captureMode = uiState.captureMode
                                )
                                else -> DashboardScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    cameraModel = uiState.cameraModel,
                                    batteryLevel = uiState.batteryLevel,
                                    isCharging = uiState.isCharging,
                                    storageInfo = uiState.storageInfo,
                                    isRecording = uiState.isRecording,
                                    isCountdownActive = uiState.isCountdownActive,
                                    isTimerModeEnabled = uiState.isTimerModeEnabled,
                                    displayTime = uiState.displayTime,
                                    onStartRecording = { viewModel.startRecording() },
                                    onStopRecording = { viewModel.stopRecording() },
                                    onTakePhoto = { viewModel.takePhoto() },
                                    onMarkHilight = { viewModel.markHilight() },
                                    onDisconnect = { viewModel.disconnect() },
                                    onShutdownCamera = { viewModel.shutdownCamera() },
                                    onToggleTimerMode = { viewModel.toggleTimerMode() },
                                    onAdjustTimer = { viewModel.adjustTimer(it) },
                                    onSnapTimer = { viewModel.snapTimerToFive() }
                                )
                            }
                        }
                    } else {
                        ConnectionScreen(
                            connectionState = uiState.connectionState,
                            scannedDevices = devices,
                            wifiSsid = uiState.wifiSsid,
                            wifiPassword = uiState.wifiPassword,
                            onStartScan = { requestPermissionsAndScan() },
                            onStopScan = { viewModel.stopScan() },
                            onConnectDevice = { viewModel.connectDevice(it) },
                            onDisconnect = { viewModel.disconnect() },
                            onConnectDebugEmulator = { viewModel.connectDebugEmulator() }
                        )
                    }
                }
            }
        }
        }   // CompositionLocalProvider
    }

    override fun onDestroy() {
        super.onDestroy()
        bubbleController.stopAndCleanup()
    }

    private fun requestPermissionsAndScan() {
        val needed = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) needed += Manifest.permission.BLUETOOTH_SCAN
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) needed += Manifest.permission.BLUETOOTH_CONNECT
        }
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            needed += Manifest.permission.ACCESS_FINE_LOCATION
        }
        if (needed.isNotEmpty()) permissionLauncher.launch(needed.toTypedArray())
        else viewModel.startScan()
    }

    private fun hasPermission(perm: String) =
        ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
}
