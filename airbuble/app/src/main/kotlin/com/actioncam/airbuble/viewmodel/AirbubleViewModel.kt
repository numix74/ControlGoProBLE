package com.actioncam.airbuble.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.actioncam.airbuble.camera.CameraUiState
import com.actioncam.airbuble.camera.ConnectionState
import com.actioncam.airbuble.camera.ScannedDevice
import com.actioncam.airbuble.camera.StorageInfo
import com.actioncam.airbuble.gps.GpsTracker
import com.actioncam.airbuble.insta360.Insta360ConnectionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AirbubleViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "AirbubleVM"
        private const val PREFS_NAME = "airbuble_prefs"
        private const val KEY_TIMER_VALUE = "timer_value"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_BUBBLE_ENABLED = "bubble_enabled"
    }

    private val prefs by lazy {
        getApplication<Application>().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val connectionManager = Insta360ConnectionManager()

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    val scannedDevices: StateFlow<List<ScannedDevice>> = connectionManager.scannedDevices

    private var sessionJob: Job? = null
    private var gpsTracker: GpsTracker? = null

    init {
        val savedTimer = loadTimerValue()
        val savedDarkMode = prefs.getBoolean(KEY_DARK_MODE, true)
        val savedBubble = prefs.getBoolean(KEY_BUBBLE_ENABLED, true)
        _uiState.update { it.copy(
            initialTimerValue = savedTimer, currentTimerValue = savedTimer,
            isDarkMode = savedDarkMode, isBubbleEnabled = savedBubble
        ) }

        // Status flows
        viewModelScope.launch {
            connectionManager.batteryLevel.collect { level ->
                _uiState.update { it.copy(batteryLevel = level) }
            }
        }
        viewModelScope.launch {
            connectionManager.isCharging.collect { charging ->
                _uiState.update { it.copy(isCharging = charging) }
            }
        }
        viewModelScope.launch {
            connectionManager.storageInfo.collect { info ->
                _uiState.update { it.copy(storageInfo = info) }
            }
        }
        viewModelScope.launch {
            connectionManager.isOverheating.collect { hot ->
                _uiState.update { it.copy(isOverheating = hot) }
            }
        }

        // Connection state
        viewModelScope.launch {
            connectionManager.connectionState.collect { state ->
                Log.d(TAG, "Connection state: $state")
                _uiState.update { it.copy(connectionState = state) }

                when (state) {
                    ConnectionState.BLE_CONNECTED -> {
                        _uiState.update {
                            it.copy(
                                cameraModel = "Insta360",
                                cameraSerial = connectionManager.wifiSsid,
                                wifiSsid = connectionManager.wifiSsid,
                                wifiPassword = connectionManager.wifiPassword
                            )
                        }
                        viewModelScope.launch { connectionManager.connectViaWifi(getApplication()) }
                    }
                    ConnectionState.CONNECTED -> {
                        _uiState.update { it.copy(
                            isCameraReady = true,
                            cameraModel = connectionManager.cameraDeviceType.ifEmpty { "Insta360" },
                            firmwareVersion = connectionManager.firmwareVersion,
                            cameraSerial = connectionManager.cameraDeviceSerial.ifEmpty { connectionManager.wifiSsid }
                        ) }
                        gpsTracker = GpsTracker(getApplication()) {
                            _uiState.update { it.copy(waypointCount = it.waypointCount + 1) }
                        }
                        gpsTracker?.startSession(System.currentTimeMillis())
                        viewModelScope.launch {
                            delay(3000)
                            loadModesAndSettings()
                        }
                    }
                    ConnectionState.DISCONNECTED -> {
                        sessionJob?.cancel(); sessionJob = null
                        gpsTracker?.endSession(); gpsTracker = null
                        _uiState.update {
                            it.copy(
                                isCameraReady = false, isRecording = false, isCountdownActive = false,
                                cameraModel = "", cameraSerial = "", wifiSsid = "", wifiPassword = "",
                                emulatorBaseUrl = "", recordingTimeMs = 0,
                                availableModes = emptyList(), availableSettings = emptyList(),
                                captureMode = "", captureModeId = "",
                                firmwareVersion = "", batteryLevel = 0, isCharging = false,
                                storageInfo = StorageInfo(), isOverheating = false,
                                displayTime = if (it.isTimerModeEnabled) formatTimerDisplay(it.initialTimerValue) else "00:00"
                            )
                        }
                    }
                    else -> {}
                }
            }
        }

        // Recording state (real SDK)
        viewModelScope.launch {
            connectionManager.isRecording.collect { recording ->
                if (_uiState.value.emulatorBaseUrl.isNotEmpty()) return@collect
                val isTimed = _uiState.value.isTimerModeEnabled
                _uiState.update { it.copy(isRecording = recording, isCountdownActive = if (recording) isTimed else false) }
                if (recording) {
                    startSessionTimer(isTimed)
                    gpsTracker?.onRecordingStarted(System.currentTimeMillis())
                } else {
                    gpsTracker?.onRecordingStopped(System.currentTimeMillis())
                    stopSessionTimer()
                }
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  BLE Scan                                                            //
    // ------------------------------------------------------------------ //

    fun startScan() { connectionManager.startScan() }
    fun stopScan() { connectionManager.stopScan() }
    fun connectDevice(device: ScannedDevice) { connectionManager.connect(device) }

    fun disconnect() {
        sessionJob?.cancel(); sessionJob = null
        gpsTracker?.endSession(); gpsTracker = null
        connectionManager.disconnect()
        _uiState.update { curr ->
            CameraUiState(
                initialTimerValue = curr.initialTimerValue,
                currentTimerValue = curr.initialTimerValue,
                isTimerModeEnabled = curr.isTimerModeEnabled,
                displayTime = if (curr.isTimerModeEnabled) formatTimerDisplay(curr.initialTimerValue) else "00:00"
            )
        }
    }

    // ------------------------------------------------------------------ //
    //  Recording                                                           //
    // ------------------------------------------------------------------ //

    fun startRecording() {
        val isTimed = _uiState.value.isTimerModeEnabled
        if (_uiState.value.emulatorBaseUrl.isNotEmpty()) {
            sessionJob?.cancel()
            _uiState.update { it.copy(isRecording = true, isCountdownActive = isTimed) }
            startSessionTimer(isTimed)
            gpsTracker?.onRecordingStarted(System.currentTimeMillis())
        } else {
            connectionManager.startRecording()
        }
    }

    fun stopRecording() {
        if (_uiState.value.emulatorBaseUrl.isNotEmpty()) {
            gpsTracker?.onRecordingStopped(System.currentTimeMillis())
            stopSessionTimer()
        } else {
            connectionManager.stopRecording()
        }
    }

    fun takePhoto() { if (_uiState.value.emulatorBaseUrl.isEmpty()) connectionManager.takePhoto() }

    fun markHilight() {
        connectionManager.markHilight()
        gpsTracker?.onHilight(System.currentTimeMillis())
    }

    private fun startSessionTimer(isTimed: Boolean) {
        sessionJob?.cancel()
        sessionJob = viewModelScope.launch {
            if (isTimed) {
                var current = _uiState.value.initialTimerValue
                while (current > 0) {
                    _uiState.update { it.copy(displayTime = formatTimerDisplay(current), currentTimerValue = current) }
                    delay(1000)
                    current--
                }
                _uiState.update { it.copy(isRecording = false, isCountdownActive = false, displayTime = "00:00", recordingTimeMs = 0) }
                if (_uiState.value.emulatorBaseUrl.isEmpty()) connectionManager.stopRecording()
                delay(500)
                _uiState.update { it.copy(displayTime = if (it.isTimerModeEnabled) formatTimerDisplay(it.initialTimerValue) else "00:00") }
            } else {
                val startMs = System.currentTimeMillis()
                while (true) {
                    delay(1000)
                    val elapsed = System.currentTimeMillis() - startMs
                    val secs = (elapsed / 1000).toInt()
                    _uiState.update { it.copy(recordingTimeMs = elapsed, displayTime = "%02d:%02d".format(secs / 60, secs % 60)) }
                }
            }
        }
    }

    private fun stopSessionTimer() {
        sessionJob?.cancel(); sessionJob = null
        _uiState.update { it.copy(
            isRecording = false, isCountdownActive = false, recordingTimeMs = 0,
            displayTime = if (it.isTimerModeEnabled) formatTimerDisplay(it.initialTimerValue) else "00:00"
        ) }
    }

    // ------------------------------------------------------------------ //
    //  Timer mode                                                          //
    // ------------------------------------------------------------------ //

    fun toggleTimerMode() {
        if (!_uiState.value.isRecording && !_uiState.value.isCountdownActive) {
            _uiState.update {
                val newMode = !it.isTimerModeEnabled
                it.copy(isTimerModeEnabled = newMode, displayTime = if (newMode) formatTimerDisplay(it.initialTimerValue) else "00:00")
            }
        }
    }

    fun adjustTimer(delta: Int) {
        if (!_uiState.value.isRecording && !_uiState.value.isCountdownActive) {
            _uiState.update {
                val newValue = (it.initialTimerValue + delta).coerceIn(5, 300)
                saveTimerValue(newValue)
                it.copy(initialTimerValue = newValue, currentTimerValue = newValue,
                    displayTime = if (it.isTimerModeEnabled) formatTimerDisplay(newValue) else "00:00")
            }
        }
    }

    fun snapTimerToFive() {
        if (!_uiState.value.isRecording && !_uiState.value.isCountdownActive) {
            _uiState.update {
                val snapped = ((it.initialTimerValue + 2) / 5 * 5).coerceIn(5, 300)
                saveTimerValue(snapped)
                it.copy(initialTimerValue = snapped, currentTimerValue = snapped,
                    displayTime = if (it.isTimerModeEnabled) formatTimerDisplay(snapped) else "00:00")
            }
        }
    }

    private fun formatTimerDisplay(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)
    private fun saveTimerValue(v: Int) = prefs.edit().putInt(KEY_TIMER_VALUE, v).apply()
    private fun loadTimerValue(): Int = prefs.getInt(KEY_TIMER_VALUE, 15)

    // ------------------------------------------------------------------ //
    //  Modes & Settings                                                    //
    // ------------------------------------------------------------------ //

    fun loadModesAndSettings() {
        val modes = connectionManager.getAvailableModes()
        val current = connectionManager.getCurrentMode()
        val settings = connectionManager.getAvailableSettings()
        Log.d(TAG, "Modes: ${modes.size}, settings: ${settings.size}, current: ${current?.id}")
        _uiState.update { it.copy(
            availableModes = modes,
            availableSettings = settings,
            captureMode = current?.name ?: "",
            captureModeId = current?.id ?: ""
        ) }
    }

    fun switchMode(modeId: String) {
        if (_uiState.value.emulatorBaseUrl.isNotEmpty()) {
            val mode = emuModes().find { it.id == modeId } ?: return
            _uiState.update { it.copy(
                captureModeId = modeId,
                captureMode = mode.name,
                availableSettings = emuSettings(modeId)
            ) }
        } else {
            connectionManager.switchCaptureMode(modeId)
            viewModelScope.launch {
                delay(600)
                loadModesAndSettings()
            }
        }
    }

    fun changeSetting(settingId: String, valueId: String) {
        connectionManager.changeSetting(settingId, valueId)
        val settings = connectionManager.getAvailableSettings()
        _uiState.update { it.copy(availableSettings = settings) }
    }

    fun selectTab(tab: Int) { _uiState.update { it.copy(selectedTab = tab) } }

    fun toggleBubble() {
        val newValue = !_uiState.value.isBubbleEnabled
        prefs.edit().putBoolean(KEY_BUBBLE_ENABLED, newValue).apply()
        _uiState.update { it.copy(isBubbleEnabled = newValue) }
    }

    fun toggleDarkMode() {
        val newValue = !_uiState.value.isDarkMode
        prefs.edit().putBoolean(KEY_DARK_MODE, newValue).apply()
        _uiState.update { it.copy(isDarkMode = newValue) }
    }

    fun shutdownCamera() {
        connectionManager.shutdownCamera()
        disconnect()
    }

    fun syncTime() {
        connectionManager.syncTime()
    }

    // ------------------------------------------------------------------ //
    //  Debug emulator shortcut                                             //
    // ------------------------------------------------------------------ //

    fun connectDebugEmulator(baseUrl: String = "http://192.168.23.8:8080") {
        _uiState.update {
            it.copy(
                connectionState = ConnectionState.CONNECTED,
                isCameraReady = true,
                cameraModel = "X4 [EMU]",
                cameraSerial = "IS4A1234567890",
                firmwareVersion = "v3.0.55.0",
                wifiSsid = "X4.IS4A1234567890.OSC",
                wifiPassword = "88888888",
                emulatorBaseUrl = baseUrl,
                batteryLevel = 85,
                isCharging = false,
                storageInfo = StorageInfo(
                    freeSpaceBytes = 29_500_000_000L,
                    totalSpaceBytes = 63_900_000_000L,
                    sdCardPresent = true
                ),
                captureModeId = "RECORD_NORMAL",
                captureMode = "Vidéo normale",
                availableModes = emuModes(),
                availableSettings = emuSettings("RECORD_NORMAL")
            )
        }
    }

    private fun emuModes() = listOf(
        com.actioncam.airbuble.camera.CaptureMode("RECORD_NORMAL",    "Vidéo normale",   isVideoMode = true),
        com.actioncam.airbuble.camera.CaptureMode("TIMELAPSE",        "Timelapse",       isVideoMode = true),
        com.actioncam.airbuble.camera.CaptureMode("HDR_RECORD",       "Vidéo HDR",       isVideoMode = true),
        com.actioncam.airbuble.camera.CaptureMode("BULLETTIME",       "Bullet Time",     isVideoMode = true),
        com.actioncam.airbuble.camera.CaptureMode("SLOW_MOTION",      "Ralenti",         isVideoMode = true),
        com.actioncam.airbuble.camera.CaptureMode("LOOPER_RECORDING", "Boucle",          isVideoMode = true),
        com.actioncam.airbuble.camera.CaptureMode("CAPTURE_NORMAL",   "Photo",           isPhotoMode = true),
        com.actioncam.airbuble.camera.CaptureMode("HDR_CAPTURE",      "Photo HDR",       isPhotoMode = true),
        com.actioncam.airbuble.camera.CaptureMode("BURST",            "Rafale",          isPhotoMode = true),
        com.actioncam.airbuble.camera.CaptureMode("INTERVAL_SHOOTING","Intervalomètre",  isPhotoMode = true)
    )

    private fun emuSettings(modeId: String): List<com.actioncam.airbuble.camera.SettingDescriptor> {
        val isVideo = emuModes().find { it.id == modeId }?.isVideoMode ?: true
        val res = if (isVideo) com.actioncam.airbuble.camera.SettingDescriptor(
            id = "RECORD_RESOLUTION", name = "Résolution vidéo", currentValue = "4K 30fps",
            availableValues = listOf("5.7K 30fps","5.7K 24fps","4K 60fps","4K 30fps","2.7K 60fps","2.7K 30fps","1080p 60fps","1080p 30fps")
                .mapIndexed { i, v -> com.actioncam.airbuble.camera.SettingValue(i.toString(), v) }
        ) else com.actioncam.airbuble.camera.SettingDescriptor(
            id = "PHOTO_RESOLUTION", name = "Résolution photo", currentValue = "72MP",
            availableValues = listOf("72MP","18MP","12MP")
                .mapIndexed { i, v -> com.actioncam.airbuble.camera.SettingValue(i.toString(), v) }
        )
        val iso = com.actioncam.airbuble.camera.SettingDescriptor(
            id = "ISO", name = "ISO", currentValue = "Auto",
            availableValues = listOf("Auto","100","200","400","800","1600","3200","6400")
                .mapIndexed { i, v -> com.actioncam.airbuble.camera.SettingValue(i.toString(), v) }
        )
        val ev = com.actioncam.airbuble.camera.SettingDescriptor(
            id = "EV", name = "Compensation EV", currentValue = "0.0",
            availableValues = listOf("-3.0","-2.0","-1.5","-1.0","-0.5","0.0","+0.5","+1.0","+1.5","+2.0","+3.0")
                .mapIndexed { i, v -> com.actioncam.airbuble.camera.SettingValue(i.toString(), v) }
        )
        val wb = com.actioncam.airbuble.camera.SettingDescriptor(
            id = "WB", name = "Balance des blancs", currentValue = "Auto",
            availableValues = listOf("Auto","2700K","4000K","5000K","5500K","6000K","6500K","7500K")
                .mapIndexed { i, v -> com.actioncam.airbuble.camera.SettingValue(i.toString(), v) }
        )
        val result = mutableListOf(res, iso, ev, wb)
        if (isVideo) {
            result += com.actioncam.airbuble.camera.SettingDescriptor(
                id = "SHUTTER", name = "Vitesse d'obturation", currentValue = "Auto",
                availableValues = listOf("Auto","1/8000","1/4000","1/2000","1/1000","1/500","1/250","1/120","1/60","1/30","1/15","1/8","1/4","1/2","1\"")
                    .mapIndexed { i, v -> com.actioncam.airbuble.camera.SettingValue(i.toString(), v) }
            )
            result += com.actioncam.airbuble.camera.SettingDescriptor(
                id = "GAMMA_MODE", name = "Mode gamma", currentValue = "Standard",
                availableValues = listOf("Standard","Vivid","LOG","Flat")
                    .mapIndexed { i, v -> com.actioncam.airbuble.camera.SettingValue(i.toString(), v) }
            )
        }
        if (modeId == "TIMELAPSE" || modeId == "INTERVAL_SHOOTING") {
            result += com.actioncam.airbuble.camera.SettingDescriptor(
                id = "INTERVAL", name = "Intervalle", currentValue = "2s",
                availableValues = listOf("2s","3s","5s","10s","15s","20s","30s","60s","120s","300s")
                    .mapIndexed { i, v -> com.actioncam.airbuble.camera.SettingValue(i.toString(), v) }
            )
        }
        return result
    }

    // ------------------------------------------------------------------ //
    //  Lifecycle                                                           //
    // ------------------------------------------------------------------ //

    override fun onCleared() {
        super.onCleared()
        sessionJob?.cancel()
        gpsTracker?.endSession()
        connectionManager.destroy()
    }
}
