package com.actioncam.airbuble.bubble

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.actioncam.airbuble.viewmodel.AirbubleViewModel
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
    private val viewModel: AirbubleViewModel,
    private val lifecycleScope: CoroutineScope
) {
    var isBubbleServiceRunning = false
        private set

    private var observerJob: Job? = null

    /**
     * Câble les callbacks du BubbleStateHolder.
     * [onRecordToggle] : appelé quand l'utilisateur fait un appui long sur la bulle.
     * [onHilight]      : appelé quand l'utilisateur tape la bulle en enregistrement.
     */
    fun setupCallbacks(onRecordToggle: () -> Unit, onHilight: () -> Unit) {
        BubbleStateHolder.onBubbleDismissed = { viewModel.toggleBubble() }
        BubbleStateHolder.onRecordToggle    = { onRecordToggle() }
        BubbleStateHolder.onHilight         = { onHilight() }
    }

    /**
     * Observe le ViewModel et démarre/arrête le service bulle automatiquement.
     */
    fun startObserving(activity: ComponentActivity, overlayLauncher: ActivityResultLauncher<Intent>) {
        observerJob?.cancel()
        observerJob = lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                BubbleStateHolder.updateState(
                    isConnected       = state.isCameraReady,
                    isRecording       = state.isRecording,
                    isCountdownActive = state.isCountdownActive,
                    displayTime       = state.displayTime,
                    isTimerModeEnabled = state.isTimerModeEnabled
                )

                val shouldShowBubble = state.isBubbleEnabled && state.isCameraReady
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
            overlayLauncher.launch(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
            )
        }
    }

    fun onOverlayPermissionResult() {
        if (Settings.canDrawOverlays(context)) {
            FloatingBubbleService.start(context)
        }
    }

    fun stopAndCleanup() {
        observerJob?.cancel()
        if (isBubbleServiceRunning) {
            FloatingBubbleService.stop(context)
            isBubbleServiceRunning = false
        }
        BubbleStateHolder.onBubbleDismissed = null
        BubbleStateHolder.onRecordToggle    = null
        BubbleStateHolder.onHilight         = null
    }
}
