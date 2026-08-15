package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class ChatCompletionRequest(
  val model: String,
  val messages: List<ChatMessage>,
  val temperature: Double = 0.2,
  @Json(name = "max_tokens") val maxTokens: Int = 4096,
  val stream: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ChatMessage(
  val role: String, // "system", "user", "assistant"
  val content: String
)

@JsonClass(generateAdapter = true)
data class ChatCompletionResponse(
  val id: String?,
  val choices: List<ChatChoice>?,
  val usage: UsageInfo?
)

@JsonClass(generateAdapter = true)
data class ChatChoice(
  val index: Int?,
  val message: ChatMessage?,
  @Json(name = "finish_reason") val finishReason: String?
)

@JsonClass(generateAdapter = true)
data class UsageInfo(
  @Json(name = "prompt_tokens") val promptTokens: Int?,
  @Json(name = "completion_tokens") val completionTokens: Int?,
  @Json(name = "total_tokens") val totalTokens: Int?
)

// Gemini Direct API Models
@JsonClass(generateAdapter = true)
data class GeminiGenerateRequest(
  val contents: List<GeminiContent>,
  val systemInstruction: GeminiContent? = null,
  val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
  val role: String? = null,
  val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
  val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
  val temperature: Double = 0.2,
  val maxOutputTokens: Int = 4096
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateResponse(
  val candidates: List<GeminiCandidate>?,
  val usageMetadata: GeminiUsageMetadata?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
  val content: GeminiContent?,
  val finishReason: String?
)

@JsonClass(generateAdapter = true)
data class GeminiUsageMetadata(
  val promptTokenCount: Int?,
  val candidatesTokenCount: Int?,
  val totalTokenCount: Int?
)

interface OpenCodeApiClient {
  @POST
  suspend fun chatCompletions(
    @Url url: String,
    @Header("Authorization") authorization: String?,
    @Header("HTTP-Referer") referer: String = "https://opencode.ai",
    @Header("X-Title") title: String = "OpenCode CLI Android",
    @Body request: ChatCompletionRequest
  ): Response<ChatCompletionResponse>

  @POST
  suspend fun generateGemini(
    @Url url: String,
    @Body request: GeminiGenerateRequest
  ): Response<GeminiGenerateResponse>
}

object ApiClientProvider {
  private val okHttpClient: OkHttpClient by lazy {
    val logging = HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BASIC
    }
    OkHttpClient.Builder()
      .addInterceptor(logging)
      .connectTimeout(60, TimeUnit.SECONDS)
      .readTimeout(60, TimeUnit.SECONDS)
      .writeTimeout(60, TimeUnit.SECONDS)
      .build()
  }

  val client: OpenCodeApiClient by lazy {
    Retrofit.Builder()
      .baseUrl("https://api.opencode.ai/")
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create())
      .build()
      .create(OpenCodeApiClient::class.java)
  }
}
