package com.astral.ocr.network

import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.Method

class GeminiNormalizationTest {

    @Test
    fun testNormalizeOutputWithDefaultLegend() {
        val ocrService = GeminiOcrService()
        val rawOutput = """
            [BLOCK 1] () Halo!
            [BLOCK 2] [] Narasi kotak.
            [BLOCK 3] // *sfx crash*
            [BLOCK 4] '' Teks luar bubble.
        """.trimIndent()

        // normalizeOutput is private, let's use reflection to test it.
        val normalizeMethod: Method = GeminiOcrService::class.java.getDeclaredMethod(
            "normalizeOutput",
            String::class.java,
            String::class.java
        ).apply { isAccessible = true }

        val result = normalizeMethod.invoke(ocrService, rawOutput, "//") as String
        val expected = """
            () : Halo!
            [] : Narasi kotak.
            // : *sfx crash*
            '' : Teks luar bubble.
        """.trimIndent()

        assertEquals(expected, result)
    }

    @Test
    fun testNormalizeOutputWithCustomLegend() {
        val ocrService = GeminiOcrService()
        val rawOutput = """
            [BLOCK 1] () Halo!
            [BLOCK 2] [] Narasi kotak.
            [BLOCK 3] ** *sfx crash*
            [BLOCK 4] '' Teks luar bubble.
        """.trimIndent()

        val normalizeMethod: Method = GeminiOcrService::class.java.getDeclaredMethod(
            "normalizeOutput",
            String::class.java,
            String::class.java
        ).apply { isAccessible = true }

        val result = normalizeMethod.invoke(ocrService, rawOutput, "**") as String
        val expected = """
            () : Halo!
            [] : Narasi kotak.
            ** : *sfx crash*
            '' : Teks luar bubble.
        """.trimIndent()

        assertEquals(expected, result)
    }
}
