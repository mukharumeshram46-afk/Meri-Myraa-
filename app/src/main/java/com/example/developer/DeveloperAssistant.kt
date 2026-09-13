package com.example.developer

import com.example.actions.ActionValidator
import com.example.gemini.GeminiLiveRepository

data class DevQueryResponse(
    val title: String,
    val explanation: String,
    val codeSnippet: String? = null,
    val recommendations: List<String> = emptyList()
)

class DeveloperAssistant(private val geminiRepository: GeminiLiveRepository) {

    suspend fun consultDeveloper(query: String): DevQueryResponse {
        if (ActionValidator.isCommandMalicious(query)) {
            return DevQueryResponse(
                title = "Security Alert",
                explanation = "Piyush, ye command security policies ke according safe nahi hai. Potentially harmful or destructive shell operations are blocked."
            )
        }

        val prompt = """
            You are MYRAA's specialized Developer Copilot mode helping Piyush, an Android and software developer.
            User Query: $query
            
            Provide:
            1. Clear, practical technical explanation.
            2. Android/Kotlin best practices (Compose, Coroutines, Room, Architecture).
            3. Idiomatic, clean code snippets where relevant.
            4. Performance or security notes.
            Tone: Sharp, expert, supportive, addressing him as Piyush.
        """.trimIndent()

        val response = geminiRepository.generateResponse(prompt, "Piyush is an active software developer.")
        val text = response.getOrDefault(
            "Piyush, developer query analyze karne ke liye internet connection check karein."
        )

        return DevQueryResponse(
            title = "MYRAA Developer Copilot",
            explanation = text,
            recommendations = listOf(
                "Use M3 design tokens and edge-to-edge in Compose",
                "Ensure Kotlin coroutines use structured concurrency",
                "Verify ProGuard rules before release builds"
            )
        )
    }
}
