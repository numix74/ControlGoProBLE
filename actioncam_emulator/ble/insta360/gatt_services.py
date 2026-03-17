"""
Insta360 BLE GATT handler.
UUIDs réels (reverse-engineered, confirmés SDK v1.9.11) :
  Service : 0000be80-0000-1000-8000-00805f9b34fb  (0xBE80)
  Write   : 0000be81-0000-1000-8000-00805f9b34fb  (0xBE81) — commandes app→caméra
  Notify  : 0000be82-0000-1000-8000-00805f9b34fb  (0xBE82) — réponses caméra→app

Flux d'échange credentials WiFi :
  1. Client active les notifications sur BE82
  2. Client écrit sur BE81 pour demander les credentials
  3. Caméra envoie SSID + password via notifications BE82 (TLV)
  4. Client se connecte au WiFi AP (SSID: Model.Serial.OSC)
  5. Contrôle HTTP OSC sur 192.168.42.1
"""

import asyncio
import logging

from core.camera_state import CameraState
from core.profile_loader import CameraProfile

logger = logging.getLogger(__name__)

# Control command bytes
CMD_REQUEST_WIFI_CREDS = 0x01
CMD_KEEP_ALIVE         = 0x02
CMD_SHUTTER_ON         = 0x10
CMD_SHUTTER_OFF        = 0x11


class Insta360GattHandler:
    """Handles Insta360 BLE credential exchange and basic camera control."""

    def __init__(self, state: CameraState, profile: CameraProfile):
        self.state = state
        self.profile = profile
        self.char_uuids = profile.ble.characteristics
        self._send_notification = None
        self._client_authenticated = False

    def set_notification_sender(self, sender):
        self._send_notification = sender

    def get_service_uuid(self) -> str:
        return self.profile.ble.service_uuid

    def get_write_uuids(self) -> list:
        return [self.char_uuids["control"]]

    def get_notify_uuids(self) -> list:
        return [self.char_uuids["notify"]]

    def get_read_uuids(self) -> list:
        """BE82 est readable + notifiable."""
        return [self.char_uuids["notify"]]

    def get_characteristic_value(self, char_uuid: str) -> bytes:
        """Valeur par défaut de BE82 = SSID encodé UTF-8."""
        if char_uuid.lower() == self.char_uuids["notify"].lower():
            return self.profile.wifi.ssid.encode("utf-8")
        return b""

    async def handle_write(self, char_uuid: str, data: bytes):
        """Handle a write to the control characteristic."""
        ctrl_uuid = self.char_uuids["control"].lower()
        if char_uuid.lower() != ctrl_uuid:
            logger.warning(f"Write on unexpected char: {char_uuid}")
            return

        if not data:
            return

        cmd = data[0]

        if cmd == CMD_REQUEST_WIFI_CREDS:
            self.state.log_command("Insta360: WiFi Creds Requested")
            await self._send_wifi_credentials()

        elif cmd == CMD_KEEP_ALIVE:
            self.state.log_command("Insta360: Keep Alive")
            await self._notify(bytes([0x02, 0x00]))  # ACK

        elif cmd == CMD_SHUTTER_ON:
            self.state.is_recording = True
            import time; self.state.recording_start_time = time.time()
            self.state.log_command("Insta360: Shutter ON")
            await self.state.event_bus.emit("state_changed", status_id=10)
            await self._notify(bytes([0x10, 0x00]))  # ACK

        elif cmd == CMD_SHUTTER_OFF:
            self.state.is_recording = False
            self.state.recording_start_time = 0.0
            self.state.log_command("Insta360: Shutter OFF")
            await self.state.event_bus.emit("state_changed", status_id=10)
            await self._notify(bytes([0x11, 0x00]))  # ACK

        else:
            logger.warning(f"Unknown command: 0x{cmd:02X}")
            await self._notify(bytes([cmd, 0xFF]))  # NACK

    async def _send_wifi_credentials(self):
        """
        Send WiFi SSID then password as TLV notifications.
        Format: [Type, Length, ...Value]
          Type 0x01 = SSID
          Type 0x02 = Password
          Type 0x03 = IP address (optional)
        """
        ssid = self.profile.wifi.ssid.encode("utf-8")
        pwd  = self.profile.wifi.password.encode("utf-8")
        ip   = self.profile.wifi.camera_ip.encode("utf-8")

        # SSID packet
        await self._notify(bytes([0x01, len(ssid)]) + ssid)
        # Password packet
        await self._notify(bytes([0x02, len(pwd)]) + pwd)
        # Camera IP
        await self._notify(bytes([0x03, len(ip)]) + ip)

    async def _notify(self, data: bytes):
        if self._send_notification:
            await self._send_notification(self.char_uuids["notify"], data)
