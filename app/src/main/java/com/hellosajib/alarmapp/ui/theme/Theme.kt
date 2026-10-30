package com.hellosajib.alarmapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Blue600,
    onPrimary = Color.White,
    primaryContainer = Blue100,
    onPrimaryContainer = Blue900,
    secondary = Sky500,
    onSecondary = Color.White,
    secondaryContainer = Blue50,
    onSecondaryContainer = Blue700,
    tertiary = Blue700,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceContainerHigh,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainerLowest = LightSurface,
    surfaceContainerLow = LightSurface,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightOutlineVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = ErrorLight,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = Color(0xFF7F1D1D)
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue300,
    onPrimary = Color(0xFF00205A),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Blue100,
    secondary = Sky300,
    onSecondary = Color(0xFF00344A),
    secondaryContainer = Color(0xFF142A4D),
    onSecondaryContainer = Blue200,
    tertiary = Blue200,
    onTertiary = Blue900,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceContainerHigh,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkOutlineVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = ErrorDark,
    onError = Color(0xFF450A0A),
    errorContainer = ErrorContainerDark,
    onErrorContainer = Color(0xFFFFDADA)
)

/** Colors that Material 3 has no role for. */
@Immutable
data class ExtendedColors(
    val success: Color,
    val successContainer: Color,
    val heroGradient: List<Color>
)

private val LightExtendedColors = ExtendedColors(
    success = SuccessLight,
    successContainer = SuccessContainerLight,
    heroGradient = listOf(Blue600, Blue500)
)

private val DarkExtendedColors = ExtendedColors(
    success = SuccessDark,
    successContainer = SuccessContainerDark,
    heroGradient = listOf(Color(0xFF1E40AF), Color(0xFF1D4ED8))
)

private val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun HeyUthoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalExtendedColors provides if (darkTheme) DarkExtendedColors else LightExtendedColors
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
