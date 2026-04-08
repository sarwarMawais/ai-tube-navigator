package com.londontubeai.navigator.ml

import com.londontubeai.navigator.data.local.dao.TubeDao
import com.londontubeai.navigator.data.local.entity.SavedJourneyEntity
import com.londontubeai.navigator.data.model.AiInsight
import com.londontubeai.navigator.data.model.InsightType
import com.londontubeai.navigator.data.model.TubeData
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Personal Commute AI — learns from user journey history to provide
 * personalised commute insights, time-to-leave alerts, and route preferences.
 *
 * Analyses:
 * - Most frequent routes (home ↔ work detection)
 * - Preferred travel times
 * - Favourite stations
 * - Commute pattern regularity
 */
@Singleton
class PersonalCommuteAI @Inject constructor(
    private val dao: TubeDao,
) {
    data class CommuteProfile(
        val topRoutes: List<FrequentRoute>,
        val homeStation: String?,
        val workStation: String?,
        val preferredDepartureHour: Int?,
        val averageTripsPerWeek: Float,
        val favouriteLines: List<String>,
        val isRegularCommuter: Boolean,
    )

    data class FrequentRoute(
        val fromStationId: String,
        val fromStationName: String,
        val toStationId: String,
        val toStationName: String,
        val frequency: Int,
        val lastUsed: Long,
    )

    suspend fun analyseCommuteProfile(): CommuteProfile {
        val journeys = dao.getAllSavedJourneys().first()
        if (journeys.isEmpty()) {
            return CommuteProfile(
                topRoutes = emptyList(), homeStation = null, workStation = null,
                preferredDepartureHour = null, averageTripsPerWeek = 0f,
                favouriteLines = emptyList(), isRegularCommuter = false,
            )
        }

        val routeCounts = journeys
            .groupBy { "${it.fromStationId}_${it.toStationId}" }
            .mapValues { (_, trips) ->
                FrequentRoute(
                    fromStationId = trips.first().fromStationId,
                    fromStationName = trips.first().fromStationName,
                    toStationId = trips.first().toStationId,
                    toStationName = trips.first().toStationName,
                    frequency = trips.size,
                    lastUsed = trips.maxOf { it.timestamp },
                )
            }
            .values
            .sortedByDescending { it.frequency }

        val topRoutes = routeCounts.take(5)

        // Detect home/work stations from most frequent bidirectional routes
        val stationFrequency = mutableMapOf<String, Int>()
        journeys.forEach { j ->
            stationFrequency[j.fromStationId] = (stationFrequency[j.fromStationId] ?: 0) + 1
            stationFrequency[j.toStationId] = (stationFrequency[j.toStationId] ?: 0) + 1
        }
        val topStations = stationFrequency.entries.sortedByDescending { it.value }.map { it.key }
        val homeStation = topStations.getOrNull(0)
        val workStation = topStations.getOrNull(1)

        // Preferred departure hour from journey timestamps
        val hourCounts = journeys
            .map { Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(Calendar.HOUR_OF_DAY) }
            .groupBy { it }
            .mapValues { it.value.size }
        val preferredHour = hourCounts.maxByOrNull { it.value }?.key

        // Average trips per week
        val oldestTrip = journeys.minOf { it.timestamp }
        val weeksSpan = ((System.currentTimeMillis() - oldestTrip) / (7 * 24 * 60 * 60 * 1000.0)).coerceAtLeast(1.0)
        val tripsPerWeek = (journeys.size / weeksSpan).toFloat()

        // Favourite lines from station line IDs
        val lineCounts = mutableMapOf<String, Int>()
        journeys.forEach { j ->
            TubeData.getStationById(j.fromStationId)?.lineIds?.forEach { lineId ->
                lineCounts[lineId] = (lineCounts[lineId] ?: 0) + 1
            }
        }
        val favouriteLines = lineCounts.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }

        val isRegularCommuter = tripsPerWeek >= 3f && topRoutes.isNotEmpty() && topRoutes.first().frequency >= 5

        return CommuteProfile(
            topRoutes = topRoutes.toList(),
            homeStation = homeStation,
            workStation = workStation,
            preferredDepartureHour = preferredHour,
            averageTripsPerWeek = tripsPerWeek,
            favouriteLines = favouriteLines,
            isRegularCommuter = isRegularCommuter,
        )
    }

    suspend fun generatePersonalInsights(): List<AiInsight> {
        val profile = analyseCommuteProfile()
        val insights = mutableListOf<AiInsight>()

        if (profile.isRegularCommuter && profile.homeStation != null && profile.workStation != null) {
            val homeName = TubeData.getStationById(profile.homeStation)?.name ?: profile.homeStation
            val workName = TubeData.getStationById(profile.workStation)?.name ?: profile.workStation
            insights.add(
                AiInsight(
                    title = "Your Daily Commute",
                    description = "You regularly travel $homeName ↔ $workName (~${profile.averageTripsPerWeek.toInt()} trips/week). " +
                        "We've optimised route suggestions for this corridor.",
                    type = InsightType.TIME_SAVING,
                    actionLabel = "Plan commute",
                    priority = 9,
                    confidence = 0.92f,
                )
            )
        }

        if (profile.preferredDepartureHour != null) {
            val hourStr = String.format("%02d:00", profile.preferredDepartureHour)
            insights.add(
                AiInsight(
                    title = "Your Peak Travel Time",
                    description = "You usually travel around $hourStr. We'll prioritise crowd and delay predictions for this window.",
                    type = InsightType.CARRIAGE_TIP,
                    priority = 6,
                    confidence = 0.85f,
                )
            )
        }

        if (profile.topRoutes.isNotEmpty()) {
            val topRoute = profile.topRoutes.first()
            insights.add(
                AiInsight(
                    title = "Most Frequent Route",
                    description = "${topRoute.fromStationName} → ${topRoute.toStationName} (${topRoute.frequency} trips). Tap to plan this route instantly.",
                    type = InsightType.GENERAL,
                    actionLabel = "Plan route",
                    priority = 5,
                    confidence = 0.9f,
                )
            )
        }

        if (profile.favouriteLines.isNotEmpty()) {
            val lineNames = profile.favouriteLines.mapNotNull { TubeData.getLineById(it)?.name }.take(3)
            if (lineNames.isNotEmpty()) {
                insights.add(
                    AiInsight(
                        title = "Your Lines",
                        description = "You use the ${lineNames.joinToString(", ")} line${if (lineNames.size > 1) "s" else ""} most. We'll highlight disruptions on these lines first.",
                        type = InsightType.GENERAL,
                        priority = 3,
                        confidence = 0.88f,
                    )
                )
            }
        }

        return insights.sortedByDescending { it.priority }
    }
}
