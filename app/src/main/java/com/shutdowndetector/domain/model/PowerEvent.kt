package com.shutdowndetector.domain.model

data class PowerEvent(
    val id: Long = 0,
    val timestamp: Long,
    val wifiState: WifiState,
    val chargingState: ChargingState,
    val inferredPowerState: PowerState,
    val batteryLevel: Int = 0,
    val sent: Boolean = false,
    val outageDuration: Long? = null // Duración en milisegundos si es POWER_RESTORED
)
