package com.example.minimalphone.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.minimalphone.data.SettingsRepository
import com.example.minimalphone.data.UsageRepository
import com.example.minimalphone.data.UsageState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ─────────────────────────────────────────────────────────────────────────────
// 📘 BEGINNER CONCEPT: What is a ViewModel?
// ─────────────────────────────────────────────────────────────────────────────
// A ViewModel is a special class that SURVIVES screen rotations.
//
// Normally, when you rotate your phone Android DESTROYS the Activity and
// recreates it.  Any variables inside the Activity would be lost.
//
// A ViewModel lives OUTSIDE the Activity's lifecycle, so the data it holds
// (like our usage minutes) stays intact even after a rotation.
//
// Rule of thumb:
//   • Put UI data & logic in the ViewModel.
//   • Put UI rendering in Compose (or the Activity).
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// 📘 BEGINNER CONCEPT: What is StateFlow?
// ─────────────────────────────────────────────────────────────────────────────
// StateFlow is like a "live variable" that anyone can OBSERVE.
//
// • MutableStateFlow  → the ViewModel can WRITE to it  (private).
// • StateFlow         → the UI can only READ from it   (public).
//
// Whenever the value inside the StateFlow changes, every observer (our
// Compose screen) is automatically notified and updates itself.
//
// Flow of data:
//   ViewModel fetches usage in background
//     → ViewModel updates MutableStateFlow
//       → Compose sees the new value
//         → UI redraws (this is called "recomposition")
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// 📘 BEGINNER CONCEPT: What is Flow.combine()?
// ─────────────────────────────────────────────────────────────────────────────
// combine() merges multiple streams (Flows) into ONE.
//
// Example:
//   val usageFlow = repository.getUsageNow()         // emits: 30, 40, 50, ...
//   val limitFlow  = repository.getDailyLimit()      // emits: 120, 150, ...
//
//   combine(usageFlow, limitFlow) { usage, limit ->
//       DashboardState(used = usage, limit = limit)
//   }
//
// Result: each time either flow emits, the lambda runs and produces a new
// DashboardState. This keeps UI in sync with BOTH data sources automatically.
//
// Difference Flow vs StateFlow:
//   Flow:       No current value. Cold (lazy). Each subscriber starts fresh.
//   StateFlow:  Always has a current value. Warm. All subscribers see same value.
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// 📘 BEGINNER CONCEPT: Repository Pattern
// ─────────────────────────────────────────────────────────────────────────────
// Repository is a layer that abstracts WHERE data comes from.
// ViewModel doesn't care if data comes from DataStore, database, or API.
// It just calls repository methods.
//
// This makes code testable (swap real repo with fake one), maintainable,
// and flexible for future changes.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ViewModel for the Dashboard screen.
 *
 * Combines real usage data + user's daily limit preference using repositories.
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application.applicationContext)
    private val usageRepository = UsageRepository(application.applicationContext)

    // ── Internal mutable state (only this class can change it) ───────────
    private val _uiState = MutableStateFlow(UsageState())

    // ── Public read-only state (the UI observes this) ────────────────────
    // asStateFlow() returns a read-only view so the UI cannot accidentally
    // write to it — keeping the data flow ONE-DIRECTIONAL (ViewModel → UI).
    val uiState: StateFlow<UsageState> = _uiState.asStateFlow()

    init {
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (isActive) {
                refreshUsageAndSettings()
                delay(30_000L)
            }
        }
    }

    fun refreshUsageAndSettings() {
        viewModelScope.launch {
            val hasPermission = usageRepository.hasUsageAccessPermission()

            if (!hasPermission) {
                _uiState.update { current ->
                    current.copy(
                        hasUsagePermission = false,
                        isLoading = false,
                        usedMinutes = 0,
                    )
                }
                return@launch
            }

            _uiState.update { current -> current.copy(hasUsagePermission = true, isLoading = true) }

            val usageMinutes = withContext(Dispatchers.IO) {
                usageRepository.getTodayForegroundUsageMinutes()
            }

            // Collect daily limit from DataStore via repository
            settingsRepository.getDailyLimitMinutesFlow().collect { dailyLimit ->
                _uiState.update { current ->
                    current.copy(
                        hasUsagePermission = true,
                        isLoading = false,
                        usedMinutes = usageMinutes,
                        dailyLimitMinutes = dailyLimit,
                    )
                }
            }
        }
    }

    fun openUsageAccessSettings() {
        usageRepository.openUsageAccessSettings()
    }

    fun updateDailyLimit(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setDailyLimitMinutes(minutes)
            refreshUsageAndSettings()
        }
    }
}
