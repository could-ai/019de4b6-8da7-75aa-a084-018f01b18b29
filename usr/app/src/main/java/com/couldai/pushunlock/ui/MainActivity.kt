package com.couldai.pushunlock.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.couldai.pushunlock.data.BlockedApp
import com.couldai.pushunlock.domain.AppInfo
import com.couldai.pushunlock.ui.theme.PushUnlockTheme
import com.couldai.pushunlock.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PushUnlockTheme {
                val installedApps by viewModel.installedApps.collectAsState()
                val blockedApps by viewModel.blockedApps.collectAsState(initial = emptyList())
                val context = LocalContext.current

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("PushUnlock") },
                            actions = {
                                Button(onClick = {
                                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                }) {
                                    Text("Permissões")
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        if (installedApps.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else {
                            LazyColumn {
                                items(installedApps) { app ->
                                    val isBlocked = blockedApps.any { it.packageName == app.packageName }
                                    AppListItem(
                                        appInfo = app,
                                        isBlocked = isBlocked,
                                        onToggle = { blocked ->
                                            if (blocked) {
                                                viewModel.addBlockedApp(
                                                    BlockedApp(
                                                        packageName = app.packageName,
                                                        appName = app.name,
                                                        pushupsRequired = 5 // default
                                                    )
                                                )
                                            } else {
                                                val existing = blockedApps.find { it.packageName == app.packageName }
                                                existing?.let { viewModel.removeBlockedApp(it) }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppListItem(appInfo: AppInfo, isBlocked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bitmap = remember(appInfo.icon) { appInfo.icon.toBitmap().asImageBitmap() }
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = appInfo.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = appInfo.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = isBlocked,
            onCheckedChange = onToggle
        )
    }
}
