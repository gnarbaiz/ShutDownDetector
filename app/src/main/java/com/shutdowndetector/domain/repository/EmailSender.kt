package com.shutdowndetector.domain.repository

import com.shutdowndetector.domain.model.PowerEvent

interface EmailSender {
    suspend fun sendEventNotification(event: PowerEvent): Result<Unit>
    suspend fun sendMultipleEvents(events: List<PowerEvent>): Result<Unit>
}
