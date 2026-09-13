package com.example.wake

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class WakeMode {
    FOREGROUND_LISTENING,
    NOTIFICATION_TRIGGER,
    MANUAL_TOUCH
}

class WakeWordManager {
    private val _wakeMode = MutableStateFlow(WakeMode.FOREGROUND_LISTENING)
    val wakeMode: StateFlow<WakeMode> = _wakeMode.asStateFlow()

    private val _isWakeListening = MutableStateFlow(false)
    val isWakeListening: StateFlow<Boolean> = _isWakeListening.asStateFlow()

    fun setWakeMode(mode: WakeMode) {
        _wakeMode.value = mode
    }

    fun setWakeListening(active: Boolean) {
        _isWakeListening.value = active
    }

    fun checkAndProcessWake(
        input: String,
        onWakeDetected: (command: String) -> Unit
    ): Boolean {
        if (WakeWordEngine.containsWakeWord(input)) {
            val command = WakeWordEngine.stripWakeWord(input)
            onWakeDetected(command)
            return true
        }
        return false
    }
}
