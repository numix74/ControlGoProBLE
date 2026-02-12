package com.ximun.gopropro.ble

import android.util.Log
import com.ximun.gopropro.proto.GoProProtos

class GoProStatusParser {
    companion object {
    private const val TAG = "GoProStatusParser"

        fun parseQueryResponse(data: ByteArray): Map<Int, Any> {
            val result = mutableMapOf<Int, Any>()
            if (data.size < 2) return result

            val queryId = data[0].toInt() and 0xFF
            val statusByte = if (data.size > 1) data[1].toInt() and 0xFF else 0
            // Vérification du status byte : 0 = succès, autre = erreur
            // Exceptions : 0xF5 (Protobuf, byte 1 = action ID) et 0x3C (HW Info)
            if (statusByte != 0 && queryId != 0xF5 && queryId != 0x3C) {
                Log.w(TAG, "⚠️ Query 0x${queryId.toString(16)} rejetée (status=$statusByte)")
                return result
            }

            // Cas spécial pour les Query Response sur 0xF5 (Protobuf)
            // L'ID 0xF5 signifie souvent un retour Protobuf pour les presets/modes
            if (queryId == 0xF5) {
                // Structure: ID(F5) + Action/Status + Protobuf
                // Généralement: F5 [Action] [Protobuf...]
                if (data.size > 2) {
                    val actionId = data[1].toInt() and 0xFF
                    val protoData = data.copyOfRange(2, data.size)
                    Log.d(TAG, "📦 Détection Protobuf 0xF5 (Action 0x${actionId.toString(16)})")
                    
                    try {
                        val message = GoProProtos.NotifyPresetStatus.parseFrom(protoData)
                        // On retourne le message protobuf complet avec un ID spécial (ex: 0xF500)
                        result[0xF500] = message
                        return result
                    } catch (e: Exception) {
                         Log.e(TAG, "❌ Erreur décodage Protobuf: ${e.message}")
                    }
                }
            }

            if (queryId == 0x3C) { // CMD_GET_HARDWARE_INFO
                // Format: [0x3C] [Status] puis champs séquentiels [LEN] [VALUE]
                // Champ 1=ModelNumber(bytes), 2=ModelName, 3=BoardType,
                // 4=Firmware, 5=Serial, 6=AP_SSID, 7=AP_MAC
                var i = 2
                var fieldIndex = 0
                try {
                    while (i < data.size) {
                        val length = data[i++].toInt() and 0xFF
                        if (length == 0) {
                            fieldIndex++
                            continue
                        }
                        if (i + length > data.size) break
                        fieldIndex++
                        val bytes = data.copyOfRange(i, i + length)
                        val text = bytes.toString(Charsets.UTF_8)
                        result[fieldIndex] = text
                        Log.d(TAG, "ℹ️ HW Info - Field $fieldIndex (len=$length): $text")
                        i += length
                    }
                    return result
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Erreur parsing Hardware Info: ${e.message}")
                }
            }

            // Offset : toujours 2 (ID + Status byte) pour les réponses Query
            // Les async (0x92, 0x93, 0xA2) incluent aussi un byte status après le queryId
            var index = 2

            // ⚠️ Protection Spécifique 0x93 (Similaire JS gopro_ble.js)
            // Si c'est un Status Async (0x93), on doit être très strict pour éviter les paquets corrompus (ex: len=54 dans payload de 12)
            if (queryId == 0x93) {
                // On pré-valide ou on parse avec politique "Tout ou Rien"
                val tempResult = mutableMapOf<Int, Any>()
                var tempIndex = index
                while (tempIndex + 1 < data.size) {
                    val id = data[tempIndex++].toInt() and 0xFF
                    val length = data[tempIndex++].toInt() and 0xFF
                    
                    if (length == 0) continue

                    if (tempIndex + length > data.size) {
                        Log.w(TAG, "⚠️ 0x93 Mal formé (ID=$id, Len=$length, Size=${data.size}) -> Paquet ignoré.")
                        return emptyMap() // On jette tout le paquet corrompu
                    }
                    
                    val value = if (length == 1) {
                         data[tempIndex].toInt() and 0xFF
                    } else {
                         data.copyOfRange(tempIndex, tempIndex + length)
                    }
                    tempResult[id] = value
                    tempIndex += length
                }
                return tempResult
            }

            try {
                // On boucle tant qu'il reste au moins un ID (1 byte) et une Longueur (1 byte)
                while (index + 1 < data.size) {
                    val id = data[index++].toInt() and 0xFF
                    val length = data[index++].toInt() and 0xFF

                    // Gestion cas length == 0
                    if (length == 0) {
                         // Pas de valeur, on passe au suivant si possible
                         // Mais le curseur index est déjà après length byte.
                         // Donc on ne fait rien de spécial, on va juste boucler.
                         continue
                    }

                    // Vérification critique des limites du buffer pour la valeur
                    if (index + length > data.size) {
                        Log.e(TAG, "❌ Buffer underrun pour ID $id : besoin de $length bytes, mais il n'en reste que ${data.size - index}")
                        break // On sauve ce qu'on a déjà parsé
                    }

                    when (queryId) {
                        0x32, 0x62, 0xA2 -> { // Canal des Capacités
                            // Format: [SettingID][TotalLen][ValBytes...]
                            // Chaque valeur occupe valueSize bytes (1, 2, ou 4)
                            // La taille de chaque valeur dépend du setting
                            @Suppress("UNCHECKED_CAST")
                            val existing = (result[id] as? MutableList<Int>) ?: mutableListOf()
                            val valueSize = getCapabilityValueSize(id, length)
                            var offset = 0
                            while (offset + valueSize <= length) {
                                var v = 0
                                for (b in 0 until valueSize) {
                                    v = (v shl 8) or (data[index + offset + b].toInt() and 0xFF)
                                }
                                existing.add(v)
                                offset += valueSize
                            }
                            result[id] = existing
                        }

                        else -> { // Canal Status et Settings (format standard)
                            val value = if (length == 1) {
                                data[index].toInt() and 0xFF
                            } else {
                                data.copyOfRange(index, index + length)
                            }
                            result[id] = value
                        }
                    }

                    index += length
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Crash durant le parsing à l'index $index: ${e.message}")
                Log.e(TAG, "Data hex: ${data.joinToString("-") { String.format(java.util.Locale.US, "%02X", it) }}")
            }

            return result
        }

        /**
         * Détermine la taille (en bytes) de chaque valeur de capability pour un setting donné.
         *
         * La plupart des settings utilisent 1 byte par valeur (0..255).
         * Certains settings (Photo Timelapse Rate, Night Lapse Rate, Scheduled Capture)
         * utilisent 4 bytes par valeur en big-endian (doc OpenGoPro: "64-bit unsigned",
         * mais l'analyse des données réelles montre un encodage 4 bytes dans les capabilities).
         *
         * Preuve empirique pour setting 32 (Night Lapse Rate):
         *   - totalLength = 48 bytes
         *   - 48 / 4 = 12 valeurs → correspond exactement aux 12 options connues
         *     (4, 5, 10, 15, 20, 30, 100, 120, 300, 1800, 3600, 3601)
         *   - Avec 2 bytes: 24 valeurs dont la moitié sont 0 (zéros alternés confirmés)
         *   - Avec 8 bytes: 6 valeurs seulement (insuffisant)
         *
         * On utilise le totalLength pour valider : si length n'est pas divisible par
         * valueSize, on fallback progressivement (4→2→1).
         */
        private fun getCapabilityValueSize(settingId: Int, totalLength: Int): Int {
            // Settings connus pour avoir des valeurs encodées sur 4 bytes dans les capabilities
            // (valeurs > 255, ex: Night Lapse Rate peut aller jusqu'à 3601)
            val fourByteSettings = setOf(
                30,  // Photo Timelapse Rate (valeurs: 1..3601)
                32,  // Night Lapse Rate (valeurs: 4..3601)
                64,  // Scheduled Capture
                168  // Scheduled Capture (alias)
            )

            // Settings connus pour avoir des valeurs sur 2 bytes dans les capabilities
            val twoByteSettings = setOf(
                62,  // Setup Language
                84,  // Date/Time related
                85,  // Date/Time related
                115, // Camera Control
                118  // Exposure/ISO
            )

            val candidateSize = when (settingId) {
                in fourByteSettings -> 4
                in twoByteSettings -> 2
                else -> 1
            }

            // Validation : le totalLength doit être divisible par la taille candidate.
            // Sinon on tente la taille inférieure, puis fallback à 1.
            return when {
                totalLength % candidateSize == 0 -> candidateSize
                candidateSize == 4 && totalLength % 2 == 0 -> 2
                else -> 1
            }
        }
    }
}
