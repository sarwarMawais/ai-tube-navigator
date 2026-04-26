package com.londontubeai.navigator.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        private val KEY_PREFER_FASTEST = booleanPreferencesKey("prefer_fastest")
        private val KEY_PREFER_LESS_CROWDS = booleanPreferencesKey("prefer_less_crowds")
        private val KEY_PREFER_LESS_WALKING = booleanPreferencesKey("prefer_less_walking")
        private val KEY_PREFER_STEP_FREE = booleanPreferencesKey("prefer_step_free")
        private val KEY_PUSH_DISRUPTIONS = booleanPreferencesKey("push_disruptions")
        private val KEY_PUSH_COMMUTE = booleanPreferencesKey("push_commute")
        private val KEY_PUSH_AI_TIPS = booleanPreferencesKey("push_ai_tips")
        private val KEY_HOME_STATION_ID = stringPreferencesKey("home_station_id")
        private val KEY_WORK_STATION_ID = stringPreferencesKey("work_station_id")
        private val KEY_DARK_MODE = stringPreferencesKey("dark_mode") // "system", "light", "dark"
        private val KEY_HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        private val KEY_LARGE_TEXT = booleanPreferencesKey("large_text")
        private val KEY_LIVE_LOCATION = booleanPreferencesKey("live_location")
        private val KEY_QUIET_HOURS = booleanPreferencesKey("quiet_hours")
        private val KEY_SEVERE_ONLY = booleanPreferencesKey("severe_disruptions_only")
        private val KEY_FAVOURITE_STATIONS = stringPreferencesKey("favourite_stations")
        private val KEY_RECENT_STATIONS = stringPreferencesKey("recent_stations")
        private val KEY_MAX_WALKING_METRES = intPreferencesKey("max_walking_metres")
        private val KEY_MAX_INTERCHANGES = intPreferencesKey("max_interchanges")
        private val KEY_FAVOURITE_LINES = stringPreferencesKey("favourite_lines")
        private val KEY_MAP_STYLE = stringPreferencesKey("map_style")
        private val KEY_MAP_LINE_FILTER = stringPreferencesKey("map_line_filter")
        private val KEY_RECENT_MAP_PLACES = stringPreferencesKey("recent_map_places")
        // Privacy opt-out toggles (GDPR). Default = off (opt-in explicit).
        // Even though we don't currently send analytics, we keep these
        // persisted so the UX promise to users is honoured the moment we do.
        private val KEY_ANALYTICS_ENABLED = booleanPreferencesKey("privacy_analytics_enabled")
        private val KEY_CRASH_REPORTS_ENABLED = booleanPreferencesKey("privacy_crash_reports_enabled")
        private val KEY_PERSONALISATION_ENABLED = booleanPreferencesKey("privacy_personalisation_enabled")
        // UK GDPR consent — set to true once the user has explicitly seen and
        // dismissed the consent screen. Until then, analytics/crash collection
        // remain off (defaults above) per UK GDPR / PECR opt-in requirements.
        private val KEY_CONSENT_DECISION_MADE = booleanPreferencesKey("privacy_consent_decision_made")
    }

    val onboardingComplete: Flow<Boolean> = context.dataStore.data.map { it[KEY_ONBOARDING_COMPLETE] ?: false }
    val preferFastest: Flow<Boolean> = context.dataStore.data.map { it[KEY_PREFER_FASTEST] ?: true }
    val preferLessCrowds: Flow<Boolean> = context.dataStore.data.map { it[KEY_PREFER_LESS_CROWDS] ?: true }
    val preferLessWalking: Flow<Boolean> = context.dataStore.data.map { it[KEY_PREFER_LESS_WALKING] ?: false }
    val preferStepFree: Flow<Boolean> = context.dataStore.data.map { it[KEY_PREFER_STEP_FREE] ?: false }
    val pushDisruptions: Flow<Boolean> = context.dataStore.data.map { it[KEY_PUSH_DISRUPTIONS] ?: true }
    val pushCommute: Flow<Boolean> = context.dataStore.data.map { it[KEY_PUSH_COMMUTE] ?: true }
    val pushAiTips: Flow<Boolean> = context.dataStore.data.map { it[KEY_PUSH_AI_TIPS] ?: false }
    val homeStationId: Flow<String?> = context.dataStore.data.map { it[KEY_HOME_STATION_ID] }
    val workStationId: Flow<String?> = context.dataStore.data.map { it[KEY_WORK_STATION_ID] }
    val darkMode: Flow<String> = context.dataStore.data.map { it[KEY_DARK_MODE] ?: "system" }
    val highContrast: Flow<Boolean> = context.dataStore.data.map { it[KEY_HIGH_CONTRAST] ?: true }
    val largeText: Flow<Boolean> = context.dataStore.data.map { it[KEY_LARGE_TEXT] ?: false }
    val liveLocation: Flow<Boolean> = context.dataStore.data.map { it[KEY_LIVE_LOCATION] ?: false }
    val quietHours: Flow<Boolean> = context.dataStore.data.map { it[KEY_QUIET_HOURS] ?: false }
    val severeOnly: Flow<Boolean> = context.dataStore.data.map { it[KEY_SEVERE_ONLY] ?: false }
    val favouriteStations: Flow<String> = context.dataStore.data.map { it[KEY_FAVOURITE_STATIONS] ?: "" }
    val recentStations: Flow<String> = context.dataStore.data.map { it[KEY_RECENT_STATIONS] ?: "" }
    val maxWalkingMetres: Flow<Int> = context.dataStore.data.map { it[KEY_MAX_WALKING_METRES] ?: 500 }
    val maxInterchanges: Flow<Int> = context.dataStore.data.map { it[KEY_MAX_INTERCHANGES] ?: 3 }
    val favouriteLines: Flow<String> = context.dataStore.data.map { it[KEY_FAVOURITE_LINES] ?: "" }
    val mapStyle: Flow<String> = context.dataStore.data.map { it[KEY_MAP_STYLE] ?: "NORMAL" }
    val mapLineFilter: Flow<String?> = context.dataStore.data.map { it[KEY_MAP_LINE_FILTER] }
    val recentMapPlaces: Flow<String> = context.dataStore.data.map { it[KEY_RECENT_MAP_PLACES] ?: "" }
    val analyticsEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_ANALYTICS_ENABLED] ?: false }
    val crashReportsEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_CRASH_REPORTS_ENABLED] ?: false }
    val personalisationEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_PERSONALISATION_ENABLED] ?: true }
    val consentDecisionMade: Flow<Boolean> = context.dataStore.data.map { it[KEY_CONSENT_DECISION_MADE] ?: false }

    suspend fun setConsentDecisionMade(value: Boolean) {
        context.dataStore.edit { it[KEY_CONSENT_DECISION_MADE] = value }
    }

    suspend fun setAnalyticsEnabled(value: Boolean) {
        context.dataStore.edit { it[KEY_ANALYTICS_ENABLED] = value }
    }
    suspend fun setCrashReportsEnabled(value: Boolean) {
        context.dataStore.edit { it[KEY_CRASH_REPORTS_ENABLED] = value }
    }
    suspend fun setPersonalisationEnabled(value: Boolean) {
        context.dataStore.edit { it[KEY_PERSONALISATION_ENABLED] = value }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETE] = complete }
    }

    suspend fun setPreferFastest(value: Boolean) {
        context.dataStore.edit { it[KEY_PREFER_FASTEST] = value }
    }

    suspend fun setPreferLessCrowds(value: Boolean) {
        context.dataStore.edit { it[KEY_PREFER_LESS_CROWDS] = value }
    }

    suspend fun setPreferLessWalking(value: Boolean) {
        context.dataStore.edit { it[KEY_PREFER_LESS_WALKING] = value }
    }

    suspend fun setPreferStepFree(value: Boolean) {
        context.dataStore.edit { it[KEY_PREFER_STEP_FREE] = value }
    }

    suspend fun setPushDisruptions(value: Boolean) {
        context.dataStore.edit { it[KEY_PUSH_DISRUPTIONS] = value }
    }

    suspend fun setPushCommute(value: Boolean) {
        context.dataStore.edit { it[KEY_PUSH_COMMUTE] = value }
    }

    suspend fun setPushAiTips(value: Boolean) {
        context.dataStore.edit { it[KEY_PUSH_AI_TIPS] = value }
    }

    suspend fun setHomeStationId(id: String?) {
        context.dataStore.edit {
            if (id == null) it.remove(KEY_HOME_STATION_ID)
            else it[KEY_HOME_STATION_ID] = id
        }
    }

    suspend fun setWorkStationId(id: String?) {
        context.dataStore.edit {
            if (id == null) it.remove(KEY_WORK_STATION_ID)
            else it[KEY_WORK_STATION_ID] = id
        }
    }

    suspend fun setDarkMode(mode: String) {
        context.dataStore.edit { it[KEY_DARK_MODE] = mode }
    }

    suspend fun setHighContrast(value: Boolean) {
        context.dataStore.edit { it[KEY_HIGH_CONTRAST] = value }
    }

    suspend fun setLargeText(value: Boolean) {
        context.dataStore.edit { it[KEY_LARGE_TEXT] = value }
    }

    suspend fun setLiveLocation(value: Boolean) {
        context.dataStore.edit { it[KEY_LIVE_LOCATION] = value }
    }

    suspend fun setQuietHours(value: Boolean) {
        context.dataStore.edit { it[KEY_QUIET_HOURS] = value }
    }

    suspend fun setSevereOnly(value: Boolean) {
        context.dataStore.edit { it[KEY_SEVERE_ONLY] = value }
    }

    suspend fun setFavouriteStations(value: String) {
        context.dataStore.edit { it[KEY_FAVOURITE_STATIONS] = value }
    }

    suspend fun setRecentStations(value: String) {
        context.dataStore.edit { it[KEY_RECENT_STATIONS] = value }
    }

    suspend fun setMaxWalkingMetres(value: Int) {
        context.dataStore.edit { it[KEY_MAX_WALKING_METRES] = value }
    }

    suspend fun setMaxInterchanges(value: Int) {
        context.dataStore.edit { it[KEY_MAX_INTERCHANGES] = value }
    }

    suspend fun setFavouriteLines(value: String) {
        context.dataStore.edit { it[KEY_FAVOURITE_LINES] = value }
    }

    suspend fun setMapStyle(value: String) {
        context.dataStore.edit { it[KEY_MAP_STYLE] = value }
    }

    suspend fun setMapLineFilter(value: String?) {
        context.dataStore.edit {
            if (value == null) it.remove(KEY_MAP_LINE_FILTER)
            else it[KEY_MAP_LINE_FILTER] = value
        }
    }

    suspend fun setRecentMapPlaces(value: String) {
        context.dataStore.edit { it[KEY_RECENT_MAP_PLACES] = value }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun getAllPrefsMap(): Map<String, Any?> {
        val data = context.dataStore.data.first()
        return buildMap {
            put("prefer_fastest", data[KEY_PREFER_FASTEST] ?: true)
            put("prefer_less_crowds", data[KEY_PREFER_LESS_CROWDS] ?: true)
            put("prefer_less_walking", data[KEY_PREFER_LESS_WALKING] ?: false)
            put("prefer_step_free", data[KEY_PREFER_STEP_FREE] ?: false)
            put("push_disruptions", data[KEY_PUSH_DISRUPTIONS] ?: true)
            put("push_commute", data[KEY_PUSH_COMMUTE] ?: true)
            put("push_ai_tips", data[KEY_PUSH_AI_TIPS] ?: false)
            put("home_station_id", data[KEY_HOME_STATION_ID])
            put("work_station_id", data[KEY_WORK_STATION_ID])
            put("dark_mode", data[KEY_DARK_MODE] ?: "system")
            put("high_contrast", data[KEY_HIGH_CONTRAST] ?: false)
            put("large_text", data[KEY_LARGE_TEXT] ?: false)
            put("live_location", data[KEY_LIVE_LOCATION] ?: false)
            put("quiet_hours", data[KEY_QUIET_HOURS] ?: false)
            put("severe_disruptions_only", data[KEY_SEVERE_ONLY] ?: false)
            put("favourite_stations", data[KEY_FAVOURITE_STATIONS] ?: "")
            put("max_walking_metres", data[KEY_MAX_WALKING_METRES] ?: 500)
            put("max_interchanges", data[KEY_MAX_INTERCHANGES] ?: 3)
            put("analytics_enabled", data[KEY_ANALYTICS_ENABLED] ?: false)
            put("crash_reports_enabled", data[KEY_CRASH_REPORTS_ENABLED] ?: false)
            put("personalisation_enabled", data[KEY_PERSONALISATION_ENABLED] ?: true)
        }
    }
}
