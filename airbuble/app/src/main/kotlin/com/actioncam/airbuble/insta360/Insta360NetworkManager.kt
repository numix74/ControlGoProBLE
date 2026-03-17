package com.actioncam.airbuble.insta360

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Manages network binding for Insta360 camera WiFi + cellular.
 *
 * Flow:
 *   1. init(context)          — tracks mobileNet (cellular)
 *   2. connectToCamera(ssid, pwd) — WifiNetworkSpecifier → stores cameraNet
 *   3. bindToCameraNetwork()  — routes process HTTP through camera WiFi
 *   4. setNetworkHandle(sdk)  — tells SDK which networkHandle to use
 *   5. bindToMobileNetwork()  — switches back to mobile (after openCamera succeeds)
 *   6. unbind()               — on disconnect, releases everything
 */
object Insta360NetworkManager {

    private const val TAG = "Insta360NetworkMgr"

    var cameraNet: Network? = null
        private set

    var mobileNet: Network? = null
        private set

    private var connectivityManager: ConnectivityManager? = null
    private var wifiManager: WifiManager? = null
    private var mobileCallback: ConnectivityManager.NetworkCallback? = null
    private var cameraCallback: ConnectivityManager.NetworkCallback? = null

    // ------------------------------------------------------------------ //
    //  Init                                                                //
    // ------------------------------------------------------------------ //

    fun init(context: Context) {
        if (connectivityManager != null) return
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        connectivityManager = cm
        wifiManager = wm

        // Track cellular network for post-connect mobile rebinding
        val mobileRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        mobileCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                mobileNet = network
                Log.d(TAG, "mobileNet available: ${network.networkHandle}")
            }
            override fun onLost(network: Network) {
                if (mobileNet == network) mobileNet = null
            }
        }
        cm.registerNetworkCallback(mobileRequest, mobileCallback!!)
        Log.d(TAG, "init — cellular listener registered")
    }

    // ------------------------------------------------------------------ //
    //  Camera WiFi connect (WifiNetworkSpecifier)                         //
    // ------------------------------------------------------------------ //

    /**
     * Connects Android to the camera WiFi AP using WifiNetworkSpecifier.
     * Shows the system WiFi selection dialog. Returns true if network is available.
     * Stores the camera [Network] object in [cameraNet] for later binding.
     */
    suspend fun connectToCamera(ssid: String, pwd: String): Boolean {
        val cm = connectivityManager ?: run {
            Log.e(TAG, "Not initialized — call init() first")
            return false
        }
        val wm = wifiManager ?: return false
        if (!wm.isWifiEnabled) {
            Log.w(TAG, "WiFi disabled on device")
            return false
        }

        Log.i(TAG, "Connecting to camera WiFi: $ssid")
        return suspendCancellableCoroutine { cont ->
            val specifier = WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(pwd)
                .build()

            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(specifier)
                .build()

            var resumed = false
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    if (!resumed) {
                        resumed = true
                        cameraNet = network
                        cameraCallback = this
                        Log.i(TAG, "Camera WiFi onAvailable handle=${network.networkHandle}")
                        cont.resume(true)
                    }
                }
                override fun onUnavailable() {
                    if (!resumed) {
                        resumed = true
                        Log.w(TAG, "Camera WiFi onUnavailable")
                        cont.resume(false)
                    }
                }
                override fun onLost(network: Network) {
                    if (cameraNet == network) {
                        Log.w(TAG, "Camera WiFi lost")
                        cameraNet = null
                    }
                }
            }
            cm.requestNetwork(request, callback)
            // callback stays alive to keep the WiFi connection — unregistered on unbind()
        }
    }

    // ------------------------------------------------------------------ //
    //  Bind helpers                                                        //
    // ------------------------------------------------------------------ //

    /**
     * Binds process to camera WiFi network so SDK HTTP calls reach the camera.
     * Must be called after [connectToCamera] succeeds.
     */
    fun bindToCameraNetwork(): Boolean {
        val net = cameraNet ?: run {
            Log.e(TAG, "bindToCameraNetwork: cameraNet is null")
            return false
        }
        val result = connectivityManager?.bindProcessToNetwork(net) ?: false
        Log.i(TAG, "bindToCameraNetwork: $result (handle=${net.networkHandle})")
        return result
    }

    /**
     * Switches process binding back to mobile/internet.
     * Called after openCamera(CONNECT_TYPE_WIFI) succeeds — camera SDK has its own network handle,
     * we restore general internet access for the rest of the app.
     */
    fun bindToMobileNetwork() {
        connectivityManager?.bindProcessToNetwork(mobileNet)
        Log.d(TAG, "bindToMobileNetwork: handle=${mobileNet?.networkHandle}")
    }

    /**
     * Releases all network bindings and unregisters the camera WiFi callback.
     * Call on disconnect.
     */
    fun unbind() {
        connectivityManager?.bindProcessToNetwork(null)
        cameraCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
        cameraCallback = null
        cameraNet = null
        Log.d(TAG, "unbind — done")
    }

    fun stop() {
        unbind()
        mobileCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
        mobileCallback = null
        connectivityManager = null
        wifiManager = null
        Log.d(TAG, "stop")
    }
}
