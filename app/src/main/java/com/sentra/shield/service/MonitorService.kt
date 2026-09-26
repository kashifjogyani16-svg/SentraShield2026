package com.sentra.shield.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sentra.shield.R
import com.sentra.shield.ThreatDetailActivity
import com.sentra.shield.data.db.SentraDatabase
import com.sentra.shield.data.db.entity.AppUsageEntity
import com.sentra.shield.data.db.entity.ThreatLog
import com.sentra.shield.util.NetworkStatsUtil
import com.sentra.shield.util.ThreatAnalyzer
import kotlinx.coroutines.*

class MonitorService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var job: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannels()
        startForeground(1001, buildMonitorNotification())
        job = scope.launch {
            while (isActive) {
                scanApps()
                delay(60_000)
            }
        }
    }

    private suspend fun scanApps() {
        val pm = packageManager
        val db = SentraDatabase.getInstance(this)
        val apps = pm.getInstalledApplications(0)

        for (app in apps) {
            val result = ThreatAnalyzer.analyze(this, app.packageName)
            val usage = NetworkStatsUtil.getUsage(this, app.packageName)
            val label = pm.getApplicationLabel(app).toString()

            db.appUsageDao().insertUsage(
                AppUsageEntity(app.packageName, label, usage.first, usage.second)
            )

            if (result.score > 30) {
                db.threatLogDao().insert(
                    ThreatLog(
                        packageName = app.packageName,
                        appLabel = label,
                        riskScore = result.score,
                        reason = result.reasons.joinToString(", ")
                    )
                )

                // ✅ iPhone-style notification
                showThreatNotification(
                    app.packageName, label,
                    result.score, result.reasons.firstOrNull() ?: "Suspicious activity",
                    usage.first, usage.second
                )

                // ✅ Water Island bhi update karein
                WaterIslandService.showAlert(
                    this, app.packageName, label,
                    result.reasons.firstOrNull() ?: "Suspicious",
                    result.score
                )
            }
        }
    }

    private fun showThreatNotification(
        packageName: String, appLabel: String,
        riskScore: Int, reason: String,
        rxBytes: Long, txBytes: Long
    ) {
        // Click par ThreatDetailActivity khulegi
        val intent = Intent(this, ThreatDetailActivity::class.java).apply {
            putExtra("appLabel", appLabel)
            putExtra("packageName", packageName)
            putExtra("riskScore", riskScore)
            putExtra("reason", reason)
            putExtra("rxBytes", rxBytes)
            putExtra("txBytes", txBytes)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            packageName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Risk ke hisaab se emoji aur color
        val (emoji, color) = when {
            riskScore >= 70 -> "😱" to 0xFFFF5252.toInt()
            riskScore >= 50 -> "🥺" to 0xFFFFB300.toInt()
            riskScore >= 30 -> "🙂" to 0xFF00B4D8.toInt()
            else -> "🥳" to 0xFF00E676.toInt()
        }

        val builder = NotificationCompat.Builder(this, "sentra_threat")
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle("$emoji $appLabel")
            .setContentText("Risk: $riskScore/100 • $reason")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Risk Score: $riskScore/100\n" +
                        "Reason: $reason\n" +
                        "Data Sent ↑: ${txBytes / 1024} KB\n" +
                        "Data Received ↓: ${rxBytes / 1024} KB\n\n" +
                        "Tap karein poori details ke liye"
                    )
            )
            .setColor(color)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // ✅ Har app ka apna notification ID (baar baar ek hi nahi aayega)
        nm.notify(packageName.hashCode(), builder.build())
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val monitorChannel = NotificationChannel(
                "sentra_monitor", "SentraShield Monitor",
                NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(monitorChannel)

            val threatChannel = NotificationChannel(
                "sentra_threat", "Threat Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Suspicious app alerts"
                enableVibration(true)
            }
            nm.createNotificationChannel(threatChannel)
        }
    }

    private fun buildMonitorNotification(): Notification {
        return NotificationCompat.Builder(this, "sentra_monitor")
            .setContentTitle("🛡️ SentraShield Active")
            .setContentText("Monitoring your apps...")
            .setSmallIcon(R.drawable.ic_shield)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        job?.cancel()
        scope.cancel()
        super.onDestroy()
    }
}
