package com.londontubeai.navigator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.londontubeai.navigator.data.network.NetworkMonitor
import com.londontubeai.navigator.data.preferences.AppPreferences
import com.londontubeai.navigator.navigation.AppNavigation
import com.londontubeai.navigator.ui.appicon.AppIconManager
import com.londontubeai.navigator.ui.theme.AiTubeNavigatorTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var networkMonitor: NetworkMonitor
    @Inject lateinit var appPreferences: AppPreferences
    @Inject lateinit var iconManager: AppIconManager

    private var isReady by mutableStateOf(false)
    private var showOnboarding by mutableStateOf(false)
    private var showConsent by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep splash screen until we know whether to show onboarding
        splashScreen.setKeepOnScreenCondition { !isReady }

        lifecycleScope.launch {
            showConsent = !appPreferences.consentDecisionMade.first()
            showOnboarding = !appPreferences.onboardingComplete.first()
            isReady = true
        }

        setContent {
            val darkModePref by appPreferences.darkMode.collectAsState(initial = "system")
            val highContrast by appPreferences.highContrast.collectAsState(initial = false)
            val largeText by appPreferences.largeText.collectAsState(initial = false)
            val isSystemDark = isSystemInDarkTheme()
            val useDarkTheme = when (darkModePref) {
                "dark" -> true
                "light" -> false
                else -> isSystemDark
            }

            val iconOption = remember(iconManager) {
                val id = iconManager.getCurrentIconId()
                AppIconManager.ALL_ICONS.find { it.id == id } ?: AppIconManager.ALL_ICONS.first()
            }

            AiTubeNavigatorTheme(
                darkTheme = useDarkTheme,
                highContrast = highContrast,
                largeText = largeText,
            ) {
                if (isReady) {
                    AppNavigation(
                        networkMonitor = networkMonitor,
                        startOnboarding = showOnboarding,
                        startConsent = showConsent,
                        appPreferences = appPreferences,
                        iconGradientStart = Color(iconOption.gradientStart),
                        iconGradientEnd = Color(iconOption.gradientEnd),
                    )
                }
            }
        }
    }
}
