package com.londontubeai.navigator.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppReviewManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val reviewManager = ReviewManagerFactory.create(context)
    private val sp: SharedPreferences =
        context.getSharedPreferences("app_review", Context.MODE_PRIVATE)

    companion object {
        private const val MIN_JOURNEYS_BEFORE_REVIEW = 3
        private const val MIN_DAYS_BETWEEN_PROMPTS_MS = 14L * 24 * 60 * 60 * 1000
        private const val KEY_COUNT = "journey_count"
        private const val KEY_LAST_PROMPT = "last_prompt_ms"
    }

    /** Call after a route is successfully calculated to trigger review when conditions are met. */
    fun onJourneyCompleted(activity: Activity) {
        val count = sp.getInt(KEY_COUNT, 0) + 1
        sp.edit().putInt(KEY_COUNT, count).apply()

        val lastPromptMs = sp.getLong(KEY_LAST_PROMPT, 0L)
        val msSinceLast = System.currentTimeMillis() - lastPromptMs
        val shouldPrompt = count >= MIN_JOURNEYS_BEFORE_REVIEW &&
            (lastPromptMs == 0L || msSinceLast >= MIN_DAYS_BETWEEN_PROMPTS_MS)

        if (shouldPrompt) {
            sp.edit().putLong(KEY_LAST_PROMPT, System.currentTimeMillis()).apply()
            requestReview(activity)
        }
    }

    private fun requestReview(activity: Activity) {
        reviewManager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                reviewManager.launchReviewFlow(activity, task.result)
            }
        }
    }
}
