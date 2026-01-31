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

    // Settings
    const val SETTING_ID_RESOLUTION = 2
    const val SETTING_ID_FPS = 3
    const val SETTING_ID_LENS = 121
}
