package com.ximun.gopropro.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data

class GoProBleManager(context: Context) : BleManager(context) {
    private val TAG = "GoProBleManager"

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
        Log.println(priority, TAG, message)
    }

    private inner class GoProGattCallback : BleManagerGattCallback() {
        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(GoProConstants.GOPRO_SERVICE_UUID)
            if (service != null) {
                commandChar = service.getCharacteristic(GoProConstants.COMMAND_CHAR_UUID)
                commandRspChar = service.getCharacteristic(GoProConstants.COMMAND_RSP_CHAR_UUID)
                settingsChar = service.getCharacteristic(GoProConstants.SETTINGS_CHAR_UUID)
                settingsRspChar = service.getCharacteristic(GoProConstants.SETTINGS_RSP_CHAR_UUID)
                queryChar = service.getCharacteristic(GoProConstants.QUERY_CHAR_UUID)
                queryRspChar = service.getCharacteristic(GoProConstants.QUERY_RSP_CHAR_UUID)
            }
            return commandChar != null && commandRspChar != null
        }

        override fun initialize() {
            // Activer les notifications
            setNotificationCallback(commandRspChar).with { _, data ->
                Log.d(TAG, "Notification brute b5f90073 (CMD RSP): ${data.value?.joinToString("-") { String.format("%02X", it) }}")
                commandDefragmenter.processPacket(data.value ?: byteArrayOf())?.let {
                    Log.d(TAG, "Message défragmenté (CMD RSP): ${it.joinToString("-") { String.format("%02X", it) }}")
                    callback?.onMessageReceived(GoProConstants.COMMAND_RSP_CHAR_UUID.toString(), it)
                }
            }
            enableNotifications(commandRspChar).enqueue()

            setNotificationCallback(settingsRspChar).with { _, data ->
                Log.d(TAG, "Notification brute b5f90075 (SET RSP): ${data.value?.joinToString("-") { String.format("%02X", it) }}")
                settingsDefragmenter.processPacket(data.value ?: byteArrayOf())?.let {
                    callback?.onMessageReceived(GoProConstants.SETTINGS_RSP_CHAR_UUID.toString(), it)
                }
            }
            enableNotifications(settingsRspChar).enqueue()

            setNotificationCallback(queryRspChar).with { _, data ->
                Log.d(TAG, "Notification brute b5f90077 (QRY RSP): ${data.value?.joinToString("-") { String.format("%02X", it) }}")
                queryDefragmenter.processPacket(data.value ?: byteArrayOf())?.let {
                    Log.d(TAG, "Message défragmenté (QRY RSP): ${it.joinToString("-") { String.format("%02X", it) }}")
                    callback?.onMessageReceived(GoProConstants.QUERY_RSP_CHAR_UUID.toString(), it)
                }
            }
            enableNotifications(queryRspChar).enqueue()
            
            Log.d(TAG, "GATT Manager Initialisé et Notifications activées")
        }

        override fun onDeviceDisconnected() {
            commandChar = null
            commandRspChar = null
            callback?.onConnectionStatusChanged(false)
        }

        override fun onDeviceReady() {
            // Nécessaire pour passer à l'écran de contrôle
            callback?.onConnectionStatusChanged(true)
        }

        override fun onServicesInvalidated() {
            // Nettoyage optionnel
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
            val hexString = packet.joinToString("-") { String.format("%02X", it) }
            Log.d(TAG, ">>> ENVOI BLE ($charUuid): $hexString")
            
            writeCharacteristic(characteristic, packet, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                .with { _, data -> Log.d(TAG, "Packet envoyé avec succès: ${data.value?.size} bytes") }
                .enqueue()
        }
    }
}
