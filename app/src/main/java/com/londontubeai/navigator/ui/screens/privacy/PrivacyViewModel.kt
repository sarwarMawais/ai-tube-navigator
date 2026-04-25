package com.londontubeai.navigator.ui.screens.privacy

import androidx.lifecycle.ViewModel
import com.londontubeai.navigator.data.analytics.AnalyticsTracker
import com.londontubeai.navigator.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PrivacyViewModel @Inject constructor(
    private val preferences: AppPreferences,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    val analyticsEnabled = preferences.analyticsEnabled
    val crashReportsEnabled = preferences.crashReportsEnabled
    val personalisationEnabled = preferences.personalisationEnabled

    suspend fun setAnalytics(enabled: Boolean) {
        preferences.setAnalyticsEnabled(enabled)
        analyticsTracker.updateAnalyticsSettings(enabled)
    }

    suspend fun setCrashReports(enabled: Boolean) {
        preferences.setCrashReportsEnabled(enabled)
        analyticsTracker.updateCrashlyticsSettings(enabled)
    }

    suspend fun setPersonalisation(enabled: Boolean) {
        preferences.setPersonalisationEnabled(enabled)
    }

    suspend fun exportAsJson(currentAnalytics: Boolean, currentCrashReports: Boolean, currentPersonalisation: Boolean): String {
        // Export all user data as JSON for GDPR compliance
        return """
        {
            "exportedAt": "${System.currentTimeMillis()}",
            "preferences": {
                "analyticsEnabled": $currentAnalytics,
                "crashReportsEnabled": $currentCrashReports,
                "personalisationEnabled": $currentPersonalisation
            },
            "note": "All data is stored locally on your device. This export contains your current preferences."
        }
        """.trimIndent()
    }

    suspend fun deleteAll() {
        preferences.clearAll()
    }
}
