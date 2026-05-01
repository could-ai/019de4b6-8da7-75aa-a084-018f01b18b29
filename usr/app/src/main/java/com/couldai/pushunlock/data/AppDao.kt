package com.couldai.pushunlock.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM blocked_apps")
    fun getAllBlockedApps(): Flow<List<BlockedApp>>

    @Query("SELECT * FROM blocked_apps WHERE isBlocked = 1")
    fun getActiveBlockedApps(): Flow<List<BlockedApp>>

    @Query("SELECT * FROM blocked_apps WHERE packageName = :packageName")
    suspend fun getBlockedApp(packageName: String): BlockedApp?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedApp(app: BlockedApp)
    
    @Update
    suspend fun updateBlockedApp(app: BlockedApp)

    @Query("DELETE FROM blocked_apps WHERE packageName = :packageName")
    suspend fun deleteBlockedApp(packageName: String)

    @Insert
    suspend fun insertUnlockHistory(history: UnlockHistory)

    @Query("SELECT * FROM unlock_history ORDER BY timestamp DESC")
    fun getUnlockHistory(): Flow<List<UnlockHistory>>
}
