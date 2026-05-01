package ai.could.pushunlock.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ai.could.pushunlock.data.AppDatabase
import ai.could.pushunlock.data.UnlockSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UnlockViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()

    private val _pushupsDone = MutableStateFlow(0)
    val pushupsDone: StateFlow<Int> = _pushupsDone

    private val _targetPushups = MutableStateFlow(10)
    val targetPushups: StateFlow<Int> = _targetPushups

    private val _unlockDuration = MutableStateFlow(15)

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val settings = dao.getSettings()
            if (settings != null) {
                _targetPushups.value = settings.pushupsRequired
                _unlockDuration.value = settings.unlockDurationMinutes
            }
        }
    }

    fun onPushupCompleted() {
        if (_isUnlocked.value) return
        val current = _pushupsDone.value + 1
        _pushupsDone.value = current
        if (current >= _targetPushups.value) {
            _isUnlocked.value = true
        }
    }

    fun saveUnlockSession(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val expiresAt = now + (_unlockDuration.value * 60 * 1000)
            dao.insertUnlockSession(
                UnlockSession(
                    packageName = packageName,
                    unlockedAt = now,
                    expiresAt = expiresAt,
                    pushupsCompleted = _pushupsDone.value
                )
            )
        }
    }
}
