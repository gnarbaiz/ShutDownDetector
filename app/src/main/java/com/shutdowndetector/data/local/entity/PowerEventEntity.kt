package com.shutdowndetector.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shutdowndetector.domain.model.ChargingState
import com.shutdowndetector.domain.model.PowerState
import com.shutdowndetector.domain.model.WifiState

@Entity(tableName = "power_events")
data class PowerEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val wifiState: WifiState,
    val chargingState: ChargingState,
    val inferredPowerState: PowerState,
    val batteryLevel: Int = 0,
    val sent: Boolean = false,
    val outageDuration: Long? = null
)
