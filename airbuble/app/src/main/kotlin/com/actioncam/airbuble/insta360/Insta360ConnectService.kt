package com.actioncam.airbuble.insta360

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Foreground service that keeps the Insta360 camera connection alive
 * while the app is in the background.
 * Stub — full implementation in Phase 3 (WiFi connect).
 */
class Insta360ConnectService : Service() {

    companion object {
        private const val CHANNEL_ID = "airbuble_connect"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            CHANNEL_ID, "Camera Connection",
            NotificationManager.IMPORTANCE_LOW
        )
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("AirBuble")
            .setContentText("Camera connected")
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .build()
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
