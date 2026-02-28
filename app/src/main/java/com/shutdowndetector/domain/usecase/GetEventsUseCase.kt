package com.shutdowndetector.domain.usecase

import com.shutdowndetector.domain.model.PowerEvent
import com.shutdowndetector.domain.repository.PowerEventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEventsUseCase @Inject constructor(
    private val repository: PowerEventRepository
) {
    operator fun invoke(): Flow<List<PowerEvent>> {
        return repository.getAllEvents()
    }
}
