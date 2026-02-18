package com.example.minimalphone.data.datasource

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.minimalphone.domain.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstalledAppsDataSource(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    suspend fun getLaunchableApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        val resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        val fromLauncher = resolveInfos
            .mapNotNull { resolveInfo ->
                val packageName = resolveInfo.activityInfo?.packageName ?: return@mapNotNull null
                if (packageName == context.packageName) return@mapNotNull null
                InstalledApp(
                    packageName = packageName,
                    label = resolveInfo.loadLabel(packageManager).toString(),
                )
            }

        val fromInstalledApps = packageManager.getInstalledApplications(PackageManager.MATCH_ALL)
            .mapNotNull { appInfo ->
                val packageName = appInfo.packageName
                if (packageName == context.packageName) return@mapNotNull null
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return@mapNotNull null
                val label = appInfo.loadLabel(packageManager)?.toString().orEmpty()
                if (label.isBlank()) return@mapNotNull null
                InstalledApp(
                    packageName = launchIntent.component?.packageName ?: packageName,
                    label = label,
                )
            }

        (fromLauncher + fromInstalledApps)
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }
}
