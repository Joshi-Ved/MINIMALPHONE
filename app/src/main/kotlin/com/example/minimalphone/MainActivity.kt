package com.example.minimalphone

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minimalphone.domain.model.AppBlockMode
import com.example.minimalphone.ui.AppSelectionScreen
import com.example.minimalphone.ui.DashboardScreen
import com.example.minimalphone.ui.LockScreen
import com.example.minimalphone.ui.SetLimitScreen
import com.example.minimalphone.ui.SettingsScreen
import com.example.minimalphone.ui.theme.FocusLiteTheme
import com.example.minimalphone.viewmodel.FocusViewModel
import com.example.minimalphone.viewmodel.Screen

// ─────────────────────────────────────────────────────────────────────────────
// 📘 BEGINNER CONCEPT: What is an Activity?
// ─────────────────────────────────────────────────────────────────────────────
// An Activity is the ENTRY POINT of an Android app — it is the "window" that
// appears on screen.  Every app needs at least one Activity.
//
// In modern Android we use a SINGLE Activity and let Jetpack Compose handle
// all the different screens (no Fragments or XML needed).
//
// ComponentActivity is the base class that adds Compose support via
// setContent { }.
// ─────────────────────────────────────────────────────────────────────────────

class MainActivity : ComponentActivity() {

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FocusLiteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNavigation(forceLock = intent.getBooleanExtra(EXTRA_FORCE_LOCK, false))
                }
            }
        }
    }

    companion object {
        const val EXTRA_FORCE_LOCK = "extra_force_lock"
    }
}

@Composable
fun AppNavigation(
    forceLock: Boolean,
    viewModel: FocusViewModel = viewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    if (forceLock) {
        viewModel.onScreenRequested(Screen.LOCK)
    }

    when (state.currentScreen) {
        Screen.DASHBOARD -> {
            DashboardScreen(
                state = state.dashboardState,
                onOpenUsageSettingsClick = viewModel::openUsageAccessSettings,
                onRefreshClick = viewModel::refreshNow,
                onSetLimitClick = { viewModel.onScreenRequested(Screen.SET_LIMIT) },
                onAppSelectionClick = { viewModel.onScreenRequested(Screen.APP_SELECTION) },
                onSettingsClick = { viewModel.onScreenRequested(Screen.SETTINGS) },
            )
        }

        Screen.SET_LIMIT -> {
            SetLimitScreen(
                currentLimitMinutes = state.settings.dailyLimitMinutes,
                onSave = viewModel::saveDailyLimit,
                onBack = { viewModel.onScreenRequested(Screen.DASHBOARD) },
            )
        }

        Screen.APP_SELECTION -> {
            val selectedPackages = if (state.settings.blockMode == AppBlockMode.BLOCK_SELECTED) {
                state.settings.blockedPackages
            } else {
                state.settings.allowedPackages
            }

            AppSelectionScreen(
                apps = state.apps,
                selectedPackages = selectedPackages,
                mode = state.settings.blockMode,
                onTogglePackage = { packageName ->
                    if (state.settings.blockMode == AppBlockMode.BLOCK_SELECTED) {
                        viewModel.toggleBlockedPackage(packageName)
                    } else {
                        viewModel.toggleAllowedPackage(packageName)
                    }
                },
                onModeChanged = viewModel::setBlockMode,
                onBack = { viewModel.onScreenRequested(Screen.DASHBOARD) },
            )
        }

        Screen.SETTINGS -> {
            SettingsScreen(
                lockModeEnabled = state.settings.lockModeEnabled,
                premiumEnabled = state.settings.premiumEnabled,
                blockMode = state.settings.blockMode,
                onLockModeChanged = viewModel::setLockModeEnabled,
                onPremiumChanged = viewModel::setPremiumEnabled,
                onSetLimit = { viewModel.onScreenRequested(Screen.SET_LIMIT) },
                onOpenAppSelection = { viewModel.onScreenRequested(Screen.APP_SELECTION) },
                onOpenAccessibilitySettings = viewModel::openAccessibilitySettings,
                onBack = { viewModel.onScreenRequested(Screen.DASHBOARD) },
            )
        }

        Screen.LOCK -> {
            LockScreen(
                onOpenSettings = { viewModel.onScreenRequested(Screen.SETTINGS) },
            )
        }
    }
}
