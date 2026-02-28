package com.shutdowndetector.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.shutdowndetector.MainActivity
import com.shutdowndetector.R
import com.shutdowndetector.core.util.DeviceStateProvider
import com.shutdowndetector.domain.model.DeviceState
import com.shutdowndetector.domain.model.PowerEvent
import com.shutdowndetector.domain.model.PowerState
import com.shutdowndetector.domain.repository.EmailSender
import com.shutdowndetector.domain.repository.PowerEventRepository
import com.shutdowndetector.domain.usecase.PowerEventAnalyzer
import com.shutdowndetector.domain.usecase.SavePowerEventUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PowerMonitoringService : Service() {

    @Inject
    lateinit var repository: PowerEventRepository

    @Inject
    lateinit var emailSender: EmailSender

    @Inject
    lateinit var analyzer: PowerEventAnalyzer

    @Inject
    lateinit var saveEventUseCase: SavePowerEventUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val deviceStateProvider by lazy { DeviceStateProvider(this) }
    
    private val _currentState = MutableStateFlow<DeviceState?>(null)
    private val currentState = _currentState.asStateFlow()

    private var lastKnownState: DeviceState? = null
    private var lastOutageEvent: PowerEvent? = null

    companion object {
        private const val CHANNEL_ID = "PowerMonitoringChannel"
        private const val NOTIFICATION_ID = 1
        private const val MONITORING_INTERVAL_MS = 5000L // 5 segundos
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        startMonitoring()
        Timber.d("PowerMonitoringService created and started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // Reinicia el servicio si es terminado
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun startMonitoring() {
        serviceScope.launch {
            while (true) {
                try {
                    val currentState = deviceStateProvider.getCurrentState()
                    _currentState.value = currentState

                    // Obtener último estado conocido de la DB
                    if (lastKnownState == null) {
                        val lastEvent = repository.getLastEvent()
                        if (lastEvent != null) {
                            lastKnownState = DeviceState(
                                wifiState = lastEvent.wifiState,
                                chargingState = lastEvent.chargingState,
                                batteryLevel = lastEvent.batteryLevel
                            )
                        }
                    }

                    // Analizar el estado
                    val inferredState = analyzer.analyzePowerState(currentState, lastKnownState)

                    // Si hay un cambio significativo, guardar evento
                    if (shouldSaveEvent(currentState, lastKnownState, inferredState)) {
                        val event = PowerEvent(
                            timestamp = System.currentTimeMillis(),
                            wifiState = currentState.wifiState,
                            chargingState = currentState.chargingState,
                            inferredPowerState = inferredState,
                            batteryLevel = currentState.batteryLevel
                        )

                        // Calcular duración si es restauración
                        var finalEvent = event
                        val lastOutage = lastOutageEvent
                        if (inferredState == PowerState.POWER_RESTORED && lastOutage != null) {
                            val duration = analyzer.calculateOutageDuration(lastOutage, event)
                            finalEvent = event.copy(outageDuration = duration)
                        }

                        val eventId = saveEventUseCase(finalEvent)
                        val savedEvent = finalEvent.copy(id = eventId)

                        // Si es un corte, guardar referencia
                        if (inferredState == PowerState.POSSIBLE_OUTAGE) {
                            lastOutageEvent = savedEvent
                        }

                        // Enviar notificación y email si es crítico
                        if (inferredState == PowerState.POSSIBLE_OUTAGE || inferredState == PowerState.POWER_RESTORED) {
                            sendNotification(inferredState)
                            serviceScope.launch {
                                emailSender.sendEventNotification(savedEvent)
                                    .onSuccess {
                                        repository.markEventAsSent(eventId)
                                    }
                                    .onFailure {
                                        Timber.e(it, "Failed to send email notification")
                                    }
                            }
                        }
                    }

                    lastKnownState = currentState
                    delay(MONITORING_INTERVAL_MS)
                } catch (e: Exception) {
                    Timber.e(e, "Error in monitoring loop")
                    delay(MONITORING_INTERVAL_MS)
                }
            }
        }
    }

    private fun shouldSaveEvent(
        current: DeviceState,
        last: DeviceState?,
        inferredState: PowerState
    ): Boolean {
        if (last == null) return true

        // Guardar si hay cambio en WiFi o carga
        val wifiChanged = current.wifiState != last.wifiState
        val chargingChanged = current.chargingState != last.chargingState

        // O si el estado inferido es crítico
        val isCritical = inferredState == PowerState.POSSIBLE_OUTAGE || 
                        inferredState == PowerState.POWER_RESTORED

        return wifiChanged || chargingChanged || isCritical
    }

    private fun sendNotification(powerState: PowerState) {
        val title = when (powerState) {
            PowerState.POSSIBLE_OUTAGE -> getString(R.string.notification_outage_title)
            PowerState.POWER_RESTORED -> getString(R.string.notification_restored_title)
            else -> getString(R.string.notification_state_title)
        }
        val stateLabel = when (powerState) {
            PowerState.NORMAL -> getString(R.string.power_normal)
            PowerState.POSSIBLE_OUTAGE -> getString(R.string.power_possible_outage)
            PowerState.POWER_RESTORED -> getString(R.string.power_power_restored)
            PowerState.UNKNOWN -> getString(R.string.power_unknown)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(getString(R.string.notification_state_text, stateLabel))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Timber.d("PowerMonitoringService destroyed")
    }
}
