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
 * (nose, leftEar y rightEar se añadieron para el ejercicio de cuello; van al final
 * con valor por defecto para no romper el código existente).
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
    val rightAnkle: PoseLandmarkPoint? = null,
    val nose: PoseLandmarkPoint? = null,
    val leftEar: PoseLandmarkPoint? = null,
    val rightEar: PoseLandmarkPoint? = null
)
