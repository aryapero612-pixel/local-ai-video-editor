package com.localai.videoeditor

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7C5CFF),
    onPrimary = Color.White,
    secondary = Color(0xFF22D3EE),
    background = Color(0xFF0B0F1A),
    onBackground = Color(0xFFE8ECF8),
    surface = Color(0xFF141A2B),
    onSurface = Color(0xFFE8ECF8),
    surfaceVariant = Color(0xFF1C2338),
    onSurfaceVariant = Color(0xFF9AA4BF)
)

@Composable
fun VideoEditorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}
