package com.example.actions

import java.util.regex.Pattern

object ActionRegistry {

    private val APP_MAPPINGS = mapOf(
        "whatsapp" to "com.whatsapp",
        "youtube" to "com.google.android.youtube",
        "instagram" to "com.instagram.android",
        "chrome" to "com.android.chrome",
        "settings" to "com.android.settings",
        "maps" to "com.google.android.apps.maps",
        "camera" to "camera_intent",
        "dialer" to "dialer_intent",
        "phone" to "dialer_intent"
    )

    fun parseCommand(input: String): AssistantAction? {
        val text = input.trim().lowercase()

        // Flashlight
        if (text.contains("flashlight on") || text.contains("torch on") || text.contains("flashlight chalu") || text.contains("torch jalao")) {
            return AssistantAction.ToggleFlashlight(true)
        }
        if (text.contains("flashlight off") || text.contains("torch off") || text.contains("flashlight band") || text.contains("torch bujhao")) {
            return AssistantAction.ToggleFlashlight(false)
        }

        // Battery
        if (text.contains("battery") && (text.contains("kitni") || text.contains("percentage") || text.contains("status") || text.contains("charge"))) {
            return AssistantAction.CheckBattery
        }

        // Network / Connectivity
        if (text.contains("internet") || text.contains("online hai") || text.contains("wifi connected") || text.contains("network status")) {
            return AssistantAction.CheckNetwork
        }

        // Volume
        if (text.contains("volume badhao") || text.contains("volume up") || text.contains("awaaz badhao") || text.contains("sound badhao")) {
            return AssistantAction.AdjustVolume(isIncrease = true)
        }
        if (text.contains("volume kam") || text.contains("volume down") || text.contains("awaaz kam") || text.contains("sound kam")) {
            return AssistantAction.AdjustVolume(isIncrease = false)
        }

        // Media Controls
        if (text.contains("music play") || text.contains("gaana chalao") || text.contains("play karo") || text.contains("play music")) {
            return AssistantAction.MediaControl("PLAY")
        }
        if (text.contains("pause karo") || text.contains("music roko") || text.contains("stop music") || text.contains("gaana roko")) {
            return AssistantAction.MediaControl("PAUSE")
        }
        if (text.contains("next song") || text.contains("agla gaana") || text.contains("next track")) {
            return AssistantAction.MediaControl("NEXT")
        }
        if (text.contains("previous song") || text.contains("pichhla gaana") || text.contains("prev song")) {
            return AssistantAction.MediaControl("PREVIOUS")
        }

        // Timer
        if (text.contains("timer cancel") || text.contains("timer band")) {
            return AssistantAction.CancelTimer
        }
        val timerPattern = Pattern.compile("(\\d+)\\s*(minute|min|second|sec|ghante|hour)")
        val timerMatcher = timerPattern.matcher(text)
        if (timerMatcher.find() && (text.contains("timer") || text.contains("lagao"))) {
            val amount = timerMatcher.group(1)?.toIntOrNull() ?: 1
            val unit = timerMatcher.group(2) ?: "second"
            val totalSeconds = when {
                unit.startsWith("min") -> amount * 60
                unit.startsWith("hour") || unit.startsWith("ghante") -> amount * 3600
                else -> amount
            }
            return AssistantAction.SetTimer(totalSeconds, "Timer")
        }

        // Memory Operations
        if (text.contains("yaad rakhna") || text.contains("remember that") || text.contains("yaad rakho")) {
            val note = text.replace("myraa", "").replace("yaad rakhna", "")
                .replace("yaad rakho", "").replace("remember that", "").trim()
            if (note.isNotBlank()) {
                return AssistantAction.Remember(key = "note_${System.currentTimeMillis()}", value = note)
            }
        }
        if (text.contains("bhool jao") || text.contains("forget")) {
            val query = text.replace("myraa", "").replace("bhool jao", "")
                .replace("forget", "").replace("mera", "").trim()
            return AssistantAction.Forget(query)
        }
        if (text.contains("kya yaad hai") || text.contains("what do you remember") || text.contains("meri memory batao")) {
            return AssistantAction.RecallMemory
        }
        if (text.contains("memory clear karo") || text.contains("clear all memory") || text.contains("sab bhool jao")) {
            return AssistantAction.ClearAllMemory
        }

        // Notification reading
        if (text.contains("notification") && (text.contains("padho") || text.contains("read") || text.contains("kya hai") || text.contains("aayi hai"))) {
            return AssistantAction.ReadNotifications
        }

        // Settings Shortcuts
        if (text.contains("settings") || text.contains("setting")) {
            return when {
                text.contains("wi-fi") || text.contains("wifi") -> AssistantAction.OpenSettings("wifi")
                text.contains("bluetooth") -> AssistantAction.OpenSettings("bluetooth")
                text.contains("display") -> AssistantAction.OpenSettings("display")
                text.contains("sound") || text.contains("sound") -> AssistantAction.OpenSettings("sound")
                text.contains("battery") -> AssistantAction.OpenSettings("battery")
                text.contains("app") -> AssistantAction.OpenSettings("apps")
                else -> AssistantAction.OpenSettings("main")
            }
        }

        // Web Control / Google
        if (text.startsWith("google kholo") || text == "open google") {
            return AssistantAction.OpenWeb("https://www.google.com")
        }
        if (text.startsWith("google par search karo") || text.startsWith("search on google")) {
            val query = text.replace("google par search karo", "").replace("search on google", "").trim()
            return AssistantAction.OpenWeb("https://www.google.com/search?q=$query", query)
        }

        // App Launching
        for ((name, pkg) in APP_MAPPINGS) {
            if (text.contains("$name kholo") || text.contains("open $name") || text.contains("$name open karo")) {
                return AssistantAction.LaunchApp(name, pkg)
            }
        }

        // Generic launch pattern: "... kholo" or "open ..."
        if (text.endsWith("kholo") || text.endsWith("open karo") || text.startsWith("open ")) {
            val app = text.replace("kholo", "").replace("open karo", "").replace("open", "").replace("app", "").trim()
            if (app.isNotBlank()) {
                return AssistantAction.LaunchApp(app)
            }
        }

        // WhatsApp message pattern
        if (text.contains("whatsapp") && (text.contains("message") || text.contains("bhejo"))) {
            return AssistantAction.SendMessage(app = "WhatsApp", contactName = null, messageText = "Main 10 minute mein aa raha hoon.")
        }

        // Accessibility click
        if (text.contains("click karo") || text.contains("press karo")) {
            val target = text.replace("click karo", "").replace("press karo", "").trim()
            return AssistantAction.AccessibilityAction(targetText = target)
        }

        return null
    }
}
