package com.example.minimalphone.domain.repository

import com.example.minimalphone.domain.model.AppBlockMode
import com.example.minimalphone.domain.model.FocusSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<FocusSettings>
    suspend fun updateDailyLimit(minutes: Int)
    suspend fun setLockModeEnabled(enabled: Boolean)
    suspend fun setPremiumEnabled(enabled: Boolean)
    suspend fun setBlockMode(mode: AppBlockMode)
    suspend fun toggleBlockedPackage(packageName: String)
    suspend fun toggleAllowedPackage(packageName: String)
    suspend fun clearDailyResetStamp()
    suspend fun setDailyResetStamp(dateStamp: String)
}
