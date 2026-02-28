package com.shutdowndetector.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shutdowndetector.core.util.CsvExporter
import com.shutdowndetector.core.util.DeviceStateProvider
import com.shutdowndetector.domain.model.DeviceState
import com.shutdowndetector.domain.model.PowerEvent
import com.shutdowndetector.domain.usecase.GetEventsUseCase
import com.shutdowndetector.domain.usecase.SendPendingEventsUseCase
import com.shutdowndetector.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase,
    private val sendPendingEventsUseCase: SendPendingEventsUseCase,
    private val deviceStateProvider: DeviceStateProvider,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _events = MutableStateFlow<List<PowerEvent>>(emptyList())
    val events: StateFlow<List<PowerEvent>> = _events.asStateFlow()

    private val _currentState = MutableStateFlow<DeviceState?>(null)
    val currentState: StateFlow<DeviceState?> = _currentState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _sendResult = MutableStateFlow<String?>(null)
    val sendResult: StateFlow<String?> = _sendResult.asStateFlow()

    private val _exportResult = MutableStateFlow<String?>(null)
    val exportResult: StateFlow<String?> = _exportResult.asStateFlow()

    private val csvExporter = CsvExporter()

    init {
        observeEvents()
        updateCurrentState()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            getEventsUseCase().collect { eventsList ->
                _events.value = eventsList
            }
        }
    }

    fun updateCurrentState() {
        _currentState.value = deviceStateProvider.getCurrentState()
    }

    fun sendPendingEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            _sendResult.value = null
            
            sendPendingEventsUseCase()
                .onSuccess { count ->
                    _sendResult.value = if (count > 0) {
                        context.getString(R.string.send_success, count)
                    } else {
                        context.getString(R.string.send_no_pending)
                    }
                }
                .onFailure { error ->
                    _sendResult.value = context.getString(R.string.send_error, error.message ?: "")
                    Timber.e(error, "Failed to send pending events")
                }
            
            _isLoading.value = false
        }
    }

    fun exportToCsv() {
        viewModelScope.launch {
            _isLoading.value = true
            _exportResult.value = null
            
            try {
                val events = _events.value
                if (events.isEmpty()) {
                    _exportResult.value = context.getString(R.string.export_no_data)
                    return@launch
                }

                val fileName = "power_events_${System.currentTimeMillis()}.csv"
                val file = File(context.getExternalFilesDir(null), fileName)
                
                csvExporter.exportToCsv(events, file)
                    .onSuccess {
                        _exportResult.value = context.getString(R.string.export_success, file.absolutePath)
                    }
                    .onFailure { error ->
                        _exportResult.value = context.getString(R.string.export_error, error.message ?: "")
                        Timber.e(error, "Failed to export CSV")
                    }
            } catch (e: Exception) {
                _exportResult.value = context.getString(R.string.error_generic, e.message ?: "")
                Timber.e(e, "Failed to export CSV")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSendResult() {
        _sendResult.value = null
    }

    fun clearExportResult() {
        _exportResult.value = null
    }

    fun getEventsByMonth(): Map<String, Int> {
        val eventsByMonth = mutableMapOf<String, Int>()
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
        
        _events.value.forEach { event ->
            val month = dateFormat.format(java.util.Date(event.timestamp))
            eventsByMonth[month] = (eventsByMonth[month] ?: 0) + 1
        }
        
        return eventsByMonth
    }
}
