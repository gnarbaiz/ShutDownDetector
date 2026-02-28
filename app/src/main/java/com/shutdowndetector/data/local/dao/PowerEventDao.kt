package com.shutdowndetector.data.local.dao

import androidx.room.*
import com.shutdowndetector.data.local.entity.PowerEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PowerEventDao {
    @Query("SELECT * FROM power_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<PowerEventEntity>>

    @Query("SELECT * FROM power_events WHERE sent = 0 ORDER BY timestamp ASC")
    suspend fun getPendingEvents(): List<PowerEventEntity>

    @Query("SELECT * FROM power_events ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastEvent(): PowerEventEntity?

    @Query("SELECT * FROM power_events WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getEventsByDateRange(startTime: Long, endTime: Long): List<PowerEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: PowerEventEntity): Long

    @Update
    suspend fun updateEvent(event: PowerEventEntity)

    @Query("UPDATE power_events SET sent = 1 WHERE id = :eventId")
    suspend fun markAsSent(eventId: Long)
}
