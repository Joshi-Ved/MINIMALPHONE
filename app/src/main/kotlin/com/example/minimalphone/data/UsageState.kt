package com.example.minimalphone.data

// ─────────────────────────────────────────────────────────────────────────────
// 📘 BEGINNER CONCEPT: Data Classes
// ─────────────────────────────────────────────────────────────────────────────
// A "data class" in Kotlin is a simple class whose main purpose is to HOLD DATA.
// Kotlin automatically generates equals(), hashCode(), toString(), and copy()
// for you — so you don't have to write boilerplate code.
//
// Think of it like a labeled container:
//   UsageState holds two numbers — how many minutes were used, and the daily limit.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Represents the current screen-time state shown on the dashboard.
 *
 * @param usedMinutes  How many minutes the user has used today.
 * @param dailyLimitMinutes  The maximum allowed screen time per day.
 * @param hasUsagePermission Whether Usage Access permission is granted.
 * @param isLoading Whether usage data is currently being loaded in background.
 */
data class UsageState(
    val usedMinutes: Int = 0,
    val dailyLimitMinutes: Int = 120,
    val hasUsagePermission: Boolean = false,
    val isLoading: Boolean = true
) {
    // ── Computed properties ──────────────────────────────────────────────
    // These are NOT stored — they are calculated every time you read them.

    /** Minutes still available before hitting the daily limit. */
    val remainingMinutes: Int
        get() = (dailyLimitMinutes - usedMinutes).coerceAtLeast(0)
    // coerceAtLeast(0) ensures we never show negative remaining time.

    /** Pretty-printed string like "1h 20m". */
    val usedFormatted: String
        get() = formatMinutes(usedMinutes)

    /** Pretty-printed remaining time. */
    val remainingFormatted: String
        get() = formatMinutes(remainingMinutes)

    /** True when the user has exceeded the daily limit. */
    val isOverLimit: Boolean
        get() = usedMinutes >= dailyLimitMinutes

    /** Guidance text shown when usage access is not granted yet. */
    val permissionHelpText: String
        get() = "Enable Usage Access: Settings > Apps > Special app access > Usage access > FocusLite"
}

// ─────────────────────────────────────────────────────────────────────────────
// Helper: turn raw minutes into a human-readable "Xh Ym" string.
// ─────────────────────────────────────────────────────────────────────────────
private fun formatMinutes(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val mins  = totalMinutes % 60
    return when {
        hours > 0 && mins > 0 -> "${hours}h ${mins}m"
        hours > 0             -> "${hours}h"
        else                  -> "${mins}m"
    }
}
