package ai.could.pushunlock.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import ai.could.pushunlock.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppBlockerService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var dao: ai.could.pushunlock.data.AppDao

    override fun onServiceConnected() {
        super.onServiceConnected()
        dao = AppDatabase.getDatabase(applicationContext).appDao()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        }
        serviceInfo = info
        Log.d("AppBlockerService", "Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        
        val packageName = event.packageName?.toString() ?: return
        
        if (packageName == applicationContext.packageName) return
        
        scope.launch {
            val blockedApp = dao.getBlockedApp(packageName)
            if (blockedApp != null) {
                val session = dao.getLatestSessionForApp(packageName)
                val now = System.currentTimeMillis()
                val isUnlocked = session != null && session.expiresAt > now

                if (!isUnlocked) {
                    Log.d("AppBlockerService", "Blocking app: $packageName")
                    withContext(Dispatchers.Main) {
                        blockApp(packageName)
                    }
                }
            }
        }
    }

    private fun blockApp(packageName: String) {
        // Needs the full class name string to avoid immediate resolution issues if not created
        val intent = Intent().apply {
            setClassName(applicationContext, "ai.could.pushunlock.ui.UnlockActivity")
            putExtra("PACKAGE_NAME", packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}
}
