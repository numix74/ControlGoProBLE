package com.ximun.gopropro.bubble

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.ximun.gopropro.ble.GoProConnectionManager
import com.ximun.gopropro.viewmodel.GoProViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Contrôle le cycle de vie de la bulle flottante :
 * observation du state, start/stop du service, callbacks.
 */
class BubbleController(
    private val context: Context,
    private val viewModel: GoProViewModel,
    private val lifecycleScope: CoroutineScope,
    private val connectionManager: GoProConnectionManager
) {
    var isBubbleServiceRunning = false
        private set

    private var observerJob: Job? = null

    /**
     * Câble les callbacks du BubbleStateHolder.
     */
    fun setupCallbacks(onRecordToggle: () -> Unit) {
        BubbleStateHolder.onBubbleDismissed = {
            viewModel.toggleBubble()
        }
        BubbleStateHolder.onRecordToggle = {
            onRecordToggle()
        }
        BubbleStateHolder.onHilight = {
            connectionManager.sendHilight()
        }
        BubbleStateHolder.onReconnect = {
            connectionManager.reconnectManually()
        }
    }

    /**
     * Observe le ViewModel et démarre/arrête le service bulle automatiquement.
     */
    fun startObserving(activity: ComponentActivity, overlayLauncher: ActivityResultLauncher<Intent>) {
        observerJob?.cancel()
        observerJob = lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                BubbleStateHolder.updateState(
                    isConnected = state.isConnected,
                    isRecording = state.isRecording,
                    isCountdownActive = state.isCountdownActive,
                    displayTime = state.displayTime,
                    isTimerModeEnabled = state.isTimerModeEnabled
                )

                val shouldShowBubble = state.isBubbleEnabled
                if (shouldShowBubble && !isBubbleServiceRunning) {
                    requestOverlayPermissionAndStart(activity, overlayLauncher)
                    isBubbleServiceRunning = true
                } else if (!shouldShowBubble && isBubbleServiceRunning) {
                    FloatingBubbleService.stop(context)
                    isBubbleServiceRunning = false
                }
            }
        }
    }

    private fun requestOverlayPermissionAndStart(
        activity: ComponentActivity,
        overlayLauncher: ActivityResultLauncher<Intent>
    ) {
        if (Settings.canDrawOverlays(context)) {
            FloatingBubbleService.start(context)
        } else {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            overlayLauncher.launch(intent)
        }
    }

    fun onOverlayPermissionResult() {
        if (Settings.canDrawOverlays(context)) {
            FloatingBubbleService.start(context)
        }
    }

    /**
     * Arrête le service et nettoie les callbacks.
     */
    fun stopAndCleanup() {
        observerJob?.cancel()
        if (isBubbleServiceRunning) {
            FloatingBubbleService.stop(context)
            isBubbleServiceRunning = false
        }
        BubbleStateHolder.onBubbleDismissed = null
        BubbleStateHolder.onRecordToggle = null
        BubbleStateHolder.onHilight = null
        BubbleStateHolder.onReconnect = null
    }
}
