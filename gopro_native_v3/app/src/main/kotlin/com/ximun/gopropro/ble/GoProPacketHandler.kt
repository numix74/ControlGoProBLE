package com.ximun.gopropro.ble

import android.util.Log

class GoProPacketHandler {
    companion object {
        private const val TAG = "GoProPacketHandler"

        /**
         * Construit les paquets BLE (fragmentation) selon la spec OpenGoPro.
         * Supporte les headers 5-bit (court) et 13-bit (long) + continuation (0x80).
         */
        fun buildBlePackets(payload: ByteArray, mtu: Int = 20): List<ByteArray> {
            val packets = mutableListOf<ByteArray>()
            val totalLength = payload.size
            var bytesSent = 0
            var packetCounter = 0

            // Spec user: Si > 20 octets, toujours header Extended 13-bit
            val useExtended = totalLength > 20

            // --- Premier Paquet ---
            val headerLength = if (useExtended) 2 else 1
            val firstPacketPayloadSize = Math.min(totalLength, mtu - headerLength)
            val firstPacket = ByteArray(headerLength + firstPacketPayloadSize)

            if (!useExtended) {
                // Header 5-bit (000LLLLL)
                firstPacket[0] = (totalLength and 0x1F).toByte()
            } else {
                // Header 13-bit (010LLLLL LLLLLLLL)
                firstPacket[0] = (0x40 or ((totalLength shr 8) and 0x1F)).toByte()
                firstPacket[1] = (totalLength and 0xFF).toByte()
            }

            System.arraycopy(payload, 0, firstPacket, headerLength, firstPacketPayloadSize)
            packets.add(firstPacket)
            bytesSent += firstPacketPayloadSize

            // --- Paquets de Continuation (0x80) ---
            while (bytesSent < totalLength) {
                val remainingBytes = totalLength - bytesSent
                val currentPayloadSize = Math.min(remainingBytes, mtu - 1)
                val continuationPacket = ByteArray(1 + currentPayloadSize)
                
                // Header: 1000CCCC (Counter sur 4 bits)
                continuationPacket[0] = (0x80 or (packetCounter and 0x0F)).toByte()
                packetCounter = (packetCounter + 1) % 16
                
                System.arraycopy(payload, bytesSent, continuationPacket, 1, currentPayloadSize)
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

            fun processPacket(packet: ByteArray): ByteArray? {
                if (packet.isEmpty()) return null
                val firstByte = packet[0].toInt() and 0xFF
                val isContinuation = (firstByte and 0x80) != 0

                if (!isContinuation) {
                    // Nouveau message
                    isAssembling = true
                    val headerType = (firstByte and 0x60) shr 5
                    var headerLen = 1
                    
                    if (headerType == 0) { // 5-bit
                        expectedLength = firstByte and 0x1F
                    } else if (headerType == 1) { // 13-bit
                        expectedLength = ((firstByte and 0x1F) shl 8) or (packet[1].toInt() and 0xFF)
                        headerLen = 2
                    }
                    
                    buffer = packet.copyOfRange(headerLen, packet.size)
                    receivedLength = buffer.size
                } else {
                    // Suite du message
                    if (!isAssembling) return null
                    val payloadPart = packet.copyOfRange(1, packet.size)
                    val newBuffer = ByteArray(buffer.size + payloadPart.size)
                    System.arraycopy(buffer, 0, newBuffer, 0, buffer.size)
                    System.arraycopy(payloadPart, 0, newBuffer, buffer.size, payloadPart.size)
                    buffer = newBuffer
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
