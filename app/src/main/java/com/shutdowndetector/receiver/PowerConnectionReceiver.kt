package com.shutdowndetector.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import com.shutdowndetector.service.PowerMonitoringService
import timber.log.Timber

class PowerConnectionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        when (action) {
            Intent.ACTION_POWER_CONNECTED -> {
                Timber.d("PowerConnectionReceiver: Power connected")
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                Timber.d("PowerConnectionReceiver: Power disconnected")
            }
        }
        // Forzar verificación en el servicio
        val serviceIntent = Intent(context, PowerMonitoringService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
