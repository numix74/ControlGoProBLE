package com.ximun.gopropro.ble

import java.util.UUID

@Suppress("unused")
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
    const val CMD_GET_VERSION = 0x51
    const val CMD_SET_SHUTTER = 0x01
    const val CMD_HILIGHT = 0x18
    const val CMD_LOAD_PRESET = 0x40
    const val CMD_KEEP_ALIVE = 0x5B
    const val CMD_KEEP_ALIVE_VAL = 0x42
    const val CMD_SLEEP = 0x05
    const val CMD_REBOOT = 0x11
    const val CMD_SET_DATE = 0x0D
    const val CMD_CAMERA_CONTROL = 0xF1 // Commande via Protobuf/Global

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
    const val SETTING_ID_ASPECT_RATIO = 108
    const val SETTING_ID_LENS = 121
    const val SETTING_ID_PHOTO_LENS = 122
    const val SETTING_ID_HYPERSMOOTH = 135
    const val SETTING_ID_ANTI_FLICKER = 134   // 0x86 - Anti-Flicker
    const val SETTING_ID_BIT_RATE = 182       // 0xB6 - Video Bit Rate
    const val SETTING_ID_BIT_DEPTH = 183      // 0xB7 - Video Bit Depth
    const val SETTING_ID_VIDEO_PROFILE = 184  // 0xB8 - Video Profile
    const val SETTING_ID_HINDSIGHT = 167
    const val SETTING_ID_TIMELAPSE_RATE = 5
    const val SETTING_ID_PHOTO_TIMELAPSE_RATE = 30
    const val SETTING_ID_NIGHT_LAPSE_RATE = 32
    const val SETTING_ID_TIMEWARP_SPEED = 111
    const val SETTING_ID_TIMELAPSE_LENS = 123
    const val SETTING_ID_PHOTO_OUTPUT = 125
    const val SETTING_ID_MEDIA_FORMAT = 128
    const val SETTING_ID_PHOTO_SINGLE_INTERVAL = 171
    const val SETTING_ID_PHOTO_INTERVAL_DURATION = 172
    const val SETTING_ID_VIDEO_PERFORMANCE_MODE = 173
    const val SETTING_ID_CONTROL_MODE = 175
    const val SETTING_ID_EASY_MODE_SPEED = 176
    const val SETTING_ID_NIGHT_PHOTO = 177
    const val SETTING_ID_WIRELESS_BAND = 178
    const val SETTING_ID_STAR_TRAILS_LENGTH = 179
    const val SETTING_ID_SYSTEM_VIDEO_MODE = 180
    const val SETTING_ID_VIDEO_EASY_MODE = 186
    const val SETTING_ID_LAPSE_MODE = 187
    const val SETTING_ID_MAX_LENS_MOD = 189
    const val SETTING_ID_MAX_LENS_MOD_ENABLE = 190
    const val SETTING_ID_EASY_NIGHT_PHOTO = 191
    const val SETTING_ID_MULTI_SHOT_ASPECT_RATIO = 192
    const val SETTING_ID_FRAMING = 193
    const val SETTING_ID_CAMERA_MODE = 194
    const val SETTING_ID_BEEP_VOLUME = 216
    const val SETTING_ID_SCREEN_SAVER = 219
    const val SETTING_ID_LANGUAGE = 223
    const val SETTING_ID_PHOTO_MODE = 227
    const val SETTING_ID_VIDEO_FRAMING = 232
    const val SETTING_ID_MULTI_SHOT_FRAMING = 233
    const val SETTING_ID_FRAME_RATE = 234
    const val SETTING_ID_AUTO_POWER_DOWN = 59
    const val SETTING_ID_GPS = 83
    const val SETTING_ID_LCD_BRIGHTNESS = 88
    const val SETTING_ID_LED = 91

    // Status IDs
    const val STATUS_ID_RECORDING = 10
    const val STATUS_ID_BATTERY = 70
    const val STATUS_ID_BATTERY_BARS = 2 // Internal Battery Bars / Charging state
    const val STATUS_ID_STORAGE = 54 // SD Remaining (KB) - UInt64
    const val STATUS_ID_SD_CAPACITY = 117 // SD Capacity (KB) - UInt64
    const val STATUS_ID_SD_STATUS = 33 // SD State (OK, Full, Missing...)
    const val STATUS_ID_SYSTEM_READY = 82 // Is the system fully booted and ready?
    const val STATUS_ID_PHOTOS_REMAINING = 34 // Remaining Photos (uint32) - Photos remaining before SD full
    const val STATUS_ID_PHOTOS_TOTAL = 38    // Total photos on sdcard (uint32)
    const val STATUS_ID_VIDEOS_COUNT = 39    // Total videos on sdcard (uint32)
    const val STATUS_ID_VIDEO_REMAINING = 35 // Video Remaining (Sec) - UInt32
    const val STATUS_ID_ACTIVE_PRESET = 97
    const val STATUS_ID_BUSY = 8
    const val STATUS_ID_OVERHEATING = 6
}
