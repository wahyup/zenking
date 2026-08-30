package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = KivyEmerald,
    onPrimary = SlateBackground,
    primaryContainer = KivyEmeraldDark,
    onPrimaryContainer = TextPrimary,
    secondary = PythonBlue,
    onSecondary = TextPrimary,
    tertiary = KivyCyan,
    background = SlateBackground,
    surface = SlateSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = SlateCard,
    onSurfaceVariant = TextSecondary,
    outline = SlateBorder
)

private val LightColorScheme = DarkColorScheme // Default to high-contrast Dark Developer Studio scheme

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

