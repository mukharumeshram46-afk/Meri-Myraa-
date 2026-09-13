package com.example.actions

sealed class AssistantAction(val riskLevel: RiskLevel = RiskLevel.LOW) {
    // App Launch
    data class LaunchApp(val appName: String, val specificPackage: String? = null) : AssistantAction(RiskLevel.LOW)

    // Web Control
    data class OpenWeb(val url: String?, val searchQuery: String? = null) : AssistantAction(RiskLevel.LOW)

    // Settings
    data class OpenSettings(val settingType: String) : AssistantAction(RiskLevel.LOW)

    // Flashlight
    data class ToggleFlashlight(val enable: Boolean) : AssistantAction(RiskLevel.LOW)

    // Volume & Media
    data class AdjustVolume(val isIncrease: Boolean) : AssistantAction(RiskLevel.LOW)
    data class MediaControl(val command: String) : AssistantAction(RiskLevel.LOW)

    // Device Info
    object CheckBattery : AssistantAction(RiskLevel.LOW)
    object CheckNetwork : AssistantAction(RiskLevel.LOW)

    // Timer
    data class SetTimer(val durationSeconds: Int, val label: String? = null) : AssistantAction(RiskLevel.LOW)
    object CancelTimer : AssistantAction(RiskLevel.LOW)

    // Memory
    data class Remember(val key: String, val value: String) : AssistantAction(RiskLevel.LOW)
    data class Forget(val query: String) : AssistantAction(RiskLevel.MEDIUM)
    object RecallMemory : AssistantAction(RiskLevel.LOW)
    object ClearAllMemory : AssistantAction(RiskLevel.HIGH)

    // Notifications
    object ReadNotifications : AssistantAction(RiskLevel.LOW)

    // Messaging (WhatsApp / Intent)
    data class SendMessage(
        val app: String,
        val contactName: String?,
        val messageText: String
    ) : AssistantAction(RiskLevel.MEDIUM)

    // Accessibility Navigation
    data class AccessibilityAction(
        val targetText: String,
        val interactionType: String = "CLICK" // CLICK, SCROLL_DOWN, SCROLL_UP
    ) : AssistantAction(RiskLevel.MEDIUM)

    // Vision
    object TakeCameraSnapshot : AssistantAction(RiskLevel.LOW)
    object AnalyzeScreen : AssistantAction(RiskLevel.MEDIUM)
}
