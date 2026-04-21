package com.londontubeai.navigator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Theme-aware brand gradient colors used by UnifiedHeader and hero cards.
 * Automatically swaps to a darker, lifted palette in dark mode so the
 * UI doesn't blind users with a bright light-mode blue.
 */
data class BrandGradient(
    val top: Color,
    val mid: Color,
    val bottom: Color,
    val onBrand: Color = OnBrandGradient,
) {
    fun toList(): List<Color> = listOf(top, mid, bottom)
    fun vertical(): Brush = Brush.verticalGradient(toList())
    fun linear(): Brush = Brush.linearGradient(toList())
}

private val LightBrandGradient = BrandGradient(
    top = BrandGradientTopLight,
    mid = BrandGradientMidLight,
    bottom = BrandGradientBottomLight,
)

private val DarkBrandGradient = BrandGradient(
    top = BrandGradientTopDark,
    mid = BrandGradientMidDark,
    bottom = BrandGradientBottomDark,
)

/**
 * Returns the brand gradient appropriate for the current theme.
 * Use this wherever a navy→primary hero gradient is drawn.
 */
@Composable
@ReadOnlyComposable
fun brandGradient(): BrandGradient =
    if (isSystemInDarkTheme()) DarkBrandGradient else LightBrandGradient
