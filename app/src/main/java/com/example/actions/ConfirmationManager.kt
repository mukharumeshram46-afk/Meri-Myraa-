package com.example.actions

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

data class ConfirmationRequest(
    val action: AssistantAction,
    val title: String,
    val message: String,
    val riskLevel: RiskLevel,
    val onConfirm: suspend () -> ActionResult,
    val onCancel: () -> Unit = {}
)

class ConfirmationManager {
    private val _currentRequest = MutableStateFlow<ConfirmationRequest?>(null)
    val currentRequest: StateFlow<ConfirmationRequest?> = _currentRequest.asStateFlow()

    fun requestConfirmation(request: ConfirmationRequest) {
        _currentRequest.value = request
    }

    suspend fun confirm(): ActionResult? {
        val request = _currentRequest.value ?: return null
        _currentRequest.value = null
        return request.onConfirm.invoke()
    }

    fun cancel() {
        val request = _currentRequest.value
        _currentRequest.value = null
        request?.onCancel?.invoke()
    }
}
