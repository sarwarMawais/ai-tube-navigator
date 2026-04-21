package com.londontubeai.navigator.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.londontubeai.navigator.BuildConfig
import com.londontubeai.navigator.data.local.entity.CachedLineStatusEntity
import com.londontubeai.navigator.data.local.entity.SavedJourneyEntity
import com.londontubeai.navigator.data.model.AiInsight
import com.londontubeai.navigator.data.model.InsightType
import com.londontubeai.navigator.data.model.JourneyRoute
import com.londontubeai.navigator.data.model.LineStatus
import com.londontubeai.navigator.data.model.TransportMode
import com.londontubeai.navigator.data.model.TubeData
import com.londontubeai.navigator.data.preferences.AppPreferences
import com.londontubeai.navigator.data.remote.WeatherApiService
import com.londontubeai.navigator.data.remote.mapWeatherCodeToImpact
import com.londontubeai.navigator.data.repository.TubeRepository
import com.londontubeai.navigator.ml.PersonalCommuteAI
import com.londontubeai.navigator.utils.PermissionsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val lineStatuses: List<LineStatus> = emptyList(),
    val isLoadingStatus: Boolean = true,
    val statusError: String? = null,
    val aiInsights: List<AiInsight> = emptyList(),
    val lastUpdated: String? = null,
    val networkStationCount: Int = 0,
    val networkLineCount: Int = 0,
    val stepFreeCount: Int = 0,
    val serviceQualityScore: Float = 0f,
    val weatherInfo: WeatherInfo? = null,
    val commuteTimeEstimate: String? = null,
    /** 0–99 health score for the live commute: 100 = on-time + quiet, 0 = severe. `null` when unknown. */
    val commuteHealthScore: Int? = null,
    /** Extra delay in minutes vs. baseline commute time. Drives the health score and UI chip. */
    val commuteExtraDelayMinutes: Int? = null,
    val homeStationId: String? = null,
    val homeStationName: String? = null,
    val workStationId: String? = null,
    val workStationName: String? = null,
    val commuteDestinationId: String? = null,
    val commuteDestinationName: String? = null,
    val nearbyStationArrivals: List<NearbyArrival> = emptyList(),
    val nearbyStatusMessage: String = "Location permission required",
    val isNearbyUsingFallback: Boolean = false,
    val isNearbyLoading: Boolean = false,
    val isOutsideLondon: Boolean = false,
    val minorDelayCount: Int = 0,
    val severeDisruptionCount: Int = 0,
    val crowdLevel: CrowdLevel = CrowdLevel.forCurrentTime(),
    val commuterSnapshot: CommuterSnapshot? = null,
    val leaveNowAssistant: LeaveNowAssistant? = null,
    val fallbackRoutes: List<FallbackRouteOption> = emptyList(),
    val visitorLandmarks: List<VisitorLandmarkGuide> = emptyList(),
)

data class WeatherInfo(
    val temperature: Int,
    val condition: String,
    val icon: String,
    val impactLevel: ImpactLevel,
)

data class NearbyArrival(
    val stationName: String,
    val lineName: String,
    val destination: String,
    val minutesUntil: Int,
    val platform: String?,
    val distanceKm: Double? = null,
    val lineColor: androidx.compose.ui.graphics.Color? = null,
)

enum class ImpactLevel {
    LOW, MEDIUM, HIGH, EXTREME
}

data class CrowdLevel(
    val label: String,
    val level: String,
    val percentage: Float,
) {
    companion object {
        fun forCurrentTime(): CrowdLevel {
            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            return when (hour) {
                in 7..9 -> CrowdLevel("Morning Rush", "High", 0.85f)
                in 10..11 -> CrowdLevel("Late Morning", "Moderate", 0.45f)
                in 12..14 -> CrowdLevel("Midday", "Low", 0.30f)
                in 15..16 -> CrowdLevel("Afternoon", "Moderate", 0.50f)
                in 17..19 -> CrowdLevel("Evening Rush", "Very High", 0.92f)
                in 20..22 -> CrowdLevel("Evening", "Low", 0.25f)
                else -> CrowdLevel("Night", "Very Low", 0.10f)
            }
        }
    }
}

data class CommuterSnapshot(
    val isRegularCommuter: Boolean,
    val topRouteLabel: String?,
    val weeklyTripsLabel: String?,
    val preferredDepartureLabel: String?,
    val preferredDepartureHour: Int?,
    val favoriteLineNames: List<String>,
    val favoriteLineIds: List<String>,
    val disruptedFavoriteLines: Int,
)

data class LeaveNowAssistant(
    val title: String,
    val message: String,
    val actionLabel: String,
    val status: LeaveNowStatus,
)

enum class LeaveNowStatus {
    WAIT,
    SOON,
    NOW,
    LATE,
}

data class FallbackRouteOption(
    val title: String,
    val summary: String,
    val reason: String,
    val durationMinutes: Int,
    val interchanges: Int,
    val usesDisruptedLines: Boolean,
)

data class VisitorLandmarkGuide(
    val landmark: String,
    val stationName: String,
    val exitName: String,
    val lineNames: List<String>,
    val tip: String,
)

