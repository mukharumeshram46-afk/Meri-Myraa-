package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MyraaDarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF020617),
    primaryContainer = Color(0xFF083344),
    onPrimaryContainer = Color(0xFFA5F3FC),
    secondary = CyberMagenta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF500724),
    onSecondaryContainer = Color(0xFFFBCFE8),
    tertiary = CyberViolet,
    onTertiary = Color.White,
    background = ObsidianBackground,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = ObsidianCardBorder,
    error = CyberCrimson,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // MYRAA uses its signature premium sci-fi cyberpunk dark theme
    MaterialTheme(
        colorScheme = MyraaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
