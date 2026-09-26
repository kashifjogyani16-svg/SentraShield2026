package com.sentra.shield.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.*
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import androidx.core.app.NotificationCompat
import com.sentra.shield.R
import com.sentra.shield.ui.components.WaterWaveView

class WaterIslandService : Service() {
    private lateinit var windowManager: WindowManager
    private var islandView: View? = null
    private var wave: WaterWaveView? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1002, buildNotification())
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        addIslandView()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val title = it.getStringExtra("title") ?: "Alert"
            val score = it.getIntExtra("score", 0)
            showAlert(score, title)
        }
        return START_STICKY
    }

    private fun addIslandView() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = getNotchOffset()

        val density = resources.displayMetrics.density
        val container = FrameLayout(this)
        val size = (44 * density).toInt()
        wave = WaterWaveView(this)
        container.addView(wave, FrameLayout.LayoutParams(size, size))
        islandView = container
        windowManager.addView(container, params)
    }

    private fun getNotchOffset(): Int {
        val default = (30 * resources.displayMetrics.density).toInt()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val cutout = windowManager.defaultDisplay.cutout
            cutout?.safeInsetTop?.takeIf { it > 0 }?.minus((8 * resources.displayMetrics.density).toInt()) ?: default
        } else default
    }

    private fun showAlert(score: Int, title: String) {
        val color = when {
            score >= 70 -> Color.parseColor("#FF5252")
            score >= 50 -> Color.parseColor("#FFB300")
            score >= 30 -> Color.parseColor("#00B4D8")
            else -> Color.parseColor("#00E676")
        }
        wave?.setWaterColor(color)
        expand()
    }

    private fun expand() {
        islandView?.animate()?.scaleX(1f)?.scaleY(1f)
            ?.setInterpolator(OvershootInterpolator(1.2f))?.setDuration(300)?.start()
        handler.postDelayed({ collapse() }, 4000)
    }

    private fun collapse() {
        islandView?.animate()?.scaleX(0.7f)?.scaleY(0.7f)?.setDuration(250)?.start()
    }

    private fun buildNotification(): Notification {
        val channelId = "sentra_island"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(channelId, "SentraShield Island", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("SentraShield Island")
            .setSmallIcon(R.drawable.ic_shield)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        islandView?.let { windowManager.removeView(it) }
        super.onDestroy()
    }

    companion object {
        fun showAlert(context: Context, pkg: String, title: String, subtitle: String, score: Int) {
            val intent = Intent(context, WaterIslandService::class.java)
            intent.putExtra("pkg", pkg)
            intent.putExtra("title", title)
            intent.putExtra("subtitle", subtitle)
            intent.putExtra("score", score)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(intent)
            else context.startService(intent)
        }
    }
}
