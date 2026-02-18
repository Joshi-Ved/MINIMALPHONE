package com.example.minimalphone.domain.model

data class FocusSettings(
    val dailyLimitMinutes: Int = 120,
    val lockModeEnabled: Boolean = true,
    val premiumEnabled: Boolean = false,
    val blockMode: AppBlockMode = AppBlockMode.BLOCK_SELECTED,
    val blockedPackages: Set<String> = emptySet(),
    val allowedPackages: Set<String> = emptySet(),
    val lastResetDate: String = "",
)
