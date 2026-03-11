package com.ximun.gopropro.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import com.ximun.gopropro.GoProSettingsMappings
import com.ximun.gopropro.gps.GpsTracker
import com.ximun.gopropro.proto.GoProProtos
import com.ximun.gopropro.viewmodel.GoProViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Centralise toute la logique BLE : scan, connexion, parsing des réponses,
 * commandes GoPro, keep-alive et subscriptions.
 */
class GoProConnectionManager(
    private val context: Context,
    private val viewModel: GoProViewModel,
    private val lifecycleScope: CoroutineScope,
    private val gpsTracker: GpsTracker? = null
) {
    companion object {
        private const val TAG = "GoProConnectionManager"
        private const val MAX_RECONNECT_ATTEMPTS = 3
        private const val PREFS_NAME = "gopro_prefs"
        private const val KEY_LAST_MAC = "last_device_mac"
        private const val KEY_PREFERRED_APD = "pref_apd"

        /**
         * BleManager conservé entre deux recréations d'Activity (changement de langue, rotation…).
         * Permet de ne pas fermer la connexion GATT lors d'un recreate().
         */
        @Volatile
        internal var retainedBleManager: GoProBleManager? = null

        /** Mapping setting 59 value → durée en secondes (-1 = jamais) */
        private val AUTO_OFF_DURATIONS = mapOf(
            0 to -1,   // Jamais
            11 to 8,   // 8 sec
            12 to 30,  // 30 sec
            1 to 60,   // 1 min
            4 to 300,  // 5 min
            6 to 900,  // 15 min
            7 to 1800  // 30 min
        )
    }

    private lateinit var bleManager: GoProBleManager
    private var keepAliveJob: Job? = null
    private var activityHeartbeatJob: Job? = null
    private var reconnectJob: Job? = null
    private var isDestroyed = false
    @Volatile private var isAppForeground = true
    private var wasConnectedBefore = false
    private var lastConnectedTimestamp = 0L
    private var wasRecording = false
    @Volatile private var lastCommandSentAt: Long = 0L
    @Volatile private var lastKeepAliveSentAt: Long = 0L
    @Volatile private var savedAutoOffValue: Int? = null
    private val bleScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val bluetoothManager by lazy { context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager }
    private val bluetoothAdapter: BluetoothAdapter? by lazy { bluetoothManager.adapter }
    private val bleScanner by lazy { bluetoothAdapter?.bluetoothLeScanner }

    val isBluetoothEnabled: Boolean get() = bluetoothAdapter?.isEnabled == true

    // ── Initialisation ───────────────────────────────────────────────

    fun initialize() {
        bleScope.launch {
            withContext(Dispatchers.Main) {
                val callback = object : GoProBleManager.GoProBleCallback {
                    override fun onMessageReceived(charUuid: String, data: ByteArray) {
                        lifecycleScope.launch(Dispatchers.Default) {
                            processBleMessage(charUuid, data)
                        }
                    }

                    override fun onConnectionStatusChanged(connected: Boolean, reason: Int) {
                        viewModel.updateConnection(connected)
                        if (connected) {
                            wasConnectedBefore = true
                            lastConnectedTimestamp = System.currentTimeMillis()
                            reconnectJob?.cancel()
                            viewModel.resetWaypointCount()
                            lifecycleScope.launch(Dispatchers.IO) {
                                gpsTracker?.startSession(System.currentTimeMillis())
                                delay(500)
                                startKeepAlive()
                                delay(200)
                                performInitialPolling()
                            }
                        } else {
                            val now = System.currentTimeMillis()
                            val sinceLastKA = now - lastKeepAliveSentAt
                            val sinceConnected = now - lastConnectedTimestamp
                            Log.w(TAG, "DÉCONNEXION — reason=0x${reason.toString(16).uppercase()} | " +
                                "dernier KA il y a ${sinceLastKA}ms | " +
                                "connecté depuis ${sinceConnected}ms (${sinceConnected/1000}s)")
                            keepAliveJob?.cancel()
                            activityHeartbeatJob?.cancel()
                            activityHeartbeatJob = null
                            savedAutoOffValue = null
                            lifecycleScope.launch(Dispatchers.IO) { gpsTracker?.endSession() }
                            wasRecording = false
                            // 0x13 (PEER USER) = caméra a fermé la connexion délibérément
                            // (extinction manuelle ou auto-off) → pas de reconnexion
                            val isPeerInitiated = reason == 0x13
                            if (wasConnectedBefore && !isDestroyed && !isPeerInitiated) {
                                attemptReconnect()
                            } else if (isPeerInitiated) {
                                Log.d(TAG, "Déconnexion initiée par la caméra (0x13), pas de reconnexion")
                            }
                        }
                    }
                }

                // Réutiliser le bleManager existant si l'Activity a été recréée (changement de langue, rotation…)
                val retained = retainedBleManager
                if (retained != null && viewModel.uiState.value.isConnected) {
                    Log.d(TAG, "Réutilisation du bleManager retenu (recréation Activity)")
                    bleManager = retained
                    retainedBleManager = null
                    bleManager.callback = callback
                    wasConnectedBefore = true
                    viewModel.resetWaypointCount()
                    // Relancer le keep-alive et le heartbeat APD sur le nouveau lifecycleScope
                    startKeepAlive()
                    if (isAppForeground) disableAutoOff()
                    // Relancer la session GPS (la précédente a été fermée dans prepareForRecreation)
                    lifecycleScope.launch(Dispatchers.IO) {
                        gpsTracker?.startSession(System.currentTimeMillis())
                    }
                } else {
                    retainedBleManager = null
                    bleManager = GoProBleManager(context.applicationContext)
                    bleManager.callback = callback
                }
                viewModel.setBleReady(true)
            }
        }
    }

    /**
     * À appeler depuis onDestroy() quand isChangingConfigurations = true.
     * Conserve la connexion BLE active en sauvegardant le bleManager dans le companion object,
     * sans fermer le GATT.
     */
    fun prepareForRecreation() {
        Log.d(TAG, "prepareForRecreation: conservation du bleManager pour la nouvelle Activity")
        keepAliveJob?.cancel()
        activityHeartbeatJob?.cancel()
        activityHeartbeatJob = null
        reconnectJob?.cancel()
        if (::bleManager.isInitialized) {
            retainedBleManager = bleManager
        }
        // Fermer proprement le fichier GPX avant la recréation
        gpsTracker?.endSession()
    }

    fun destroy() {
        isDestroyed = true
        keepAliveJob?.cancel()
        activityHeartbeatJob?.cancel()
        activityHeartbeatJob = null
        reconnectJob?.cancel()
        retainedBleManager = null
        gpsTracker?.endSession()
        if (::bleManager.isInitialized) {
            bleManager.disconnect().enqueue()
            bleManager.close()
        }
    }

    // ── Scan & Connexion ─────────────────────────────────────────────

    fun checkPermissionsAndScan(
        activity: ComponentActivity,
        permissionLauncher: ActivityResultLauncher<Array<String>>
    ) {
        val permissions = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            // WRITE_EXTERNAL_STORAGE nécessaire pour écrire dans Documents/ sur API 26-28
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
        if (permissions.all { ActivityCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED }) {
            startScan()
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        val filter = ScanFilter.Builder().setServiceUuid(ParcelUuid(GoProConstants.GOPRO_SERVICE_UUID)).build()
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        bleScanner?.startScan(listOf(filter), settings, scanCallback)
    }

    @SuppressLint("MissingPermission")
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            bleScanner?.stopScan(this)
            // Sauvegarder l'adresse MAC pour reconnexion directe
            prefs.edit().putString(KEY_LAST_MAC, result.device.address).apply()
            Log.d(TAG, "MAC sauvegardée: ${result.device.address}")
            bleManager.connect(result.device).retry(3, 100).useAutoConnect(false).enqueue()
        }
    }

    // ── Reconnexion automatique ──────────────────────────────────────

    /**
     * Retourne la durée d'extinction auto en secondes, ou -1 si "Jamais".
     */
    private fun getAutoPowerOffSeconds(): Int {
        val settingValue = viewModel.uiState.value.settings[GoProConstants.SETTING_ID_AUTO_POWER_DOWN]
        return AUTO_OFF_DURATIONS[settingValue] ?: -1
    }

    private fun attemptReconnect() {
        reconnectJob?.cancel()
        reconnectJob = lifecycleScope.launch(Dispatchers.IO) {
            val savedMac = prefs.getString(KEY_LAST_MAC, null)
            if (savedMac == null) {
                Log.d(TAG, "Pas de MAC sauvegardée, reconnexion impossible")
                return@launch
            }

            // Vérifier si c'est une extinction auto ou une perte de signal
            val autoOffSec = getAutoPowerOffSeconds()
            val elapsed = (System.currentTimeMillis() - lastConnectedTimestamp) / 1000
            if (autoOffSec > 0 && elapsed >= autoOffSec) {
                Log.d(TAG, "Extinction auto probable (écoulé=${elapsed}s >= autoOff=${autoOffSec}s), pas de reconnexion")
                wasConnectedBefore = false
                return@launch
            }

            Log.d(TAG, "Perte de signal probable (écoulé=${elapsed}s, autoOff=${autoOffSec}s), tentative de reconnexion vers $savedMac")

            for (attempt in 1..MAX_RECONNECT_ATTEMPTS) {
                if (isDestroyed || viewModel.uiState.value.isConnected) break

                val delayMs = (1L shl attempt) * 1000L // 2s, 4s, 8s
                Log.d(TAG, "Reconnexion tentative $attempt/$MAX_RECONNECT_ATTEMPTS (attente ${delayMs}ms)")
                delay(delayMs)

                if (isDestroyed || viewModel.uiState.value.isConnected) break

                try {
                    reconnectToDevice(savedMac)
                    delay(5000)
                    if (viewModel.uiState.value.isConnected) {
                        Log.d(TAG, "Reconnexion réussie (tentative $attempt)")
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Reconnexion tentative $attempt échouée: ${e.message}")
                }
            }

            if (!viewModel.uiState.value.isConnected) {
                Log.w(TAG, "Reconnexion échouée après $MAX_RECONNECT_ATTEMPTS tentatives")
                wasConnectedBefore = false
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun reconnectToDevice(macAddress: String) {
        val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(macAddress)
        if (device != null) {
            bleManager.connect(device).retry(1, 200).useAutoConnect(false).enqueue()
        }
    }

    /**
     * Reconnexion manuelle déclenchée depuis la bulle (tap long en état déconnecté).
     */
    @SuppressLint("MissingPermission")
    fun reconnectManually() {
        val savedMac = prefs.getString(KEY_LAST_MAC, null)
        if (savedMac == null) {
            Log.d(TAG, "Pas de MAC sauvegardée, reconnexion manuelle impossible")
            return
        }
        Log.d(TAG, "Reconnexion manuelle vers $savedMac")
        wasConnectedBefore = true
        reconnectToDevice(savedMac)
    }

    // ── Commandes de haut niveau ─────────────────────────────────────

    /** Envoie une commande BLE et met à jour le timestamp (évite un keep-alive inutile). */
    private fun bleCommand(charUuid: java.util.UUID, payload: ByteArray) {
        lastCommandSentAt = System.currentTimeMillis()
        bleManager.sendGoProCommand(charUuid, payload)
    }

    fun sendCommand(charUuid: java.util.UUID, payload: ByteArray) {
        bleCommand(charUuid, payload)
    }

    /**
     * Déconnexion volontaire — désactive la reconnexion auto.
     */
    fun disconnect() {
        wasConnectedBefore = false
        reconnectJob?.cancel()
        restoreAutoOff()
        bleManager.disconnect().enqueue()
    }

    fun sendShutterCommand(enable: Boolean) {
        bleCommand(
            GoProConstants.COMMAND_CHAR_UUID,
            byteArrayOf(GoProConstants.CMD_SET_SHUTTER.toByte(), 1, if (enable) 1 else 0)
        )
    }

    fun sendHilight() {
        bleCommand(
            GoProConstants.COMMAND_CHAR_UUID,
            byteArrayOf(GoProConstants.CMD_HILIGHT.toByte())
        )
        gpsTracker?.onHilight(System.currentTimeMillis())
    }

    /**
     * Extinction volontaire de la caméra — désactive la reconnexion auto.
     */
    fun sendSleep() {
        wasConnectedBefore = false
        reconnectJob?.cancel()
        restoreAutoOff()
        bleCommand(
            GoProConstants.COMMAND_CHAR_UUID,
            byteArrayOf(GoProConstants.CMD_SLEEP.toByte())
        )
    }

    fun sendReboot() {
        bleCommand(
            GoProConstants.COMMAND_CHAR_UUID,
            byteArrayOf(GoProConstants.CMD_REBOOT.toByte())
        )
    }

    fun syncDateTime() {
        val now = Calendar.getInstance()
        val year = now.get(Calendar.YEAR)
        val month = (now.get(Calendar.MONTH) + 1).toByte()
        val day = now.get(Calendar.DAY_OF_MONTH).toByte()
        val hour = now.get(Calendar.HOUR_OF_DAY).toByte()
        val min = now.get(Calendar.MINUTE).toByte()
        val sec = now.get(Calendar.SECOND).toByte()

        val payload = byteArrayOf(
            GoProConstants.CMD_SET_DATE.toByte(),
            7,
            (year shr 8).toByte(), (year and 0xFF).toByte(),
            month, day, hour, min, sec
        )
        bleCommand(GoProConstants.COMMAND_CHAR_UUID, payload)
    }

    fun loadPreset(presetId: Int) {
        val payload = ByteArray(6)
        payload[0] = 0x40.toByte()
        payload[1] = 0x04.toByte()
        payload[2] = ((presetId shr 24) and 0xFF).toByte()
        payload[3] = ((presetId shr 16) and 0xFF).toByte()
        payload[4] = ((presetId shr 8) and 0xFF).toByte()
        payload[5] = (presetId and 0xFF).toByte()
        bleCommand(GoProConstants.COMMAND_CHAR_UUID, payload)

        // Re-query les settings et capabilities après changement de preset
        lifecycleScope.launch(Dispatchers.IO) {
            delay(1000)
            bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_GET_SETTINGS_VALUES.toByte()))
            delay(1000)
            bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_GET_SETTING_CAPABILITIES.toByte()))
        }
    }

    fun updateSetting(settingId: Int, value: Int) {
        if (settingId == GoProConstants.SETTING_ID_AUTO_POWER_DOWN && isAppForeground) {
            // En premier plan : sauvegarder la préférence utilisateur pour restauration en arrière-plan.
            // Ne PAS envoyer à la caméra — le heartbeat maintient 59=0 pour la connexion HERO11 Mini.
            // L'UI affiche la valeur choisie (mise à jour optimiste dans le ViewModel par MainActivity).
            savedAutoOffValue = value
            prefs.edit().putInt(KEY_PREFERRED_APD, value).apply()
            Log.d(TAG, "APD préférence sauvegardée: $value (caméra maintenue à 59=0 par heartbeat)")
            return
        }
        bleCommand(
            GoProConstants.SETTINGS_CHAR_UUID,
            byteArrayOf(settingId.toByte(), 1, value.toByte())
        )
    }

    // ── Keep-alive & Subscriptions ───────────────────────────────────

    private fun startKeepAlive() {
        keepAliveJob?.cancel()
        lastCommandSentAt = System.currentTimeMillis()
        lastKeepAliveSentAt = lastCommandSentAt
        keepAliveJob = lifecycleScope.launch {
            while (true) {
                delay(3000)
                if (viewModel.uiState.value.isConnected &&
                    System.currentTimeMillis() - lastCommandSentAt >= 3000L) {
                    val now = System.currentTimeMillis()
                    val sinceLastKA = now - lastKeepAliveSentAt
                    Log.d(TAG, "KEEP_ALIVE envoyé (intervalle depuis dernier KA: ${sinceLastKA}ms)")
                    lastKeepAliveSentAt = now
                    bleManager.sendGoProCommand(
                        GoProConstants.COMMAND_CHAR_UUID,
                        byteArrayOf(GoProConstants.CMD_KEEP_ALIVE.toByte(), 1, GoProConstants.CMD_KEEP_ALIVE_VAL.toByte())
                    )
                } else if (viewModel.uiState.value.isConnected) {
                    Log.d(TAG, "KEEP_ALIVE skippé (commande récente il y a ${System.currentTimeMillis() - lastCommandSentAt}ms)")
                }
            }
        }
        // Heartbeat démarré par disableAutoOff() après chargement des settings, pas ici.
    }

    /**
     * Force le setting 59 (Auto Power Down) à 0 (Jamais) pour maintenir la connexion.
     * Sauvegarde la valeur préférée de l'utilisateur pour restauration en arrière-plan.
     * Démarre ensuite le heartbeat APD qui ré-envoie 59=0 toutes les 5s.
     */
    private fun disableAutoOff() {
        val currentValue = viewModel.uiState.value.settings[GoProConstants.SETTING_ID_AUTO_POWER_DOWN]
        if (currentValue == null) {
            Log.w(TAG, "disableAutoOff: setting 59 absent, heartbeat reporté")
            return
        }
        // Sauvegarder la préférence utilisateur (priorité : mémoire > prefs > valeur caméra)
        if (savedAutoOffValue == null) {
            val fromPrefs = prefs.getInt(KEY_PREFERRED_APD, -1)
            savedAutoOffValue = if (fromPrefs >= 0) fromPrefs else currentValue
        }
        prefs.edit().putInt(KEY_PREFERRED_APD, savedAutoOffValue!!).apply()
        // Afficher la préférence utilisateur dans l'UI (pas la valeur caméra forcée à 0)
        if (savedAutoOffValue != currentValue) {
            viewModel.updateSettings(mapOf(GoProConstants.SETTING_ID_AUTO_POWER_DOWN to savedAutoOffValue!!))
        }
        Log.d(TAG, "disableAutoOff: APD=$currentValue sauvegardé=$savedAutoOffValue → forçage 59=0 (Jamais)")
        // Forcer la caméra à APD=0 (requis HERO11 Mini pour maintenir la connexion)
        bleCommand(GoProConstants.SETTINGS_CHAR_UUID,
            byteArrayOf(GoProConstants.SETTING_ID_AUTO_POWER_DOWN.toByte(), 1, 0))
        startActivityHeartbeat()
    }

    /**
     * Restaure le setting APD à la valeur préférée de l'utilisateur.
     * Appelé quand l'app passe en arrière-plan (sans action en cours).
     */
    private fun restoreAutoOff() {
        val saved = savedAutoOffValue ?: prefs.getInt(KEY_PREFERRED_APD, -1).takeIf { it >= 0 }
        savedAutoOffValue = null
        if (saved != null && saved != 0 && viewModel.uiState.value.isConnected) {
            Log.d(TAG, "restoreAutoOff: restauration APD=$saved")
            bleManager.sendGoProCommand(GoProConstants.SETTINGS_CHAR_UUID,
                byteArrayOf(GoProConstants.SETTING_ID_AUTO_POWER_DOWN.toByte(), 1, saved.toByte()))
        }
    }

    /**
     * Appelé depuis MainActivity.onResume() / onPause().
     * En foreground : démarre le heartbeat APD (caméra reste éveillée).
     * En background sans action : arrête le heartbeat → la caméra suit son timer APD naturel.
     * En background avec action en cours (record/timer) : heartbeat maintenu, caméra reste active.
     */
    fun setAppForeground(foreground: Boolean) {
        isAppForeground = foreground
        if (!::bleManager.isInitialized) return
        if (foreground) {
            Log.d(TAG, "App premier plan — heartbeat APD démarré")
            if (viewModel.uiState.value.isConnected) disableAutoOff()
        } else {
            val state = viewModel.uiState.value
            if (state.isRecording || state.isCountdownActive) {
                Log.d(TAG, "App arrière-plan — action en cours (recording=${state.isRecording}, countdown=${state.isCountdownActive}) — heartbeat maintenu")
            } else {
                Log.d(TAG, "App arrière-plan — heartbeat arrêté, APD restauré")
                activityHeartbeatJob?.cancel()
                activityHeartbeatJob = null
                restoreAutoOff()
            }
        }
    }

    /**
     * Reset périodique du timer APD firmware en ré-envoyant 59=0 (Jamais) toutes les 5s.
     * Doc OpenGoPro : la caméra dort si les DEUX timers (APD + KA) tombent à zéro.
     * Le keep-alive reset le KA timer. Ce heartbeat reset le APD timer.
     * IMPORTANT : HERO11 Mini nécessite 59=0 (pas une valeur non-nulle, même ré-envoyée
     * périodiquement) pour maintenir la connexion. Toujours envoyer 0.
     */
    private fun startActivityHeartbeat() {
        activityHeartbeatJob?.cancel()
        Log.d(TAG, "APD heartbeat démarré (intervalle=5s, valeur=0/Jamais)")
        activityHeartbeatJob = lifecycleScope.launch {
            while (true) {
                delay(5_000)
                if (viewModel.uiState.value.isConnected) {
                    bleCommand(
                        GoProConstants.SETTINGS_CHAR_UUID,
                        byteArrayOf(GoProConstants.SETTING_ID_AUTO_POWER_DOWN.toByte(), 1, 0)
                    )
                    Log.d(TAG, "APD heartbeat → setting 59 = 0 (Jamais, reset timer)")
                }
            }
        }
    }

    private suspend fun performInitialPolling() {
        // Skip HW Info si on connaît déjà le numéro de série (reconnexion rapide)
        val knownSerial = withContext(Dispatchers.Main) { viewModel.uiState.value.serialNumber }
        var isReady = knownSerial.isNotEmpty()
        if (isReady) {
            Log.d(TAG, "HW Info déjà connu (serial=$knownSerial), skip polling")
        } else {
            var attempt = 0
            while (!isReady && attempt < 10) {
                Log.d(TAG, "HW Info polling attempt ${attempt + 1}/10")
                bleManager.sendGoProCommand(GoProConstants.COMMAND_CHAR_UUID, byteArrayOf(GoProConstants.CMD_GET_HARDWARE_INFO.toByte()))
                delay(2000)
                val serial = withContext(Dispatchers.Main) {
                    viewModel.uiState.value.serialNumber
                }
                if (serial.isNotEmpty()) isReady = true
                attempt++
            }
        }
        if (isReady) {
            Log.d(TAG, "Camera ready, starting subscriptions...")
            try {
                subscribeToUpdates()
                // Settings chargés → désactiver l'extinction auto si l'app est au premier plan.
                val settingsCount = viewModel.uiState.value.settings.size
                val hasSetting59 = viewModel.uiState.value.settings.containsKey(GoProConstants.SETTING_ID_AUTO_POWER_DOWN)
                Log.d(TAG, "Post-subscribe: isAppForeground=$isAppForeground, settings=$settingsCount, hasSetting59=$hasSetting59")
                if (isAppForeground) disableAutoOff()
                // Données initiales chargées → l'écran de contrôle peut s'afficher
                withContext(Dispatchers.Main) { viewModel.setCameraReady(true) }
                Log.d(TAG, "Camera ready: basculement vers dashboard")
            } catch (e: Exception) {
                Log.e(TAG, "Error subscribing", e)
            }
        } else {
            Log.e(TAG, "Camera not ready after 10 attempts")
        }
    }

    private suspend fun subscribeToUpdates() {
        val statusIds = byteArrayOf(
            GoProConstants.STATUS_ID_RECORDING.toByte(),
            GoProConstants.STATUS_ID_BATTERY.toByte(),
            GoProConstants.STATUS_ID_BATTERY_BARS.toByte(),
            GoProConstants.STATUS_ID_STORAGE.toByte(),
            GoProConstants.STATUS_ID_SD_CAPACITY.toByte(),
            GoProConstants.STATUS_ID_SD_STATUS.toByte(),
            GoProConstants.STATUS_ID_PHOTOS_REMAINING.toByte(),
            GoProConstants.STATUS_ID_VIDEOS_COUNT.toByte(),
            GoProConstants.STATUS_ID_VIDEO_REMAINING.toByte(),
            GoProConstants.STATUS_ID_ACTIVE_PRESET.toByte(),
            GoProConstants.STATUS_ID_BUSY.toByte(),
            GoProConstants.STATUS_ID_OVERHEATING.toByte()
        )

        // Optimisé : les Register retournent déjà les valeurs initiales (doc OpenGoPro)
        // → Get Settings (0x12), Get Capabilities (0x32), Get Status (0x13) supprimés (redondants)
        Log.d(TAG, "Subscribe: Claim Control...")
        bleManager.sendGoProCommand(GoProConstants.COMMAND_CHAR_UUID, byteArrayOf(0xF1.toByte(), 0x69.toByte(), 0x08.toByte(), 0x02.toByte()))
        delay(500)

        Log.d(TAG, "Subscribe: Register Status Updates (+ valeurs initiales)...")
        bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_REGISTER_STATUS_UPDATES.toByte()) + statusIds)
        delay(500)

        Log.d(TAG, "Subscribe: Register ALL Settings Updates (+ 43 settings initiaux)...")
        bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_REGISTER_SETTINGS_UPDATES.toByte()))
        delay(700)

        Log.d(TAG, "Subscribe: Register ALL Capabilities Updates (+ 31 capabilities initiales)...")
        bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_REGISTER_CAPABILITIES_UPDATES.toByte()))
        delay(700)

        Log.d(TAG, "Subscribe: Fetch Presets...")
        val protoBytes = byteArrayOf(0x08, 0x01, 0x08, 0x02)
        val packet = ByteArray(2 + protoBytes.size)
        packet[0] = 0xF5.toByte()
        packet[1] = 0x72.toByte()
        System.arraycopy(protoBytes, 0, packet, 2, protoBytes.size)
        bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, packet)
        delay(700)
        Log.d(TAG, "Subscribe: TERMINÉ (~3.1s au lieu de ~10.5s)")
    }

    // ── Parsing des réponses BLE ─────────────────────────────────────

    private suspend fun processBleMessage(charUuid: String, data: ByteArray) {
        when (charUuid) {
            GoProConstants.COMMAND_RSP_CHAR_UUID.toString() -> handleCommandResponse(data)
            GoProConstants.QUERY_RSP_CHAR_UUID.toString() -> handleQueryResponse(data)
            GoProConstants.SETTINGS_RSP_CHAR_UUID.toString() -> handleSettingsResponse(data)
        }
    }

    private suspend fun handleCommandResponse(data: ByteArray) {
        val cmdId = data[0].toInt() and 0xFF
        if (cmdId == GoProConstants.CMD_GET_HARDWARE_INFO) {
            val info = GoProStatusParser.parseQueryResponse(data)
            val modelName = info[2] as? String ?: "HERO Device"
            val serial = info[5] as? String ?: ""
            val version = info[4] as? String ?: ""
            withContext(Dispatchers.Main) {
                viewModel.updateHardwareInfo(serial, version, modelName)
            }
            Log.d(TAG, "Hardware Info: Model=$modelName, Serial=$serial, Ver=$version")
        }
    }

    private fun handleSettingsResponse(data: ByteArray) {
        val settingId = data[0].toInt() and 0xFF
        val status = if (data.size > 1) data[1].toInt() and 0xFF else -1
        if (status == 0) {
            Log.d(TAG, "Setting 0x${settingId.toString(16).uppercase()} appliqué, re-query capabilities")
            // Re-query uniquement les capabilities (pas les settings — l'update optimiste suffit)
            // Les capabilities changent quand un setting change (ex: changer résolution → nouveaux FPS dispo)
            lifecycleScope.launch(Dispatchers.IO) {
                delay(500)
                bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_GET_SETTING_CAPABILITIES.toByte()))
            }
        } else {
            Log.w(TAG, "Setting 0x${settingId.toString(16).uppercase()} refusé (status=$status)")
            // En cas de rejet, re-query les settings pour corriger l'update optimiste
            lifecycleScope.launch(Dispatchers.IO) {
                delay(300)
                bleManager.sendGoProCommand(GoProConstants.QUERY_CHAR_UUID, byteArrayOf(GoProConstants.QRY_GET_SETTINGS_VALUES.toByte()))
            }
        }
    }

    private suspend fun handleQueryResponse(data: ByteArray) {
        val queryId = data[0].toInt() and 0xFF
        Log.d(TAG, "Query Response ID: 0x${queryId.toString(16).uppercase()}")

        if (queryId == 0xF5) {
            val updates = GoProStatusParser.parseQueryResponse(data)
            if (updates.containsKey(0xF500)) {
                val notifyMsg = updates[0xF500] as? GoProProtos.NotifyPresetStatus
                if (notifyMsg != null) {
                    for (group in notifyMsg.presetGroupArrayList) {
                        Log.d(TAG, "PresetGroup id=${group.id}")
                        for (preset in group.presetArrayList) {
                            Log.d(TAG, "  Preset id=${preset.id}, titleId=${if (preset.hasTitleId()) preset.titleId else "N/A"}, icon=${if (preset.hasIcon()) preset.icon else "N/A"}, mode=${if (preset.hasMode()) preset.mode else "N/A"}")
                            for (setting in preset.settingArrayList) {
                                val label = GoProSettingsMappings.getLabel(setting.id, setting.value)
                                val name = GoProSettingsMappings.getSettingName(setting.id)
                                Log.d(TAG, "    Setting id=${setting.id} ($name), value=${setting.value} -> \"$label\"")
                            }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        viewModel.updatePresets(notifyMsg.presetGroupArrayList)
                    }
                }
            }
            return
        }

        val updates = try {
            GoProStatusParser.parseQueryResponse(data)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur parsing: ${e.message}")
            emptyMap()
        }

        if (updates.isEmpty()) return

        withContext(Dispatchers.Main) {
            applyUpdatesToViewModel(queryId, updates)
        }
    }

    private fun applyUpdatesToViewModel(queryId: Int, updates: Map<Int, Any>) {
        fun convertToInt(v: Any?): Int = when (v) {
            is Int -> v
            is Long -> v.toInt()
            is ByteArray -> {
                var res = 0
                for (b in v) res = (res shl 8) or (b.toInt() and 0xFF)
                res
            }
            is List<*> -> (v.firstOrNull() as? Int) ?: 0
            else -> 0
        }

        fun convertToLong(v: Any?): Long = when (v) {
            is Int -> v.toLong()
            is Long -> v
            is ByteArray -> {
                var res: Long = 0
                for (b in v) res = (res shl 8) or (b.toInt() and 0xFF).toLong()
                res
            }
            else -> 0L
        }

        when (queryId) {
            GoProConstants.QRY_GET_STATUS_VALUES, GoProConstants.RSP_ASYNC_STATUS, GoProConstants.QRY_REGISTER_STATUS_UPDATES -> {
                updates.forEach { (id, value) ->
                    when (id) {
                        GoProConstants.STATUS_ID_BATTERY -> viewModel.updateBattery(convertToInt(value))
                        GoProConstants.STATUS_ID_BATTERY_BARS -> viewModel.updateBatteryBars(convertToInt(value))
                        GoProConstants.STATUS_ID_RECORDING -> {
                            val isNowRecording = convertToInt(value) == 1
                            viewModel.updateRecording(isNowRecording)
                            val now = System.currentTimeMillis()
                            if (isNowRecording && !wasRecording) {
                                gpsTracker?.onRecordingStarted(now)
                            } else if (!isNowRecording && wasRecording) {
                                gpsTracker?.onRecordingStopped(now)
                            }
                            wasRecording = isNowRecording
                        }
                        GoProConstants.STATUS_ID_STORAGE -> viewModel.updateSdRemaining(convertToLong(value))
                        GoProConstants.STATUS_ID_SD_CAPACITY -> viewModel.updateSdCapacity(convertToLong(value))
                        GoProConstants.STATUS_ID_VIDEO_REMAINING -> viewModel.updateVideoRemaining(convertToInt(value))
                        GoProConstants.STATUS_ID_PHOTOS_REMAINING -> viewModel.updatePhotosRemaining(convertToInt(value))
                        GoProConstants.STATUS_ID_VIDEOS_COUNT -> viewModel.updateVideosCount(convertToInt(value))
                        GoProConstants.STATUS_ID_SD_STATUS -> viewModel.updateSdStatus(convertToInt(value))
                        GoProConstants.STATUS_ID_OVERHEATING -> viewModel.updateTempStatus(convertToInt(value) == 1, false)
                        GoProConstants.STATUS_ID_ACTIVE_PRESET -> {
                            val v = convertToInt(value)
                            viewModel.updateCurrentPresetId(v)
                        }
                    }
                }
            }
            GoProConstants.QRY_GET_SETTINGS_VALUES, GoProConstants.RSP_ASYNC_SETTING, GoProConstants.QRY_REGISTER_SETTINGS_UPDATES -> {
                val rawSettings = updates.mapValues { (_, v) -> convertToInt(v) }
                // En foreground avec heartbeat actif : filtrer setting 59 de TOUTE source (0x12, 0x93, 0x62)
                // pour empêcher la caméra (forcée à 0) d'écraser la préférence utilisateur.
                // Avant le heartbeat (init) : laisser passer pour que disableAutoOff() puisse lire la valeur.
                val settingsMap = if (isAppForeground && activityHeartbeatJob != null)
                    rawSettings.filterKeys { it != GoProConstants.SETTING_ID_AUTO_POWER_DOWN }
                else rawSettings
                Log.d(TAG, "Settings reçus (${settingsMap.size} entries)")
                viewModel.updateSettings(settingsMap)
                // Démarrer le heartbeat dès la réception des settings initiaux (avant fin de subscribe)
                if (isAppForeground && rawSettings.containsKey(GoProConstants.SETTING_ID_AUTO_POWER_DOWN)
                        && activityHeartbeatJob == null) {
                    disableAutoOff()
                }
            }
            GoProConstants.QRY_GET_SETTING_CAPABILITIES, GoProConstants.RSP_ASYNC_CAPABILITIES, GoProConstants.QRY_REGISTER_CAPABILITIES_UPDATES -> {
                val capsUpdate = updates.mapValues { (_, v) ->
                    when (v) {
                        is List<*> -> v.map { it as Int }
                        is ByteArray -> listOf(v[0].toInt() and 0xFF)
                        is Int -> listOf(v)
                        else -> emptyList()
                    }
                }
                Log.d(TAG, "Capabilities reçues (${capsUpdate.size} entries)")
                viewModel.updateCapabilities(capsUpdate)
            }
        }
    }
}
