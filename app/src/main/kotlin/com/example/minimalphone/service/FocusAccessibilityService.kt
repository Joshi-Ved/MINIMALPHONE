package com.example.minimalphone.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.minimalphone.FocusLiteApplication
import com.example.minimalphone.MainActivity
import com.example.minimalphone.domain.usecase.ShouldBlockPackageUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FocusAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val shouldBlockPackage = ShouldBlockPackageUseCase()

    private val settingsRepository by lazy {
        (application as FocusLiteApplication).appContainer.settingsRepository
    }

    private val usageRepository by lazy {
        (application as FocusLiteApplication).appContainer.usageRepository
    }

    private val checkMutex = Mutex()
    @Volatile private var lastCheckAtMillis: Long = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val foregroundPackage = event.packageName?.toString() ?: return
        if (foregroundPackage == packageName) return

        scope.launch {
            val now = System.currentTimeMillis()
            if (now - lastCheckAtMillis < 1200L) return@launch

            checkMutex.withLock {
                val settings = settingsRepository.observeSettings().first()
                if (!settings.lockModeEnabled) return@withLock
                if (!usageRepository.hasUsageAccessPermission()) return@withLock

                val usage = usageRepository.queryTodayUsage(settings)
                val overLimit = usage.totalForegroundMinutes >= settings.dailyLimitMinutes
                val blockedByList = shouldBlockPackage(settings, foregroundPackage)
                if (!overLimit && !blockedByList) return@withLock

                lastCheckAtMillis = now
                performGlobalAction(GLOBAL_ACTION_HOME)
                launchLockScreen()
            }
        }
    }

    private fun launchLockScreen() {
        val intent = Intent(this, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_FORCE_LOCK, true)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
