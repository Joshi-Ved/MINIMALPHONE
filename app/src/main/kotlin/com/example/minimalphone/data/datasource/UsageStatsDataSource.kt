package com.example.minimalphone.data.datasource

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import com.example.minimalphone.domain.model.UsageSnapshot

class UsageStatsDataSource(private val context: Context) {

    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    fun hasUsageAccessPermission(): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageAccessSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun queryUsageEvents(
        startMillis: Long,
        endMillis: Long,
        trackedPackages: Set<String>,
    ): UsageSnapshot {
        val usageEvents = usageStatsManager.queryEvents(startMillis, endMillis)
        val event = UsageEvents.Event()

        val activeSessions = mutableMapOf<String, Long>()
        val totalsByPackage = mutableMapOf<String, Long>()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            val packageName = event.packageName ?: continue
            if (packageName !in trackedPackages) continue

            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED,
                UsageEvents.Event.MOVE_TO_FOREGROUND,
                -> {
                    activeSessions[packageName] = event.timeStamp
                }

                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.MOVE_TO_BACKGROUND,
                -> {
                    val startedAt = activeSessions.remove(packageName) ?: continue
                    val duration = (event.timeStamp - startedAt).coerceAtLeast(0L)
                    totalsByPackage[packageName] = (totalsByPackage[packageName] ?: 0L) + duration
                }
            }
        }

        activeSessions.forEach { (packageName, startedAt) ->
            val duration = (endMillis - startedAt).coerceAtLeast(0L)
            totalsByPackage[packageName] = (totalsByPackage[packageName] ?: 0L) + duration
        }

        return UsageSnapshot(
            totalForegroundMillis = totalsByPackage.values.sum(),
            usageByPackage = totalsByPackage.toMap(),
        )
    }
}
