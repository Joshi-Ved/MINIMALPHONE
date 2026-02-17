package com.example.minimalphone.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minimalphone.data.UsageStatsHelper
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
// 📘 BEGINNER CONCEPT: What is a Coroutine?
// ─────────────────────────────────────────────────────────────────────────────
// A coroutine is a lightweight task that can pause and resume without blocking
// the whole thread.  We use coroutines to do async work in a clean way.
//
// 📘 BEGINNER CONCEPT: What is Dispatchers.IO?
// Dispatchers.IO is a coroutine dispatcher optimized for disk/network/system
// operations. Querying UsageStatsManager can be heavy, so we run it on IO.
//
// 📘 BEGINNER CONCEPT: Why use viewModelScope?
// viewModelScope is tied to the ViewModel lifecycle. When ViewModel is cleared,
// its coroutines are automatically cancelled (no leaks, no wasted work).
//
// 📘 BEGINNER CONCEPT: Why avoid heavy work on Main thread?
// The main thread draws UI and handles touch input. Heavy work there causes
// jank (skipped frames). Running heavy work on IO keeps UI smooth.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ViewModel for the Dashboard screen.
 *
 * Holds the current [UsageState] and exposes actions the UI can trigger.
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val usageStatsHelper = UsageStatsHelper(application.applicationContext)
    private val refreshIntervalMs = 30_000L

    // ── Internal mutable state (only this class can change it) ───────────
    private val _uiState = MutableStateFlow(UsageState())

    // ── Public read-only state (the UI observes this) ────────────────────
    // asStateFlow() returns a read-only view so the UI cannot accidentally
    // write to it — keeping the data flow ONE-DIRECTIONAL (ViewModel → UI).
    val uiState: StateFlow<UsageState> = _uiState.asStateFlow()

    init {
        startUsageAutoRefresh()
    }

    private fun startUsageAutoRefresh() {
        viewModelScope.launch {
            while (isActive) {
                refreshUsageNow()
                delay(refreshIntervalMs)
            }
        }
    }

    fun refreshUsageNow() {
        viewModelScope.launch {
            val hasPermission = usageStatsHelper.hasUsageAccessPermission()

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
                usageStatsHelper.getTodayForegroundUsageMinutes()
            }

            _uiState.update { current ->
                current.copy(
                    hasUsagePermission = true,
                    isLoading = false,
                    usedMinutes = usageMinutes,
                )
            }
        }
    }
}
