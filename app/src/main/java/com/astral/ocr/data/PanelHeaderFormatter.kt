package com.astral.ocr.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

data class ImageFileInfo(
    val name: String,
    val ext: String,
    val path: String
)

fun parseImageFileInfo(context: Context?, uriString: String): ImageFileInfo {
    if (uriString.isBlank()) {
        return ImageFileInfo(name = "", ext = "", path = "")
    }

    // Check if it's a file URI or raw path
    if (uriString.startsWith("file://") || uriString.startsWith("/")) {
        val cleanPath = if (uriString.startsWith("file://")) uriString.removePrefix("file://") else uriString
        val file = java.io.File(cleanPath)
        val nameWithoutExt = file.nameWithoutExtension
        val extension = file.extension
        return ImageFileInfo(
            name = nameWithoutExt,
            ext = extension,
            path = cleanPath
        )
    }

    // Handle content:// URI
    if (uriString.startsWith("content://") && context != null) {
        val uri = Uri.parse(uriString)
        var displayName: String? = null
        try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        displayName = cursor.getString(index)
                    }
                }
            }
        } catch (_: Exception) { }

        val actualName = displayName ?: uri.lastPathSegment ?: "image"
        val dotIndex = actualName.lastIndexOf('.')
        val (name, ext) = if (dotIndex > 0 && dotIndex < actualName.length - 1) {
            actualName.substring(0, dotIndex) to actualName.substring(dotIndex + 1)
        } else {
            actualName to ""
        }
        val pathStr = uri.path ?: uriString
        return ImageFileInfo(name = name, ext = ext, path = pathStr)
    }

    // Fallback parsing for unknown uri/string
    val lastSegment = uriString.substringAfterLast('/').substringAfterLast('\\')
    val dotIndex = lastSegment.lastIndexOf('.')
    val (name, ext) = if (dotIndex > 0 && dotIndex < lastSegment.length - 1) {
        lastSegment.substring(0, dotIndex) to lastSegment.substring(dotIndex + 1)
    } else {
        lastSegment to ""
    }
    return ImageFileInfo(name = name, ext = ext, path = uriString)
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
    context: Context?,
    results: List<OcrResult>,
    headerFormat: String
): String {
    if (results.isEmpty()) return ""
    return results.mapIndexed { index, result ->
        val fileInfo = parseImageFileInfo(context, result.imageUri)
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
