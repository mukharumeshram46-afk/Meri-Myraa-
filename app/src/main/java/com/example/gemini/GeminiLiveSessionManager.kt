package com.example.gemini

import com.example.utils.Logger
import com.example.utils.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GeminiLiveSessionManager(
    private val repository: GeminiLiveRepository,
    private val audioInputManager: GeminiAudioInputManager,
    private val audioOutputManager: GeminiAudioOutputManager,
    private val networkMonitor: NetworkMonitor
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _connectionState = MutableStateFlow(GeminiConnectionState.DISCONNECTED)
    val connectionState: StateFlow<GeminiConnectionState> = _connectionState.asStateFlow()

    private var sessionJob: Job? = null

    fun connect() {
        if (!networkMonitor.isCurrentlyOnline()) {
            _connectionState.value = GeminiConnectionState.OFFLINE
            return
        }

        _connectionState.value = GeminiConnectionState.CONNECTING
        sessionJob = scope.launch {
            try {
                // Verify initial capability
                _connectionState.value = GeminiConnectionState.CONNECTED
                Logger.i("Gemini Live Session connected")
            } catch (e: Exception) {
                Logger.e("Gemini Live connection failed", e)
                _connectionState.value = GeminiConnectionState.ERROR
            }
        }
    }

    fun startAudioStreaming(onAudioChunk: (ByteArray) -> Unit) {
        if (_connectionState.value != GeminiConnectionState.CONNECTED) {
            connect()
        }
        audioInputManager.startRecording { chunk ->
            onAudioChunk(chunk)
        }
    }

    fun stopAudioStreaming() {
        audioInputManager.stopRecording()
    }

    fun playAudioResponse(chunk: ByteArray) {
        audioOutputManager.playAudioChunk(chunk)
    }

    fun interrupt() {
        // Barge-in: immediate cancellation of output audio
        audioOutputManager.stopPlayback()
    }

    fun reconnect() {
        _connectionState.value = GeminiConnectionState.RECONNECTING
        disconnect()
        connect()
    }

    fun disconnect() {
        audioInputManager.stopRecording()
        audioOutputManager.stopPlayback()
        sessionJob?.cancel()
        sessionJob = null
        _connectionState.value = GeminiConnectionState.DISCONNECTED
        Logger.i("Gemini Live Session disconnected")
    }

    fun cleanup() {
        disconnect()
        audioOutputManager.release()
    }
}
