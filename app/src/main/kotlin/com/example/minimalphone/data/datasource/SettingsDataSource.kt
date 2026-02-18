package com.example.minimalphone.data.datasource

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.minimalphone.domain.model.AppBlockMode
import com.example.minimalphone.domain.model.FocusSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsDataSource(context: Context) {

    private val dataStore = PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile("focuslite_settings")
    }

    private object Keys {
        val DAILY_LIMIT_MINUTES = intPreferencesKey("daily_limit_minutes")
        val LOCK_MODE_ENABLED = booleanPreferencesKey("lock_mode_enabled")
        val PREMIUM_ENABLED = booleanPreferencesKey("premium_enabled")
        val BLOCK_MODE = stringPreferencesKey("block_mode")
        val BLOCKED_PACKAGES = stringSetPreferencesKey("blocked_packages")
        val ALLOWED_PACKAGES = stringSetPreferencesKey("allowed_packages")
        val LAST_RESET_DATE = stringPreferencesKey("last_reset_date")
    }

    fun observeSettings(): Flow<FocusSettings> {
        return dataStore.data.map { preferences ->
            FocusSettings(
                dailyLimitMinutes = preferences[Keys.DAILY_LIMIT_MINUTES] ?: 120,
                lockModeEnabled = preferences[Keys.LOCK_MODE_ENABLED] ?: true,
                premiumEnabled = preferences[Keys.PREMIUM_ENABLED] ?: false,
                blockMode = runCatching {
                    AppBlockMode.valueOf(preferences[Keys.BLOCK_MODE] ?: AppBlockMode.BLOCK_SELECTED.name)
                }.getOrDefault(AppBlockMode.BLOCK_SELECTED),
                blockedPackages = preferences[Keys.BLOCKED_PACKAGES] ?: emptySet(),
                allowedPackages = preferences[Keys.ALLOWED_PACKAGES] ?: emptySet(),
                lastResetDate = preferences[Keys.LAST_RESET_DATE] ?: "",
            )
        }
    }

    suspend fun updateDailyLimit(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.DAILY_LIMIT_MINUTES] = minutes
        }
    }

    suspend fun setLockModeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.LOCK_MODE_ENABLED] = enabled
        }
    }

    suspend fun setPremiumEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.PREMIUM_ENABLED] = enabled
        }
    }

    suspend fun setBlockMode(mode: AppBlockMode) {
        dataStore.edit { preferences ->
            preferences[Keys.BLOCK_MODE] = mode.name
        }
    }

    suspend fun setBlockedPackages(packages: Set<String>) {
        dataStore.edit { preferences ->
            preferences[Keys.BLOCKED_PACKAGES] = packages
        }
    }

    suspend fun setAllowedPackages(packages: Set<String>) {
        dataStore.edit { preferences ->
            preferences[Keys.ALLOWED_PACKAGES] = packages
        }
    }

    suspend fun setDailyResetStamp(dateStamp: String) {
        dataStore.edit { preferences ->
            preferences[Keys.LAST_RESET_DATE] = dateStamp
        }
    }

    suspend fun clearDailyResetStamp() {
        dataStore.edit { preferences ->
            preferences[Keys.LAST_RESET_DATE] = ""
        }
    }
}
