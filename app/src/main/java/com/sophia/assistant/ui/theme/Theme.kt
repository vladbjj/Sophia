package com.sophia.assistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SophiaColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonPurple,
    tertiary = NeonPink,
    background = DarkBg,
    surface = SurfaceDark,
    surfaceVariant = SurfaceCard,
    onPrimary = DarkBg,
    onSecondary = DarkBg,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun SophiaAssistantTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SophiaColorScheme,
        content = content
    )
}
