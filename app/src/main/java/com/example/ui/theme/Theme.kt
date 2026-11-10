package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BlueLight,
    onPrimary = NavyDark,
    primaryContainer = NavyPrimary,
    onPrimaryContainer = BlueContainer,
    secondary = SkyAccent,
    onSecondary = White,
    secondaryContainer = NavySurface,
    onSecondaryContainer = BlueContainer,
    tertiary = GoldAccent,
    onTertiary = NavyDark,
    tertiaryContainer = Color(0xFF78350F),
    onTertiaryContainer = GoldLight,
    background = NavyDark,
    onBackground = White,
    surface = NavySurface,
    onSurface = White,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569)
)

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = White,
    primaryContainer = BlueContainer,
    onPrimaryContainer = NavyPrimary,
    secondary = BlueModern,
    onSecondary = White,
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = NavyPrimary,
    tertiary = GoldPrimary,
    onTertiary = White,
    tertiaryContainer = GoldLight,
    onTertiaryContainer = Color(0xFF78350F),
    background = GrayBackground,
    onBackground = DarkText,
    surface = White,
    onSurface = DarkText,
    surfaceVariant = GraySurface,
    onSurfaceVariant = GrayText,
    outline = GrayBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep brand navy/blue/gold consistent
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
