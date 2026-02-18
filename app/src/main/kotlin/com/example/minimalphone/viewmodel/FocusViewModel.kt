package com.example.minimalphone.viewmodel

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.minimalphone.FocusLiteApplication
import com.example.minimalphone.domain.model.AppBlockMode
import com.example.minimalphone.domain.model.FocusSettings
import com.example.minimalphone.domain.model.InstalledApp
import com.example.minimalphone.domain.usecase.ShouldBlockPackageUseCase
import com.example.minimalphone.service.FocusTrackingService
import com.example.minimalphone.ui.state.DashboardUiState
import com.example.minimalphone.util.todayStamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Screen {
    DASHBOARD,
    SET_LIMIT,
    APP_SELECTION,
    SETTINGS,
    LOCK,
}

data class AppUiState(
    val currentScreen: Screen = Screen.DASHBOARD,
    val dashboardState: DashboardUiState = DashboardUiState.Loading,
    val settings: FocusSettings = FocusSettings(),
    val apps: List<InstalledApp> = emptyList(),
)

class FocusViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as FocusLiteApplication).appContainer
    private val settingsRepository = container.settingsRepository
    private val usageRepository = container.usageRepository
    private val appsRepository = container.appsRepository
    private val shouldBlockPackage = ShouldBlockPackageUseCase()

    private val settingsFlow = settingsRepository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.Eagerly, FocusSettings())

    private val _dashboardState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    private val _apps = MutableStateFlow<List<InstalledApp>>(emptyList())

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private var trackingJob: Job? = null

    init {
        viewModelScope.launch {
            settingsFlow.collect { settings ->
                _uiState.update { current ->
                    current.copy(settings = settings)
                }
            }
        }

        viewModelScope.launch {
            _dashboardState.collect { dashboard ->
                _uiState.update { current ->
                    current.copy(dashboardState = dashboard)
                }
            }
        }

        viewModelScope.launch {
            _currentScreen.collect { screen ->
                _uiState.update { current ->
                    current.copy(currentScreen = screen)
                }
            }
        }

        viewModelScope.launch {
            _apps.collect { apps ->
                _uiState.update { current ->
                    current.copy(apps = apps)
                }
            }
        }

        loadApps()
        startTracking()
        startTrackingService()
    }

    fun onScreenRequested(screen: Screen) {
        _currentScreen.value = screen
    }

    fun openUsageAccessSettings() {
        usageRepository.openUsageAccessSettings()
    }

    fun openAccessibilitySettings() {
        val context = getApplication<Application>().applicationContext
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun refreshNow() {
        viewModelScope.launch {
            updateDashboardSnapshot()
        }
    }

    fun saveDailyLimit(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.updateDailyLimit(minutes)
            _currentScreen.value = Screen.DASHBOARD
            updateDashboardSnapshot()
        }
    }

    fun setLockModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setLockModeEnabled(enabled)
            updateDashboardSnapshot()
        }
    }

    fun setPremiumEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setPremiumEnabled(enabled)
            updateDashboardSnapshot()
        }
    }

    fun setBlockMode(mode: AppBlockMode) {
        viewModelScope.launch {
            settingsRepository.setBlockMode(mode)
            updateDashboardSnapshot()
        }
    }

    fun toggleBlockedPackage(packageName: String) {
        viewModelScope.launch {
            settingsRepository.toggleBlockedPackage(packageName)
            updateDashboardSnapshot()
        }
    }

    fun toggleAllowedPackage(packageName: String) {
        viewModelScope.launch {
            settingsRepository.toggleAllowedPackage(packageName)
            updateDashboardSnapshot()
        }
    }

    fun dismissLockScreen() {
        _currentScreen.value = Screen.DASHBOARD
    }

    private fun startTracking() {
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch {
            while (isActive) {
                updateDashboardSnapshot()
                delay(15_000L)
            }
        }
    }

    private suspend fun updateDashboardSnapshot() {
        val hasPermission = usageRepository.hasUsageAccessPermission()
        if (!hasPermission) {
            _dashboardState.value = DashboardUiState.PermissionRequired(
                helpText = "Enable Usage Access in Settings > Special app access > Usage access > FocusLite",
            )
            return
        }

        val settings = settingsFlow.first()
        ensureDailyStamp(settings)

        val usageSnapshot = withContext(Dispatchers.IO) {
            usageRepository.queryTodayUsage(settings)
        }

        val usedMinutes = usageSnapshot.totalForegroundMinutes
        val remaining = (settings.dailyLimitMinutes - usedMinutes).coerceAtLeast(0)
        val overLimit = usedMinutes >= settings.dailyLimitMinutes

        _dashboardState.value = DashboardUiState.Ready(
            usedMinutes = usedMinutes,
            dailyLimitMinutes = settings.dailyLimitMinutes,
            remainingMinutes = remaining,
            isOverLimit = overLimit,
            lockModeEnabled = settings.lockModeEnabled,
        )

        if (settings.lockModeEnabled && overLimit) {
            _currentScreen.value = Screen.LOCK
        }
    }

    private suspend fun ensureDailyStamp(settings: FocusSettings) {
        val currentDate = todayStamp()
        if (settings.lastResetDate != currentDate) {
            settingsRepository.setDailyResetStamp(currentDate)
        }
    }

    private fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _apps.value = appsRepository.getLaunchableApps()
        }
    }

    private fun startTrackingService() {
        val context = getApplication<Application>().applicationContext
        val intent = Intent(context, FocusTrackingService::class.java)
        context.startForegroundService(intent)
    }

    fun shouldPackageBeBlocked(packageName: String): Boolean {
        val settings = settingsFlow.value
        return shouldBlockPackage(settings, packageName)
    }
}
