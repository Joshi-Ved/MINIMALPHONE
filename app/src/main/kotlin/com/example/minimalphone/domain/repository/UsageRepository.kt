package com.example.minimalphone.domain.repository

import com.example.minimalphone.domain.model.FocusSettings
import com.example.minimalphone.domain.model.UsageSnapshot

interface UsageRepository {
    fun hasUsageAccessPermission(): Boolean
    fun openUsageAccessSettings()
    suspend fun queryTodayUsage(settings: FocusSettings): UsageSnapshot
}
