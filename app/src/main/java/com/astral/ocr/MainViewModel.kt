package com.astral.ocr

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.astral.ocr.data.DEFAULT_SEGMENT_HEIGHT
import com.astral.ocr.data.MIN_SEGMENT_HEIGHT
import com.astral.ocr.data.OcrResult
import com.astral.ocr.data.SettingsRepository
import com.astral.ocr.network.GeminiOcrService
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
import kotlin.coroutines.coroutineContext

class MainViewModel(
    private val context: Context,
    private val settingsRepository: SettingsRepository = SettingsRepository(context),
    private val geminiOcrService: GeminiOcrService = GeminiOcrService()
) : ViewModel() {

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
        val legendOutside: String = "''",
        val ocrHistory: List<com.astral.ocr.data.OcrHistoryItem> = emptyList(),
        val batchSize: Int = 5,
        val includeBubbleRound: Boolean = true,
        val includeBubbleSquare: Boolean = true,
        val includeSFX: Boolean = true,
        val includeOutside: Boolean = true
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
        settingsRepository.ocrHistory,
        settingsRepository.batchSize,
        settingsRepository.includeBubbleRound,
        settingsRepository.includeBubbleSquare,
        settingsRepository.includeSFX,
        settingsRepository.includeOutside,
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
        val ocrHistoryStr = values[9] as String
        val batchSizeVal = values[10] as Int
        val includeBubbleRoundVal = values[11] as Boolean
        val includeBubbleSquareVal = values[12] as Boolean
        val includeSFXVal = values[13] as Boolean
        val includeOutsideVal = values[14] as Boolean
        val processing = values[15] as Boolean
        val results = values[16] as List<OcrResult>
        val bulk = values[17] as Boolean
        val saved = values[18] as String?
        val progress = values[19] as String?

        val historyList = try {
            kotlinx.serialization.json.Json.decodeFromString<List<com.astral.ocr.data.OcrHistoryItem>>(ocrHistoryStr)
        } catch (e: Exception) {
            emptyList()
        }

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
            legendOutside = if (legendOutside.isBlank()) "''" else legendOutside,
            ocrHistory = historyList,
            batchSize = batchSizeVal,
            includeBubbleRound = includeBubbleRoundVal,
            includeBubbleSquare = includeBubbleSquareVal,
            includeSFX = includeSFXVal,
            includeOutside = includeOutsideVal
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    var onImagePickedCallback: ((Uri?) -> Unit)? = null
    var onMultipleImagesPickedCallback: ((List<Uri>) -> Unit)? = null
    var onDocumentCreatedCallback: ((Uri?) -> Unit)? = null

    fun toggleBulkMode(enabled: Boolean) {
        mutableBulkMode.value = enabled
    }

    fun updateApiKey(value: String) {
        viewModelScope.launch {
            settingsRepository.updateApiKey(value)
        }
    }

    fun updateModel(value: String) {
        viewModelScope.launch {
            settingsRepository.updateModel(value)
        }
    }

    fun updateApiProvider(value: String) {
        viewModelScope.launch {
            settingsRepository.updateApiProvider(value)
        }
    }

    fun updateSliceEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSliceEnabled(enabled)
        }
    }

    fun updateSliceHeight(value: Int) {
        viewModelScope.launch {
            val safeValue = value.coerceAtLeast(MIN_SEGMENT_HEIGHT)
            settingsRepository.updateSliceHeight(safeValue)
        }
    }

    fun updateCustomLegend(value: String) {
        viewModelScope.launch {
            settingsRepository.updateCustomLegend(value)
        }
    }

    fun updateLegendBubbleRound(value: String) {
        viewModelScope.launch {
            settingsRepository.updateLegendBubbleRound(value)
        }
    }

    fun updateLegendBubbleSquare(value: String) {
        viewModelScope.launch {
            settingsRepository.updateLegendBubbleSquare(value)
        }
    }

    fun updateLegendOutside(value: String) {
        viewModelScope.launch {
            settingsRepository.updateLegendOutside(value)
        }
    }

    fun updateBatchSize(value: Int) {
        viewModelScope.launch {
            settingsRepository.updateBatchSize(value)
        }
    }

    fun updateIncludeBubbleRound(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateIncludeBubbleRound(value)
        }
    }

    fun updateIncludeBubbleSquare(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateIncludeBubbleSquare(value)
        }
    }

    fun updateIncludeSFX(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateIncludeSFX(value)
        }
    }

    fun updateIncludeOutside(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateIncludeOutside(value)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            settingsRepository.updateOcrHistory("[]")
        }
    }

    fun addHistoryItem(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val historyList = uiState.value.ocrHistory.toMutableList()
            val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            val newItem = com.astral.ocr.data.OcrHistoryItem(
                id = java.util.UUID.randomUUID().toString(),
                timestamp = timestamp,
                text = text
            )
            historyList.add(0, newItem)
            while (historyList.size > 50) {
                historyList.removeAt(historyList.lastIndex)
            }
            val serialized = kotlinx.serialization.json.Json.encodeToString(
                kotlinx.serialization.builtins.ListSerializer(com.astral.ocr.data.OcrHistoryItem.serializer()),
                historyList
            )
            settingsRepository.updateOcrHistory(serialized)
        }
    }

    fun processSingle(contentResolver: ContentResolver, uri: Uri) {
        processingJob?.cancel()
        processingJob = viewModelScope.launch {
            performProcessing(contentResolver, listOf(uri))
        }
    }

    fun processBulk(contentResolver: ContentResolver, uris: List<Uri>) {
        if (uris.isEmpty()) return
        processingJob?.cancel()
        processingJob = viewModelScope.launch {
            performProcessing(contentResolver, uris)
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
        viewModelScope.launch {
            notifications.emit(message)
        }
    }

    private suspend fun performProcessing(contentResolver: ContentResolver, uris: List<Uri>) {
        mutableProcessing.value = true
        mutableProgress.value = "Menyiapkan gambar..."
        val newResults = mutableListOf<OcrResult>()
        val total = uris.size

        try {
            for ((index, uri) in uris.withIndex()) {
                coroutineContext.ensureActive()
                val start = System.currentTimeMillis()
                val result = geminiOcrService.extractSpeech(
                    contentResolver,
                    uri,
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
                    legendOutside = uiState.value.legendOutside,
                    batchSize = uiState.value.batchSize,
                    includeBubbleRound = uiState.value.includeBubbleRound,
                    includeBubbleSquare = uiState.value.includeBubbleSquare,
                    includeSFX = uiState.value.includeSFX,
                    includeOutside = uiState.value.includeOutside
                )
                result.fold(
                    onSuccess = { text ->
                        val duration = System.currentTimeMillis() - start
                        newResults.add(OcrResult(uri.toString(), text, duration))
                    },
                    onFailure = { ex ->
                        notifyError(ex)
                    }
                )
            }
            mutableResults.value = newResults
            if (newResults.isNotEmpty()) {
                val combinedText = newResults.joinToString(separator = "\n\n") { it.processedText }
                addHistoryItem(combinedText)
            }
        } finally {
            mutableProgress.value = null
            mutableProcessing.value = false
            processingJob = null
        }
    }
}

class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
