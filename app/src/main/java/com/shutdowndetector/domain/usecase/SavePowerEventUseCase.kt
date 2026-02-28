package com.shutdowndetector.domain.usecase

import com.shutdowndetector.domain.model.PowerEvent
import com.shutdowndetector.domain.repository.PowerEventRepository
import javax.inject.Inject

class SavePowerEventUseCase @Inject constructor(
    private val repository: PowerEventRepository
) {
    suspend operator fun invoke(event: PowerEvent): Long {
        return repository.saveEvent(event)
    }
}
