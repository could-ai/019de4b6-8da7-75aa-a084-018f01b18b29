package ai.could.pushunlock.ml

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions

class PoseDetectorHelper(
    private val onPushupStateChanged: (Boolean) -> Unit // true for UP, false for DOWN
) : ImageAnalysis.Analyzer {

    private val options = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
        .build()

    private val poseDetector = PoseDetection.getClient(options)
    
    private var isDown = false

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            poseDetector.process(image)
                .addOnSuccessListener { pose ->
                    processPose(pose)
                }
                .addOnFailureListener {
                    // handle error
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun processPose(pose: Pose) {
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)

        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        if (leftShoulder != null && leftElbow != null && leftWrist != null) {
            val angle = getAngle(leftShoulder, leftElbow, leftWrist)
            updatePushupState(angle)
        } else if (rightShoulder != null && rightElbow != null && rightWrist != null) {
            val angle = getAngle(rightShoulder, rightElbow, rightWrist)
            updatePushupState(angle)
        }
    }

    private fun updatePushupState(angle: Double) {
        if (angle < 90.0 && !isDown) {
            isDown = true
            onPushupStateChanged(false) // Down position
        } else if (angle > 160.0 && isDown) {
            isDown = false
            onPushupStateChanged(true) // Up position -> pushup complete
        }
    }

    private fun getAngle(
        firstPoint: PoseLandmark,
        midPoint: PoseLandmark,
        lastPoint: PoseLandmark
    ): Double {
        val p1x = firstPoint.position.x
        val p1y = firstPoint.position.y
        val p2x = midPoint.position.x
        val p2y = midPoint.position.y
        val p3x = lastPoint.position.x
        val p3y = lastPoint.position.y

        val result = Math.toDegrees(
            Math.atan2((p3y - p2y).toDouble(), (p3x - p2x).toDouble()) -
            Math.atan2((p1y - p2y).toDouble(), (p1x - p2x).toDouble())
        )
        val angle = Math.abs(result)
        return if (angle > 180.0) {
            360.0 - angle
        } else {
            angle
        }
    }
}
