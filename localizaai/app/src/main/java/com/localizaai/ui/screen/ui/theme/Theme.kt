package com.localizaai.ui.screen.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = DarkModeSecondary1,
    inversePrimary = DarkModeHighlight,
    secondary = DarkModeSecondary2,
    background = DarkModeBackground,
    onPrimary = DarkModeText,
    onSecondary = DarkModeSecundaryText,
    onSurface = OffWhite,
    tertiary = DarkModeBackgroundTopbar
)

private val LightColorScheme = lightColorScheme(
    primary = LightModeSecondary1,
    inversePrimary = LightModeHighlight,
    secondary = LightModeSecondary2,
    background = LightModeBackground,
    onPrimary = LightModeText,
    onSecondary = LightModeSecondaryText,
    onSurface = OffWhite,
    tertiary = LightModeBackgroundTopbar
)

@Composable
fun localizaaiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}