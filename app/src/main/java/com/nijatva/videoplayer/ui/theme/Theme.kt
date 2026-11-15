package com.nijatva.videoplayer.ui.theme

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

/** One accent family: the bits that change when the user picks Green / Saffron / Indigo. */
private data class Accent(
    val primary: Color,
    val onPrimary: Color,
    val container: Color,
    val onContainer: Color,
    val secondary: Color,
    val tertiary: Color,
    val lightPrimary: Color,
)

private val GreenAccent = Accent(
    primary = EmeraldGreen,
    onPrimary = Color(0xFF00281B),
    container = ForestGreen,
    onContainer = Color(0xFFD1FAE5),
    secondary = Azure,
    tertiary = Cyan,
    lightPrimary = Color(0xFF0E9F6E),
)

private val SaffronAccent = Accent(
    primary = Saffron,
    onPrimary = Color(0xFF2B1600),
    container = SaffronDark,
    onContainer = SaffronSoft,
    secondary = Orange,
    tertiary = Amber,
    lightPrimary = SaffronDeep,
)

private val IndigoAccent = Accent(
    primary = Indigo,
    onPrimary = PureWhite,
    container = IndigoDark,
    onContainer = IndigoSoft,
    secondary = IndigoLight,
    tertiary = Violet,
    lightPrimary = IndigoDeep,
)

private fun accentOf(index: Int): Accent = when (index) {
    1 -> SaffronAccent
    2 -> IndigoAccent
    else -> GreenAccent
}

private fun darkScheme(a: Accent) = darkColorScheme(
    primary = a.primary,
    onPrimary = a.onPrimary,
    primaryContainer = a.container,
    onPrimaryContainer = a.onContainer,
    secondary = a.secondary,
    onSecondary = PureWhite,
    tertiary = a.tertiary,
    onTertiary = Color(0xFF00272B),
    background = MatteBlack,
    onBackground = Color(0xFFF3F4F6),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = CharcoalGrey,
    onSurfaceVariant = SilverGrey,
    outline = Slate,
    error = Color(0xFFEF4444),
)

private fun blackScheme(a: Accent) = darkColorScheme(
    primary = a.primary,
    onPrimary = a.onPrimary,
    primaryContainer = a.container,
    onPrimaryContainer = a.onContainer,
    secondary = a.secondary,
    onSecondary = PureWhite,
    tertiary = a.tertiary,
    background = Color(0xFF000000),
    onBackground = PureWhite,
    surface = Color(0xFF0A0A0A),
    onSurface = PureWhite,
    surfaceVariant = Color(0xFF1C1C1C),
    onSurfaceVariant = Color(0xFFE5E7EB),
    outline = Color(0xFF3A3A3A),
    error = Color(0xFFEF4444),
)

private fun lightScheme(a: Accent) = lightColorScheme(
    primary = a.lightPrimary,
    onPrimary = PureWhite,
    primaryContainer = a.onContainer,
    onPrimaryContainer = a.container,
    secondary = a.secondary,
    onSecondary = PureWhite,
    tertiary = a.tertiary,
    background = PureWhite,
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFF4F7F9),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE6EDF2),
    onSurfaceVariant = Slate,
    outline = SilverGrey,
)

/**
 * @param themeMode 0 = system, 1 = light, 2 = dark, 3 = black (pure black / OLED)
 * @param accent    0 = green, 1 = saffron, 2 = indigo
 */
@Composable
fun NijatvaTheme(
    themeMode: Int = 0,
    accent: Int = 0,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val a = accentOf(accent)
    val systemDark = isSystemInDarkTheme()

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            val dark = when (themeMode) {
                1 -> false
                2, 3 -> true
                else -> systemDark
            }
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeMode == 1 -> lightScheme(a)
        themeMode == 3 -> blackScheme(a)
        themeMode == 2 -> darkScheme(a)
        else -> if (systemDark) darkScheme(a) else lightScheme(a)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
