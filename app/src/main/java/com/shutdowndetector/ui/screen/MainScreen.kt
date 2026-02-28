package com.shutdowndetector.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shutdowndetector.R
import com.shutdowndetector.domain.model.*
import com.shutdowndetector.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onExportCsv: () -> Unit = {}
) {
    val events by viewModel.events.collectAsState()
    val currentState by viewModel.currentState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val sendResult by viewModel.sendResult.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.updateCurrentState()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_title)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estado Actual
            CurrentStateCard(currentState = currentState)

            // Botones de Acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.sendPendingEvents() },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.button_send_events))
                }
                
                Button(
                    onClick = { viewModel.exportToCsv() },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text(stringResource(R.string.button_export_csv))
                }
            }

            // Resultado del envío
            sendResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            result.startsWith("✅") -> MaterialTheme.colorScheme.primaryContainer
                            result.startsWith("❌") -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Text(
                        text = result,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                LaunchedEffect(result) {
                    kotlinx.coroutines.delay(5000)
                    viewModel.clearSendResult()
                }
            }

            // Resultado de exportación
            val exportResult by viewModel.exportResult.collectAsState()
            exportResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            result.startsWith("✅") -> MaterialTheme.colorScheme.primaryContainer
                            result.startsWith("❌") -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Text(
                        text = result,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                LaunchedEffect(result) {
                    kotlinx.coroutines.delay(5000)
                    viewModel.clearExportResult()
                }
            }

            // Gráfico de eventos por mes
            val eventsByMonth = viewModel.getEventsByMonth()
            if (eventsByMonth.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.events_by_month),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                EventsChart(eventsByMonth = eventsByMonth)
            }

            // Lista de Eventos
            Text(
                text = stringResource(R.string.event_history_count, events.size),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events) { event ->
                    EventItem(event = event)
                }
            }
        }
    }
}

@Composable
fun CurrentStateCard(currentState: DeviceState?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.state_current),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            currentState?.let { state ->
                StateRow(label = stringResource(R.string.label_wifi), value = stringResource(state.wifiState.toLabelResId()))
                StateRow(label = stringResource(R.string.label_charging), value = stringResource(state.chargingState.toLabelResId()))
                StateRow(label = stringResource(R.string.label_battery), value = "${state.batteryLevel}%")
            } ?: Text(stringResource(R.string.loading))
        }
    }
}

@Composable
fun StateRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EventItem(event: PowerEvent) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val date = dateFormat.format(Date(event.timestamp))
    
    val cardColor = when (event.inferredPowerState) {
        PowerState.POSSIBLE_OUTAGE -> MaterialTheme.colorScheme.errorContainer
        PowerState.POWER_RESTORED -> MaterialTheme.colorScheme.primaryContainer
        PowerState.NORMAL -> MaterialTheme.colorScheme.surfaceVariant
        PowerState.UNKNOWN -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                if (event.sent) {
                    Text(
                        text = stringResource(R.string.event_sent),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Text(
                text = stringResource(R.string.event_state, stringResource(event.inferredPowerState.toLabelResId())),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.label_wifi) + ": " + stringResource(event.wifiState.toLabelResId()), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.label_charging) + ": " + stringResource(event.chargingState.toLabelResId()), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.label_battery) + ": ${event.batteryLevel}%", style = MaterialTheme.typography.bodySmall)
            }
            
            event.outageDuration?.let { duration ->
                val minutes = duration / 60000
                val hours = minutes / 60
                val durationText = if (hours > 0) {
                    stringResource(R.string.duration_h_m, hours, minutes % 60)
                } else {
                    stringResource(R.string.duration_m, minutes)
                }
                Text(
                    text = stringResource(R.string.event_duration, durationText),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun EventsChart(eventsByMonth: Map<String, Int>) {
    val maxEvents = eventsByMonth.values.maxOrNull() ?: 1
    val sortedMonths = eventsByMonth.toList().sortedBy { it.first }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sortedMonths.forEach { (month, count) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = month,
                        modifier = Modifier.width(80.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                    ) {
                        val width = (count.toFloat() / maxEvents.toFloat())
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(width)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                    
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
