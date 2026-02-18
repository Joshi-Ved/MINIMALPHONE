package com.example.minimalphone

import android.content.Context
import com.example.minimalphone.data.datasource.InstalledAppsDataSource
import com.example.minimalphone.data.datasource.SettingsDataSource
import com.example.minimalphone.data.datasource.UsageStatsDataSource
import com.example.minimalphone.data.repository.AppsRepositoryImpl
import com.example.minimalphone.data.repository.SettingsRepositoryImpl
import com.example.minimalphone.data.repository.UsageRepositoryImpl
import com.example.minimalphone.domain.repository.AppsRepository
import com.example.minimalphone.domain.repository.SettingsRepository
import com.example.minimalphone.domain.repository.UsageRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val settingsDataSource by lazy { SettingsDataSource(appContext) }
    private val installedAppsDataSource by lazy { InstalledAppsDataSource(appContext) }
    private val usageStatsDataSource by lazy { UsageStatsDataSource(appContext) }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(settingsDataSource)
    }

    val usageRepository: UsageRepository by lazy {
        UsageRepositoryImpl(usageStatsDataSource, installedAppsDataSource)
    }

    val appsRepository: AppsRepository by lazy {
        AppsRepositoryImpl(installedAppsDataSource)
    }
}
