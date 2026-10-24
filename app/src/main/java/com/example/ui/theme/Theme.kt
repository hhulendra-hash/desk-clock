package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricIndigoLight,
    onPrimary = Color(0xFF090D16),
    primaryContainer = ElectricIndigoContainer,
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = HydrationCyanLight,
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = HydrationCyanContainer,
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = RestEmeraldLight,
    onTertiary = Color(0xFF022C22),
    tertiaryContainer = RestEmeraldContainer,
    onTertiaryContainer = Color(0xFFD1FAE5),
    background = SlateBackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SlateSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SlateCardDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF334155),
    error = AlertRose
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = HydrationCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF083344),
    tertiary = RestEmerald,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF064E3B),
    background = SlateBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SlateSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SlateCardLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFCBD5E1),
    error = AlertRose
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Desk-clock defaults to sleek dark-mode studio aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
