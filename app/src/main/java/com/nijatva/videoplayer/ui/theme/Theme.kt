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

/** Green + blue + white + black palette. */
private val DarkColors = darkColorScheme(
    primary = EmeraldGreen,
    onPrimary = Color(0xFF00281B),
    primaryContainer = ForestGreen,
    onPrimaryContainer = Color(0xFFD1FAE5),
    secondary = Azure,
    onSecondary = PureWhite,
    secondaryContainer = NavyBlue,
    onSecondaryContainer = SkyBlue,
    tertiary = Cyan,
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

private val HighContrastDarkColors = darkColorScheme(
    primary = MintGreen,
    onPrimary = Color(0xFF00281B),
    primaryContainer = ForestGreen,
    onPrimaryContainer = Color(0xFFD1FAE5),
    secondary = SkyBlue,
    onSecondary = NavyBlue,
    background = Color(0xFF000000),
    onBackground = PureWhite,
    surface = Color(0xFF0A0A0A),
    onSurface = PureWhite,
    surfaceVariant = Color(0xFF1C1C1C),
    onSurfaceVariant = Color(0xFFE5E7EB),
    tertiary = Cyan,
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0E9F6E),
    onPrimary = PureWhite,
    primaryContainer = MintGreen,
    onPrimaryContainer = ForestGreen,
    secondary = RoyalBlue,
    onSecondary = PureWhite,
    secondaryContainer = SkyBlue,
    onSecondaryContainer = NavyBlue,
    tertiary = Teal,
    background = PureWhite,
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFF4F7F9),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE6EDF2),
    onSurfaceVariant = Slate,
    outline = SilverGrey,
)

@Composable
fun NijatvaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    highContrastDark: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> if (highContrastDark) HighContrastDarkColors else DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
