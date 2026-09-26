package com.sentra.shield.service

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.sentra.shield.MainActivity
import com.sentra.shield.R
import com.sentra.shield.SentraApp
import com.sentra.shield.ui.components.WaterWaveView

/**
 * Foreground service that draws the iPhone-style "Dynamic Island" water-wave
 * overlay near the display cutout, and animates it between a small collapsed
 * pill and an expanded alert pill.
 */
class WaterIslandService : Service() {

    private lateinit var windowManager: WindowManager
    private var rootView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private var collapsedContainer: View? = null
    private var expandedContainer: View? = null
    private var collapsedWaterView: WaterWaveView? = null
    private var expandedWaterView: WaterWaveView? = null
    private var titleView: TextView? = null
    private var subtitleView: TextView? = null
    private var statusDot: View? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private val autoCollapseRunnable = Runnable { collapse() }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        inflateOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())

        when (intent?.action) {
            ACTION_SHOW_ALERT -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE).orEmpty()
                val title = intent.getStringExtra(EXTRA_TITLE) ?: packageName
                val subtitle = intent.getStringExtra(EXTRA_SUBTITLE).orEmpty()
                val score = intent.getIntExtra(EXTRA_SCORE, 0)
                showAlert(packageName, title, subtitle, score)
            }
            ACTION_COLLAPSE -> collapse()
        }
        return START_NOT_STICKY
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun inflateOverlay() {
        if (rootView != null) return
        if (!android.provider.Settings.canDrawOverlays(this)) return

        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dynamic_island_water, null)
        rootView = view

        collapsedContainer = view.findViewById(com.sentra.shield.R.id.collapsedContainer)
        expandedContainer = view.findViewById(com.sentra.shield.R.id.expandedContainer)
        collapsedWaterView = view.findViewById(com.sentra.shield.R.id.collapsedWaterView)
        expandedWaterView = view.findViewById(com.sentra.shield.R.id.expandedWaterView)
        titleView = view.findViewById(com.sentra.shield.R.id.islandTitle)
        subtitleView = view.findViewById(com.sentra.shield.R.id.islandSubtitle)
        statusDot = view.findViewById(com.sentra.shield.R.id.islandStatusDot)

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN_ABOVE_STATUS_BAR,
            PixelFormat.TRANSLUCENT
        )

        val notchInset = getNotchTopInset()
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = (notchInset - dpToPx(8)).coerceAtLeast(dpToPx(4))
        layoutParams = params

        try {
            windowManager.addView(view, params)
        } catch (e: Exception) {
            rootView = null
        }
    }

    /** Reads the notch/cutout top inset from the current display; falls back to 30dp. */
    private fun getNotchTopInset(): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val metrics = windowManager.currentWindowMetrics
                val cutout = metrics.windowInsets.displayCutout
                cutout?.safeInsetTop ?: dpToPx(30)
            } else {
                dpToPx(30)
            }
        } catch (e: Exception) {
            dpToPx(30)
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    fun showAlert(packageName: String, title: String, subtitle: String, score: Int) {
        if (rootView == null) inflateOverlay()
        val color = colorForScore(score)

        titleView?.text = title
        subtitleView?.text = subtitle
        statusDot?.background?.setTint(color)
        collapsedWaterView?.setWaterColor(color)
        expandedWaterView?.setWaterColor(color)
        collapsedWaterView?.setWaterLevel(0.6f)
        expandedWaterView?.setWaterLevel(0.6f)

        expand()

        mainHandler.removeCallbacks(autoCollapseRunnable)
        mainHandler.postDelayed(autoCollapseRunnable, AUTO_COLLAPSE_MS)
    }

    private fun colorForScore(score: Int): Int = when {
        score >= 70 -> Color.parseColor("#FF5252") // danger
        score >= 40 -> Color.parseColor("#FFB300") // warning
        score >= 15 -> Color.parseColor("#00B4D8") // info
        else -> Color.parseColor("#00E676") // safe
    }

    private fun expand() {
        val collapsed = collapsedContainer ?: return
        val expanded = expandedContainer ?: return

        expanded.visibility = View.VISIBLE
        expanded.scaleX = 0.7f
        expanded.scaleY = 0.7f
        expanded.alpha = 0f
        collapsed.visibility = View.GONE

        val scaleX = ObjectAnimator.ofFloat(expanded, View.SCALE_X, 0.7f, 1f)
        val scaleY = ObjectAnimator.ofFloat(expanded, View.SCALE_Y, 0.7f, 1f)
        val alpha = ObjectAnimator.ofFloat(expanded, View.ALPHA, 0f, 1f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 380
            interpolator = OvershootInterpolator(1.2f)
            start()
        }
    }

    private fun collapse() {
        val collapsed = collapsedContainer ?: return
        val expanded = expandedContainer ?: return
        if (expanded.visibility != View.VISIBLE) return

        val scaleX = ObjectAnimator.ofFloat(expanded, View.SCALE_X, 1f, 0.7f)
        val scaleY = ObjectAnimator.ofFloat(expanded, View.SCALE_Y, 1f, 0.7f)
        val alpha = ObjectAnimator.ofFloat(expanded, View.ALPHA, 1f, 0f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 260
            interpolator = OvershootInterpolator(1f)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    expanded.visibility = View.GONE
                    collapsed.visibility = View.VISIBLE
                }
            })
            start()
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, SentraApp.CHANNEL_ALERTS)
            .setContentTitle("SentraShield Dynamic Island")
            .setContentText("Threat alert overlay is active")
            .setSmallIcon(R.drawable.ic_shield)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(autoCollapseRunnable)
        rootView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) { /* already removed */ }
        }
        rootView = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 1002
        private const val AUTO_COLLAPSE_MS = 4000L

        const val ACTION_SHOW_ALERT = "com.sentra.shield.action.SHOW_ALERT"
        const val ACTION_COLLAPSE = "com.sentra.shield.action.COLLAPSE"
        const val EXTRA_PACKAGE = "extra_package"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_SUBTITLE = "extra_subtitle"
        const val EXTRA_SCORE = "extra_score"
    }
}
