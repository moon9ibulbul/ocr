package com.astral.ocr.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.astral.ocr.MainViewModel
import com.astral.ocr.data.MIN_SEGMENT_HEIGHT

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: MainViewModel.UiState,
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onApiKeyChanged: (String) -> Unit,
    onModelChanged: (String) -> Unit,
    onApiProviderChanged: (String) -> Unit,
    onSliceEnabledChanged: (Boolean) -> Unit,
    onSliceHeightChanged: (Int) -> Unit,
    onCustomLegendChanged: (String) -> Unit,
    onLegendBubbleRoundChanged: (String) -> Unit,
    onLegendBubbleSquareChanged: (String) -> Unit,
    onLegendOutsideChanged: (String) -> Unit,
    onBatchSizeChanged: (Int) -> Unit,
    onIncludeBubbleRoundChanged: (Boolean) -> Unit,
    onIncludeBubbleSquareChanged: (Boolean) -> Unit,
    onIncludeSFXChanged: (Boolean) -> Unit,
    onIncludeOutsideChanged: (Boolean) -> Unit,
    onComicModeChanged: (String) -> Unit,
    onClearHistory: () -> Unit,
    onSaveHistoryItem: (String, String) -> Unit
) {
    val apiKeyState = remember(uiState.apiKey) { mutableStateOf(uiState.apiKey) }
    val modelState = remember(uiState.model) { mutableStateOf(uiState.model) }
    val apiProviderState = remember(uiState.apiProvider) { mutableStateOf(uiState.apiProvider) }
    val sliceEnabledState = remember(uiState.sliceEnabled) { mutableStateOf(uiState.sliceEnabled) }
    val sliceHeightState = remember(uiState.sliceHeight) { mutableStateOf(uiState.sliceHeight.toString()) }
    val customLegendState = remember(uiState.customLegend) { mutableStateOf(uiState.customLegend) }
    val legendBubbleRoundState = remember(uiState.legendBubbleRound) { mutableStateOf(uiState.legendBubbleRound) }
    val legendBubbleSquareState = remember(uiState.legendBubbleSquare) { mutableStateOf(uiState.legendBubbleSquare) }
    val legendOutsideState = remember(uiState.legendOutside) { mutableStateOf(uiState.legendOutside) }
    val batchSizeState = remember(uiState.batchSize) { mutableStateOf(uiState.batchSize.toString()) }
    val includeBubbleRoundState = remember(uiState.includeBubbleRound) { mutableStateOf(uiState.includeBubbleRound) }
    val includeBubbleSquareState = remember(uiState.includeBubbleSquare) { mutableStateOf(uiState.includeBubbleSquare) }
    val includeSFXState = remember(uiState.includeSFX) { mutableStateOf(uiState.includeSFX) }
    val includeOutsideState = remember(uiState.includeOutside) { mutableStateOf(uiState.includeOutside) }
    val comicModeState = remember(uiState.comicMode) { mutableStateOf(uiState.comicMode) }

    var apiKeyVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopAppBar(
            title = { Text("Pengaturan", style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }
            }
        )

        // 1. KONEKSI API CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Api, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Koneksi API", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("gemini" to "Gemini", "sumopod" to "Sumopod AI").forEach { (id, label) ->
                        Button(
                            onClick = { apiProviderState.value = id },
                            modifier = Modifier.weight(1f),
                            colors = if (apiProviderState.value == id) {
                                androidx.compose.material3.ButtonDefaults.buttonColors()
                            } else {
                                androidx.compose.material3.ButtonDefaults.filledTonalButtonColors()
                            }
                        ) {
                            Text(label)
                        }
                    }
                }

                OutlinedTextField(
                    value = apiKeyState.value,
                    onValueChange = { apiKeyState.value = it },
                    label = { Text(if (apiProviderState.value == "gemini") "API Key Gemini" else "Sumopod Token") },
                    visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                            Icon(
                                imageVector = if (apiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (apiKeyVisible) "Sembunyikan" else "Tampilkan"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = modelState.value,
                    onValueChange = { modelState.value = it },
                    label = { Text(if (apiProviderState.value == "gemini") "Model Gemini" else "Model Sumopod") },
                    placeholder = { Text(if (apiProviderState.value == "gemini") "gemini-3.1-flash-lite" else "gemini/gemini-3.1-flash-lite") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // 2. MODE KOMIK CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Book, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mode Komik", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("manhwa" to "Mode Manhwa", "manga" to "Mode Manga").forEach { (id, label) ->
                        Button(
                            onClick = { comicModeState.value = id },
                            modifier = Modifier.weight(1f),
                            colors = if (comicModeState.value == id) {
                                androidx.compose.material3.ButtonDefaults.buttonColors()
                            } else {
                                androidx.compose.material3.ButtonDefaults.filledTonalButtonColors()
                            }
                        ) {
                            Text(label)
                        }
                    }
                }
            }
        }

        // 3. SEGMENTASI GAMBAR CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Crop, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Segmentasi Gambar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Potong halaman panjang", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "Iris halaman tinggi menjadi segmen kecil sebelum dikirim.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = sliceEnabledState.value,
                        onCheckedChange = { sliceEnabledState.value = it }
                    )
                }

                if (sliceEnabledState.value) {
                    OutlinedTextField(
                        value = sliceHeightState.value,
                        onValueChange = { input ->
                            sliceHeightState.value = input.filter { it.isDigit() }
                        },
                        label = { Text("Tinggi Segmen (px)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = batchSizeState.value,
                    onValueChange = { input ->
                        batchSizeState.value = input.filter { it.isDigit() }
                    },
                    label = { Text("Batch Size (segmen per batch)") },
                    placeholder = { Text("1") },
                    supportingText = { Text("Default: 1") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }

        // 4. LEGENDA OUTPUT CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Label, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Legenda Output", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Text("Sertakan di Output:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Checkbox(
                            checked = includeBubbleRoundState.value,
                            onCheckedChange = { includeBubbleRoundState.value = it }
                        )
                        Text("Bubble Bulat", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Checkbox(
                            checked = includeBubbleSquareState.value,
                            onCheckedChange = { includeBubbleSquareState.value = it }
                        )
                        Text("Bubble Kotak", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Checkbox(
                            checked = includeSFXState.value,
                            onCheckedChange = { includeSFXState.value = it }
                        )
                        Text("SFX", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Checkbox(
                            checked = includeOutsideState.value,
                            onCheckedChange = { includeOutsideState.value = it }
                        )
                        Text("Luar Bubble", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = legendBubbleRoundState.value,
                        onValueChange = { legendBubbleRoundState.value = it },
                        label = { Text("Bubble Bulat") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = legendBubbleSquareState.value,
                        onValueChange = { legendBubbleSquareState.value = it },
                        label = { Text("Bubble Kotak") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customLegendState.value,
                        onValueChange = { customLegendState.value = it },
                        label = { Text("SFX") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = legendOutsideState.value,
                        onValueChange = { legendOutsideState.value = it },
                        label = { Text("Luar Bubble") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        Button(
            onClick = {
                onApiKeyChanged(apiKeyState.value)
                onModelChanged(modelState.value)
                onApiProviderChanged(apiProviderState.value)
                val parsedHeight = sliceHeightState.value.toIntOrNull()?.coerceAtLeast(MIN_SEGMENT_HEIGHT)
                    ?: uiState.sliceHeight
                onSliceEnabledChanged(sliceEnabledState.value)
                onSliceHeightChanged(parsedHeight)
                onCustomLegendChanged(customLegendState.value)
                onLegendBubbleRoundChanged(legendBubbleRoundState.value)
                onLegendBubbleSquareChanged(legendBubbleSquareState.value)
                onLegendOutsideChanged(legendOutsideState.value)
                val parsedBatchSize = batchSizeState.value.toIntOrNull()?.coerceAtLeast(1) ?: 1
                onBatchSizeChanged(parsedBatchSize)
                onIncludeBubbleRoundChanged(includeBubbleRoundState.value)
                onIncludeBubbleSquareChanged(includeBubbleSquareState.value)
                onIncludeSFXChanged(includeSFXState.value)
                onIncludeOutsideChanged(includeOutsideState.value)
                onComicModeChanged(comicModeState.value)
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Simpan Pengaturan")
        }

        // 5. RIWAYAT OCR SECTION
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Riwayat OCR",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (uiState.ocrHistory.isNotEmpty()) {
                        IconButton(onClick = onClearHistory) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus Riwayat",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                if (uiState.ocrHistory.isEmpty()) {
                    Text(
                        text = "Belum ada riwayat pemrosesan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                } else {
                    uiState.ocrHistory.forEach { historyItem ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = historyItem.timestamp,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = historyItem.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = {
                                    val sanitizedDate = historyItem.timestamp.replace(" ", "_").replace(":", "-")
                                    val filename = "ocr_history_$sanitizedDate.txt"
                                    onSaveHistoryItem(filename, historyItem.text)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = "Unduh Ulang",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
