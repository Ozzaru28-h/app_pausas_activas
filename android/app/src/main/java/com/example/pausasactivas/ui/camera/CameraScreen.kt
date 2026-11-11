package com.example.pausasactivas.ui.camera

import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pausasactivas.data.pose.PoseAnalyzer
import com.example.pausasactivas.domain.rules.PostureStatus
import java.util.concurrent.Executors

private val ColorCorrect = Color(0xFF2E7D32)
private val ColorAdjust = Color(0xFFC62828)
private val ColorNeutral = Color(0xFF1565C0)
private val ColorNotVisible = Color(0xFFEF6C00)
private val ColorNoPerson = Color(0xFF616161)

private fun statusColor(status: PostureStatus): Color = when (status) {
    PostureStatus.CORRECT -> ColorCorrect
    PostureStatus.ADJUST -> ColorAdjust
    PostureStatus.NEUTRAL -> ColorNeutral
    PostureStatus.NOT_VISIBLE -> ColorNotVisible
    PostureStatus.NO_PERSON -> ColorNoPerson
}

private fun statusLabel(status: PostureStatus): String = when (status) {
    PostureStatus.CORRECT -> "Postura correcta"
    PostureStatus.ADJUST -> "Ajusta tu postura"
    PostureStatus.NEUTRAL -> "Listo para el movimiento"
    PostureStatus.NOT_VISIBLE -> "Cuerpo parcialmente visible"
    PostureStatus.NO_PERSON -> "No se detecta a nadie"
}

@Composable
fun CameraScreen(
    onClose: () -> Unit,
    viewModel: CameraViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val poseAnalyzer = remember {
        PoseAnalyzer { detectedPose -> viewModel.processPose(detectedPose) }
    }
    var cameraError by remember { mutableStateOf<String?>(null) }

    BackHandler {
        viewModel.stop()
        onClose()
    }

    // Al salir de la pantalla se suelta la cámara y se liberan ML Kit y el hilo de análisis.
    DisposableEffect(Unit) {
        onDispose {
            if (cameraProviderFuture.isDone) {
                runCatching { cameraProviderFuture.get().unbindAll() }
            }
            cameraExecutor.shutdown()
            poseAnalyzer.close()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Vista previa de la cámara real
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(cameraExecutor, poseAnalyzer)
                        }

                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                        cameraError = null
                    } catch (exc: Exception) {
                        Log.e("CameraScreen", "Error al vincular CameraX", exc)
                        cameraError = "No se pudo iniciar la cámara frontal de este dispositivo."
                    }
                }, ContextCompat.getMainExecutor(context))

                previewView
            }
        )

        // Tarjeta superior: ejercicio, estado visual y mensaje de retroalimentación
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp)
                .align(Alignment.TopCenter),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = uiState.exercise?.nombre ?: "Ejercicio",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (cameraError != null) {
                    Text(
                        text = cameraError.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else if (uiState.phase != SessionPhase.FINISHED) {
                    Surface(
                        color = statusColor(uiState.status),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = statusLabel(uiState.status),
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    if (uiState.phase == SessionPhase.RUNNING) {
                        Text(
                            text = "Tiempo restante: ${uiState.secondsLeft} s" +
                                (uiState.livePrecisionPct?.let { "  ·  Precisión: $it %" } ?: ""),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        LinearProgressIndicator(
                            progress = {
                                1f - uiState.secondsLeft.toFloat() / EXERCISE_DURATION_SECONDS
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "El tiempo de ${EXERCISE_DURATION_SECONDS} s empieza cuando " +
                                "te detectemos frente a la cámara.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Text(
                    text = "Demostración académica; no es un diagnóstico médico.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Zona inferior: botones o resultado final
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (uiState.phase == SessionPhase.FINISHED) {
                ResultCard(
                    state = uiState,
                    onRetrySave = viewModel::retrySave,
                    onRepeat = viewModel::restart,
                    onClose = onClose
                )
            } else {
                if (uiState.phase == SessionPhase.RUNNING) {
                    Button(
                        onClick = viewModel::finishNow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Finalizar y guardar sesión")
                    }
                }
                OutlinedButton(
                    onClick = {
                        viewModel.stop()
                        onClose()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    state: AnalysisState,
    onRetrySave: () -> Unit,
    onRepeat: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val summary = state.summary

            if (summary == null) {
                Text(
                    text = "No se pudo evaluar el ejercicio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "No detectamos el movimiento completo. Asegúrate de que se vea tu " +
                        "cuerpo y realiza el ejercicio. La sesión no se guardó.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "Resultado de la sesión",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Precisión de postura: ${summary.precisionPct.toInt()} %  ·  " +
                        if (summary.posturaValida) "Postura válida" else "Por mejorar",
                    style = MaterialTheme.typography.bodyLarge
                )
                summary.errors.forEach { error ->
                    Text(
                        text = "• ${error.articulacionFalla}: ${error.anguloDetectado?.toInt()}° " +
                            "detectado (esperado ${error.anguloEsperado?.toInt()}°)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                when (val save = state.saveState) {
                    SaveState.Saving -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(18.dp),
                            strokeWidth = 2.dp
                        )
                        Text("Guardando sesión…")
                    }

                    SaveState.Saved -> Text(
                        text = "Sesión guardada correctamente.",
                        color = ColorCorrect,
                        fontWeight = FontWeight.Bold
                    )

                    is SaveState.Error -> {
                        Text(
                            text = save.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedButton(
                            onClick = onRetrySave,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reintentar guardado")
                        }
                    }

                    else -> Unit
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onRepeat, modifier = Modifier.weight(1f)) {
                    Text("Repetir")
                }
                Button(onClick = onClose, modifier = Modifier.weight(1f)) {
                    Text("Volver al inicio")
                }
            }
        }
    }
}
