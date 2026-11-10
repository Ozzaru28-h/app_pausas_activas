package com.example.pausasactivas.domain.model

/**
 * Coordenadas 3D simplificadas de un punto clave del cuerpo.
 */
data class PoseLandmarkPoint(
    val x: Float,
    val y: Float,
    val z: Float,
    val likelihood: Float
)

/**
 * Puntos clave principales de la postura corporal detectada.
 */
data class DetectedPose(
    val leftShoulder: PoseLandmarkPoint? = null,
    val rightShoulder: PoseLandmarkPoint? = null,
    val leftElbow: PoseLandmarkPoint? = null,
    val rightElbow: PoseLandmarkPoint? = null,
    val leftWrist: PoseLandmarkPoint? = null,
    val rightWrist: PoseLandmarkPoint? = null,
    val leftHip: PoseLandmarkPoint? = null,
    val rightHip: PoseLandmarkPoint? = null,
    val leftKnee: PoseLandmarkPoint? = null,
    val rightKnee: PoseLandmarkPoint? = null,
    val leftAnkle: PoseLandmarkPoint? = null,
    val rightAnkle: PoseLandmarkPoint? = null
)

/**
 * Estado general de la sesión de pausas activas.
 */
data class ActivePauseState(
    val isPersonDetected: Boolean = false,
    val exerciseName: String = "Estiramiento de Brazos",
    val repetitionCount: Int = 0,
    val targetRepetitions: Int = 10,
    val feedbackMessage: String = "Colócate frente a la cámara",
    val isCompleted: Boolean = false,
    val pose: DetectedPose? = null
)
