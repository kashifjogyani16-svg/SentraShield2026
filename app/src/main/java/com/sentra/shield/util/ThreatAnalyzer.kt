package com.sentra.shield.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.sentra.shield.data.model.ThreatScore

/**
 * Pure heuristic risk-scoring engine. No network calls — everything here is
 * derived from PackageManager data already on-device.
 */
object ThreatAnalyzer {

    /** Permissions considered high-risk if held by a non-system app. */
    val HIGH_RISK_PERMISSIONS = listOf(
        "android.permission.READ_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.READ_CALL_LOG",
        "android.permission.RECORD_AUDIO",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.READ_CONTACTS",
        "android.permission.SYSTEM_ALERT_WINDOW",
        "android.permission.REQUEST_INSTALL_PACKAGES"
    )

    fun analyze(context: Context, packageName: String): ThreatScore {
        val pm = context.packageManager
        var score = 0
        val reasons = mutableListOf<String>()

        val packageInfo = try {
            pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        } catch (e: PackageManager.NameNotFoundException) {
            return ThreatScore(0, listOf("Package not found"))
        }

        val appInfo = packageInfo.applicationInfo
        val requestedPermissions = packageInfo.requestedPermissions?.toList().orEmpty()

        // Rule 1: 3+ high risk permissions
        val heldHighRisk = requestedPermissions.filter { it in HIGH_RISK_PERMISSIONS }
        if (heldHighRisk.size >= 3) {
            score += 30
            reasons.add("Holds ${heldHighRisk.size} high-risk permissions (${heldHighRisk.joinToString { it.substringAfterLast('.') }})")
        }

        // Rule 2: INTERNET + SEND_SMS combo
        val hasInternet = requestedPermissions.contains("android.permission.INTERNET")
        val hasSendSms = requestedPermissions.contains("android.permission.SEND_SMS")
        if (hasInternet && hasSendSms) {
            score += 40
            reasons.add("Requests INTERNET + SEND_SMS (possible SMS-fraud combo)")
        }

        // Rule 3: OVERLAY + ACCESSIBILITY combo
        val hasOverlay = requestedPermissions.contains("android.permission.SYSTEM_ALERT_WINDOW")
        val hasAccessibility = requestedPermissions.contains("android.permission.BIND_ACCESSIBILITY_SERVICE")
        if (hasOverlay && hasAccessibility) {
            score += 50
            reasons.add("Requests SYSTEM_ALERT_WINDOW + Accessibility Service (overlay/keylogging risk)")
        }

        // Rule 4: sideloaded from non-Play-Store
        val installer = try {
            @Suppress("DEPRECATION")
            pm.getInstallerPackageName(packageName)
        } catch (e: Exception) {
            null
        }
        val trustedInstallers = setOf(
            "com.android.vending",
            "com.google.android.packageinstaller",
            "com.android.packageinstaller"
        )
        if (appInfo != null &&
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 &&
            (installer == null || installer !in trustedInstallers)
        ) {
            score += 15
            reasons.add("Sideloaded (installer: ${installer ?: "unknown"})")
        }

        // Rule 5: debuggable build
        if (appInfo != null && (appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            score += 20
            reasons.add("App is built with debuggable=true")
        }

        val finalScore = score.coerceIn(0, 100)
        if (reasons.isEmpty()) {
            reasons.add("No risk signals detected")
        }
        return ThreatScore(finalScore, reasons)
    }
}
