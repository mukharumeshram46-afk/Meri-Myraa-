package com.example.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MyraaSettings(
    val isAssistantActive: Boolean = true,
    val isBackgroundModeEnabled: Boolean = false,
    val isMemoryEnabled: Boolean = true,
    val isVisionEnabled: Boolean = true,
    val isScreenAwarenessEnabled: Boolean = false,
    val userName: String = "Piyush",
    val preferredLanguage: String = "Hinglish",
    val speechRate: Float = 1.05f,
    val speechPitch: Float = 1.15f
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("myraa_preferences", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<MyraaSettings> = _settings.asStateFlow()

    private fun loadSettings(): MyraaSettings {
        return MyraaSettings(
            isAssistantActive = prefs.getBoolean("assistant_active", true),
            isBackgroundModeEnabled = prefs.getBoolean("background_mode", false),
            isMemoryEnabled = prefs.getBoolean("memory_enabled", true),
            isVisionEnabled = prefs.getBoolean("vision_enabled", true),
            isScreenAwarenessEnabled = prefs.getBoolean("screen_awareness", false),
            userName = prefs.getString("user_name", "Piyush") ?: "Piyush",
            preferredLanguage = prefs.getString("preferred_language", "Hinglish") ?: "Hinglish",
            speechRate = prefs.getFloat("speech_rate", 1.05f),
            speechPitch = prefs.getFloat("speech_pitch", 1.15f)
        )
    }

    fun updateAssistantActive(active: Boolean) {
        prefs.edit().putBoolean("assistant_active", active).apply()
        _settings.value = _settings.value.copy(isAssistantActive = active)
    }

    fun updateBackgroundMode(enabled: Boolean) {
        prefs.edit().putBoolean("background_mode", enabled).apply()
        _settings.value = _settings.value.copy(isBackgroundModeEnabled = enabled)
    }

    fun updateMemoryEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("memory_enabled", enabled).apply()
        _settings.value = _settings.value.copy(isMemoryEnabled = enabled)
    }

    fun updateVisionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("vision_enabled", enabled).apply()
        _settings.value = _settings.value.copy(isVisionEnabled = enabled)
    }

    fun updateScreenAwareness(enabled: Boolean) {
        prefs.edit().putBoolean("screen_awareness", enabled).apply()
        _settings.value = _settings.value.copy(isScreenAwarenessEnabled = enabled)
    }

    fun updateUserName(name: String) {
        val safeName = name.ifBlank { "Piyush" }
        prefs.edit().putString("user_name", safeName).apply()
        _settings.value = _settings.value.copy(userName = safeName)
    }
}
