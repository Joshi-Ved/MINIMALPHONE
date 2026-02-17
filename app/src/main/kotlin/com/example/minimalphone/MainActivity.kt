package com.example.minimalphone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.minimalphone.ui.DashboardScreen
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

        // enableEdgeToEdge() lets our UI draw behind the system bars
        // (status bar, navigation bar) for a modern full-screen look.
        enableEdgeToEdge()

        // setContent { } is where we plug in our Compose UI.
        // Everything inside this lambda IS the user interface.
        setContent {
            // FocusLiteTheme wraps the app in our custom Material3 colors
            // and typography so every composable inside inherits the theme.
            FocusLiteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    // DashboardScreen is our one-and-only screen.
                    // It creates its own DashboardViewModel internally.
                    DashboardScreen()
                }
            }
        }
    }
}
