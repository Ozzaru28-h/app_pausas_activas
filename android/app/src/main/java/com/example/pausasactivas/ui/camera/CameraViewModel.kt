package com.example.pausasactivas.ui.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pausasactivas.data.api.RetrofitClient
import com.example.pausasactivas.data.api.SessionReportRequest
import com.example.pausasactivas.domain.model.ActivePauseState
import com.example.pausasactivas.domain.model.DetectedPose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CameraViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ActivePauseState())
    val uiState: StateFlow<ActivePauseState> = _uiState.asStateFlow()

    private var isArmsUpLastFrame = false

    fun processPose(pose: DetectedPose?) {
        if (pose == null) {
            _uiState.update {
                it.copy(
                    isPersonDetected = false,
                    feedbackMessage = "Colócate frente a la cámara",
                    pose = null
                )
            }
            return
        }

        // Detección simple: Verificar si ambas muñecas están por encima de los hombros
        val leftWrist = pose.leftWrist
        val rightWrist = pose.rightWrist
        val leftShoulder = pose.leftShoulder
        val rightShoulder = pose.rightShoulder

        val isArmsUp = leftWrist != null && rightWrist != null &&
                leftShoulder != null && rightShoulder != null &&
                leftWrist.y < leftShoulder.y && rightWrist.y < rightShoulder.y

        var newRepCount = _uiState.value.repetitionCount
        var feedback = "Mantén la postura o eleva los brazos"

        if (isArmsUp && !isArmsUpLastFrame) {
            newRepCount++
            feedback = "¡Excelente repetición! ($newRepCount/${_uiState.value.targetRepetitions})"
        } else if (isArmsUp) {
            feedback = "¡Bien! Brazos arriba sostenidos"
        }

        isArmsUpLastFrame = isArmsUp

        val isCompleted = newRepCount >= _uiState.value.targetRepetitions

        _uiState.update {
            it.copy(
                isPersonDetected = true,
                repetitionCount = newRepCount,
                feedbackMessage = if (isCompleted) "¡Pausa activa completada! 🎉" else feedback,
                isCompleted = isCompleted,
                pose = pose
            )
        }
    }

    fun sendSessionReport() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val request = SessionReportRequest(
                    exerciseName = currentState.exerciseName,
                    repetitions = currentState.repetitionCount,
                    durationSeconds = 60
                )
                val response = RetrofitClient.apiService.sendSessionReport(request)
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(feedbackMessage = "Reporte guardado en el servidor Node.js correctamente")
                    }
                } else {
                    _uiState.update {
                        it.copy(feedbackMessage = "Servidor respondió con error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(feedbackMessage = "No se pudo conectar con Node.js (Servidor offline o IP incorrecta)")
                }
            }
        }
    }
}
