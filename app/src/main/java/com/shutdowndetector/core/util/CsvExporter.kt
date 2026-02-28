package com.shutdowndetector.core.util

import com.shutdowndetector.domain.model.PowerEvent
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CsvExporter {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun exportToCsv(events: List<PowerEvent>, file: File): Result<File> {
        return try {
            val csvContent = buildString {
                appendLine("ID,Timestamp,Date,WiFi State,Charging State,Power State,Battery Level,Sent,Outage Duration (ms)")
                events.forEach { event ->
                    val date = dateFormat.format(Date(event.timestamp))
                    val duration = event.outageDuration?.toString() ?: ""
                    appendLine(
                        "${event.id}," +
                        "${event.timestamp}," +
                        "$date," +
                        "${event.wifiState}," +
                        "${event.chargingState}," +
                        "${event.inferredPowerState}," +
                        "${event.batteryLevel}," +
                        "${event.sent}," +
                        duration
                    )
                }
            }
            
            file.writeText(csvContent)
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
