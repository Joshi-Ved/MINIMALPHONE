package com.example.minimalphone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.minimalphone.ui.DashboardScreen
import com.example.minimalphone.ui.SettingsScreen
import com.example.minimalphone.ui.theme.FocusLiteTheme

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FocusLiteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

// Simple screen navigation state
enum class AppScreen {
    DASHBOARD,
    SETTINGS,
}

@Composable
fun AppNavigation() {
    val currentScreen = remember { mutableStateOf(AppScreen.DASHBOARD) }

    when (currentScreen.value) {
        AppScreen.DASHBOARD -> {
            DashboardScreen(
                onSettingsClick = { currentScreen.value = AppScreen.SETTINGS }
            )
        }
        AppScreen.SETTINGS -> {
            SettingsScreen(
                onBackClick = { currentScreen.value = AppScreen.DASHBOARD }
            )
        }
    }
}
