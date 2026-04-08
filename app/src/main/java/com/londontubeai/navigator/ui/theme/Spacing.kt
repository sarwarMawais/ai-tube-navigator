package com.londontubeai.navigator.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized spacing constants for consistent UI across the app
 */
object Spacing {
    // Base spacing units
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    
    // Screen padding
    val screenHorizontal = 16.dp
    val screenVertical = 8.dp
    val screenPadding = PaddingValues(horizontal = screenHorizontal, vertical = screenVertical)
    
    // Card padding
    val cardPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    val cardPaddingHorizontal = 16.dp
    val cardPaddingVertical = 12.dp
    
    // Section spacing
    val sectionSpacing = 12.dp
    val itemSpacing = 8.dp
    val sectionSpacingLarge = 20.dp
    
    // Component spacing
    val iconTextSpacing = 8.dp
    val buttonSpacing = 12.dp
    val listSpacing = 6.dp
    
    // Bottom sheet padding
    val bottomSheetPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp)
}

/**
 * Extension functions for applying spacing
 */
fun Modifier.screenPadding() = padding(Spacing.screenPadding)
fun Modifier.cardPadding() = padding(Spacing.cardPadding)
fun Modifier.horizontalScreenPadding() = padding(horizontal = Spacing.screenHorizontal)
fun Modifier.verticalScreenPadding() = padding(vertical = Spacing.screenVertical)
