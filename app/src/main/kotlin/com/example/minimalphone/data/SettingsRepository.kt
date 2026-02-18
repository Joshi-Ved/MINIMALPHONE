package com.example.minimalphone.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────────────────────
// 📘 BEGINNER CONCEPT: Why Repository Pattern?
// ─────────────────────────────────────────────────────────────────────────────
// The Repository pattern is a layer between the ViewModel and the actual
// data sources (DataStore, UsageStatsManager, database, API, etc.).
//
// Benefits:
//   1. ABSTRACTION: ViewModel doesn't care if data comes from DataStore, DB,
//      or a REST API — it just calls repository methods.
//   2. TESTABILITY: You can swap the real repository with a fake one in tests.
//   3. MAINTAINABILITY: If data source changes, only the repository changes,
//      not the ViewModel.
//
// Analogy:
//   ViewModel = Customer at a restaurant
//   Repository = Waiter
//   Data sources = Kitchen
// The customer (ViewModel) asks the waiter (Repository) for food. The waiter
// handles how/where to get it. The customer doesn't care about kitchen details.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Repository for user settings.
 * Wraps SettingsDataStore and exposes settings as Flows.
 */
class SettingsRepository(context: Context) {

    private val settingsDataStore = SettingsDataStore(context)

    /**
     * Daily limit in minutes as a Flow.
     * Emits whenever the limit is changed.
     */
    fun getDailyLimitMinutesFlow(): Flow<Int> {
        return settingsDataStore.getDailyLimitMinutesFlow()
    }

    /**
     * Updates the daily limit.
     */
    suspend fun setDailyLimitMinutes(minutes: Int) {
        settingsDataStore.setDailyLimitMinutes(minutes)
    }
}

/**
 * Repository for usage statistics.
 * Wraps UsageStatsHelper and exposes usage as a Flow.
 */
class UsageRepository(context: Context) {

    private val usageStatsHelper = UsageStatsHelper(context)

    /**
     * True if the app has Usage Access permission.
     * (Synchronous — permissions don't change during app runtime usually.)
     */
    fun hasUsageAccessPermission(): Boolean {
        return usageStatsHelper.hasUsageAccessPermission()
    }

    /**
     * Opens the Usage Access settings screen.
     */
    fun openUsageAccessSettings() {
        usageStatsHelper.openUsageAccessSettings()
    }

    /**
     * Gets today's foreground usage in minutes.
     * (Synchronous — called from IO dispatcher, not blocking main thread.)
     */
    fun getTodayForegroundUsageMinutes(): Int {
        return usageStatsHelper.getTodayForegroundUsageMinutes()
    }
}
