package com.londontubeai.navigator.data.notifications

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.londontubeai.navigator.data.model.TubeData
import com.londontubeai.navigator.data.preferences.AppPreferences
import com.londontubeai.navigator.data.remote.TflApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar

@HiltWorker
class DisruptionCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val api: TflApiService,
    private val prefs: AppPreferences,
    private val notificationService: TubeNotificationService,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "disruption_check"
        private const val TAG = "DisruptionCheck"
    }

    override suspend fun doWork(): Result {
        return try {
            // Check quiet hours first — applies to all notification types
            val quietHours = prefs.quietHours.first()
            if (quietHours && isQuietHours()) {
                Log.d(TAG, "Quiet hours active, skipping")
                return Result.success()
            }

            val disruptionsEnabled = prefs.pushDisruptions.first()
            val commuteEnabled = prefs.pushCommute.first()

            if (!disruptionsEnabled && !commuteEnabled) {
                Log.d(TAG, "All disruption alerts disabled, skipping")
                return Result.success()
            }

            // Fetch live line statuses once
            val statuses = api.getAllLineStatuses()
            val severeOnly = prefs.severeOnly.first()

            // Helper: is a line API response disrupted?
            fun isDisrupted(line: com.londontubeai.navigator.data.remote.TflLineStatusResponse): Boolean {
                return line.lineStatuses.any { status ->
                    if (severeOnly) status.statusSeverity < 6 else status.statusSeverity < 10
                }
            }

            // ── 1. Commute line alerts ───────────────────────────────────────
            if (commuteEnabled) {
                val homeId = prefs.homeStationId.first()
                val workId = prefs.workStationId.first()
                val commuteLineIds: Set<String> = if (homeId != null && workId != null) {
                    // Derive lines from the stations' known line associations
                    val homeLines = TubeData.getStationById(homeId)?.lineIds?.toSet() ?: emptySet()
                    val workLines = TubeData.getStationById(workId)?.lineIds?.toSet() ?: emptySet()
                    // Lines that serve both home and work are the commute lines
                    val shared = homeLines.intersect(workLines)
                    if (shared.isNotEmpty()) shared else homeLines.union(workLines)
                } else {
                    emptySet()
                }

                if (commuteLineIds.isNotEmpty()) {
                    val commuteDisrupted = statuses.filter { line ->
                        line.id in commuteLineIds && isDisrupted(line)
                    }
                    if (commuteDisrupted.isNotEmpty()) {
                        val lineNames = commuteDisrupted.map { it.name }.take(2)
                        val reason = commuteDisrupted.firstOrNull()
                            ?.lineStatuses?.firstOrNull { it.statusSeverity < 10 }?.reason
                        val title = "⚠️ Your commute is affected"
                        val message = reason
                            ?: "${lineNames.joinToString(", ")} ${if (commuteDisrupted.size == 1) "is" else "are"} disrupted"
                        notificationService.showCommuteReminder(title, message)
                        Log.d(TAG, "Sent commute disruption alert: ${commuteDisrupted.size} lines")
                    }
                }
            }

            // ── 2. Favourite / general disruption alerts ─────────────────────
            if (disruptionsEnabled) {
                val favRaw = prefs.favouriteLines.first()
                val favLines = if (favRaw.isBlank()) emptySet()
                else favRaw.split(",").filter { it.isNotBlank() }.toSet()

                val disrupted = statuses.filter { isDisrupted(it) }

                val relevant = if (favLines.isNotEmpty()) {
                    disrupted.filter { line ->
                        val id = line.id ?: ""
                        val name = line.name?.lowercase() ?: ""
                        id in favLines || name in favLines.map { it.lowercase() }
                    }
                } else {
                    disrupted
                }

                if (relevant.isNotEmpty()) {
                    val lineNames = relevant.mapNotNull { it.name }.take(3)
                    val reason = relevant.firstOrNull()
                        ?.lineStatuses?.firstOrNull { it.statusSeverity < 10 }?.reason
                    val title = if (lineNames.size == 1) lineNames.first()
                    else "${lineNames.first()} +${lineNames.size - 1} more"
                    val description = reason
                        ?: "${relevant.size} line${if (relevant.size > 1) "s" else ""} disrupted"
                    notificationService.showDisruptionAlert(lineName = title, description = description)
                    Log.d(TAG, "Sent general disruption alert for ${relevant.size} lines")
                } else {
                    Log.d(TAG, "No relevant disruptions found")
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Worker failed: ${e.message}")
            Result.retry()
        }
    }

    private fun isQuietHours(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour >= 22 || hour < 7
    }
}
