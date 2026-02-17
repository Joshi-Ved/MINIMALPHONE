package com.example.minimalphone.viewmodel

import androidx.lifecycle.ViewModel
import com.example.minimalphone.data.UsageState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
//   User clicks button
//     → ViewModel updates MutableStateFlow
//       → Compose sees the new value
//         → UI redraws (this is called "recomposition")
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ViewModel for the Dashboard screen.
 *
 * Holds the current [UsageState] and exposes actions the UI can trigger.
 */
class DashboardViewModel : ViewModel() {

    // ── Internal mutable state (only this class can change it) ───────────
    private val _uiState = MutableStateFlow(UsageState())

    // ── Public read-only state (the UI observes this) ────────────────────
    // asStateFlow() returns a read-only view so the UI cannot accidentally
    // write to it — keeping the data flow ONE-DIRECTIONAL (ViewModel → UI).
    val uiState: StateFlow<UsageState> = _uiState.asStateFlow()

    // ── Actions ──────────────────────────────────────────────────────────

    /**
     * Adds 10 minutes of "usage".
     *
     * We use [MutableStateFlow.update] which gives us the CURRENT value,
     * lets us create a NEW copy with the change, and sets it atomically
     * (thread-safe).
     */
    fun increaseUsage() {
        _uiState.update { current ->
            // .copy() creates a new UsageState with only the changed field.
            // The old object is NOT modified — this is called "immutability".
            current.copy(usedMinutes = current.usedMinutes + 10)
        }
    }

    /**
     * Resets usage back to 0 minutes.
     */
    fun resetUsage() {
        _uiState.update { current ->
            current.copy(usedMinutes = 0)
        }
    }
}
