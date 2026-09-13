package com.example.gemini

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.example.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GeminiAudioOutputManager {
    companion object {
        const val SAMPLE_RATE = 24000
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var audioTrack: AudioTrack? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    init {
        initAudioTrack()
    }

    private fun initAudioTrack() {
        val bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            .coerceAtLeast(4096)

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANT)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setChannelMask(CHANNEL_CONFIG)
            .setEncoding(AUDIO_FORMAT)
            .build()

        try {
            audioTrack = AudioTrack(
                attributes,
                format,
                bufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )
            audioTrack?.play()
        } catch (e: Exception) {
            Logger.e("Failed to initialize AudioTrack for Gemini Live", e)
        }
    }

    fun playAudioChunk(data: ByteArray) {
        if (audioTrack == null || audioTrack?.state != AudioTrack.STATE_INITIALIZED) {
            initAudioTrack()
        }
        try {
            _isPlaying.value = true
            audioTrack?.write(data, 0, data.size)
        } catch (e: Exception) {
            Logger.e("Error writing audio to AudioTrack", e)
        }
    }

    fun stopPlayback() {
        try {
            audioTrack?.pause()
            audioTrack?.flush()
        } catch (e: Exception) {
            Logger.w("Error stopping AudioTrack: ${e.message}")
        } finally {
            _isPlaying.value = false
        }
    }

    fun release() {
        stopPlayback()
        try {
            audioTrack?.release()
        } catch (e: Exception) {
            Logger.w("Error releasing AudioTrack: ${e.message}")
        } finally {
            audioTrack = null
        }
    }
}
