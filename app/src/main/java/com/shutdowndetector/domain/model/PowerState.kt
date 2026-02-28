package com.shutdowndetector.domain.model

enum class PowerState {
    NORMAL,
    POSSIBLE_OUTAGE,
    POWER_RESTORED,
    UNKNOWN
}
