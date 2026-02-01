package com.ximun.gopropro.ble

import java.util.UUID

object GoProConstants {
    // Services
    val GOPRO_SERVICE_UUID: UUID = UUID.fromString("0000fea6-0000-1000-8000-00805f9b34fb")
    
    // Characteristics
    val COMMAND_CHAR_UUID: UUID = UUID.fromString("b5f90072-aa8d-11e3-9046-0002a5d5c51b")
    val COMMAND_RSP_CHAR_UUID: UUID = UUID.fromString("b5f90073-aa8d-11e3-9046-0002a5d5c51b")
    val SETTINGS_CHAR_UUID: UUID = UUID.fromString("b5f90074-aa8d-11e3-9046-0002a5d5c51b")
    val SETTINGS_RSP_CHAR_UUID: UUID = UUID.fromString("b5f90075-aa8d-11e3-9046-0002a5d5c51b")
    val QUERY_CHAR_UUID: UUID = UUID.fromString("b5f90076-aa8d-11e3-9046-0002a5d5c51b")
    val QUERY_RSP_CHAR_UUID: UUID = UUID.fromString("b5f90077-aa8d-11e3-9046-0002a5d5c51b")

    // Commands
    const val CMD_GET_HARDWARE_INFO = 0x3C
    const val CMD_SET_SHUTTER = 0x01
    const val CMD_HILIGHT = 0x18
    const val CMD_LOAD_PRESET = 0x40
    const val CMD_KEEP_ALIVE = 0x5B
    const val CMD_KEEP_ALIVE_VAL = 0x42

    // Query IDs (Corrected for HERO 9/10/11/12)
    const val QRY_REGISTER_SETTINGS_UPDATES = 0x52
    const val QRY_REGISTER_STATUS_UPDATES = 0x53
    const val QRY_GET_STATUS_VALUES = 0x13
    const val QRY_GET_SETTINGS_VALUES = 0x12
    const val QRY_GET_SETTING_CAPABILITIES = 0x32
    const val QRY_REGISTER_CAPABILITIES_UPDATES = 0x62
    
    // Async responses
    const val RSP_ASYNC_SETTING = 0x92
    const val RSP_ASYNC_STATUS = 0x93
    const val RSP_ASYNC_CAPABILITIES = 0xA2

    // Setting IDs
    const val SETTING_ID_RESOLUTION = 2
    const val SETTING_ID_FPS = 3
    const val SETTING_ID_LENS = 121
    const val SETTING_ID_HYPERSMOOTH = 135
    const val SETTING_ID_COLOR = 134
    const val SETTING_ID_ISO_MAX = 122
    const val SETTING_ID_WHITE_BALANCE = 124

    // Status IDs
    const val STATUS_ID_RECORDING = 10
    const val STATUS_ID_BATTERY = 70
    const val STATUS_ID_STORAGE = 54
    const val STATUS_ID_ACTIVE_PRESET = 97
    const val STATUS_ID_BUSY = 8
}
