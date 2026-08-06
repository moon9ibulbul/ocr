package com.astral.ocr.network

import com.astral.ocr.data.OcrSegmentResult
import com.astral.ocr.data.DEFAULT_SEGMENT_HEIGHT
import com.astral.ocr.data.DEFAULT_SEGMENT_OVERLAP
import com.astral.ocr.data.MIN_SEGMENT_HEIGHT
import com.astral.ocr.data.sliceVerticalWithOverlap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.Base64
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class GeminiOcrService(
    private val client: OkHttpClient = defaultClient(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) {

    suspend fun extractSpeech(
        file: File,
        apiKey: String,
        model: String,
        apiProvider: String = "gemini",
        sliceEnabled: Boolean = true,
        targetSliceHeight: Int = DEFAULT_SEGMENT_HEIGHT,
        pageIndex: Int = 0,
        totalPages: Int = 1,
        onProgress: (String) -> Unit = {},
        customLegend: String = "//",
        legendBubbleRound: String = "()",
        legendBubbleSquare: String = "[]",
        legendOutside: String = "''",
        batchSize: Int = 5,
        includeBubbleRound: Boolean = true,
        includeBubbleSquare: Boolean = true,
        includeSFX: Boolean = true,
        includeOutside: Boolean = true
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || model.isBlank()) {
            return@withContext Result.failure(IllegalStateException("API key dan model harus diisi pada pengaturan."))
        }

        if (!file.exists()) {
            return@withContext Result.failure(IOException("Berkas gambar tidak ditemukan: ${file.absolutePath}"))
        }

        val payloadMimeType = "image/jpeg"
        val image = try {
            ImageIO.read(file)
        } catch (ex: Exception) {
            null
        } ?: return@withContext Result.failure(IOException("Gagal memuat gambar."))

        val safeSliceHeight = targetSliceHeight.coerceAtLeast(MIN_SEGMENT_HEIGHT)
        val segments = if (sliceEnabled && image.height > LONG_PAGE_HEIGHT_THRESHOLD) {
            sliceVerticalWithOverlap(image, targetHeight = safeSliceHeight, overlap = SLICE_OVERLAP)
        } else {
            listOf(image)
        }

        val segmentResults = mutableListOf<OcrSegmentResult>()
        val totalSegments = segments.size

        val chunkedSegments = segments.withIndex().chunked(batchSize)
        val totalBatches = chunkedSegments.size

        chunkedSegments.forEachIndexed { batchIdx, indexedSegments ->
            val startIdx = indexedSegments.first().index
            val endIdx = indexedSegments.last().index
            val segmentRangeStr = if (indexedSegments.size == 1) "${startIdx + 1}" else "${startIdx + 1}-${endIdx + 1}"
            onProgress("Gambar ${pageIndex + 1}/$totalPages, segmen $segmentRangeStr/$totalSegments")

            val prompt = buildBatchPrompt(startIdx + 1, endIdx + 1, totalSegments, customLegend, legendBubbleRound, legendBubbleSquare, legendOutside)
            val base64s = indexedSegments.map { encodeBufferedImage(it.value) }

            val response = requestWithRetry(apiKey, model, payloadMimeType, base64s, prompt, apiProvider = apiProvider)
            response.fold(
                onSuccess = { raw ->
                    segmentResults.add(
                        OcrSegmentResult(
                            pageIndex = pageIndex,
                            segmentIndex = batchIdx,
                            totalSegments = totalBatches,
                            rawText = normalizeOutput(
                                raw,
                                customLegend,
                                legendBubbleRound,
                                legendBubbleSquare,
                                legendOutside,
                                includeBubbleRound,
                                includeBubbleSquare,
                                includeSFX,
                                includeOutside
                            )
                        )
                    )
                },
                onFailure = { ex ->
                    return@withContext Result.failure(ex)
                }
            )
        }

        val merged = mergeSegments(segmentResults)
        Result.success(merged)
    }

    private fun parseErrorMessage(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return try {
            val parsed = json.decodeFromString(GeminiErrorResponse.serializer(), raw)
            parsed.error?.message
        } catch (_: Exception) {
            raw
        }
    }

    private fun buildBatchPrompt(
        startSegment: Int,
        endSegment: Int,
        totalSegments: Int,
        customLegend: String = "//",
        legendBubbleRound: String = "()",
        legendBubbleSquare: String = "[]",
        legendOutside: String = "''"
    ): String {
        val segmentRange = if (startSegment == endSegment) "SEGMEN $startSegment" else "SEGMEN $startSegment sampai $endSegment"
        return """
            Kamu adalah asisten OCR khusus untuk manhwa. Input yang diberikan terdiri dari beberapa segmen gambar berturut-turut ($segmentRange dari total $totalSegments segmen).
            PENTING: Proses segmen-segmen gambar ini sesuai urutannya. Hanya baca teks yang benar-benar terlihat pada segmen-segmen ini, jangan menebak kelanjutan di luar gambar.
            If segmen gambar ini kosong, tidak memiliki balon ucapan (speech bubble), tidak memiliki efek suara (SFX), atau tidak memiliki teks sama sekali, kamu HARUS mengembalikan teks "Tidak ada teks yang terdeteksi". Jangan berhalisnan, jangan menebak dialog, dan jangan mengasumsikan dialog atau cerita sendiri jika gambarnya kosong atau tidak ada teks.

            Tugas:
            - Temukan semua teks pada bubble bulat/oval, bubble kotak, efek suara (SFX), dan teks luar bubble pada seluruh segmen tersebut secara berurutan.
            - Urutkan berdasarkan posisi visual: dari atas ke bawah, dan jika sejajar secara vertikal, dari kiri ke kanan.
            - Beri nomor setiap blok teks agar urutan mudah diikuti. Gunakan format `[BLOCK n] <tipe> <teks>`.
            - Tipe teks:
              * Bubble bulat/oval -> `$legendBubbleRound`
              * Bubble kotak -> `$legendBubbleSquare`
              * SFX -> `$customLegend`
              * Teks luar bubble -> `$legendOutside`
            - Contoh keluaran:
              [BLOCK 1] $legendBubbleRound Halo apa kabar?
              [BLOCK 2] $legendBubbleSquare Ini contoh narasi.
              [BLOCK 3] $customLegend *tap tap*
              [BLOCK 4] $legendOutside Catatan editor
            Aturan tambahan:
            - Urutkan teks sesuai instruksi posisi, jangan mengubah urutan dialog seenaknya.
            - Jangan menggabungkan bubble berbeda menjadi satu kalimat jika posisinya terpisah.
            - Gunakan bahasa asli hasil OCR, jangan terjemahkan.
            - Output hanya daftar teks dengan format di atas tanpa penjelasan tambahan.
        """.trimIndent()
    }

    private fun normalizeOutput(
        raw: String,
        customLegend: String = "//",
        legendBubbleRound: String = "()",
        legendBubbleSquare: String = "[]",
        legendOutside: String = "''",
        includeBubbleRound: Boolean = true,
        includeBubbleSquare: Boolean = true,
        includeSFX: Boolean = true,
        includeOutside: Boolean = true
    ): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""

        val normalizedLower = trimmed.lowercase()
        val emptyMarkers = listOf(
            "tidak ada teks yang terdeteksi",
            "tampaknya tidak ada teks yang dapat dibaca",
            "tidak ada teks yang dapat dibaca",
            "no text detected",
            "no readable text"
        )
        if (emptyMarkers.any { normalizedLower.contains(it) }) return ""

        val fillerPrefixes = listOf(
            "oke",
            "ok,",
            "okey",
            "baik",
            "berikut hasil",
            "ini dia hasil",
            "oke, ini dia hasil",
            "baik, berikut",
            "berikut adalah hasil"
        )

        val lines = trimmed.split('\n')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .filterNot { line ->
                val lower = line.lowercase()
                fillerPrefixes.any { prefix -> lower.startsWith(prefix) }
            }

        data class Block(val order: Int?, val prefix: String?, val builder: StringBuilder, val originalIndex: Int)

        val blockRegex = Regex("^\\[BLOCK\\s*(\\d+)]\\s*(.*)$", RegexOption.IGNORE_CASE)
        val prefixes = listOf(legendBubbleRound, legendBubbleSquare, customLegend, legendOutside)
        val blocks = mutableListOf<Block>()
        var lastBlock: Block? = null

        fun extractPrefix(text: String): Pair<String?, String> {
            val prefix = prefixes.firstOrNull { candidate ->
                text.startsWith(candidate) || text.startsWith("${candidate} :")
            }
            return if (prefix != null) {
                val cleaned = text.removePrefix(prefix).trim().removePrefix(":").trim()
                prefix to cleaned
            } else prefix to text
        }

        for ((index, line) in lines.withIndex()) {
            val match = blockRegex.find(line)
            val order = match?.groupValues?.getOrNull(1)?.toIntOrNull()
            val rawContent = match?.groupValues?.getOrNull(2)?.trim().orEmpty().ifBlank { line }
            val (prefix, content) = extractPrefix(rawContent)

            when {
                order != null -> {
                    val block = Block(order, prefix ?: lastBlock?.prefix, StringBuilder(content), index)
                    blocks.add(block)
                    lastBlock = block
                }

                prefix != null -> {
                    val block = Block(null, prefix, StringBuilder(content), index)
                    blocks.add(block)
                    lastBlock = block
                }

                lastBlock != null -> {
                    lastBlock.builder.append(' ').append(rawContent)
                }

                else -> {
                    val block = Block(null, null, StringBuilder(rawContent), index)
                    blocks.add(block)
                    lastBlock = block
                }
            }
        }

        val sorted = blocks.sortedWith { a, b ->
            when {
                a.order != null && b.order != null -> a.order.compareTo(b.order)
                a.order != null -> -1
                b.order != null -> 1
                else -> a.originalIndex.compareTo(b.originalIndex)
            }
        }

        return sorted.mapNotNull { block ->
            val prefix = block.prefix
            val text = block.builder.toString()
                .replace("\\n", " ")
                .replace("\\r", " ")
                .replace(Regex("\\s+"), " ")
                .trim()
            if (text.isBlank()) return@mapNotNull null

            if (prefix != null) {
                if (prefix == legendBubbleRound && !includeBubbleRound) return@mapNotNull null
                if (prefix == legendBubbleSquare && !includeBubbleSquare) return@mapNotNull null
                if (prefix == customLegend && !includeSFX) return@mapNotNull null
                if (prefix == legendOutside && !includeOutside) return@mapNotNull null
            }

            if (prefix.isNullOrBlank()) text else "$prefix : $text"
        }.joinToString(separator = "\n")
    }

    private fun encodeBufferedImage(image: BufferedImage): String {
        val stream = ByteArrayOutputStream()
        ImageIO.write(image, "jpeg", stream)
        val bytes = stream.toByteArray()
        return Base64.getEncoder().encodeToString(bytes)
    }

    private suspend fun requestWithRetry(
        apiKey: String,
        model: String,
        mimeType: String,
        base64s: List<String>,
        prompt: String,
        retries: Int = MAX_RETRIES,
        apiProvider: String = "gemini"
    ): Result<String> {
        var attempt = 0
        var delayMs = INITIAL_BACKOFF_MS

        while (attempt <= retries) {
            val request = if (apiProvider == "sumopod") {
                val contentList = mutableListOf<OpenAiContentPart>()
                contentList.add(OpenAiContentPart(type = "text", text = prompt))
                base64s.forEach { base64 ->
                    contentList.add(
                        OpenAiContentPart(
                            type = "image_url",
                            imageUrl = OpenAiImageUrl(url = "data:$mimeType;base64,$base64")
                        )
                    )
                }
                val requestBody = OpenAiRequest(
                    model = model,
                    messages = listOf(
                        OpenAiMessage(
                            role = "user",
                            content = contentList
                        )
                    ),
                    maxTokens = 2048
                )
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = json.encodeToString(requestBody).toRequestBody(mediaType)
                Request.Builder()
                    .url("https://ai.sumopod.com/v1/chat/completions")
                    .header("Authorization", "Bearer $apiKey")
                    .post(body)
                    .build()
            } else {
                val partsList = mutableListOf<GeminiPart>()
                partsList.add(GeminiPart(text = prompt))
                base64s.forEach { base64 ->
                    partsList.add(
                        GeminiPart(
                            inlineData = InlineData(
                                mimeType = mimeType,
                                data = base64
                            )
                        )
                    )
                }
                val requestBody = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = partsList
                        )
                    )
                )
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = json.encodeToString(requestBody).toRequestBody(mediaType)
                Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
                    .post(body)
                    .build()
            }

            try {
                executeRequest(request).use { response ->
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string()
                        val message = if (apiProvider == "sumopod") {
                            try {
                                json.decodeFromString(OpenAiResponse.serializer(), errorBody ?: "").error?.message
                            } catch (_: Exception) {
                                errorBody
                            }
                        } else {
                            parseErrorMessage(errorBody)
                        }
                        val friendly = if (response.code == 429) {
                            "Batas kuota API tercapai. Coba lagi nanti atau gunakan model lain."
                        } else message
                        throw IOException(friendly ?: "Permintaan gagal dengan kode ${response.code}")
                    }

                    val responseBody = response.body?.string() ?: return Result.failure(IOException("Respon kosong dari API"))
                    val text = if (apiProvider == "sumopod") {
                        val parsed = json.decodeFromString(OpenAiResponse.serializer(), responseBody)
                        parsed.choices?.firstOrNull()?.message?.content
                    } else {
                        val parsed = json.decodeFromString(GenerateContentResponse.serializer(), responseBody)
                        parsed.candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.text != null }?.text
                    }

                    if (text.isNullOrBlank()) {
                        return Result.failure(IllegalStateException("API tidak mengembalikan teks."))
                    }
                    return Result.success(text)
                }
            } catch (ex: IOException) {
                if (attempt == retries) {
                    return Result.failure(ex)
                }
                delay(delayMs)
                delayMs = (delayMs * BACKOFF_MULTIPLIER).toLong()
                attempt++
            }
        }
        return Result.failure(IOException("Gagal memproses permintaan."))
    }

    private suspend fun executeRequest(request: Request): Response = suspendCancellableCoroutine { continuation ->
        val call = client.newCall(request)
        continuation.invokeOnCancellation { call.cancel() }
        try {
            val response = call.execute()
            if (continuation.isActive) {
                continuation.resume(response)
            } else {
                response.close()
            }
        } catch (ex: IOException) {
            if (continuation.isActive) {
                continuation.resumeWithException(ex)
            }
        }
    }

    private fun mergeSegments(segments: List<OcrSegmentResult>): String {
        if (segments.isEmpty()) return ""
        val sorted = segments.sortedBy { it.segmentIndex }

        val mergedLines = mutableListOf<String>()
        for (segment in sorted) {
            val lines = segment.rawText.split('\n')
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            for (line in lines) {
                val window = mergedLines.takeLast(3)
                if (window.contains(line)) continue
                mergedLines.add(line)
            }
        }
        return mergedLines.joinToString(separator = "\n")
    }

    companion object {
        const val LONG_PAGE_HEIGHT_THRESHOLD = 3400
        const val SLICE_OVERLAP = DEFAULT_SEGMENT_OVERLAP
        private const val MAX_RETRIES = 3
        private const val INITIAL_BACKOFF_MS = 2000L
        private const val BACKOFF_MULTIPLIER = 2

        private fun defaultClient(): OkHttpClient {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            return OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build()
        }
    }
}
