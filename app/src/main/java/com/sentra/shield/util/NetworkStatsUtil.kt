package com.sentra.shield.util

import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Process
import android.telephony.TelephonyManager

/** Wraps [NetworkStatsManager] to return per-uid rx/tx totals for a rolling window. */
object NetworkStatsUtil {

    data class UsageBucket(val rxBytes: Long, val txBytes: Long)

    /**
     * Returns whether the app has been granted the special "Usage access"
     * (PACKAGE_USAGE_STATS) permission, required for [queryUsageForUid].
     */
    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Sums mobile + wifi rx/tx bytes for [uid] over [startMillis]..[endMillis].
     * Returns null if usage access isn't granted or stats aren't available.
     */
    fun queryUsageForUid(
        context: Context,
        uid: Int,
        startMillis: Long,
        endMillis: Long
    ): UsageBucket? {
        if (!hasUsageAccess(context)) return null

        val statsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE)
            as? NetworkStatsManager ?: return null

        var rx = 0L
        var tx = 0L

        for (networkType in listOf(ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_WIFI)) {
            try {
                val subscriberId = null // wifi/general query, no subscriber id needed
                val bucket = NetworkStats.Bucket()
                val stats = statsManager.queryDetailsForUid(
                    networkType,
                    subscriberId,
                    startMillis,
                    endMillis,
                    uid
                )
                while (stats.hasNextBucket()) {
                    stats.getNextBucket(bucket)
                    rx += bucket.rxBytes
                    tx += bucket.txBytes
                }
                stats.close()
            } catch (e: Exception) {
                // Some OEMs / network types throw for uninstrumented interfaces — skip.
            }
        }
        return UsageBucket(rx, tx)
    }
}
