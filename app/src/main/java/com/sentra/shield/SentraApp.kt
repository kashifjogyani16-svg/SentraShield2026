package com.sentra.shield

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class SentraApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)

        val monitorChannel = NotificationChannel(
            CHANNEL_MONITOR,
            "Network Monitoring",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Persistent notification shown while SentraShield monitors network traffic"
        }

        val alertChannel = NotificationChannel(
            CHANNEL_ALERTS,
            "Threat Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "High-priority alerts for detected threats"
        }

        manager.createNotificationChannel(monitorChannel)
        manager.createNotificationChannel(alertChannel)
    }

    companion object {
        const val CHANNEL_MONITOR = "sentra_monitor_channel"
        const val CHANNEL_ALERTS = "sentra_alerts_channel"
    }
}
