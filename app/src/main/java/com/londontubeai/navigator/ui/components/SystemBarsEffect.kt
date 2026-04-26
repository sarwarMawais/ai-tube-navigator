package com.londontubeai.navigator.ui.components

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Forces the system status & navigation bar icon tint while a screen is
 * visible, then restores the previous tint when the screen leaves.
 *
 * The app runs `enableEdgeToEdge()` so the system bars are *transparent* —
 * whatever the screen renders behind them shows through. That means the
 * caller is responsible for picking a tint that's readable against its own
 * background:
 *
 *   • Light bg (Home, Route, Map)  → `lightBackground = true`  (dark icons)
 *   • Dark gradient (Consent,
 *     Onboarding, Premium,
 *     StationDetail hero)          → `lightBackground = false` (light icons)
 *
 * This fixes the "status bar invisible / data cut at the top" reports on
 * dark-themed screens.
 */
@Composable
fun SystemBarsEffect(lightBackground: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(lightBackground) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)
        val previousStatus = controller.isAppearanceLightStatusBars
        val previousNav = controller.isAppearanceLightNavigationBars

        // Set the appearance
        controller.isAppearanceLightStatusBars = lightBackground
        controller.isAppearanceLightNavigationBars = lightBackground

        // Also set via decorView for older API levels as fallback
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                if (lightBackground) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = if (lightBackground) {
                window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
        }

        onDispose {
            controller.isAppearanceLightStatusBars = previousStatus
            controller.isAppearanceLightNavigationBars = previousNav
        }
    }
}
