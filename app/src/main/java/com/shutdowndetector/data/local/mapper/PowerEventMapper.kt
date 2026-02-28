package com.shutdowndetector.data.local.mapper

import com.shutdowndetector.data.local.entity.PowerEventEntity
import com.shutdowndetector.domain.model.PowerEvent

object PowerEventMapper {
    fun toDomain(entity: PowerEventEntity): PowerEvent {
        return PowerEvent(
            id = entity.id,
            timestamp = entity.timestamp,
            wifiState = entity.wifiState,
            chargingState = entity.chargingState,
            inferredPowerState = entity.inferredPowerState,
            batteryLevel = entity.batteryLevel,
            sent = entity.sent,
            outageDuration = entity.outageDuration
        )
    }

    fun toEntity(domain: PowerEvent): PowerEventEntity {
        return PowerEventEntity(
            id = domain.id,
            timestamp = domain.timestamp,
            wifiState = domain.wifiState,
            chargingState = domain.chargingState,
            inferredPowerState = domain.inferredPowerState,
            batteryLevel = domain.batteryLevel,
            sent = domain.sent,
            outageDuration = domain.outageDuration
        )
    }
}
