package com.shutdowndetector.data.local.database

import androidx.room.TypeConverter
import com.shutdowndetector.domain.model.ChargingState
import com.shutdowndetector.domain.model.PowerState
import com.shutdowndetector.domain.model.WifiState

class Converters {
    @TypeConverter
    fun fromWifiState(value: WifiState): String {
        return value.name
    }

    @TypeConverter
    fun toWifiState(value: String): WifiState {
        return WifiState.valueOf(value)
    }

    @TypeConverter
    fun fromChargingState(value: ChargingState): String {
        return value.name
    }

    @TypeConverter
    fun toChargingState(value: String): ChargingState {
        return ChargingState.valueOf(value)
    }

    @TypeConverter
    fun fromPowerState(value: PowerState): String {
        return value.name
    }

    @TypeConverter
    fun toPowerState(value: String): PowerState {
        return PowerState.valueOf(value)
    }
}
