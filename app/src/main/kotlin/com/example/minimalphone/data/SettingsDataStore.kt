package com.example.minimalphone.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ─────────────────────────────────────────────────────────────────────────────
// 📘 BEGINNER CONCEPT: What is Flow?
// ─────────────────────────────────────────────────────────────────────────────
// Flow is like StateFlow but only EMITS when YOU ask it to — it's "cold."
//
// Comparison:
//   StateFlow:  Warm. Always has a current value. Multiple subscribers all see
//               the same current value when they subscribe.
//   Flow:       Cold. No current value. Each time you collect(), it runs from
//               scratch. Multiple subscribers get independent runs.
//
// In DataStore: we use Flow<T> because we don't need a "current" value, just
// a stream of events when data changes.
// ─────────────────────────────────────────────────────────────────────────────

// Singleton DataStore instance (shared across app)
private val Context.settingsDataStore by preferencesDataStore(name = "app_settings")

/**
 * Helper class to read/write settings to DataStore (Preferences format).
 */
class SettingsDataStore(private val context: Context) {

    companion object {
        private val DAILY_LIMIT_MINUTES = intPreferencesKey("daily_limit_minutes")
        private const val DEFAULT_DAILY_LIMIT = 120  // 2 hours
    }

    /**
     * Reads daily limit as a FLOW.
     * Each time the limit changes, this emits a new value.
     * Multiple subscribers each get independent streams.
     */
    fun getDailyLimitMinutesFlow(): Flow<Int> {
        return context.settingsDataStore.data.map { preferences ->
            preferences[DAILY_LIMIT_MINUTES] ?: DEFAULT_DAILY_LIMIT
        }
    }

    /**
     * Writes daily limit to DataStore.
     * This is a suspend function — it runs in a coroutine context.
     */
    suspend fun setDailyLimitMinutes(minutes: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[DAILY_LIMIT_MINUTES] = minutes
        }
    }
}
