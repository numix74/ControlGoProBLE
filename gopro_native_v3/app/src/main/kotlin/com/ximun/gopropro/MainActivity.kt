@file:OptIn(androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi::class)

package com.ximun.gopropro

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.ximun.gopropro.ble.GoProConnectionManager
import com.ximun.gopropro.bubble.BubbleController
import com.ximun.gopropro.ui.ConnectionScreen
import com.ximun.gopropro.ui.DashboardLayout
import com.ximun.gopropro.ui.theme.GoProTheme
import com.ximun.gopropro.viewmodel.GoProViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GoProViewModel by viewModels()
    private lateinit var connectionManager: GoProConnectionManager
    private lateinit var bubbleController: BubbleController

    // ── BroadcastReceiver Bluetooth ──────────────────────────────────

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                viewModel.setBluetoothEnabled(state == BluetoothAdapter.STATE_ON)
            }
        }
    }

    // ── Activity Result Launchers (doivent rester dans l'Activity) ───

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            connectionManager.startScan()
        }
    }

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        bubbleController.onOverlayPermissionResult()
    }

    // ── Lifecycle ────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialiser les managers
        connectionManager = GoProConnectionManager(this, viewModel, lifecycleScope)
        bubbleController = BubbleController(this, viewModel, lifecycleScope, connectionManager)

        // Bluetooth : état initial + écoute des changements
        viewModel.setBluetoothEnabled(connectionManager.isBluetoothEnabled)
        registerReceiver(bluetoothReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))

        // Lancer l'init BLE
        connectionManager.initialize()

        // UI Compose
        setContent {
            val state by viewModel.uiState.collectAsState()
            val windowSizeClass = calculateWindowSizeClass(this)

            GoProTheme(darkTheme = state.isDarkMode) {
                if (!state.isConnected) {
                    ConnectionScreen(
                        isBleReady = state.isBleReady,
                        isBluetoothEnabled = state.isBluetoothEnabled,
                        windowSizeClass = windowSizeClass,
                        onConnect = { connectionManager.checkPermissionsAndScan(this, requestPermissionLauncher) }
                    )
                } else {
                    DashboardLayout(
                        windowSizeClass = windowSizeClass,
                        viewModel = viewModel,
                        onRecordToggle = { handleRecordToggle() },
                        onHilight = { connectionManager.sendHilight() },
                        onDisconnect = { connectionManager.disconnect() },
                        onSleep = { connectionManager.sendSleep() },
                        onReboot = { connectionManager.sendReboot() },
                        onSyncTime = { connectionManager.syncDateTime() },
                        onToggleTimerMode = { viewModel.toggleTimerMode() },
                        onAdjustTimer = { delta -> viewModel.adjustTimer(delta) },
                        onSnapTimer = { viewModel.snapTimerToFive() },
                        onTabSelected = { index -> viewModel.setTab(index) },
                        onUpdateSetting = { id, value ->
                            viewModel.updateSettings(mapOf(id to value))
                            connectionManager.updateSetting(id, value)
                        },
                        onLoadPreset = { presetId ->
                            viewModel.updateCurrentPresetId(presetId)
                            connectionManager.loadPreset(presetId)
                        },
                        onToggleDarkMode = { viewModel.toggleDarkMode() },
                        onToggleBubble = { viewModel.toggleBubble() }
                    )
                }
            }
        }

        // Bulle flottante : callbacks + observation
        bubbleController.setupCallbacks(onRecordToggle = { handleRecordToggle() })
        bubbleController.startObserving(this, overlayPermissionLauncher)
    }

    // ── Logique métier ───────────────────────────────────────────────

    private fun handleRecordToggle() {
        val uiState = viewModel.uiState.value
        val isStarted = uiState.isRecording || uiState.isCountdownActive
        if (isStarted) {
            viewModel.updateRecording(false)
            connectionManager.sendShutterCommand(false)
        } else {
            val useTimer = uiState.isTimerModeEnabled && uiState.initialTimerValue > 0
            if (useTimer) {
                viewModel.updateRecording(true, isTimed = true) { connectionManager.sendShutterCommand(false) }
            } else {
                viewModel.updateRecording(true, isTimed = false)
            }
            connectionManager.sendShutterCommand(true)
        }
    }

    // ── Cleanup ──────────────────────────────────────────────────────

    override fun onDestroy() {
        super.onDestroy()
        connectionManager.destroy()
        bubbleController.stopAndCleanup()
        try { unregisterReceiver(bluetoothReceiver) } catch (_: Exception) {}
    }
}
