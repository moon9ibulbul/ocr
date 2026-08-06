package com.astral.ocr.network

import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.Method

class GeminiNormalizationTest {

    @Test
    fun testNormalizeOutputWithDefaultLegends() {
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
            String::class.java,
            String::class.java,
            String::class.java,
            String::class.java,
            Boolean::class.javaPrimitiveType,
            Boolean::class.javaPrimitiveType,
            Boolean::class.javaPrimitiveType,
            Boolean::class.javaPrimitiveType
        ).apply { isAccessible = true }

        val result = normalizeMethod.invoke(ocrService, rawOutput, "//", "()", "[]", "''", true, true, true, true) as String
        val expected = """
            () : Halo!
            [] : Narasi kotak.
            // : *sfx crash*
            '' : Teks luar bubble.
        """.trimIndent()

        assertEquals(expected, result)
    }

    @Test
    fun testNormalizeOutputWithCustomLegends() {
        val ocrService = GeminiOcrService()
        val rawOutput = """
            [BLOCK 1] (B_ROUND) Halo!
            [BLOCK 2] [B_SQUARE] Narasi kotak.
            [BLOCK 3] SFX_EFFECT *sfx crash*
            [BLOCK 4] OUTSIDE_TEXT Teks luar bubble.
        """.trimIndent()

        val normalizeMethod: Method = GeminiOcrService::class.java.getDeclaredMethod(
            "normalizeOutput",
            String::class.java,
            String::class.java,
            String::class.java,
            String::class.java,
            String::class.java,
            Boolean::class.javaPrimitiveType,
            Boolean::class.javaPrimitiveType,
            Boolean::class.javaPrimitiveType,
            Boolean::class.javaPrimitiveType
        ).apply { isAccessible = true }

        val result = normalizeMethod.invoke(
            ocrService,
            rawOutput,
            "SFX_EFFECT",
            "(B_ROUND)",
            "[B_SQUARE]",
            "OUTSIDE_TEXT",
            true,
            true,
            true,
            true
        ) as String

        val expected = """
            (B_ROUND) : Halo!
            [B_SQUARE] : Narasi kotak.
            SFX_EFFECT : *sfx crash*
            OUTSIDE_TEXT : Teks luar bubble.
        """.trimIndent()

        assertEquals(expected, result)
    }

    @Test
    fun testNormalizeOutputWithFilters() {
        val ocrService = GeminiOcrService()
        val rawOutput = """
            [BLOCK 1] () Halo!
            [BLOCK 2] [] Narasi kotak.
            [BLOCK 3] // *sfx crash*
            [BLOCK 4] '' Teks luar bubble.
        """.trimIndent()

        val normalizeMethod: Method = GeminiOcrService::class.java.getDeclaredMethod(
            "normalizeOutput",
            String::class.java,
            String::class.java,
            String::class.java,
            String::class.java,
            String::class.java,
            Boolean::class.javaPrimitiveType,
            Boolean::class.javaPrimitiveType,
            Boolean::class.javaPrimitiveType,
            Boolean::class.javaPrimitiveType
        ).apply { isAccessible = true }

        // Filter out Bubble Square and Outside Bubble, keeping Bubble Round and SFX
        val result = normalizeMethod.invoke(ocrService, rawOutput, "//", "()", "[]", "''", true, false, true, false) as String
        val expected = """
            () : Halo!
            // : *sfx crash*
        """.trimIndent()

        assertEquals(expected, result)
    }
}
