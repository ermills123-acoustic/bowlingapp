package com.bowlvision.app

import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

data class ShotResult(
    val score: Int,
    val feedbackBullets: List<String>
)

class BowlingAnalyzer {
    private var currentState = "START"
    private val accumulatedFeedback = mutableSetOf<String>()
    
    fun reset() {
        currentState = "START"
        accumulatedFeedback.clear()
    }
    
    fun analyzeFrame(result: PoseLandmarkerResult): ShotResult? {
        if (result.landmarks().isEmpty()) return null
        
        val landmarks = result.landmarks()[0]
        val rightWrist = landmarks[16]
        val rightElbow = landmarks[14]
        val rightShoulder = landmarks[12]
        val leftAnkle = landmarks[27] 
        val rightHip = landmarks[24]

        // 1. Arm Swing Plane Check
        val horizontalDeviation = Math.abs(rightWrist.x() - rightShoulder.x())
        if (horizontalDeviation > 0.15f) {
            accumulatedFeedback.add("Arm swing deviated horizontally. Keep it closer to your body.")
        }

        // 2. Release timing
        if (rightWrist.y() > rightHip.y() && currentState != "RELEASE") {
            currentState = "RELEASE"
            if (leftAnkle.y() < rightWrist.y() - 0.1f) {
                accumulatedFeedback.add("Late slide detected. Start your slide earlier.")
            } else {
                accumulatedFeedback.add("Great release timing!")
            }
        }
        
        // 3. Follow through & Completion
        if (currentState == "RELEASE" && rightWrist.y() < rightShoulder.y()) {
            currentState = "FOLLOW_THROUGH"
            accumulatedFeedback.add("Good follow through height.")
            
            // Calculate final score
            var score = 100
            if (accumulatedFeedback.any { it.contains("deviated") }) score -= 15
            if (accumulatedFeedback.any { it.contains("Late slide") }) score -= 10
            
            return ShotResult(score, accumulatedFeedback.toList())
        }
        
        return null // Shot not finished yet
    }
}
