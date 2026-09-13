package com.example.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.utils.Logger
import java.lang.ref.WeakReference

class MyraaAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = WeakReference(this)
        Logger.i("MyraaAccessibilityService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Safe passive event listener
    }

    override fun onInterrupt() {
        Logger.w("MyraaAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Logger.i("MyraaAccessibilityService destroyed")
    }

    companion object {
        private var instance: WeakReference<MyraaAccessibilityService>? = null

        fun isServiceRunning(): Boolean = instance?.get() != null

        fun clickNodeByText(text: String): Boolean {
            val service = instance?.get() ?: return false
            val root = service.rootInActiveWindow ?: return false
            val nodes = root.findAccessibilityNodeInfosByText(text)
            if (nodes.isNullOrEmpty()) return false

            for (node in nodes) {
                if (node.isClickable) {
                    val clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    if (clicked) return true
                }
                var parent = node.parent
                while (parent != null) {
                    if (parent.isClickable) {
                        val clicked = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (clicked) return true
                    }
                    parent = parent.parent
                }
            }
            return false
        }

        fun scrollForward(): Boolean {
            val service = instance?.get() ?: return false
            val root = service.rootInActiveWindow ?: return false
            return root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        }

        fun scrollBackward(): Boolean {
            val service = instance?.get() ?: return false
            val root = service.rootInActiveWindow ?: return false
            return root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
        }

        fun performSwipeUp(): Boolean {
            val service = instance?.get() ?: return false
            val path = Path().apply {
                moveTo(500f, 1500f)
                lineTo(500f, 500f)
            }
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 300))
                .build()
            return service.dispatchGesture(gesture, null, null)
        }

        fun performBack(): Boolean {
            val service = instance?.get() ?: return false
            return service.performGlobalAction(GLOBAL_ACTION_BACK)
        }

        fun performHome(): Boolean {
            val service = instance?.get() ?: return false
            return service.performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }
}
