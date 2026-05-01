package com.couldai.pushunlock.domain

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlin.math.abs
import kotlin.math.atan2

class PushupAnalyzer(
    private val onPushupDetected: (Int) -> Unit,
    private val onBodyFound: (Boolean) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()
        
    private val poseDetector = PoseDetection.getClient(options)
    
    private var pushupCount = 0
    private var isDown = false

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            poseDetector.process(image)
                .addOnSuccessListener { pose ->
                    analyzePose(pose)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun analyzePose(pose: Pose) {
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        val hasLeft = leftShoulder != null && leftElbow != null && leftWrist != null &&
                leftShoulder.inFrameLikelihood > 0.5f && leftElbow.inFrameLikelihood > 0.5f && leftWrist.inFrameLikelihood > 0.5f
                
        val hasRight = rightShoulder != null && rightElbow != null && rightWrist != null &&
                rightShoulder.inFrameLikelihood > 0.5f && rightElbow.inFrameLikelihood > 0.5f && rightWrist.inFrameLikelihood > 0.5f

        onBodyFound(hasLeft || hasRight)

        if (hasLeft) {
            val leftAngle = getAngle(leftShoulder!!, leftElbow!!, leftWrist!!)
            checkPushup(leftAngle)
        } else if (hasRight) {
            val rightAngle = getAngle(rightShoulder!!, rightElbow!!, rightWrist!!)
            checkPushup(rightAngle)
        }
    }

    private fun checkPushup(angle: Double) {
        if (angle < 90) {
            isDown = true
        } else if (angle > 160 && isDown) {
            isDown = false
            pushupCount++
            onPushupDetected(pushupCount)
        }
    }

    private fun getAngle(first: PoseLandmark, middle: PoseLandmark, last: PoseLandmark): Double {
        var angle = Math.toDegrees(
            atan2(last.position.y - middle.position.y, last.position.x - middle.position.x) -
            atan2(first.position.y - middle.position.y, first.position.x - middle.position.x).toDouble()
        )
        angle = abs(angle)
        if (angle > 180) {
            angle = 360.0 - angle
        }
        return angle
    }
}
