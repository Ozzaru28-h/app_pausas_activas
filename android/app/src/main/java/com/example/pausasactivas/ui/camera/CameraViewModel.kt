package com.example.pausasactivas.ui.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pausasactivas.data.api.ExerciseRefDto
import com.example.pausasactivas.data.api.MetricErrorDto
import com.example.pausasactivas.data.api.SessionRequest
import com.example.pausasactivas.data.api.TelemetryDto
import com.example.pausasactivas.data.repository.PausasRepository
import com.example.pausasactivas.domain.model.DetectedPose
import com.example.pausasactivas.domain.model.Exercise
import com.example.pausasactivas.domain.rules.PostureRules
import com.example.pausasactivas.domain.rules.PostureStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class SessionPhase { WAITING, RUNNING, FINISHED }

sealed interface SaveState {
    data object Idle : SaveState
    data object Saving : SaveState
    data object Saved : SaveState

    /** No hubo datos suficientes: la sesión NO se guarda (no se inventan resultados). */
    data object NotEvaluable : SaveState
    data class Error(val message: String) : SaveState
}

data class SessionSummary(
    val precisionPct: Double,
    val posturaValida: Boolean,
    val errors: List<MetricErrorDto>
)

data class AnalysisState(
    val exercise: Exercise? = null,
    val phase: SessionPhase = SessionPhase.WAITING,
    val status: PostureStatus = PostureStatus.NO_PERSON,
    val message: String = "Colócate frente a la cámara",
    val secondsLeft: Int = EXERCISE_DURATION_SECONDS,
    val livePrecisionPct: Int? = null,
    val summary: SessionSummary? = null,
    val saveState: SaveState = SaveState.Idle
)

const val EXERCISE_DURATION_SECONDS = 30

class CameraViewModel : ViewModel() {

    private val repository = PausasRepository()

    private val _uiState = MutableStateFlow(AnalysisState())
    val uiState: StateFlow<AnalysisState> = _uiState.asStateFlow()

    private var userId: String = ""
    private var timerJob: Job? = null
    private var correctFrames = 0
    private var adjustFrames = 0
    private val errorStats = mutableMapOf<String, ErrorAccumulator>()

    private class ErrorAccumulator(
        var count: Int = 0,
        var angleSum: Double = 0.0,
        var expected: Float = 0f
    )

    /** Prepara una nueva sesión para [exercise]. Se llama antes de abrir la pantalla de cámara. */
    fun start(exercise: Exercise, userId: String) {
        timerJob?.cancel()
        this.userId = userId
        resetCounters()
        _uiState.value = AnalysisState(exercise = exercise)
    }

    /** Repite el mismo ejercicio desde cero. */
    fun restart() {
        val exercise = _uiState.value.exercise ?: return
        start(exercise, userId)
    }

    /** Detiene el temporizador (al salir de la pantalla). */
    fun stop() {
        timerJob?.cancel()
    }

    fun processPose(pose: DetectedPose?) {
        val current = _uiState.value
        val exercise = current.exercise ?: return
        if (current.phase == SessionPhase.FINISHED) return

        val feedback = PostureRules.evaluate(exercise.codigo, pose)
        val isEvaluable = feedback.status == PostureStatus.NEUTRAL ||
            feedback.status == PostureStatus.CORRECT ||
            feedback.status == PostureStatus.ADJUST

        var phase = current.phase
        if (phase == SessionPhase.WAITING && isEvaluable) {
            phase = SessionPhase.RUNNING
            startTimer()
        }

        if (phase == SessionPhase.RUNNING) {
            when (feedback.status) {
                PostureStatus.CORRECT -> correctFrames++
                PostureStatus.ADJUST -> {
                    adjustFrames++
                    feedback.error?.let { error ->
                        val acc = errorStats.getOrPut(error.joint) { ErrorAccumulator() }
                        acc.count++
                        acc.angleSum += error.detectedAngle
                        acc.expected = error.expectedAngle
                    }
                }
                else -> Unit
            }
        }

        val activeFrames = correctFrames + adjustFrames
        val livePrecision = if (activeFrames > 0) {
            (correctFrames * 100.0 / activeFrames).roundToInt()
        } else {
            null
        }

        _uiState.update {
            it.copy(
                phase = phase,
                status = feedback.status,
                message = feedback.message,
                livePrecisionPct = livePrecision
            )
        }
    }

