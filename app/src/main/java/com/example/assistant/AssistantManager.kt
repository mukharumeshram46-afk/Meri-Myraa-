package com.example.assistant

import android.content.Context
import com.example.actions.ActionExecutor
import com.example.actions.ActionResult
import com.example.actions.ActionStatus
import com.example.actions.ConfirmationManager
import com.example.audio.AudioInputManager
import com.example.audio.AudioOutputManager
import com.example.gemini.GeminiAudioInputManager
import com.example.gemini.GeminiAudioOutputManager
import com.example.gemini.GeminiConnectionState
import com.example.gemini.GeminiLiveRepository
import com.example.gemini.GeminiLiveSessionManager
import com.example.memory.MemoryDao
import com.example.memory.MemoryManager
import com.example.memory.MemoryRepository
import com.example.memory.MyraaDatabase
import com.example.settings.SettingsRepository
import com.example.utils.Logger
import com.example.utils.NetworkMonitor
import com.example.wake.WakeWordManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AssistantManager(
    val context: Context,
    val database: MyraaDatabase,
    val settingsRepository: SettingsRepository,
    val networkMonitor: NetworkMonitor
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    val memoryDao: MemoryDao = database.memoryDao()
    val memoryRepository = MemoryRepository(memoryDao)
    val memoryManager = MemoryManager(memoryRepository)

    val confirmationManager = ConfirmationManager()
    val actionExecutor = ActionExecutor(context, memoryManager, confirmationManager, networkMonitor)

    val geminiRepository = GeminiLiveRepository()
    val geminiLiveSessionManager = GeminiLiveSessionManager(
        repository = geminiRepository,
        audioInputManager = GeminiAudioInputManager(),
        audioOutputManager = GeminiAudioOutputManager(),
        networkMonitor = networkMonitor
    )

    private val offlineEngine = OfflineAssistantEngine(actionExecutor, memoryManager)
    private val onlineEngine = OnlineAssistantEngine(geminiRepository, actionExecutor, offlineEngine)

    val audioInputManager = AudioInputManager(context)
    val audioOutputManager = AudioOutputManager(context)
    val wakeWordManager = WakeWordManager()

    private val _assistantState = MutableStateFlow(AssistantState.IDLE)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.MYRAA,
                text = "Hey Piyush ❤️ Main ready hoon. Bolo, kya karna hai?"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _currentStatusText = MutableStateFlow("MYRAA • Ready for Piyush")
    val currentStatusText: StateFlow<String> = _currentStatusText.asStateFlow()

    init {
        // Monitor network
        scope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (!isOnline && _assistantState.value == AssistantState.IDLE) {
                    _currentStatusText.value = "OFFLINE MODE • Local commands only"
                } else if (isOnline && _currentStatusText.value.contains("OFFLINE")) {
                    _currentStatusText.value = "ONLINE • Gemini connected"
                }
            }
        }
    }

    fun startListening() {
        if (!settingsRepository.settings.value.isAssistantActive) {
            _currentStatusText.value = "MYRAA is currently turned off in settings"
            return
        }

        audioOutputManager.stop()
        _assistantState.value = AssistantState.LISTENING
        _currentStatusText.value = "Listening to Piyush..."

        audioInputManager.startListening(
            onResult = { text ->
                handleUserInput(text)
            },
            onError = { err ->
                _assistantState.value = AssistantState.IDLE
                _currentStatusText.value = "Ready • Tap to speak"
            }
        )
    }

    fun stopListening() {
        audioInputManager.stopListening()
        if (_assistantState.value == AssistantState.LISTENING) {
            _assistantState.value = AssistantState.IDLE
            _currentStatusText.value = "Ready"
        }
    }

    fun handleUserInput(input: String) {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return

        audioInputManager.stopListening()

        // Append user chat message
        val userMsg = ChatMessage(sender = MessageSender.USER, text = trimmed)
        _chatMessages.value = _chatMessages.value + userMsg

        _assistantState.value = AssistantState.THINKING
        _currentStatusText.value = "Thinking..."

        scope.launch {
            val memoryContext = memoryManager.getMemoryContextForPrompt()
            val isOnline = networkMonitor.isCurrentlyOnline()

            val engine: AssistantEngine = if (isOnline) onlineEngine else offlineEngine
            val response = engine.processCommand(trimmed, memoryContext)

            when (response) {
                is EngineResponse.Text -> {
                    deliverAssistantResponse(response.message)
                }

                is EngineResponse.ActionExecuted -> {
                    handleActionResult(response.result)
                }

                is EngineResponse.OfflineFallback -> {
                    if (response.actionResult != null) {
                        handleActionResult(response.actionResult)
                    } else {
                        deliverAssistantResponse(response.message)
                    }
                }
            }
        }
    }

    private fun handleActionResult(result: ActionResult) {
        when (result.status) {
            ActionStatus.SUCCESS -> {
                _assistantState.value = AssistantState.SUCCESS
                _currentStatusText.value = "Action Completed"
                deliverAssistantResponse(result.spokenResponse, actionDetail = result.details)
            }

            ActionStatus.REQUIRES_CONFIRMATION -> {
                _assistantState.value = AssistantState.IDLE
                _currentStatusText.value = "Confirmation Required"
                deliverAssistantResponse(result.spokenResponse)
            }

            ActionStatus.REQUIRES_PERMISSION -> {
                _assistantState.value = AssistantState.ERROR
                _currentStatusText.value = "Permission Required"
                deliverAssistantResponse(result.spokenResponse)
            }

            ActionStatus.UNAVAILABLE, ActionStatus.NOT_ALLOWED, ActionStatus.FAILED -> {
                _assistantState.value = AssistantState.ERROR
                _currentStatusText.value = "Action Failed"
                deliverAssistantResponse(result.spokenResponse)
            }
        }
    }

    private fun deliverAssistantResponse(message: String, actionDetail: String? = null) {
        val myraaMsg = ChatMessage(
            sender = MessageSender.MYRAA,
            text = message,
            actionDetail = actionDetail
        )
        _chatMessages.value = _chatMessages.value + myraaMsg

        _assistantState.value = AssistantState.SPEAKING
        _currentStatusText.value = "Speaking..."

        audioOutputManager.speak(message) {
            _assistantState.value = AssistantState.IDLE
            _currentStatusText.value = "MYRAA • Ready for Piyush"
        }
    }

    fun interruptSpeech() {
        audioOutputManager.stop()
        _assistantState.value = AssistantState.IDLE
        _currentStatusText.value = "Ready"
    }

    fun clearHistory() {
        _chatMessages.value = listOf(
            ChatMessage(
                sender = MessageSender.MYRAA,
                text = "Chat history cleared Piyush. How can I help you right now?"
            )
        )
    }

    fun shutdown() {
        audioInputManager.stopListening()
        audioOutputManager.shutdown()
        geminiLiveSessionManager.cleanup()
    }
}
