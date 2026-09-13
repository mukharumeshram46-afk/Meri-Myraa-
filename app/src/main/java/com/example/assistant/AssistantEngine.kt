package com.example.assistant

import com.example.actions.ActionExecutor
import com.example.actions.ActionRegistry
import com.example.actions.ActionResult
import com.example.actions.ActionStatus
import com.example.gemini.GeminiLiveRepository
import com.example.memory.MemoryManager
import com.example.utils.Logger

sealed class EngineResponse {
    data class Text(val message: String) : EngineResponse()
    data class ActionExecuted(val result: ActionResult) : EngineResponse()
    data class OfflineFallback(val message: String, val actionResult: ActionResult?) : EngineResponse()
}

interface AssistantEngine {
    suspend fun processCommand(
        input: String,
        memoryContext: String
    ): EngineResponse
}

class OfflineAssistantEngine(
    private val actionExecutor: ActionExecutor,
    private val memoryManager: MemoryManager
) : AssistantEngine {

    override suspend fun processCommand(
        input: String,
        memoryContext: String
    ): EngineResponse {
        val parsedAction = ActionRegistry.parseCommand(input)
        return if (parsedAction != null) {
            val result = actionExecutor.execute(parsedAction)
            EngineResponse.ActionExecuted(result)
        } else {
            EngineResponse.OfflineFallback(
                message = "Gemini connection abhi available nahi hai Piyush, lekin main phone ke local commands jaise Flashlight, Apps, Settings, Timer, aur Battery handle kar sakti hoon.",
                actionResult = null
            )
        }
    }
}

class OnlineAssistantEngine(
    private val geminiRepository: GeminiLiveRepository,
    private val actionExecutor: ActionExecutor,
    private val offlineEngine: OfflineAssistantEngine
) : AssistantEngine {

    override suspend fun processCommand(
        input: String,
        memoryContext: String
    ): EngineResponse {
        // First check if this is an explicit phone hardware / system control command
        val directAction = ActionRegistry.parseCommand(input)
        if (directAction != null) {
            val actionResult = actionExecutor.execute(directAction)
            return EngineResponse.ActionExecuted(actionResult)
        }

        // If not a direct local action, send to real Gemini AI for intelligent conversation
        val geminiResult = geminiRepository.generateResponse(
            prompt = input,
            memoryContext = memoryContext
        )

        return if (geminiResult.isSuccess) {
            EngineResponse.Text(geminiResult.getOrThrow())
        } else {
            Logger.w("Gemini request failed, falling back to local offline logic")
            offlineEngine.processCommand(input, memoryContext)
        }
    }
}
