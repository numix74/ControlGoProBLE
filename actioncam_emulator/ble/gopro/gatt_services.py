"""
GoPro GATT service handler.
Routes BLE writes to the appropriate handler and sends fragmented responses.
"""

import asyncio
import logging

from core.camera_state import CameraState
from core.profile_loader import CameraProfile
from ble.gopro.packet_codec import build_ble_packets, Defragmenter
from ble.gopro.command_handler import CommandHandler
from ble.gopro.settings_handler import SettingsHandler
from ble.gopro.query_handler import QueryHandler

logger = logging.getLogger(__name__)


class GoProGattHandler:
    """Manages GoPro GATT characteristics and routes writes to handlers."""

    def __init__(self, state: CameraState, profile: CameraProfile):
        self.state = state
        self.profile = profile
        self.char_uuids = profile.ble.characteristics

        # Handlers
        self._command_handler = CommandHandler(state, profile)
        self._settings_handler = SettingsHandler(state)
        self._query_handler = QueryHandler(state, profile)

        # Per-characteristic defragmenters
        self._defragmenters: dict[str, Defragmenter] = {
            self.char_uuids["command"]: Defragmenter(),
            self.char_uuids["settings"]: Defragmenter(),
            self.char_uuids["query"]: Defragmenter(),
        }

        # Map write UUIDs to (handler, response UUID)
        self._routes: dict[str, tuple] = {
            self.char_uuids["command"]: (self._command_handler, self.char_uuids["command_rsp"]),
            self.char_uuids["settings"]: (self._settings_handler, self.char_uuids["settings_rsp"]),
            self.char_uuids["query"]: (self._query_handler, self.char_uuids["query_rsp"]),
        }

        # Notification sender — set by BleServer
        self._send_notification = None

    def set_notification_sender(self, sender):
        """Set callback: async sender(char_uuid: str, data: bytes)"""
        self._send_notification = sender

    def get_service_uuid(self) -> str:
        return self.profile.ble.service_uuid

    def get_write_uuids(self) -> list[str]:
        return [
            self.char_uuids["command"],
            self.char_uuids["settings"],
            self.char_uuids["query"],
        ]

    def get_notify_uuids(self) -> list[str]:
        return [
            self.char_uuids["command_rsp"],
            self.char_uuids["settings_rsp"],
            self.char_uuids["query_rsp"],
        ]

    def _match_uuid(self, incoming: str, reference: str) -> bool:
        return incoming.lower() == reference.lower()

    async def handle_write(self, char_uuid: str, data: bytes):
        """Process an incoming write on a characteristic."""
        # Find matching defragmenter
        defrag = None
        matched_key = None
        for ref_uuid, d in self._defragmenters.items():
            if self._match_uuid(char_uuid, ref_uuid):
                defrag = d
                matched_key = ref_uuid
                break

        if defrag is None:
            logger.warning(f"Write on unknown characteristic: {char_uuid}")
            return

        # Defragment — returns complete payload or None
        payload = defrag.process_packet(data)
        if payload is None:
            return

        # Route to handler
        handler, rsp_uuid = self._routes[matched_key]
        logger.debug(f"Payload [{len(payload)} bytes] → {handler.__class__.__name__}")

        response = await handler.handle(payload)

        if response is not None and self._send_notification:
            packets = build_ble_packets(response)
            for packet in packets:
                await self._send_notification(rsp_uuid, packet)
