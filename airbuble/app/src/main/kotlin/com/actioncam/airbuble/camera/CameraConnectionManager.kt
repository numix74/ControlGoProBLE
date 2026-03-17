package com.actioncam.airbuble.camera

import kotlinx.coroutines.flow.StateFlow

/**
 * Connection states shared across all camera implementations (GoPro, Insta360, etc.).
 */
enum class ConnectionState {
    DISCONNECTED,
    SCANNING,
    BLE_CONNECTING,
    BLE_CONNECTED,
    WIFI_CONNECTING,
    CONNECTED,
    ERROR
}

/**
 * Device discovered during BLE scan.
 * [raw] holds the SDK-specific object (BleDevice for Insta360, ScanResult for GoPro).
 */
data class ScannedDevice(
    val name: String,
    val id: String,
    val type: String = "",
    val rssi: Int = 0,
    val raw: Any? = null
)

/**
 * Abstract camera control interface.
 * Each camera brand implements this to expose a unified API to the ViewModel/UI layers.
 * When merging GoPro + Insta360 into one app, the ViewModel only interacts with this interface.
 */
interface CameraConnectionManager {

    /** Current connection state. */
    val connectionState: StateFlow<ConnectionState>

    /** Devices found during the current BLE scan. */
    val scannedDevices: StateFlow<List<ScannedDevice>>

    // --- Scan ---
    fun startScan()
    fun stopScan()

    // --- Connection ---
    fun connect(device: ScannedDevice)
    fun disconnect()

    // --- Camera control ---
    fun startRecording()
    fun stopRecording()
    fun takePhoto()
    fun shutdownCamera()
    fun syncTime()

    /**
     * Mark a highlight moment.
     * GoPro: sends hilight BLE command to camera.
     * Insta360: GPS waypoint only (SDK has no hilight concept).
     */
    fun markHilight()

    // --- Capture modes ---
    fun switchCaptureMode(modeId: String)
    fun getAvailableModes(): List<CaptureMode>
    fun getCurrentMode(): CaptureMode?

    // --- Settings ---
    fun getAvailableSettings(): List<SettingDescriptor>
    fun changeSetting(settingId: String, valueId: String)

    // --- Lifecycle ---
    fun setAppForeground(foreground: Boolean)
    fun destroy()
}
