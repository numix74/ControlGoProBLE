package com.actioncam.airbuble.bubble

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * États visuels de la bulle flottante.
 */
enum class BubbleVisualState {
    /** Gris, icône caméra grise */
    DISCONNECTED,
    /** Cyan, icône caméra cyan, pastille verte */
    CONNECTED,
    /** Rouge, "REC" blanc, point rouge clignotant */
    RECORDING,
    /** Rouge, timer "MM:SS" blanc, point rouge clignotant */
    RECORDING_TIMER
}

data class BubbleState(
    val visualState: BubbleVisualState = BubbleVisualState.DISCONNECTED,
    val displayTime: String = "00:00",
    val isVisible: Boolean = false,
    val isTimerModeEnabled: Boolean = false
)

/**
 * Singleton pont entre ViewModel/Activity et FloatingBubbleService.
 */
object BubbleStateHolder {
    private val _state = MutableStateFlow(BubbleState())
    val state: StateFlow<BubbleState> = _state.asStateFlow()

    var onBubbleDismissed: (() -> Unit)? = null
    var onRecordToggle: (() -> Unit)? = null
    var onHilight: (() -> Unit)? = null

    fun updateState(
        isConnected: Boolean,
        isRecording: Boolean,
        isCountdownActive: Boolean,
        displayTime: String,
        isTimerModeEnabled: Boolean = false
    ) {
        val visual = when {
            !isConnected -> BubbleVisualState.DISCONNECTED
            isRecording && isCountdownActive -> BubbleVisualState.RECORDING_TIMER
            isRecording -> BubbleVisualState.RECORDING
            else -> BubbleVisualState.CONNECTED
        }
        _state.value = BubbleState(
            visualState = visual,
            displayTime = displayTime,
            isVisible = _state.value.isVisible,
            isTimerModeEnabled = isTimerModeEnabled
        )
    }

    fun setVisible(visible: Boolean) {
        _state.value = _state.value.copy(isVisible = visible)
    }
}
