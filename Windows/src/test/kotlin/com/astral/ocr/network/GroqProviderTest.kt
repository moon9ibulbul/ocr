package com.astral.ocr.network

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GroqProviderTest {

    @Test
    fun testGroqRequestFormattingAndResponseParsing() = runBlocking {
        var capturedUrl = ""
        var capturedAuthHeader = ""
        var capturedBody = ""

        val interceptor = Interceptor { chain ->
            val request = chain.request()
            capturedUrl = request.url.toString()
            capturedAuthHeader = request.header("Authorization").orEmpty()
            capturedBody = request.body?.let {
                val buffer = okio.Buffer()
                it.writeTo(buffer)
                buffer.readUtf8()
            }.orEmpty()

            val successJson = """
                {
                    "choices": [
                        {
                            "message": {
                                "role": "assistant",
                                "content": "[BLOCK 1] () Halo dari Groq Qwen!"
                            }
                        }
                    ]
                }
            """.trimIndent()
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(successJson.toResponseBody("application/json".toMediaType()))
                .build()
        }

        val testClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val service = GeminiOcrService(client = testClient)

        val result = service.requestWithRetry(
            apiKey = "gsk_test_groq_key",
            model = "qwen/qwen3.8-27b",
            mimeType = "image/jpeg",
            base64s = listOf("dGVzdGdyb3E="),
            prompt = "OCR Manga Prompt",
            apiProvider = "groq"
        )

        assertTrue(result.isSuccess)
        assertEquals("[BLOCK 1] () Halo dari Groq Qwen!", result.getOrNull())
        assertEquals("https://api.groq.com/openai/v1/chat/completions", capturedUrl)
        assertEquals("Bearer gsk_test_groq_key", capturedAuthHeader)
        assertTrue(capturedBody.contains("\"model\":\"qwen/qwen3.8-27b\""))
        assertTrue(capturedBody.contains("data:image/jpeg;base64,dGVzdGdyb3E="))
        assertTrue(capturedBody.contains("OCR Manga Prompt"))
    }

    @Test
    fun testGroqErrorHandling() = runBlocking {
        val interceptor = Interceptor { chain ->
            val request = chain.request()
            val errorJson = """
                {
                    "error": {
                        "message": "Invalid API Key provided",
                        "type": "invalid_request_error"
                    }
                }
            """.trimIndent()
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(401)
                .message("Unauthorized")
                .body(errorJson.toResponseBody("application/json".toMediaType()))
                .build()
        }

        val testClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val service = GeminiOcrService(client = testClient)

        val result = service.requestWithRetry(
            apiKey = "bad_key",
            model = "qwen/qwen3.8-27b",
            mimeType = "image/jpeg",
            base64s = listOf("dGVzdA=="),
            prompt = "OCR prompt",
            apiProvider = "groq"
        )

        assertTrue(result.isFailure)
        assertEquals("Invalid API Key provided", result.exceptionOrNull()?.message)
    }
}
