package com.textlexiq.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    secondary = SecondaryBlue,
    onSecondary = Color.White,
    tertiary = AccentPink,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = TextWhite,
    onSurface = TextGray,
    surfaceVariant = SurfaceBorder // Use variant for borders if needed
)

private val LightColors = lightColorScheme(
    primary = PrimaryPurple,
    secondary = SecondaryBlue,
    tertiary = AccentPink,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = TextBlack,
    onSurface = TextBlack
)

@Composable
fun TextLexiqTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
