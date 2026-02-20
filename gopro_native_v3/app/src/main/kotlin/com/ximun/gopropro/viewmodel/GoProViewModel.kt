package com.ximun.gopropro.viewmodel
 
import java.util.Locale

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.ximun.gopropro.proto.GoProProtos

data class CameraUiState(
    val isConnected: Boolean = false,
    val isBleReady: Boolean = false,
    val isBluetoothEnabled: Boolean = false,
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
    val presetGroups: List<GoProProtos.PresetGroup> = emptyList(),
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
    
    val firmwareVersion: String = "",
    val serialNumber: String = "",
    val cameraName: String = "",

    // GPS & waypoints
    val waypointCount: Int = 0,

    // Préférences app
    val isDarkMode: Boolean = true,
    val isBubbleEnabled: Boolean = true
)


class GoProViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val PREFS_NAME = "gopro_prefs"
        private const val KEY_TIMER_VALUE = "timer_value"
        private const val DEFAULT_TIMER = 15
    }

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val savedTimer = prefs.getInt(KEY_TIMER_VALUE, DEFAULT_TIMER).coerceIn(5, 300)

    private val _uiState = MutableStateFlow(CameraUiState(
        initialTimerValue = savedTimer,
        currentTimerValue = savedTimer
    ))
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private var sessionJob: Job? = null
    private var recordingSeconds = 0

    private fun saveTimerValue(value: Int) {
        prefs.edit().putInt(KEY_TIMER_VALUE, value).apply()
    }

    fun setBleReady(ready: Boolean) {
        _uiState.update { it.copy(isBleReady = ready) }
    }

    fun setBluetoothEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isBluetoothEnabled = enabled) }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun toggleBubble() {
        _uiState.update { it.copy(isBubbleEnabled = !it.isBubbleEnabled) }
    }

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
        _uiState.update { old -> old.copy(settings = old.settings + newSettings) }
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
                displayTime = if (it.isTimerModeEnabled) formatTimerDisplay(it.initialTimerValue) else "00:00"
            ) }
        }
    }

    private fun startSessionTimer(isTimed: Boolean, onAutoStop: (() -> Unit)?) {
        sessionJob = viewModelScope.launch {
            if (isTimed) {
                var current = _uiState.value.initialTimerValue
                while (current > 0) {
                    _uiState.update { it.copy(
                        displayTime = formatTimerDisplay(current),
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
                    _uiState.update { it.copy(displayTime = String.format(Locale.US, "%02d:%02d", m, s)) }
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
            displayTime = if (it.isTimerModeEnabled) formatTimerDisplay(it.initialTimerValue) else "00:00"
        ) }
    }

    fun toggleTimerMode() {
        if (!_uiState.value.isRecording && !_uiState.value.isCountdownActive) {
            _uiState.update { 
                val newMode = !it.isTimerModeEnabled
                it.copy(
                    isTimerModeEnabled = newMode,
                    displayTime = if (newMode) formatTimerDisplay(it.initialTimerValue) else "00:00"
                )
            }
        }
    }

    fun adjustTimer(delta: Int) {
        if (!_uiState.value.isRecording && !_uiState.value.isCountdownActive) {
            _uiState.update {
                val newValue = (it.initialTimerValue + delta).coerceIn(5, 300)
                saveTimerValue(newValue)
                it.copy(
                    initialTimerValue = newValue,
                    currentTimerValue = newValue,
                    displayTime = if (it.isTimerModeEnabled) formatTimerDisplay(newValue) else "00:00"
                )
            }
        }
    }

    /**
     * Arrondit la valeur du timer au multiple de 5 le plus proche.
     * Appelé quand l'utilisateur relâche un appui long (pas de 1s).
     */
    fun snapTimerToFive() {
        if (!_uiState.value.isRecording && !_uiState.value.isCountdownActive) {
            _uiState.update {
                val snapped = ((it.initialTimerValue + 2) / 5 * 5).coerceIn(5, 300)
                saveTimerValue(snapped)
                it.copy(
                    initialTimerValue = snapped,
                    currentTimerValue = snapped,
                    displayTime = if (it.isTimerModeEnabled) formatTimerDisplay(snapped) else "00:00"
                )
            }
        }
    }

    private fun formatTimerDisplay(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format(Locale.US, "%02d:%02d", m, s)
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

    fun updatePresets(newGroups: List<GoProProtos.PresetGroup>) {
        _uiState.update { state ->
            // Cache tous les presets qui ont des infos utiles
            val oldPresetCache = mutableMapOf<Int, GoProProtos.Preset>()
            for (group in state.presetGroups) {
                for (preset in group.presetArrayList) {
                    if (preset.hasTitleId() || preset.hasIcon() || preset.hasCustomName()
                        || preset.settingArrayCount > 0) {
                        oldPresetCache[preset.id] = preset
                    }
                }
            }

            val mergedGroups = newGroups.map { group ->
                val mergedPresets = group.presetArrayList.map { newPreset ->
                    val oldPreset = oldPresetCache[newPreset.id]
                    if (oldPreset != null) {
                        val builder = newPreset.toBuilder()
                        var modified = false
                        if (!newPreset.hasTitleId() && oldPreset.hasTitleId()) {
                            builder.titleId = oldPreset.titleId
                            modified = true
                        }
                        if (!newPreset.hasIcon() && oldPreset.hasIcon()) {
                            builder.icon = oldPreset.icon
                            modified = true
                        }
                        if (!newPreset.hasMode() && oldPreset.hasMode()) {
                            builder.mode = oldPreset.mode
                            modified = true
                        }
                        if (newPreset.settingArrayCount == 0 && oldPreset.settingArrayCount > 0) {
                            builder.addAllSettingArray(oldPreset.settingArrayList)
                            modified = true
                        }
                        if (modified) builder.build() else newPreset
                    } else {
                        newPreset
                    }
                }
                group.toBuilder().clearPresetArray().addAllPresetArray(mergedPresets).build()
            }

            state.copy(presetGroups = mergedGroups)
        }
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

    fun incrementWaypointCount() {
        _uiState.update { it.copy(waypointCount = it.waypointCount + 1) }
    }

    fun resetWaypointCount() {
        _uiState.update { it.copy(waypointCount = 0) }
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

    private fun formatSize(kb: Long): String {
        val mb = kb / 1024f
        val gb = mb / 1024f
        return when {
            gb >= 1 -> String.format(Locale.US, "%.0f Go", gb)
            mb >= 1 -> String.format(Locale.US, "%.0f Mo", mb)
            else -> "$kb Ko"
        }
    }

    private fun formatDuration(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) {
            String.format(Locale.US, "%d:%02d:%02d", h, m, s)
        } else {
            String.format(Locale.US, "%02d:%02d", m, s)
        }
    }
}
