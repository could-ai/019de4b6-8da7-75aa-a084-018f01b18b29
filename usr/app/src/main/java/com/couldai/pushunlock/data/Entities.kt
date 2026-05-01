package com.couldai.pushunlock.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_apps")
data class BlockedApp(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isBlocked: Boolean = true,
    val unlockedUntil: Long = 0L // Timestamp until which the app is unlocked (0 if blocked)
)

@Entity(tableName = "unlock_history")
data class UnlockHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val timestamp: Long,
    val pushupsDone: Int,
    val timeUnlockedMillis: Long
)
