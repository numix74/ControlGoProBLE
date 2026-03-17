"""Tests for BLE packet fragmentation / defragmentation."""

import pytest
from ble.gopro.packet_codec import build_ble_packets, Defragmenter


def roundtrip(payload: bytes, mtu: int = 244) -> bytes:
    """Fragment then defragment a payload. Must return the original bytes."""
    packets = build_ble_packets(payload, mtu=mtu)
    defrag = Defragmenter()
    result = None
    for p in packets:
        result = defrag.process_packet(p)
    assert result is not None, "Defragmenter never completed"
    return result


class TestShortPayload:
    """Payloads < 32 bytes use 5-bit single-byte header."""

    def test_single_byte_payload(self):
        assert roundtrip(b"\x3C") == b"\x3C"

    def test_31_byte_payload(self):
        payload = bytes(range(31))
        assert roundtrip(payload) == payload

    def test_header_is_one_byte(self):
        packets = build_ble_packets(b"\x01\x02\x03")
        assert len(packets) == 1
        assert packets[0][0] & 0xE0 == 0x00  # type bits = 0b000


class TestLongPayload:
    """Payloads >= 32 bytes use 13-bit two-byte header."""

    def test_32_byte_payload(self):
        payload = bytes(range(32))
        assert roundtrip(payload) == payload

    def test_84_byte_payload(self):
        payload = bytes(range(84))
        assert roundtrip(payload) == payload

    def test_header_is_two_bytes(self):
        payload = bytes(range(50))
        packets = build_ble_packets(payload)
        # First byte: type bits [6:5] = 0b01 → 0x20
        assert packets[0][0] & 0x60 == 0x20

    def test_length_encoded_correctly(self):
        payload = bytes(range(84))  # length=84=0x54
        packets = build_ble_packets(payload)
        first = packets[0]
        length = ((first[0] & 0x1F) << 8) | first[1]
        assert length == 84


class TestFragmentation:
    """Multi-packet fragmentation with small MTU."""

    def test_small_mtu_produces_multiple_packets(self):
        payload = bytes(range(50))
        packets = build_ble_packets(payload, mtu=20)
        assert len(packets) > 1

    def test_continuation_flag(self):
        payload = bytes(range(50))
        packets = build_ble_packets(payload, mtu=20)
        for p in packets[1:]:
            assert p[0] & 0x80 == 0x80, "Continuation packet must have bit7 set"

    def test_roundtrip_small_mtu(self):
        payload = bytes(range(100))
        assert roundtrip(payload, mtu=20) == payload

    def test_exact_mtu_boundary(self):
        # Payload that exactly fills one packet with long header (mtu-2 bytes)
        payload = bytes(range(22))  # mtu=24: 2 header + 22 payload = 24
        result = roundtrip(payload, mtu=24)
        assert result == payload


class TestDefragmenterReset:
    """Defragmenter handles interleaved new messages."""

    def test_two_sequential_messages(self):
        defrag = Defragmenter()
        p1 = build_ble_packets(b"\x01\x02")
        p2 = build_ble_packets(b"\x03\x04")

        result1 = None
        for p in p1:
            result1 = defrag.process_packet(p)
        assert result1 == b"\x01\x02"

        result2 = None
        for p in p2:
            result2 = defrag.process_packet(p)
        assert result2 == b"\x03\x04"
