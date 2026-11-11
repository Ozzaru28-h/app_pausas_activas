package com.example.pausasactivas.data.pose

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.pausasactivas.domain.model.DetectedPose
import com.example.pausasactivas.domain.model.PoseLandmarkPoint
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

class PoseAnalyzer(
    private val onPoseDetected: (DetectedPose?) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()

    private val poseDetector = PoseDetection.getClient(options)

    @Volatile
    private var isClosed = false

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || isClosed) {
            imageProxy.close()
            return
        }
        try {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            poseDetector.process(image)
                .addOnSuccessListener { pose ->
                    val detectedPose = mapMlKitPoseToDetectedPose(pose)
                    onPoseDetected(detectedPose)
                }
                .addOnFailureListener {
                    onPoseDetected(null)
                }
                .addOnCompleteListener {
                    // Siempre se libera el frame; si no, la cámara deja de entregar imágenes.
                    imageProxy.close()
                }
        } catch (e: Exception) {
            imageProxy.close()
            onPoseDetected(null)
        }
    }

    /** Libera el detector de ML Kit. Llamar cuando la pantalla de cámara se destruye. */
    fun close() {
        isClosed = true
        poseDetector.close()
    }

    private fun mapMlKitPoseToDetectedPose(pose: Pose): DetectedPose? {
        val landmarks = pose.allPoseLandmarks
        if (landmarks.isEmpty()) return null

        fun getPoint(type: Int): PoseLandmarkPoint? {
            val landmark = pose.getPoseLandmark(type) ?: return null
            val pos = landmark.position3D
            return PoseLandmarkPoint(
                x = pos.x,
                y = pos.y,
                z = pos.z,
                likelihood = landmark.inFrameLikelihood
            )
        }

        return DetectedPose(
            leftShoulder = getPoint(PoseLandmark.LEFT_SHOULDER),
            rightShoulder = getPoint(PoseLandmark.RIGHT_SHOULDER),
            leftElbow = getPoint(PoseLandmark.LEFT_ELBOW),
            rightElbow = getPoint(PoseLandmark.RIGHT_ELBOW),
            leftWrist = getPoint(PoseLandmark.LEFT_WRIST),
            rightWrist = getPoint(PoseLandmark.RIGHT_WRIST),
            leftHip = getPoint(PoseLandmark.LEFT_HIP),
            rightHip = getPoint(PoseLandmark.RIGHT_HIP),
            leftKnee = getPoint(PoseLandmark.LEFT_KNEE),
            rightKnee = getPoint(PoseLandmark.RIGHT_KNEE),
            leftAnkle = getPoint(PoseLandmark.LEFT_ANKLE),
            rightAnkle = getPoint(PoseLandmark.RIGHT_ANKLE),
            nose = getPoint(PoseLandmark.NOSE),
            leftEar = getPoint(PoseLandmark.LEFT_EAR),
            rightEar = getPoint(PoseLandmark.RIGHT_EAR)
        )
    }
}
