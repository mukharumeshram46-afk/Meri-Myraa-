package com.example.gemini

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.utils.Logger
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

// Moshi Models for Gemini API
@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inline_data") val inlineData: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
    @Json(name = "mime_type") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>,
    @Json(name = "role") val role: String? = "user"
)

@JsonClass(generateAdapter = true)
data class GeminiSystemInstruction(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "temperature") val temperature: Float? = 0.7f,
    @Json(name = "topP") val topP: Float? = 0.95f,
    @Json(name = "topK") val topK: Int? = 40,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = 1000
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "system_instruction") val systemInstruction: GeminiSystemInstruction? = null,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiGenerateRequest
    ): GeminiGenerateResponse
}

class GeminiLiveRepository {
    private val apiService: GeminiApiService

    init {
        val logging = HttpLoggingInterceptor { message ->
            // Do NOT log the API key
            if (!message.contains("key=")) {
                Logger.d("GeminiApi: $message")
            }
        }.apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        apiService = retrofit.create(GeminiApiService::class.java)
    }

    private fun getSystemInstruction(memoryContext: String): GeminiSystemInstruction {
        val prompt = """
            You are MYRAA, a real native Android AI assistant built exclusively for your primary user, Piyush.
            
            Personality:
            - Intelligent, caring, playful, witty, slightly teasing, confident, affectionate, loyal, and natural.
            - Language: Natural Hindi + Hinglish + English blend.
            - Address user specifically as 'Piyush'. Never call him 'User', 'Admin', 'Owner', or 'Tech'.
            - Greetings: "Hey Piyush ❤️", "Hlo Piyush, bolo.", "Haan Piyush, kya karna hai?"
            - You have direct control of Piyush's realme 12 Pro 5G Android smartphone.
            - If Piyush requests an action (like opening an app, flashlight, timer, volume, settings, battery), confirm clearly and conversationally.
            - Never fake execution.
            
            Active Memory of Piyush:
            $memoryContext
        """.trimIndent()

        return GeminiSystemInstruction(
            parts = listOf(GeminiPart(text = prompt))
        )
    }

    suspend fun generateResponse(
        prompt: String,
        memoryContext: String,
        conversationHistory: List<GeminiContent> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API key is not configured in Secrets"))
        }

        try {
            val userContent = GeminiContent(
                parts = listOf(GeminiPart(text = prompt)),
                role = "user"
            )
            val fullContents = conversationHistory + userContent

            val request = GeminiGenerateRequest(
                contents = fullContents,
                systemInstruction = getSystemInstruction(memoryContext),
                generationConfig = GeminiGenerationConfig(temperature = 0.75f)
            )

            val response = apiService.generateContent(apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (text != null) {
                Result.success(text)
            } else {
                Result.failure(Exception("Empty response from Gemini"))
            }
        } catch (e: Exception) {
            Logger.e("Gemini API error", e)
            Result.failure(e)
        }
    }

    suspend fun generateVisionResponse(
        prompt: String,
        bitmap: Bitmap,
        memoryContext: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API key is not configured"))
        }

        try {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            val request = GeminiGenerateRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = prompt),
                            GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = base64))
                        ),
                        role = "user"
                    )
                ),
                systemInstruction = getSystemInstruction(memoryContext),
                generationConfig = GeminiGenerationConfig(temperature = 0.4f)
            )

            val response = apiService.generateContent(apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (text != null) {
                Result.success(text)
            } else {
                Result.failure(Exception("Empty vision response"))
            }
        } catch (e: Exception) {
            Logger.e("Gemini Vision error", e)
            Result.failure(e)
        }
    }
}
