package com.example.minimalphone.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    background = OffWhite,
    onBackground = Black,
    surface = White,
    onSurface = DarkGray,
    primary = Black,
    onPrimary = White,
    secondary = MediumGray,
    onSecondary = White,
    error = Black,
    onError = White,
)

private val DarkColorScheme = darkColorScheme(
    background = Black,
    onBackground = White,
    surface = DarkGray,
    onSurface = LightGray,
    primary = White,
    onPrimary = Black,
    secondary = MediumGray,
    onSecondary = Black,
    error = White,
    onError = Black,
)

@Composable
fun FocusLiteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
