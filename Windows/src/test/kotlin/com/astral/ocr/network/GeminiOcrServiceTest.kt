package com.astral.ocr.network

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class GeminiOcrServiceTest {

    @Test
    fun testPngWithAlphaChannelImageIOEncoding() {
        // Create an ARGB PNG image with transparency (typical PNG from graphics tools)
        val width = 200
        val height = 200
        val argbImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = argbImage.createGraphics()
        g.color = Color(255, 0, 0, 128) // semi-transparent red
        g.fillRect(0, 0, width, height)
        g.color = Color.BLACK
        g.drawString("Test PNG", 20, 20)
        g.dispose()

        // Save as PNG file
        val tempPngFile = File.createTempFile("test_sample", ".png")
        tempPngFile.deleteOnExit()
        ImageIO.write(argbImage, "png", tempPngFile)

        // Read PNG back using ImageIO.read (simulating extractSpeech loading)
        val loadedImage = ImageIO.read(tempPngFile)
        assertNotNull(loadedImage)
        assertTrue(loadedImage.colorModel.hasAlpha() || loadedImage.type == BufferedImage.TYPE_INT_ARGB || loadedImage.type == BufferedImage.TYPE_4BYTE_ABGR)

        val service = GeminiOcrService()

        // Direct test on encodeBufferedImage
        val base64Encoded = service.encodeBufferedImage(loadedImage)
        assertTrue(base64Encoded.isNotEmpty(), "Base64 encoded string must not be empty for PNG with alpha channel")
        val decodedBytes = java.util.Base64.getDecoder().decode(base64Encoded)
        assertTrue(decodedBytes.isNotEmpty(), "Decoded byte array must not be empty")
        // Check JPEG header magic numbers (0xFF, 0xD8)
        assertEquals(0xFF.toByte(), decodedBytes[0])
        assertEquals(0xD8.toByte(), decodedBytes[1])

        // Test running extractSpeech with invalid API key to trigger request processing but verify image encoding doesn't fail silently or pass empty data
        runBlocking {
            val result = service.extractSpeech(
                file = tempPngFile,
                apiKey = "fake_key",
                model = "gemini-2.0-flash",
                sliceEnabled = false
            )
            assertTrue(result.isFailure)
            val errorMsg = result.exceptionOrNull()?.message.orEmpty()
            // Error should be an API error or network error, NOT "Gagal memuat gambar" or "Gagal mengodekan gambar"
            assertTrue(
                !errorMsg.contains("Gagal memuat gambar"),
                "Should successfully load and process PNG image without failing on image loading"
            )
        }
    }
}
