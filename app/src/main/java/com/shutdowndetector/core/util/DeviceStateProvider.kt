package com.shutdowndetector.core.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import com.shutdowndetector.domain.model.ChargingState
import com.shutdowndetector.domain.model.DeviceState
import com.shutdowndetector.domain.model.WifiState

class DeviceStateProvider(private val context: Context) {
    
    fun getCurrentState(): DeviceState {
        val wifiState = getWifiState()
        val chargingState = getChargingState()
        val batteryLevel = getBatteryLevel()
        
        return DeviceState(
            wifiState = wifiState,
            chargingState = chargingState,
            batteryLevel = batteryLevel
        )
    }

    private fun getWifiState(): WifiState {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return WifiState.DISCONNECTED
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return WifiState.DISCONNECTED
        
        return if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            WifiState.CONNECTED
        } else {
            WifiState.DISCONNECTED
        }
    }

    private fun getChargingState(): ChargingState {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val isCharging = batteryManager.isCharging
        
        return if (isCharging) {
            ChargingState.CHARGING
        } else {
            ChargingState.NOT_CHARGING
        }
    }

    private fun getBatteryLevel(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
}
