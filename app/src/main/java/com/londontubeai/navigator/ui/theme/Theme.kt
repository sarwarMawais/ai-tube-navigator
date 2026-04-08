package com.londontubeai.navigator.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    errorContainer = md_theme_dark_errorContainer,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
)

@Composable
fun AiTubeNavigatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    highContrast: Boolean = false,
    largeText: Boolean = false,
    content: @Composable () -> Unit,
) {
    val baseColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val colorScheme = if (highContrast) {
        if (darkTheme) {
            baseColorScheme.copy(
                background = Color.Black,
                surface = Color(0xFF0A0A0A),
                onBackground = Color.White,
                onSurface = Color.White,
                outline = Color.White.copy(alpha = 0.6f),
            )
        } else {
            baseColorScheme.copy(
                background = Color.White,
                surface = Color.White,
                onBackground = Color.Black,
                onSurface = Color.Black,
                outline = Color.Black.copy(alpha = 0.6f),
            )
        }
    } else baseColorScheme

    val typography = if (largeText) {
        val scale = 1.2f
        Typography(
            displayLarge = AppTypography.displayLarge.copy(fontSize = (57 * scale).sp),
            displayMedium = AppTypography.displayMedium.copy(fontSize = (45 * scale).sp),
            displaySmall = AppTypography.displaySmall.copy(fontSize = (36 * scale).sp),
            headlineLarge = AppTypography.headlineLarge.copy(fontSize = (32 * scale).sp),
            headlineMedium = AppTypography.headlineMedium.copy(fontSize = (28 * scale).sp),
            headlineSmall = AppTypography.headlineSmall.copy(fontSize = (24 * scale).sp),
            titleLarge = AppTypography.titleLarge.copy(fontSize = (22 * scale).sp),
            titleMedium = AppTypography.titleMedium.copy(fontSize = (16 * scale).sp),
            titleSmall = AppTypography.titleSmall.copy(fontSize = (14 * scale).sp),
            bodyLarge = AppTypography.bodyLarge.copy(fontSize = (16 * scale).sp),
            bodyMedium = AppTypography.bodyMedium.copy(fontSize = (14 * scale).sp),
            bodySmall = AppTypography.bodySmall.copy(fontSize = (12 * scale).sp),
            labelLarge = AppTypography.labelLarge.copy(fontSize = (14 * scale).sp),
            labelMedium = AppTypography.labelMedium.copy(fontSize = (12 * scale).sp),
            labelSmall = AppTypography.labelSmall.copy(fontSize = (11 * scale).sp),
        )
    } else AppTypography

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content,
    )
}
