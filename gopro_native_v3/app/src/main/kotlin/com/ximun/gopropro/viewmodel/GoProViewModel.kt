package com.ximun.gopropro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CameraUiState(
    val isConnected: Boolean = false,
    val isRecording: Boolean = false,
    val isCountdownActive: Boolean = false,
    val initialTimerValue: Int = 15,
    val currentTimerValue: Int = 15,
    val batteryLevel: Int = 0,
    val batteryBars: Int = 0,
    val isCharging: Boolean = false,
    val storageSpace: String = "N/A",
    val currentPresetName: String = "Vidéo",
    val displayTime: String = "00:00",
    val isTimerModeEnabled: Boolean = false,
    val selectedTab: Int = 0,
    
    // Nouveaux états pour les réglages
    val settings: Map<Int, Int> = emptyMap(),
    val capabilities: Map<Int, List<Int>> = emptyMap(),

    // Nouveaux états pour les presets
    val presetGroups: List<com.ximun.gopropro.proto.GoProProtos.PresetGroup> = emptyList(),
    val currentPresetId: Int = -1,
    
    // Nouveaux états pour les status
    val sdRemainingKb: Long = 0L,
    val sdCapacityKb: Long = 0L,
    val videoRemainingSec: Int = 0,
    val storagePercent: Int = 0, 
    val sdCapacityFormatted: String = "N/A",
    val videoRemainingTime: String = "N/A", 
    val tempStatus: String = "OK",
    val isOverheating: Boolean = false,
    
    val photosRemaining: Int = 0,
    val videosCount: Int = 0,
    val sdStatusLabel: String = "OK", // Mapping ID 33
    
    val firmwareVersion: String = "v1.40",
    val serialNumber: String = "C34413...",
    val cameraName: String = "HERO 11 Mini" // Valeur par défaut
)


class GoProViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private var sessionJob: Job? = null
    private var recordingSeconds = 0

    fun updateConnection(connected: Boolean) {
        _uiState.update { it.copy(isConnected = connected) }
    }

    fun updateBattery(level: Int) {
        _uiState.update { it.copy(batteryLevel = level) }
    }

    fun updateBatteryBars(bars: Int) {
        // Selon doc OpenGoPro : 0=Zero, 1=One, 2=Two, 3=Three, 4=Charging
        _uiState.update { it.copy(batteryBars = bars, isCharging = (bars == 4)) }
    }
    
    fun updateTempStatus(isHot: Boolean, isCold: Boolean) {
        val status = if (isHot) "HOT" else if (isCold) "COLD" else "OK"
        _uiState.update { it.copy(tempStatus = status, isOverheating = isHot) }
    }

    fun updateSettings(newSettings: Map<Int, Int>) {
        _uiState.update { it.copy(settings = it.settings + newSettings) }
    }

    fun updateCapabilities(newCapabilities: Map<Int, List<Int>>) {
        _uiState.update { it.copy(capabilities = it.capabilities + newCapabilities) }
    }

    fun updateRecording(started: Boolean, isTimed: Boolean = false, onAutoStop: (() -> Unit)? = null) {
        if (started == _uiState.value.isRecording && !isTimed) return
        
        sessionJob?.cancel()
        _uiState.update { it.copy(
            isRecording = started,
            isCountdownActive = if (started) isTimed else false
        ) }

        if (started) {
            startSessionTimer(isTimed, onAutoStop)
        } else {
            _uiState.update { it.copy(
                displayTime = if (it.isTimerModeEnabled) String.format("00:%02d", it.initialTimerValue) else "00:00"
            ) }
        }
    }

    private fun startSessionTimer(isTimed: Boolean, onAutoStop: (() -> Unit)?) {
        sessionJob = viewModelScope.launch {
            if (isTimed) {
                var current = _uiState.value.initialTimerValue
                while (current > 0) {
                    _uiState.update { it.copy(
                        displayTime = String.format("00:%02d", current),
                        currentTimerValue = current
                    ) }
                    delay(1000)
                    current--
                }
                _uiState.update { it.copy(isRecording = false, isCountdownActive = false, displayTime = "00:00") }
                onAutoStop?.invoke()
            } else {
                recordingSeconds = 0
                while (true) {
                    val m = (recordingSeconds % 3600) / 60
                    val s = recordingSeconds % 60
                    _uiState.update { it.copy(displayTime = String.format("%02d:%02d", m, s)) }
                    delay(1000)
                    recordingSeconds++
                }
            }
        }
    }

    fun stopCountdown() {
        sessionJob?.cancel()
        _uiState.update { it.copy(
            isRecording = false,
            isCountdownActive = false, 
            displayTime = if (it.isTimerModeEnabled) String.format("00:%02d", it.initialTimerValue) else "00:00"
        ) }
    }

    fun toggleTimerMode() {
        if (!_uiState.value.isRecording && !_uiState.value.isCountdownActive) {
            _uiState.update { 
                val newMode = !it.isTimerModeEnabled
                it.copy(
                    isTimerModeEnabled = newMode,
                    displayTime = if (newMode) String.format("00:%02d", it.initialTimerValue) else "00:00"
                )
            }
        }
    }

    fun adjustTimer(delta: Int) {
        if (!_uiState.value.isRecording && !_uiState.value.isCountdownActive) {
            _uiState.update { 
                val newValue = (it.initialTimerValue + delta).coerceIn(1, 60)
                it.copy(
                    initialTimerValue = newValue, 
                    currentTimerValue = newValue,
                    displayTime = if (it.isTimerModeEnabled) String.format("00:%02d", newValue) else "00:00"
                ) 
            }
        }
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun updateStorage(space: String) {
        android.util.Log.d("GoProViewModel", "Update storage: $space")
        _uiState.update { it.copy(storageSpace = space) }
    }

    fun updatePreset(name: String) {
        _uiState.update { it.copy(currentPresetName = name) }
    }

    fun updatePresets(groups: List<com.ximun.gopropro.proto.GoProProtos.PresetGroup>) {
        _uiState.update { it.copy(presetGroups = groups) }
    }

    fun updateCurrentPresetId(id: Int) {
        _uiState.update { it.copy(currentPresetId = id) }
    }

    fun updateHardwareInfo(serial: String, version: String, name: String) {
        _uiState.update { it.copy(serialNumber = serial, firmwareVersion = version, cameraName = name) }
    }

    fun updateSdRemaining(kb: Long) {
        val s = formatSize(kb)
        // Calcul pourcentage si on a la capacité
        val cap = _uiState.value.sdCapacityKb
        val pct = if (cap > 0) ((cap - kb).toFloat() / cap * 100).toInt() else 0

        _uiState.update { it.copy(
            sdRemainingKb = kb, 
            storageSpace = s,
            storagePercent = pct
        ) }
    }

    fun updateSdCapacity(kb: Long) {
        val s = formatSize(kb)
        // Recalcul pourcentage avec nouvelle capacité
        val rem = _uiState.value.sdRemainingKb
        val pct = if (kb > 0) ((kb - rem).toFloat() / kb * 100).toInt() else 0

        _uiState.update { it.copy(
            sdCapacityKb = kb, 
            sdCapacityFormatted = s,
            storagePercent = pct
        ) }
    }

    fun updateVideoRemaining(seconds: Int) {
        val t = formatDuration(seconds)
        _uiState.update { it.copy(videoRemainingSec = seconds, videoRemainingTime = t) }
    }

    fun updatePhotosRemaining(count: Int) {
        _uiState.update { it.copy(photosRemaining = count) }
    }

    fun updateVideosCount(count: Int) {
        _uiState.update { it.copy(videosCount = count) }
    }

    fun updateSdStatus(code: Int) {
        val label = when (code) {
            0 -> "OK"
            1 -> "CARTE PLEINE"
            2 -> "ABSENTE"
            3 -> "ERREUR FORMAT"
            4 -> "OCCUPÉE"
            8 -> "CHANGÉE"
            else -> "ERREUR ($code)"
        }
        _uiState.update { it.copy(sdStatusLabel = label) }
    }

    // --- HELPERS DE MAPPING ---

    fun getLensLabel(id: Int): String {
        return when (id) {
            0 -> "Large"
            3 -> "SuperView"
            4 -> "Linéaire"
            10 -> "HyperView"
            else -> "Standard"
        }
    }

    fun getRatioLabel(id: Int): String {
        return when (id) {
            0 -> "4:3"
            1 -> "16:9"
            2 -> "8:7"
            4 -> "9:16"
            else -> "Auto"
        }
    }

    fun getGpsLabel(id: Int): String {
        return if (id == 1) "ACTIF" else "DÉSACTIVÉ"
    }

    private fun formatSize(kb: Long): String {
        val mb = kb / 1024f
        val gb = mb / 1024f
        return when {
            gb >= 1 -> String.format("%.0f Go", gb)
            mb >= 1 -> String.format("%.0f Mo", mb)
            else -> "$kb Ko"
        }
    }

    private fun formatDuration(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) {
            String.format("%d:%02d:%02d", h, m, s)
        } else {
            String.format("%02d:%02d", m, s)
        }
    }
}
