package com.ximun.gopropro.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import no.nordicsemi.android.ble.BleManager
import java.util.Locale

class GoProBleManager(context: Context) : BleManager(context) {
    private val tag = "GoProBleManager"

    // Les caractéristiques
    private var commandChar: BluetoothGattCharacteristic? = null
    private var commandRspChar: BluetoothGattCharacteristic? = null
    private var settingsChar: BluetoothGattCharacteristic? = null
    private var settingsRspChar: BluetoothGattCharacteristic? = null
    private var queryChar: BluetoothGattCharacteristic? = null
    private var queryRspChar: BluetoothGattCharacteristic? = null

    // Les défragmenteurs (un par canal de réponse)
    private val commandDefragmenter = GoProPacketHandler.Companion.Defragmenter()
    private val settingsDefragmenter = GoProPacketHandler.Companion.Defragmenter()
    private val queryDefragmenter = GoProPacketHandler.Companion.Defragmenter()

    // Interface pour remonter les messages complets
    interface GoProBleCallback {
        fun onMessageReceived(charUuid: String, data: ByteArray)
        fun onConnectionStatusChanged(connected: Boolean)
    }
    
    var callback: GoProBleCallback? = null

    override fun getGattCallback(): BleManagerGattCallback = GoProGattCallback()

    override fun log(priority: Int, message: String) {
        Log.println(priority, tag, message)
    }

    private inner class GoProGattCallback : BleManagerGattCallback() {
        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            Log.d(tag, "🔍 Discovery: Services trouvés, vérification du profil GoPro...")
            val service = gatt.getService(GoProConstants.GOPRO_SERVICE_UUID)
            if (service != null) {
                commandChar = service.getCharacteristic(GoProConstants.COMMAND_CHAR_UUID)
                commandRspChar = service.getCharacteristic(GoProConstants.COMMAND_RSP_CHAR_UUID)
                settingsChar = service.getCharacteristic(GoProConstants.SETTINGS_CHAR_UUID)
                settingsRspChar = service.getCharacteristic(GoProConstants.SETTINGS_RSP_CHAR_UUID)
                queryChar = service.getCharacteristic(GoProConstants.QUERY_CHAR_UUID)
                queryRspChar = service.getCharacteristic(GoProConstants.QUERY_RSP_CHAR_UUID)
                
                Log.d(tag, "✅ Profil GoPro identifié: CMD=${commandChar!=null}, SET=${settingsChar!=null}, QRY=${queryChar!=null}")
            } else {
                Log.e(tag, "❌ Service GoPro (FEA6) non trouvé !")
            }
            return commandChar != null && commandRspChar != null
        }

