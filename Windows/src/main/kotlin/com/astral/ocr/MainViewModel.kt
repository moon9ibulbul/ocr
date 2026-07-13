package com.astral.ocr

import com.astral.ocr.data.DEFAULT_SEGMENT_HEIGHT
import com.astral.ocr.data.MIN_SEGMENT_HEIGHT
import com.astral.ocr.data.OcrResult
import com.astral.ocr.data.SettingsRepository
import com.astral.ocr.network.GeminiOcrService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.coroutineContext

class MainViewModel(
    private val scope: CoroutineScope,
    private val settingsRepository: SettingsRepository = SettingsRepository(),
    private val geminiOcrService: GeminiOcrService = GeminiOcrService()
) {

    data class UiState(
        val apiKey: String = "",
        val model: String = DEFAULT_MODEL,
        val isProcessing: Boolean = false,
        val results: List<OcrResult> = emptyList(),
        val bulkMode: Boolean = false,
        val lastSavedPath: String? = null,
        val progressMessage: String? = null,
        val apiProvider: String = "gemini",
        val sliceEnabled: Boolean = true,
        val sliceHeight: Int = DEFAULT_SEGMENT_HEIGHT,
        val customLegend: String = "//",
        val legendBubbleRound: String = "()",
        val legendBubbleSquare: String = "[]",
        val legendOutside: String = "''"
    )

    private val mutableResults = MutableStateFlow<List<OcrResult>>(emptyList())
    private val mutableProcessing = MutableStateFlow(false)
    private val mutableBulkMode = MutableStateFlow(false)
    private val mutableLastSavedPath = MutableStateFlow<String?>(null)
    private val mutableProgress = MutableStateFlow<String?>(null)

    private var processingJob: Job? = null

    val notifications = MutableSharedFlow<String?>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<UiState> = combine(
        settingsRepository.apiKey,
        settingsRepository.model,
        settingsRepository.apiProvider,
        settingsRepository.sliceEnabled,
        settingsRepository.sliceHeight,
        settingsRepository.customLegend,
        settingsRepository.legendBubbleRound,
        settingsRepository.legendBubbleSquare,
        settingsRepository.legendOutside,
        mutableProcessing,
        mutableResults,
        mutableBulkMode,
        mutableLastSavedPath,
        mutableProgress
    ) { values ->
        val apiKey = values[0] as String
        val model = values[1] as String
        val apiProvider = values[2] as String
        val sliceEnabled = values[3] as Boolean
        val sliceHeight = values[4] as Int
        val customLegend = values[5] as String
        val legendBubbleRound = values[6] as String
        val legendBubbleSquare = values[7] as String
        val legendOutside = values[8] as String
        val processing = values[9] as Boolean
        val results = values[10] as List<OcrResult>
        val bulk = values[11] as Boolean
        val saved = values[12] as String?
        val progress = values[13] as String?

        UiState(
            apiKey = apiKey,
            model = if (model.isBlank()) DEFAULT_MODEL else model,
            apiProvider = apiProvider,
            isProcessing = processing,
            results = results,
            bulkMode = bulk,
            lastSavedPath = saved,
            progressMessage = progress,
            sliceEnabled = sliceEnabled,
            sliceHeight = sliceHeight.coerceAtLeast(MIN_SEGMENT_HEIGHT),
            customLegend = if (customLegend.isBlank()) "//" else customLegend,
            legendBubbleRound = if (legendBubbleRound.isBlank()) "()" else legendBubbleRound,
            legendBubbleSquare = if (legendBubbleSquare.isBlank()) "[]" else legendBubbleSquare,
            legendOutside = if (legendOutside.isBlank()) "''" else legendOutside
        )
    }.stateIn(scope, SharingStarted.Eagerly, UiState())

    fun toggleBulkMode(enabled: Boolean) {
        mutableBulkMode.value = enabled
    }

    fun updateApiKey(value: String) {
        scope.launch {
            settingsRepository.updateApiKey(value)
        }
    }

    fun updateModel(value: String) {
        scope.launch {
            settingsRepository.updateModel(value)
        }
    }

    fun updateApiProvider(value: String) {
        scope.launch {
            settingsRepository.updateApiProvider(value)
        }
    }

    fun updateSliceEnabled(enabled: Boolean) {
        scope.launch {
            settingsRepository.updateSliceEnabled(enabled)
        }
    }

    fun updateSliceHeight(value: Int) {
        scope.launch {
            val safeValue = value.coerceAtLeast(MIN_SEGMENT_HEIGHT)
            settingsRepository.updateSliceHeight(safeValue)
        }
    }

    fun updateCustomLegend(value: String) {
        scope.launch {
            settingsRepository.updateCustomLegend(value)
        }
    }

    fun updateLegendBubbleRound(value: String) {
        scope.launch {
            settingsRepository.updateLegendBubbleRound(value)
        }
    }

    fun updateLegendBubbleSquare(value: String) {
        scope.launch {
            settingsRepository.updateLegendBubbleSquare(value)
        }
    }

    fun updateLegendOutside(value: String) {
        scope.launch {
            settingsRepository.updateLegendOutside(value)
        }
    }

    fun processSingle(file: File) {
        processingJob?.cancel()
        processingJob = scope.launch {
            performProcessing(listOf(file))
        }
    }

    fun processBulk(files: List<File>) {
        if (files.isEmpty()) return
        processingJob?.cancel()
        processingJob = scope.launch {
            performProcessing(files)
        }
    }

    fun clearResults() {
        mutableResults.value = emptyList()
    }

    fun cancelProcessing() {
        processingJob?.cancel()
        mutableProcessing.value = false
        mutableProgress.value = null
        processingJob = null
    }

    fun setLastSavedPath(path: String?) {
        mutableLastSavedPath.value = path
    }

    companion object {
        const val DEFAULT_MODEL = "gemini-2.0-flash"
    }

    private fun notifyError(ex: Throwable) {
        val message = ex.message ?: "Terjadi kesalahan tidak diketahui"
        scope.launch {
            notifications.emit(message)
        }
    }

    private suspend fun performProcessing(files: List<File>) {
        mutableProcessing.value = true
        mutableProgress.value = "Menyiapkan gambar..."
        val newResults = mutableListOf<OcrResult>()
        val total = files.size

        try {
            for ((index, file) in files.withIndex()) {
                coroutineContext.ensureActive()
                val start = System.currentTimeMillis()
                val result = geminiOcrService.extractSpeech(
                    file,
                    uiState.value.apiKey,
                    uiState.value.model,
                    apiProvider = uiState.value.apiProvider,
                    sliceEnabled = uiState.value.sliceEnabled,
                    targetSliceHeight = uiState.value.sliceHeight,
                    pageIndex = index,
                    totalPages = total,
                    onProgress = { message -> mutableProgress.value = message },
                    customLegend = uiState.value.customLegend,
                    legendBubbleRound = uiState.value.legendBubbleRound,
                    legendBubbleSquare = uiState.value.legendBubbleSquare,
                    legendOutside = uiState.value.legendOutside
                )
                result.fold(
                    onSuccess = { text ->
                        val duration = System.currentTimeMillis() - start
                        newResults.add(OcrResult(file.name, text, duration))
                    },
                    onFailure = { ex ->
                        notifyError(ex)
                    }
                )
            }
            mutableResults.value = newResults
        } finally {
            mutableProgress.value = null
            mutableProcessing.value = false
            processingJob = null
        }
    }
}
