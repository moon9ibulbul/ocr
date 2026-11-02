package com.astral.ocr.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PanelHeaderFormatterTest {

    @Test
    fun testParseImageFileInfoPath() {
        val path = "/Downloads/Raw/Guide_39.png"
        val info = parseImageFileInfo(null, path)
        assertEquals("Guide_39", info.name)
        assertEquals("png", info.ext)
        assertEquals("/Downloads/Raw/Guide_39.png", info.path)
    }

    @Test
    fun testFormatPanelHeaderVariables() {
        val fileInfo = ImageFileInfo(name = "Guide_39", ext = "jpg", path = "/Downloads/Raw/Guide_39.jpg")
        val format = "{name}.{ext} [{num}] ({path})"
        val result = formatPanelHeader(format, 1, fileInfo)
        assertEquals("Guide_39.jpg [1] (/Downloads/Raw/Guide_39.jpg)", result)
    }

    @Test
    fun testFormatCombinedResultsDefaultFormat() {
        val results = listOf(
            OcrResult("/Downloads/Raw/Guide_39.png", "Hello world", 100L),
            OcrResult("/Downloads/Raw/Guide_40.jpg", "Second line", 150L)
        )
        val combined = formatCombinedResults(null, results, "Panel {num}")
        val expected = "Panel 1\nHello world\n\nPanel 2\nSecond line"
        assertEquals(expected, combined)
    }

    @Test
    fun testFormatCombinedResultsCustomVariables() {
        val results = listOf(
            OcrResult("/Downloads/Raw/Guide_39.png", "Hello world", 100L)
        )
        val combined = formatCombinedResults(null, results, "{name}_{num}.{ext}")
        val expected = "Guide_39_1.png\nHello world"
        assertEquals(expected, combined)
    }
}
