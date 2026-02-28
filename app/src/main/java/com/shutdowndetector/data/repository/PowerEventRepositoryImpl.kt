package com.shutdowndetector.data.repository

import com.shutdowndetector.data.local.dao.PowerEventDao
import com.shutdowndetector.data.local.mapper.PowerEventMapper
import com.shutdowndetector.domain.model.PowerEvent
import com.shutdowndetector.domain.repository.PowerEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PowerEventRepositoryImpl @Inject constructor(
    private val dao: PowerEventDao
) : PowerEventRepository {
    override suspend fun saveEvent(event: PowerEvent): Long {
        return dao.insertEvent(PowerEventMapper.toEntity(event))
    }

    override fun getAllEvents(): Flow<List<PowerEvent>> {
        return dao.getAllEvents().map { entities ->
            entities.map { PowerEventMapper.toDomain(it) }
        }
    }

    override suspend fun getPendingEvents(): List<PowerEvent> {
        return dao.getPendingEvents().map { PowerEventMapper.toDomain(it) }
    }

    override suspend fun markEventAsSent(eventId: Long) {
        dao.markAsSent(eventId)
    }

    override suspend fun getLastEvent(): PowerEvent? {
        return dao.getLastEvent()?.let { PowerEventMapper.toDomain(it) }
    }

    override suspend fun getEventsByDateRange(startTime: Long, endTime: Long): List<PowerEvent> {
        return dao.getEventsByDateRange(startTime, endTime).map { PowerEventMapper.toDomain(it) }
    }
}
