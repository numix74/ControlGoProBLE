"""Integration tests for GoPro BLE command / settings / query handlers."""

import asyncio
import pytest

from core.profile_loader import load_profile
from core.camera_state import CameraState
from ble.gopro.command_handler import CommandHandler
from ble.gopro.settings_handler import SettingsHandler
from ble.gopro.query_handler import QueryHandler
from ble.gopro.tlv_builder import build_command_response


@pytest.fixture
def ctx():
    profile = load_profile("gopro_hero12")
    state = CameraState(profile)
    return state, profile


# ---------------------------------------------------------------------------
# CommandHandler
# ---------------------------------------------------------------------------

class TestCommandHandler:

    def test_hw_info(self, ctx):
        state, profile = ctx
        h = CommandHandler(state, profile)
        resp = asyncio.run(h.handle(bytes([0x3C])))
        assert resp[0] == 0x3C
        assert resp[1] == 0x00
        # field count = 7
        assert resp[2] == 7

    def test_shutter_on(self, ctx):
        state, profile = ctx
        h = CommandHandler(state, profile)
        resp = asyncio.run(h.handle(bytes([0x01, 0x01, 0x01])))
        assert state.is_recording is True
        assert resp == bytes([0x01, 0x00])

    def test_shutter_off(self, ctx):
        state, profile = ctx
        h = CommandHandler(state, profile)
        asyncio.run(h.handle(bytes([0x01, 0x01, 0x01])))  # ON
        resp = asyncio.run(h.handle(bytes([0x01, 0x01, 0x00])))  # OFF
        assert state.is_recording is False
        assert resp == bytes([0x01, 0x00])

    def test_keep_alive(self, ctx):
        state, profile = ctx
        h = CommandHandler(state, profile)
        resp = asyncio.run(h.handle(bytes([0x5B])))
        assert resp == bytes([0x5B, 0x00])

    def test_load_valid_preset(self, ctx):
        state, profile = ctx
        h = CommandHandler(state, profile)
        # Preset 0 = Activity (exists in HERO12 profile)
        resp = asyncio.run(h.handle(bytes([0x40, 0x04, 0x00, 0x00, 0x00, 0x00])))
        assert resp[0] == 0x40
        assert resp[1] == 0x00  # success
        assert state.active_preset_id == 0

    def test_load_invalid_preset(self, ctx):
        state, profile = ctx
        h = CommandHandler(state, profile)
        resp = asyncio.run(h.handle(bytes([0x40, 0x04, 0x00, 0x00, 0xFF, 0xFF])))
        assert resp[1] == 0x02  # error


# ---------------------------------------------------------------------------
# SettingsHandler
# ---------------------------------------------------------------------------

class TestSettingsHandler:

    def test_set_valid_setting(self, ctx):
        state, profile = ctx
        h = SettingsHandler(state)
        # Setting 2 (resolution), value 4 (valid per YAML)
        resp = asyncio.run(h.handle(bytes([0x02, 0x01, 0x04])))
        assert resp == bytes([0x02, 0x00])
        assert state.settings[2] == 4

    def test_set_invalid_value_rejected(self, ctx):
        state, profile = ctx
        h = SettingsHandler(state)
        original = state.settings[2]
        resp = asyncio.run(h.handle(bytes([0x02, 0x01, 0x63])))  # 99 not in caps
        assert resp == bytes([0x02, 0x06])
        assert state.settings[2] == original  # unchanged

    def test_set_unknown_setting_accepted(self, ctx):
        """Unknown setting IDs (not in caps) are accepted with no capability check."""
        state, profile = ctx
        h = SettingsHandler(state)
        resp = asyncio.run(h.handle(bytes([0xFF, 0x01, 0x01])))
        # No capabilities defined → accepted
        assert resp[1] == 0x00

    def test_short_payload_returns_none(self, ctx):
        state, profile = ctx
        h = SettingsHandler(state)
        resp = asyncio.run(h.handle(bytes([0x02, 0x01])))  # only 2 bytes
        assert resp is None


# ---------------------------------------------------------------------------
# QueryHandler
# ---------------------------------------------------------------------------

class TestQueryHandler:

    def test_get_all_settings(self, ctx):
        state, profile = ctx
        h = QueryHandler(state, profile)
        resp = asyncio.run(h.handle(bytes([0x12])))
        assert resp[0] == 0x12
        assert resp[1] == 0x00
        assert len(resp) > 4  # at least one setting TLV

    def test_get_specific_status(self, ctx):
        state, profile = ctx
        h = QueryHandler(state, profile)
        # Query status IDs 10 (recording) and 70 (battery %)
        resp = asyncio.run(h.handle(bytes([0x13, 10, 70])))
        assert resp[0] == 0x13
        assert resp[1] == 0x00
        # TLV: [10, len, val, 70, len, val]
        assert 10 in resp[2:]
        assert 70 in resp[2:]

    def test_get_capabilities(self, ctx):
        state, profile = ctx
        h = QueryHandler(state, profile)
        resp = asyncio.run(h.handle(bytes([0x32])))
        assert resp[0] == 0x32
        assert resp[1] == 0x00
        assert len(resp) > 4

    def test_register_settings(self, ctx):
        state, profile = ctx
        h = QueryHandler(state, profile)
        resp = asyncio.run(h.handle(bytes([0x52, 2, 3])))  # register IDs 2 and 3
        assert 2 in state.registered_setting_ids
        assert 3 in state.registered_setting_ids
        assert resp[0] == 0x52

    def test_unknown_query_returns_ack(self, ctx):
        state, profile = ctx
        h = QueryHandler(state, profile)
        resp = asyncio.run(h.handle(bytes([0xAB])))
        assert resp[0] == 0xAB
        assert resp[1] == 0x00
