package com.couldai.pushunlock.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.couldai.pushunlock.domain.PushupAnalyzer
import com.couldai.pushunlock.service.AppBlockerService
import com.couldai.pushunlock.ui.theme.PushUnlockTheme
import java.util.concurrent.Executors

class UnlockActivity : ComponentActivity() {

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var packageNameToUnlock: String? = null
    private var requiredPushups: Int = 5

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Permissão de câmera é obrigatória", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        packageNameToUnlock = intent.getStringExtra("PACKAGE_NAME")
        requiredPushups = intent.getIntExtra("REQUIRED_PUSHUPS", 5)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        setContent {
            PushUnlockTheme {
                UnlockScreen(
                    requiredPushups = requiredPushups,
                    onUnlocked = {
                        packageNameToUnlock?.let {
                            AppBlockerService.unlockedApps[it] = System.currentTimeMillis() + (15 * 60 * 1000) // 15 mins
                        }
                        finish()
                    },
                    analyzerExecutor = cameraExecutor
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

@Composable
fun UnlockScreen(
    requiredPushups: Int,
    onUnlocked: () -> Unit,
    analyzerExecutor: java.util.concurrent.ExecutorService
) {
    var pushupsDone by remember { mutableStateOf(0) }
    var bodyDetected by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val analyzer = PushupAnalyzer(
                        onPushupDetected = { count ->
                            pushupsDone = count
                            if (pushupsDone >= requiredPushups) {
                                onUnlocked()
                            }
                        },
                        onBodyFound = { found ->
                            bodyDetected = found
                        }
                    )

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(analyzerExecutor, analyzer)
                        }

                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, preview, imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
                .background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.medium)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Flexões: $pushupsDone / $requiredPushups",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            if (!bodyDetected) {
                Text(
                    text = "Posicione-se na câmera",
                    color = Color.Red,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
