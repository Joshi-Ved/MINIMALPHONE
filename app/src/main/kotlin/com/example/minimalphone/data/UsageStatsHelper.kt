package com.example.minimalphone.data

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Helper for reading app-usage data from UsageStatsManager.
 *
 * UsageStatsManager returns app foreground time at system level. We use it to
 * calculate the total phone usage for today.
 */
class UsageStatsHelper(private val context: Context) {

    /**
     * Checks whether the user granted "Usage Access" for this app.
     */
    fun hasUsageAccessPermission(): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Opens Android settings screen where user can grant Usage Access.
     */
    fun openUsageAccessSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * Calculates total foreground usage for TODAY in minutes.
     */
    fun getTodayForegroundUsageMinutes(): Int {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val startOfDayMillis = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val nowMillis = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDayMillis,
            nowMillis,
        )

        val totalForegroundMillis = stats.sumOf { it.totalTimeInForeground.coerceAtLeast(0L) }
        return TimeUnit.MILLISECONDS.toMinutes(totalForegroundMillis).toInt()
    }
}
