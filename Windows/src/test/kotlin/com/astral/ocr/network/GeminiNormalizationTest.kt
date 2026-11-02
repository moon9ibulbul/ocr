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
            Boolean::class.javaPrimitiveType,
            String::class.java
        ).apply { isAccessible = true }

        val result = normalizeMethod.invoke(ocrService, rawOutput, "//", "()", "[]", "''", true, true, true, true, "default") as String
        val expected = """
            () : Halo!
            [] : Narasi kotak.
            // : *sfx crash*
            '' : Teks luar bubble.
        """.trimIndent()

        assertEquals(expected, result)
    }

    @Test
    fun testNormalizeOutputWithLegendOverrideNone() {
        val ocrService = GeminiOcrService()
        val rawOutput = """
            [BLOCK 1] () Halo!
            [BLOCK 2] [] Narasi kotak.
            [BLOCK 3] // *sfx crash*
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
            Boolean::class.javaPrimitiveType,
            String::class.java
        ).apply { isAccessible = true }

        val result = normalizeMethod.invoke(ocrService, rawOutput, "//", "()", "[]", "''", true, true, true, true, "none") as String
        val expected = """
            Halo!
            Narasi kotak.
            *sfx crash*
        """.trimIndent()

        assertEquals(expected, result)
    }

    @Test
    fun testNormalizeOutputWithLegendOverrideStrip() {
        val ocrService = GeminiOcrService()
        val rawOutput = """
            [BLOCK 1] () Halo!
            [BLOCK 2] [] Narasi kotak.
            [BLOCK 3] // *sfx crash*
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
            Boolean::class.javaPrimitiveType,
            String::class.java
        ).apply { isAccessible = true }

        val result = normalizeMethod.invoke(ocrService, rawOutput, "//", "()", "[]", "''", true, true, true, true, "strip") as String
        val expected = """
            - Halo!
            - Narasi kotak.
            - *sfx crash*
        """.trimIndent()

        assertEquals(expected, result)
    }

    @Test
    fun testNormalizeOutputWithLegendOverrideNumber() {
        val ocrService = GeminiOcrService()
        val rawOutput = """
            [BLOCK 1] () Teks 1
            [BLOCK 2] [] Teks 2
            [BLOCK 3] // Teks 3
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
            Boolean::class.javaPrimitiveType,
            String::class.java
        ).apply { isAccessible = true }

        val result = normalizeMethod.invoke(ocrService, rawOutput, "//", "()", "[]", "''", true, true, true, true, "number") as String
        val expected = """
            1. Teks 1
            2. Teks 2
            3. Teks 3
        """.trimIndent()

        assertEquals(expected, result)
    }
}
