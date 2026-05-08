package com.astral.ocr.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>
)

@Serializable
data class GeminiContent(
    val role: String = "user",
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String? = null,
    @SerialName("inline_data") val inlineData: InlineData? = null
)

@Serializable
data class InlineData(
    @SerialName("mime_type") val mimeType: String,
    val data: String
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null,
    @SerialName("promptFeedback") val promptFeedback: PromptFeedback? = null
)

@Serializable
data class Candidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null
)

@Serializable
data class PromptFeedback(
    val safetyRatings: List<SafetyRating>? = null
)

@Serializable
data class SafetyRating(
    val category: String,
    val probability: String
)

@Serializable
data class GeminiErrorResponse(
    val error: GeminiError? = null
)

@Serializable
data class GeminiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

@Serializable
data class OpenAiRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    @SerialName("max_tokens") val maxTokens: Int? = null,
    val temperature: Float? = null
)

@Serializable
data class OpenAiMessage(
    val role: String,
    val content: List<OpenAiContentPart>
)

@Serializable
data class OpenAiContentPart(
    val type: String,
    val text: String? = null,
    @SerialName("image_url") val imageUrl: OpenAiImageUrl? = null
)

@Serializable
data class OpenAiImageUrl(
    val url: String
)

@Serializable
data class OpenAiResponse(
    val choices: List<OpenAiChoice>? = null,
    val error: OpenAiError? = null
)

@Serializable
data class OpenAiChoice(
    val message: OpenAiMessageResponse? = null,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class OpenAiMessageResponse(
    val role: String? = null,
    val content: String? = null
)

@Serializable
data class OpenAiError(
    val message: String? = null,
    val type: String? = null
)
