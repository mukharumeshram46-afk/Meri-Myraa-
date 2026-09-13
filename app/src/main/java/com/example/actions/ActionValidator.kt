package com.example.actions

import android.net.Uri

object ActionValidator {
    private val BLOCKED_KEYWORDS = listOf(
        "rm -rf", "format", "wipe", "su", "root", "chmod 777", "dd if="
    )

    fun validate(action: AssistantAction): ValidationResult {
        return when (action) {
            is AssistantAction.OpenWeb -> {
                if (action.url != null) {
                    try {
                        val uri = Uri.parse(action.url)
                        val scheme = uri.scheme?.lowercase()
                        if (scheme != "http" && scheme != "https") {
                            return ValidationResult.Invalid("Piyush, ye URL protocol safe nahi hai.")
                        }
                    } catch (e: Exception) {
                        return ValidationResult.Invalid("Malformed URL.")
                    }
                }
                ValidationResult.Valid
            }

            is AssistantAction.SendMessage -> {
                if (action.messageText.isBlank()) {
                    return ValidationResult.Invalid("Message khali nahi ho sakta Piyush.")
                }
                ValidationResult.Valid
            }

            is AssistantAction.AccessibilityAction -> {
                if (action.targetText.isBlank()) {
                    return ValidationResult.Invalid("Target element specify nahi kiya gaya.")
                }
                ValidationResult.Valid
            }

            else -> ValidationResult.Valid
        }
    }

    fun isCommandMalicious(command: String): Boolean {
        val lower = command.lowercase()
        return BLOCKED_KEYWORDS.any { lower.contains(it) }
    }
}

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
}