data class RankedInsight(
    val score: Int,
    val insight: AiInsight,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TubeRepository,
    private val locationService: com.londontubeai.navigator.utils.LocationService,
    private val weatherApi: WeatherApiService,
    private val prefs: AppPreferences,
    private val personalCommuteAI: PersonalCommuteAI,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var nearbyArrivalsJob: Job? = null

    val recentJourneys: StateFlow<List<SavedJourneyEntity>> =
        repository.getRecentJourneys(5)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadNetworkStats()
        loadLineStatuses()
        loadWeatherInfo()
        loadCommuteEstimate()
        loadNearbyArrivals()
        observeRecentJourneys()
        loadVisitorLandmarks()
    }

    private fun loadWeatherInfo() {
        viewModelScope.launch {
            locationService.getCurrentLocation()
                .onSuccess { location ->
                    try {
                        val response = weatherApi.getCurrentWeather(
                            latitude = location.latitude,
                            longitude = location.longitude,
                        )
                        val impact = mapWeatherCodeToImpact(response.current.weatherCode)
                        val condition = mapWeatherCodeToCondition(response.current.weatherCode)
                        val icon = mapWeatherCodeToIcon(response.current.weatherCode)
                        _uiState.value = _uiState.value.copy(
                            weatherInfo = WeatherInfo(
                                temperature = response.current.temperature.toInt(),
                                condition = condition,
                                icon = icon,
                                impactLevel = impact,
                            )
                        )
                        refreshPersonalizedHomeFeatures()
                        return@launch
                    } catch (e: Exception) {
                        Log.e("Weather", "API call failed: ${e.message}")
                    }
                    // Fallback: use London coords
                    try {
                        val response = weatherApi.getCurrentWeather(
                            latitude = 51.5074, longitude = -0.1278,
                        )
                        val impact = mapWeatherCodeToImpact(response.current.weatherCode)
                        val condition = mapWeatherCodeToCondition(response.current.weatherCode)
                        val icon = mapWeatherCodeToIcon(response.current.weatherCode)
                        _uiState.value = _uiState.value.copy(
                            weatherInfo = WeatherInfo(
                                temperature = response.current.temperature.toInt(),
                                condition = condition,
                                icon = icon,
                                impactLevel = impact,
                            )
                        )
                        refreshPersonalizedHomeFeatures()
                        return@launch
                    } catch (e: Exception) {
                        Log.e("Weather", "Fallback API call failed: ${e.message}")
                    }
                    setFallbackWeather()
                }
                .onFailure {
                    setFallbackWeather()
                }
        }
    }

    private fun mapWeatherCodeToCondition(code: Int): String = when (code) {
        0, 1 -> "Clear"
        2, 3 -> "Partly Cloudy"
        45, 48 -> "Fog"
        51, 53, 55, 56, 57 -> "Drizzle"
        61, 63, 65, 66, 67 -> "Rain"
        71, 73, 75, 77, 85, 86 -> "Snow"
        80, 81, 82 -> "Showers"
        95, 96, 99 -> "Thunderstorm"
        else -> "Clear"
    }

    private fun mapWeatherCodeToIcon(code: Int): String = when (code) {
        0, 1 -> "01d"  // Clear
        2, 3 -> "02d"  // Partly cloudy
        45, 48 -> "50d" // Fog
        51, 53, 55 -> "09d" // Drizzle
        56, 57 -> "13d" // Freezing drizzle
        61, 63 -> "10d" // Rain light
        65, 66, 67 -> "10d" // Rain heavy
        71, 73 -> "13d" // Snow light
        75, 77, 85, 86 -> "13d" // Snow heavy
        80, 81 -> "09d" // Rain showers
        82 -> "11d" // Thunderstorm showers
        95, 96, 99 -> "11d" // Thunderstorm
        else -> "01d"
    }

    private fun setFallbackWeather() {
        _uiState.value = _uiState.value.copy(
            weatherInfo = WeatherInfo(
                temperature = 18,
                condition = "Partly Cloudy",
                icon = "02d",
                impactLevel = ImpactLevel.LOW,
            )
        )
    }

    private fun loadCommuteEstimate() {
        viewModelScope.launch {
            val homeId = prefs.homeStationId.first()
            val workId = prefs.workStationId.first()
            val homeName = homeId?.let { TubeData.getStationById(it)?.name }
            val workName = workId?.let { TubeData.getStationById(it)?.name }
            val (commuteDestinationId, commuteDestinationName) = resolveCommuteDestination(homeId, workId)
            _uiState.value = _uiState.value.copy(
                homeStationId = homeId,
                homeStationName = homeName,
                workStationId = workId,
                workStationName = workName,
                commuteDestinationId = commuteDestinationId,
                commuteDestinationName = commuteDestinationName,
            )
            if (homeId != null && workId != null) {
                val route = repository.findRoute(homeId, workId)
                if (route != null) {
                    _uiState.value = _uiState.value.copy(
                        commuteTimeEstimate = "${route.totalDurationMinutes} min"
                    )
                }
            }
            refreshPersonalizedHomeFeatures()
        }
    }

    private fun observeRecentJourneys() {
        viewModelScope.launch {
            recentJourneys.collect {
                loadCommuterSnapshot()
            }
        }
    }

    private fun loadCommuterSnapshot() {
        viewModelScope.launch {
            val profile = personalCommuteAI.analyseCommuteProfile()
            if (profile.topRoutes.isEmpty() && profile.preferredDepartureHour == null && profile.favouriteLines.isEmpty()) {
                _uiState.value = _uiState.value.copy(commuterSnapshot = null)
                refreshPersonalizedHomeFeatures()
                return@launch
            }

            val favoriteLineNames = profile.favouriteLines
                .mapNotNull { TubeData.getLineById(it)?.name }
                .take(3)
            val disruptedFavoriteLines = _uiState.value.lineStatuses.count { status ->
                !status.isGoodService && status.lineId in profile.favouriteLines
            }

            _uiState.value = _uiState.value.copy(
                commuterSnapshot = CommuterSnapshot(
                    isRegularCommuter = profile.isRegularCommuter,
                    topRouteLabel = profile.topRoutes.firstOrNull()?.let { route ->
                        "${route.fromStationName} → ${route.toStationName}"
                    },
                    weeklyTripsLabel = profile.averageTripsPerWeek
                        .takeIf { it >= 1f }
                        ?.let { "${it.toInt().coerceAtLeast(1)} trips/week" },
                    preferredDepartureLabel = profile.preferredDepartureHour?.let { hour ->
                        String.format("%02d:00", hour)
                    },
                    preferredDepartureHour = profile.preferredDepartureHour,
                    favoriteLineNames = favoriteLineNames,
                    favoriteLineIds = profile.favouriteLines,
                    disruptedFavoriteLines = disruptedFavoriteLines,
                )
            )
            refreshPersonalizedHomeFeatures()
        }
    }

    private fun loadVisitorLandmarks() {
        val landmarkQueries = listOf(
            "Big Ben",
            "Buckingham Palace",
            "British Museum",
            "Tower of London",
            "Camden Market",
            "London Eye",
        )
        val guides = landmarkQueries.mapNotNull { landmark ->
            val station = TubeData.searchByLandmark(landmark).firstOrNull() ?: return@mapNotNull null
            val exit = station.exits.firstOrNull { stationExit ->
                stationExit.nearbyLandmarks.any { it.contains(landmark, ignoreCase = true) }
            } ?: station.exits.minByOrNull { it.walkingTimeSeconds } ?: return@mapNotNull null
            VisitorLandmarkGuide(
                landmark = landmark,
                stationName = station.name,
                exitName = exit.name,
                lineNames = station.lineIds.mapNotNull { TubeData.getLineById(it)?.name }.take(3),
                tip = buildString {
                    append(exit.description)
                    if (exit.bestCarriagePosition > 0) {
                        append(" · Best from carriage ")
                        append(exit.bestCarriagePosition)
                    }
                },
            )
        }.distinctBy { it.landmark }.take(4)
        _uiState.value = _uiState.value.copy(visitorLandmarks = guides)
    }

    private fun refreshPersonalizedHomeFeatures() {
        viewModelScope.launch {
            val homeId = prefs.homeStationId.first()
            val workId = prefs.workStationId.first()
            val statuses = _uiState.value.lineStatuses
            val snapshot = _uiState.value.commuterSnapshot
            val (commuteDestinationId, commuteDestinationName) = resolveCommuteDestination(homeId, workId)
            _uiState.value = _uiState.value.copy(
                commuteDestinationId = commuteDestinationId,
                commuteDestinationName = commuteDestinationName,
                leaveNowAssistant = buildLeaveNowAssistant(homeId, workId, snapshot, statuses),
                fallbackRoutes = buildFallbackRoutes(homeId, workId, snapshot, statuses),
            )
            refreshInsights()
        }
    }

    private fun refreshInsights() {
        val statuses = _uiState.value.lineStatuses
        if (statuses.isEmpty()) {
            _uiState.value = _uiState.value.copy(aiInsights = emptyList())
            return
        }
        _uiState.value = _uiState.value.copy(aiInsights = buildRankedInsights(statuses))
    }

    private fun buildRankedInsights(statuses: List<LineStatus>): List<AiInsight> {
        val state = _uiState.value
        val now = System.currentTimeMillis()
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val recent = recentJourneys.value.take(4)
        val relevantLineIds = mutableSetOf<String>().apply {
            addAll(state.commuterSnapshot?.favoriteLineIds.orEmpty())
            addAll(lineIdsForStation(state.homeStationId))
            addAll(lineIdsForStation(state.workStationId))
            recent.forEach { journey ->
                addAll(lineIdsForStation(journey.fromStationId))
                addAll(lineIdsForStation(journey.toStationId))
            }
        }
        val relevantLineNames = relevantLineIds
            .mapNotNull { lineId -> TubeData.getLineById(lineId)?.name }
            .toSet()
        val relevantStationNames = mutableSetOf<String>().apply {
            state.homeStationName?.let(::add)
            state.workStationName?.let(::add)
            addAll(state.nearbyStationArrivals.map { it.stationName })
            recent.flatMapTo(this) { listOf(it.fromStationName, it.toStationName) }
        }
        val baseInsights = repository.generateInsights(statuses)
        val rankedInsights = mutableListOf<RankedInsight>()

        if (state.commuteDestinationId != null && state.commuteDestinationName != null && state.leaveNowAssistant != null) {
            val commuteIsLive = state.leaveNowAssistant.status != LeaveNowStatus.WAIT
            rankedInsights += RankedInsight(
                score = 2_000,
                insight = AiInsight(
                    title = state.leaveNowAssistant.title,
                    description = state.leaveNowAssistant.message,
                    type = InsightType.PERSONAL_ROUTE,
                    actionLabel = "Open commute",
                    priority = 12,
                    confidence = 0.95f,
                    isPersonalized = true,
                    isLive = commuteIsLive,
                    expiresAt = now + 45 * 60_000L,
                    metadata = mapOf(
                        "freshnessLabel" to if (commuteIsLive) "Live commute window" else "Based on your routine",
                    ),
                    userImpact = when (state.leaveNowAssistant.status) {
                        LeaveNowStatus.LATE -> com.londontubeai.navigator.data.model.UserImpact.CRITICAL
                        LeaveNowStatus.NOW, LeaveNowStatus.SOON -> com.londontubeai.navigator.data.model.UserImpact.HIGH
                        LeaveNowStatus.WAIT -> com.londontubeai.navigator.data.model.UserImpact.MEDIUM
                    },
                ),
            )
        }

        state.nearbyStationArrivals.firstOrNull()?.let { nearby ->
            rankedInsights += RankedInsight(
                score = 1_250,
                insight = AiInsight(
                    title = "Near you: ${nearby.stationName}",
                    description = "${nearby.lineName} to ${nearby.destination} is ${if (nearby.minutesUntil <= 0) "due now" else "${nearby.minutesUntil} min away"} from your current area.",
                    type = InsightType.STATION_HABIT,
                    actionLabel = "See nearby",
                    priority = 9,
                    confidence = 0.87f,
                    isPersonalized = true,
                    isLive = !state.isNearbyUsingFallback,
                    expiresAt = now + 12 * 60_000L,
                    metadata = mapOf(
                        "freshnessLabel" to if (state.isNearbyUsingFallback) "Cached nearby" else "Live nearby",
                    ),
                    userImpact = com.londontubeai.navigator.data.model.UserImpact.MEDIUM,
                ),
            )
        }

        baseInsights.forEach { insight ->
            val matchesRelevantLines = relevantLineNames.any { lineName ->
                insight.title.contains(lineName, ignoreCase = true) ||
                    insight.description.contains(lineName, ignoreCase = true)
            }
            val matchesRelevantStations = relevantStationNames.any { stationName ->
                insight.title.contains(stationName, ignoreCase = true) ||
                    insight.description.contains(stationName, ignoreCase = true)
            }
            var score = (insight.priority * 100) + (insight.confidence * 100).toInt()
            var isPersonalized = insight.isPersonalized
            var impact = insight.userImpact
            if (matchesRelevantLines) {
                score += 220
                isPersonalized = true
                impact = maxImpact(
                    impact,
                    if (insight.type == InsightType.DELAY_WARNING || insight.type == InsightType.DISRUPTION_IMPACT) {
                        com.londontubeai.navigator.data.model.UserImpact.CRITICAL
                    } else {
                        com.londontubeai.navigator.data.model.UserImpact.HIGH
                    },
                )
            }
            if (matchesRelevantStations) {
                score += 140
                isPersonalized = true
                impact = maxImpact(impact, com.londontubeai.navigator.data.model.UserImpact.HIGH)
            }
            if (state.commuteDestinationName != null && insight.type in setOf(
                    InsightType.TIME_SAVING,
                    InsightType.TIME_PREFERENCE,
                    InsightType.SAVINGS_OPPORTUNITY,
                )
            ) {
                score += 120
                isPersonalized = true
                impact = maxImpact(impact, com.londontubeai.navigator.data.model.UserImpact.MEDIUM)
            }
            if (currentHour in 7..9 || currentHour in 17..19) {
                if (insight.type == InsightType.CROWD_ALERT) {
                    score += 180
                }
                if (insight.type == InsightType.DELAY_WARNING) {
                    score += 80
                }
            }
            val expiresAt = insight.expiresAt ?: when (insight.type) {
                InsightType.DELAY_WARNING,
                InsightType.DISRUPTION_IMPACT,
                -> now + 15 * 60_000L
                InsightType.CROWD_ALERT -> now + 25 * 60_000L
                InsightType.TIME_SAVING,
                InsightType.TIME_PREFERENCE,
                InsightType.SAVINGS_OPPORTUNITY,
                -> now + 60 * 60_000L
                else -> null
            }
            val isLive = insight.isLive || insight.type in setOf(
                InsightType.DELAY_WARNING,
                InsightType.DISRUPTION_IMPACT,
                InsightType.CROWD_ALERT,
            )
            val freshnessLabel = when {
                isLive && expiresAt != null -> {
                    val minutesLeft = ((expiresAt - now) / 60_000L).coerceAtLeast(1L)
                    "Live · ${minutesLeft}m window"
                }
                expiresAt != null -> {
                    val minutesLeft = ((expiresAt - now) / 60_000L).coerceAtLeast(1L)
                    "Time-sensitive · ${minutesLeft}m left"
                }
                else -> "Fresh today"
            }
            rankedInsights += RankedInsight(
                score = score,
                insight = insight.copy(
                    isPersonalized = isPersonalized,
                    isLive = isLive,
                    expiresAt = expiresAt,
                    metadata = insight.metadata + mapOf("freshnessLabel" to freshnessLabel),
                    userImpact = impact,
                ),
            )
        }

        return rankedInsights
            .distinctBy { "${it.insight.title}|${it.insight.type}" }
            .sortedByDescending { it.score }
            .map { it.insight }
    }

    private fun lineIdsForStation(stationId: String?): Set<String> {
        return stationId
            ?.takeUnless { it.startsWith("my-location") || it.startsWith("place:") }
            ?.let { TubeData.getStationById(it)?.lineIds?.toSet() }
            .orEmpty()
    }

    private fun maxImpact(
        current: com.londontubeai.navigator.data.model.UserImpact,
        candidate: com.londontubeai.navigator.data.model.UserImpact,
    ): com.londontubeai.navigator.data.model.UserImpact {
        val order = listOf(
            com.londontubeai.navigator.data.model.UserImpact.LOW,
            com.londontubeai.navigator.data.model.UserImpact.MEDIUM,
            com.londontubeai.navigator.data.model.UserImpact.HIGH,
            com.londontubeai.navigator.data.model.UserImpact.CRITICAL,
        )
        return if (order.indexOf(candidate) > order.indexOf(current)) candidate else current
    }

    private fun resolveCommuteDestination(homeId: String?, workId: String?): Pair<String?, String?> {
        if (homeId == null || workId == null) return null to null
        val cal = java.util.Calendar.getInstance()
        val dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)
        val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val isWeekend = dayOfWeek == java.util.Calendar.SATURDAY || dayOfWeek == java.util.Calendar.SUNDAY

        // Prefer the direction implied by the user's most recent journey, if it matches the
        // home/work pair — this handles shift-workers and non-9-to-5 routines.
        val recentDirection = recentJourneys.value.firstOrNull { journey ->
            (journey.fromStationId == homeId && journey.toStationId == workId) ||
                (journey.fromStationId == workId && journey.toStationId == homeId)
        }
        val recentWasHomeToWork = recentDirection?.let { it.fromStationId == homeId && it.toStationId == workId }

        // Weekends: default to home (people rarely commute to work).
        // Weekdays: morning window (04:00-14:00) = to work, else back home.
        // If last known journey direction disagrees with the time-of-day guess AND was within
        // the last 12h, trust the recent journey's opposite (user returning).
        val defaultsTowardsWork = !isWeekend && hour in 4..13
        val recentAge = recentDirection?.let { System.currentTimeMillis() - it.timestamp }
        val destinationId = when {
            recentWasHomeToWork == true && recentAge != null && recentAge < 12 * 3_600_000L -> homeId
            recentWasHomeToWork == false && recentAge != null && recentAge < 12 * 3_600_000L -> workId
            defaultsTowardsWork -> workId
            else -> homeId
        }
        val destinationName = TubeData.getStationById(destinationId)?.name
        return destinationId to destinationName
    }

    private fun buildLeaveNowAssistant(
        homeId: String?,
        workId: String?,
        snapshot: CommuterSnapshot?,
        statuses: List<LineStatus>,
    ): LeaveNowAssistant? {
        if (homeId == null || workId == null) return null
        val now = java.util.Calendar.getInstance()
        val currentHour = now.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(java.util.Calendar.MINUTE)
        val currentMinutes = currentHour * 60 + currentMinute
        val preferredHour = snapshot?.preferredDepartureHour ?: when (currentHour) {
            in 5..11 -> 8
            in 12..20 -> 17
            else -> return null
        }
        val severeCount = statuses.count { !it.isGoodService && it.statusSeverity < 6 }
        val favouriteDisruptionCount = snapshot?.disruptedFavoriteLines ?: 0
        val extraBufferMinutes = when {
            favouriteDisruptionCount > 0 -> 6 + (favouriteDisruptionCount * 2)
            severeCount > 0 -> 4
            else -> 0
        }
        val recommendedDepartureMinutes = (preferredHour * 60) - extraBufferMinutes
        val deltaMinutes = recommendedDepartureMinutes - currentMinutes
        val directionLabel = if (currentHour >= 15) {
            "${_uiState.value.workStationName ?: "Work"} → ${_uiState.value.homeStationName ?: "Home"}"
        } else {
            "${_uiState.value.homeStationName ?: "Home"} → ${_uiState.value.workStationName ?: "Work"}"
        }
        val contextMessage = when {
            favouriteDisruptionCount > 0 -> "$favouriteDisruptionCount of your regular lines have disruption risk."
            severeCount > 0 -> "Network disruption may slow your usual corridor."
            else -> "Your regular corridor is looking steady right now."
        }
        return when {
            deltaMinutes > 15 -> LeaveNowAssistant(
                title = "Leave in ${deltaMinutes} min",
                message = "Aim for ${formatClockTime(recommendedDepartureMinutes)} for $directionLabel. $contextMessage",
                actionLabel = "Open commute",
                status = LeaveNowStatus.WAIT,
            )
            deltaMinutes in 1..15 -> LeaveNowAssistant(
                title = "Almost time to leave",
                message = "Your best departure window starts in ${deltaMinutes} min for $directionLabel. $contextMessage",
                actionLabel = "Get ready",
                status = LeaveNowStatus.SOON,
            )
            deltaMinutes in -4..0 -> LeaveNowAssistant(
                title = "Leave now",
                message = "You are right on time for your usual journey window on $directionLabel. $contextMessage",
                actionLabel = "Start route",
                status = LeaveNowStatus.NOW,
            )
            else -> LeaveNowAssistant(
                title = "Running ${kotlin.math.abs(deltaMinutes)} min late",
                message = "Open your commute now for the quickest available option on $directionLabel. $contextMessage",
                actionLabel = "Find best option",
                status = LeaveNowStatus.LATE,
            )
        }
    }

    private fun buildFallbackRoutes(
        homeId: String?,
        workId: String?,
        snapshot: CommuterSnapshot?,
        statuses: List<LineStatus>,
    ): List<FallbackRouteOption> {
        if (homeId == null || workId == null) return emptyList()
        val disruptedLineIds = statuses.filter { !it.isGoodService }.map { it.lineId }.toSet()
        val priorityDisruptedLines = snapshot?.favoriteLineIds?.filter { it in disruptedLineIds }?.toSet().orEmpty()
        val relevantDisruptedLines = if (priorityDisruptedLines.isNotEmpty()) priorityDisruptedLines else disruptedLineIds
        if (relevantDisruptedLines.isEmpty()) return emptyList()

        val primaryRoute = repository.findRoute(homeId, workId, "FASTEST")
        val primarySignature = primaryRoute?.let { routeSignature(it) }
        val primaryDisruptedCount = primaryRoute?.let { routeDisruptedLineCount(it, relevantDisruptedLines) } ?: Int.MAX_VALUE

        return listOf(
            "FEWEST_CHANGES" to "Fewer changes",
            "LEAST_WALKING" to "Less walking",
            "STEP_FREE" to "Step-free option",
            "FASTEST" to "Fastest backup",
        ).mapNotNull { (preference, title) ->
            val route = repository.findRoute(homeId, workId, preference) ?: return@mapNotNull null
            val signature = routeSignature(route)
            if (signature == primarySignature && preference != "FASTEST") return@mapNotNull null
            val disruptedCount = routeDisruptedLineCount(route, relevantDisruptedLines)
            val lineNames = route.legs
                .filter { it.mode == TransportMode.TUBE }
                .map { it.line.name }
                .distinct()
                .take(3)
            FallbackRouteOption(
                title = title,
                summary = "${route.totalDurationMinutes} min · ${route.totalInterchanges} change${if (route.totalInterchanges == 1) "" else "s"} · ${lineNames.joinToString(" → ")}",
                reason = when {
                    disruptedCount == 0 && primaryDisruptedCount > 0 -> "Avoids the disrupted lines on your usual route"
                    disruptedCount < primaryDisruptedCount -> "Touches fewer disrupted lines than your usual route"
                    route.totalWalkingMinutes <= 4 -> "Simple backup with limited walking"
                    else -> "Useful fallback if platforms feel busy"
                },
                durationMinutes = route.totalDurationMinutes,
                interchanges = route.totalInterchanges,
                usesDisruptedLines = disruptedCount > 0,
            )
        }
            .distinctBy { it.summary }
            .sortedWith(compareBy<FallbackRouteOption> { it.usesDisruptedLines }.thenBy { it.durationMinutes }.thenBy { it.interchanges })
            .take(2)
    }

    private fun routeSignature(route: JourneyRoute): String {
        return route.legs
            .filter { it.mode == TransportMode.TUBE }
            .joinToString("|") { "${it.line.id}:${it.fromStation.id}-${it.toStation.id}" }
    }

    private fun routeDisruptedLineCount(route: JourneyRoute, disruptedLines: Set<String>): Int {
        return route.legs
            .filter { it.mode == TransportMode.TUBE }
            .map { it.line.id }
            .distinct()
            .count { it in disruptedLines }
    }

    private fun formatClockTime(totalMinutes: Int): String {
        val safeMinutes = totalMinutes.coerceIn(0, (23 * 60) + 59)
        val hour = safeMinutes / 60
        val minute = safeMinutes % 60
        return String.format("%02d:%02d", hour, minute)
    }

    companion object {
        // Greater London bounding box (generous to cover all TfL stations)
        private const val LONDON_LAT_MIN = 51.28
        private const val LONDON_LAT_MAX = 51.69
        private const val LONDON_LON_MIN = -0.51
        private const val LONDON_LON_MAX = 0.33
        // Max distance to nearest station to show arrivals (km) — ~3 miles
        private const val MAX_NEARBY_DISTANCE_KM = 4.8
    }

    private fun isInLondon(latitude: Double, longitude: Double): Boolean {
        return latitude in LONDON_LAT_MIN..LONDON_LAT_MAX &&
                longitude in LONDON_LON_MIN..LONDON_LON_MAX
    }

    private fun loadNearbyArrivals() {
        nearbyArrivalsJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isNearbyLoading = true,
            nearbyStatusMessage = "Finding nearby stations...",
            isOutsideLondon = false,
        )
        nearbyArrivalsJob = viewModelScope.launch {
            locationService.getCurrentLocation()
                .onSuccess { location ->
                    // Check if user is within London
                    if (!isInLondon(location.latitude, location.longitude)) {
                        _uiState.value = _uiState.value.copy(
                            nearbyStationArrivals = emptyList(),
                            isNearbyLoading = false,
                            isOutsideLondon = true,
                            isNearbyUsingFallback = false,
                            nearbyStatusMessage = "You're not in London",
                        )
                        return@launch
                    }

                    // Find nearest stations based on user's actual location
                    val allStations = TubeData.getAllStationsSorted()
                    val nearestStations = allStations
                        .map { station ->
                            val distance = locationService.calculateDistance(
                                location.latitude,
                                location.longitude,
                                station.latitude,
                                station.longitude
                            )
                            station to distance
                        }
                        .sortedBy { it.second }
                        .take(5)
                        .filter { it.second < MAX_NEARBY_DISTANCE_KM }

                    if (nearestStations.isEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            nearbyStationArrivals = emptyList(),
                            isNearbyLoading = false,
                            nearbyStatusMessage = "No stations within ${MAX_NEARBY_DISTANCE_KM.toInt()}km",
                            isNearbyUsingFallback = false,
                        )
                        return@launch
                    }

                    val stationResults = nearestStations
                        .map { (station, distance) ->
                            async {
                                repository.fetchStationArrivals(station.id)
                                    .getOrElse { error ->
                                        Log.e("NearbyArrivals", "Failed for ${station.name}: ${error.message}")
                                        null
                                    }
                                    ?.let { stationArrivals -> Triple(station, distance, stationArrivals) }
                            }
                        }
                        .awaitAll()
                        .filterNotNull()

                    val arrivals = stationResults.flatMap { (station, distance, stationArrivals) ->
                        stationArrivals.arrivals
                            .filter { arrival ->
                                arrival.lineId in station.lineIds && arrival.timeToStationMinutes >= 0
                            }
                            .groupBy { arrival -> arrival.lineId }
                            .values
                            .mapNotNull { lineArrivals ->
                                lineArrivals.minByOrNull { arrival -> arrival.timeToStationSeconds }
                            }
                            .sortedBy { arrival -> arrival.timeToStationMinutes }
                            .take(3)
                            .map { arrival ->
                                NearbyArrival(
                                    stationName = station.name,
                                    lineName = arrival.lineName,
                                    destination = arrival.destination,
                                    minutesUntil = arrival.timeToStationMinutes,
                                    platform = arrival.platform,
                                    distanceKm = distance,
                                    lineColor = arrival.lineColor,
                                )
                            }
                    }
                    val isUsingFallback = stationResults.any { (_, _, stationArrivals) -> stationArrivals.isCached }

                    // Sort by arrival time, take top results
                    val sortedArrivals = arrivals
                        .distinctBy { arrival ->
                            "${arrival.stationName}-${arrival.lineName}-${arrival.destination}-${arrival.platform}-${arrival.minutesUntil}"
                        }
                        .sortedBy { it.minutesUntil }
                        .take(6)

                    _uiState.value = _uiState.value.copy(
                        nearbyStationArrivals = sortedArrivals,
                        isNearbyLoading = false,
                        nearbyStatusMessage = when {
                            sortedArrivals.isNotEmpty() && isUsingFallback -> "Cached arrivals near you"
                            sortedArrivals.isNotEmpty() -> "Live arrivals near you"
                            isUsingFallback -> "No cached arrivals available right now"
                            else -> "No live arrivals available right now"
                        },
                        isNearbyUsingFallback = isUsingFallback,
                        isOutsideLondon = false,
                    )
                    refreshInsights()
                }
                .onFailure { error ->
                    Log.e("NearbyArrivals", "Location failed: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        nearbyStationArrivals = emptyList(),
                        isNearbyLoading = false,
                        nearbyStatusMessage = "Unable to get your location",
                        isNearbyUsingFallback = false,
                    )
                    refreshInsights()
                }
        }
    }

    fun retryNearbyArrivals() {
        loadNearbyArrivals()
    }

    fun refreshHomeData() {
        loadLineStatuses()
        loadNearbyArrivals()
        loadWeatherInfo()
    }

    // ── Silent auto-refresh loop ───────────────────────────────────────────────
    private var screenActive = false
    private var autoRefreshJob: Job? = null

    fun setScreenActive(active: Boolean) {
        if (screenActive == active) return
        screenActive = active
        if (active) startAutoRefreshLoop() else autoRefreshJob?.cancel()
    }

    private fun startAutoRefreshLoop() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (screenActive) {
                kotlinx.coroutines.delay(60_000L) // refresh every 60 s
                if (!screenActive) break
                // Silent: no loading spinners, just swap data when responses return.
                loadLineStatuses()
                loadNearbyArrivals()
            }
        }
    }

    private fun loadNetworkStats() {
        val stats = TubeData.getNetworkStats()
        _uiState.value = _uiState.value.copy(
            networkStationCount = stats.totalStations,
            networkLineCount = stats.totalLines,
            stepFreeCount = stats.stationsWithStepFree,
        )
    }

    fun loadLineStatuses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingStatus = true, statusError = null)

            repository.fetchLiveLineStatuses()
                .onSuccess { statuses ->
                    val lastStatusUpdate = repository.getLastStatusUpdate()
                    _uiState.value = _uiState.value.copy(
                        lineStatuses = statuses,
                        isLoadingStatus = false,
                        lastUpdated = formatStatusUpdatedLabel(lastStatusUpdate, isCached = false),
                    )
                    computeDerivedStats(statuses)
                }
                .onFailure {
                    val cached = loadCachedStatuses()
                    val lastStatusUpdate = repository.getLastStatusUpdate()
                    _uiState.value = _uiState.value.copy(
                        lineStatuses = cached,
                        isLoadingStatus = false,
                        statusError = if (cached.isEmpty()) "Unable to fetch line status. Check your connection." else null,
                        lastUpdated = if (cached.isNotEmpty()) formatStatusUpdatedLabel(lastStatusUpdate, isCached = true) else null,
                    )
                    computeDerivedStats(cached)
                }
        }
    }

    private fun computeDerivedStats(statuses: List<LineStatus>) {
        if (statuses.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                serviceQualityScore = 0f,
                minorDelayCount = 0,
                severeDisruptionCount = 0,
                aiInsights = emptyList(),
            )
            refreshPersonalizedHomeFeatures()
            return
        }
        val good = statuses.count { it.isGoodService }
        // TfL severity: 10 = Good, 6-9 = Minor, 0-5 = Severe/Suspended
        val minor = statuses.count { !it.isGoodService && it.statusSeverity in 6..9 }
        val severe = statuses.count { !it.isGoodService && it.statusSeverity < 6 }
        val quality = (good.toFloat() / statuses.size) * 100f

        val commuterSnapshot = _uiState.value.commuterSnapshot?.let { snapshot ->
            snapshot.copy(
                disruptedFavoriteLines = statuses.count { status ->
                    !status.isGoodService && status.lineId in snapshot.favoriteLineIds
                }
            )
        }

        _uiState.value = _uiState.value.copy(
            serviceQualityScore = quality,
            minorDelayCount = minor,
            severeDisruptionCount = severe,
            crowdLevel = CrowdLevel.forCurrentTime(),
            commuterSnapshot = commuterSnapshot,
        )
        updateCommuteEstimate(statuses)
        refreshPersonalizedHomeFeatures()
    }

    private fun updateCommuteEstimate(statuses: List<LineStatus>) {
        viewModelScope.launch {
            val homeId = prefs.homeStationId.first()
            val workId = prefs.workStationId.first()
            if (homeId != null && workId != null) {
                val route = repository.findRoute(homeId, workId)
                if (route != null) {
                    // Only count disruptions on lines actually used by the commute route.
                    val routeLineIds = route.legs
                        .filter { it.mode == TransportMode.TUBE }
                        .map { it.line.id }
                        .toSet()
                    val routeDisruptedSeverities = statuses
                        .filter { !it.isGoodService && it.lineId in routeLineIds }
                        .map { it.statusSeverity }
                    val delay: Int = routeDisruptedSeverities.sumOf { severity ->
                        when {
                            severity < 4 -> 8 // Severe / Part Suspended
                            severity < 7 -> 4 // Minor Delays
                            else -> 2         // Reduced Service
                        } as Int
                    }
                    // Phase D: derive a 0–100 commute-health score factoring in disruption delay
                    // AND current crowd level so the user can see at a glance whether now is a
                    // good time to travel.
                    val crowdPenalty = (_uiState.value.crowdLevel.percentage * 25f).toInt()
                    val delayPenalty = (delay * 3).coerceAtMost(60)
                    val score = (100 - delayPenalty - crowdPenalty).coerceIn(0, 100)
                    _uiState.value = _uiState.value.copy(
                        commuteTimeEstimate = "${route.totalDurationMinutes + delay} min",
                        commuteHealthScore = score,
                        commuteExtraDelayMinutes = delay,
                    )
                    return@launch
                }
            }
            // Fallback if no home/work set: show network-wide qualitative estimate.
            val severeCount = statuses.count { !it.isGoodService && it.statusSeverity < 6 }
            val baseTime = 25
            _uiState.value = _uiState.value.copy(
                commuteTimeEstimate = "${baseTime + severeCount * 3} min"
            )
        }
    }

    private fun formatStatusUpdatedLabel(lastUpdatedMillis: Long?, isCached: Boolean): String {
        val prefix = if (isCached) "Cached" else "Updated"
        if (lastUpdatedMillis == null) return if (isCached) "Cached" else "Just now"
        val ageMinutes = ((System.currentTimeMillis() - lastUpdatedMillis) / 60_000L).coerceAtLeast(0L)
        return when {
            !isCached && ageMinutes == 0L -> "Just now"
            ageMinutes == 0L -> "$prefix just now"
            ageMinutes < 60L -> "$prefix ${ageMinutes}m ago"
            else -> "$prefix ${ageMinutes / 60L}h ago"
        }
    }

    fun onPermissionsChanged(permissionsState: PermissionsState) {
        if (permissionsState.locationPermissionGranted) {
            loadNearbyArrivals()
        } else {
            _uiState.value = _uiState.value.copy(
                nearbyStationArrivals = emptyList(),
                isNearbyUsingFallback = false,
                isNearbyLoading = false,
                nearbyStatusMessage = "Location permission required",
            )
        }
    }

    private suspend fun loadCachedStatuses(): List<LineStatus> =
        repository.getCachedLineStatuses().first().map { it.toLineStatus() }

    private fun CachedLineStatusEntity.toLineStatus(): LineStatus {
        val tubeColor = TubeData.getLineById(lineId)?.color
            ?: com.londontubeai.navigator.ui.theme.TubeLineColors.Jubilee
        return LineStatus(
            lineId = lineId,
            lineName = lineName,
            lineColor = tubeColor,
            statusSeverity = statusSeverity,
            statusDescription = statusDescription,
            reason = reason,
            isGoodService = statusSeverity >= 10,
        )
    }
}
