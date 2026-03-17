"""
Handles GoPro BLE command writes on characteristic GP-0072.
Produces responses to be sent on GP-0073.
"""

import asyncio
from core.camera_state import CameraState
from core.profile_loader import CameraProfile
from ble.gopro.tlv_builder import build_command_response, build_hw_info_response


class CommandHandler:
    """Dispatches GoPro commands and returns response payloads."""

    # Command IDs
    CMD_SET_SHUTTER = 0x01
    CMD_SLEEP = 0x05
    CMD_SET_DATE = 0x0D
    CMD_REBOOT = 0x11
    CMD_HILIGHT = 0x18
    CMD_HW_INFO = 0x3C
    CMD_LOAD_PRESET = 0x40
    CMD_GET_VERSION = 0x51
    CMD_KEEP_ALIVE = 0x5B
    CMD_CAMERA_CONTROL = 0xF1

    def __init__(self, state: CameraState, profile: CameraProfile):
        self.state = state
        self.profile = profile

    async def handle(self, payload: bytes) -> bytes | None:
        """
        Process a defragmented command payload.
        Returns the response payload, or None if no response.
        """
        if not payload:
            return None

        cmd_id = payload[0]

        if cmd_id == self.CMD_HW_INFO:
            return self._handle_hw_info()

        elif cmd_id == self.CMD_SET_SHUTTER:
            return await self._handle_shutter(payload)

        elif cmd_id == self.CMD_KEEP_ALIVE:
            self.state.log_command("Keep Alive")
            return build_command_response(cmd_id, 0x00)

        elif cmd_id == self.CMD_HILIGHT:
            self.state.hilight_count += 1
            self.state.log_command(f"HiLight #{self.state.hilight_count}")
            return build_command_response(cmd_id, 0x00)

        elif cmd_id == self.CMD_LOAD_PRESET:
            return await self._handle_load_preset(payload)

        elif cmd_id == self.CMD_SLEEP:
            self.state.log_command("Sleep")
            return build_command_response(cmd_id, 0x00)

        elif cmd_id == self.CMD_SET_DATE:
            self.state.log_command("Set Date/Time")
            return build_command_response(cmd_id, 0x00)

        elif cmd_id == self.CMD_REBOOT:
            self.state.log_command("Reboot")
            return build_command_response(cmd_id, 0x00)

        elif cmd_id == self.CMD_GET_VERSION:
            self.state.log_command("Get Version")
            return build_command_response(cmd_id, 0x00)

        elif cmd_id == self.CMD_CAMERA_CONTROL:
            return self._handle_camera_control(payload)

        else:
            self.state.log_command(f"Unknown cmd 0x{cmd_id:02X}")
            return build_command_response(cmd_id, 0x00)

    def _handle_hw_info(self) -> bytes:
        self.state.log_command("HW Info")
        return build_hw_info_response(
            model_name=self.profile.model,
            model_number=str(self.profile.model_id),
            board_type=self.profile.board_type,
            firmware=self.profile.firmware,
            serial=self.profile.serial,
            ap_ssid=self.profile.ble.ap_ssid,
        )

    async def _handle_shutter(self, payload: bytes) -> bytes:
        if len(payload) >= 3:
            shutter_on = payload[2] == 1
            if shutter_on:
                self.state.is_recording = True
                import time
                self.state.recording_start_time = time.time()
                self.state.log_command("Shutter ON (recording)")
            else:
                self.state.is_recording = False
                self.state.recording_start_time = 0.0
                self.state.log_command("Shutter OFF (stopped)")
            await self.state.event_bus.emit("state_changed", status_id=10)
        else:
            await self.state.toggle_recording()
            action = "ON" if self.state.is_recording else "OFF"
            self.state.log_command(f"Shutter {action}")
        return build_command_response(self.CMD_SET_SHUTTER, 0x00)

    async def _handle_load_preset(self, payload: bytes) -> bytes:
        if len(payload) >= 6:
            # Format: 0x40 0x04 <4 bytes preset ID big-endian>
            preset_id = int.from_bytes(payload[2:6], byteorder="big")
        elif len(payload) >= 3:
            preset_id = payload[2]
        else:
            return build_command_response(self.CMD_LOAD_PRESET, 0x02)

        success = await self.state.load_preset(preset_id)
        self.state.log_command(f"Load Preset {preset_id} ({'OK' if success else 'FAIL'})")
        return build_command_response(self.CMD_LOAD_PRESET, 0x00 if success else 0x02)

    def _handle_camera_control(self, payload: bytes) -> bytes:
        """Handle protobuf camera control (0xF1). For MVP, just ACK."""
        self.state.log_command("Camera Control (protobuf)")
        # Response: feature_id=0xF1, action_id=0xE9, result=0x00
        return bytes([0xF1, 0xE9, 0x00])
