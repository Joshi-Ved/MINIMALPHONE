package com.example.minimalphone.ui.state

sealed interface DashboardUiState {
    data object Loading : DashboardUiState

    data class PermissionRequired(
        val helpText: String,
    ) : DashboardUiState

    data class Ready(
        val usedMinutes: Int,
        val dailyLimitMinutes: Int,
        val remainingMinutes: Int,
        val isOverLimit: Boolean,
        val lockModeEnabled: Boolean,
    ) : DashboardUiState
}

fun formatMinutes(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}
