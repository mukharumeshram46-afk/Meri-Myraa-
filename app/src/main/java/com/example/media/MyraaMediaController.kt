package com.example.media

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent

class MyraaMediaController(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    fun play() {
        sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY)
    }

    fun pause() {
        sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PAUSE)
    }

    fun togglePlayPause() {
        sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
    }

    fun next() {
        sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT)
    }

    fun previous() {
        sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
    }

    fun isMusicActive(): Boolean {
        return audioManager?.isMusicActive == true
    }

    private fun sendMediaKeyEvent(keyCode: Int) {
        val down = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val up = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        audioManager?.dispatchMediaKeyEvent(down)
        audioManager?.dispatchMediaKeyEvent(up)
    }
}
