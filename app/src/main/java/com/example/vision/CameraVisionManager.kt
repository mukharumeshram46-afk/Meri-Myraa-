package com.example.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.gemini.GeminiLiveRepository
import com.example.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CameraVisionManager(
    private val context: Context,
    private val geminiRepository: GeminiLiveRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _visionState = MutableStateFlow<VisionState>(VisionState.Idle)
    val visionState: StateFlow<VisionState> = _visionState.asStateFlow()

    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null

    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider
    ) {
        _visionState.value = VisionState.Initializing
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(surfaceProvider)
                }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                _visionState.value = VisionState.Previewing
                Logger.i("CameraX initialized and previewing")
            } catch (e: Exception) {
                Logger.e("Camera initialization failed", e)
                _visionState.value = VisionState.Error("Camera could not be started: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun captureAndAnalyze(prompt: String, memoryContext: String) {
        val capture = imageCapture ?: run {
            _visionState.value = VisionState.Error("Camera is not ready")
            return
        }

        _visionState.value = VisionState.Capturing("Taking snapshot...")

        capture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = imageProxyToBitmap(image)
                    image.close()

                    if (bitmap == null) {
                        _visionState.value = VisionState.Error("Failed to process image frame")
                        return
                    }

                    _visionState.value = VisionState.Analyzing("Piyush, Gemini is looking at this image...")
                    scope.launch {
                        val result = geminiRepository.generateVisionResponse(
                            prompt = prompt.ifBlank { "What do you see in this image? Explain clearly and helpfully to Piyush in friendly Hinglish." },
                            bitmap = bitmap,
                            memoryContext = memoryContext
                        )
                        result.onSuccess { text ->
                            _visionState.value = VisionState.Success(text, bitmap)
                        }.onFailure { err ->
                            _visionState.value = VisionState.Error("Vision analysis failed: ${err.message}")
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Logger.e("Image capture failed", exception)
                    _visionState.value = VisionState.Error("Capture failed: ${exception.message}")
                }
            }
        )
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        val plane = image.planes[0]
        val buffer = plane.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null

        val rotation = image.imageInfo.rotationDegrees
        return if (rotation != 0) {
            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }

    fun stopCamera() {
        cameraProvider?.unbindAll()
        cameraProvider = null
        imageCapture = null
        _visionState.value = VisionState.Idle
    }
}
