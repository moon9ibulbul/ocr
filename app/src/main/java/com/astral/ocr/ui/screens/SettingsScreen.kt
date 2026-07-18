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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopAppBar(
            title = { Text("Pengaturan API") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali")
                }
            }
        )

        Text(
            text = "Pilih penyedia API dan masukkan konfigurasi yang diperlukan.",
            style = MaterialTheme.typography.bodyLarge
        )

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
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = modelState.value,
            onValueChange = { modelState.value = it },
            label = { Text(if (apiProviderState.value == "gemini") "Model Gemini" else "Model Sumopod") },
            placeholder = { Text(if (apiProviderState.value == "gemini") "gemini-2.0-flash" else "gemini/gemini-3.1-flash-lite-preview") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Potong halaman panjang menjadi segmen")
                Text(
                    text = "Aktifkan agar halaman tinggi di-slice sebelum dikirim ke Gemini.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = sliceEnabledState.value,
                onCheckedChange = { sliceEnabledState.value = it }
            )
        }

        OutlinedTextField(
            value = sliceHeightState.value,
            onValueChange = { input ->
                sliceHeightState.value = input.filter { it.isDigit() }
            },
            label = { Text("Tinggi segmen (px)") },
            supportingText = { Text("Digunakan saat memotong halaman panjang.") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = sliceEnabledState.value
        )

        OutlinedTextField(
            value = legendBubbleRoundState.value,
            onValueChange = { legendBubbleRoundState.value = it },
            label = { Text("Custom Legend (Bubble Bulat)") },
            placeholder = { Text("()") },
            supportingText = { Text("Default: ()") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = legendBubbleSquareState.value,
            onValueChange = { legendBubbleSquareState.value = it },
            label = { Text("Custom Legend (Bubble Kotak)") },
            placeholder = { Text("[]") },
            supportingText = { Text("Default: []") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = customLegendState.value,
            onValueChange = { customLegendState.value = it },
            label = { Text("Custom Legend (SFX)") },
            placeholder = { Text("//") },
            supportingText = { Text("Default: //") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = legendOutsideState.value,
            onValueChange = { legendOutsideState.value = it },
            label = { Text("Custom Legend (Luar Bubble)") },
            placeholder = { Text("''") },
            supportingText = { Text("Default: ''") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = {
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
            onBack()
        }) {
            Text("Simpan")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // HISTORY SECTION
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
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
