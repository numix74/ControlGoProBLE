package com.ximun.gopropro.bubble

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Etats visuels de la bulle flottante.
 * Chaque état correspond à un design UI Figma distinct.
 */
enum class BubbleVisualState {
    /** Gris, icône caméra grise, pas d'indicateur */
    DISCONNECTED,
    /** Cyan, icône caméra cyan, pastille verte */
    CONNECTED,
    /** Rouge, "REC" blanc, point rouge clignotant */
    RECORDING,
    /** Rouge, timer "MM:SS" blanc, point rouge clignotant */
    RECORDING_TIMER
}

/**
 * Données d'état de la bulle flottante.
 */
data class BubbleState(
    val visualState: BubbleVisualState = BubbleVisualState.DISCONNECTED,
    val displayTime: String = "00:00",
    val isVisible: Boolean = false,
    val isTimerModeEnabled: Boolean = false
)

/**
 * Singleton qui sert de pont entre le ViewModel/Activity et le FloatingBubbleService.
 * Le service observe ce StateFlow pour mettre à jour l'affichage de la bulle.
 */
object BubbleStateHolder {
    private val _state = MutableStateFlow(BubbleState())
    val state: StateFlow<BubbleState> = _state.asStateFlow()

    /** Callback appelé quand l'utilisateur ferme la bulle via la Close Zone */
    var onBubbleDismissed: (() -> Unit)? = null

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
