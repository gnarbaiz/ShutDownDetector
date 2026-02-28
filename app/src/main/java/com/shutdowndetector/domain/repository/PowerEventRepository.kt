package com.shutdowndetector.domain.repository

import com.shutdowndetector.domain.model.PowerEvent
import kotlinx.coroutines.flow.Flow

interface PowerEventRepository {
    suspend fun saveEvent(event: PowerEvent): Long
    fun getAllEvents(): Flow<List<PowerEvent>>
    suspend fun getPendingEvents(): List<PowerEvent>
    suspend fun markEventAsSent(eventId: Long)
    suspend fun getLastEvent(): PowerEvent?
    suspend fun getEventsByDateRange(startTime: Long, endTime: Long): List<PowerEvent>
}
