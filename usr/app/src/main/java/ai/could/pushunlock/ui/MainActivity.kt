package ai.could.pushunlock.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import ai.could.pushunlock.ui.viewmodels.MainViewModel
import ai.could.pushunlock.ui.models.InstalledApp

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF00E5FF),
                    background = Color.Black,
                    surface = Color(0xFF1A1A1A),
                    onPrimary = Color.Black,
                    onBackground = Color.White,
                    onSurface = Color.White
                )
            ) {
                MainScreen(viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadApps()
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val apps by viewModel.installedApps.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var showSettings by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PushUnlock", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Button(onClick = { showSettings = true }) {
                    Text("Config")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Habilitar Serviço de Acessibilidade")
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Aplicativos Instalados", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(apps) { app ->
                    AppItem(app = app, onToggle = { viewModel.toggleAppBlock(app) })
                }
            }
        }

        if (showSettings) {
            AlertDialog(
                onDismissRequest = { showSettings = false },
                title = { Text("Configurações") },
                text = {
                    Column {
                        var pushups by remember { mutableStateOf(settings.pushupsRequired.toString()) }
                        var duration by remember { mutableStateOf(settings.unlockDurationMinutes.toString()) }
                        
                        OutlinedTextField(
                            value = pushups,
                            onValueChange = { pushups = it },
                            label = { Text("Nº de Flexões") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Tempo Desbloqueio (min)") }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            viewModel.updateSettings(
                                pushups.toIntOrNull() ?: 10,
                                duration.toIntOrNull() ?: 15
                            )
                            showSettings = false
                        }) {
                            Text("Salvar")
                        }
                    }
                },
                confirmButton = { }
            )
        }
    }
}

@Composable
fun AppItem(app: InstalledApp, onToggle: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                bitmap = app.icon.toBitmap(100, 100).asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = app.appName,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = app.isBlocked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}
