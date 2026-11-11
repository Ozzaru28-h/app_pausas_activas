package com.example.pausasactivas.domain.rules

import com.example.pausasactivas.domain.model.DetectedPose
import com.example.pausasactivas.domain.model.ExerciseCatalog
import com.example.pausasactivas.domain.model.PoseLandmarkPoint
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min

/** Estado de un cuadro (frame) evaluado. */
enum class PostureStatus {
    /** No se detectó ninguna persona. */
    NO_PERSON,

    /** Se detectó a la persona, pero faltan puntos necesarios para este ejercicio. */
    NOT_VISIBLE,

    /** Persona visible, pero aún no está en la posición del ejercicio (reposo / transición). */
    NEUTRAL,

    /** Posición del ejercicio alcanzada y bien ejecutada. */
    CORRECT,

    /** Posición del ejercicio con un error de postura. */
    ADJUST
}

/** Error articular detectado: articulación, ángulo medido y ángulo esperado (límite del rango). */
data class PostureError(
    val joint: String,
    val detectedAngle: Float,
    val expectedAngle: Float
)

data class PostureFeedback(
    val status: PostureStatus,
    val message: String,
    val error: PostureError? = null
)

/**
 * Reglas sencillas por ángulos para la DEMO académica.
 * No es un diagnóstico médico ni una evaluación clínica.
 *
 * Trabaja con coordenadas 2D de la imagen (x hacia la derecha, y hacia abajo).
 * Las reglas no dependen de qué lado se inclina la persona, así que no les afecta
 * el efecto espejo de la cámara frontal.
 */
object PostureRules {

    // Umbral de confianza de ML Kit (inFrameLikelihood) para usar un punto.
    private const val MIN_LIKELIHOOD = 0.5f

    // Cuello
    const val NECK_MIN_HEAD_TILT = 12f
    const val NECK_MAX_HEAD_TILT = 40f
    const val NECK_MAX_SHOULDER_TILT = 12f

    // Brazos
    const val ARMS_MIN_ELBOW_ANGLE = 150f

    // Tronco
    const val TRUNK_MIN_TILT = 12f
    const val TRUNK_MAX_TILT = 40f
    const val TRUNK_MAX_HIP_TILT = 10f

    fun evaluate(exerciseCode: String, pose: DetectedPose?): PostureFeedback {
        if (pose == null) {
            return PostureFeedback(PostureStatus.NO_PERSON, "Colócate frente a la cámara")
        }
        return when (exerciseCode) {
            ExerciseCatalog.CUELLO_LATERAL -> evaluateNeck(pose)
            ExerciseCatalog.ELEVACION_BRAZOS -> evaluateArms(pose)
            ExerciseCatalog.FLEXION_LATERAL_TRONCO -> evaluateTrunk(pose)
            else -> PostureFeedback(PostureStatus.NEUTRAL, "Ejercicio no reconocido")
        }
    }

    // ---------------------------------------------------------------- Cuello

    private fun evaluateNeck(p: DetectedPose): PostureFeedback {
        val leftEar = p.leftEar.visible()
        val rightEar = p.rightEar.visible()
        val leftShoulder = p.leftShoulder.visible()
        val rightShoulder = p.rightShoulder.visible()

        if (leftEar == null || rightEar == null || leftShoulder == null || rightShoulder == null) {
            return PostureFeedback(
                PostureStatus.NOT_VISIBLE,
                "No se ven bien tu cabeza y tus hombros. Ponte de frente y un poco más lejos"
            )
        }

        val shoulderTilt = tiltFromHorizontal(leftShoulder, rightShoulder)
        val headTilt = tiltFromHorizontal(leftEar, rightEar)

        return when {
            shoulderTilt > NECK_MAX_SHOULDER_TILT -> PostureFeedback(
                PostureStatus.ADJUST,
                "Mantén los hombros nivelados, no los subas",
                PostureError("Hombros", shoulderTilt, NECK_MAX_SHOULDER_TILT)
            )

            headTilt > NECK_MAX_HEAD_TILT -> PostureFeedback(
                PostureStatus.ADJUST,
                "Inclina un poco menos la cabeza, sin forzar",
                PostureError("Cuello (inclinación lateral)", headTilt, NECK_MAX_HEAD_TILT)
            )

            headTilt >= NECK_MIN_HEAD_TILT -> PostureFeedback(
                PostureStatus.CORRECT,
                "Postura correcta. Vuelve al centro con calma y cambia de lado"
            )

            else -> PostureFeedback(
                PostureStatus.NEUTRAL,
                "Inclina lentamente la cabeza hacia un hombro"
            )
        }
    }

    // ---------------------------------------------------------------- Brazos

