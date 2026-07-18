package com.astral.ocr.data

import kotlinx.serialization.Serializable

@Serializable
data class OcrHistoryItem(
    val id: String,
    val timestamp: String,
    val text: String
)
