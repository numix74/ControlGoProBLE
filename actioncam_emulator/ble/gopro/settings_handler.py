"""
Handles GoPro BLE setting writes on characteristic GP-0074.
Produces responses to be sent on GP-0075.
"""

from core.camera_state import CameraState
from ble.gopro.tlv_builder import build_setting_write_response


class SettingsHandler:
    """Dispatches GoPro setting writes and returns response payloads."""

    def __init__(self, state: CameraState):
        self.state = state

    async def handle(self, payload: bytes) -> bytes | None:
        """
        Process a setting write payload.
        Format: [SettingID, ValueLen, Value...]
        Returns the response payload.
        """
        if len(payload) < 3:
            return None

        setting_id = payload[0]
        value_len = payload[1]
        value = int.from_bytes(payload[2:2 + value_len], byteorder="big")

        success = await self.state.set_setting(setting_id, value)

        if success:
            self.state.log_command(f"Setting {setting_id} = {value}")
        else:
            self.state.log_command(f"Setting {setting_id} = {value} (REJECTED)")

        return build_setting_write_response(setting_id, 0x00 if success else 0x06)
