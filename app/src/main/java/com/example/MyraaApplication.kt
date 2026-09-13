package com.example

import android.app.Application
import com.example.assistant.AssistantManager
import com.example.memory.MyraaDatabase
import com.example.service.ServiceNotification
import com.example.settings.SettingsRepository
import com.example.utils.Logger
import com.example.utils.NetworkMonitor

class MyraaApplication : Application() {

    lateinit var database: MyraaDatabase
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var networkMonitor: NetworkMonitor
        private set

    lateinit var assistantManager: AssistantManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        Logger.i("MyraaApplication onCreate initializing subsystems")

        ServiceNotification.createNotificationChannel(this)

        database = MyraaDatabase.getInstance(this)
        settingsRepository = SettingsRepository(this)
        networkMonitor = NetworkMonitor(this)
        assistantManager = AssistantManager(this, database, settingsRepository, networkMonitor)
    }

    companion object {
        lateinit var instance: MyraaApplication
            private set
    }
}
