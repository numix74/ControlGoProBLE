package com.ximun.gopropro

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.ximun.gopropro.ble.GoProBleManager
import com.ximun.gopropro.ble.GoProConstants
import com.ximun.gopropro.ble.GoProStatusParser
import com.ximun.gopropro.ui.ConnectionScreen
import com.ximun.gopropro.ui.DashboardLayout
import com.ximun.gopropro.ui.theme.GoProTheme
import com.ximun.gopropro.viewmodel.GoProViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: GoProViewModel by viewModels()
    private lateinit var bleManager: GoProBleManager

    private var keepAliveJob: Job? = null

    private val bluetoothManager by lazy { getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager }
    private val bluetoothAdapter by lazy { bluetoothManager.adapter }
    private val bleScanner by lazy { bluetoothAdapter.bluetoothLeScanner }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            startScan()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        bleManager = GoProBleManager(this)
        bleManager.callback = object : GoProBleManager.GoProBleCallback {
            override fun onMessageReceived(charUuid: String, data: ByteArray) {
                when (charUuid) {
                    GoProConstants.QUERY_RSP_CHAR_UUID.toString() -> {
                        val queryId = data[0].toInt() and 0xFF
                        val updates = GoProStatusParser.parseQueryResponse(data)
                        
                        // Helper pour convertir ByteArray en Int (Big Endian)
                        fun toInt(v: Any?): Int {
                            val bytes = v as? ByteArray ?: return 0
                            return when (bytes.size) {
                                1 -> bytes[0].toInt() and 0xFF
                                2 -> ((bytes[0].toInt() and 0xFF) shl 8) or (bytes[1].toInt() and 0xFF)
                                4 -> ((bytes[0].toInt() and 0xFF) shl 24) or ((bytes[1].toInt() and 0xFF) shl 16) or 
                                     ((bytes[2].toInt() and 0xFF) shl 8) or (bytes[3].toInt() and 0xFF)
                                else -> 0
                            }
                        }

                        when (queryId) {
                            GoProConstants.QRY_GET_STATUS_VALUES, GoProConstants.RSP_ASYNC_STATUS, GoProConstants.QRY_REGISTER_STATUS_UPDATES -> {
                                updates.forEach { (id, value) ->
                                    val intVal = toInt(value)
                                    when (id) {
                                        GoProConstants.STATUS_ID_BATTERY -> viewModel.updateBattery(intVal)
                                        GoProConstants.STATUS_ID_RECORDING -> viewModel.updateRecording(intVal == 1)
                                        GoProConstants.STATUS_ID_STORAGE -> viewModel.updateStorage("$intVal KB")
                                        GoProConstants.STATUS_ID_ACTIVE_PRESET -> viewModel.updatePreset("Mode $intVal")
                                    }
                                }
                            }
                            GoProConstants.QRY_GET_SETTINGS_VALUES, GoProConstants.RSP_ASYNC_SETTING, GoProConstants.QRY_REGISTER_SETTINGS_UPDATES -> {
                                val settingsUpdate = updates.mapValues { (_, v) -> toInt(v) }
                                viewModel.updateSettings(settingsUpdate)
                            }
                            GoProConstants.QRY_GET_SETTING_CAPABILITIES, GoProConstants.RSP_ASYNC_CAPABILITIES, GoProConstants.QRY_REGISTER_CAPABILITIES_UPDATES -> {
                                val capsUpdate = updates.mapValues { (_, v) -> 
                                    (v as? ByteArray)?.map { it.toInt() and 0xFF } ?: emptyList()
                                }
                                viewModel.updateCapabilities(capsUpdate)
                                Log.d("MainActivity", "Capacités reçues : ${capsUpdate.keys}")
                            }
                        }
                    }
                    GoProConstants.COMMAND_RSP_CHAR_UUID.toString() -> {
                        // ACK de commande simple
                    }
                }
            }

            override fun onConnectionStatusChanged(connected: Boolean) {
                viewModel.updateConnection(connected)
                if (connected) {
                    lifecycleScope.launch {
                        delay(500) // Un court délai suffit après onDeviceReady
                        subscribeToUpdates()
                        startKeepAlive()
                    }
                } else {
                    keepAliveJob?.cancel()
                }
            }
        }

        setContent {
            val state by viewModel.uiState.collectAsState()
            
            GoProTheme {
                if (!state.isConnected) {
                    ConnectionScreen(isConnected = false, onConnect = { checkPermissionsAndScan() })
                } else {
                    DashboardLayout(
                        state = state,
                        onRecordToggle = { handleRecordToggle() },
                        onHilight = {
                            bleManager.sendGoProCommand(
                                GoProConstants.COMMAND_CHAR_UUID,
                                byteArrayOf(GoProConstants.CMD_HILIGHT.toByte())
                            )
                        },
                        onDisconnect = { bleManager.disconnect().enqueue() },
                        onToggleTimerMode = { viewModel.toggleTimerMode() },
                        onAdjustTimer = { delta -> viewModel.adjustTimer(delta) },
                        onTabSelected = { index -> viewModel.setTab(index) },
                        onUpdateSetting = { id, value -> 
                            // Envoi de la commande de changement de paramètre [ID, Len, Val]
                            bleManager.sendGoProCommand(
                                GoProConstants.SETTINGS_CHAR_UUID,
                                byteArrayOf(id.toByte(), 1, value.toByte())
                            )
                        }
                    )
                }
            }
        }
    }

    private fun startKeepAlive() {
        keepAliveJob?.cancel()
        keepAliveJob = lifecycleScope.launch {
            while (true) {
                if (viewModel.uiState.value.isConnected) {
                    // 1. Keep Alive pur sur Settings Char (ID 0x5B, Val 0x42)
                    bleManager.sendGoProCommand(
                        GoProConstants.SETTINGS_CHAR_UUID,
                        byteArrayOf(GoProConstants.CMD_KEEP_ALIVE.toByte(), 1, GoProConstants.CMD_KEEP_ALIVE_VAL.toByte())
                    )
                    
                    delay(3500) // Recommandé toutes les 3-4 secondes
                    
                    // 2. Refresh Status (Batterie, Enregistrement) sur Query Char
                    bleManager.sendGoProCommand(
                        GoProConstants.QUERY_CHAR_UUID,
                        byteArrayOf(GoProConstants.QRY_GET_STATUS_VALUES.toByte())
                    )
                }
                delay(5000)
            }
        }
    }

    private suspend fun subscribeToUpdates() {
        Log.d("MainActivity", "Abonnement aux notifications (0x53, 0x52)...")
        
        // Liste des IDs à surveiller (basé sur le JS)
        val statusIds = byteArrayOf(
            GoProConstants.STATUS_ID_RECORDING.toByte(),
            GoProConstants.STATUS_ID_BATTERY.toByte(),
            GoProConstants.STATUS_ID_STORAGE.toByte(),
            GoProConstants.STATUS_ID_ACTIVE_PRESET.toByte(),
            GoProConstants.STATUS_ID_BUSY.toByte()
        )
        
        val settingIds = byteArrayOf(
            GoProConstants.SETTING_ID_RESOLUTION.toByte(),
            GoProConstants.SETTING_ID_FPS.toByte(),
            GoProConstants.SETTING_ID_LENS.toByte(),
            GoProConstants.SETTING_ID_HYPERSMOOTH.toByte(),
            GoProConstants.SETTING_ID_COLOR.toByte(),
            GoProConstants.SETTING_ID_ISO_MAX.toByte(),
            GoProConstants.SETTING_ID_WHITE_BALANCE.toByte()
        )

        // 1. S'abonner aux changements (Notifications asynchrones 0x93, 0x92, 0xA2)
        bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_REGISTER_STATUS_UPDATES.toByte()) + statusIds)
        delay(300)
        bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_REGISTER_SETTINGS_UPDATES.toByte()) + settingIds)
        delay(300)
        bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_REGISTER_CAPABILITIES_UPDATES.toByte()) + settingIds)
        delay(300)

        // 2. Récupérer l'état initial (Get All)
        bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_GET_STATUS_VALUES.toByte()))
        delay(300)
        bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_GET_SETTINGS_VALUES.toByte()))
        delay(300)
        
        // 3. Récupérer les capacités (options disponibles pour chaque réglage)
        Log.d("MainActivity", "Récupération des capacités (0x32)...")
        bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_GET_SETTING_CAPABILITIES.toByte()) + settingIds)
    }

    private fun handleRecordToggle() {
        val uiState = viewModel.uiState.value
        val isStarted = uiState.isRecording || uiState.isCountdownActive
        if (isStarted) {
            viewModel.updateRecording(false)
            sendShutterCommand(false)
        } else {
            val useTimer = uiState.isTimerModeEnabled && uiState.initialTimerValue > 0
            if (useTimer) {
                viewModel.updateRecording(true, isTimed = true) {
                    sendShutterCommand(false)
                }
            } else {
                viewModel.updateRecording(true, isTimed = false)
            }
            sendShutterCommand(true)
        }
    }

    private fun sendShutterCommand(enable: Boolean) {
        bleManager.sendGoProCommand(
            GoProConstants.COMMAND_CHAR_UUID,
            byteArrayOf(GoProConstants.CMD_SET_SHUTTER.toByte(), 1, if (enable) 1 else 0)
        )
    }

    private fun checkPermissionsAndScan() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (permissions.all { ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            startScan()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun startScan() {
        val filter = ScanFilter.Builder().setServiceUuid(ParcelUuid(GoProConstants.GOPRO_SERVICE_UUID)).build()
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            bleScanner?.startScan(listOf(filter), settings, scanCallback)
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            bleScanner?.stopScan(this)
            bleManager.connect(result.device).retry(3, 100).useAutoConnect(false).enqueue()
        }
    }
}
