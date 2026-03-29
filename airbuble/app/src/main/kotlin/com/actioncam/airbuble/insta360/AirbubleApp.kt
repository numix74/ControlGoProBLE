package com.actioncam.airbuble.insta360

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import com.actioncam.airbuble.diagnostic.DiagnosticProvider
import com.actioncam.airbuble.diagnostic.NoopDiagnosticProvider
import com.arashivision.sdkcamera.InstaCameraSDK
import com.arashivision.sdkmedia.InstaMediaSDK

class AirbubleApp : Application() {

    companion object {
        private const val TAG = "AirbubleApp"

        @SuppressLint("StaticFieldLeak")
        lateinit var instance: AirbubleApp
            private set

        /** Diagnostic : Noop en release, DebugDiagnosticProvider en debug. */
        var diagnostic: DiagnosticProvider = NoopDiagnosticProvider()
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Diagnostic provider — surchargé en debug via initDiagnostic()
        initDiagnostic()

        // SDK init — MUST happen before any InstaCameraManager call
        InstaCameraSDK.init(this)
        InstaMediaSDK.init(this)

        // Init network manager (tracks cellular for post-connect rebinding)
        Insta360NetworkManager.init(this)

        diagnostic.log(TAG, "App démarrée — SDK Insta360 initialisé")
    }

    /**
     * En main/ : ne fait rien (reste NoopDiagnosticProvider).
     * Surchargée dans debug/ via DebugAirbubleAppInit.
     */
    private fun initDiagnostic() {
        try {
            val clazz = Class.forName("com.actioncam.airbuble.diagnostic.DebugDiagnosticProvider")
            diagnostic = clazz.getDeclaredConstructor().newInstance() as DiagnosticProvider
            Log.i(TAG, "DiagnosticProvider: DEBUG mode actif")
        } catch (_: ClassNotFoundException) {
            // Release build — DebugDiagnosticProvider n'existe pas, on garde Noop
        }
    }
}
