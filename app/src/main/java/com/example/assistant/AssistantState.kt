package com.example.assistant

enum class AssistantState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    EXECUTING,
    SUCCESS,
    ERROR,
    OFFLINE
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionDetail: String? = null
)

enum class MessageSender {
    USER,
    MYRAA,
    SYSTEM
}
