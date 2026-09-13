package com.example.actions

enum class ActionStatus {
    SUCCESS,
    FAILED,
    NOT_ALLOWED,
    REQUIRES_PERMISSION,
    UNAVAILABLE,
    REQUIRES_CONFIRMATION
}

data class ActionResult(
    val status: ActionStatus,
    val spokenResponse: String,
    val details: String? = null,
    val pendingAction: AssistantAction? = null
)
