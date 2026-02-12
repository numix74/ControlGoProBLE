package com.ximun.gopropro.ble

import kotlin.math.min

class GoProPacketHandler {
    companion object {

        /**
         * Construit les paquets BLE (fragmentation) selon la spec OpenGoPro.
         * Supporte les headers 5-bit (court) et 13-bit (long) + continuation (0x80).
         */
        fun buildBlePackets(payload: ByteArray, mtu: Int = 244): List<ByteArray> {
            val packets = mutableListOf<ByteArray>()
            val totalLength = payload.size
            var bytesSent = 0
            var packetCounter = 0

            // Détermination du type de header
            val isLongHeader = totalLength >= 32
            val headerLength = if (isLongHeader) 2 else 1
            
            val firstPacketPayloadSize = min(totalLength, mtu - headerLength)
            val firstPacket = ByteArray(headerLength + firstPacketPayloadSize)

            if (isLongHeader) {
                // Header 13-bit (010LLLLL LLLLLLLL)
                firstPacket[0] = (0x40 or ((totalLength shr 8) and 0x1F)).toByte()
                firstPacket[1] = (totalLength and 0xFF).toByte()
            } else {
                // Header 5-bit (000LLLLL)
                firstPacket[0] = (totalLength and 0x1F).toByte()
            }

            payload.copyInto(firstPacket, destinationOffset = headerLength, startIndex = 0, endIndex = firstPacketPayloadSize)
            packets.add(firstPacket)
            bytesSent += firstPacketPayloadSize

            // --- Paquets de Continuation (0x80) ---
            while (bytesSent < totalLength) {
                val remainingBytes = totalLength - bytesSent
                val currentPayloadSize = min(remainingBytes, mtu - 1)
                val continuationPacket = ByteArray(1 + currentPayloadSize)
                
                // Header: 1000CCCC (Counter sur 4 bits)
                continuationPacket[0] = (0x80 or (packetCounter and 0x0F)).toByte()
                packetCounter = (packetCounter + 1) % 16
                
                payload.copyInto(continuationPacket, destinationOffset = 1, startIndex = bytesSent, endIndex = bytesSent + currentPayloadSize)
                packets.add(continuationPacket)
                bytesSent += currentPayloadSize
            }

            return packets
        }

        /**
         * Défragmenteur pour les messages entrants.
         */
        class Defragmenter {
            private var buffer = ByteArray(0)
            private var expectedLength = 0
            private var receivedLength = 0
            private var isAssembling = false

            fun reset() {
                buffer = ByteArray(0)
                expectedLength = 0
                receivedLength = 0
                isAssembling = false
            }

            fun processPacket(packet: ByteArray): ByteArray? {
                if (packet.isEmpty()) return null
                val firstByte = packet[0].toInt() and 0xFF
                val isContinuation = (firstByte and 0x80) != 0

                if (!isContinuation) {
                    // Nouveau message
                    isAssembling = true
                    val headerType = (firstByte and 0x60) shr 5
                    var headerLen = 1
                    
                    when (headerType) {
                        0 -> { // 5-bit
                            expectedLength = firstByte and 0x1F
                        }
                        1 -> { // 13-bit
                            expectedLength = ((firstByte and 0x1F) shl 8) or (packet[1].toInt() and 0xFF)
                            headerLen = 2
                        }
                        2 -> { // 16-bit
                            expectedLength = ((packet[1].toInt() and 0xFF) shl 8) or (packet[2].toInt() and 0xFF)
                            headerLen = 3
                        }
                    }
                    
                    buffer = packet.copyOfRange(headerLen, packet.size)
                    receivedLength = buffer.size
                } else {
                    // Suite du message
                    if (!isAssembling) return null
                    val payloadPart = packet.copyOfRange(1, packet.size)
                    buffer += payloadPart
                    receivedLength += payloadPart.size
                }

                return if (receivedLength >= expectedLength) {
                    isAssembling = false
                    buffer.copyOfRange(0, expectedLength)
                } else {
                    null
                }
            }
        }
    }
}
