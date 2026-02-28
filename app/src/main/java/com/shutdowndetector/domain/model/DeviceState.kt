package com.shutdowndetector.domain.model

data class DeviceState(
    val wifiState: WifiState,
    val chargingState: ChargingState,
    val batteryLevel: Int
)