        override fun initialize() {
            // Augmenter le MTU pour les GoPro HERO 10/11/12
            requestMtu(512).enqueue()

            // COMMAND RSP: d'abord enregistrer le callback, puis activer les notifications
            setNotificationCallback(commandRspChar).with { _, data ->
                if (data.value == null || data.value!!.isEmpty()) {
                    Log.w(tag, "⚠️ Paquet vide ignoré sur CMD RSP")
                    return@with
                }
                val hexData = data.value!!.joinToString("") { String.format(Locale.US, "%02X", it) }
                Log.d(tag, "<<< RX CMD RSP: $hexData")
                commandDefragmenter.processPacket(data.value!!)?.let {
                    callback?.onMessageReceived(GoProConstants.COMMAND_RSP_CHAR_UUID.toString(), it)
                }
            }
            enableNotifications(commandRspChar)
                .done { Log.d(tag, "✅ Notifications activées sur CMD RSP") }
                .fail { _, status -> Log.e(tag, "❌ Échec activation CMD RSP: $status") }
                .enqueue()

            // SETTINGS RSP
            setNotificationCallback(settingsRspChar).with { _, data ->
                if (data.value == null || data.value!!.isEmpty()) {
                    Log.w(tag, "⚠️ Paquet vide ignoré sur SET RSP")
                    return@with
                }
                val hexData = data.value!!.joinToString("") { String.format(Locale.US, "%02X", it) }
                Log.d(tag, "<<< RX SET RSP: $hexData")
                settingsDefragmenter.processPacket(data.value!!)?.let {
                    callback?.onMessageReceived(GoProConstants.SETTINGS_RSP_CHAR_UUID.toString(), it)
                }
            }
            enableNotifications(settingsRspChar)
                .done { Log.d(tag, "✅ Notifications activées sur SET RSP") }
                .fail { _, status -> Log.e(tag, "❌ Échec activation SET RSP: $status") }
                .enqueue()

            // QUERY RSP
            setNotificationCallback(queryRspChar).with { _, data ->
                if (data.value == null || data.value!!.isEmpty()) {
                    Log.w(tag, "⚠️ Paquet vide ignoré sur QRY RSP")
                    return@with
                }
                val hexData = data.value!!.joinToString("") { String.format(Locale.US, "%02X", it) }
                Log.d(tag, "<<< RX QRY RSP: $hexData")
                queryDefragmenter.processPacket(data.value!!)?.let {
                    callback?.onMessageReceived(GoProConstants.QUERY_RSP_CHAR_UUID.toString(), it)
                }
            }
            enableNotifications(queryRspChar)
                .done { Log.d(tag, "✅ Notifications activées sur QRY RSP") }
                .fail { _, status -> Log.e(tag, "❌ Échec activation QRY RSP: $status") }
                .enqueue()

            Log.d(tag, "GATT Manager Initialisé - File d'attente CCCD envoyée")
        }



        override fun onDeviceDisconnected() {
            // Nettoyage de toutes les caractéristiques
            commandChar = null
            commandRspChar = null
            settingsChar = null
            settingsRspChar = null
            queryChar = null
            queryRspChar = null
            // Reset des défragmenteurs pour éviter la corruption au prochain connect
            commandDefragmenter.reset()
            settingsDefragmenter.reset()
            queryDefragmenter.reset()
            callback?.onConnectionStatusChanged(false)
        }

        override fun onDeviceReady() {
            Log.d(tag, "🔌 Connection: status=CONNECTED, state=READY")
            // Nécessaire pour passer à l'écran de contrôle
            callback?.onConnectionStatusChanged(true)
        }

        @Deprecated("Deprecated in Nordic BLE")
        override fun onServicesInvalidated() {
            // Nettoyage optionnel
        }

        // Sonde de réception bas niveau (recommandée pour le debug)
        @Deprecated("Deprecated in Nordic BLE")
        override fun onCharacteristicNotified(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            // Nordic recommande d'utiliser les callbacks définis dans initialize() avec enableNotifications()
            // Cependant, on garde cette sonde pour le debug, avec l'annotation Deprecated.
            super.onCharacteristicNotified(gatt, characteristic)
        }
    }

    /**
     * Envoie une commande (fragmentée si nécessaire)
     */
    fun sendGoProCommand(charUuid: java.util.UUID, payload: ByteArray) {
        val characteristic = when(charUuid) {
            GoProConstants.COMMAND_CHAR_UUID -> commandChar
            GoProConstants.SETTINGS_CHAR_UUID -> settingsChar
            GoProConstants.QUERY_CHAR_UUID -> queryChar
            else -> null
        } ?: return

        val packets = GoProPacketHandler.buildBlePackets(payload)
        packets.forEach { packet ->
            val hexString = packet.joinToString("-") { String.format(Locale.US, "%02X", it) }
            Log.d(tag, ">>> ENVOI BLE ($charUuid): $hexString")
            
            writeCharacteristic(characteristic, packet, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                .with { _, data -> Log.d(tag, "✍️ Write Success: ${characteristic.uuid}, size=${data.value?.size} bytes") }
                .fail { _, status -> Log.e(tag, "❌ Write Fail: ${characteristic.uuid}, status=$status") }
                .enqueue()
        }
    }
}
