package io.github.winter000boy.minimalist.system

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager

/**
 * Exists only to lock the screen on a double-tap of the home screen: Android lets a home app do
 * that through an accessibility service, and through nothing that keeps fingerprint and face
 * unlock working. It subscribes to no events and cannot read the screen (see
 * res/xml/lock_screen_service.xml).
 */
class LockScreenService : AccessibilityService() {

    override fun onServiceConnected() {
        running = this
    }

    override fun onUnbind(intent: Intent?): Boolean {
        running = null
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    companion object {
        @Volatile
        private var running: LockScreenService? = null

        /** Android 9+ only. False when the user hasn't turned the service on. */
        fun lockScreen(): Boolean =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                running?.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN) == true
    }
}

fun Context.isLockScreenServiceOn(): Boolean =
    getSystemService(AccessibilityManager::class.java)
        .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        .any {
            val service = it.resolveInfo.serviceInfo
            service.packageName == packageName && service.name == LockScreenService::class.java.name
        }
