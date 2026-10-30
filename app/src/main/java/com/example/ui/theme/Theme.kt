package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DawnOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A1A05),
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = ElectricCyan,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004E58),
    onSecondaryContainer = Color(0xFF97F0FF),
    tertiary = AmberGlow,
    onTertiary = Color(0xFF432C00),
    background = MidnightBg,
    onBackground = TextPrimary,
    surface = MidnightSurface,
    onSurface = TextPrimary,
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = MidnightCardBorder,
    error = AlertCrimson,
    onError = Color.White
)

@Composable
fun UthoSajibTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
