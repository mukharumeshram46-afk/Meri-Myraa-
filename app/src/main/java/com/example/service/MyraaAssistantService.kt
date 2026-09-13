package com.example.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyraaAssistantService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var isPaused = false

    override fun onCreate() {
        super.onCreate()
        _isServiceActive.value = true
        Logger.i("MyraaAssistantService onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ServiceNotification.ACTION_STOP -> {
                Logger.i("Stop requested on MyraaAssistantService")
                stopSelf()
                return START_NOT_STICKY
            }
            ServiceNotification.ACTION_PAUSE -> {
                isPaused = true
                _isServicePaused.value = true
                updateNotification()
            }
            ServiceNotification.ACTION_RESUME -> {
                isPaused = false
                _isServicePaused.value = false
                updateNotification()
            }
            else -> {
                startForeground(
                    ServiceNotification.NOTIFICATION_ID,
                    ServiceNotification.buildNotification(this, isPaused)
                )
            }
        }
        return START_STICKY
    }

    private fun updateNotification() {
        val notification = ServiceNotification.buildNotification(this, isPaused)
        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(ServiceNotification.NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        _isServiceActive.value = false
        _isServicePaused.value = false
        Logger.i("MyraaAssistantService onDestroy")
    }

    companion object {
        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        private val _isServicePaused = MutableStateFlow(false)
        val isServicePaused: StateFlow<Boolean> = _isServicePaused.asStateFlow()
    }
}
