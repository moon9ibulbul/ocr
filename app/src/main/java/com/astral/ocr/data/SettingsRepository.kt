package com.astral.ocr.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.astral.ocr.data.DEFAULT_SEGMENT_HEIGHT
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "astral_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val apiKey = stringPreferencesKey("api_key")
        val model = stringPreferencesKey("model")
        val apiProvider = stringPreferencesKey("api_provider")
        val sliceEnabled = booleanPreferencesKey("slice_enabled")
        val sliceHeight = intPreferencesKey("slice_height")
        val customLegend = stringPreferencesKey("custom_legend")
        val legendBubbleRound = stringPreferencesKey("legend_bubble_round")
        val legendBubbleSquare = stringPreferencesKey("legend_bubble_square")
        val legendOutside = stringPreferencesKey("legend_outside")
        val ocrHistory = stringPreferencesKey("ocr_history")
        val batchSize = intPreferencesKey("batch_size")
        val includeBubbleRound = booleanPreferencesKey("include_bubble_round")
        val includeBubbleSquare = booleanPreferencesKey("include_bubble_square")
        val includeSFX = booleanPreferencesKey("include_sfx")
        val includeOutside = booleanPreferencesKey("include_outside")
    }

    val apiKey: Flow<String> = context.dataStore.data.map { it[Keys.apiKey].orEmpty() }
    val model: Flow<String> = context.dataStore.data.map { it[Keys.model].orEmpty() }
    val apiProvider: Flow<String> = context.dataStore.data.map { it[Keys.apiProvider] ?: "gemini" }
    val sliceEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.sliceEnabled] ?: true }
    val sliceHeight: Flow<Int> = context.dataStore.data.map { it[Keys.sliceHeight] ?: DEFAULT_SEGMENT_HEIGHT }
    val customLegend: Flow<String> = context.dataStore.data.map { it[Keys.customLegend].orEmpty().ifBlank { "//" } }
    val legendBubbleRound: Flow<String> = context.dataStore.data.map { it[Keys.legendBubbleRound].orEmpty().ifBlank { "()" } }
    val legendBubbleSquare: Flow<String> = context.dataStore.data.map { it[Keys.legendBubbleSquare].orEmpty().ifBlank { "[]" } }
    val legendOutside: Flow<String> = context.dataStore.data.map { it[Keys.legendOutside].orEmpty().ifBlank { "''" } }
    val ocrHistory: Flow<String> = context.dataStore.data.map { it[Keys.ocrHistory].orEmpty().ifBlank { "[]" } }
    val batchSize: Flow<Int> = context.dataStore.data.map { it[Keys.batchSize] ?: 5 }
    val includeBubbleRound: Flow<Boolean> = context.dataStore.data.map { it[Keys.includeBubbleRound] ?: true }
    val includeBubbleSquare: Flow<Boolean> = context.dataStore.data.map { it[Keys.includeBubbleSquare] ?: true }
    val includeSFX: Flow<Boolean> = context.dataStore.data.map { it[Keys.includeSFX] ?: true }
    val includeOutside: Flow<Boolean> = context.dataStore.data.map { it[Keys.includeOutside] ?: true }

    suspend fun updateBatchSize(value: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.batchSize] = value
        }
    }

    suspend fun updateIncludeBubbleRound(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.includeBubbleRound] = value
        }
    }

    suspend fun updateIncludeBubbleSquare(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.includeBubbleSquare] = value
        }
    }

    suspend fun updateIncludeSFX(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.includeSFX] = value
        }
    }

    suspend fun updateIncludeOutside(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.includeOutside] = value
        }
    }

    suspend fun updateApiKey(value: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.apiKey] = value
        }
    }

    suspend fun updateModel(value: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.model] = value
        }
    }

    suspend fun updateApiProvider(value: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.apiProvider] = value
        }
    }

    suspend fun updateSliceEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.sliceEnabled] = enabled
        }
    }

    suspend fun updateSliceHeight(value: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.sliceHeight] = value
        }
    }

    suspend fun updateCustomLegend(value: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.customLegend] = value
        }
    }

    suspend fun updateLegendBubbleRound(value: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.legendBubbleRound] = value
        }
    }

    suspend fun updateLegendBubbleSquare(value: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.legendBubbleSquare] = value
        }
    }

    suspend fun updateLegendOutside(value: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.legendOutside] = value
        }
    }

    suspend fun updateOcrHistory(value: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ocrHistory] = value
        }
    }
}
