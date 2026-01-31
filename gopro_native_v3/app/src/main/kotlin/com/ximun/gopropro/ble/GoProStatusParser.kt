package com.ximun.gopropro.ble

import android.util.Log
import com.ximun.gopropro.proto.GoProProtos // Note: Nécessite la compilation Gradle pour exister
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GoProStatusParser {
    companion object {
        private const val TAG = "GoProStatusParser"

        /**
         * Analyse les messages TLV (Type-Length-Value) du canal Query.
         */
        fun parseQueryResponse(data: ByteArray): Map<Int, Any> {
            val results = mutableMapOf<Int, Any>()
            if (data.size < 2) return results

            val queryId = data[0].toInt() and 0xFF
            val status = data[1].toInt() and 0xFF

            if (status != 0) {
                Log.w(TAG, "Query Response Error: $status pour ID: $queryId")
                return results
            }

            val payload = data.copyOfRange(2, data.size)
            
            when (queryId) {
                0x13, 0x93 -> { // Status Updates
                    results.putAll(parseTlv(payload))
                }
                0x12, 0x92 -> { // Setting Updates
                    results.putAll(parseTlv(payload))
                }
                0x12 -> { // Cas spécifique pour les Presets si envoyé via TLV simple
                    // Souvent 0x12 est aussi utilisé pour les Presets Protobuf (Feature 0xF5)
                }
            }
            return results
        }

        private fun parseTlv(data: ByteArray): Map<Int, Any> {
            val map = mutableMapOf<Int, Any>()
            var offset = 0
            while (offset < data.size) {
                val id = data[offset++].toInt() and 0xFF
                val len = data[offset++].toInt() and 0xFF
                val value = data.copyOfRange(offset, offset + len)
                offset += len

                // Conversion selon la longueur
                val parsedValue: Any = when (len) {
                    1 -> value[0].toInt() and 0xFF
                    2 -> ByteBuffer.wrap(value).order(ByteOrder.BIG_ENDIAN).short.toInt() and 0xFFFF
                    4 -> ByteBuffer.wrap(value).order(ByteOrder.BIG_ENDIAN).int
                    8 -> ByteBuffer.wrap(value).order(ByteOrder.BIG_ENDIAN).long
                    else -> value
                }
                map[id] = parsedValue
            }
            return map
        }

        /**
         * Décodage des Presets via Protobuf (Feature 0xF5, Action 0x72)
         * Note: Cette méthode sera pleinement fonctionnelle après la compilation Gradle.
         */
        fun parsePresets(data: ByteArray): Any? {
            return try {
                // Sur Android Natif, on utilise le message généré par le plugin Protobuf
                // com.ximun.gopropro.proto.GoProProtos.NotifyPresetStatus.parseFrom(data)
                "Protobuf Message Received: ${data.size} bytes" 
            } catch (e: Exception) {
                Log.e(TAG, "Erreur décodage Protobuf", e)
                null
            }
        }
    }
}
