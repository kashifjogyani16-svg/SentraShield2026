package com.sentra.shield.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.*
import android.view.animation.OvershootInterpolator
import android.widget.*
import androidx.core.app.NotificationCompat
import com.sentra.shield.R

class WaterIslandService : Service() {
    private lateinit var windowManager: WindowManager
    private var islandView: View? = null
    private var buddyView: ImageView? = null
    private var listContainer: LinearLayout? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isListVisible = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1002, buildNotification())
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        addBuddyView()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val title = it.getStringExtra("title") ?: "Alert"
            val score = it.getIntExtra("score", 0)
            showAlert(score, title)
        }
        return START_STICKY
    }

    private fun addBuddyView() {
        val density = resources.displayMetrics.density
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = getNotchOffset()

        // Container
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        // Buddy emoji
        buddyView = ImageView(this).apply {
            setImageResource(R.drawable.ic_buddy)
            val size = (48 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size)
            setOnClickListener {
                if (isListVisible) hideList() else showList()
            }
        }
        container.addView(buddyView)

        // List popup (initially hidden)
        listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setBackgroundColor(Color.parseColor("#EE0B0F14"))
            setPadding(24, 24, 24, 24)
            val lp = LinearLayout.LayoutParams(
                (220 * density).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = (8 * density).toInt()
            layoutParams = lp
        }
        container.addView(listContainer)

        islandView = container
        windowManager.addView(container, params)
    }

    private fun getNotchOffset(): Int {
        val density = resources.displayMetrics.density
        val default = (8 * density).toInt()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val cutout = windowManager.defaultDisplay.cutout
            val safeInset = cutout?.safeInsetTop ?: 0
            if (safeInset > 0) safeInset - (4 * density).toInt() else default
        } else default
    }

    private fun showList() {
        isListVisible = true
        listContainer?.removeAllViews()
        listContainer?.visibility = View.VISIBLE

        // Pop animation
        listContainer?.scaleX = 0.7f
        listContainer?.scaleY = 0.7f
        listContainer?.animate()?.scaleX(1f)?.scaleY(1f)
            ?.setInterpolator(OvershootInterpolator(1.2f))
            ?.setDuration(300)?.start()

        // Title
        val title = TextView(this).apply {
            text = "SentraBuddy"
            setTextColor(Color.parseColor("#00B4D8"))
            textSize = 14f
            setPadding(0, 0, 0, 12)
        }
        listContainer?.addView(title)

        // Sample list items (baad mein database se aayenge)
        val items = listOf(
            "🟢 System Safe",
            "📱 42 apps monitored",
            "⚠️ 2 suspicious apps",
            "🛡️ Antivirus active"
        )
        items.forEach { item ->
            val tv = TextView(this).apply {
                text = item
                setTextColor(Color.WHITE)
                textSize = 12f
                setPadding(0, 8, 0, 8)
            }
            listContainer?.addView(tv)
        }
    }

    private fun hideList() {
        isListVisible = false
        listContainer?.animate()?.scaleX(0.7f)?.scaleY(0.7f)
            ?.setDuration(200)
            ?.withEndAction { listContainer?.visibility = View.GONE }
            ?.start()
    }

    private fun showAlert(score: Int, title: String) {
        val color = when {
            score >= 70 -> Color.parseColor("#FF5252")
            score >= 50 -> Color.parseColor("#FFB300")
            score >= 30 -> Color.parseColor("#00B4D8")
            else -> Color.parseColor("#00E676")
        }
        // Buddy par pulse animation
        buddyView?.animate()?.scaleX(1.3f)?.scaleY(1.3f)?.setDuration(200)
            ?.withEndAction {
                buddyView?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(200)?.start()
            }?.start()
        showList()
    }

    private fun buildNotification(): Notification {
        val channelId = "sentra_island"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(channelId, "SentraShield Island", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("SentraShield Buddy")
            .setContentText("Buddy is active")
            .setSmallIcon(R.drawable.ic_shield)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        islandView?.let { try { windowManager.removeView(it) } catch (e: Exception) {} }
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

        fun start(context: Context) {
            val intent = Intent(context, WaterIslandService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(intent)
            else context.startService(intent)
        }
    }
}
