package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class QiraatExtendedColors(
    val diffHighlightBg: Color,
    val diffHighlightText: Color,
    val goldAccent: Color,
    val cardBorder: Color
)

val LocalQiraatColors = staticCompositionLocalOf {
    QiraatExtendedColors(
        diffHighlightBg = DiffHighlightBgLight,
        diffHighlightText = DiffHighlightTextLight,
        goldAccent = GoldSecondary,
        cardBorder = ParchmentOutline
    )
}

enum class DarkThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainerLight,
    onPrimaryContainer = OnEmeraldContainerLight,
    secondary = GoldSecondary,
    onSecondary = Color.White,
    secondaryContainer = GoldContainerLight,
    onSecondaryContainer = OnGoldContainerLight,
    background = ParchmentBackground,
    onBackground = Color(0xFF1B1B1B),
    surface = ParchmentSurface,
    onSurface = Color(0xFF1B1B1B),
    surfaceVariant = ParchmentSurfaceVariant,
    onSurfaceVariant = Color(0xFF545A57),
    outline = ParchmentOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimaryDark,
    onPrimary = Color(0xFF04382A),
    primaryContainer = EmeraldContainerDark,
    onPrimaryContainer = OnEmeraldContainerDark,
    secondary = GoldSecondaryDark,
    onSecondary = Color(0xFF332600),
    secondaryContainer = GoldContainerDark,
    onSecondaryContainer = OnGoldContainerDark,
    background = DarkManuscriptBg,
    onBackground = Color(0xFFEAECEB),
    surface = DarkManuscriptSurface,
    onSurface = Color(0xFFEAECEB),
    surfaceVariant = DarkManuscriptSurfaceVariant,
    onSurfaceVariant = Color(0xFFA7B3AE),
    outline = DarkManuscriptOutline
)

@Composable
fun QiraatAsharahTheme(
    darkThemeMode: DarkThemeMode = DarkThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (darkThemeMode) {
        DarkThemeMode.SYSTEM -> isSystemInDarkTheme()
        DarkThemeMode.LIGHT -> false
        DarkThemeMode.DARK -> true
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
    val extendedColors = if (isDark) {
        QiraatExtendedColors(
            diffHighlightBg = DiffHighlightBgDark,
            diffHighlightText = DiffHighlightTextDark,
            goldAccent = GoldSecondaryDark,
            cardBorder = DarkManuscriptOutline
        )
    } else {
        QiraatExtendedColors(
            diffHighlightBg = DiffHighlightBgLight,
            diffHighlightText = DiffHighlightTextLight,
            goldAccent = GoldSecondary,
            cardBorder = ParchmentOutline
        )
    }

    CompositionLocalProvider(LocalQiraatColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
