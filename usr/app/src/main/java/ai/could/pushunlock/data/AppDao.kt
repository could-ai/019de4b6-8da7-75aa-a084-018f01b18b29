package ai.could.pushunlock.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM blocked_apps")
    fun getAllBlockedApps(): Flow<List<BlockedApp>>

    @Query("SELECT * FROM blocked_apps WHERE isBlocked = 1")
    suspend fun getBlockedAppsList(): List<BlockedApp>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedApp(app: BlockedApp)

    @Query("DELETE FROM blocked_apps WHERE packageName = :packageName")
    suspend fun removeBlockedApp(packageName: String)

    @Query("SELECT * FROM blocked_apps WHERE packageName = :packageName AND isBlocked = 1")
    suspend fun getBlockedApp(packageName: String): BlockedApp?

    @Insert
    suspend fun insertUnlockSession(session: UnlockSession)

    @Query("SELECT * FROM unlock_sessions ORDER BY unlockedAt DESC")
    fun getAllSessions(): Flow<List<UnlockSession>>

    @Query("SELECT * FROM unlock_sessions WHERE packageName = :packageName ORDER BY expiresAt DESC LIMIT 1")
    suspend fun getLatestSessionForApp(packageName: String): UnlockSession?

    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettings(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AppSettings)
}
