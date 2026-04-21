package com.londontubeai.navigator.data.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class JourneyReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationService: TubeNotificationService,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE).orEmpty()
        val message = inputData.getString(KEY_MESSAGE).orEmpty()

        if (title.isBlank() || message.isBlank()) {
            return Result.failure()
        }

        notificationService.showCommuteReminder(title, message)
        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "key_title"
        const val KEY_MESSAGE = "key_message"
    }
}
