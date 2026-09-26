package com.sentra.shield.util

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager

object NetworkStatsUtil {
    fun getUsage(context: Context, packageName: String): Pair<Long, Long> {
        return try {
            val nsm = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
            val now = System.currentTimeMillis()
            val start = now - 24 * 60 * 60 * 1000
            val uid = context.packageManager.getApplicationInfo(packageName, 0).uid
            var rx = 0L
            var tx = 0L
            val stats: NetworkStats = nsm.queryDetailsForUid(
                ConnectivityManager.TYPE_WIFI, null, start, now, uid
            )
            val bucket = NetworkStats.Bucket()
            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket)
                rx += bucket.rxBytes
                tx += bucket.txBytes
            }
            Pair(rx, tx)
        } catch (e: Exception) { 
            Pair(0L, 0L) 
        }
    }
}
