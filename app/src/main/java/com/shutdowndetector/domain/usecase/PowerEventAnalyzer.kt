package com.shutdowndetector.domain.usecase

import com.shutdowndetector.domain.model.ChargingState
import com.shutdowndetector.domain.model.DeviceState
import com.shutdowndetector.domain.model.PowerState
import com.shutdowndetector.domain.model.WifiState
import com.shutdowndetector.domain.repository.PowerEventRepository
import javax.inject.Inject

class PowerEventAnalyzer @Inject constructor(
    private val repository: PowerEventRepository
) {
    suspend fun analyzePowerState(
        currentState: DeviceState,
        lastState: DeviceState?
    ): PowerState {
        val wifiDisconnected = currentState.wifiState == WifiState.DISCONNECTED
        val notCharging = currentState.chargingState == ChargingState.NOT_CHARGING
        val wifiConnected = currentState.wifiState == WifiState.CONNECTED
        val charging = currentState.chargingState == ChargingState.CHARGING

        // Si no hay estado previo, solo podemos inferir basado en el estado actual
        if (lastState == null) {
            return when {
                wifiDisconnected && notCharging -> PowerState.POSSIBLE_OUTAGE
                wifiConnected && charging -> PowerState.NORMAL
                else -> PowerState.UNKNOWN
            }
        }

        val wasWifiConnected = lastState.wifiState == WifiState.CONNECTED
        val wasCharging = lastState.chargingState == ChargingState.CHARGING

        // Detectar posible corte: WiFi se desconecta Y deja de cargar
        val wifiJustDisconnected = wasWifiConnected && wifiDisconnected
        val chargingJustStopped = wasCharging && notCharging

        if (wifiJustDisconnected && chargingJustStopped) {
            return PowerState.POSSIBLE_OUTAGE
        }

        // Detectar restauración: WiFi se conecta Y vuelve a cargar
        val wifiJustConnected = !wasWifiConnected && wifiConnected
        val chargingJustStarted = !wasCharging && charging

        if (wifiJustConnected && chargingJustStarted) {
            return PowerState.POWER_RESTORED
        }

        // Si ambos están conectados/cargando, estado normal
        if (wifiConnected && charging) {
            return PowerState.NORMAL
        }

        // Si ambos están desconectados/no cargando, posible corte
        if (wifiDisconnected && notCharging) {
            return PowerState.POSSIBLE_OUTAGE
        }

        return PowerState.UNKNOWN
    }

    suspend fun calculateOutageDuration(
        outageEvent: com.shutdowndetector.domain.model.PowerEvent,
        restoredEvent: com.shutdowndetector.domain.model.PowerEvent
    ): Long {
        return restoredEvent.timestamp - outageEvent.timestamp
    }
}
