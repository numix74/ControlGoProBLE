"""
Handles GoPro BLE query writes on characteristic GP-0076.
Produces responses to be sent on GP-0077.
"""

from core.camera_state import CameraState
from core.profile_loader import CameraProfile
from ble.gopro.tlv_builder import (
    build_settings_response,
    build_status_response,
    build_capabilities_response,
)

# All known status IDs to return
ALL_STATUS_IDS = [2, 6, 8, 10, 13, 33, 34, 35, 38, 39, 54, 70, 82, 97, 117]


class QueryHandler:
    """Dispatches GoPro queries and returns response payloads."""

    # Query command IDs
    QRY_GET_SETTINGS = 0x12
    QRY_GET_STATUS = 0x13
    QRY_GET_CAPABILITIES = 0x32
    QRY_REGISTER_SETTINGS = 0x52
    QRY_REGISTER_STATUS = 0x53
    QRY_REGISTER_CAPABILITIES = 0x62

    # Protobuf query
    QRY_PROTOBUF = 0xF5

    def __init__(self, state: CameraState, profile: CameraProfile):
        self.state = state
        self.profile = profile

    async def handle(self, payload: bytes) -> bytes | None:
        """
        Process a defragmented query payload.
        Returns the response payload.
        """
        if not payload:
            return None

        query_id = payload[0]

        if query_id == self.QRY_GET_SETTINGS:
            return self._handle_get_settings(payload)

        elif query_id == self.QRY_GET_STATUS:
            return self._handle_get_status(payload)

        elif query_id == self.QRY_GET_CAPABILITIES:
            return self._handle_get_capabilities(payload)

        elif query_id == self.QRY_REGISTER_SETTINGS:
            return self._handle_register_settings(payload)

        elif query_id == self.QRY_REGISTER_STATUS:
            return self._handle_register_status(payload)

        elif query_id == self.QRY_REGISTER_CAPABILITIES:
            return self._handle_register_capabilities(payload)

        elif query_id == self.QRY_PROTOBUF:
            return self._handle_protobuf_query(payload)

        else:
            self.state.log_command(f"Unknown query 0x{query_id:02X}")
            return bytes([query_id, 0x00])

    def _handle_get_settings(self, payload: bytes) -> bytes:
        """Return all current setting values."""
        self.state.log_command("Get All Settings")
        requested_ids = list(payload[1:]) if len(payload) > 1 else list(self.state.settings.keys())
        settings = {sid: self.state.settings.get(sid, 0) for sid in requested_ids}
        return build_settings_response(self.QRY_GET_SETTINGS, settings)

    def _handle_get_status(self, payload: bytes) -> bytes:
        """Return all current status values."""
        self.state.log_command("Get All Status")
        requested_ids = list(payload[1:]) if len(payload) > 1 else ALL_STATUS_IDS
        statuses = {sid: self.state.get_status_value(sid) for sid in requested_ids}
        return build_status_response(self.QRY_GET_STATUS, statuses)

    def _handle_get_capabilities(self, payload: bytes) -> bytes:
        """Return capabilities for all or requested settings."""
        self.state.log_command("Get Capabilities")
        requested_ids = list(payload[1:]) if len(payload) > 1 else list(self.state.capabilities.keys())
        caps = {sid: self.state.capabilities.get(sid, []) for sid in requested_ids}
        return build_capabilities_response(self.QRY_GET_CAPABILITIES, caps)

    def _handle_register_settings(self, payload: bytes) -> bytes:
        """Register for async setting updates and return current values."""
        setting_ids = list(payload[1:])
        self.state.registered_setting_ids.update(setting_ids)
        self.state.log_command(f"Register Settings [{len(setting_ids)} IDs]")
        settings = {sid: self.state.settings.get(sid, 0) for sid in setting_ids}
        return build_settings_response(self.QRY_REGISTER_SETTINGS, settings)

    def _handle_register_status(self, payload: bytes) -> bytes:
        """Register for async status updates and return current values."""
        status_ids = list(payload[1:])
        self.state.registered_status_ids.update(status_ids)
        self.state.log_command(f"Register Status [{len(status_ids)} IDs]")
        statuses = {sid: self.state.get_status_value(sid) for sid in status_ids}
        return build_status_response(self.QRY_REGISTER_STATUS, statuses)

    def _handle_register_capabilities(self, payload: bytes) -> bytes:
        """Register for async capability updates and return current values."""
        cap_ids = list(payload[1:])
        self.state.registered_capability_ids.update(cap_ids)
        self.state.log_command(f"Register Capabilities [{len(cap_ids)} IDs]")
        caps = {sid: self.state.capabilities.get(sid, []) for sid in cap_ids}
        return build_capabilities_response(self.QRY_REGISTER_CAPABILITIES, caps)

    def _handle_protobuf_query(self, payload: bytes) -> bytes:
        """Handle protobuf queries (0xF5). MVP: return preset status stub."""
        if len(payload) >= 2:
            action_id = payload[1]
            self.state.log_command(f"Protobuf Query action=0x{action_id:02X}")

            if action_id == 0x72:
                # Get Preset Status — return minimal protobuf response
                return self._build_preset_status_response()

        return bytes([0xF5, 0xF2, 0x00])

    def _build_preset_status_response(self) -> bytes:
        """
        Build a minimal preset status protobuf response.
        For MVP, we return the raw preset groups from the profile
        as a simplified protobuf-like structure.
        Full protobuf implementation in Phase 2.
        """
        # Header: feature_id=0xF5, action_id=0xF2 (response), result=0x00
        # Followed by serialized NotifyPresetStatus protobuf
        # For now, return success with empty payload
        self.state.log_command("Preset Status (stub)")
        return bytes([0xF5, 0xF2, 0x00])
