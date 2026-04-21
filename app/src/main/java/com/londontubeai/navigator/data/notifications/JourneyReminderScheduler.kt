package com.londontubeai.navigator.data.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JourneyReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun scheduleReminder(
        workName: String,
        triggerAtMillis: Long,
        title: String,
        message: String,
    ): Boolean {
        val delayMs = triggerAtMillis - System.currentTimeMillis()
        if (delayMs <= 0L) return false

        val request = OneTimeWorkRequestBuilder<JourneyReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    JourneyReminderWorker.KEY_TITLE to title,
                    JourneyReminderWorker.KEY_MESSAGE to message,
                ),
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, request)

        return true
    }
}
