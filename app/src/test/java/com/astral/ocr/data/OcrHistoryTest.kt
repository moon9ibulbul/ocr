package com.astral.ocr.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer

class OcrHistoryTest {

    @Test
    fun testOcrHistoryItemSerialization() {
        val item = OcrHistoryItem(
            id = "test-id",
            timestamp = "2025-01-01 12:00:00",
            text = "Hello OCR"
        )
        val serialized = Json.encodeToString(OcrHistoryItem.serializer(), item)
        val deserialized = Json.decodeFromString<OcrHistoryItem>(serialized)

        assertEquals("test-id", deserialized.id)
        assertEquals("2025-01-01 12:00:00", deserialized.timestamp)
        assertEquals("Hello OCR", deserialized.text)
    }

    @Test
    fun testHistoryListSerialization() {
        val items = listOf(
            OcrHistoryItem("1", "2025-01-01 12:00:00", "Text 1"),
            OcrHistoryItem("2", "2025-01-01 12:01:00", "Text 2")
        )
        val serialized = Json.encodeToString(ListSerializer(OcrHistoryItem.serializer()), items)
        val deserialized = Json.decodeFromString<List<OcrHistoryItem>>(serialized)

        assertEquals(2, deserialized.size)
        assertEquals("Text 1", deserialized[0].text)
        assertEquals("Text 2", deserialized[1].text)
    }
}
