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
import com.ximun.gopropro.ble.GoProBleManager
import com.ximun.gopropro.ble.GoProConstants
import com.ximun.gopropro.ble.GoProStatusParser
import com.ximun.gopropro.ui.ConnectionScreen
import com.ximun.gopropro.ui.DashboardScreen
import com.ximun.gopropro.ui.theme.GoProTheme
import com.ximun.gopropro.viewmodel.GoProViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GoProViewModel by viewModels()
    private lateinit var bleManager: GoProBleManager

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
                val updates = GoProStatusParser.parseQueryResponse(data)
                
                updates[70]?.let { viewModel.updateBattery(it as Int) }
                updates[10]?.let { viewModel.updateRecording(it == 1) }
                updates[54]?.let { viewModel.updateStorage("${it} MB") }
            }

            override fun onConnectionStatusChanged(connected: Boolean) {
                viewModel.updateConnection(connected)
            }
        }

        setContent {
            val state by viewModel.uiState.collectAsState()
            
            GoProTheme {
                if (!state.isConnected) {
                    ConnectionScreen(
                        isConnected = false,
                        onConnect = {
                            checkPermissionsAndScan()
                        }
                    )
                } else {
                    DashboardScreen(
                        state = state,
                        onRecordToggle = {
                            val newRecordState = !state.isRecording
                            bleManager.sendGoProCommand(
                                GoProConstants.COMMAND_CHAR_UUID,
                                byteArrayOf(GoProConstants.CMD_SET_SHUTTER.toByte(), 1, if (newRecordState) 1 else 0)
                            )
                        },
                        onHilight = {
                            bleManager.sendGoProCommand(
                                GoProConstants.COMMAND_CHAR_UUID,
                                byteArrayOf(GoProConstants.CMD_HILIGHT.toByte(), 1, 1)
                            )
                        }
                    )
                }
            }
        }
    }

    private fun checkPermissionsAndScan() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        if (permissions.all { ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            startScan()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun startScan() {
        Log.d("MainActivity", "Démarrage du scan BLE...")
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(GoProConstants.GOPRO_SERVICE_UUID))
            .build()
        
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            bleScanner?.startScan(listOf(filter), settings, scanCallback)
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.d("MainActivity", "GoPro trouvée: ${result.device.address}")
            bleScanner?.stopScan(this)
            connectToDevice(result.device)
        }
    }

    private fun connectToDevice(device: android.bluetooth.BluetoothDevice) {
        bleManager.connect(device)
            .retry(3, 100)
            .useAutoConnect(false)
            .enqueue()
    }
}
