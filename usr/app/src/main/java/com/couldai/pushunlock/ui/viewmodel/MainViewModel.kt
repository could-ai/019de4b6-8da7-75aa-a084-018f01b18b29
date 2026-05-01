package com.couldai.pushunlock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.couldai.pushunlock.data.AppDatabase
import com.couldai.pushunlock.data.AppRepository
import com.couldai.pushunlock.data.BlockedApp
import com.couldai.pushunlock.domain.InstalledApp
import com.couldai.pushunlock.domain.getInstalledApps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository
    
    val blockedApps: StateFlow<List<BlockedApp>>
    
    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    init {
        val appDao = AppDatabase.getDatabase(application).appDao()
        repository = AppRepository(appDao)
        blockedApps = repository.allBlockedApps.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            _installedApps.value = getInstalledApps(getApplication())
        }
    }

    fun toggleAppBlock(app: InstalledApp, isCurrentlyBlocked: Boolean) {
        viewModelScope.launch {
            if (isCurrentlyBlocked) {
                repository.removeBlockedApp(app.packageName)
            } else {
                repository.addBlockedApp(app.packageName, app.appName)
            }
        }
    }
}