    /** Botón "Finalizar y guardar". */
    fun finishNow() {
        if (_uiState.value.phase == SessionPhase.FINISHED) return
        timerJob?.cancel()
        finishSession()
    }

    /** Reintenta el envío cuando falló la conexión. */
    fun retrySave() = saveSession()

    // ------------------------------------------------------------------ Internos

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var left = EXERCISE_DURATION_SECONDS
            while (left > 0) {
                delay(1000)
                left--
                _uiState.update { it.copy(secondsLeft = left) }
            }
            finishSession()
        }
    }

    private fun finishSession() {
        val activeFrames = correctFrames + adjustFrames
        if (activeFrames < MIN_ACTIVE_FRAMES) {
            _uiState.update {
                it.copy(
                    phase = SessionPhase.FINISHED,
                    summary = null,
                    saveState = SaveState.NotEvaluable
                )
            }
            return
        }

        val precision = round1(correctFrames * 100.0 / activeFrames)
        val errors = errorStats
            .filter { (_, acc) -> acc.count * 100.0 / activeFrames >= ERROR_MIN_SHARE_PCT }
            .map { (joint, acc) ->
                MetricErrorDto(
                    articulacionFalla = joint,
                    anguloDetectado = round1(acc.angleSum / acc.count),
                    anguloEsperado = acc.expected.toDouble()
                )
            }

        _uiState.update {
            it.copy(
                phase = SessionPhase.FINISHED,
                summary = SessionSummary(
                    precisionPct = precision,
                    posturaValida = precision >= VALID_POSTURE_THRESHOLD_PCT,
                    errors = errors
                ),
                saveState = SaveState.Idle
            )
        }
        saveSession()
    }

    private fun saveSession() {
        val state = _uiState.value
        val exercise = state.exercise ?: return
        val summary = state.summary ?: return
        if (state.saveState == SaveState.Saving) return

        _uiState.update { it.copy(saveState = SaveState.Saving) }

        viewModelScope.launch {
            val request = SessionRequest(
                usuarioId = userId,
                ejercicio = ExerciseRefDto(codigo = exercise.codigo, nombre = exercise.nombre),
                telemetria = TelemetryDto(
                    posturaValida = summary.posturaValida,
                    precisionPromedioPct = summary.precisionPct,
                    metricasError = summary.errors
                )
            )
            repository.saveSession(request).fold(
                onSuccess = { _ ->
                    _uiState.update { s -> s.copy(saveState = SaveState.Saved) }
                },
                onFailure = { e ->
                    _uiState.update { s ->
                        s.copy(
                            saveState = SaveState.Error(
                                e.message ?: "No se pudo guardar la sesión."
                            )
                        )
                    }
                }
            )
        }
    }

    private fun resetCounters() {
        correctFrames = 0
        adjustFrames = 0
        errorStats.clear()
    }

    private fun round1(value: Double): Double = (value * 10.0).roundToInt() / 10.0

    private companion object {
        /** Mínimo de cuadros evaluados (en posición de ejercicio) para considerar válida la sesión. */
        const val MIN_ACTIVE_FRAMES = 15

        /** Precisión mínima para marcar "postura_valida". */
        const val VALID_POSTURE_THRESHOLD_PCT = 60.0

        /** Un error se reporta si ocurrió en al menos este % de los cuadros evaluados. */
        const val ERROR_MIN_SHARE_PCT = 15.0
    }
}
