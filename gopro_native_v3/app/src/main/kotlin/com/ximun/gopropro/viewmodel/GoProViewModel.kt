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
    val storageSpace: String = "64 Go",
    val currentPresetName: String = "Vidéo",
    val displayTime: String = "00:00",
    val isTimerModeEnabled: Boolean = false,
    val selectedTab: Int = 0,
    
    // Nouveaux états pour les réglages
    val settings: Map<Int, Int> = emptyMap(),
    val capabilities: Map<Int, List<Int>> = emptyMap()
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
        _uiState.update { it.copy(storageSpace = space) }
    }

    fun updatePreset(name: String) {
        _uiState.update { it.copy(currentPresetName = name) }
    }
}
