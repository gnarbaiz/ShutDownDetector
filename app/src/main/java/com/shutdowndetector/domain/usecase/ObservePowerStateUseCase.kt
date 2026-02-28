package com.shutdowndetector.domain.usecase

import com.shutdowndetector.domain.model.DeviceState
import kotlinx.coroutines.flow.Flow

interface ObservePowerStateUseCase {
    fun invoke(): Flow<DeviceState>
}
