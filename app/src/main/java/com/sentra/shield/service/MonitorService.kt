package com.sentra.shield.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sentra.shield.MainActivity
import com.sentra.shield.R
import com.sentra.shield.SentraApp
import com.sentra.shield.data.db.SentraDatabase
import com.sentra.shield.data.db.entity.AppUsageEntity
import com.sentra.shield.data.db.entity.ThreatLog
import com.sentra.shield.util.NetworkStatsUtil
import com.sentra.shield.util.ThreatAnalyzer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Foreground service that periodically scans installed apps for network usage
 * and heuristic threat score, persists results to Room, and notifies
 * [WaterIslandService] when a high-risk app is found.
 */
class MonitorService : Service() {

    private val serviceJob = Job()
    private val scope = CoroutineScope(Dispatchers.Default + serviceJob)
    private lateinit var db: SentraDatabase

    private var running = false

    override fun onCreate() {
        super.onCreate()
        db = SentraDatabase.getInstance(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        if (!running) {
            running = true
            startMonitorLoop()
        }
        return START_STICKY
    }

    private fun startMonitorLoop() {
        scope.launch {
            while (running) {
                try {
                    scanInstalledApps()
                } catch (e: Exception) {
                    // Swallow per-cycle errors so the loop keeps running.
                }
                delay(SCAN_INTERVAL_MS)
            }
        }
    }

    private suspend fun scanInstalledApps() {
        val pm = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val now = System.currentTimeMillis()
        val windowStart = now - SCAN_INTERVAL_MS

        val usageEntities = mutableListOf<AppUsageEntity>()

        for (appInfo: ApplicationInfo in apps) {
            val packageName = appInfo.packageName
            val label = try {
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                packageName
            }

            val usage = NetworkStatsUtil.queryUsageForUid(
                applicationContext, appInfo.uid, windowStart, now
            )
            if (usage != null && (usage.rxBytes > 0 || usage.txBytes > 0)) {
                usageEntities.add(
                    AppUsageEntity(
                        packageName = packageName,
                        label = label,
                        rxBytes = usage.rxBytes,
                        txBytes = usage.txBytes,
                        lastUpdated = now
                    )
                )
            }

            val threat = ThreatAnalyzer.analyze(applicationContext, packageName)
            if (threat.score > 0) {
                db.threatLogDao().insert(
                    ThreatLog(
                        packageName = packageName,
                        appLabel = label,
                        riskScore = threat.score,
                        reason = threat.reasons.joinToString("; "),
                        timestamp = now
                    )
                )
            }

            if (threat.score > ALERT_THRESHOLD) {
                notifyIsland(packageName, label, threat.score)
            }
        }

        if (usageEntities.isNotEmpty()) {
            db.appUsageDao().upsertAll(usageEntities)
        }
    }

    private fun notifyIsland(packageName: String, label: String, score: Int) {
        val intent = Intent(applicationContext, WaterIslandService::class.java).apply {
            action = WaterIslandService.ACTION_SHOW_ALERT
            putExtra(WaterIslandService.EXTRA_PACKAGE, packageName)
            putExtra(WaterIslandService.EXTRA_TITLE, label)
            putExtra(WaterIslandService.EXTRA_SUBTITLE, "Risk score: $score")
            putExtra(WaterIslandService.EXTRA_SCORE, score)
        }
        try {
            androidx.core.content.ContextCompat.startForegroundService(applicationContext, intent)
        } catch (e: Exception) {
            // Overlay permission likely not granted yet — ignore.
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, SentraApp.CHANNEL_MONITOR)
            .setContentTitle(getString(R.string.monitor_notification_title))
            .setContentText(getString(R.string.monitor_notification_text))
            .setSmallIcon(R.drawable.ic_shield)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        running = false
        serviceJob.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val SCAN_INTERVAL_MS = 60_000L
        private const val ALERT_THRESHOLD = 30
    }
}
