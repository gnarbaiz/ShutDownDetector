package com.shutdowndetector.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import com.shutdowndetector.service.PowerMonitoringService
import timber.log.Timber

class ConnectivityReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("ConnectivityReceiver: Connectivity changed")
        // El servicio monitorea continuamente, pero podemos forzar una verificación
        val serviceIntent = Intent(context, PowerMonitoringService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
