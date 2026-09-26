package com.sentra.shield.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "threat_logs")
data class ThreatLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appLabel: String,
    val riskScore: Int,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)
