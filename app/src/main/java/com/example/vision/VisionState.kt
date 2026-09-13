package com.example.vision

import android.graphics.Bitmap

sealed class VisionState {
    object Idle : VisionState()
    object Initializing : VisionState()
    object Previewing : VisionState()
    data class Capturing(val message: String = "Capturing image...") : VisionState()
    data class Analyzing(val message: String = "Analyzing with Gemini Vision...") : VisionState()
    data class Success(val result: String, val snapshot: Bitmap? = null) : VisionState()
    data class Error(val message: String) : VisionState()
}

data class DetectedObject(
    val label: String,
    val confidence: Float,
    val description: String
)
