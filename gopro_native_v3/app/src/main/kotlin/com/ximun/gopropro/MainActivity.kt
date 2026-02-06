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
import com.ximun.gopropro.proto.GoProProtos
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
                Log.d("MainActivity", "📥 Message reçu sur $charUuid, taille=${data.size}")
                
                // On délègue le traitement au thread UI pour garantir la recomposition fluide
                lifecycleScope.launch {
                    when (charUuid) {
                        GoProConstants.COMMAND_RSP_CHAR_UUID.toString() -> {
                            // Vérifier si c'est la réponse à Get Hardware Info (0x3C)
                            val cmdId = data[0].toInt() and 0xFF
                            if (cmdId == GoProConstants.CMD_GET_HARDWARE_INFO) {
                                val info = GoProStatusParser.parseQueryResponse(data)
                                // Selon le document : model_number(1), model_name(2), firmware_version(6)
                                // ID 3 reste le Serial pour la plupart des modèles
                                val modelName = (info[2] as? String) ?: (info[1] as? String) ?: "HERO Device"
                                val serial = info[3] as? String ?: "Unknown"
                                val version = info[6] as? String ?: "v0.0"
                                viewModel.updateHardwareInfo(serial, version, modelName)
                                Log.d("MainActivity", "ℹ️ Hardware Info: Model=$modelName, Serial=$serial, Ver=$version")
                            }
                            
                            if (cmdId == GoProConstants.CMD_GET_VERSION) {
                                // Format major/minor (2 octets après l'ID)
                                val major = if (data.size > 2) data[2].toInt() else 0
                                val minor = if (data.size > 3) data[3].toInt() else 0
                                Log.d("MainActivity", "🌐 OpenGoPro API Version : $major.$minor")
                            }
                        }
                        
                        GoProConstants.QUERY_RSP_CHAR_UUID.toString() -> {
                            val queryId = data[0].toInt() and 0xFF
                            Log.d("MainActivity", "🔍 Query Response ID: 0x${queryId.toString(16).uppercase()}")
                            
                            // Détection explicite de la notification Preset Protobuf 0xF5
                            if (queryId == 0xF5) {
                                val updates = GoProStatusParser.parseQueryResponse(data)
                                if (updates.containsKey(0xF500)) {
                                    val notifyMsg = updates[0xF500] as? GoProProtos.NotifyPresetStatus
                                    if (notifyMsg != null) {
                                        Log.d("MainActivity", "📺 Presets Reçus : ${notifyMsg.presetGroupArrayCount} groupes")
                                        viewModel.updatePresets(notifyMsg.presetGroupArrayList)
                                    }
                                }
                                return@launch
                            }

                            val updates = try {
                                GoProStatusParser.parseQueryResponse(data)
                            } catch (e: Exception) {
                                Log.e("MainActivity", "❌ Erreur critique lors du parsing: ${e.message}")
                                Log.e("MainActivity", "Dump hex: ${data.joinToString("-") { String.format("%02X", it) }}")
                                emptyMap()
                            }
                            
                            if (updates.isEmpty()) return@launch
                            
                            // Helper robuste pour convertir n'importe quel type en Int
                            fun convertToInt(v: Any?): Int {
                                return when (v) {
                                    is Int -> v
                                    is Long -> v.toInt()
                                    is ByteArray -> {
                                        var res = 0
                                        for (b in v) {
                                            res = (res shl 8) or (b.toInt() and 0xFF)
                                        }
                                        res
                                    }
                                    is List<*> -> (v.firstOrNull() as? Int) ?: 0
                                    else -> 0
                                }
                            }

                            // Helper pour convertir en Long (jusqu'à 8 bytes)
                            fun convertToLong(v: Any?): Long {
                                when (v) {
                                    is Int -> return v.toLong()
                                    is Long -> return v
                                    is ByteArray -> {
                                        var res: Long = 0
                                        for (b in v) {
                                            res = (res shl 8) or (b.toInt() and 0xFF).toLong()
                                        }
                                        return res
                                    }
                                    else -> return 0L
                                }
                            }

                            when (queryId) {
                                GoProConstants.QRY_GET_STATUS_VALUES, GoProConstants.RSP_ASYNC_STATUS, GoProConstants.QRY_REGISTER_STATUS_UPDATES -> {
                                    updates.forEach { (id, value) ->
                                        when (id) {
                                            GoProConstants.STATUS_ID_BATTERY -> viewModel.updateBattery(convertToInt(value))
                                            GoProConstants.STATUS_ID_BATTERY_BARS -> viewModel.updateBatteryBars(convertToInt(value))
                                            GoProConstants.STATUS_ID_RECORDING -> viewModel.updateRecording(convertToInt(value) == 1)
                                            
                                            // 64-bit Values
                                            GoProConstants.STATUS_ID_STORAGE -> viewModel.updateSdRemaining(convertToLong(value))
                                            GoProConstants.STATUS_ID_SD_CAPACITY -> viewModel.updateSdCapacity(convertToLong(value))
                                            
                                            // 32-bit Values
                                            GoProConstants.STATUS_ID_VIDEO_REMAINING -> viewModel.updateVideoRemaining(convertToInt(value))
                                            GoProConstants.STATUS_ID_PHOTOS_REMAINING -> viewModel.updatePhotosRemaining(convertToInt(value))
                                            GoProConstants.STATUS_ID_VIDEOS_COUNT -> viewModel.updateVideosCount(convertToInt(value))
                                            
                                            // Enums / Bools
                                            GoProConstants.STATUS_ID_SD_STATUS -> viewModel.updateSdStatus(convertToInt(value))
                                            GoProConstants.STATUS_ID_OVERHEATING -> viewModel.updateTempStatus(convertToInt(value) == 1, false)
                                            
                                            GoProConstants.STATUS_ID_ACTIVE_PRESET -> {
                                                val v = convertToInt(value)
                                                viewModel.updatePreset("Mode $v")
                                                viewModel.updateCurrentPresetId(v)
                                            }
                                            GoProConstants.STATUS_ID_BUSY -> Log.d("MainActivity", if (convertToInt(value) == 1) "Caméra occupée..." else "Caméra prête")
                                        }
                                    }
                                }
                                GoProConstants.QRY_GET_SETTINGS_VALUES, GoProConstants.RSP_ASYNC_SETTING, GoProConstants.QRY_REGISTER_SETTINGS_UPDATES -> {
                                    val settingsUpdate = updates.mapValues { (_, v) -> convertToInt(v) }
                                    Log.d("MainActivity", "⚙️ Settings mis à jour: $settingsUpdate")
                                    viewModel.updateSettings(settingsUpdate)
                                }
                                GoProConstants.QRY_GET_SETTING_CAPABILITIES, GoProConstants.RSP_ASYNC_CAPABILITIES, GoProConstants.QRY_REGISTER_CAPABILITIES_UPDATES -> {
                                    val capsUpdate = updates.mapValues { (_, v) ->
                                        @Suppress("UNCHECKED_CAST")
                                        when (v) {
                                            is List<*> -> v.map { it as Int }
                                            is ByteArray -> listOf(v[0].toInt() and 0xFF)
                                            is Int -> listOf(v)
                                            else -> emptyList()
                                        }
                                    }
                                    Log.d("MainActivity", "📦 Réception Capacités (${capsUpdate.size} réglages)")
                                    viewModel.updateCapabilities(capsUpdate)
                                }
                            }
                        }
                        GoProConstants.COMMAND_RSP_CHAR_UUID.toString() -> {
                            Log.d("MainActivity", "✅ ACK Reçu (CMD RSP)")
                        }
                    }
                }
            }

            override fun onConnectionStatusChanged(connected: Boolean) {
                viewModel.updateConnection(connected)
                if (connected) {
                    lifecycleScope.launch {
                        delay(500)
                        startKeepAlive() // Lancer Keep Alive IMMÉDIATEMENT pour sécuriser la session
                        delay(500)
                        try {
                            subscribeToUpdates()
                        } catch (e: Exception) {
                            Log.e("MainActivity", "❌ Erreur subscribeToUpdates: ${e.message}", e)
                        }
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
                        viewModel = viewModel,
                        onRecordToggle = { handleRecordToggle() },
                        onHilight = {
                            bleManager.sendGoProCommand(
                                GoProConstants.COMMAND_CHAR_UUID,
                                byteArrayOf(GoProConstants.CMD_HILIGHT.toByte())
                            )
                        },
                        onDisconnect = { bleManager.disconnect().enqueue() },
                        onSleep = {
                            bleManager.sendGoProCommand(
                                GoProConstants.COMMAND_CHAR_UUID,
                                byteArrayOf(GoProConstants.CMD_SLEEP.toByte())
                            )
                        },
                        onReboot = {
                            bleManager.sendGoProCommand(
                                GoProConstants.COMMAND_CHAR_UUID,
                                byteArrayOf(GoProConstants.CMD_REBOOT.toByte())
                            )
                        },
                        onSyncTime = { syncDateTime() },
                        onToggleTimerMode = { viewModel.toggleTimerMode() },
                        onAdjustTimer = { delta -> viewModel.adjustTimer(delta) },
                        onTabSelected = { index -> viewModel.setTab(index) },
                        onUpdateSetting = { id, value -> 
                            // Envoi de la commande de changement de paramètre [ID, Len, Val]
                            bleManager.sendGoProCommand(
                                GoProConstants.SETTINGS_CHAR_UUID,
                                byteArrayOf(id.toByte(), 1, value.toByte())
                            )
                        },
                        onLoadPreset = { loadPreset(it) }
                    )
                }
            }
        }
    }

    private fun syncDateTime() {
        val now = java.util.Calendar.getInstance()
        val year = now.get(java.util.Calendar.YEAR)
        val month = (now.get(java.util.Calendar.MONTH) + 1).toByte()
        val day = now.get(java.util.Calendar.DAY_OF_MONTH).toByte()
        val hour = now.get(java.util.Calendar.HOUR_OF_DAY).toByte()
        val min = now.get(java.util.Calendar.MINUTE).toByte()
        val sec = now.get(java.util.Calendar.SECOND).toByte()

        val payload = byteArrayOf(
            GoProConstants.CMD_SET_DATE.toByte(), // 0x0D
            7, // Longueur
            (year shr 8).toByte(),
            (year and 0xFF).toByte(),
            month,
            day,
            hour,
            min,
            sec
        )

        bleManager.sendGoProCommand(GoProConstants.COMMAND_CHAR_UUID, payload)
        Log.d("MainActivity", "🕒 Horloge synchronisée : $day/$month/$year $hour:$min:$sec")
    }

    private fun startKeepAlive() {
        keepAliveJob?.cancel()
        keepAliveJob = lifecycleScope.launch {
            while (true) {
                if (viewModel.uiState.value.isConnected) {
                    // Les GoPro HERO nécessitent un Keep Alive toutes les ~3s
                    // IMPORTANT : Doit être envoyé sur COMMAND_CHAR_UUID (b5f90072)
                    bleManager.sendGoProCommand(
                        GoProConstants.COMMAND_CHAR_UUID,
                        byteArrayOf(GoProConstants.CMD_KEEP_ALIVE.toByte(), 1, GoProConstants.CMD_KEEP_ALIVE_VAL.toByte())
                    )
                }
                delay(3000)
            }
        }
    }

    private suspend fun subscribeToUpdates() {
        Log.d("MainActivity", "🚀 DÉBUT subscribeToUpdates")
        
        try {
            // IDs des status
            val statusIds = byteArrayOf(
                GoProConstants.STATUS_ID_RECORDING.toByte(),
                GoProConstants.STATUS_ID_BATTERY.toByte(),
                GoProConstants.STATUS_ID_BATTERY_BARS.toByte(), // ID 2
                GoProConstants.STATUS_ID_STORAGE.toByte(), // ID 54
                GoProConstants.STATUS_ID_SD_CAPACITY.toByte(), // ID 117
                GoProConstants.STATUS_ID_SD_STATUS.toByte(), // ID 33
                GoProConstants.STATUS_ID_PHOTOS_REMAINING.toByte(), // ID 38
                GoProConstants.STATUS_ID_VIDEOS_COUNT.toByte(), // ID 39
                GoProConstants.STATUS_ID_VIDEO_REMAINING.toByte(), // ID 35
                GoProConstants.STATUS_ID_ACTIVE_PRESET.toByte(),
                GoProConstants.STATUS_ID_BUSY.toByte(),
                GoProConstants.STATUS_ID_OVERHEATING.toByte() // ID 6
            )
            
            // IDs des settings
            val settingIds = listOf(
                GoProConstants.SETTING_ID_RESOLUTION,
                GoProConstants.SETTING_ID_FPS,
                GoProConstants.SETTING_ID_LENS,
                GoProConstants.SETTING_ID_HYPERSMOOTH,
                GoProConstants.SETTING_ID_COLOR,
                GoProConstants.SETTING_ID_ISO_MAX,
                GoProConstants.SETTING_ID_WHITE_BALANCE,
                GoProConstants.SETTING_ID_SHARPNESS,
                GoProConstants.SETTING_ID_BIT_RATE,
                GoProConstants.SETTING_ID_BIT_DEPTH,
                GoProConstants.SETTING_ID_VIDEO_PROFILE,
                GoProConstants.SETTING_ID_ASPECT_RATIO,
                GoProConstants.SETTING_ID_PHOTO_LENS,
                GoProConstants.SETTING_ID_TIMELAPSE_RATE,
                GoProConstants.SETTING_ID_PHOTO_TIMELAPSE_RATE,
                GoProConstants.SETTING_ID_NIGHT_LAPSE_RATE,
                GoProConstants.SETTING_ID_AUTO_POWER_DOWN,
                GoProConstants.SETTING_ID_GPS,
                GoProConstants.SETTING_ID_LCD_BRIGHTNESS,
                GoProConstants.SETTING_ID_LED
            )
            
            Log.d("MainActivity", "📡 1. Version et Infos Matériel")
            bleManager.sendGoProCommand(GoProConstants.COMMAND_CHAR_UUID, byteArrayOf(GoProConstants.CMD_GET_VERSION.toByte(), 0x00))
            delay(500)
            getHardwareInfo()
            delay(1000)

            Log.d("MainActivity", "📡 2. Abonnements (Status & Settings)")
            // On s'abonne à TOUT d'un coup (plus stable au démarrage)
            bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_REGISTER_STATUS_UPDATES.toByte()) + statusIds)
            delay(500)
            bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_REGISTER_SETTINGS_UPDATES.toByte()) + settingIds.map { it.toByte() }.toByteArray())
            delay(1000)

            Log.d("MainActivity", "📡 3. Récupération des valeurs actuelles")
            // Requête globale (sans IDs) : la caméra renvoie tout son état actuel
            bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_GET_STATUS_VALUES.toByte()))
            delay(800)
            bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_GET_SETTINGS_VALUES.toByte()))
            delay(800)

            Log.d("MainActivity", "📡 4. Récupération des Presets (Protobuf)")
            fetchPresets()
            delay(800)
            
            Log.d("MainActivity", "📡 5. Récupération des capacités")
            // On ne demande que le strict minimum pour ne pas saturer le buffer
            val essentialCaps = listOf(GoProConstants.SETTING_ID_RESOLUTION, GoProConstants.SETTING_ID_FPS, GoProConstants.SETTING_ID_LENS, GoProConstants.SETTING_ID_ASPECT_RATIO)
            essentialCaps.forEach { id ->
                bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_GET_SETTING_CAPABILITIES.toByte(), id.toByte()))
                delay(400)
            }

            Log.d("MainActivity", "✅ subscribeToUpdates TERMINÉ")
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ Erreur dans subscribeToUpdates: ${e.message}", e)
            throw e
        }
    }

    private fun fetchPresets() {
        try {
            // Encodage manuel du Protobuf RequestGetPresetStatus
            // Wire format: repeated enum (field 1, wire type 0)
            // Tag = (1 << 3) | 0 = 0x08
            val protoBytes = byteArrayOf(
                0x08, 0x01,  // Field 1 = REGISTER_PRESET_STATUS_PRESET (1)
                0x08, 0x02   // Field 1 = REGISTER_PRESET_STATUS_PRESET_GROUP_ARRAY (2)
            )

            // Format GoPro: [Feature ID] [Action ID] [Protobuf Payload]
            val packet = ByteArray(2 + protoBytes.size)
            packet[0] = 0xF5.toByte()  // Feature ID: Preset Status
            packet[1] = 0x01.toByte()  // Action ID: Register
            System.arraycopy(protoBytes, 0, packet, 2, protoBytes.size)

            bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, packet)
            Log.d("MainActivity", "📨 Requête Presets envoyée (${packet.size} bytes)")
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ Erreur construction requête Presets: ${e.message}")
        }
    }

    private fun getHardwareInfo() {
        // Envoi commande 0x3C sur COMMAND UUID
        // Selon spec OpenGoPro : ID(3C) + Length(00)
        bleManager.sendGoProCommand(
            GoProConstants.COMMAND_CHAR_UUID,
            byteArrayOf(GoProConstants.CMD_GET_HARDWARE_INFO.toByte(), 0x00)
        )
    }

    private fun loadPreset(presetId: Int) {
        // Commande 0x40 (Load Preset) - Payload 4 bytes Big Endian
        val payload = ByteArray(6)
        payload[0] = 0x40.toByte() // ID
        payload[1] = 0x04.toByte() // Length
        payload[2] = ((presetId shr 24) and 0xFF).toByte()
        payload[3] = ((presetId shr 16) and 0xFF).toByte()
        payload[4] = ((presetId shr 8) and 0xFF).toByte()
        payload[5] = (presetId and 0xFF).toByte()
        
        // Les commandes comme Load Preset (0x40) vont sur COMMAND_CHAR_UUID (GP-0072)
        bleManager.sendGoProCommand(GoProConstants.COMMAND_CHAR_UUID, payload)
        Log.d("MainActivity", "▶️ Load Preset $presetId sent")
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
