package com.couldai.pushunlock.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.couldai.pushunlock.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppBlockerService : AccessibilityService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Ignore own package
            if (packageName == this.packageName) return
            
            scope.launch {
                val db = AppDatabase.getDatabase(applicationContext)
                val blockedApp = db.appDao().getBlockedApp(packageName)
                
                if (blockedApp != null && blockedApp.isBlocked) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime > blockedApp.unlockedUntil) {
                        blockApp(packageName)
                    }
                }
            }
        }
    }

    private fun blockApp(packageName: String) {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        
        val intent = Intent().apply {
            setClassName(this@AppBlockerService, "com.couldai.pushunlock.ui.UnlockActivity")
            putExtra("PACKAGE_NAME", packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
