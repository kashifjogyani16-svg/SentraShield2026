package com.sentra.shield.util

import android.app.usage.UsageStatsManager
import android.content.Context

object ActivityTracker {
    fun getForegroundApp(context: Context): String? {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 1000 * 60 * 5
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginTime, endTime)
        if (stats.isNullOrEmpty()) return null
        var recentApp: String? = null
        var lastTime = 0L
        for (stat in stats) {
            if (stat.lastTimeUsed > lastTime) {
                lastTime = stat.lastTimeUsed
                recentApp = stat.packageName
            }
        }
        return recentApp
    }

    fun hasUsageAccess(context: Context): Boolean {
        return try {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val now = System.currentTimeMillis()
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 1000 * 60, now)
            !stats.isNullOrEmpty()
        } catch (e: Exception) { false }
    }
}
