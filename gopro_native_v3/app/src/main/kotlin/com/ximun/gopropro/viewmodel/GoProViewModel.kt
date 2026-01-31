package com.ximun.gopropro.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CameraUiState(
    val isConnected: Boolean = false,
    val isRecording: Boolean = false,
    val batteryLevel: Int = 0,
    val statusText: String = "Déconnecté",
    val storageSpace: String = "N/A",
    val currentPreset: String = "Vidéo"
)

class GoProViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun updateConnection(connected: Boolean) {
        _uiState.update { it.copy(
            isConnected = connected,
            statusText = if (connected) "Prêt" else "Déconnecté"
        ) }
    }

    fun updateBattery(level: Int) {
        _uiState.update { it.copy(batteryLevel = level) }
    }

    fun updateRecording(recording: Boolean) {
        _uiState.update { it.copy(isRecording = recording) }
    }

    fun updateStatusText(text: String) {
        _uiState.update { it.copy(statusText = text) }
    }

    fun updateStorage(space: String) {
        _uiState.update { it.copy(storageSpace = space) }
    }
}
