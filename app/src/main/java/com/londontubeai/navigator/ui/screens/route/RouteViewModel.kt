package com.londontubeai.navigator.ui.screens.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.londontubeai.navigator.data.local.entity.SavedJourneyEntity
import com.londontubeai.navigator.data.model.CarriageRecommendation
import com.londontubeai.navigator.data.model.CrowdPrediction
import com.londontubeai.navigator.data.model.JourneyRoute
import com.londontubeai.navigator.data.model.Station
import com.londontubeai.navigator.data.model.TubeData
import com.londontubeai.navigator.data.preferences.AppPreferences
import com.londontubeai.navigator.data.repository.TubeRepository
import com.londontubeai.navigator.ml.CrowdPredictionEngine
import com.londontubeai.navigator.utils.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class NearbyBusRoute(
    val busNumber: String,
    val direction: String,
    val estimatedMinutes: Int,
    val stopName: String,
)

data class RouteOption(
    val label: String,
    val description: String,
    val route: JourneyRoute,
    val isRecommended: Boolean = false,
)

enum class RoutePreference(val label: String, val description: String) {
    FASTEST("Fastest", "Minimum travel time"),
    FEWEST_CHANGES("Fewest Changes", "Minimize interchanges"),
    LEAST_WALKING("Least Walking", "Shorter walking distance"),
    STEP_FREE("Step Free", "Accessible route"),
}

enum class DepartureOption(val label: String) {
    LEAVE_NOW("Leave Now"),
    DEPART_AT("Depart At"),
    ARRIVE_BY("Arrive By"),
}

