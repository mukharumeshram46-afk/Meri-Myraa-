package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class AudioOutputManager(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var onDoneCallback: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            // Support Hindi / Indian English
            val hiLocale = Locale("hi", "IN")
            val result = tts?.setLanguage(hiLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
            // Tuning for natural, expressive female voice
            tts?.setPitch(1.15f)
            tts?.setSpeechRate(1.05f)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    onDoneCallback?.invoke()
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    onDoneCallback?.invoke()
                }
            })
            Logger.i("TextToSpeech initialized successfully")
        } else {
            Logger.e("TextToSpeech initialization failed: $status")
        }
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!isInitialized || tts == null) {
            Logger.w("TTS not ready to speak")
            onDone?.invoke()
            return
        }
        stop()
        onDoneCallback = onDone
        val utteranceId = "myraa_utterance_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Logger.w("Error stopping TTS: ${e.message}")
        } finally {
            _isSpeaking.value = false
        }
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
