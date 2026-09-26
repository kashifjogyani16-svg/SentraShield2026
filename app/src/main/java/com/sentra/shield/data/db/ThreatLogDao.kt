package com.sentra.shield.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sentra.shield.data.db.entity.ThreatLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreatLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ThreatLog): Long

    @Query("SELECT * FROM threat_logs ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<ThreatLog>>

    @Query("SELECT * FROM threat_logs WHERE packageName = :packageName ORDER BY timestamp DESC")
    fun observeForPackage(packageName: String): Flow<List<ThreatLog>>

    @Query("SELECT * FROM threat_logs WHERE riskScore >= :minScore ORDER BY timestamp DESC")
    fun observeAboveScore(minScore: Int): Flow<List<ThreatLog>>

    @Query("DELETE FROM threat_logs WHERE timestamp < :olderThan")
    suspend fun purgeOlderThan(olderThan: Long)

    @Query("DELETE FROM threat_logs")
    suspend fun clearAll()
}
