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
    val highContrast: Flow<Boolean> = context.dataStore.data.map { it[KEY_HIGH_CONTRAST] ?: false }
    val largeText: Flow<Boolean> = context.dataStore.data.map { it[KEY_LARGE_TEXT] ?: false }
    val liveLocation: Flow<Boolean> = context.dataStore.data.map { it[KEY_LIVE_LOCATION] ?: false }
    val quietHours: Flow<Boolean> = context.dataStore.data.map { it[KEY_QUIET_HOURS] ?: false }
    val severeOnly: Flow<Boolean> = context.dataStore.data.map { it[KEY_SEVERE_ONLY] ?: false }
    val favouriteStations: Flow<String> = context.dataStore.data.map { it[KEY_FAVOURITE_STATIONS] ?: "" }
    val recentStations: Flow<String> = context.dataStore.data.map { it[KEY_RECENT_STATIONS] ?: "" }
    val maxWalkingMetres: Flow<Int> = context.dataStore.data.map { it[KEY_MAX_WALKING_METRES] ?: 500 }
    val maxInterchanges: Flow<Int> = context.dataStore.data.map { it[KEY_MAX_INTERCHANGES] ?: 3 }
    val favouriteLines: Flow<String> = context.dataStore.data.map { it[KEY_FAVOURITE_LINES] ?: "" }

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

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
