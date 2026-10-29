package com.lira.field.core.network

import com.lira.field.core.model.SseStreamEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SseConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    POLLING_FALLBACK
}

class LiraSseClient(
    private val okHttpClient: OkHttpClient,
    private val getBaseUrl: () -> String,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    private val _connectionState = MutableStateFlow(SseConnectionState.DISCONNECTED)
    val connectionState: StateFlow<SseConnectionState> = _connectionState

    private val _eventsFlow = MutableSharedFlow<SseStreamEvent>(extraBufferCapacity = 64)
    val eventsFlow: SharedFlow<SseStreamEvent> = _eventsFlow

    private val _lastEventAt = MutableStateFlow<String?>(null)
    val lastEventAt: StateFlow<String?> = _lastEventAt

    private var eventSource: EventSource? = null
    var isRunning = false
        private set
    var pollingJob: Job? = null
        private set
    var reconnectAttempt = 0
        private set

    var onTriggerRefetch: (suspend () -> Unit)? = null

    companion object {
        const val POLLING_FALLBACK_MS = 10_000L
    }

    fun start() {
        if (isRunning) return
        isRunning = true
        connectSse()
    }

    fun stop() {
        isRunning = false
        eventSource?.cancel()
        eventSource = null
        pollingJob?.cancel()
        pollingJob = null
        _connectionState.value = SseConnectionState.DISCONNECTED
    }

    fun connectSse() {
        if (!isRunning) return
        _connectionState.value = SseConnectionState.CONNECTING
        pollingJob?.cancel()

        val baseUrl = getBaseUrl()
        val streamUrl = if (baseUrl.endsWith("/")) "${baseUrl}driver/stream" else "$baseUrl/driver/stream"
        val request = Request.Builder()
            .url(streamUrl)
            .header("Accept", "text/event-stream")
            .build()

        val factory = EventSources.createFactory(okHttpClient)
        eventSource = factory.newEventSource(request, object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                reconnectAttempt = 0
                _connectionState.value = SseConnectionState.CONNECTED
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                _lastEventAt.value = timestamp
                coroutineScope.launch {
                    try {
                        val adapter = NetworkClient.moshi.adapter(SseStreamEvent::class.java)
                        val event = adapter.fromJson(data) ?: SseStreamEvent(type = type ?: "stream_event", at = timestamp)
                        _eventsFlow.emit(event)
                        onTriggerRefetch?.invoke()
                    } catch (_: Exception) {
                        _eventsFlow.emit(SseStreamEvent(type = type ?: "raw_event", at = timestamp))
                        onTriggerRefetch?.invoke()
                    }
                }
            }

            override fun onClosed(eventSource: EventSource) {
                if (isRunning) {
                    switchToPollingFallback("SSE Stream Closed")
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                if (isRunning) {
                    switchToPollingFallback(t?.message ?: "SSE Connection Failure")
                }
            }
        })
    }

    fun switchToPollingFallback(reason: String? = null) {
        _connectionState.value = SseConnectionState.POLLING_FALLBACK
        eventSource?.cancel()
        eventSource = null

        pollingJob?.cancel()
        pollingJob = coroutineScope.launch {
            while (isActive && isRunning) {
                try {
                    onTriggerRefetch?.invoke()
                } catch (_: Exception) {}

                delay(POLLING_FALLBACK_MS) // 10s polling fallback per LIRA spec

                if (isRunning && _connectionState.value == SseConnectionState.POLLING_FALLBACK) {
                    reconnectAttempt++
                    connectSse()
                    break
                }
            }
        }
    }
}

