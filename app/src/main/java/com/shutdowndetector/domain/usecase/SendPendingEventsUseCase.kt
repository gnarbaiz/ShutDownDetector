package com.shutdowndetector.domain.usecase

import com.shutdowndetector.domain.repository.EmailSender
import com.shutdowndetector.domain.repository.PowerEventRepository
import javax.inject.Inject

class SendPendingEventsUseCase @Inject constructor(
    private val repository: PowerEventRepository,
    private val emailSender: EmailSender
) {
    suspend operator fun invoke(): Result<Int> {
        return try {
            val pendingEvents = repository.getPendingEvents()
            if (pendingEvents.isEmpty()) {
                return Result.success(0)
            }

            val result = emailSender.sendMultipleEvents(pendingEvents)
            if (result.isSuccess) {
                pendingEvents.forEach { event ->
                    repository.markEventAsSent(event.id)
                }
                Result.success(pendingEvents.size)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to send events"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
