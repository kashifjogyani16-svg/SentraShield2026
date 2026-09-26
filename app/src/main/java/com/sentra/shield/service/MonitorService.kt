package com.sentra.shield.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sentra.shield.R
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
        startForeground(1001, buildNotification())
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
            
            // Yahan humne insertUsage use kiya hai
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
                WaterIslandService.showAlert(
                    this, 
                    app.packageName, 
                    label,
                    result.reasons.firstOrNull() ?: "Suspicious",
                    result.score
                )
            }
        }
    }

    private fun buildNotification(): Notification {
        val channelId = "sentra_monitor"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(channelId, "SentraShield Monitor", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("SentraShield Active")
            .setContentText("Monitoring your apps...")
            .setSmallIcon(R.drawable.ic_shield)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        job?.cancel()
        scope.cancel()
        super.onDestroy()
    }
}
