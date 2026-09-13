package com.example.actions

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.provider.AlarmClock
import android.provider.Settings
import android.view.KeyEvent
import com.example.accessibility.MyraaAccessibilityService
import com.example.memory.MemoryManager
import com.example.notifications.MyraaNotificationListenerService
import com.example.utils.Logger
import com.example.utils.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ActionExecutor(
    private val context: Context,
    private val memoryManager: MemoryManager,
    private val confirmationManager: ConfirmationManager,
    private val networkMonitor: NetworkMonitor
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    suspend fun execute(action: AssistantAction): ActionResult = withContext(Dispatchers.Main) {
        val validation = ActionValidator.validate(action)
        if (validation is ValidationResult.Invalid) {
            return@withContext ActionResult(
                status = ActionStatus.NOT_ALLOWED,
                spokenResponse = validation.reason
            )
        }

        when (action) {
            is AssistantAction.LaunchApp -> launchApplication(action.appName, action.specificPackage)
            is AssistantAction.OpenWeb -> openWebUrl(action.url, action.searchQuery)
            is AssistantAction.OpenSettings -> openSettings(action.settingType)
            is AssistantAction.ToggleFlashlight -> toggleFlashlight(action.enable)
            is AssistantAction.AdjustVolume -> adjustVolume(action.isIncrease)
            is AssistantAction.MediaControl -> controlMedia(action.command)
            is AssistantAction.CheckBattery -> checkBattery()
            is AssistantAction.CheckNetwork -> checkNetwork()
            is AssistantAction.SetTimer -> setTimer(action.durationSeconds, action.label)
            is AssistantAction.CancelTimer -> cancelTimer()
            is AssistantAction.Remember -> rememberNote(action.key, action.value)
            is AssistantAction.Forget -> forgetNote(action.query)
            is AssistantAction.RecallMemory -> recallMemory()
            is AssistantAction.ClearAllMemory -> clearMemoryWithConfirmation()
            is AssistantAction.ReadNotifications -> readNotifications()
            is AssistantAction.SendMessage -> sendMessageWithConfirmation(action)
            is AssistantAction.AccessibilityAction -> performAccessibility(action.targetText, action.interactionType)
            is AssistantAction.TakeCameraSnapshot -> ActionResult(ActionStatus.SUCCESS, "Camera snapshot ready hai Piyush.")
            is AssistantAction.AnalyzeScreen -> ActionResult(ActionStatus.SUCCESS, "Screen analyze kar rahi hoon Piyush.")
        }
    }

    private fun launchApplication(appName: String, specificPackage: String?): ActionResult {
        val pm = context.packageManager
        val pkg = specificPackage ?: findPackageByName(appName)

        if (pkg == null) {
            return ActionResult(
                status = ActionStatus.UNAVAILABLE,
                spokenResponse = "$appName phone mein installed nahi hai Piyush."
            )
        }

        if (pkg == "camera_intent") {
            val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            return try {
                context.startActivity(intent)
                ActionResult(ActionStatus.SUCCESS, "Camera open kar diya Piyush.")
            } catch (e: Exception) {
                ActionResult(ActionStatus.FAILED, "Camera open nahi ho paya.")
            }
        }

        if (pkg == "dialer_intent") {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            return try {
                context.startActivity(intent)
                ActionResult(ActionStatus.SUCCESS, "Dialer open kar diya Piyush.")
            } catch (e: Exception) {
                ActionResult(ActionStatus.FAILED, "Dialer open nahi ho paya.")
            }
        }

        val launchIntent = pm.getLaunchIntentForPackage(pkg)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(launchIntent)
                ActionResult(
                    status = ActionStatus.SUCCESS,
                    spokenResponse = "Bilkul Piyush, $appName open kar rahi hoon."
                )
            } catch (e: Exception) {
                Logger.e("Launch failed", e)
                ActionResult(
                    status = ActionStatus.FAILED,
                    spokenResponse = "$appName open nahi ho paya Piyush."
                )
            }
        } else {
            ActionResult(
                status = ActionStatus.UNAVAILABLE,
                spokenResponse = "$appName phone mein installed nahi hai Piyush."
            )
        }
    }

    private fun findPackageByName(name: String): String? {
        val lower = name.lowercase().trim()
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(0)
        for (appInfo in packages) {
            val label = pm.getApplicationLabel(appInfo).toString().lowercase()
            if (label == lower || label.contains(lower)) {
                return appInfo.packageName
            }
        }
        return null
    }

    private fun openWebUrl(url: String?, searchQuery: String?): ActionResult {
        val targetUrl = url ?: "https://www.google.com/search?q=${Uri.encode(searchQuery ?: "")}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            ActionResult(ActionStatus.SUCCESS, "Page open kar diya Piyush.")
        } catch (e: Exception) {
            ActionResult(ActionStatus.FAILED, "Browser open karne mein dikkat aayi.")
        }
    }

    private fun openSettings(type: String): ActionResult {
        val actionIntent = when (type.lowercase()) {
            "wifi" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "display" -> Settings.ACTION_DISPLAY_SETTINGS
            "sound" -> Settings.ACTION_SOUND_SETTINGS
            "battery" -> Settings.ACTION_BATTERY_SAVER_SETTINGS
            "apps" -> Settings.ACTION_APPLICATION_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        val intent = Intent(actionIntent).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            ActionResult(ActionStatus.SUCCESS, "Settings khol di Piyush.")
        } catch (e: Exception) {
            ActionResult(ActionStatus.FAILED, "Settings open nahi ho payi.")
        }
    }

    private fun toggleFlashlight(enable: Boolean): ActionResult {
        if (cameraManager == null) {
            return ActionResult(ActionStatus.UNAVAILABLE, "Flashlight control available nahi hai.")
        }
        return try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            if (cameraId == null) {
                ActionResult(ActionStatus.UNAVAILABLE, "Flashlight hardware nahi mila.")
            } else {
                cameraManager.setTorchMode(cameraId, enable)
                val resp = if (enable) "Flashlight on kar di Piyush 🔦" else "Flashlight off kar di Piyush."
                ActionResult(ActionStatus.SUCCESS, resp)
            }
        } catch (e: CameraAccessException) {
            ActionResult(ActionStatus.FAILED, "Flashlight control nahi ho payi: ${e.message}")
        } catch (e: Exception) {
            ActionResult(ActionStatus.FAILED, "Flashlight error.")
        }
    }

    private fun adjustVolume(isIncrease: Boolean): ActionResult {
        if (audioManager == null) {
            return ActionResult(ActionStatus.UNAVAILABLE, "Volume control available nahi hai.")
        }
        val direction = if (isIncrease) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
        val text = if (isIncrease) "Volume badha di Piyush 🔊" else "Volume kam kar di Piyush 🔉"
        return ActionResult(ActionStatus.SUCCESS, text)
    }

    private fun controlMedia(command: String): ActionResult {
        val keyCode = when (command.uppercase()) {
            "PLAY" -> KeyEvent.KEYCODE_MEDIA_PLAY
            "PAUSE" -> KeyEvent.KEYCODE_MEDIA_PAUSE
            "NEXT" -> KeyEvent.KEYCODE_MEDIA_NEXT
            "PREVIOUS" -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
            else -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
        }
        val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val upEvent = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        audioManager?.dispatchMediaKeyEvent(downEvent)
        audioManager?.dispatchMediaKeyEvent(upEvent)
        return ActionResult(ActionStatus.SUCCESS, "Media command '$command' execute kar diya.")
    }

    private fun checkBattery(): ActionResult {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, filter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1

        val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else level
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val msg = if (isCharging) {
            "Piyush, battery $pct% hai aur phone charge ho raha hai ⚡"
        } else {
            "Piyush, battery abhi $pct% hai."
        }
        return ActionResult(ActionStatus.SUCCESS, msg)
    }

    private fun checkNetwork(): ActionResult {
        val isDeviceOnline = networkMonitor.isCurrentlyOnline()
        val msg = if (isDeviceOnline) {
            "Device online hai Piyush, aur internet smoothly chal raha hai 🌐"
        } else {
            "Piyush, phone abhi internet se connect nahi hai."
        }
        return ActionResult(ActionStatus.SUCCESS, msg)
    }

    private fun setTimer(seconds: Int, label: String?): ActionResult {
        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, label ?: "MYRAA Timer")
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            ActionResult(ActionStatus.SUCCESS, "$seconds seconds ka timer set kar diya Piyush ⏱️")
        } catch (e: Exception) {
            ActionResult(ActionStatus.FAILED, "Timer set nahi ho paya.")
        }
    }

    private fun cancelTimer(): ActionResult {
        return ActionResult(ActionStatus.SUCCESS, "Timer cancel karne ke liye system timer app check karein.")
    }

    private suspend fun rememberNote(key: String, value: String): ActionResult {
        val response = memoryManager.remember(key, value)
        return ActionResult(ActionStatus.SUCCESS, response)
    }

    private suspend fun forgetNote(query: String): ActionResult {
        val response = memoryManager.forget(query)
        return ActionResult(ActionStatus.SUCCESS, response)
    }

    private suspend fun recallMemory(): ActionResult {
        val response = memoryManager.recallAll()
        return ActionResult(ActionStatus.SUCCESS, response)
    }

    private fun clearMemoryWithConfirmation(): ActionResult {
        confirmationManager.requestConfirmation(
            ConfirmationRequest(
                action = AssistantAction.ClearAllMemory,
                title = "Clear All Memories?",
                message = "Piyush, kya aap sach mein apni sari saved memories delete karna chahte hain?",
                riskLevel = RiskLevel.HIGH,
                onConfirm = {
                    val resp = memoryManager.clearAll()
                    ActionResult(ActionStatus.SUCCESS, resp)
                }
            )
        )
        return ActionResult(
            status = ActionStatus.REQUIRES_CONFIRMATION,
            spokenResponse = "Piyush, sari memory clear karne ke liye confirm karein."
        )
    }

    private fun readNotifications(): ActionResult {
        if (!MyraaNotificationListenerService.isConnected) {
            return ActionResult(
                status = ActionStatus.REQUIRES_PERMISSION,
                spokenResponse = "Piyush, pehle Settings mein jaakar Notification Access enable kijiye."
            )
        }
        val notifications = MyraaNotificationListenerService.getActiveNotifications()
        if (notifications.isEmpty()) {
            return ActionResult(ActionStatus.SUCCESS, "Piyush, koi nayi unread notification nahi hai.")
        }
        val count = notifications.size
        val summary = notifications.take(3).joinToString("; ") { "${it.app}: ${it.title} - ${it.text}" }
        return ActionResult(
            status = ActionStatus.SUCCESS,
            spokenResponse = "Piyush, $count active notifications hain. Top updates: $summary"
        )
    }

    private fun sendMessageWithConfirmation(action: AssistantAction.SendMessage): ActionResult {
        val promptText = "Message ye hai: '${action.messageText}'. Send kar doon Piyush?"
        confirmationManager.requestConfirmation(
            ConfirmationRequest(
                action = action,
                title = "Send Message via ${action.app}",
                message = promptText,
                riskLevel = RiskLevel.MEDIUM,
                onConfirm = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, action.messageText)
                        setPackage("com.whatsapp")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try {
                        context.startActivity(sendIntent)
                        ActionResult(ActionStatus.SUCCESS, "Message send kar diya gaya Piyush.")
                    } catch (e: Exception) {
                        ActionResult(ActionStatus.FAILED, "Message bhejne mein dikkat aayi.")
                    }
                }
            )
        )
        return ActionResult(
            status = ActionStatus.REQUIRES_CONFIRMATION,
            spokenResponse = promptText
        )
    }

    private fun performAccessibility(targetText: String, type: String): ActionResult {
        if (!MyraaAccessibilityService.isServiceRunning()) {
            return ActionResult(
                status = ActionStatus.REQUIRES_PERMISSION,
                spokenResponse = "Piyush, is action ke liye Accessibility Service enable honi chahiye."
            )
        }
        val success = when (type.uppercase()) {
            "CLICK" -> MyraaAccessibilityService.clickNodeByText(targetText)
            "SCROLL_DOWN" -> MyraaAccessibilityService.scrollForward()
            "SCROLL_UP" -> MyraaAccessibilityService.scrollBackward()
            else -> false
        }
        return if (success) {
            ActionResult(ActionStatus.SUCCESS, "Action execute ho gaya Piyush.")
        } else {
            ActionResult(ActionStatus.FAILED, "Piyush, action verify nahi ho paya.")
        }
    }
}
