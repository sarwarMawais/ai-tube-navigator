package com.londontubeai.navigator.data.analytics

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.londontubeai.navigator.data.preferences.AppPreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Analytics tracker for measuring real impact of AI features.
 * Uses Firebase Analytics for production tracking.
 *
 * Tracks:
 * - Time saved per journey (carriage recommendation effectiveness)
 * - Crowd avoidance success rate
 * - Route prediction accuracy
 * - Feature usage (map, offline, premium)
 * - Session metrics
 */
@Singleton
class AnalyticsTracker @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics,
    private val preferences: AppPreferences,
) {

    companion object {
        private const val TAG = "TubeAnalytics"
    }

    private var analyticsEnabled: Boolean = true
    private var crashReportsEnabled: Boolean = true

    init {
        runBlocking {
            analyticsEnabled = preferences.analyticsEnabled.first()
            crashReportsEnabled = preferences.crashReportsEnabled.first()
        }
        firebaseAnalytics.setAnalyticsCollectionEnabled(analyticsEnabled)
        updateCrashlyticsCollection()
    }

    fun updateAnalyticsSettings(enabled: Boolean) {
        analyticsEnabled = enabled
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
    }

    fun updateCrashlyticsSettings(enabled: Boolean) {
        crashReportsEnabled = enabled
        updateCrashlyticsCollection()
    }

    private fun updateCrashlyticsCollection() {
        crashlytics.setCrashlyticsCollectionEnabled(crashReportsEnabled)
    }

    // ── Journey Analytics ────────────────────────────────────

    fun trackRouteCalculated(fromId: String, toId: String, durationMinutes: Int, interchanges: Int) {
        log("route_calculated", mapOf(
            "from" to fromId, "to" to toId,
            "duration_min" to durationMinutes, "interchanges" to interchanges,
        ))
    }

    fun trackCarriageRecommendationShown(stationId: String, carriageNumber: Int, timeSavedSeconds: Int) {
        log("carriage_rec_shown", mapOf(
            "station" to stationId, "carriage" to carriageNumber, "time_saved_sec" to timeSavedSeconds,
        ))
    }

    fun trackCrowdPredictionShown(stationId: String, crowdLevel: String, percentFull: Int, confidence: Float) {
        log("crowd_prediction_shown", mapOf(
            "station" to stationId, "level" to crowdLevel,
            "percent" to percentFull, "confidence" to confidence,
        ))
    }

    fun trackDelayPredictionShown(lineId: String, expectedDelay: Int, riskScore: Float) {
        log("delay_prediction_shown", mapOf(
            "line" to lineId, "delay_min" to expectedDelay, "risk" to riskScore,
        ))
    }

    // ── Feature Usage ────────────────────────────────────────

    fun trackScreenView(screenName: String) {
        log("screen_view", mapOf("screen" to screenName))
    }

    fun trackMapOpened(stationCount: Int) {
        log("map_opened", mapOf("stations_visible" to stationCount))
    }

    fun trackOfflineModeActivated() {
        log("offline_mode_activated", emptyMap())
    }

    fun trackOnlineRestored() {
        log("online_restored", emptyMap())
    }

    fun trackStationViewed(stationId: String) {
        log("station_viewed", mapOf("station" to stationId))
    }

    fun trackSearchPerformed(query: String, resultCount: Int) {
        log("search_performed", mapOf("query_length" to query.length, "results" to resultCount))
    }

    // ── Monetisation ─────────────────────────────────────────

    fun trackPremiumScreenViewed() {
        log("premium_screen_viewed", emptyMap())
    }

    fun trackPremiumPlanSelected(plan: String, price: String) {
        log("premium_plan_selected", mapOf("plan" to plan, "price" to price))
    }

    fun trackOnboardingCompleted(pagesViewed: Int) {
        log("onboarding_completed", mapOf("pages_viewed" to pagesViewed))
    }

    fun trackOnboardingSkipped(atPage: Int) {
        log("onboarding_skipped", mapOf("at_page" to atPage))
    }

    // ── AI Model Quality ─────────────────────────────────────

    fun trackModelPredictionAccuracy(modelName: String, predicted: Float, actual: Float) {
        val error = kotlin.math.abs(predicted - actual)
        log("model_accuracy", mapOf(
            "model" to modelName, "predicted" to predicted, "actual" to actual, "error" to error,
        ))
    }

    // ── Session ──────────────────────────────────────────────

    fun trackAppOpened() {
        log("app_opened", mapOf("timestamp" to System.currentTimeMillis()))
    }

    fun trackAppBackgrounded() {
        log("app_backgrounded", mapOf("timestamp" to System.currentTimeMillis()))
    }

    // ── Internal ─────────────────────────────────────────────

    private fun log(event: String, params: Map<String, Any>) {
        if (analyticsEnabled) {
            val bundle = Bundle().apply {
                params.forEach { (key, value) ->
                    when (value) {
                        is String -> putString(key, value)
                        is Int -> putInt(key, value)
                        is Long -> putLong(key, value)
                        is Float -> putFloat(key, value)
                        is Double -> putDouble(key, value)
                        is Boolean -> putBoolean(key, value)
                    }
                }
            }
            firebaseAnalytics.logEvent(event, bundle)
        }
        // Always log locally for debugging
        Log.d(TAG, "EVENT: $event | ${params.entries.joinToString(", ") { "${it.key}=${it.value}" }}")
    }
}
