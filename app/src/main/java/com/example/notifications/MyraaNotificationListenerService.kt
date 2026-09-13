package com.example.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.utils.Logger
import java.lang.ref.WeakReference

data class MyraaNotification(
    val id: Int,
    val packageName: String,
    val app: String,
    val title: String,
    val text: String,
    val timestamp: Long
)

class MyraaNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = WeakReference(this)
        isConnected = true
        Logger.i("MyraaNotificationListenerService connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isConnected = false
        instance = null
        Logger.i("MyraaNotificationListenerService disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Can be observed safely by UI/Assistant
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Updated state
    }

    companion object {
        var isConnected: Boolean = false
            private set
        private var instance: WeakReference<MyraaNotificationListenerService>? = null

        fun getActiveNotifications(): List<MyraaNotification> {
            val service = instance?.get() ?: return emptyList()
            return try {
                val sbns = service.activeNotifications ?: return emptyList()
                val pm = service.packageManager
                sbns.mapNotNull { sbn ->
                    val extras = sbn.notification?.extras ?: return@mapNotNull null
                    val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
                    val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                    if (title.isBlank() && text.isBlank()) return@mapNotNull null

                    val appName = try {
                        val appInfo = pm.getApplicationInfo(sbn.packageName, 0)
                        pm.getApplicationLabel(appInfo).toString()
                    } catch (e: Exception) {
                        sbn.packageName
                    }

                    MyraaNotification(
                        id = sbn.id,
                        packageName = sbn.packageName,
                        app = appName,
                        title = title,
                        text = text,
                        timestamp = sbn.postTime
                    )
                }
            } catch (e: Exception) {
                Logger.e("Error fetching notifications", e)
                emptyList()
            }
        }
    }
}
