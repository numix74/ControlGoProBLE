package com.actioncam.airbuble.insta360

import android.content.Context
import android.util.Log
import com.actioncam.airbuble.camera.CameraConnectionManager
import com.actioncam.airbuble.camera.CaptureMode
import com.actioncam.airbuble.camera.ConnectionState
import com.actioncam.airbuble.camera.ScannedDevice
import com.actioncam.airbuble.camera.SettingDescriptor
import com.actioncam.airbuble.camera.SettingValue
import com.actioncam.airbuble.camera.StorageInfo
import com.arashivision.sdkcamera.camera.InstaCameraManager
import com.arashivision.sdkcamera.camera.model.CaptureMode as SdkCaptureMode
import com.arashivision.sdkcamera.camera.model.CaptureSetting
import com.arashivision.sdkcamera.camera.callback.ICameraChangedCallback
import com.arashivision.sdkcamera.camera.callback.ICameraOperateCallback
import com.arashivision.sdkcamera.camera.callback.ICaptureSupportConfigCallback
import com.arashivision.sdkcamera.camera.callback.ICaptureStatusListener
import com.arashivision.sdkcamera.camera.callback.IScanBleListener
import com.arashivision.sdkcamera.camera.model.TemperatureLevel
import com.clj.fastble.data.BleDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class Insta360ConnectionManager : CameraConnectionManager, ICameraChangedCallback, ICaptureStatusListener {

    companion object {
        private const val TAG = "Insta360Conn"
        private val HIDDEN_MODES = setOf("LIVE", "LIVE_ANIMATION", "VIDEO_NONE", "PHOTO_NONE")
        private val SUPPORTED_SETTINGS = setOf(
            "RECORD_RESOLUTION", "PHOTO_RESOLUTION",
            "ISO", "EV", "WB",
            "SHUTTER", "GAMMA_MODE", "INTERVAL"
        )
    }

    private val sdk: InstaCameraManager = InstaCameraManager.getInstance()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<ScannedDevice>>(emptyList())
    override val scannedDevices: StateFlow<List<ScannedDevice>> = _scannedDevices.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _batteryLevel = MutableStateFlow(0)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

    private val _storageInfo = MutableStateFlow(StorageInfo())
    val storageInfo: StateFlow<StorageInfo> = _storageInfo.asStateFlow()

    private val _isOverheating = MutableStateFlow(false)
    val isOverheating: StateFlow<Boolean> = _isOverheating.asStateFlow()

    // WiFi credentials retrieved after BLE connect
    var wifiSsid: String = ""; private set
    var wifiPassword: String = ""; private set

    // Camera info (readable after CONNECTED)
    @Volatile var firmwareVersion: String = ""; private set
    @Volatile var cameraDeviceType: String = ""; private set
    @Volatile var cameraDeviceSerial: String = ""; private set

    // Current capture mode (updated after initCameraConfig completes)
    private var currentCaptureModeSDK: SdkCaptureMode? = null

    init {
        sdk.registerCameraChangedCallback(this)
        sdk.setCaptureStatusListener(this)
        sdk.setScanBleListener(object : IScanBleListener {
            override fun onScanStartSuccess() {
                Log.d(TAG, "BLE scan started")
                _connectionState.value = ConnectionState.SCANNING
            }
            override fun onScanStartFail() {
                Log.w(TAG, "BLE scan start failed")
                _connectionState.value = ConnectionState.ERROR
            }
            override fun onScanning(bleDevice: BleDevice) {
                val name = bleDevice.name ?: return
                Log.d(TAG, "Found: $name (${bleDevice.mac})")
                val device = ScannedDevice(name = name, id = bleDevice.mac, rssi = bleDevice.rssi, raw = bleDevice)
                _scannedDevices.update { current ->
                    if (current.any { it.id == device.id }) current else current + device
                }
            }
            override fun onScanFinish(list: List<BleDevice>) {
                Log.d(TAG, "BLE scan finished, ${list.size} devices")
                sdk.stopBleScan()
                if (_connectionState.value == ConnectionState.SCANNING) {
                    _connectionState.value = ConnectionState.DISCONNECTED
                }
            }
        })
    }

    // ------------------------------------------------------------------ //
    //  Scan                                                                //
    // ------------------------------------------------------------------ //

    override fun startScan() {
        _scannedDevices.value = emptyList()
        sdk.startBleScan()
    }

    override fun stopScan() {
        sdk.stopBleScan()
        if (_connectionState.value == ConnectionState.SCANNING) {
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    // ------------------------------------------------------------------ //
    //  BLE Connection                                                      //
    // ------------------------------------------------------------------ //

    override fun connect(device: ScannedDevice) {
        val bleDevice = device.raw as? BleDevice ?: run {
            Log.e(TAG, "connect: raw is not BleDevice for ${device.name}")
            return
        }
        Log.i(TAG, "Connecting BLE to ${device.name}")
        sdk.stopBleScan()
        _connectionState.value = ConnectionState.BLE_CONNECTING
        sdk.connectBle(bleDevice)
    }

    override fun disconnect() {
        Log.i(TAG, "Disconnecting camera")
        sdk.closeCamera()
        Insta360NetworkManager.unbind()
        currentCaptureModeSDK = null
        firmwareVersion = ""; cameraDeviceType = ""; cameraDeviceSerial = ""
        _batteryLevel.value = 0; _isCharging.value = false
        _storageInfo.value = StorageInfo(); _isOverheating.value = false
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    // ------------------------------------------------------------------ //
    //  WiFi Connection                                                     //
    // ------------------------------------------------------------------ //

    suspend fun connectViaWifi(context: Context) {
        Log.i(TAG, "connectViaWifi: ssid=$wifiSsid")
        _connectionState.value = ConnectionState.WIFI_CONNECTING
        Insta360NetworkManager.init(context)

        val connected = Insta360NetworkManager.connectToCamera(wifiSsid, wifiPassword)
        if (!connected) {
            Log.e(TAG, "WiFi connection to camera failed")
            _connectionState.value = ConnectionState.ERROR
            return
        }

        val bound = Insta360NetworkManager.bindToCameraNetwork()
        if (!bound) {
            Log.e(TAG, "Network bind failed")
            _connectionState.value = ConnectionState.ERROR
            Insta360NetworkManager.unbind()
            return
        }

        Insta360NetworkManager.cameraNet?.let { net ->
            sdk.setNetIdToCamera(net.networkHandle)
            Log.i(TAG, "setNetIdToCamera: ${net.networkHandle}")
        }

        Log.i(TAG, "openCamera(CONNECT_TYPE_WIFI)")
        sdk.openCamera(InstaCameraManager.CONNECT_TYPE_WIFI)
    }

    // ------------------------------------------------------------------ //
    //  Camera config (modes + settings init)                              //
    // ------------------------------------------------------------------ //

    private fun initCameraConfig() {
        try {
            sdk.fetchCameraOptions(object : ICameraOperateCallback {
                override fun onSuccessful() {
                    Log.d(TAG, "fetchCameraOptions OK")
                    sdk.initCameraSupportConfig(object : ICaptureSupportConfigCallback {
                        override fun onComplete() {
                            currentCaptureModeSDK = try {
                                sdk.currentCaptureMode ?: sdk.supportCaptureMode.firstOrNull()
                            } catch (e: Exception) {
                                sdk.supportCaptureMode.firstOrNull()
                            }
                            Log.i(TAG, "Camera config ready, mode=$currentCaptureModeSDK")
                            Insta360DebugLogger.dumpCaptureConfig(sdk)
                            try { sdk.fetchCameraBatteryState() } catch (_: Exception) {}
                            try { sdk.fetchCameraStorageState() } catch (_: Exception) {}
                        }
                        override fun onFailed(s: String?) {
                            Log.w(TAG, "initCameraSupportConfig failed: $s")
                            currentCaptureModeSDK = sdk.supportCaptureMode.firstOrNull()
                        }
                    })
                }
                override fun onFailed() {
                    Log.w(TAG, "fetchCameraOptions failed")
                    currentCaptureModeSDK = sdk.supportCaptureMode.firstOrNull()
                }
                override fun onCameraConnectError() {
                    Log.w(TAG, "fetchCameraOptions: camera connect error")
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "initCameraConfig: $e")
            currentCaptureModeSDK = try { sdk.supportCaptureMode.firstOrNull() } catch (_: Exception) { null }
        }
    }

    // ------------------------------------------------------------------ //
    //  ICameraChangedCallback                                              //
    // ------------------------------------------------------------------ //

    override fun onCameraStatusChanged(enabled: Boolean, connectType: Int) {
        Log.i(TAG, "onCameraStatusChanged enabled=$enabled type=$connectType")
        when (connectType) {
            InstaCameraManager.CONNECT_TYPE_BLE -> {
                if (enabled) {
                    val info = sdk.wifiInfo
                    wifiSsid = info?.ssid ?: ""
                    wifiPassword = info?.pwd ?: ""
                    Log.i(TAG, "BLE connected. WiFi SSID=$wifiSsid")
                    _connectionState.value = ConnectionState.BLE_CONNECTED
                } else {
                    _connectionState.value = ConnectionState.DISCONNECTED
                }
            }
            InstaCameraManager.CONNECT_TYPE_WIFI,
            InstaCameraManager.CONNECT_TYPE_USB -> {
                if (enabled) {
                    Log.i(TAG, "Camera fully connected via WiFi/USB")
                    firmwareVersion  = safeGet { sdk.cameraVersion }
                    cameraDeviceType = safeGet { sdk.cameraType }.ifEmpty { "Insta360" }
                    cameraDeviceSerial = safeGet { sdk.cameraSerial }
                    Insta360DebugLogger.dumpCameraInfo(sdk)
                    _connectionState.value = ConnectionState.CONNECTED
                    Insta360NetworkManager.bindToMobileNetwork()
                    initCameraConfig()
                } else {
                    Log.i(TAG, "Camera WiFi/USB disconnected")
                    currentCaptureModeSDK = null
                    _connectionState.value = ConnectionState.DISCONNECTED
                    Insta360NetworkManager.unbind()
                }
            }
        }
    }

    override fun onCameraConnectError(errorCode: Int) {
        Log.e(TAG, "Connection error: $errorCode")
        _connectionState.value = ConnectionState.ERROR
        Insta360NetworkManager.unbind()
    }

    override fun onCameraBatteryUpdate(batteryLevel: Int, isCharging: Boolean) {
        Log.d(TAG, "Battery: $batteryLevel% charging=$isCharging")
        Insta360DebugLogger.dumpBattery(batteryLevel, isCharging)
        _batteryLevel.value = batteryLevel
        _isCharging.value = isCharging
    }

    override fun onCameraStorageChanged(freeSpace: Long, totalSpace: Long) {
        Log.d(TAG, "Storage: $freeSpace/$totalSpace bytes")
        Insta360DebugLogger.dumpStorage(freeSpace, totalSpace)
        _storageInfo.value = StorageInfo(
            freeSpaceBytes = freeSpace,
            totalSpaceBytes = totalSpace,
            sdCardPresent = freeSpace > 0
        )
    }

    override fun onCameraTemperatureChanged(tempLevel: TemperatureLevel?) {
        Log.d(TAG, "Temperature: $tempLevel")
        Insta360DebugLogger.dumpTemperature(tempLevel)
        _isOverheating.value = tempLevel != null && tempLevel.name != "NORMAL"
    }

    // ------------------------------------------------------------------ //
    //  Capture modes                                                       //
    // ------------------------------------------------------------------ //

    override fun getAvailableModes(): List<CaptureMode> {
        return try {
            sdk.supportCaptureMode
                .filter { it.name !in HIDDEN_MODES }
                .map { mode ->
                    CaptureMode(
                        id = mode.name,
                        name = Insta360SettingsMappings.modeDisplayName(mode),
                        isPhotoMode = mode.isPhotoMode,
                        isVideoMode = mode.isVideoMode,
                        isLiveMode = mode.isLiveMode
                    )
                }
        } catch (e: Exception) {
            Log.w(TAG, "getAvailableModes: $e")
            emptyList()
        }
    }

    override fun getCurrentMode(): CaptureMode? {
        val mode = currentCaptureModeSDK ?: return null
        return try {
            CaptureMode(
                id = mode.name,
                name = Insta360SettingsMappings.modeDisplayName(mode),
                isPhotoMode = mode.isPhotoMode,
                isVideoMode = mode.isVideoMode,
                isLiveMode = mode.isLiveMode
            )
        } catch (e: Exception) {
            Log.w(TAG, "getCurrentMode: $e")
            null
        }
    }

    override fun switchCaptureMode(modeId: String) {
        try {
            val targetMode = sdk.supportCaptureMode.find { it.name == modeId } ?: run {
                Log.w(TAG, "Mode not found: $modeId")
                return
            }
            sdk.setCaptureMode(targetMode)
            currentCaptureModeSDK = targetMode
            Log.i(TAG, "Mode switched to $modeId")
        } catch (e: Exception) {
            Log.w(TAG, "switchCaptureMode: $e")
        }
    }

    // ------------------------------------------------------------------ //
    //  Settings                                                            //
    // ------------------------------------------------------------------ //

    override fun getAvailableSettings(): List<SettingDescriptor> {
        val mode = currentCaptureModeSDK ?: return emptyList()
        return try {
            sdk.getSupportCaptureSettingList(mode)
                .filter { it.name in SUPPORTED_SETTINGS }
                .mapNotNull { cs -> buildSettingDescriptor(mode, cs) }
        } catch (e: Exception) {
            Log.w(TAG, "getAvailableSettings: $e")
            emptyList()
        }
    }

    private fun buildSettingDescriptor(mode: SdkCaptureMode, cs: CaptureSetting): SettingDescriptor? {
        return try {
            val valueList: List<*>
            val currentStr: String
            when (cs.name) {
                "RECORD_RESOLUTION" -> {
                    valueList = sdk.getSupportRecordResolutionList(mode)
                    currentStr = safeGet { sdk.getRecordResolution(mode)?.toString() }
                }
                "PHOTO_RESOLUTION" -> {
                    valueList = sdk.getSupportPhotoResolutionList(mode)
                    currentStr = safeGet { sdk.getPhotoResolution(mode)?.toString() }
                }
                "ISO" -> {
                    valueList = sdk.getSupportISOList(mode)
                    currentStr = safeGet { sdk.getISO(mode)?.toString() }
                }
                "EV" -> {
                    valueList = sdk.getSupportEVList(mode)
                    currentStr = safeGet { sdk.getEv(mode)?.toString() }
                }
                "WB" -> {
                    valueList = sdk.getSupportWBList(mode)
                    currentStr = safeGet { sdk.getWB(mode)?.toString() }
                }
                "SHUTTER" -> {
                    valueList = sdk.getSupportShutterList(mode)
                    currentStr = safeGet { sdk.getShutter(mode)?.toString() }
                }
                "GAMMA_MODE" -> {
                    valueList = sdk.getSupportGammaModeList(mode)
                    currentStr = safeGet { sdk.getGammaMode(mode)?.toString() }
                }
                "INTERVAL" -> {
                    valueList = sdk.getSupportIntervalList(mode)
                    currentStr = safeGet { sdk.getInterval(mode)?.toString() }
                }
                else -> return null
            }
            if (valueList.isEmpty()) return null
            SettingDescriptor(
                id = cs.name,
                name = Insta360SettingsMappings.settingLabel(cs.name),
                currentValue = currentStr,
                availableValues = valueList.mapIndexed { i, v ->
                    SettingValue(id = i.toString(), displayName = v?.toString() ?: "")
                }
            )
        } catch (e: Exception) {
            Log.w(TAG, "buildSettingDescriptor ${cs.name}: $e")
            null
        }
    }

    override fun changeSetting(settingId: String, valueId: String) {
        val mode = currentCaptureModeSDK ?: return
        val idx = valueId.toIntOrNull() ?: return
        try {
            when (settingId) {
                "RECORD_RESOLUTION" -> {
                    val list = sdk.getSupportRecordResolutionList(mode)
                    if (idx in list.indices) sdk.setRecordResolution(mode, list[idx], null)
                }
                "PHOTO_RESOLUTION" -> {
                    val list = sdk.getSupportPhotoResolutionList(mode)
                    if (idx in list.indices) sdk.setPhotoResolution(mode, list[idx], null)
                }
                "ISO" -> {
                    val list = sdk.getSupportISOList(mode)
                    if (idx in list.indices) sdk.setISO(mode, list[idx], null)
                }
                "EV" -> {
                    val list = sdk.getSupportEVList(mode)
                    if (idx in list.indices) sdk.setEv(mode, list[idx], null)
                }
                "WB" -> {
                    val list = sdk.getSupportWBList(mode)
                    if (idx in list.indices) sdk.setWB(mode, list[idx], null)
                }
                "SHUTTER" -> {
                    val list = sdk.getSupportShutterList(mode)
                    if (idx in list.indices) sdk.setShutter(mode, list[idx], null)
                }
                "GAMMA_MODE" -> {
                    val list = sdk.getSupportGammaModeList(mode)
                    if (idx in list.indices) sdk.setGammaMode(mode, list[idx], null)
                }
                "INTERVAL" -> {
                    val list = sdk.getSupportIntervalList(mode)
                    if (idx in list.indices) sdk.setInterval(mode, list[idx], null)
                }
            }
            Log.i(TAG, "changeSetting $settingId[$idx]")
        } catch (e: Exception) {
            Log.w(TAG, "changeSetting $settingId: $e")
        }
    }

    private inline fun safeGet(block: () -> String?): String = try { block() ?: "" } catch (_: Exception) { "" }

    // ------------------------------------------------------------------ //
    //  Camera control                                                      //
    // ------------------------------------------------------------------ //

    override fun shutdownCamera() {
        Log.i(TAG, "shutdownCamera")
        try { sdk.shutdownCamera() } catch (e: Exception) { Log.w(TAG, "shutdownCamera: $e") }
    }

    override fun syncTime() {
        // TODO jour J : confirmer si le SDK Insta360 expose une méthode dédiée.
        // Pour l'instant, aucune méthode syncTime() / setCameraTime() visible dans sdkdemo2.
        // Piste : regarder si fetchCameraOptions() + setOptions("dateTimeZone", ...) fonctionne.
        Log.i(TAG, "syncTime — stub, implémentation à valider avec X3 physique")
    }

    override fun startRecording() {
        Log.i(TAG, "startRecording")
        sdk.startNormalRecord()
    }

    override fun stopRecording() {
        Log.i(TAG, "stopRecording")
        sdk.stopNormalRecord()
    }

    override fun takePhoto() {
        Log.i(TAG, "takePhoto")
        sdk.startNormalCapture()
    }

    // ------------------------------------------------------------------ //
    //  ICaptureStatusListener                                              //
    // ------------------------------------------------------------------ //

    override fun onCaptureStarting() { Log.d(TAG, "onCaptureStarting") }
    override fun onCaptureWorking() {
        Log.i(TAG, "onCaptureWorking — recording started")
        _isRecording.value = true
    }
    override fun onCaptureStopping() { Log.d(TAG, "onCaptureStopping") }
    override fun onCaptureFinish(paths: Array<String>?) {
        Log.i(TAG, "onCaptureFinish")
        _isRecording.value = false
    }
    override fun onCaptureError(errorCode: Int) {
        Log.e(TAG, "onCaptureError: $errorCode")
        _isRecording.value = false
    }
    override fun onCaptureTimeChanged(captureTime: Long) {}
    override fun onCaptureCountChanged(captureCount: Int) {}

    override fun markHilight() { Log.d(TAG, "markHilight — GPS waypoint only for Insta360") }

    override fun setAppForeground(foreground: Boolean) { Log.d(TAG, "setAppForeground($foreground)") }

    override fun destroy() {
        sdk.unregisterCameraChangedCallback(this)
        sdk.setScanBleListener(null)
        sdk.setCaptureStatusListener(null)
        sdk.closeCamera()
        Insta360NetworkManager.stop()
    }
}
