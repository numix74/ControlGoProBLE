"""
Builds TLV (Tag-Length-Value) responses for GoPro BLE queries.
GoPro format: [QueryID, Status, {TagID, Length, Value...}...]
"""

import struct


def _encode_value(value: int) -> bytes:
    """Encode an integer as the smallest possible big-endian byte sequence."""
    if value < 0:
        value = 0
    if value <= 0xFF:
        return struct.pack(">B", value)
    elif value <= 0xFFFF:
        return struct.pack(">H", value)
    elif value <= 0xFFFFFFFF:
        return struct.pack(">I", value)
    else:
        return struct.pack(">Q", value)


def build_settings_response(query_id: int, settings: dict[int, int]) -> bytes:
    """
    Build TLV response for settings queries (0x12, 0x52, 0x92).
    Format: [QueryID, 0x00, {SettingID, Len, Value}...]
    """
    result = bytearray([query_id, 0x00])  # 0x00 = success
    for setting_id, value in sorted(settings.items()):
        encoded = _encode_value(value)
        result.append(setting_id & 0xFF)
        result.append(len(encoded))
        result.extend(encoded)
    return bytes(result)


def build_status_response(query_id: int, statuses: dict[int, int]) -> bytes:
    """
    Build TLV response for status queries (0x13, 0x53, 0x93).
    Format: [QueryID, 0x00, {StatusID, Len, Value}...]
    """
    result = bytearray([query_id, 0x00])
    for status_id, value in sorted(statuses.items()):
        encoded = _encode_value(value)
        result.append(status_id & 0xFF)
        result.append(len(encoded))
        result.extend(encoded)
    return bytes(result)


def build_capabilities_response(query_id: int, capabilities: dict[int, list[int]]) -> bytes:
    """
    Build TLV response for capabilities queries (0x32, 0x62, 0xA2).
    Format: [QueryID, 0x00, {SettingID, Len, Val1, Val2, ...}...]
    Each capability value is encoded as 1 byte.
    """
    result = bytearray([query_id, 0x00])
    for setting_id, values in sorted(capabilities.items()):
        result.append(setting_id & 0xFF)
        result.append(len(values))
        for v in values:
            result.append(v & 0xFF)
    return bytes(result)


def build_command_response(command_id: int, status: int = 0x00) -> bytes:
    """
    Build a simple command response.
    Format: [CommandID, Status]
    Status 0x00 = success, 0x01 = error.
    """
    return bytes([command_id, status])


def build_hw_info_response(
    model_name: str,
    model_number: str,
    board_type: str,
    firmware: str,
    serial: str,
    ap_ssid: str,
    ap_mac: str = "AA:BB:CC:DD:EE:FF",
) -> bytes:
    """
    Build HW Info (0x3C) response.
    Format: [0x3C, 0x00, {field_count}, for each: {len, string_bytes}...]
    Fields: model_number, model_name, board_type, firmware, serial, ap_ssid, ap_mac
    """
    fields = [model_number, model_name, board_type, firmware, serial, ap_ssid, ap_mac]
    result = bytearray([0x3C, 0x00, len(fields)])
    for field in fields:
        encoded = field.encode("utf-8")
        result.append(len(encoded))
        result.extend(encoded)
    return bytes(result)


def build_setting_write_response(setting_id: int, status: int = 0x00) -> bytes:
    """
    Build a setting write response.
    Format: [SettingID, Status]
    """
    return bytes([setting_id, status])
