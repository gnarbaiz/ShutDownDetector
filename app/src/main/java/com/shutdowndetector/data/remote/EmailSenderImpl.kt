package com.shutdowndetector.data.remote

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.shutdowndetector.R
import com.shutdowndetector.domain.model.PowerEvent
import com.shutdowndetector.domain.model.PowerState
import com.shutdowndetector.domain.model.WifiState
import com.shutdowndetector.domain.model.ChargingState
import com.shutdowndetector.domain.repository.EmailSender
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import javax.inject.Inject
import androidx.core.net.toUri

class EmailSenderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : EmailSender {

    private val toEmail: String = "gonzalonarbaiz@gmail.com"

    override suspend fun sendEventNotification(event: PowerEvent): Result<Unit> {
        return try {
            val subject = when (event.inferredPowerState) {
                PowerState.POSSIBLE_OUTAGE -> context.getString(R.string.email_subject_outage)
                PowerState.POWER_RESTORED -> context.getString(R.string.email_subject_restored)
                else -> context.getString(R.string.email_subject_event)
            }
            val body = buildEmailBody(event)
            openEmailIntent(subject, body)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EmailSender", "Error opening email", e)
            Result.failure(e)
        }
    }

    override suspend fun sendMultipleEvents(events: List<PowerEvent>): Result<Unit> {
        return try {
            val subject = context.getString(R.string.email_subject_summary, events.size)
            val body = buildMultipleEventsBody(events)
            openEmailIntent(subject, body)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EmailSender", "Error opening email for multiple events", e)
            Result.failure(e)
        }
    }

    private fun openEmailIntent(subject: String, body: String) {
        val encodedSubject = encodeForMailto(subject)
        val encodedBody = encodeForMailto(body)
        val mailto = "mailto:$toEmail?subject=$encodedSubject&body=$encodedBody"
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = mailto.toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            throw IllegalStateException(context.getString(R.string.no_email_app))
        }
    }

    private fun getWifiLabel(s: WifiState): String = context.getString(
        when (s) {
            WifiState.CONNECTED -> R.string.wifi_connected
            WifiState.DISCONNECTED -> R.string.wifi_disconnected
            WifiState.UNKNOWN -> R.string.wifi_unknown
        }
    )

    private fun getChargingLabel(s: ChargingState): String = context.getString(
        when (s) {
            ChargingState.CHARGING -> R.string.charging_charging
            ChargingState.NOT_CHARGING -> R.string.charging_not_charging
            ChargingState.UNKNOWN -> R.string.charging_unknown
        }
    )

    private fun getPowerLabel(s: PowerState): String = context.getString(
        when (s) {
            PowerState.NORMAL -> R.string.power_normal
            PowerState.POSSIBLE_OUTAGE -> R.string.power_possible_outage
            PowerState.POWER_RESTORED -> R.string.power_power_restored
            PowerState.UNKNOWN -> R.string.power_unknown
        }
    )

    /** Encodes for mailto: URI so spaces appear as spaces (use %20, not +). */
    private fun encodeForMailto(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
            .replace("+", "%20")
    }

    private fun buildEmailBody(event: PowerEvent): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(event.timestamp))
        val durationText = event.outageDuration?.let {
            val minutes = it / 60000
            val hours = minutes / 60
            if (hours > 0) {
                context.getString(R.string.duration_h_m, hours, minutes % 60)
            } else {
                context.getString(R.string.duration_m, minutes)
            }
        } ?: context.getString(R.string.duration_n_a)

        return context.getString(
            R.string.email_body_event,
            dateStr,
            getWifiLabel(event.wifiState),
            getChargingLabel(event.chargingState),
            getPowerLabel(event.inferredPowerState),
            event.batteryLevel,
            durationText
        )
    }

    private fun buildMultipleEventsBody(events: List<PowerEvent>): String {
        val sb = StringBuilder()
        sb.append(context.getString(R.string.email_body_summary_header, events.size))

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        events.forEach { event ->
            val dateStr = dateFormat.format(Date(event.timestamp))
            sb.append(context.getString(
                R.string.email_body_summary_line,
                dateStr,
                getPowerLabel(event.inferredPowerState),
                getWifiLabel(event.wifiState),
                getChargingLabel(event.chargingState),
                event.batteryLevel
            ))
        }

        sb.append(context.getString(R.string.email_body_footer))
        return sb.toString()
    }
}
