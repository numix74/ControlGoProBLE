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
            Log.d(TAG, "🔍 Parsing Query 0x${queryId.toString(16).uppercase()} (taille: ${data.size})")

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
                // Structure: ID(3C) + Status(00) + [TLV...]
                // Les valeurs sont des Strings
                var i = 2
                 try {
                    while (i + 1 < data.size) {
                        val id = data[i++].toInt() and 0xFF
                        val length = data[i++].toInt() and 0xFF
                        
                        if (i + length > data.size) break
                        
                        val bytes = data.copyOfRange(i, i + length)
                        val text = bytes.toString(Charsets.UTF_8)
                        result[id] = text // ID: 1=Name, 2=MAC, 3=Serial, 6=Firmware
                        
                        Log.d(TAG, "ℹ️ Hardware Info - ID $id: $text")
                        i += length
                    }
                    return result // Important: ne pas continuer le parsing standard
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Erreur parsing Hardware Info: ${e.message}")
                }
            }

            // Offset : 2 pour réponse synchrone (ID + Status), 1 pour notification asynchrone
            var index = if (queryId < 0x80) 2 else 1

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

                    // Log de diagnostic demandé
                    Log.d(TAG, "Parsing TLV: ID=$id, length=$length, index=$index, size=${data.size}")

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
                            // Pour les capacités, on cumule les valeurs possibles par ID
                            @Suppress("UNCHECKED_CAST")
                            val existing = result[id] as? MutableList<Int> ?: mutableListOf()
                            
                            // ✅ FIX: Décoder la valeur selon sa longueur complète
                            val value = when (length) {
                                1 -> data[index].toInt() and 0xFF
                                2 -> ((data[index].toInt() and 0xFF) shl 8) or 
                                     (data[index + 1].toInt() and 0xFF)
                                4 -> {
                                    var v = 0
                                    for (i in 0 until 4) {
                                        v = (v shl 8) or (data[index + i].toInt() and 0xFF)
                                    }
                                    v
                                }
                                else -> {
                                    Log.w(TAG, "⚠️ Longueur inhabituelle $length pour capacité ID $id")
                                    data[index].toInt() and 0xFF
                                }
                            }
                            
                            existing.add(value)
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
                Log.e(TAG, "Data hex: ${data.joinToString("-") { String.format("%02X", it) }}")
            }

            return result
        }
    }
}
