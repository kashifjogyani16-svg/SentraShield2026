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
    suspend fun insertUsage(entity: AppUsageEntity)

    @Query("SELECT * FROM app_usage ORDER BY rxBytes DESC")
    fun getAllUsage(): Flow<List<AppUsageEntity>>
}
