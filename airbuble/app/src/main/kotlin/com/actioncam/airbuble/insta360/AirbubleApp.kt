package com.actioncam.airbuble.insta360

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import com.arashivision.sdkcamera.InstaCameraSDK
import com.arashivision.sdkmedia.InstaMediaSDK

class AirbubleApp : Application() {

    companion object {
        private const val TAG = "AirbubleApp"

        @SuppressLint("StaticFieldLeak")
        lateinit var instance: AirbubleApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // SDK init — MUST happen before any InstaCameraManager call
        InstaCameraSDK.init(this)
        InstaMediaSDK.init(this)

        // Init network manager (tracks cellular for post-connect rebinding)
        Insta360NetworkManager.init(this)

        Log.i(TAG, "Insta360 SDK initialized (sdkcamera + sdkmedia)")
    }
}
