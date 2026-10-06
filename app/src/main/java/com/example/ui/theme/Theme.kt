package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ArafBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001C38),
    secondary = ArafOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBC8),
    onSecondaryContainer = Color(0xFF321200),
    tertiary = ArafPink,
    onTertiary = Color.White,
    background = ArafBackground,
    onBackground = ArafDarkText,
    surface = ArafSurface,
    onSurface = ArafDarkText,
    surfaceVariant = Color(0xFFE8E9ED),
    onSurfaceVariant = Color(0xFF44474E),
    outline = ArafBorder
)

@Composable
fun ArafTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ArafTheme(content = content)
}
