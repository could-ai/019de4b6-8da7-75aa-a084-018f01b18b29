package ai.could.pushunlock.ui.viewmodels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ai.could.pushunlock.data.AppDatabase
import ai.could.pushunlock.data.BlockedApp
import ai.could.pushunlock.data.AppSettings
import ai.could.pushunlock.ui.models.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    private val packageManager = application.packageManager

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings

    init {
        viewModelScope.launch {
            dao.getSettingsFlow().collect { currentSettings ->
                if (currentSettings != null) {
                    _settings.value = currentSettings
                } else {
                    dao.saveSettings(AppSettings())
                }
            }
        }
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val blockedAppsList = dao.getBlockedAppsList().map { it.packageName }.toSet()
            val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            val filteredApps = apps.filter {
                (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                (it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            }.map {
                InstalledApp(
                    packageName = it.packageName,
                    appName = packageManager.getApplicationLabel(it).toString(),
                    icon = packageManager.getApplicationIcon(it),
                    isBlocked = blockedAppsList.contains(it.packageName)
                )
            }.sortedBy { it.appName }
            _installedApps.value = filteredApps
        }
    }

    fun toggleAppBlock(app: InstalledApp) {
        viewModelScope.launch(Dispatchers.IO) {
            if (app.isBlocked) {
                dao.removeBlockedApp(app.packageName)
            } else {
                dao.insertBlockedApp(BlockedApp(app.packageName, app.appName))
            }
            loadApps()
        }
    }

    fun updateSettings(pushups: Int, duration: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val newSettings = AppSettings(pushupsRequired = pushups, unlockDurationMinutes = duration)
            dao.saveSettings(newSettings)
        }
    }
}
