package com.ximun.gopropro.bubble

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.os.IBinder
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.ximun.gopropro.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Service Foreground qui affiche une bulle flottante par-dessus les autres applications.
 *
 * Etats visuels (selon design Figma) :
 * - DISCONNECTED : bordure grise #4A4A4A, icône caméra grise #6B7280
 * - CONNECTED    : bordure cyan #4CC4C4, icône caméra cyan, pastille verte #22C55E
 * - RECORDING    : bordure rouge #FF0000, texte "REC" blanc, point rouge #DC2626 clignotant
 * - RECORDING_TIMER : bordure rouge, timer "MM:SS" blanc, point rouge clignotant
 *
 * La bulle est draggable. Quand on la drag vers le bas, la Close Zone apparaît.
 */
class FloatingBubbleService : Service() {

    companion object {
        private const val CHANNEL_ID = "floating_bubble_channel"
        private const val NOTIFICATION_ID = 1001

        // Dimensions en dp
        private const val BUBBLE_WIDTH_DP = 120f
        private const val BUBBLE_HEIGHT_DP = 70f
        private const val CLOSE_ZONE_WIDTH_DP = 200f
        private const val CLOSE_ZONE_HEIGHT_DP = 100f

        fun start(context: Context) {
            val intent = Intent(context, FloatingBubbleService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FloatingBubbleService::class.java))
        }
    }

    private lateinit var windowManager: WindowManager
    private var bubbleView: BubbleView? = null
    private var closeZoneView: CloseZoneView? = null
    private var bubbleParams: WindowManager.LayoutParams? = null
    private var closeZoneParams: WindowManager.LayoutParams? = null

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var blinkJob: Job? = null
    private var currentState = BubbleVisualState.DISCONNECTED
    private var currentDisplayTime = "00:00"
    private var isBlinkOn = true
    private var isDragging = false
    private var isCloseZoneVisible = false

    // ── Lifecycle ────────────────────────────────────────────────────

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        createBubbleView()
        createCloseZoneView()
        observeState()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        blinkJob?.cancel()
        try { bubbleView?.let { windowManager.removeView(it) } } catch (_: Exception) {}
        try { closeZoneView?.let { windowManager.removeView(it) } } catch (_: Exception) {}
        BubbleStateHolder.setVisible(false)
    }

    // ── Notification (Foreground Service) ────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "GoPro Bulle Flottante",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Contrôle rapide GoPro"
                setShowBadge(false)
            }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("GoPro Control")
                .setContentText("Bulle flottante active")
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setContentIntent(pendingIntent)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("GoPro Control")
                .setContentText("Bulle flottante active")
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setContentIntent(pendingIntent)
                .build()
        }
    }

    // ── Bubble View ──────────────────────────────────────────────────

    @SuppressLint("ClickableViewAccessibility")
    private fun createBubbleView() {
        val widthPx = dpToPx(BUBBLE_WIDTH_DP).toInt()
        val heightPx = dpToPx(BUBBLE_HEIGHT_DP).toInt()

        bubbleView = BubbleView(this).apply {
            setOnTouchListener(BubbleTouchListener())
        }

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        bubbleParams = WindowManager.LayoutParams(
            widthPx, heightPx,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = dpToPx(200f).toInt()
        }

        windowManager.addView(bubbleView, bubbleParams)
        BubbleStateHolder.setVisible(true)
    }

    // ── Close Zone View ──────────────────────────────────────────────

    private fun createCloseZoneView() {
        val widthPx = dpToPx(CLOSE_ZONE_WIDTH_DP).toInt()
        val heightPx = dpToPx(CLOSE_ZONE_HEIGHT_DP).toInt()

        closeZoneView = CloseZoneView(this)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        closeZoneParams = WindowManager.LayoutParams(
            widthPx, heightPx,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = -dpToPx(16f).toInt()
        }

        closeZoneView?.alpha = 0f
        windowManager.addView(closeZoneView, closeZoneParams)
    }

    private fun showCloseZone() {
        if (isCloseZoneVisible) return
        isCloseZoneVisible = true
        closeZoneView?.animate()
            ?.alpha(1f)
            ?.setDuration(200)
            ?.start()
    }

    private fun hideCloseZone() {
        if (!isCloseZoneVisible) return
        isCloseZoneVisible = false
        closeZoneView?.animate()
            ?.alpha(0f)
            ?.setDuration(200)
            ?.start()
    }

    private fun isBubbleInCloseZone(): Boolean {
        val params = bubbleParams ?: return false
        val screenHeight: Int
        val screenWidth: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            screenWidth = bounds.width()
            screenHeight = bounds.height()
        } else {
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            screenWidth = display.width
            @Suppress("DEPRECATION")
            screenHeight = display.height
        }

        val bubbleCenterX = params.x + dpToPx(BUBBLE_WIDTH_DP) / 2
        val bubbleCenterY = params.y + dpToPx(BUBBLE_HEIGHT_DP) / 2

        val closeZoneTop = screenHeight - dpToPx(CLOSE_ZONE_HEIGHT_DP) - dpToPx(16f)
        val closeZoneLeft = (screenWidth - dpToPx(CLOSE_ZONE_WIDTH_DP)) / 2
        val closeZoneRight = closeZoneLeft + dpToPx(CLOSE_ZONE_WIDTH_DP)

        return bubbleCenterY > closeZoneTop &&
               bubbleCenterX > closeZoneLeft &&
               bubbleCenterX < closeZoneRight
    }

    // ── State observation ────────────────────────────────────────────

    private fun observeState() {
        serviceScope.launch {
            BubbleStateHolder.state.collectLatest { state ->
                val prevState = currentState
                currentState = state.visualState
                currentDisplayTime = state.displayTime
                bubbleView?.isTimerModeEnabled = state.isTimerModeEnabled

                // Gérer le clignotement du point rouge
                if (currentState == BubbleVisualState.RECORDING ||
                    currentState == BubbleVisualState.RECORDING_TIMER) {
                    if (prevState != BubbleVisualState.RECORDING &&
                        prevState != BubbleVisualState.RECORDING_TIMER) {
                        startBlinking()
                    }
                } else {
                    stopBlinking()
                }

                bubbleView?.invalidate()
            }
        }
    }

    private fun startBlinking() {
        blinkJob?.cancel()
        blinkJob = serviceScope.launch {
            while (true) {
                isBlinkOn = !isBlinkOn
                bubbleView?.invalidate()
                delay(500)
            }
        }
    }

    private fun stopBlinking() {
        blinkJob?.cancel()
        isBlinkOn = true
    }

    // ── Touch handling ───────────────────────────────────────────────

    private inner class BubbleTouchListener : View.OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f
        private var hasMoved = false

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = bubbleParams?.x ?: 0
                    initialY = bubbleParams?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    hasMoved = false
                    isDragging = false
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY

                    if (!isDragging && (Math.abs(dx) > 10 || Math.abs(dy) > 10)) {
                        isDragging = true
                        hasMoved = true
                        showCloseZone()
                        // Passer en mode drag visuel
                        bubbleView?.setDragMode(true)
                    }

                    if (isDragging) {
                        val newX = (initialX + dx).toInt()
                        val newY = (initialY + dy).toInt()

                        // Clamper dans les limites de l'écran
                        val screenWidth: Int
                        val screenHeight: Int
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val bounds = windowManager.currentWindowMetrics.bounds
                            screenWidth = bounds.width()
                            screenHeight = bounds.height()
                        } else {
                            @Suppress("DEPRECATION")
                            val display = windowManager.defaultDisplay
                            @Suppress("DEPRECATION")
                            screenWidth = display.width
                            @Suppress("DEPRECATION")
                            screenHeight = display.height
                        }
                        val bw = dpToPx(BUBBLE_WIDTH_DP).toInt()
                        val bh = dpToPx(BUBBLE_HEIGHT_DP).toInt()
                        bubbleParams?.x = newX.coerceIn(0, screenWidth - bw)
                        bubbleParams?.y = newY.coerceIn(0, screenHeight - bh)
                        try {
                            windowManager.updateViewLayout(bubbleView, bubbleParams)
                        } catch (_: Exception) {}

                        // Feedback visuel si dans la close zone
                        closeZoneView?.setHighlighted(isBubbleInCloseZone())
                    }
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    if (isDragging) {
                        isDragging = false
                        bubbleView?.setDragMode(false)
                        hideCloseZone()

                        if (isBubbleInCloseZone()) {
                            // Synchroniser le toggle dans les réglages
                            BubbleStateHolder.onBubbleDismissed?.invoke()
                            // Fermer la bulle
                            stopSelf()
                            return true
                        }
                    } else if (!hasMoved) {
                        // Simple tap → ouvrir l'app
                        val intent = Intent(this@FloatingBubbleService, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        }
                        startActivity(intent)
                    }
                    return true
                }
            }
            return false
        }
    }

    // ── Utility ──────────────────────────────────────────────────────

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
        )
    }

    // ══════════════════════════════════════════════════════════════════
    // Custom Views (dessin Canvas)
    // ══════════════════════════════════════════════════════════════════

    /**
     * Vue de la bulle flottante — capsule 120x70dp.
     * Dessine les différents états visuels directement en Canvas.
     */
    private inner class BubbleView(context: Context) : View(context) {

        private var inDragMode = false
        var isTimerModeEnabled = false

        // Paints réutilisables
        private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            style = Paint.Style.FILL
        }
        private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }
        private val innerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = dpToPx(2f)
        }
        private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = dpToPx(2.5f)
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        private val iconFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }

        private val capsuleRect = RectF()
        private val innerRect = RectF()

        fun setDragMode(drag: Boolean) {
            inDragMode = drag
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat()
            val h = height.toFloat()
            val radius = h / 2f

            // Fond solide AppBackground (#0F172A)
            capsuleRect.set(0f, 0f, w, h)
            canvas.drawRoundRect(capsuleRect, radius, radius, bgPaint)

            when (currentState) {
                BubbleVisualState.DISCONNECTED -> drawDisconnected(canvas, w, h, radius)
                BubbleVisualState.CONNECTED -> {
                    if (inDragMode) drawDragMode(canvas, w, h, radius)
                    else drawConnected(canvas, w, h, radius)
                }
                BubbleVisualState.RECORDING -> drawRecording(canvas, w, h, radius)
                BubbleVisualState.RECORDING_TIMER -> drawRecordingTimer(canvas, w, h, radius)
            }
        }

        // ── Etat DISCONNECTED ────────────────────────────────────────

        private fun drawDisconnected(canvas: Canvas, w: Float, h: Float, radius: Float) {
            // Bordure grise #4A4A4A 2dp
            borderPaint.color = Color.parseColor("#4A4A4A")
            borderPaint.strokeWidth = dpToPx(2f)
            val inset = dpToPx(1f)
            capsuleRect.set(inset, inset, w - inset, h - inset)
            canvas.drawRoundRect(capsuleRect, radius - inset, radius - inset, borderPaint)

            // Icône caméra grise centrée à droite
            iconPaint.color = Color.parseColor("#6B7280")
            val camCx = w * 0.55f
            val camCy = h * 0.5f
            drawCameraIcon(canvas, camCx, camCy, dpToPx(12f), false)

            // Overlay timer si activé
            if (isTimerModeEnabled) {
                drawTimerOverlay(canvas, camCx, camCy, Color.parseColor("#6B7280"))
            }
        }

        // ── Etat CONNECTED ───────────────────────────────────────────

        private fun drawConnected(canvas: Canvas, w: Float, h: Float, radius: Float) {
            // Bordure cyan 2dp
            borderPaint.color = Color.parseColor("#4CC4C4")
            borderPaint.strokeWidth = dpToPx(2f)
            val inset = dpToPx(1f)
            capsuleRect.set(inset, inset, w - inset, h - inset)
            canvas.drawRoundRect(capsuleRect, radius - inset, radius - inset, borderPaint)

            // Bordure intérieure cyan 30%
            innerBorderPaint.color = Color.argb(77, 76, 196, 196)
            val innerInset = dpToPx(5f)
            innerRect.set(innerInset, innerInset, w - innerInset, h - innerInset)
            val innerRadius = (h - innerInset * 2) / 2f
            canvas.drawRoundRect(innerRect, innerRadius, innerRadius, innerBorderPaint)

            // Icône caméra cyan centrée à droite
            iconPaint.color = Color.parseColor("#4CC4C4")
            val camCx = w * 0.55f
            val camCy = h * 0.5f
            drawCameraIcon(canvas, camCx, camCy, dpToPx(12f), false)

            // Overlay timer si activé
            if (isTimerModeEnabled) {
                drawTimerOverlay(canvas, camCx, camCy, Color.parseColor("#4CC4C4"))
            }

            // Pastille verte — même position que le point rouge en mode recording
            drawIndicatorDot(canvas, w * 0.2f, h * 0.5f, Color.parseColor("#22C55E"))
        }

        // ── Etat DRAG ────────────────────────────────────────────────

        private fun drawDragMode(canvas: Canvas, w: Float, h: Float, radius: Float) {
            // Bordure cyan 3dp (épaissie pendant le drag)
            borderPaint.color = Color.parseColor("#4CC4C4")
            borderPaint.strokeWidth = dpToPx(3f)
            val inset = dpToPx(1.5f)
            capsuleRect.set(inset, inset, w - inset, h - inset)
            canvas.drawRoundRect(capsuleRect, radius - inset, radius - inset, borderPaint)

            // Bordure intérieure cyan 2dp
            innerBorderPaint.color = Color.parseColor("#4CC4C4")
            val innerInset = dpToPx(5f)
            innerRect.set(innerInset, innerInset, w - innerInset, h - innerInset)
            val innerRadius = (h - innerInset * 2) / 2f
            canvas.drawRoundRect(innerRect, innerRadius, innerRadius, innerBorderPaint)

            // Icône caméra cyan FILLED
            iconPaint.color = Color.parseColor("#4CC4C4")
            iconFillPaint.color = Color.parseColor("#4CC4C4")
            val camCx = w * 0.55f
            val camCy = h * 0.5f
            drawCameraIcon(canvas, camCx, camCy, dpToPx(12f), true)

            // Overlay timer si activé
            if (isTimerModeEnabled) {
                drawTimerOverlay(canvas, camCx, camCy, Color.parseColor("#4CC4C4"))
            }

            // Pastille verte — même position que le point rouge
            drawIndicatorDot(canvas, w * 0.2f, h * 0.5f, Color.parseColor("#22C55E"))
        }

        // ── Etat RECORDING ───────────────────────────────────────────

        private fun drawRecording(canvas: Canvas, w: Float, h: Float, radius: Float) {
            drawRecordingBase(canvas, w, h, radius)

            // Point rouge clignotant à gauche
            if (isBlinkOn) {
                drawIndicatorDot(canvas, w * 0.2f, h * 0.5f, Color.parseColor("#DC2626"))
            }

            // Texte "REC" blanc
            textPaint.color = Color.WHITE
            textPaint.textSize = dpToPx(14f)
            canvas.drawText("REC", w * 0.6f, h * 0.5f + dpToPx(5f), textPaint)
        }

        // ── Etat RECORDING_TIMER ─────────────────────────────────────

        private fun drawRecordingTimer(canvas: Canvas, w: Float, h: Float, radius: Float) {
            drawRecordingBase(canvas, w, h, radius)

            // Point rouge clignotant à gauche
            if (isBlinkOn) {
                drawIndicatorDot(canvas, w * 0.2f, h * 0.5f, Color.parseColor("#DC2626"))
            }

            // Timer blanc
            textPaint.color = Color.WHITE
            textPaint.textSize = dpToPx(13f)
            canvas.drawText(currentDisplayTime, w * 0.6f, h * 0.5f + dpToPx(5f), textPaint)
        }

        // ── Base recording (partagée) ────────────────────────────────

        private fun drawRecordingBase(canvas: Canvas, w: Float, h: Float, radius: Float) {
            // Bordure rouge 3dp
            borderPaint.color = Color.parseColor("#FF0000")
            borderPaint.strokeWidth = dpToPx(3f)
            val inset = dpToPx(1.5f)
            capsuleRect.set(inset, inset, w - inset, h - inset)
            canvas.drawRoundRect(capsuleRect, radius - inset, radius - inset, borderPaint)

            // Bordure intérieure #EE2222
            innerBorderPaint.color = Color.parseColor("#EE2222")
            val innerInset = dpToPx(5f)
            innerRect.set(innerInset, innerInset, w - innerInset, h - innerInset)
            val innerRadius = (h - innerInset * 2) / 2f
            canvas.drawRoundRect(innerRect, innerRadius, innerRadius, innerBorderPaint)
        }

        // ── Helpers dessin ───────────────────────────────────────────

        /**
         * Dessine l'icône caméra (outline ou filled) centrée sur (cx, cy).
         */
        private fun drawCameraIcon(canvas: Canvas, cx: Float, cy: Float, size: Float, filled: Boolean) {
            val bodyW = size * 2f
            val bodyH = size * 1.4f
            val bodyLeft = cx - bodyW / 2f
            val bodyTop = cy - bodyH / 2f + size * 0.15f
            val bodyRadius = size * 0.25f

            // Corps de la caméra
            val bodyRect = RectF(bodyLeft, bodyTop, bodyLeft + bodyW, bodyTop + bodyH)
            canvas.drawRoundRect(bodyRect, bodyRadius, bodyRadius, iconPaint)

            // Haut de la caméra (flash/objectif top)
            val topW = bodyW * 0.55f
            val topH = size * 0.3f
            val topLeft = cx - topW / 2f
            val topTop = bodyTop - topH * 0.6f
            canvas.drawLine(topLeft, bodyTop, topLeft, topTop, iconPaint)
            canvas.drawLine(topLeft, topTop, topLeft + topW, topTop, iconPaint)
            canvas.drawLine(topLeft + topW, topTop, topLeft + topW, bodyTop, iconPaint)

            // Objectif circulaire
            val lensRadius = size * 0.35f
            if (filled) {
                canvas.drawCircle(cx, cy + size * 0.15f, lensRadius, iconFillPaint)
            }
            canvas.drawCircle(cx, cy + size * 0.15f, lensRadius, iconPaint)
        }

        /**
         * Dessine un point indicateur (vert ou rouge) avec glow.
         */
        private fun drawIndicatorDot(canvas: Canvas, cx: Float, cy: Float, color: Int) {
            val radius = dpToPx(5f)

            // Glow
            glowPaint.color = Color.argb(100, Color.red(color), Color.green(color), Color.blue(color))
            glowPaint.strokeWidth = dpToPx(3f)
            canvas.drawCircle(cx, cy, radius + dpToPx(2f), glowPaint)

            // Point
            dotPaint.color = color
            canvas.drawCircle(cx, cy, radius, dotPaint)
        }

        /**
         * Dessine une petite icône timer (horloge) en overlay sur la caméra,
         * décalée en haut à droite. Un cercle de fond AppBackground masque
         * proprement le contour de la caméra en dessous.
         */
        private fun drawTimerOverlay(canvas: Canvas, cameraCx: Float, cameraCy: Float, iconColor: Int) {
            val size = dpToPx(8f) // rayon de l'horloge
            // Position décalée en haut à droite de l'icône caméra
            val ox = cameraCx + dpToPx(10f)
            val oy = cameraCy - dpToPx(10f)

            // Fond opaque AppBackground pour masquer les contours de la caméra
            bgPaint.color = Color.parseColor("#0F172A")
            canvas.drawCircle(ox, oy, size + dpToPx(3f), bgPaint)

            // Cercle de l'horloge
            iconPaint.color = iconColor
            iconPaint.strokeWidth = dpToPx(1.8f)
            canvas.drawCircle(ox, oy, size, iconPaint)

            // Aiguilles : une verticale courte (12h → centre) et une horizontale courte (centre → 3h)
            val handLong = size * 0.65f
            val handShort = size * 0.45f
            // Aiguille des minutes (vers 12h)
            canvas.drawLine(ox, oy, ox, oy - handLong, iconPaint)
            // Aiguille des heures (vers 3h)
            canvas.drawLine(ox, oy, ox + handShort, oy, iconPaint)
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // Close Zone View
    // ══════════════════════════════════════════════════════════════════

    /**
     * Vue de la zone de fermeture en bas de l'écran.
     * Panneau sombre arrondi avec icône X rouge et texte "CLOSE ZONE".
     */
    private inner class CloseZoneView(context: Context) : View(context) {

        private var isHighlighted = false

        private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(200, 30, 30, 30)
        }
        private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = dpToPx(1f)
            color = Color.argb(38, 255, 255, 255) // 15% blanc
        }
        private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = dpToPx(1.5f)
            color = Color.parseColor("#EF4444")
        }
        private val circleFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(26, 239, 68, 68) // EF4444 10%
        }
        private val xPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = dpToPx(2.5f)
            strokeCap = Paint.Cap.ROUND
            color = Color.parseColor("#FF0000")
        }
        private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun setHighlighted(highlighted: Boolean) {
            if (isHighlighted != highlighted) {
                isHighlighted = highlighted
                invalidate()
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat()
            val h = height.toFloat()
            val cornerRadius = dpToPx(16f)

            // Fond
            val bgAlpha = if (isHighlighted) 230 else 200
            bgPaint.color = Color.argb(bgAlpha, 30, 30, 30)
            val rect = RectF(0f, 0f, w, h)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)

            // Bordure
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)

            // Icône X dans un cercle (centré)
            val centerX = w / 2f
            val circleY = h * 0.45f
            val circleRadius = dpToPx(23.5f)

            // Cercle fond
            canvas.drawCircle(centerX, circleY, circleRadius, circleFillPaint)
            // Cercle bordure
            canvas.drawCircle(centerX, circleY, circleRadius, circlePaint)

            // X
            val xSize = dpToPx(6f)
            canvas.drawLine(centerX - xSize, circleY - xSize, centerX + xSize, circleY + xSize, xPaint)
            canvas.drawLine(centerX - xSize, circleY + xSize, centerX + xSize, circleY - xSize, xPaint)

            // Ligne dégradée rouge en bas
            val lineY = h - dpToPx(4f)
            val lineHeight = dpToPx(4f)
            val gradient = android.graphics.LinearGradient(
                0f, lineY, w, lineY,
                intArrayOf(
                    Color.argb(0, 239, 68, 68),
                    Color.argb(51, 239, 68, 68),
                    Color.argb(0, 239, 68, 68)
                ),
                floatArrayOf(0f, 0.5f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )
            gradientPaint.shader = gradient
            canvas.drawRect(dpToPx(1f), lineY, w - dpToPx(1f), lineY + lineHeight, gradientPaint)
        }
    }
}
