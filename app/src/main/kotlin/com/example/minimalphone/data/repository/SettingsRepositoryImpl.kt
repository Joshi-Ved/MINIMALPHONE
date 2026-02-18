package com.example.minimalphone.data.repository

import com.example.minimalphone.data.datasource.SettingsDataSource
import com.example.minimalphone.domain.model.AppBlockMode
import com.example.minimalphone.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first

class SettingsRepositoryImpl(
    private val settingsDataSource: SettingsDataSource,
) : SettingsRepository {

    override fun observeSettings() = settingsDataSource.observeSettings()

    override suspend fun updateDailyLimit(minutes: Int) {
        settingsDataSource.updateDailyLimit(minutes.coerceAtLeast(1))
    }

    override suspend fun setLockModeEnabled(enabled: Boolean) {
        settingsDataSource.setLockModeEnabled(enabled)
    }

    override suspend fun setPremiumEnabled(enabled: Boolean) {
        settingsDataSource.setPremiumEnabled(enabled)
    }

    override suspend fun setBlockMode(mode: AppBlockMode) {
        settingsDataSource.setBlockMode(mode)
    }

    override suspend fun toggleBlockedPackage(packageName: String) {
        val current = observeSettings().first().blockedPackages.toMutableSet()
        if (!current.add(packageName)) current.remove(packageName)
        settingsDataSource.setBlockedPackages(current)
    }

    override suspend fun toggleAllowedPackage(packageName: String) {
        val current = observeSettings().first().allowedPackages.toMutableSet()
        if (!current.add(packageName)) current.remove(packageName)
        settingsDataSource.setAllowedPackages(current)
    }

    override suspend fun clearDailyResetStamp() {
        settingsDataSource.clearDailyResetStamp()
    }

    override suspend fun setDailyResetStamp(dateStamp: String) {
        settingsDataSource.setDailyResetStamp(dateStamp)
    }
}
