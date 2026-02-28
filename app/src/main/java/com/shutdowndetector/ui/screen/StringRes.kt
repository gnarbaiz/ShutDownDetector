package com.shutdowndetector.ui.screen

import com.shutdowndetector.R
import com.shutdowndetector.domain.model.ChargingState
import com.shutdowndetector.domain.model.PowerState
import com.shutdowndetector.domain.model.WifiState

fun WifiState.toLabelResId(): Int = when (this) {
    WifiState.CONNECTED -> R.string.wifi_connected
    WifiState.DISCONNECTED -> R.string.wifi_disconnected
    WifiState.UNKNOWN -> R.string.wifi_unknown
}

fun ChargingState.toLabelResId(): Int = when (this) {
    ChargingState.CHARGING -> R.string.charging_charging
    ChargingState.NOT_CHARGING -> R.string.charging_not_charging
    ChargingState.UNKNOWN -> R.string.charging_unknown
}

fun PowerState.toLabelResId(): Int = when (this) {
    PowerState.NORMAL -> R.string.power_normal
    PowerState.POSSIBLE_OUTAGE -> R.string.power_possible_outage
    PowerState.POWER_RESTORED -> R.string.power_power_restored
    PowerState.UNKNOWN -> R.string.power_unknown
}
