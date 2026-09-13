package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.service.MyraaAssistantService
import com.example.settings.SettingsRepository
import com.example.utils.Logger

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Logger.i("Boot completed received in MYRAA BootReceiver")
            val settingsRepo = SettingsRepository(context)
            val settings = settingsRepo.settings.value
            // Only start background service if explicitly configured by Piyush
            if (settings.isBackgroundModeEnabled && settings.isAssistantActive) {
                val serviceIntent = Intent(context, MyraaAssistantService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                Logger.i("Restored MYRAA Background Service after boot")
            }
        }
    }
}
