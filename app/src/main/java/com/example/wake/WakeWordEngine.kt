package com.example.wake

object WakeWordEngine {
    private val WAKE_PHRASES = listOf(
        "hey myraa",
        "hlo myraa",
        "hello myraa",
        "hii myraa",
        "hi myraa",
        "myraa"
    )

    fun containsWakeWord(transcript: String): Boolean {
        val lower = transcript.lowercase().trim()
        return WAKE_PHRASES.any { phrase ->
            lower.contains(phrase)
        }
    }

    fun stripWakeWord(transcript: String): String {
        var cleaned = transcript.trim()
        for (phrase in WAKE_PHRASES) {
            val regex = Regex("(?i)\\b$phrase\\b")
            cleaned = cleaned.replace(regex, "").trim()
        }
        return cleaned.replace(Regex("^[,\\.\\s]+"), "").trim()
    }
}
