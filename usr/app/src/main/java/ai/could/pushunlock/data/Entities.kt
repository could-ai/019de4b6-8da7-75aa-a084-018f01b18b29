package ai.could.pushunlock.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_apps")
data class BlockedApp(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isBlocked: Boolean = true
)

@Entity(tableName = "unlock_sessions")
data class UnlockSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val unlockedAt: Long,
    val expiresAt: Long,
    val pushupsCompleted: Int
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val pushupsRequired: Int = 10,
    val unlockDurationMinutes: Int = 15
)