    private fun evaluateArms(p: DetectedPose): PostureFeedback {
        val leftShoulder = p.leftShoulder.visible()
        val rightShoulder = p.rightShoulder.visible()
        val leftElbow = p.leftElbow.visible()
        val rightElbow = p.rightElbow.visible()
        val leftWrist = p.leftWrist.visible()
        val rightWrist = p.rightWrist.visible()

        if (leftShoulder == null || rightShoulder == null || leftElbow == null ||
            rightElbow == null || leftWrist == null || rightWrist == null
        ) {
            return PostureFeedback(
                PostureStatus.NOT_VISIBLE,
                "No se ven tus brazos completos. Aléjate un poco de la cámara"
            )
        }

        // En la imagen, "más arriba" significa una coordenada y menor.
        val leftUp = leftWrist.y < leftShoulder.y
        val rightUp = rightWrist.y < rightShoulder.y

        if (!leftUp && !rightUp) {
            return PostureFeedback(PostureStatus.NEUTRAL, "Sube ambos brazos por encima de los hombros")
        }
        if (leftUp != rightUp) {
            return PostureFeedback(PostureStatus.NEUTRAL, "Sube también el otro brazo")
        }

        val leftAngle = angleAt(leftShoulder, leftElbow, leftWrist)
        val rightAngle = angleAt(rightShoulder, rightElbow, rightWrist)
        val worstAngle = min(leftAngle, rightAngle)

        return if (worstAngle < ARMS_MIN_ELBOW_ANGLE) {
            PostureFeedback(
                PostureStatus.ADJUST,
                "Ajusta la posición de los brazos: estira los codos",
                PostureError(
                    joint = if (leftAngle <= rightAngle) "Codo izquierdo" else "Codo derecho",
                    detectedAngle = worstAngle,
                    expectedAngle = ARMS_MIN_ELBOW_ANGLE
                )
            )
        } else {
            PostureFeedback(PostureStatus.CORRECT, "Postura correcta. Baja despacio y repite")
        }
    }

    // ---------------------------------------------------------------- Tronco

    private fun evaluateTrunk(p: DetectedPose): PostureFeedback {
        val leftShoulder = p.leftShoulder.visible()
        val rightShoulder = p.rightShoulder.visible()
        val leftHip = p.leftHip.visible()
        val rightHip = p.rightHip.visible()

        if (leftShoulder == null || rightShoulder == null || leftHip == null || rightHip == null) {
            return PostureFeedback(
                PostureStatus.NOT_VISIBLE,
                "No se ve tu torso completo. Aléjate para que se vean hombros y caderas"
            )
        }

        val shoulderCenter = midpoint(leftShoulder, rightShoulder)
        val hipCenter = midpoint(leftHip, rightHip)
        val trunkTilt = tiltFromVertical(shoulderCenter, hipCenter)
        val hipTilt = tiltFromHorizontal(leftHip, rightHip)

        return when {
            hipTilt > TRUNK_MAX_HIP_TILT -> PostureFeedback(
                PostureStatus.ADJUST,
                "Mantén la cadera estable y nivelada",
                PostureError("Cadera", hipTilt, TRUNK_MAX_HIP_TILT)
            )

            trunkTilt > TRUNK_MAX_TILT -> PostureFeedback(
                PostureStatus.ADJUST,
                "Inclínate un poco menos, sin forzar",
                PostureError("Tronco (inclinación lateral)", trunkTilt, TRUNK_MAX_TILT)
            )

            trunkTilt >= TRUNK_MIN_TILT -> PostureFeedback(
                PostureStatus.CORRECT,
                "Postura correcta. Vuelve al centro y cambia de lado"
            )

            else -> PostureFeedback(PostureStatus.NEUTRAL, "Inclina el tronco hacia un lado")
        }
    }

    // ------------------------------------------------------------ Geometría

    private fun PoseLandmarkPoint?.visible(): PoseLandmarkPoint? =
        if (this != null && likelihood >= MIN_LIKELIHOOD) this else null

    private fun midpoint(a: PoseLandmarkPoint, b: PoseLandmarkPoint) = PoseLandmarkPoint(
        x = (a.x + b.x) / 2f,
        y = (a.y + b.y) / 2f,
        z = (a.z + b.z) / 2f,
        likelihood = min(a.likelihood, b.likelihood)
    )

    /** Ángulo (0°–180°) en el vértice [vertex] formado por los puntos [a] y [c]. */
    fun angleAt(a: PoseLandmarkPoint, vertex: PoseLandmarkPoint, c: PoseLandmarkPoint): Float {
        val v1x = a.x - vertex.x
        val v1y = a.y - vertex.y
        val v2x = c.x - vertex.x
        val v2y = c.y - vertex.y
        val magnitude = hypot(v1x, v1y) * hypot(v2x, v2y)
        if (magnitude == 0f) return 0f
        val cos = ((v1x * v2x + v1y * v2y) / magnitude).coerceIn(-1f, 1f)
        return Math.toDegrees(acos(cos).toDouble()).toFloat()
    }

    /** Inclinación (0°–90°) de la recta que une [a] y [b] respecto a la horizontal. */
    fun tiltFromHorizontal(a: PoseLandmarkPoint, b: PoseLandmarkPoint): Float =
        Math.toDegrees(atan2(abs(b.y - a.y), abs(b.x - a.x)).toDouble()).toFloat()

    /** Inclinación (0°–90°) de la recta que une [top] y [bottom] respecto a la vertical. */
    fun tiltFromVertical(top: PoseLandmarkPoint, bottom: PoseLandmarkPoint): Float =
        Math.toDegrees(atan2(abs(bottom.x - top.x), abs(bottom.y - top.y)).toDouble()).toFloat()
}
