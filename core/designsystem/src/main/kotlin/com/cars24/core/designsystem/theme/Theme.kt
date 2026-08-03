package com.cars24.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LightScheme = lightColorScheme(
    primary = Indigo600,
    onPrimary = Neutral0,
    primaryContainer = Indigo100,
    onPrimaryContainer = Indigo900,
    secondary = Amber600,
    onSecondary = Neutral0,
    secondaryContainer = Amber100,
    onSecondaryContainer = Neutral900,
    background = Neutral50,
    onBackground = Neutral900,
    surface = Neutral0,
    onSurface = Neutral900,
    surfaceVariant = Neutral100,
    onSurfaceVariant = Neutral600,
    outline = Neutral200,
    error = Red600,
    onError = Neutral0,
    errorContainer = Red100,
    onErrorContainer = Red600,
)

private val DarkScheme = darkColorScheme(
    primary = Indigo400,
    onPrimary = Neutral0,
    primaryContainer = Indigo800,
    onPrimaryContainer = Indigo100,
    secondary = Amber500,
    onSecondary = Neutral900,
    secondaryContainer = Dark200,
    onSecondaryContainer = Amber100,
    background = Dark0,
    onBackground = Neutral0,
    surface = Dark100,
    onSurface = Neutral0,
    surfaceVariant = Dark200,
    onSurfaceVariant = Neutral400,
    outline = Dark200,
    error = Red600,
    onError = Neutral0,
    errorContainer = Red100,
    onErrorContainer = Red600,
)

val LocalCars24Colors = staticCompositionLocalOf { LightCars24Colors }

@Composable
fun Cars24Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val extended = if (darkTheme) DarkCars24Colors else LightCars24Colors
    CompositionLocalProvider(LocalCars24Colors provides extended) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = Cars24Typography,
            content = content,
        )
    }
}

object Cars24 {
    val colors: Cars24Colors
        @Composable @ReadOnlyComposable
        get() = LocalCars24Colors.current
}
