package com.anxincaiguan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Blue600,
    onPrimary = SurfaceLight,
    primaryContainer = Blue50,
    onPrimaryContainer = Blue600,
    secondary = Orange500,
    onSecondary = SurfaceLight,
    secondaryContainer = Orange50,
    onSecondaryContainer = Orange500,
    background = BackgroundLight,
    onBackground = Gray900,
    surface = SurfaceLight,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,
    outline = Gray300,
    outlineVariant = Gray200,
    error = ErrorColor,
    onError = SurfaceLight
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue400,
    onPrimary = Gray900,
    primaryContainer = Blue600,
    onPrimaryContainer = Blue50,
    secondary = Orange400,
    onSecondary = Gray900,
    secondaryContainer = Orange500,
    onSecondaryContainer = Orange50,
    background = Gray900,
    onBackground = Gray100,
    surface = Gray800,
    onSurface = Gray100,
    surfaceVariant = Gray700,
    onSurfaceVariant = Gray300,
    outline = Gray600,
    outlineVariant = Gray700,
    error = ErrorColor,
    onError = Gray900
)

@Composable
fun AnxinCaiGuanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
