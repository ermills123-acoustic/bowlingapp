package com.bowlvision.app

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

data class BowlingFeedback(
    val score: Int,
    val feedbackMessage: String,
    val isError: Boolean
)

class BowlingAnalyzer {
    // A simple state machine for bowling form
    // States: START, APPROACH, SLIDE, RELEASE, FOLLOW_THROUGH
    private var currentState = "START"
    private var lastSlideX = 0f
    
    fun analyzeFrame(result: PoseLandmarkerResult): BowlingFeedback? {
        if (result.landmarks().isEmpty()) return null
        
        val landmarks = result.landmarks()[0]
        val rightWrist = landmarks[16]
        val rightElbow = landmarks[14]
        val rightShoulder = landmarks[12]
        val leftAnkle = landmarks[27] // Assuming right-handed bowler, slide foot is left

        // Basic heuristic analysis (simplified for MVP)
        // 1. Arm Swing Plane Check (Shoulder, Elbow, Wrist should be vertically aligned)
        val horizontalDeviation = Math.abs(rightWrist.x() - rightShoulder.x())
        if (horizontalDeviation > 0.15f) {
            return BowlingFeedback(
                score = 80,
                feedbackMessage = "Keep your arm swing closer to your body.",
                isError = true
            )
        }

        // 2. Release timing (Wrist drops below waist)
        val rightHip = landmarks[24]
        if (rightWrist.y() > rightHip.y() && currentState != "RELEASE") {
            currentState = "RELEASE"
            // Check slide foot position relative to release
            if (leftAnkle.y() < rightWrist.y() - 0.1f) {
                return BowlingFeedback(
                    score = 75,
                    feedbackMessage = "Late slide detected! Start your slide earlier.",
                    isError = true
                )
            }
            return BowlingFeedback(
                score = 95,
                feedbackMessage = "Great release timing!",
                isError = false
            )
        }
        
        return BowlingFeedback(
            score = 100,
            feedbackMessage = "Good form, continue approach.",
            isError = false
        )
    }
}
