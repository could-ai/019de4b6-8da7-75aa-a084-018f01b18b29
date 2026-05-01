package com.couldai.pushunlock.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {
    val allBlockedApps: Flow<List<BlockedApp>> = appDao.getAllBlockedApps()
    val unlockHistory: Flow<List<UnlockHistory>> = appDao.getUnlockHistory()

    suspend fun addBlockedApp(packageName: String, appName: String) {
        appDao.insertBlockedApp(BlockedApp(packageName = packageName, appName = appName, isBlocked = true))
    }

    suspend fun removeBlockedApp(packageName: String) {
        appDao.deleteBlockedApp(packageName)
    }

    suspend fun unlockApp(packageName: String, durationMillis: Long) {
        val app = appDao.getBlockedApp(packageName)
        if (app != null) {
            appDao.updateBlockedApp(app.copy(unlockedUntil = System.currentTimeMillis() + durationMillis))
        }
    }

    suspend fun recordUnlockHistory(history: UnlockHistory) {
        appDao.insertUnlockHistory(history)
    }
}
