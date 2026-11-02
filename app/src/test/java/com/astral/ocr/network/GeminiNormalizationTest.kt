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
            Boolean::class.javaPrimitiveType,
            String::class.java
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
            true,
            "default"
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
            Boolean::class.javaPrimitiveType,
            String::class.java
        ).apply { isAccessible = true }

        // Filter out Bubble Square and Outside Bubble, keeping Bubble Round and SFX
        val result = normalizeMethod.invoke(ocrService, rawOutput, "//", "()", "[]", "''", true, false, true, false, "default") as String
        val expected = """
            () : Halo!
            // : *sfx crash*
        """.trimIndent()

        assertEquals(expected, result)
    }

    @Test
    fun testNormalizeOutputSanitizesLiteralNewlines() {
        val ocrService = GeminiOcrService()
        val rawOutput = """
            [BLOCK 1] () Halo!\n\n
            [BLOCK 2] [] Narasi\nkotak.
            [BLOCK 3] // *sfx\rcrash*
            [BLOCK 4] '' Teks luar bubble.\n
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

    @Test
    fun testApplyLegendOverrideMultiSegmentPanel() {
        val ocrService = GeminiOcrService()
        val mergedSegmentsText = """
            () : Segmen 1 Teks A
            [] : Segmen 1 Teks B
            () : Segmen 2 Teks C
            // : Segmen 2 SFX
        """.trimIndent()

        val applyMethod: Method = GeminiOcrService::class.java.getDeclaredMethod(
            "applyLegendOverride",
            String::class.java,
            String::class.java,
            List::class.java
        ).apply { isAccessible = true }

        val prefixes = listOf("()", "[]", "//", "''")

        // 1. Numbered: should be sequentially numbered across the entire panel (1 to 4)
        val numberedResult = applyMethod.invoke(ocrService, mergedSegmentsText, "number", prefixes) as String
        val expectedNumbered = """
            1. Segmen 1 Teks A
            2. Segmen 1 Teks B
            3. Segmen 2 Teks C
            4. Segmen 2 SFX
        """.trimIndent()
        assertEquals(expectedNumbered, numberedResult)

        // 2. Strip: all lines prefixed with '-'
        val stripResult = applyMethod.invoke(ocrService, mergedSegmentsText, "strip", prefixes) as String
        val expectedStrip = """
            - Segmen 1 Teks A
            - Segmen 1 Teks B
            - Segmen 2 Teks C
            - Segmen 2 SFX
        """.trimIndent()
        assertEquals(expectedStrip, stripResult)

        // 3. None: plain texts
        val noneResult = applyMethod.invoke(ocrService, mergedSegmentsText, "none", prefixes) as String
        val expectedNone = """
            Segmen 1 Teks A
            Segmen 1 Teks B
            Segmen 2 Teks C
            Segmen 2 SFX
        """.trimIndent()
        assertEquals(expectedNone, noneResult)
    }
}
