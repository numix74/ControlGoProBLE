"""Tests fonctionnels protocole BLE Insta360 — sans hardware BLE.

Protocole testé (UUIDs SDK 1.9.11 réels, reverse-engineered DEX) :
  Service  : 00003366-0000-1000-8000-00805f9b34fb
  Char     : 00003377-0000-1000-8000-00805f9b34fb  (write + notify)

Flux credentials :
  app → caméra : [0x01]              demande creds WiFi
  caméra → app : [0x01, len, SSID]  notification TLV type 1
  caméra → app : [0x02, len, pwd]   notification TLV type 2
  caméra → app : [0x03, len, ip]    notification TLV type 3
"""

import asyncio
import pytest

from core.profile_loader import load_profile
from core.camera_state import CameraState
from ble.insta360.gatt_services import (
    Insta360GattHandler,
    CMD_REQUEST_WIFI_CREDS,
    CMD_KEEP_ALIVE,
    CMD_SHUTTER_ON,
    CMD_SHUTTER_OFF,
)


# ---------------------------------------------------------------------------
# Fixture
# ---------------------------------------------------------------------------

@pytest.fixture
def ctx():
    profile = load_profile("insta360_x4")
    state = CameraState(profile)
    handler = Insta360GattHandler(state, profile)

    notifications: list[tuple[str, bytes]] = []

    async def collect(uuid: str, data: bytes) -> None:
        notifications.append((uuid, data))

    handler.set_notification_sender(collect)
    return handler, state, profile, notifications


def ctrl_uuid(ctx_tuple) -> str:
    _, _, profile, _ = ctx_tuple
    return profile.ble.characteristics["control"].lower()


def notify_uuid(ctx_tuple) -> str:
    _, _, profile, _ = ctx_tuple
    return profile.ble.characteristics["notify"].lower()


def write(ctx_tuple, data: bytes) -> None:
    handler, _, _, _ = ctx_tuple
    asyncio.run(handler.handle_write(ctrl_uuid(ctx_tuple), data))


# ---------------------------------------------------------------------------
# WiFi credentials flow
# ---------------------------------------------------------------------------

class TestWifiCredentials:

    def test_three_notifications_sent(self, ctx):
        """CMD 0x01 déclenche exactement 3 notifications TLV."""
        _, _, _, notifs = ctx
        write(ctx, bytes([CMD_REQUEST_WIFI_CREDS]))
        assert len(notifs) == 3

    def test_ssid_packet_type_and_content(self, ctx):
        """1ère notification : type=0x01, payload = SSID UTF-8."""
        _, _, profile, notifs = ctx
        write(ctx, bytes([CMD_REQUEST_WIFI_CREDS]))
        _, data = notifs[0]
        assert data[0] == 0x01                               # type SSID
        length = data[1]
        payload = data[2:2 + length]
        assert payload == profile.wifi.ssid.encode("utf-8")

    def test_password_packet_type_and_content(self, ctx):
        """2ème notification : type=0x02, payload = password UTF-8."""
        _, _, profile, notifs = ctx
        write(ctx, bytes([CMD_REQUEST_WIFI_CREDS]))
        _, data = notifs[1]
        assert data[0] == 0x02                               # type password
        length = data[1]
        payload = data[2:2 + length]
        assert payload == profile.wifi.password.encode("utf-8")

    def test_ip_packet_type_and_content(self, ctx):
        """3ème notification : type=0x03, payload = IP caméra UTF-8."""
        _, _, profile, notifs = ctx
        write(ctx, bytes([CMD_REQUEST_WIFI_CREDS]))
        _, data = notifs[2]
        assert data[0] == 0x03                               # type IP
        length = data[1]
        payload = data[2:2 + length]
        assert payload == profile.wifi.camera_ip.encode("utf-8")

    def test_tlv_length_fields_are_accurate(self, ctx):
        """Le champ Length de chaque TLV correspond exactement à len(Value)."""
        write(ctx, bytes([CMD_REQUEST_WIFI_CREDS]))
        _, _, _, notifs = ctx
        for _, data in notifs:
            claimed_len = data[1]
            actual_len = len(data) - 2          # header = type + length
            assert claimed_len == actual_len

    def test_ssid_matches_profile_format(self, ctx):
        """Le SSID extrait respecte le format <Model>.<Serial>.OSC."""
        _, _, profile, notifs = ctx
        write(ctx, bytes([CMD_REQUEST_WIFI_CREDS]))
        _, data = notifs[0]
        ssid = data[2:].decode("utf-8")
        assert ssid == profile.wifi.ssid
        assert ".OSC" in ssid

    def test_notifications_sent_to_notify_char(self, ctx):
        """Toutes les notifications sont envoyées sur la characteristique notify (0x3377)."""
        _, _, profile, notifs = ctx
        write(ctx, bytes([CMD_REQUEST_WIFI_CREDS]))
        expected_uuid = profile.ble.characteristics["notify"].lower()
        for uuid, _ in notifs:
            assert uuid.lower() == expected_uuid


