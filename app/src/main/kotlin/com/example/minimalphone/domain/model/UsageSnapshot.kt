package com.example.minimalphone.domain.model

data class UsageSnapshot(
    val totalForegroundMillis: Long,
    val usageByPackage: Map<String, Long>,
) {
    val totalForegroundMinutes: Int
        get() = (totalForegroundMillis / 60_000L).toInt()
}
