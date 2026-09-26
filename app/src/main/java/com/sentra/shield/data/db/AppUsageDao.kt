package com.sentra.shield.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sentra.shield.data.db.entity.AppUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(usage: AppUsageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(usages: List<AppUsageEntity>)

    @Query("SELECT * FROM app_usage ORDER BY (rxBytes + txBytes) DESC")
    fun observeAll(): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage WHERE packageName = :packageName LIMIT 1")
    suspend fun getForPackage(packageName: String): AppUsageEntity?

    @Query("DELETE FROM app_usage")
    suspend fun clearAll()
}
