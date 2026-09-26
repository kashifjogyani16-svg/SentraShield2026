package com.sentra.shield.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey val packageName: String,
    val label: String,
    val rxBytes: Long,
    val txBytes: Long,
    val lastUpdated: Long = System.currentTimeMillis()
)
