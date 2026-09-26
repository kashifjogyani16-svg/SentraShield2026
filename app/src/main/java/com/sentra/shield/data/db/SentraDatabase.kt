package com.sentra.shield.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sentra.shield.data.db.entity.AppUsageEntity
import com.sentra.shield.data.db.entity.ThreatLog

@Database(entities = [ThreatLog::class, AppUsageEntity::class], version = 2, exportSchema = false)
abstract class SentraDatabase : RoomDatabase() {
    abstract fun threatLogDao(): ThreatLogDao
    abstract fun appUsageDao(): AppUsageDao

    companion object {
        @Volatile private var INSTANCE: SentraDatabase? = null
        fun getInstance(context: Context): SentraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SentraDatabase::class.java,
                    "sentra_shield_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
