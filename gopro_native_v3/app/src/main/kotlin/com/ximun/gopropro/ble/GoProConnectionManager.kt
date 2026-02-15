package com.ximun.gopropro.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import com.ximun.gopropro.GoProSettingsMappings
import com.ximun.gopropro.proto.GoProProtos
import com.ximun.gopropro.viewmodel.GoProViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Centralise toute la logique BLE : scan, connexion, parsing des réponses,
 * commandes GoPro, keep-alive et subscriptions.
 */
class GoProConnectionManager(
    private val context: Context,
    private val viewModel: GoProViewModel,
    private val lifecycleScope: CoroutineScope
) {
    companion object {
        private const val TAG = "GoProConnectionManager"
    }

    private lateinit var bleManager: GoProBleManager
    private var keepAliveJob: Job? = null
    private val bleScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val bluetoothManager by lazy { context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager }
    private val bluetoothAdapter: BluetoothAdapter? by lazy { bluetoothManager.adapter }
    private val bleScanner by lazy { bluetoothAdapter?.bluetoothLeScanner }

    val isBluetoothEnabled: Boolean get() = bluetoothAdapter?.isEnabled == true

    // ── Initialisation ───────────────────────────────────────────────

    fun initialize() {
        bleScope.launch {
            withContext(Dispatchers.Main) {
                bleManager = GoProBleManager(context)
                bleManager.callback = object : GoProBleManager.GoProBleCallback {
                    override fun onMessageReceived(charUuid: String, data: ByteArray) {
                        lifecycleScope.launch(Dispatchers.Default) {
                            processBleMessage(charUuid, data)
                        }
                    }

                    override fun onConnectionStatusChanged(connected: Boolean) {
                        viewModel.updateConnection(connected)
                        if (connected) {
                            lifecycleScope.launch(Dispatchers.IO) {
                                delay(500)
                                startKeepAlive()
                                delay(200)
                                performInitialPolling()
                            }
                        } else {
                            keepAliveJob?.cancel()
                        }
                    }
                }
                viewModel.setBleReady(true)
            }
        }
    }

    fun destroy() {
        keepAliveJob?.cancel()
    }

    // ── Scan & Connexion ─────────────────────────────────────────────

    fun checkPermissionsAndScan(
        activity: ComponentActivity,
        permissionLauncher: ActivityResultLauncher<Array<String>>
    ) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (permissions.all { ActivityCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED }) {
            startScan()
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        val filter = ScanFilter.Builder().setServiceUuid(ParcelUuid(GoProConstants.GOPRO_SERVICE_UUID)).build()
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        bleScanner?.startScan(listOf(filter), settings, scanCallback)
    }

    @SuppressLint("MissingPermission")
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            bleScanner?.stopScan(this)
            bleManager.connect(result.device).retry(3, 100).useAutoConnect(false).enqueue()
        }
    }

    // ── Commandes de haut niveau ─────────────────────────────────────

    fun sendCommand(charUuid: java.util.UUID, payload: ByteArray) {
        bleManager.sendGoProCommand(charUuid, payload)
    }

    fun disconnect() {
        bleManager.disconnect().enqueue()
    }

    fun sendShutterCommand(enable: Boolean) {
        bleManager.sendGoProCommand(
            GoProConstants.COMMAND_CHAR_UUID,
            byteArrayOf(GoProConstants.CMD_SET_SHUTTER.toByte(), 1, if (enable) 1 else 0)
        )
    }

    fun sendHilight() {
        bleManager.sendGoProCommand(
            GoProConstants.COMMAND_CHAR_UUID,
            byteArrayOf(GoProConstants.CMD_HILIGHT.toByte())
        )
    }

    fun sendSleep() {
        bleManager.sendGoProCommand(
            GoProConstants.COMMAND_CHAR_UUID,
            byteArrayOf(GoProConstants.CMD_SLEEP.toByte())
        )
    }

    fun sendReboot() {
        bleManager.sendGoProCommand(
            GoProConstants.COMMAND_CHAR_UUID,
            byteArrayOf(GoProConstants.CMD_REBOOT.toByte())
        )
    }

    fun syncDateTime() {
        val now = Calendar.getInstance()
        val year = now.get(Calendar.YEAR)
        val month = (now.get(Calendar.MONTH) + 1).toByte()
        val day = now.get(Calendar.DAY_OF_MONTH).toByte()
        val hour = now.get(Calendar.HOUR_OF_DAY).toByte()
        val min = now.get(Calendar.MINUTE).toByte()
        val sec = now.get(Calendar.SECOND).toByte()

        val payload = byteArrayOf(
            GoProConstants.CMD_SET_DATE.toByte(),
            7,
            (year shr 8).toByte(), (year and 0xFF).toByte(),
            month, day, hour, min, sec
        )
        bleManager.sendGoProCommand(GoProConstants.COMMAND_CHAR_UUID, payload)
    }

    fun loadPreset(presetId: Int) {
        val payload = ByteArray(6)
        payload[0] = 0x40.toByte()
        payload[1] = 0x04.toByte()
        payload[2] = ((presetId shr 24) and 0xFF).toByte()
        payload[3] = ((presetId shr 16) and 0xFF).toByte()
        payload[4] = ((presetId shr 8) and 0xFF).toByte()
        payload[5] = (presetId and 0xFF).toByte()
        bleManager.sendGoProCommand(GoProConstants.COMMAND_CHAR_UUID, payload)
    }

    fun updateSetting(settingId: Int, value: Int) {
        bleManager.sendGoProCommand(
            GoProConstants.SETTINGS_CHAR_UUID,
            byteArrayOf(settingId.toByte(), 1, value.toByte())
        )
    }

    // ── Keep-alive & Subscriptions ───────────────────────────────────

    private fun startKeepAlive() {
        keepAliveJob?.cancel()
        keepAliveJob = lifecycleScope.launch {
            while (true) {
                if (viewModel.uiState.value.isConnected) {
                    bleManager.sendGoProCommand(
                        GoProConstants.COMMAND_CHAR_UUID,
                        byteArrayOf(GoProConstants.CMD_KEEP_ALIVE.toByte(), 1, GoProConstants.CMD_KEEP_ALIVE_VAL.toByte())
                    )
                }
                delay(3000)
            }
        }
    }

    private suspend fun performInitialPolling() {
        var isReady = false
        var attempt = 0
        while (!isReady && attempt < 10) {
            Log.d(TAG, "HW Info polling attempt ${attempt + 1}/10")
            bleManager.sendGoProCommand(GoProConstants.COMMAND_CHAR_UUID, byteArrayOf(GoProConstants.CMD_GET_HARDWARE_INFO.toByte()))
            delay(2000)
            val serial = withContext(Dispatchers.Main) {
                viewModel.uiState.value.serialNumber
            }
            if (serial.isNotEmpty()) isReady = true
            attempt++
        }
        if (isReady) {
            Log.d(TAG, "Camera ready, starting subscriptions...")
            try {
                subscribeToUpdates()
            } catch (e: Exception) {
                Log.e(TAG, "Error subscribing", e)
            }
        } else {
            Log.e(TAG, "Camera not ready after 10 attempts")
        }
    }

    private suspend fun subscribeToUpdates() {
        val statusIds = byteArrayOf(
            GoProConstants.STATUS_ID_RECORDING.toByte(),
            GoProConstants.STATUS_ID_BATTERY.toByte(),
            GoProConstants.STATUS_ID_BATTERY_BARS.toByte(),
            GoProConstants.STATUS_ID_STORAGE.toByte(),
            GoProConstants.STATUS_ID_SD_CAPACITY.toByte(),
            GoProConstants.STATUS_ID_SD_STATUS.toByte(),
            GoProConstants.STATUS_ID_PHOTOS_REMAINING.toByte(),
            GoProConstants.STATUS_ID_VIDEOS_COUNT.toByte(),
            GoProConstants.STATUS_ID_VIDEO_REMAINING.toByte(),
            GoProConstants.STATUS_ID_ACTIVE_PRESET.toByte(),
            GoProConstants.STATUS_ID_BUSY.toByte(),
            GoProConstants.STATUS_ID_OVERHEATING.toByte()
        )

        Log.d(TAG, "Subscribe: Claim Control...")
        bleManager.sendGoProCommand(GoProConstants.COMMAND_CHAR_UUID, byteArrayOf(0xF1.toByte(), 0x69.toByte(), 0x08.toByte(), 0x02.toByte()))
        delay(1000)

        Log.d(TAG, "Subscribe: Register Status Updates...")
        bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_REGISTER_STATUS_UPDATES.toByte()) + statusIds)
        delay(1000)

        Log.d(TAG, "Subscribe: Get ALL Settings Values...")
        bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_GET_SETTINGS_VALUES.toByte()))
        delay(2000)

        Log.d(TAG, "Subscribe: Get ALL Capabilities...")
        bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_GET_SETTING_CAPABILITIES.toByte()))
        delay(2000)

        Log.d(TAG, "Subscribe: Register ALL Settings Updates...")
        bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_REGISTER_SETTINGS_UPDATES.toByte()))
        delay(1000)

        Log.d(TAG, "Subscribe: Register ALL Capabilities Updates...")
        bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_REGISTER_CAPABILITIES_UPDATES.toByte()))
        delay(1000)

        Log.d(TAG, "Subscribe: Get Status Values...")
        bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_GET_STATUS_VALUES.toByte()) + statusIds)
        delay(1000)

        Log.d(TAG, "Subscribe: Fetch Presets...")
        val protoBytes = byteArrayOf(0x08, 0x01, 0x08, 0x02)
        val packet = ByteArray(2 + protoBytes.size)
        packet[0] = 0xF5.toByte()
        packet[1] = 0x72.toByte()
        System.arraycopy(protoBytes, 0, packet, 2, protoBytes.size)
        bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, packet)
        Log.d(TAG, "Subscribe: TERMINÉ")
    }

    // ── Parsing des réponses BLE ─────────────────────────────────────

    private suspend fun processBleMessage(charUuid: String, data: ByteArray) {
        when (charUuid) {
            GoProConstants.COMMAND_RSP_CHAR_UUID.toString() -> handleCommandResponse(data)
            GoProConstants.QUERY_RSP_CHAR_UUID.toString() -> handleQueryResponse(data)
            GoProConstants.SETTINGS_RSP_CHAR_UUID.toString() -> handleSettingsResponse(data)
        }
    }

    private suspend fun handleCommandResponse(data: ByteArray) {
        val cmdId = data[0].toInt() and 0xFF
        if (cmdId == GoProConstants.CMD_GET_HARDWARE_INFO) {
            val info = GoProStatusParser.parseQueryResponse(data)
            val modelName = info[2] as? String ?: "HERO Device"
            val serial = info[5] as? String ?: ""
            val version = info[4] as? String ?: ""
            withContext(Dispatchers.Main) {
                viewModel.updateHardwareInfo(serial, version, modelName)
            }
            Log.d(TAG, "Hardware Info: Model=$modelName, Serial=$serial, Ver=$version")
        }
    }

    private fun handleSettingsResponse(data: ByteArray) {
        val settingId = data[0].toInt() and 0xFF
        val status = if (data.size > 1) data[1].toInt() and 0xFF else -1
        if (status == 0) {
            Log.d(TAG, "Setting 0x${settingId.toString(16).uppercase()} appliqué")
        } else {
            Log.w(TAG, "Setting 0x${settingId.toString(16).uppercase()} refusé (status=$status)")
        }
    }

    private suspend fun handleQueryResponse(data: ByteArray) {
        val queryId = data[0].toInt() and 0xFF
        Log.d(TAG, "Query Response ID: 0x${queryId.toString(16).uppercase()}")

        if (queryId == 0xF5) {
            val updates = GoProStatusParser.parseQueryResponse(data)
            if (updates.containsKey(0xF500)) {
                val notifyMsg = updates[0xF500] as? GoProProtos.NotifyPresetStatus
                if (notifyMsg != null) {
                    for (group in notifyMsg.presetGroupArrayList) {
                        Log.d(TAG, "PresetGroup id=${group.id}")
                        for (preset in group.presetArrayList) {
                            Log.d(TAG, "  Preset id=${preset.id}, titleId=${if (preset.hasTitleId()) preset.titleId else "N/A"}, icon=${if (preset.hasIcon()) preset.icon else "N/A"}, mode=${if (preset.hasMode()) preset.mode else "N/A"}")
                            for (setting in preset.settingArrayList) {
                                val label = GoProSettingsMappings.getLabel(setting.id, setting.value)
                                val name = GoProSettingsMappings.getSettingName(setting.id)
                                Log.d(TAG, "    Setting id=${setting.id} ($name), value=${setting.value} -> \"$label\"")
                            }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        viewModel.updatePresets(notifyMsg.presetGroupArrayList)
                    }
                }
            }
            return
        }

        val updates = try {
            GoProStatusParser.parseQueryResponse(data)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur parsing: ${e.message}")
            emptyMap()
        }

        if (updates.isEmpty()) return

        withContext(Dispatchers.Main) {
            applyUpdatesToViewModel(queryId, updates)
        }
    }

    private fun applyUpdatesToViewModel(queryId: Int, updates: Map<Int, Any>) {
        fun convertToInt(v: Any?): Int = when (v) {
            is Int -> v
            is Long -> v.toInt()
            is ByteArray -> {
                var res = 0
                for (b in v) res = (res shl 8) or (b.toInt() and 0xFF)
                res
            }
            is List<*> -> (v.firstOrNull() as? Int) ?: 0
            else -> 0
        }

        fun convertToLong(v: Any?): Long = when (v) {
            is Int -> v.toLong()
            is Long -> v
            is ByteArray -> {
                var res: Long = 0
                for (b in v) res = (res shl 8) or (b.toInt() and 0xFF).toLong()
                res
            }
            else -> 0L
        }

        when (queryId) {
            GoProConstants.QRY_GET_STATUS_VALUES, GoProConstants.RSP_ASYNC_STATUS, GoProConstants.QRY_REGISTER_STATUS_UPDATES -> {
                updates.forEach { (id, value) ->
                    when (id) {
                        GoProConstants.STATUS_ID_BATTERY -> viewModel.updateBattery(convertToInt(value))
                        GoProConstants.STATUS_ID_BATTERY_BARS -> viewModel.updateBatteryBars(convertToInt(value))
                        GoProConstants.STATUS_ID_RECORDING -> viewModel.updateRecording(convertToInt(value) == 1)
                        GoProConstants.STATUS_ID_STORAGE -> viewModel.updateSdRemaining(convertToLong(value))
                        GoProConstants.STATUS_ID_SD_CAPACITY -> viewModel.updateSdCapacity(convertToLong(value))
                        GoProConstants.STATUS_ID_VIDEO_REMAINING -> viewModel.updateVideoRemaining(convertToInt(value))
                        GoProConstants.STATUS_ID_PHOTOS_REMAINING -> viewModel.updatePhotosRemaining(convertToInt(value))
                        GoProConstants.STATUS_ID_VIDEOS_COUNT -> viewModel.updateVideosCount(convertToInt(value))
                        GoProConstants.STATUS_ID_SD_STATUS -> viewModel.updateSdStatus(convertToInt(value))
                        GoProConstants.STATUS_ID_OVERHEATING -> viewModel.updateTempStatus(convertToInt(value) == 1, false)
                        GoProConstants.STATUS_ID_ACTIVE_PRESET -> {
                            val v = convertToInt(value)
                            viewModel.updatePreset("Mode $v")
                            viewModel.updateCurrentPresetId(v)
                        }
                    }
                }
            }
            GoProConstants.QRY_GET_SETTINGS_VALUES, GoProConstants.RSP_ASYNC_SETTING, GoProConstants.QRY_REGISTER_SETTINGS_UPDATES -> {
                val settingsMap = updates.mapValues { (_, v) -> convertToInt(v) }
                Log.d(TAG, "Settings reçus (${settingsMap.size} entries)")
                viewModel.updateSettings(settingsMap)
            }
            GoProConstants.QRY_GET_SETTING_CAPABILITIES, GoProConstants.RSP_ASYNC_CAPABILITIES, GoProConstants.QRY_REGISTER_CAPABILITIES_UPDATES -> {
                val capsUpdate = updates.mapValues { (_, v) ->
                    when (v) {
                        is List<*> -> v.map { it as Int }
                        is ByteArray -> listOf(v[0].toInt() and 0xFF)
                        is Int -> listOf(v)
                        else -> emptyList()
                    }
                }
                Log.d(TAG, "Capabilities reçues (${capsUpdate.size} entries)")
                viewModel.updateCapabilities(capsUpdate)
            }
        }
    }
}
