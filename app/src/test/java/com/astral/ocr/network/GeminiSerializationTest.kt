package com.astral.ocr.network

import kotlinx.serialization.json.Json
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GeminiSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testGenerateContentResponseWithMissingParts() {
        val responseBody = """
            {
              "candidates": [
                {
                  "content": {
                    "role": "model"
                  },
                  "finishReason": "OTHER"
                }
              ]
            }
        """.trimIndent()

        val parsed = json.decodeFromString(GenerateContentResponse.serializer(), responseBody)
        assertNotNull(parsed)
        assertNotNull(parsed.candidates?.get(0)?.content)
    }

    @Test
    fun testGenerateContentResponseWithMissingContent() {
        val responseBody = """
            {
              "candidates": [
                {
                  "finishReason": "SAFETY"
                }
              ]
            }
        """.trimIndent()

        val parsed = json.decodeFromString(GenerateContentResponse.serializer(), responseBody)
        assertNotNull(parsed)
        assertNull(parsed.candidates?.get(0)?.content)
    }
}
