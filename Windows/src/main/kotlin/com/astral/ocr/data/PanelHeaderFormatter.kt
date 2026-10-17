package com.astral.ocr.data

import java.io.File

data class ImageFileInfo(
    val name: String,
    val ext: String,
    val path: String
)

fun parseImageFileInfo(uriOrPath: String): ImageFileInfo {
    if (uriOrPath.isBlank()) {
        return ImageFileInfo(name = "", ext = "", path = "")
    }

    val cleanPath = if (uriOrPath.startsWith("file://")) uriOrPath.removePrefix("file://") else uriOrPath
    val normalizedPath = cleanPath.replace('\\', '/')
    val file = File(normalizedPath)

    val nameWithoutExt = file.nameWithoutExtension
    val extension = file.extension

    return ImageFileInfo(
        name = nameWithoutExt,
        ext = extension,
        path = cleanPath
    )
}

fun formatPanelHeader(
    format: String,
    num: Int,
    fileInfo: ImageFileInfo
): String {
    val effectiveFormat = if (format.isBlank()) "Panel {num}" else format
    return effectiveFormat
        .replace("{num}", num.toString())
        .replace("{name}", fileInfo.name)
        .replace("{ext}", fileInfo.ext)
        .replace("{path}", fileInfo.path)
}

fun formatCombinedResults(
    results: List<OcrResult>,
    headerFormat: String
): String {
    if (results.isEmpty()) return ""
    return results.mapIndexed { index, result ->
        val fileInfo = parseImageFileInfo(result.imageUri)
        val header = formatPanelHeader(headerFormat, index + 1, fileInfo)
        buildString {
            if (header.isNotEmpty()) {
                append(header)
                append('\n')
            }
            append(result.processedText.trimEnd())
        }
    }.joinToString(separator = "\n\n")
}
