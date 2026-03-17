package com.actioncam.airbuble.camera

/**
 * Unified camera UI state, brand-agnostic.
 * Both GoPro and Insta360 implementations converge to this single state class.
 * The ViewModel exposes a StateFlow<CameraUiState> consumed by all Compose screens.
 */
data class CameraUiState(
    // Connection
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val isCameraReady: Boolean = false,
    val cameraModel: String = "",
    val cameraSerial: String = "",
    val firmwareVersion: String = "",
    val wifiSsid: String = "",
    val wifiPassword: String = "",
    val emulatorBaseUrl: String = "",   // DEBUG only — empty in prod

    // Recording
    val isRecording: Boolean = false,
    val captureMode: String = "",
    val captureModeId: String = "",
    val isPhotoMode: Boolean = false,
    val recordingTimeMs: Long = 0,

    // Battery & Storage
    val batteryLevel: Int = 0,
    val isCharging: Boolean = false,
    val storageInfo: StorageInfo = StorageInfo(),
    val sdRemainingFormatted: String = "N/A",

    // Modes & Settings
    val availableModes: List<CaptureMode> = emptyList(),
    val availableSettings: List<SettingDescriptor> = emptyList(),

    // Temperature
    val isOverheating: Boolean = false,

    // Timer
    val isTimerModeEnabled: Boolean = false,
    val isCountdownActive: Boolean = false,
    val initialTimerValue: Int = 15,
    val currentTimerValue: Int = 15,
    val displayTime: String = "00:00",

    // GPS
    val waypointCount: Int = 0,

    // App UI
    val isDarkMode: Boolean = true,
    val isBubbleEnabled: Boolean = true,
    val selectedTab: Int = 0
)

data class StorageInfo(
    val freeSpaceBytes: Long = 0,
    val totalSpaceBytes: Long = 0,
    val sdCardPresent: Boolean = false
)

data class CaptureMode(
    val id: String,
    val name: String,
    val isPhotoMode: Boolean = false,
    val isVideoMode: Boolean = false,
    val isLiveMode: Boolean = false
)

data class SettingDescriptor(
    val id: String,
    val name: String,
    val currentValue: String,
    val availableValues: List<SettingValue>
)

data class SettingValue(
    val id: String,
    val displayName: String
)
