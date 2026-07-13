package com.astral.ocr.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class SettingsData(
    val apiKey: String = "",
    val model: String = "gemini-2.0-flash",
    val apiProvider: String = "gemini",
    val sliceEnabled: Boolean = true,
    val sliceHeight: Int = 1400,
    val customLegend: String = "//",
    val legendBubbleRound: String = "()",
    val legendBubbleSquare: String = "[]",
    val legendOutside: String = "''"
)

class SettingsRepository {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val settingsFile = File(System.getProperty("user.home") + File.separator + ".astralocr", "settings.json")

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<SettingsData> = _settings.asStateFlow()

    val apiKey: Flow<String> = _settings.map { it.apiKey }
    val model: Flow<String> = _settings.map { it.model }
    val apiProvider: Flow<String> = _settings.map { it.apiProvider }
    val sliceEnabled: Flow<Boolean> = _settings.map { it.sliceEnabled }
    val sliceHeight: Flow<Int> = _settings.map { it.sliceHeight }
    val customLegend: Flow<String> = _settings.map { it.customLegend }
    val legendBubbleRound: Flow<String> = _settings.map { it.legendBubbleRound }
    val legendBubbleSquare: Flow<String> = _settings.map { it.legendBubbleSquare }
    val legendOutside: Flow<String> = _settings.map { it.legendOutside }

    private fun loadSettings(): SettingsData {
        return try {
            if (settingsFile.exists()) {
                val content = settingsFile.readText()
                json.decodeFromString<SettingsData>(content)
            } else {
                SettingsData()
            }
        } catch (e: Exception) {
            SettingsData()
        }
    }

    private fun saveSettings(data: SettingsData) {
        try {
            settingsFile.parentFile?.mkdirs()
            settingsFile.writeText(json.encodeToString(SettingsData.serializer(), data))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateApiKey(value: String) {
        val newSettings = _settings.value.copy(apiKey = value)
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    suspend fun updateModel(value: String) {
        val newSettings = _settings.value.copy(model = value)
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    suspend fun updateApiProvider(value: String) {
        val newSettings = _settings.value.copy(apiProvider = value)
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    suspend fun updateSliceEnabled(enabled: Boolean) {
        val newSettings = _settings.value.copy(sliceEnabled = enabled)
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    suspend fun updateSliceHeight(value: Int) {
        val newSettings = _settings.value.copy(sliceHeight = value)
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    suspend fun updateCustomLegend(value: String) {
        val newSettings = _settings.value.copy(customLegend = value)
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    suspend fun updateLegendBubbleRound(value: String) {
        val newSettings = _settings.value.copy(legendBubbleRound = value)
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    suspend fun updateLegendBubbleSquare(value: String) {
        val newSettings = _settings.value.copy(legendBubbleSquare = value)
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    suspend fun updateLegendOutside(value: String) {
        val newSettings = _settings.value.copy(legendOutside = value)
        _settings.value = newSettings
        saveSettings(newSettings)
    }
}
