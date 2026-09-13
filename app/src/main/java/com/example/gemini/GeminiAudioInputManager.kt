package com.example.gemini

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.example.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class GeminiAudioInputManager {
    companion object {
        const val SAMPLE_RATE = 16000
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    @SuppressLint("MissingPermission")
    fun startRecording(onAudioData: (ByteArray) -> Unit) {
        if (_isRecording.value) return

        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            .coerceAtLeast(2048)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Logger.e("AudioRecord initialization failed")
                return
            }

            audioRecord?.startRecording()
            _isRecording.value = true

            recordingJob = scope.launch {
                val buffer = ByteArray(bufferSize)
                while (isActive && _isRecording.value) {
                    val read = audioRecord?.read(buffer, 0, bufferSize) ?: -1
                    if (read > 0) {
                        val chunk = buffer.copyOf(read)
                        onAudioData(chunk)

                        // Compute RMS amplitude for visualizer
                        var sum = 0.0
                        for (i in 0 until read step 2) {
                            if (i + 1 < read) {
                                val sample = (buffer[i].toInt() and 0xFF) or (buffer[i + 1].toInt() shl 8)
                                val shortSample = sample.toShort()
                                sum += (shortSample * shortSample)
                            }
                        }
                        val rms = sqrt(sum / (read / 2.0))
                        _amplitude.value = (rms / 32767.0).toFloat().coerceIn(0f, 1f)
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e("Error starting GeminiAudioInputManager", e)
            stopRecording()
        }
    }

    fun stopRecording() {
        _isRecording.value = false
        _amplitude.value = 0f
        recordingJob?.cancel()
        recordingJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Logger.w("Error releasing AudioRecord: ${e.message}")
        } finally {
            audioRecord = null
        }
    }
}
