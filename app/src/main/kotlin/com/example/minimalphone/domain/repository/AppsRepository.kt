package com.example.minimalphone.domain.repository

import com.example.minimalphone.domain.model.InstalledApp

interface AppsRepository {
    suspend fun getLaunchableApps(): List<InstalledApp>
}
