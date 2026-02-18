package com.example.minimalphone.data.repository

import com.example.minimalphone.data.datasource.InstalledAppsDataSource
import com.example.minimalphone.data.datasource.UsageStatsDataSource
import com.example.minimalphone.domain.model.FocusSettings
import com.example.minimalphone.domain.model.UsageSnapshot
import com.example.minimalphone.domain.repository.UsageRepository
import com.example.minimalphone.util.startOfTodayMillis

class UsageRepositoryImpl(
    private val usageStatsDataSource: UsageStatsDataSource,
    private val installedAppsDataSource: InstalledAppsDataSource,
) : UsageRepository {

    override fun hasUsageAccessPermission(): Boolean = usageStatsDataSource.hasUsageAccessPermission()

    override fun openUsageAccessSettings() = usageStatsDataSource.openUsageAccessSettings()

    override suspend fun queryTodayUsage(settings: FocusSettings): UsageSnapshot {
        val now = System.currentTimeMillis()
        val start = startOfTodayMillis()
        val launchablePackages = installedAppsDataSource.getLaunchableApps()
            .map { it.packageName }
            .toSet()

        val trackedPackages = launchablePackages

        if (trackedPackages.isEmpty()) {
            return UsageSnapshot(totalForegroundMillis = 0L, usageByPackage = emptyMap())
        }

        return usageStatsDataSource.queryUsageEvents(
            startMillis = start,
            endMillis = now,
            trackedPackages = trackedPackages,
        )
    }
}
