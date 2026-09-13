package com.example.vision

import android.graphics.Bitmap
import com.example.gemini.GeminiLiveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScreenContextProvider(
    private val geminiRepository: GeminiLiveRepository
) {
    private val _isScreenCaptureActive = MutableStateFlow(false)
    val isScreenCaptureActive: StateFlow<Boolean> = _isScreenCaptureActive.asStateFlow()

    private val _screenAnalysisResult = MutableStateFlow<String?>(null)
    val screenAnalysisResult: StateFlow<String?> = _screenAnalysisResult.asStateFlow()

    fun setScreenSharingState(active: Boolean) {
        _isScreenCaptureActive.value = active
    }

    suspend fun analyzeScreenshot(
        bitmap: Bitmap,
        userPrompt: String,
        memoryContext: String
    ): Result<String> {
        _screenAnalysisResult.value = "Piyush, screen analysis in progress..."
        val prompt = userPrompt.ifBlank {
            "Analyze this screen capture for Piyush. Describe what is displayed on the smartphone screen, summarize relevant information or help him with actions."
        }
        val result = geminiRepository.generateVisionResponse(
            prompt = prompt,
            bitmap = bitmap,
            memoryContext = memoryContext
        )
        result.onSuccess {
            _screenAnalysisResult.value = it
        }.onFailure {
            _screenAnalysisResult.value = "Screen analysis error: ${it.message}"
        }
        return result
    }
}