# ---------------------------------------------------------------------------
# Keep Alive
# ---------------------------------------------------------------------------

class TestKeepAlive:

    def test_ack_format(self, ctx):
        """CMD 0x02 → ACK [0x02, 0x00]."""
        _, _, _, notifs = ctx
        write(ctx, bytes([CMD_KEEP_ALIVE]))
        assert len(notifs) == 1
        assert notifs[0][1] == bytes([0x02, 0x00])

    def test_state_unchanged(self, ctx):
        """Keep-alive ne modifie pas l'état de recording."""
        _, state, _, _ = ctx
        write(ctx, bytes([CMD_KEEP_ALIVE]))
        assert state.is_recording is False


# ---------------------------------------------------------------------------
# Shutter ON / OFF
# ---------------------------------------------------------------------------

class TestShutter:

    def test_shutter_on_sets_recording(self, ctx):
        """CMD 0x10 → state.is_recording == True."""
        _, state, _, _ = ctx
        write(ctx, bytes([CMD_SHUTTER_ON]))
        assert state.is_recording is True

    def test_shutter_on_ack(self, ctx):
        """CMD 0x10 → ACK [0x10, 0x00]."""
        _, _, _, notifs = ctx
        write(ctx, bytes([CMD_SHUTTER_ON]))
        assert notifs[-1][1] == bytes([0x10, 0x00])

    def test_shutter_off_clears_recording(self, ctx):
        """CMD 0x11 → state.is_recording == False."""
        _, state, _, _ = ctx
        write(ctx, bytes([CMD_SHUTTER_ON]))
        write(ctx, bytes([CMD_SHUTTER_OFF]))
        assert state.is_recording is False

    def test_shutter_off_ack(self, ctx):
        """CMD 0x11 → ACK [0x11, 0x00]."""
        _, _, _, notifs = ctx
        write(ctx, bytes([CMD_SHUTTER_ON]))
        write(ctx, bytes([CMD_SHUTTER_OFF]))
        assert notifs[-1][1] == bytes([0x11, 0x00])

    def test_shutter_on_sets_recording_start_time(self, ctx):
        """CMD 0x10 → recording_start_time > 0."""
        _, state, _, _ = ctx
        write(ctx, bytes([CMD_SHUTTER_ON]))
        assert state.recording_start_time > 0

    def test_shutter_off_resets_recording_start_time(self, ctx):
        """CMD 0x11 → recording_start_time == 0."""
        _, state, _, _ = ctx
        write(ctx, bytes([CMD_SHUTTER_ON]))
        write(ctx, bytes([CMD_SHUTTER_OFF]))
        assert state.recording_start_time == 0.0


# ---------------------------------------------------------------------------
# Edge cases
# ---------------------------------------------------------------------------

class TestEdgeCases:

    def test_unknown_command_nack(self, ctx):
        """Commande inconnue → NACK [cmd, 0xFF]."""
        _, _, _, notifs = ctx
        write(ctx, bytes([0xAB]))
        assert len(notifs) == 1
        assert notifs[0][1] == bytes([0xAB, 0xFF])

    def test_empty_data_no_notification(self, ctx):
        """Write vide → aucune notification."""
        handler, _, _, notifs = ctx
        asyncio.run(handler.handle_write(ctrl_uuid(ctx), b""))
        assert len(notifs) == 0

    def test_wrong_uuid_no_notification(self, ctx):
        """Write sur une UUID inconnue → aucune notification."""
        handler, _, _, notifs = ctx
        asyncio.run(handler.handle_write("00001234-0000-1000-8000-00805f9b34fb", bytes([0x01])))
        assert len(notifs) == 0

    def test_get_characteristic_value_notify_returns_ssid(self, ctx):
        """Lecture de la char notify → SSID en UTF-8 (valeur GATT par défaut)."""
        handler, _, profile, _ = ctx
        uuid = profile.ble.characteristics["notify"]
        val = handler.get_characteristic_value(uuid)
        assert val == profile.wifi.ssid.encode("utf-8")

    def test_get_characteristic_value_unknown_returns_empty(self, ctx):
        """Lecture d'une UUID inconnue → b''."""
        handler, _, _, _ = ctx
        val = handler.get_characteristic_value("00001234-0000-1000-8000-00805f9b34fb")
        assert val == b""

    def test_no_sender_no_crash(self, ctx):
        """Sans notification sender, handle_write ne plante pas."""
        _, _, profile, _ = ctx
        state = CameraState(profile)
        handler = Insta360GattHandler(state, profile)
        # sender non initialisé → _send_notification is None
        uuid = profile.ble.characteristics["control"].lower()
        asyncio.run(handler.handle_write(uuid, bytes([CMD_REQUEST_WIFI_CREDS])))
        # pas d'exception = succès
