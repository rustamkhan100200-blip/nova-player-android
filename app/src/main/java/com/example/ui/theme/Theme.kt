package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class NovaColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceCard: Color,
    val surfaceElevated: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val cardBorder: Color,
    val glassBorder: Color,
    val accent: Color
)

val LocalNovaColors = staticCompositionLocalOf {
    NovaColors(
        isDark = true,
        background = Color(0xFF080B14),
        surface = Color(0xFF121726),
        surfaceCard = Color(0xFF151B2C),
        surfaceElevated = Color(0xFF1E263D),
        primaryText = Color(0xFFF5F7FF),
        secondaryText = Color(0xFF9AA6BD),
        mutedText = Color(0xFF67738C),
        cardBorder = Color(0x338257FF),
        glassBorder = Color(0x3300D9FF),
        accent = Color(0xFF00D9FF)
    )
}

object NovaTheme {
    val colors: NovaColors
        @Composable
        @ReadOnlyComposable
        get() = LocalNovaColors.current
}

@Composable
fun MyApplicationTheme(
    appTheme: String = "Dark", // "Dark", "Light", "System"
    accentColorHex: Long = 0xFF00D9FF,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (appTheme) {
        "Light" -> false
        "System" -> systemDark
        else -> true // "Dark"
    }

    val accent = Color(accentColorHex)

    val customColors = if (isDark) {
        NovaColors(
            isDark = true,
            background = Color(0xFF080B14),
            surface = Color(0xFF121726),
            surfaceCard = Color(0xFF151B2C),
            surfaceElevated = Color(0xFF1E263D),
            primaryText = Color(0xFFF5F7FF),
            secondaryText = Color(0xFF9AA6BD),
            mutedText = Color(0xFF67738C),
            cardBorder = accent.copy(alpha = 0.25f),
            glassBorder = accent.copy(alpha = 0.4f),
            accent = accent
        )
    } else {
        NovaColors(
            isDark = false,
            background = Color(0xFFF4F6FB),
            surface = Color(0xFFFFFFFF),
            surfaceCard = Color(0xFFFFFFFF),
            surfaceElevated = Color(0xFFE9EEF7),
            primaryText = Color(0xFF121624),
            secondaryText = Color(0xFF57637C),
            mutedText = Color(0xFF8895AD),
            cardBorder = Color(0x1E000000),
            glassBorder = accent.copy(alpha = 0.35f),
            accent = accent
        )
    }

    val m3ColorScheme: ColorScheme = if (isDark) {
        darkColorScheme(
            primary = accent,
            onPrimary = Color.Black,
            primaryContainer = customColors.surfaceElevated,
            onPrimaryContainer = Color.White,
            secondary = accent,
            onSecondary = Color.Black,
            secondaryContainer = customColors.surfaceCard,
            onSecondaryContainer = Color.White,
            background = customColors.background,
            onBackground = customColors.primaryText,
            surface = customColors.surface,
            onSurface = customColors.primaryText,
            surfaceVariant = customColors.surfaceElevated,
            onSurfaceVariant = customColors.secondaryText,
            outline = customColors.cardBorder
        )
    } else {
        lightColorScheme(
            primary = accent,
            onPrimary = Color.White,
            primaryContainer = customColors.surfaceElevated,
            onPrimaryContainer = customColors.primaryText,
            secondary = accent,
            onSecondary = Color.White,
            secondaryContainer = customColors.surfaceCard,
            onSecondaryContainer = customColors.primaryText,
            background = customColors.background,
            onBackground = customColors.primaryText,
            surface = customColors.surface,
            onSurface = customColors.primaryText,
            surfaceVariant = customColors.surfaceElevated,
            onSurfaceVariant = customColors.secondaryText,
            outline = customColors.cardBorder
        )
    }

    CompositionLocalProvider(LocalNovaColors provides customColors) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            typography = Typography,
            content = content
        )
    }
}
