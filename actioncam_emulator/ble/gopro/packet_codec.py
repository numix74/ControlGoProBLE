"""
GoPro BLE packet fragmentation/defragmentation.
Direct port from GoProPacketHandler.kt.
"""


def build_ble_packets(payload: bytes, mtu: int = 244) -> list[bytes]:
    """
    Fragment a payload into BLE packets per GoPro OpenAPI spec.
    Short header (5-bit) for payloads < 32 bytes.
    Long header (13-bit) for payloads >= 32 bytes.
    Continuation packets use 0x80 | counter.
    """
    packets = []
    total_length = len(payload)
    bytes_sent = 0
    packet_counter = 0

    is_long_header = total_length >= 32
    header_length = 2 if is_long_header else 1

    first_payload_size = min(total_length, mtu - header_length)

    if is_long_header:
        # 13-bit header: 001LLLLL LLLLLLLL  (type bits [6:5] = 0b01)
        header = bytes([
            0x20 | ((total_length >> 8) & 0x1F),
            total_length & 0xFF,
        ])
    else:
        # 5-bit header: 000LLLLL
        header = bytes([total_length & 0x1F])

    first_packet = header + payload[:first_payload_size]
    packets.append(first_packet)
    bytes_sent += first_payload_size

    # Continuation packets
    while bytes_sent < total_length:
        remaining = total_length - bytes_sent
        chunk_size = min(remaining, mtu - 1)

        cont_header = bytes([0x80 | (packet_counter & 0x0F)])
        packet_counter = (packet_counter + 1) % 16

        cont_packet = cont_header + payload[bytes_sent:bytes_sent + chunk_size]
        packets.append(cont_packet)
        bytes_sent += chunk_size

    return packets


class Defragmenter:
    """Reassembles fragmented BLE packets into complete messages."""

    def __init__(self):
        self.reset()

    def reset(self):
        self._buffer = bytearray()
        self._expected_length = 0
        self._received_length = 0
        self._is_assembling = False

    def process_packet(self, packet: bytes) -> bytes | None:
        """
        Process an incoming BLE packet.
        Returns the complete payload when all fragments are received, or None.
        """
        if not packet:
            return None

        first_byte = packet[0] & 0xFF
        is_continuation = (first_byte & 0x80) != 0

        if not is_continuation:
            # New message — determine header type
            self._is_assembling = True
            header_type = (first_byte & 0x60) >> 5
            header_len = 1

            if header_type == 0:
                # 5-bit length
                self._expected_length = first_byte & 0x1F
            elif header_type == 1:
                # 13-bit length
                self._expected_length = ((first_byte & 0x1F) << 8) | (packet[1] & 0xFF)
                header_len = 2
            elif header_type == 2:
                # 16-bit length
                self._expected_length = ((packet[1] & 0xFF) << 8) | (packet[2] & 0xFF)
                header_len = 3

            self._buffer = bytearray(packet[header_len:])
            self._received_length = len(self._buffer)
        else:
            # Continuation packet
            if not self._is_assembling:
                return None
            payload_part = packet[1:]
            self._buffer.extend(payload_part)
            self._received_length += len(payload_part)

        if self._received_length >= self._expected_length:
            self._is_assembling = False
            result = bytes(self._buffer[:self._expected_length])
            self.reset()
            return result

        return None
