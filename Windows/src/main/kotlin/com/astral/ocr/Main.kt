package com.astral.ocr

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.astral.ocr.data.OcrResult
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// Color Scheme matching AstralOCR theme
val MidnightBlue = Color(0xFF0B0B26)
val CosmicPurple = Color(0xFF8A2BE2)
val StellarPink = Color(0xFFFF007F)
val AuroraCyan = Color(0xFF00F0FF)

private val DarkColorScheme = darkColorScheme(
    primary = AuroraCyan,
    secondary = StellarPink,
    tertiary = CosmicPurple,
    background = MidnightBlue,
    surface = Color(0xFF14143C)
)

fun Modifier.gradientBackground(): Modifier = drawBehind {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                MidnightBlue,
                CosmicPurple,
                StellarPink.copy(alpha = 0.8f)
            )
        ),
        size = size
    )
}

enum class Screen {
    Home, Settings
}

fun main() = application {
    val windowState = rememberWindowState(width = 1150.dp, height = 800.dp)
    Window(
        onCloseRequest = ::exitApplication,
        title = "AstralOCR - Windows Desktop",
        state = windowState
    ) {
        val scope = rememberCoroutineScope()
        val viewModel = remember { MainViewModel(scope) }
        val uiState by viewModel.uiState.collectAsState()
        var currentScreen by remember { mutableStateOf(Screen.Home) }

        // Alert message state
        var notificationText by remember { mutableStateOf<String?>(null) }
        var showSavedNotification by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            viewModel.notifications.collectLatest { message ->
                message?.let {
                    notificationText = it
                }
            }
        }

        LaunchedEffect(uiState.lastSavedPath) {
            uiState.lastSavedPath?.let {
                showSavedNotification = true
                viewModel.setLastSavedPath(null)
            }
        }

        MaterialTheme(colorScheme = DarkColorScheme) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(modifier = Modifier.fillMaxSize().gradientBackground()) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // SIDEBAR NAVIGATION
                        Sidebar(
                            currentScreen = currentScreen,
                            onScreenSelected = { currentScreen = it }
                        )

                        VerticalDivider(
                            color = Color.White.copy(alpha = 0.1f),
                            thickness = 1.dp
                        )

                        // MAIN CONTENT
                        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                            when (currentScreen) {
                                Screen.Home -> HomeScreen(
                                    uiState = uiState,
                                    viewModel = viewModel
                                )
                                Screen.Settings -> SettingsScreen(
                                    uiState = uiState,
                                    viewModel = viewModel
                                )
                            }
                        }
                    }

                    // Dialog Notification / Snackbar
                    notificationText?.let { text ->
                        AlertDialog(
                            onDismissRequest = { notificationText = null },
                            title = { Text("Error") },
                            text = { Text(text) },
                            confirmButton = {
                                TextButton(onClick = { notificationText = null }) {
                                    Text("OK")
                                }
                            }
                        )
                    }

                    if (showSavedNotification) {
                        AlertDialog(
                            onDismissRequest = { showSavedNotification = false },
                            title = { Text("Berhasil") },
                            text = { Text("Hasil berhasil disimpan ke dalam berkas teks.") },
                            confirmButton = {
                                TextButton(onClick = { showSavedNotification = false }) {
                                    Text("OK")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Sidebar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(240.dp)
            .background(MidnightBlue.copy(alpha = 0.7f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Analytics,
                contentDescription = null,
                tint = AuroraCyan,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "AstralOCR",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Items
        SidebarItem(
            label = "Beranda",
            icon = Icons.Default.Home,
            selected = currentScreen == Screen.Home,
            onClick = { onScreenSelected(Screen.Home) }
        )

        SidebarItem(
            label = "Pengaturan",
            icon = Icons.Default.Settings,
            selected = currentScreen == Screen.Settings,
            onClick = { onScreenSelected(Screen.Settings) }
        )
    }
}

@Composable
fun SidebarItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background = if (selected) CosmicPurple.copy(alpha = 0.3f) else Color.Transparent
    val tint = if (selected) AuroraCyan else Color.White.copy(alpha = 0.7f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, shape = RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.7f),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun HomeScreen(
    uiState: MainViewModel.UiState,
    viewModel: MainViewModel
) {
    Row(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // LEFT COLUMN: CONTROLS & PICKERS
        Card(
            modifier = Modifier.fillMaxHeight().width(350.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14143C).copy(alpha = 0.85f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Panel Kontrol",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AuroraCyan
                )

                Text(
                    text = "Ekstrak bubble speech, SFX, dan teks luar dengan sekali sentuh.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Single / Bulk selection Mode
                Text(
                    text = "Mode Pemrosesan:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val bulkOptions = listOf("Single Image", "Bulk Images")
                    bulkOptions.forEachIndexed { index, label ->
                        val isSelected = (index == 1) == uiState.bulkMode
                        OutlinedButton(
                            onClick = { viewModel.toggleBulkMode(index == 1) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) CosmicPurple.copy(alpha = 0.4f) else Color.Transparent,
                                contentColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(
                                    colors = if (isSelected) listOf(AuroraCyan, StellarPink) else listOf(Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.2f))
                                )
                            )
                        ) {
                            Text(text = label)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action File Pick Button
                Button(
                    onClick = {
                        if (uiState.bulkMode) {
                            val selectedFiles = pickMultipleFiles()
                            if (selectedFiles.isNotEmpty()) {
                                viewModel.processBulk(selectedFiles)
                            }
                        } else {
                            val selectedFile = pickSingleFile()
                            if (selectedFile != null) {
                                viewModel.processSingle(selectedFile)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicPurple)
                ) {
                    Icon(
                        imageVector = if (uiState.bulkMode) Icons.Default.LibraryAdd else Icons.Default.Image,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (uiState.bulkMode) "Pilih Banyak Gambar" else "Pilih Gambar")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Processing status indicator
                if (uiState.isProcessing) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = AuroraCyan)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.progressMessage ?: "Memproses dengan AI...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.cancelProcessing() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Batal")
                        }
                    }
                }
            }
        }

        // RIGHT COLUMN: RESULTS PANE
        Column(
            modifier = Modifier.fillMaxSize().weight(1.0f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Results Top Toolbar Actions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF14143C).copy(alpha = 0.7f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hasil OCR (${uiState.results.size} gambar)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (uiState.results.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val combinedText = remember(uiState.results, uiState.bulkMode) {
                                if (uiState.results.isEmpty()) {
                                    ""
                                } else if (uiState.bulkMode) {
                                    uiState.results.mapIndexed { index, result ->
                                        buildString {
                                            append("Panel : ")
                                            append(index + 1)
                                            append('\n')
                                            append(result.processedText.trimEnd())
                                        }
                                    }.joinToString(separator = "\n\n")
                                } else {
                                    uiState.results.joinToString(separator = "\n\n") { result ->
                                        result.processedText
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    copyToClipboard(combinedText)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Salin Semua")
                            }

                            Button(
                                onClick = {
                                    val filename = if (uiState.bulkMode) "astral_bulk_result.txt" else "astral_result.txt"
                                    val saveFile = saveFileDialog(defaultFilename = filename)
                                    if (saveFile != null) {
                                        try {
                                            saveFile.writeText(combinedText)
                                            viewModel.setLastSavedPath(saveFile.absolutePath)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicPurple)
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Simpan")
                            }

                            IconButton(onClick = { viewModel.clearResults() }) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = "Bersihkan", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Results List Scrollable Pane
            if (uiState.results.isNotEmpty()) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    uiState.results.forEach { result ->
                        ResultCard(result)
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFF14143C).copy(alpha = 0.4f), shape = RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterFrames,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "Belum ada hasil pemrosesan.",
                            color = Color.White.copy(alpha = 0.4f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResultCard(result: OcrResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14143C).copy(alpha = 0.85f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ListAlt, contentDescription = null, tint = AuroraCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = result.imageUri,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "${result.durationMillis} ms",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StellarPink,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SelectionContainer {
                Text(
                    text = result.processedText.ifBlank { "Tidak ada teks yang terdeteksi" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (result.processedText.isBlank()) Color.White.copy(alpha = 0.4f) else Color.White
                )
            }
        }
    }
}

// Emulate simple Selection Container as custom selection isn't strictly required but nice to have.
// We can easily use standard Box or text selection wrappers if needed.
@Composable
fun SelectionContainer(content: @Composable () -> Unit) {
    Box {
        content()
    }
}

@Composable
fun SettingsScreen(
    uiState: MainViewModel.UiState,
    viewModel: MainViewModel
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Pengaturan Konfigurasi",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AuroraCyan
        )

        // API CONFIGURATION
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14143C).copy(alpha = 0.85f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Koneksi API",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = StellarPink
                )

                // Provider selection
                Text("Provider API:", fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val providers = listOf("gemini" to "Gemini API", "sumopod" to "Sumopod AI")
                    providers.forEach { (id, label) ->
                        val isSelected = uiState.apiProvider == id
                        OutlinedButton(
                            onClick = { viewModel.updateApiProvider(id) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) CosmicPurple.copy(alpha = 0.4f) else Color.Transparent,
                                contentColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(
                                    colors = if (isSelected) listOf(AuroraCyan, StellarPink) else listOf(Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.2f))
                                )
                            )
                        ) {
                            Text(text = label)
                        }
                    }
                }

                // API Key Text Field
                OutlinedTextField(
                    value = uiState.apiKey,
                    onValueChange = viewModel::updateApiKey,
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuroraCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )

                // Model Text Field
                OutlinedTextField(
                    value = uiState.model,
                    onValueChange = viewModel::updateModel,
                    label = { Text("Nama Model") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuroraCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )
                Text(
                    text = "Bawaan: gemini-2.0-flash. Pastikan model yang dimasukkan mendukung input gambar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }

        // IMAGE SLICING CONFIGURATION
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14143C).copy(alpha = 0.85f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Segmentasi Gambar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = StellarPink
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Aktifkan Pemotongan Gambar Vertikal", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Memotong halaman webtoon yang panjang menjadi beberapa segmen agar resolusi tetap terjaga dan tidak terpotong batasan API.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = uiState.sliceEnabled,
                        onCheckedChange = viewModel::updateSliceEnabled,
                        colors = SwitchDefaults.colors(checkedThumbColor = AuroraCyan, checkedTrackColor = CosmicPurple)
                    )
                }

                if (uiState.sliceEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tinggi Target Segmen (px):", fontWeight = FontWeight.SemiBold)
                            Text("${uiState.sliceHeight} px", color = AuroraCyan, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = uiState.sliceHeight.toFloat(),
                            onValueChange = { viewModel.updateSliceHeight(it.toInt()) },
                            valueRange = 400f..3000f,
                            colors = SliderDefaults.colors(
                                thumbColor = AuroraCyan,
                                activeTrackColor = CosmicPurple,
                                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }

        // OUTPUT LEGEND CONFIGURATION
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14143C).copy(alpha = 0.85f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Legenda Output",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = StellarPink
                )

                Text(
                    text = "Ganti penanda legenda tipe bubble untuk hasil normalisasi teks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )

                val legends = listOf(
                    Triple("Bubble Bulat", uiState.legendBubbleRound, viewModel::updateLegendBubbleRound),
                    Triple("Bubble Kotak", uiState.legendBubbleSquare, viewModel::updateLegendBubbleSquare),
                    Triple("Efek Suara (SFX)", uiState.customLegend, viewModel::updateCustomLegend),
                    Triple("Teks Luar Bubble", uiState.legendOutside, viewModel::updateLegendOutside)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    legends.take(2).forEach { (label, value, onUpdate) ->
                        OutlinedTextField(
                            value = value,
                            onValueChange = onUpdate,
                            label = { Text(label) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AuroraCyan,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    legends.drop(2).forEach { (label, value, onUpdate) ->
                        OutlinedTextField(
                            value = value,
                            onValueChange = onUpdate,
                            label = { Text(label) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AuroraCyan,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        }
    }
}

// Clipboard copying utility using Java AWT
fun copyToClipboard(text: String) {
    try {
        val selection = StringSelection(text)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// Native AWT Windows File Dialog implementations for flawless Windows support
fun pickSingleFile(): File? {
    val dialog = FileDialog(null as Frame?, "Pilih Gambar", FileDialog.LOAD).apply {
        setFilenameFilter { _, name ->
            val lower = name.lowercase()
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")
        }
        isVisible = true
    }
    val file = dialog.file ?: return null
    return File(dialog.directory, file)
}

fun pickMultipleFiles(): List<File> {
    val dialog = FileDialog(null as Frame?, "Pilih Banyak Gambar", FileDialog.LOAD).apply {
        isMultipleMode = true
        setFilenameFilter { _, name ->
            val lower = name.lowercase()
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")
        }
        isVisible = true
    }
    return dialog.files.toList()
}

fun saveFileDialog(defaultFilename: String): File? {
    val dialog = FileDialog(null as Frame?, "Simpan Hasil Teks", FileDialog.SAVE).apply {
        file = defaultFilename
        isVisible = true
    }
    val file = dialog.file ?: return null
    return File(dialog.directory, file)
}