data class RouteUiState(
    val fromQuery: String = "",
    val toQuery: String = "",
    val fromStation: Station? = null,
    val toStation: Station? = null,
    val fromSuggestions: List<Station> = emptyList(),
    val toSuggestions: List<Station> = emptyList(),
    val isSearchingFrom: Boolean = false,
    val isSearchingTo: Boolean = false,
    val carriageRecommendation: CarriageRecommendation? = null,
    val crowdPrediction: CrowdPrediction? = null,
    val routeCalculated: Boolean = false,
    val estimatedMinutes: Int? = null,
    val isCalculating: Boolean = false,
    val journeyRoute: JourneyRoute? = null,
    val routeOptions: List<RouteOption> = emptyList(),
    val selectedRouteIndex: Int = 0,
    val routeError: String? = null,
    val selectedCrowdDayType: CrowdPredictionEngine.DayType = CrowdPredictionEngine.DayType.AUTO,
    // New enhanced state
    val selectedPreference: RoutePreference = RoutePreference.FASTEST,
    val departureOption: DepartureOption = DepartureOption.LEAVE_NOW,
    val isFavorite: Boolean = false,
    val isSwapping: Boolean = false,
    val showDepartureOptions: Boolean = false,
    val savedJourneyId: Long? = null,
    val reminderSet: Boolean = false,
    val currentHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
    val currentMinute: Int = Calendar.getInstance().get(Calendar.MINUTE),
    // Focus management
    val fromFieldFocused: Boolean = false,
    val toFieldFocused: Boolean = false,
    // Location awareness
    val isInsideLondon: Boolean = false,
    val locationChecked: Boolean = false,
    val nearestStationName: String? = null,
    val nearestStationDistance: Double? = null,
    val nearestStationWalkMinutes: Int? = null,
    val userLat: Double? = null,
    val userLng: Double? = null,
    val fromIsAutoLocation: Boolean = false,
    val isResolvingLocation: Boolean = false,
    val nearbyBusRoutes: List<NearbyBusRoute> = emptyList(),
    val isFetchingBuses: Boolean = false,
)

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val repository: TubeRepository,
    private val prefs: AppPreferences,
    private val locationService: LocationService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouteUiState())
    val uiState: StateFlow<RouteUiState> = _uiState.asStateFlow()

    val recentJourneys: StateFlow<List<SavedJourneyEntity>> =
        repository.getRecentJourneys(5)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        checkLocation()
    }

    private fun checkLocation(autoPopulateFrom: Boolean = true) {
        _uiState.value = _uiState.value.copy(isResolvingLocation = true)
        viewModelScope.launch {
            locationService.getCurrentLocation()
                .onSuccess { location ->
                    val lat = location.latitude
                    val lng = location.longitude
                    val inLondon = lat in 51.28..51.69 && lng in -0.51..0.33

                    // Always find nearest station (even outside London) for display purposes
                    val nearestStation = TubeData.getAllStationsSorted()
                        .minByOrNull { locationService.calculateDistance(lat, lng, it.latitude, it.longitude) }
                    val nearestDist = nearestStation?.let {
                        locationService.calculateDistance(lat, lng, it.latitude, it.longitude)
                    }
                    val nearestWalkMins = nearestDist?.let { km ->
                        ((km * 1000) / 80).toInt().coerceAtLeast(1)
                    }

                    val shouldAutoPopulate = autoPopulateFrom && _uiState.value.fromStation == null

                    // Use raw coordinates as origin — do NOT snap to nearest station.
                    // tryCalculateRoute will pass lat,lng directly to TfL API.
                    _uiState.value = _uiState.value.copy(
                        isInsideLondon = inLondon,
                        locationChecked = true,
                        isResolvingLocation = false,
                        nearestStationName = nearestStation?.name,
                        nearestStationDistance = nearestDist,
                        nearestStationWalkMinutes = nearestWalkMins,
                        userLat = lat,
                        userLng = lng,
                        fromStation = if (shouldAutoPopulate) null else _uiState.value.fromStation,
                        fromQuery = if (shouldAutoPopulate) "My Location" else _uiState.value.fromQuery,
                        fromIsAutoLocation = shouldAutoPopulate || _uiState.value.fromIsAutoLocation,
                    )
                    if (shouldAutoPopulate) tryCalculateRoute()
                    if (inLondon) fetchNearbyBuses(lat, lng)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        locationChecked = true,
                        isInsideLondon = false,
                        isResolvingLocation = false,
                    )
                }
        }
    }

    private fun fetchNearbyBuses(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(isFetchingBuses = true)
        viewModelScope.launch {
            try {
                val buses = repository.fetchNearbyBusStopPoints(lat, lng, 400)
                    .getOrDefault(emptyList())
                    .take(3)
                    .flatMap { stop ->
                        repository.fetchStopPointArrivals(stop.id)
                            .getOrDefault(emptyList())
                            .filter { it.timeToStationMinutes in 0..30 }
                            .sortedBy { it.timeToStationSeconds }
                            .take(2)
                            .map { arrival ->
                                NearbyBusRoute(
                                    busNumber = arrival.lineName,
                                    direction = stop.towards ?: arrival.destination,
                                    estimatedMinutes = arrival.timeToStationMinutes.coerceAtLeast(0),
                                    stopName = listOfNotNull(stop.name, stop.indicator?.takeIf { it.isNotBlank() })
                                        .joinToString(" · "),
                                )
                            }
                    }
                    .distinctBy { "${it.busNumber}-${it.direction}" }
                    .sortedBy { it.estimatedMinutes }
                    .take(5)
                _uiState.value = _uiState.value.copy(
                    nearbyBusRoutes = buses,
                    isFetchingBuses = false,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isFetchingBuses = false)
            }
        }
    }

    fun useMyLocation() {
        _uiState.value = _uiState.value.copy(
            fromStation = null,
            fromQuery = "",
            fromIsAutoLocation = false,
        )
        checkLocation(autoPopulateFrom = true)
    }

    // Debouncing jobs for search
    private var fromSearchJob: Job? = null
    private var toSearchJob: Job? = null
    private val SEARCH_DEBOUNCE_MS = 300L

    fun updateFromQuery(query: String) {
        // Cancel previous search job
        fromSearchJob?.cancel()
        
        // Update query immediately for UI responsiveness
        _uiState.value = _uiState.value.copy(
            fromQuery = query,
            isSearchingFrom = query.length >= 2,
            fromStation = if (query.isBlank()) null else _uiState.value.fromStation,
            fromIsAutoLocation = false,
            routeCalculated = false,
        )
        
        // Debounce search suggestions
        if (query.length >= 2) {
            fromSearchJob = viewModelScope.launch {
                delay(SEARCH_DEBOUNCE_MS)
                val suggestions = TubeData.searchStations(query).take(6)
                _uiState.value = _uiState.value.copy(
                    fromSuggestions = suggestions,
                )
            }
        } else {
            _uiState.value = _uiState.value.copy(
                fromSuggestions = emptyList(),
                isSearchingFrom = false,
            )
        }
    }

    fun updateToQuery(query: String) {
        // Cancel previous search job
        toSearchJob?.cancel()
        
        // Update query immediately for UI responsiveness
        _uiState.value = _uiState.value.copy(
            toQuery = query,
            isSearchingTo = query.length >= 2,
            toStation = if (query.isBlank()) null else _uiState.value.toStation,
            routeCalculated = false,
        )
        
        // Debounce search suggestions
        if (query.length >= 2) {
            toSearchJob = viewModelScope.launch {
                delay(SEARCH_DEBOUNCE_MS)
                val suggestions = TubeData.searchStations(query).take(6)
                _uiState.value = _uiState.value.copy(
                    toSuggestions = suggestions,
                )
            }
        } else {
            _uiState.value = _uiState.value.copy(
                toSuggestions = emptyList(),
                isSearchingTo = false,
            )
        }
    }

    fun selectFromStation(station: Station) {
        _uiState.value = _uiState.value.copy(
            fromStation = station,
            fromQuery = station.name,
            fromSuggestions = emptyList(),
            isSearchingFrom = false,
            fromFieldFocused = false,
            routeCalculated = false,
        )
        tryCalculateRoute()
    }

    fun selectToStation(station: Station) {
        _uiState.value = _uiState.value.copy(
            toStation = station,
            toQuery = station.name,
            toSuggestions = emptyList(),
            isSearchingTo = false,
            toFieldFocused = false,
            routeCalculated = false,
        )
        tryCalculateRoute()
    }

    fun setFromFieldFocused(focused: Boolean) {
        _uiState.value = _uiState.value.copy(fromFieldFocused = focused)
    }

    fun setToFieldFocused(focused: Boolean) {
        _uiState.value = _uiState.value.copy(toFieldFocused = focused)
    }

    fun swapStations() {
        val current = _uiState.value
        _uiState.value = current.copy(isSwapping = true)
        viewModelScope.launch {
            delay(150) // Brief animation delay
            _uiState.value = _uiState.value.copy(
                fromStation = current.toStation,
                toStation = current.fromStation,
                fromQuery = current.toQuery,
                toQuery = current.fromQuery,
                routeCalculated = false,
                isSwapping = false,
            )
            tryCalculateRoute()
        }
    }

    fun selectPreference(preference: RoutePreference) {
        _uiState.value = _uiState.value.copy(selectedPreference = preference)
        val options = _uiState.value.routeOptions
        if (options.isEmpty()) {
            tryCalculateRoute()
            return
        }
        // Snap to matching option without full recalculation
        val targetLabel = when (preference) {
            RoutePreference.FASTEST -> "Fastest"
            RoutePreference.FEWEST_CHANGES -> "Fewer changes"
            RoutePreference.LEAST_WALKING -> "Less walking"
            RoutePreference.STEP_FREE -> null
        }
        val idx = targetLabel
            ?.let { lbl -> options.indexOfFirst { it.label == lbl } }
            ?.takeIf { it >= 0 } ?: 0
        selectRouteOption(idx)
    }

    fun selectRouteOption(index: Int) {
        val options = _uiState.value.routeOptions
        if (index !in options.indices) return
        val selected = options[index]
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val dayType = _uiState.value.selectedCrowdDayType
        _uiState.value = _uiState.value.copy(
            selectedRouteIndex = index,
            journeyRoute = selected.route,
            carriageRecommendation = selected.route.carriageRecommendation,
            crowdPrediction = repository.predictCrowding(selected.route.fromStation.id, hour, dayType),
            estimatedMinutes = selected.route.totalDurationMinutes,
        )
    }

    fun updateDepartureTime(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(
            currentHour = hour,
            currentMinute = minute,
        )
        tryCalculateRoute()
    }

    fun selectDepartureOption(option: DepartureOption) {
        _uiState.value = _uiState.value.copy(
            departureOption = option,
            showDepartureOptions = false,
        )
    }

    fun toggleDepartureOptions() {
        _uiState.value = _uiState.value.copy(
            showDepartureOptions = !_uiState.value.showDepartureOptions,
        )
    }

    fun selectCrowdDayType(dayType: CrowdPredictionEngine.DayType) {
        val state = _uiState.value
        val targetStationId = state.journeyRoute?.fromStation?.id ?: state.toStation?.id
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        _uiState.value = state.copy(
            selectedCrowdDayType = dayType,
            crowdPrediction = targetStationId?.let { repository.predictCrowding(it, hour, dayType) },
        )
    }

    fun toggleFavorite() {
        val newFav = !_uiState.value.isFavorite
        _uiState.value = _uiState.value.copy(isFavorite = newFav)
        val journeyId = _uiState.value.savedJourneyId ?: return
        viewModelScope.launch {
            val journey = SavedJourneyEntity(
                id = journeyId,
                fromStationId = _uiState.value.fromStation?.id ?: "",
                fromStationName = _uiState.value.fromStation?.name ?: "",
                toStationId = _uiState.value.toStation?.id ?: "",
                toStationName = _uiState.value.toStation?.name ?: "",
                isFavourite = newFav,
            )
            repository.toggleFavourite(journey)
        }
    }

    fun setReminder() {
        _uiState.value = _uiState.value.copy(reminderSet = true)
    }

    fun getShareText(): String {
        val state = _uiState.value
        val from = state.fromStation?.name ?: "Unknown"
        val to = state.toStation?.name ?: "Unknown"
        val mins = state.estimatedMinutes ?: 0
        val route = state.journeyRoute
        val changes = route?.totalInterchanges ?: 0
        val legs = route?.legs?.joinToString(" → ") { it.line.name } ?: ""
        return "🚇 AI Tube Navigator\n$from → $to\n⏱ $mins min · $changes change${if (changes != 1) "s" else ""}\nVia: $legs\nPlan your journey: https://aitube.navigator"
    }

    fun selectRecentJourney(journey: SavedJourneyEntity) {
        val from = TubeData.getStationById(journey.fromStationId)
        val to = TubeData.getStationById(journey.toStationId)
        if (from != null && to != null) {
            _uiState.value = _uiState.value.copy(
                fromStation = from,
                toStation = to,
                fromQuery = from.name,
                toQuery = to.name,
                fromSuggestions = emptyList(),
                toSuggestions = emptyList(),
                isSearchingFrom = false,
                isSearchingTo = false,
                routeCalculated = false,
            )
            tryCalculateRoute()
        }
    }

    private fun tryCalculateRoute() {
        val state = _uiState.value
        val to = state.toStation ?: return
        val from = state.fromStation
        val lat = state.userLat
        val lng = state.userLng
        val useCoords = state.fromIsAutoLocation && lat != null && lng != null

        // Need either explicit from-station OR GPS coordinates
        if (!useCoords && from == null) return
        if (!useCoords && from != null && from.id == to.id) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCalculating = true,
                routeError = null,
                routeOptions = emptyList(),
            )

            val journeyId = from?.let { repository.saveJourney(it, to) }
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val selectedCrowdDayType = _uiState.value.selectedCrowdDayType

            val variants = listOf(
                RoutePreference.FASTEST to "Fastest",
                RoutePreference.FEWEST_CHANGES to "Fewer changes",
                RoutePreference.LEAST_WALKING to "Less walking",
            )

            val fetched = variants.map { (pref, label) ->
                async {
                    val route = if (useCoords) {
                        // Pass raw GPS coordinates to TfL — it handles walk+bus+tube automatically
                        repository.fetchJourneyRouteFromTfl(
                            fromStationId = "my-location",
                            toStationId = to.id,
                            preference = pref.name,
                            fromLat = lat,
                            fromLng = lng,
                        ).getOrNull()
                        // Local fallback: walk to nearest station + tube
                            ?: buildLocalRouteWithWalking(lat!!, lng!!, to, pref.name)
                    } else {
                        repository.fetchJourneyRouteFromTfl(from!!.id, to.id, pref.name)
                            .getOrNull()
                            ?: repository.findRoute(from.id, to.id, pref.name)
                    }
                    route?.let {
                        RouteOption(
                            label = label,
                            description = buildOptionDescription(it),
                            route = it,
                            isRecommended = pref == RoutePreference.FASTEST,
                        )
                    }
                }
            }.awaitAll().filterNotNull()

            val options = fetched
                .distinctBy { it.route.totalDurationMinutes }
                .sortedBy { it.route.totalDurationMinutes }

            val crowdStationId = from?.id ?: to.id
            if (options.isNotEmpty()) {
                val primary = options.first()
                _uiState.value = _uiState.value.copy(
                    routeOptions = options,
                    selectedRouteIndex = 0,
                    journeyRoute = primary.route,
                    carriageRecommendation = primary.route.carriageRecommendation,
                    crowdPrediction = repository.predictCrowding(crowdStationId, hour, selectedCrowdDayType),
                    routeCalculated = true,
                    estimatedMinutes = primary.route.totalDurationMinutes,
                    isCalculating = false,
                    savedJourneyId = journeyId,
                    isFavorite = false,
                    reminderSet = false,
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    carriageRecommendation = repository.getBestExitForStation(to),
                    crowdPrediction = repository.predictCrowding(to.id, hour, selectedCrowdDayType),
                    routeCalculated = false,
                    isCalculating = false,
                    routeError = buildRouteError(useCoords, state.isInsideLondon, state.nearestStationName, state.nearestStationWalkMinutes),
                )
            }
        }
    }

    /**
     * Local fallback when TfL API is unavailable.
     * Calculates walk leg from GPS to nearest tube station, then runs Dijkstra tube route.
     */
    private fun buildLocalRouteWithWalking(
        lat: Double,
        lng: Double,
        toStation: Station,
        preference: String,
    ): com.londontubeai.navigator.data.model.JourneyRoute? {
        val nearest = TubeData.getAllStationsSorted()
            .minByOrNull { locationService.calculateDistance(lat, lng, it.latitude, it.longitude) }
            ?: return null

        if (nearest.id == toStation.id) return null

        val distKm = locationService.calculateDistance(lat, lng, nearest.latitude, nearest.longitude)
        val walkMins = ((distKm * 1000) / 80).toInt().coerceAtLeast(1)
        val walkDistM = (distKm * 1000).toInt()

        val tubeRoute = repository.findRoute(nearest.id, toStation.id, preference) ?: return null

        val myLocation = Station(
            id = "my-location", name = "My Location",
            lineIds = emptyList(), zone = "",
            latitude = lat, longitude = lng,
        )
        val walkLine = com.londontubeai.navigator.data.model.TubeLine(
            id = "walking", name = "Walking",
            color = androidx.compose.ui.graphics.Color(0xFF607D8B),
            stationIds = emptyList(),
        )
        val walkLeg = com.londontubeai.navigator.data.model.JourneyLeg(
            fromStation = myLocation,
            toStation = nearest,
            line = walkLine,
            durationMinutes = walkMins,
            direction = "Walk to ${nearest.name}",
            intermediateStops = 0,
            mode = com.londontubeai.navigator.data.model.TransportMode.WALKING,
            walkingDistanceMeters = walkDistM,
            walkingDirections = "Head towards ${nearest.name} (${String.format("%.1f", distKm)} km)",
        )
        return tubeRoute.copy(
            fromStation = myLocation,
            legs = listOf(walkLeg) + tubeRoute.legs,
            totalDurationMinutes = tubeRoute.totalDurationMinutes + walkMins,
            totalWalkingMinutes = tubeRoute.totalWalkingMinutes + walkMins,
        )
    }

    private fun buildRouteError(
        useCoords: Boolean,
        inLondon: Boolean,
        nearestStation: String?,
        walkMins: Int?,
    ): String = when {
        useCoords && !inLondon && nearestStation != null ->
            "You're outside London. Nearest tube station is ${nearestStation}" +
                (walkMins?.let { " (~${it} min walk)" } ?: "") +
                ". No direct tube route available from your location."
        useCoords -> "Could not calculate route from your current location. Check your connection."
        else -> "No route found between these stations. Try nearby alternatives."
    }

    private fun buildOptionDescription(route: JourneyRoute): String {
        val changes = route.totalInterchanges
        val walk = route.totalWalkingMinutes
        return buildString {
            if (changes == 0) append("Direct") else append("$changes change${if (changes != 1) "s" else ""}") 
            if (walk > 0) append(" · ${walk}m walk")
        }
    }

    fun clearRoute() {
        fromSearchJob?.cancel()
        toSearchJob?.cancel()
        _uiState.value = RouteUiState(
            isInsideLondon = _uiState.value.isInsideLondon,
            locationChecked = _uiState.value.locationChecked,
            nearestStationName = _uiState.value.nearestStationName,
            nearestStationDistance = _uiState.value.nearestStationDistance,
            userLat = _uiState.value.userLat,
            userLng = _uiState.value.userLng,
            routeOptions = emptyList(),
        )
    }

    override fun onCleared() {
        super.onCleared()
        fromSearchJob?.cancel()
        toSearchJob?.cancel()
    }
}
