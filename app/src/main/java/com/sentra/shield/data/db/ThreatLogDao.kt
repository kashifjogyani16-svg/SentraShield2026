package com.sentra.shield.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sentra.shield.data.db.entity.ThreatLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreatLogDao {
    @Insert
    suspend fun insert(log: ThreatLog)

    @Query("SELECT * FROM threat_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ThreatLog>>
}
