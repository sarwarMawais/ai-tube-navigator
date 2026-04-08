package com.londontubeai.navigator.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.londontubeai.navigator.BuildConfig
import com.londontubeai.navigator.data.local.entity.CachedLineStatusEntity
import com.londontubeai.navigator.data.local.entity.SavedJourneyEntity
import com.londontubeai.navigator.data.model.AiInsight
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
    val homeStationName: String? = null,
    val workStationName: String? = null,
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
                        _uiState.value = _uiState.value.copy(
                            weatherInfo = WeatherInfo(
                                temperature = response.current.temperature.toInt(),
                                condition = condition,
                                icon = "01d", // OpenMeteo doesn't provide icons
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
                        _uiState.value = _uiState.value.copy(
                            weatherInfo = WeatherInfo(
                                temperature = response.current.temperature.toInt(),
                                condition = condition,
                                icon = "01d",
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
        2, 3 -> "Cloudy"
        45, 48 -> "Fog"
        51, 53, 55, 56, 57 -> "Drizzle"
        61, 63, 65, 66, 67 -> "Rain"
        71, 73, 75, 77, 85, 86 -> "Snow"
        80, 81, 82 -> "Showers"
        95, 96, 99 -> "Thunderstorm"
        else -> "Clear"
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
            _uiState.value = _uiState.value.copy(
                homeStationName = homeName,
                workStationName = workName,
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
            _uiState.value = _uiState.value.copy(
                leaveNowAssistant = buildLeaveNowAssistant(homeId, workId, snapshot, statuses),
                fallbackRoutes = buildFallbackRoutes(homeId, workId, snapshot, statuses),
            )
        }
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
        // Max distance to nearest station to show arrivals (km)
        private const val MAX_NEARBY_DISTANCE_KM = 3.0
    }

    private fun isInLondon(latitude: Double, longitude: Double): Boolean {
        return latitude in LONDON_LAT_MIN..LONDON_LAT_MAX &&
                longitude in LONDON_LON_MIN..LONDON_LON_MAX
    }

    private fun loadNearbyArrivals() {
        Log.d("NearbyArrivals", "Starting nearby arrivals loading...")
        _uiState.value = _uiState.value.copy(
            isNearbyLoading = true,
            nearbyStatusMessage = "Finding nearby stations...",
            isOutsideLondon = false,
        )
        viewModelScope.launch {
            locationService.getCurrentLocation()
                .onSuccess { location ->
                    Log.d("NearbyArrivals", "Location: ${location.latitude}, ${location.longitude}")

                    // Check if user is within London
                    if (!isInLondon(location.latitude, location.longitude)) {
                        Log.d("NearbyArrivals", "User is outside London")
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

                    Log.d("NearbyArrivals", "Found ${nearestStations.size} stations within ${MAX_NEARBY_DISTANCE_KM}km")

                    if (nearestStations.isEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            nearbyStationArrivals = emptyList(),
                            isNearbyLoading = false,
                            nearbyStatusMessage = "No stations within ${MAX_NEARBY_DISTANCE_KM.toInt()}km",
                            isNearbyUsingFallback = false,
                        )
                        return@launch
                    }

                    // Fetch real arrivals from TfL API for nearest stations
                    val arrivals = mutableListOf<NearbyArrival>()
                    nearestStations.forEach { (station, distance) ->
                        Log.d("NearbyArrivals", "Fetching arrivals for ${station.name} (${distance.format(2)}km)")
                        repository.fetchStationArrivals(station.id)
                            .onSuccess { stationArrivals ->
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
                                    .forEach { arrival ->
                                    arrivals.add(
                                        NearbyArrival(
                                            stationName = station.name,
                                            lineName = arrival.lineName,
                                            destination = arrival.destination,
                                            minutesUntil = arrival.timeToStationMinutes,
                                            platform = arrival.platform,
                                            distanceKm = distance,
                                            lineColor = arrival.lineColor,
                                        )
                                    )
                                    }
                                Log.d("NearbyArrivals", "Got ${stationArrivals.arrivals.size} arrivals for ${station.name}")
                            }
                            .onFailure { error ->
                                Log.e("NearbyArrivals", "Failed for ${station.name}: ${error.message}")
                            }
                    }

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
                        nearbyStatusMessage = if (sortedArrivals.isNotEmpty())
                            "Live arrivals near you"
                        else
                            "No live arrivals available right now",
                        isNearbyUsingFallback = false,
                        isOutsideLondon = false,
                    )
                    Log.d("NearbyArrivals", "Total arrivals: ${sortedArrivals.size}")
                }
                .onFailure { error ->
                    Log.e("NearbyArrivals", "Location failed: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        nearbyStationArrivals = emptyList(),
                        isNearbyLoading = false,
                        nearbyStatusMessage = "Unable to get your location",
                        isNearbyUsingFallback = false,
                    )
                }
        }
    }

    private fun Double.format(digits: Int): String = "%.${digits}f".format(this)

    fun retryNearbyArrivals() {
        loadNearbyArrivals()
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
                    _uiState.value = _uiState.value.copy(
                        lineStatuses = statuses,
                        isLoadingStatus = false,
                        lastUpdated = "Just now",
                        aiInsights = repository.generateInsights(statuses),
                    )
                    computeDerivedStats(statuses)
                }
                .onFailure {
                    val cached = loadCachedStatuses()
                    _uiState.value = _uiState.value.copy(
                        lineStatuses = cached,
                        isLoadingStatus = false,
                        statusError = if (cached.isEmpty()) "Unable to fetch line status. Check your connection." else null,
                        lastUpdated = if (cached.isNotEmpty()) "Cached" else null,
                        aiInsights = repository.generateInsights(cached),
                    )
                    computeDerivedStats(cached)
                }
        }
    }

    private fun computeDerivedStats(statuses: List<LineStatus>) {
        if (statuses.isEmpty()) return
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
                    val disruptedLines = statuses.count { !it.isGoodService }
                    val delay = disruptedLines * 2
                    _uiState.value = _uiState.value.copy(
                        commuteTimeEstimate = "${route.totalDurationMinutes + delay} min"
                    )
                    return@launch
                }
            }
            // Fallback if no home/work set
            val disruptedLines = statuses.count { !it.isGoodService }
            val baseTime = 25
            _uiState.value = _uiState.value.copy(
                commuteTimeEstimate = "${baseTime + disruptedLines * 3} min"
            )
        }
    }

    fun onPermissionsChanged(permissionsState: PermissionsState) {
        if (permissionsState.locationPermissionGranted) {
            Log.d("NearbyArrivals", "✅ Location permission granted, loading nearby arrivals")
            loadNearbyArrivals()
        } else {
            Log.d("NearbyArrivals", "🚫 Location permission missing, clearing nearby arrivals")
            _uiState.value = _uiState.value.copy(
                nearbyStationArrivals = emptyList(),
                isNearbyUsingFallback = false,
                isNearbyLoading = false,
                nearbyStatusMessage = "Location permission required",
            )
        }
    }

    private suspend fun loadCachedStatuses(): List<LineStatus> {
        val cachedList = mutableListOf<LineStatus>()
        repository.getCachedLineStatuses().collect { entities ->
            cachedList.addAll(entities.map { it.toLineStatus() })
            return@collect
        }
        return cachedList
    }

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
