package com.sentra.shield.data.model

/** Lightweight snapshot of an installed app used across the UI + monitor pipeline. */
data class AppInfo(
    val packageName: String,
    val label: String,
    val permissions: List<String>,
    val isSystemApp: Boolean,
    val isDebuggable: Boolean,
    val installerPackageName: String?,
    val rxBytes: Long = 0L,
    val txBytes: Long = 0L,
    val riskScore: Int = 0
)
