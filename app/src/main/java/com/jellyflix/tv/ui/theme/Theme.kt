package com.jellyflix.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

private val JellyflixColors = darkColorScheme(
    primary = Color(0xFF7E5BEF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5E3ED5),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF00D4B1),
    onSecondary = Color(0xFF00201A),
    background = Color(0xFF0B0D12),
    onBackground = Color(0xFFECEEF5),
    surface = Color(0xFF141821),
    onSurface = Color(0xFFD8DBE5),
    surfaceVariant = Color(0xFF1C2130),
    onSurfaceVariant = Color(0xFFA8ADBD),
)

@Composable
fun JellyflixTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = JellyflixColors,
        typography = JellyflixTypography,
        shapes = JellyflixShapes,
        content = content,
    )
}
