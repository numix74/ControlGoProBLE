package com.actioncam.airbuble.bubble

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
import com.actioncam.airbuble.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Service Foreground affichant une bulle flottante par-dessus les autres applications.
 *
 * États visuels :
 * - DISCONNECTED    : bordure grise #4A4A4A, icône caméra grise
 * - CONNECTED       : bordure cyan #4CC4C4, icône caméra cyan, pastille verte
 * - RECORDING       : bordure rouge, texte "REC" blanc, point rouge clignotant
 * - RECORDING_TIMER : bordure rouge, timer "MM:SS" blanc, point rouge clignotant
 *
 * Interactions :
 * - Tap simple en recording → Waypoint GPS (hilight)
 * - Double tap → ouvre l'app
 * - Appui long → toggle recording
 * - Drag vers Close Zone → ferme la bulle
 */
class FloatingBubbleService : Service() {

    companion object {
        private const val CHANNEL_ID = "airbuble_bubble"
        private const val NOTIFICATION_ID = 1001

        private const val BUBBLE_WIDTH_DP  = 120f
        private const val BUBBLE_HEIGHT_DP = 70f
        private const val CLOSE_ZONE_WIDTH_DP  = 200f
        private const val CLOSE_ZONE_HEIGHT_DP = 100f

        private const val LONG_PRESS_TIMEOUT = 500L
        private const val DOUBLE_TAP_TIMEOUT = 300L

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

    // ── Notification ─────────────────────────────────────────────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "AirBuble — Bulle flottante",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Contrôle rapide Insta360"
            setShowBadge(false)
        }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("AirBuble")
            .setContentText("Bulle flottante active")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentIntent(pendingIntent)
            .build()
    }

    // ── Bubble View ──────────────────────────────────────────────────

    @SuppressLint("ClickableViewAccessibility")
    private fun createBubbleView() {
        val widthPx  = dpToPx(BUBBLE_WIDTH_DP).toInt()
        val heightPx = dpToPx(BUBBLE_HEIGHT_DP).toInt()

        bubbleView = BubbleView(this).apply {
            setOnTouchListener(BubbleTouchListener())
        }

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        bubbleParams = WindowManager.LayoutParams(
            widthPx, heightPx, layoutFlag,
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
        val widthPx  = dpToPx(CLOSE_ZONE_WIDTH_DP).toInt()
        val heightPx = dpToPx(CLOSE_ZONE_HEIGHT_DP).toInt()

        closeZoneView = CloseZoneView(this)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        closeZoneParams = WindowManager.LayoutParams(
            widthPx, heightPx, layoutFlag,
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
        closeZoneView?.animate()?.alpha(1f)?.setDuration(200)?.start()
    }

    private fun hideCloseZone() {
        if (!isCloseZoneVisible) return
        isCloseZoneVisible = false
        closeZoneView?.animate()?.alpha(0f)?.setDuration(200)?.start()
    }

    private fun isBubbleInCloseZone(): Boolean {
        val params = bubbleParams ?: return false
        val screenWidth: Int
        val screenHeight: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            screenWidth = bounds.width(); screenHeight = bounds.height()
        } else {
            @Suppress("DEPRECATION") val display = windowManager.defaultDisplay
            @Suppress("DEPRECATION") screenWidth  = display.width
            @Suppress("DEPRECATION") screenHeight = display.height
        }
        val bubbleCenterX = params.x + dpToPx(BUBBLE_WIDTH_DP)  / 2
        val bubbleCenterY = params.y + dpToPx(BUBBLE_HEIGHT_DP) / 2
        val closeZoneTop  = screenHeight - dpToPx(CLOSE_ZONE_HEIGHT_DP) - dpToPx(16f)
        val closeZoneLeft = (screenWidth  - dpToPx(CLOSE_ZONE_WIDTH_DP))  / 2
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
            while (true) { isBlinkOn = !isBlinkOn; bubbleView?.invalidate(); delay(500) }
        }
    }

    private fun stopBlinking() { blinkJob?.cancel(); isBlinkOn = true }

    // ── Touch handling ───────────────────────────────────────────────

    private var hilightFlashJob: Job? = null

    private fun triggerHilightFlash() {
        hilightFlashJob?.cancel()
        hilightFlashJob = serviceScope.launch {
            bubbleView?.setHilightFlash(true); delay(400); bubbleView?.setHilightFlash(false)
        }
    }

    private inner class BubbleTouchListener : View.OnTouchListener {
        private var initialX = 0; private var initialY = 0
        private var initialTouchX = 0f; private var initialTouchY = 0f
        private var hasMoved = false; private var longPressTriggered = false
        private var lastTapTime = 0L; private var longPressJob: Job? = null

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = bubbleParams?.x ?: 0; initialY = bubbleParams?.y ?: 0
                    initialTouchX = event.rawX; initialTouchY = event.rawY
                    hasMoved = false; isDragging = false; longPressTriggered = false
                    longPressJob = serviceScope.launch {
                        delay(LONG_PRESS_TIMEOUT)
                        if (!hasMoved) {
                            longPressTriggered = true
                            BubbleStateHolder.onRecordToggle?.invoke()
                        }
                    }
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (!isDragging && (Math.abs(dx) > 10 || Math.abs(dy) > 10)) {
                        isDragging = true; hasMoved = true
                        longPressJob?.cancel(); showCloseZone(); bubbleView?.setDragMode(true)
                    }
                    if (isDragging) {
                        val screenWidth: Int; val screenHeight: Int
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val bounds = windowManager.currentWindowMetrics.bounds
                            screenWidth = bounds.width(); screenHeight = bounds.height()
                        } else {
                            @Suppress("DEPRECATION") val display = windowManager.defaultDisplay
                            @Suppress("DEPRECATION") screenWidth  = display.width
                            @Suppress("DEPRECATION") screenHeight = display.height
                        }
                        val bw = dpToPx(BUBBLE_WIDTH_DP).toInt()
                        val bh = dpToPx(BUBBLE_HEIGHT_DP).toInt()
                        bubbleParams?.x = (initialX + dx).toInt().coerceIn(0, screenWidth - bw)
                        bubbleParams?.y = (initialY + dy).toInt().coerceIn(0, screenHeight - bh)
                        try { windowManager.updateViewLayout(bubbleView, bubbleParams) } catch (_: Exception) {}
                        closeZoneView?.setHighlighted(isBubbleInCloseZone())
                    }
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    longPressJob?.cancel()
                    if (isDragging) {
                        isDragging = false; bubbleView?.setDragMode(false); hideCloseZone()
                        if (isBubbleInCloseZone()) {
                            BubbleStateHolder.onBubbleDismissed?.invoke(); stopSelf(); return true
                        }
                    } else if (!hasMoved && !longPressTriggered) {
                        val now = System.currentTimeMillis()
                        if (now - lastTapTime < DOUBLE_TAP_TIMEOUT) {
                            lastTapTime = 0L
                            val intent = Intent(this@FloatingBubbleService, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            }
                            startActivity(intent)
                        } else {
                            lastTapTime = now
                            serviceScope.launch {
                                delay(DOUBLE_TAP_TIMEOUT)
                                if (lastTapTime == now) {
                                    lastTapTime = 0L
                                    val isRec = currentState == BubbleVisualState.RECORDING ||
                                                currentState == BubbleVisualState.RECORDING_TIMER
                                    if (isRec) {
                                        BubbleStateHolder.onHilight?.invoke()
                                        triggerHilightFlash()
                                    }
                                }
                            }
                        }
                    }
                    return true
                }
            }
            return false
        }
    }

    // ── Utility ──────────────────────────────────────────────────────

    private fun dpToPx(dp: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

    // ══════════════════════════════════════════════════════════════════
    // BubbleView — dessin Canvas
    // ══════════════════════════════════════════════════════════════════

    private inner class BubbleView(context: Context) : View(context) {

        private var inDragMode      = false
        private var showHilightFlash = false
        var isTimerModeEnabled      = false

        private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A"); style = Paint.Style.FILL
        }
        private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
        private val innerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = dpToPx(2f)
        }
        private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = dpToPx(2.5f)
            strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
        }
        private val iconFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val dotPaint  = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

        private val capsuleRect = RectF(); private val innerRect = RectF()

        fun setDragMode(drag: Boolean) { inDragMode = drag; invalidate() }
        fun setHilightFlash(flash: Boolean) { showHilightFlash = flash; invalidate() }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat(); val h = height.toFloat(); val radius = h / 2f
            capsuleRect.set(0f, 0f, w, h)
            canvas.drawRoundRect(capsuleRect, radius, radius, bgPaint)

            when (currentState) {
                BubbleVisualState.DISCONNECTED -> drawDisconnected(canvas, w, h, radius)
                BubbleVisualState.CONNECTED -> {
                    if (inDragMode) drawDragMode(canvas, w, h, radius)
                    else drawConnected(canvas, w, h, radius)
                }
                BubbleVisualState.RECORDING       -> drawRecording(canvas, w, h, radius)
                BubbleVisualState.RECORDING_TIMER -> drawRecordingTimer(canvas, w, h, radius)
            }

            if (showHilightFlash) {
                borderPaint.color = Color.parseColor("#CA8A04")
                borderPaint.strokeWidth = dpToPx(4f)
                val fi = dpToPx(1f)
                capsuleRect.set(fi, fi, w - fi, h - fi)
                canvas.drawRoundRect(capsuleRect, radius - fi, radius - fi, borderPaint)
            }
        }

        private fun drawDisconnected(canvas: Canvas, w: Float, h: Float, radius: Float) {
            borderPaint.color = Color.parseColor("#4A4A4A"); borderPaint.strokeWidth = dpToPx(2f)
            val inset = dpToPx(1f); capsuleRect.set(inset, inset, w - inset, h - inset)
            canvas.drawRoundRect(capsuleRect, radius - inset, radius - inset, borderPaint)
            iconPaint.color = Color.parseColor("#6B7280")
            val camCx = w * 0.55f; val camCy = h * 0.5f
            drawCameraIcon(canvas, camCx, camCy, dpToPx(12f), false)
            if (isTimerModeEnabled) drawTimerOverlay(canvas, camCx, camCy, Color.parseColor("#6B7280"))
        }

        private fun drawConnected(canvas: Canvas, w: Float, h: Float, radius: Float) {
            borderPaint.color = Color.parseColor("#4CC4C4"); borderPaint.strokeWidth = dpToPx(2f)
            val inset = dpToPx(1f); capsuleRect.set(inset, inset, w - inset, h - inset)
            canvas.drawRoundRect(capsuleRect, radius - inset, radius - inset, borderPaint)
            innerBorderPaint.color = Color.argb(77, 76, 196, 196)
            val ii = dpToPx(5f); innerRect.set(ii, ii, w - ii, h - ii)
            canvas.drawRoundRect(innerRect, (h - ii * 2) / 2f, (h - ii * 2) / 2f, innerBorderPaint)
            iconPaint.color = Color.parseColor("#4CC4C4")
            val camCx = w * 0.55f; val camCy = h * 0.5f
            drawCameraIcon(canvas, camCx, camCy, dpToPx(12f), false)
            if (isTimerModeEnabled) drawTimerOverlay(canvas, camCx, camCy, Color.parseColor("#4CC4C4"))
            drawIndicatorDot(canvas, w * 0.2f, h * 0.5f, Color.parseColor("#22C55E"))
        }

        private fun drawDragMode(canvas: Canvas, w: Float, h: Float, radius: Float) {
            borderPaint.color = Color.parseColor("#4CC4C4"); borderPaint.strokeWidth = dpToPx(3f)
            val inset = dpToPx(1.5f); capsuleRect.set(inset, inset, w - inset, h - inset)
            canvas.drawRoundRect(capsuleRect, radius - inset, radius - inset, borderPaint)
            innerBorderPaint.color = Color.parseColor("#4CC4C4")
            val ii = dpToPx(5f); innerRect.set(ii, ii, w - ii, h - ii)
            canvas.drawRoundRect(innerRect, (h - ii * 2) / 2f, (h - ii * 2) / 2f, innerBorderPaint)
            iconPaint.color = Color.parseColor("#4CC4C4"); iconFillPaint.color = Color.parseColor("#4CC4C4")
            val camCx = w * 0.55f; val camCy = h * 0.5f
            drawCameraIcon(canvas, camCx, camCy, dpToPx(12f), true)
            if (isTimerModeEnabled) drawTimerOverlay(canvas, camCx, camCy, Color.parseColor("#4CC4C4"))
            drawIndicatorDot(canvas, w * 0.2f, h * 0.5f, Color.parseColor("#22C55E"))
        }

        private fun drawRecording(canvas: Canvas, w: Float, h: Float, radius: Float) {
            drawRecordingBase(canvas, w, h, radius)
            if (isBlinkOn) drawIndicatorDot(canvas, w * 0.2f, h * 0.5f, Color.parseColor("#DC2626"))
            textPaint.color = Color.WHITE; textPaint.textSize = dpToPx(14f)
            canvas.drawText("REC", w * 0.6f, h * 0.5f + dpToPx(5f), textPaint)
        }

        private fun drawRecordingTimer(canvas: Canvas, w: Float, h: Float, radius: Float) {
            drawRecordingBase(canvas, w, h, radius)
            if (isBlinkOn) drawIndicatorDot(canvas, w * 0.2f, h * 0.5f, Color.parseColor("#DC2626"))
            textPaint.color = Color.WHITE; textPaint.textSize = dpToPx(13f)
            canvas.drawText(currentDisplayTime, w * 0.6f, h * 0.5f + dpToPx(5f), textPaint)
        }

        private fun drawRecordingBase(canvas: Canvas, w: Float, h: Float, radius: Float) {
            borderPaint.color = Color.parseColor("#FF0000"); borderPaint.strokeWidth = dpToPx(3f)
            val inset = dpToPx(1.5f); capsuleRect.set(inset, inset, w - inset, h - inset)
            canvas.drawRoundRect(capsuleRect, radius - inset, radius - inset, borderPaint)
            innerBorderPaint.color = Color.parseColor("#EE2222")
            val ii = dpToPx(5f); innerRect.set(ii, ii, w - ii, h - ii)
            canvas.drawRoundRect(innerRect, (h - ii * 2) / 2f, (h - ii * 2) / 2f, innerBorderPaint)
        }

        private fun drawCameraIcon(canvas: Canvas, cx: Float, cy: Float, size: Float, filled: Boolean) {
            val bodyW = size * 2f; val bodyH = size * 1.4f
            val bodyLeft = cx - bodyW / 2f; val bodyTop = cy - bodyH / 2f + size * 0.15f
            val bodyRadius = size * 0.25f
            canvas.drawRoundRect(
                RectF(bodyLeft, bodyTop, bodyLeft + bodyW, bodyTop + bodyH),
                bodyRadius, bodyRadius, iconPaint
            )
            val topW = bodyW * 0.55f; val topH = size * 0.3f
            val topLeft = cx - topW / 2f; val topTop = bodyTop - topH * 0.6f
            canvas.drawLine(topLeft, bodyTop, topLeft, topTop, iconPaint)
            canvas.drawLine(topLeft, topTop, topLeft + topW, topTop, iconPaint)
            canvas.drawLine(topLeft + topW, topTop, topLeft + topW, bodyTop, iconPaint)
            val lensRadius = size * 0.35f
            if (filled) canvas.drawCircle(cx, cy + size * 0.15f, lensRadius, iconFillPaint)
            canvas.drawCircle(cx, cy + size * 0.15f, lensRadius, iconPaint)
        }

        private fun drawIndicatorDot(canvas: Canvas, cx: Float, cy: Float, color: Int) {
            val r = dpToPx(5f)
            glowPaint.color = Color.argb(100, Color.red(color), Color.green(color), Color.blue(color))
            glowPaint.strokeWidth = dpToPx(3f)
            canvas.drawCircle(cx, cy, r + dpToPx(2f), glowPaint)
            dotPaint.color = color; canvas.drawCircle(cx, cy, r, dotPaint)
        }

        private fun drawTimerOverlay(canvas: Canvas, cameraCx: Float, cameraCy: Float, iconColor: Int) {
            val size = dpToPx(8f)
            val ox = cameraCx + dpToPx(10f); val oy = cameraCy - dpToPx(10f)
            bgPaint.color = Color.parseColor("#0F172A")
            canvas.drawCircle(ox, oy, size + dpToPx(3f), bgPaint)
            iconPaint.color = iconColor; iconPaint.strokeWidth = dpToPx(1.8f)
            canvas.drawCircle(ox, oy, size, iconPaint)
            val handLong = size * 0.65f; val handShort = size * 0.45f
            canvas.drawLine(ox, oy, ox, oy - handLong, iconPaint)
            canvas.drawLine(ox, oy, ox + handShort, oy, iconPaint)
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // CloseZoneView
    // ══════════════════════════════════════════════════════════════════

    private inner class CloseZoneView(context: Context) : View(context) {

        private var isHighlighted = false

        private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = dpToPx(1f)
            color = Color.argb(38, 255, 255, 255)
        }
        private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = dpToPx(1.5f)
            color = Color.parseColor("#EF4444")
        }
        private val circleFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(26, 239, 68, 68)
        }
        private val xPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = dpToPx(2.5f)
            strokeCap = Paint.Cap.ROUND; color = Color.parseColor("#FF0000")
        }
        private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun setHighlighted(highlighted: Boolean) {
            if (isHighlighted != highlighted) { isHighlighted = highlighted; invalidate() }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat(); val h = height.toFloat()
            val cornerRadius = dpToPx(16f)

            bgPaint.color = Color.argb(if (isHighlighted) 230 else 200, 30, 30, 30)
            val rect = RectF(0f, 0f, w, h)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)

            val centerX = w / 2f; val circleY = h * 0.45f
            val circleRadius = dpToPx(23.5f)
            canvas.drawCircle(centerX, circleY, circleRadius, circleFillPaint)
            canvas.drawCircle(centerX, circleY, circleRadius, circlePaint)

            val xSize = dpToPx(6f)
            canvas.drawLine(centerX - xSize, circleY - xSize, centerX + xSize, circleY + xSize, xPaint)
            canvas.drawLine(centerX - xSize, circleY + xSize, centerX + xSize, circleY - xSize, xPaint)

            val lineY = h - dpToPx(4f)
            gradientPaint.shader = android.graphics.LinearGradient(
                0f, lineY, w, lineY,
                intArrayOf(Color.argb(0, 239, 68, 68), Color.argb(51, 239, 68, 68), Color.argb(0, 239, 68, 68)),
                floatArrayOf(0f, 0.5f, 1f), android.graphics.Shader.TileMode.CLAMP
            )
            canvas.drawRect(dpToPx(1f), lineY, w - dpToPx(1f), lineY + dpToPx(4f), gradientPaint)
        }
    }
}
