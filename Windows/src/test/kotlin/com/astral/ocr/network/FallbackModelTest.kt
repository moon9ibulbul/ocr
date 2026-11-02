package com.astral.ocr.network

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackModelTest {

    @Test
    fun testSumopodFallbackWhenFirstModelReturnsEmptyText() = runBlocking {
        val requestedModels = mutableListOf<String>()

        val interceptor = Interceptor { chain ->
            val request = chain.request()
            val requestBodyString = request.body?.let {
                val buffer = okio.Buffer()
                it.writeTo(buffer)
                buffer.readUtf8()
            }.orEmpty()

            if (requestBodyString.contains("primary-model")) {
                requestedModels.add("primary-model")
                val emptyJson = """
                    {
                        "choices": [
                            {
                                "message": {
                                    "role": "assistant",
                                    "content": ""
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
                    .body(emptyJson.toResponseBody("application/json".toMediaType()))
                    .build()
            } else if (requestBodyString.contains("gpt-5.6-luna")) {
                requestedModels.add("gpt-5.6-luna")
                val successJson = """
                    {
                        "choices": [
                            {
                                "message": {
                                    "role": "assistant",
                                    "content": "[BLOCK 1] () Halo dari fallback!"
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
            } else {
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(400)
                    .message("Bad Request")
                    .body("{}".toResponseBody("application/json".toMediaType()))
                    .build()
            }
        }

        val testClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val service = GeminiOcrService(client = testClient)

        val result = service.requestWithRetry(
            apiKey = "test-token",
            model = "primary-model",
            mimeType = "image/jpeg",
            base64s = listOf("dGVzdA=="),
            prompt = "OCR prompt",
            apiProvider = "sumopod",
            fallbackModel = "gpt-5.6-luna"
        )

        assertTrue(result.isSuccess)
        assertEquals("[BLOCK 1] () Halo dari fallback!", result.getOrNull())
        assertEquals(listOf("primary-model", "gpt-5.6-luna"), requestedModels)
    }

    @Test
    fun testSumopodFailsWhenFallbackAlsoReturnsEmptyText() = runBlocking {
        val requestedModels = mutableListOf<String>()

        val interceptor = Interceptor { chain ->
            val request = chain.request()
            val requestBodyString = request.body?.let {
                val buffer = okio.Buffer()
                it.writeTo(buffer)
                buffer.readUtf8()
            }.orEmpty()

            if (requestBodyString.contains("primary-model")) {
                requestedModels.add("primary-model")
            } else if (requestBodyString.contains("gpt-5.6-luna")) {
                requestedModels.add("gpt-5.6-luna")
            }

            val emptyJson = """
                {
                    "choices": [
                        {
                            "message": {
                                "role": "assistant",
                                "content": "   "
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
                .body(emptyJson.toResponseBody("application/json".toMediaType()))
                .build()
        }

        val testClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val service = GeminiOcrService(client = testClient)

        val result = service.requestWithRetry(
            apiKey = "test-token",
            model = "primary-model",
            mimeType = "image/jpeg",
            base64s = listOf("dGVzdA=="),
            prompt = "OCR prompt",
            apiProvider = "sumopod",
            fallbackModel = "gpt-5.6-luna"
        )

        assertTrue(result.isFailure)
        assertEquals("API tidak mengembalikan teks.", result.exceptionOrNull()?.message)
        assertEquals(listOf("primary-model", "gpt-5.6-luna"), requestedModels)
    }
}
