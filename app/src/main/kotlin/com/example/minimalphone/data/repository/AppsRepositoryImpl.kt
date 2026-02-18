package com.example.minimalphone.data.repository

import com.example.minimalphone.data.datasource.InstalledAppsDataSource
import com.example.minimalphone.domain.model.InstalledApp
import com.example.minimalphone.domain.repository.AppsRepository

class AppsRepositoryImpl(
    private val installedAppsDataSource: InstalledAppsDataSource,
) : AppsRepository {
    override suspend fun getLaunchableApps(): List<InstalledApp> =
        installedAppsDataSource.getLaunchableApps()
}
