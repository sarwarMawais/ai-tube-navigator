package com.londontubeai.navigator.ui.screens.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.londontubeai.navigator.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Holds in-flight setup choices made during onboarding (home/work stations,
 * step-free preference) and persists them to [AppPreferences] when the
 * user finishes onboarding. State is exposed as Compose-observable
 * mutableStateOf so the UI recomposes immediately on each selection.
 *
 * Skipping is fully supported: any field left null/false is simply not
 * written, preserving the "fully skippable" UX promise.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: AppPreferences,
) : ViewModel() {

    var homeStationId by mutableStateOf<String?>(null)
        private set
    var workStationId by mutableStateOf<String?>(null)
        private set
    var stepFreeEnabled by mutableStateOf(false)
        private set

    fun setHome(id: String?) { homeStationId = id }
    fun setWork(id: String?) { workStationId = id }
    fun setStepFree(value: Boolean) { stepFreeEnabled = value }

    /** Persists every non-null/non-default choice to DataStore. */
    suspend fun saveSetup() {
        homeStationId?.let { prefs.setHomeStationId(it) }
        workStationId?.let { prefs.setWorkStationId(it) }
        if (stepFreeEnabled) prefs.setPreferStepFree(true)
    }
}
