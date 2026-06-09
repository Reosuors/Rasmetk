package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val SophisticatedDarkColorScheme = darkColorScheme(
    primary = SophisticatedPrimary,
    secondary = SophisticatedSecondary,
    tertiary = SophisticatedTertiary,
    background = SophisticatedBg,
    surface = SophisticatedSurface,
    surfaceVariant = SophisticatedSurfaceVariant,
    onPrimary = SophisticatedOnPrimary,
    onSecondary = SophisticatedOnSecondary,
    onSurface = SophisticatedOnSurface,
    onBackground = SophisticatedOnBackground,
    primaryContainer = SophisticatedPrimaryContainer,
    onPrimaryContainer = SophisticatedOnPrimary,
    outline = SophisticatedOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force sophisticated dark by default
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve art theme styling
    content: @Composable () -> Unit,
) {
    val colorScheme = SophisticatedDarkColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
