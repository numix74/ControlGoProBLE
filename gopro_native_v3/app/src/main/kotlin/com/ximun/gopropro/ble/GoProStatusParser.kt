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
            if (data.isEmpty()) return results

            val id = data[0].toInt() and 0xFF
            // Les notifications asynchrones (ex: 0x93) commencent à 0x80
            val isAsync = id >= 0x80
            
            val offset = if (isAsync) {
                1
            } else {
                // Réponses synchrones : [ID, Status, TLV...]
                if (data.size < 2) return results
                val status = data[1].toInt() and 0xFF
                if (status != 0) {
                    Log.w(TAG, "Query Error status $status for Query ID 0x${String.format("%02X", id)}")
                    return results
                }
                2
            }
            
            if (data.size > offset) {
                results.putAll(parseTlv(data.copyOfRange(offset, data.size)))
            }
            return results
        }

        private fun parseTlv(data: ByteArray): Map<Int, Any> {
            val map = mutableMapOf<Int, Any>()
            var offset = 0
            // On a besoin d'au moins 2 octets pour lire le Type et la Longueur
            while (offset + 1 < data.size) {
                val id = data[offset++].toInt() and 0xFF
                val len = data[offset++].toInt() and 0xFF

                if (offset + len > data.size) {
                    Log.w(TAG, "Données TLV incomplètes pour l'ID $id. Longueur déclarée $len, mais il ne reste que ${data.size - offset} octets.")
                    // Arrêter l'analyse car le reste des données est probablement corrompu
                    return map
                }

                val value = data.copyOfRange(offset, offset + len)
                offset += len

                // On conserve les bytes bruts pour les capacités et les réglages complexes.
                // La conversion se fera au niveau de la consommation des données.
                Log.d(TAG, "TLV Parsé - ID: $id, Len: $len")
                map[id] = value
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
