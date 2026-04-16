package com.londontubeai.navigator.ui.screens.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.londontubeai.navigator.data.local.entity.SavedJourneyEntity
import com.londontubeai.navigator.data.model.CarriageRecommendation
import com.londontubeai.navigator.data.model.CrowdPrediction
import com.londontubeai.navigator.data.model.JourneyRoute
import com.londontubeai.navigator.data.model.Station
import com.londontubeai.navigator.data.model.TubeData
import com.londontubeai.navigator.data.billing.BillingManager
import com.londontubeai.navigator.data.preferences.AppPreferences
import com.londontubeai.navigator.data.repository.TubeRepository
import com.londontubeai.navigator.ml.CrowdPredictionEngine
import com.londontubeai.navigator.utils.AppReviewManager
import com.londontubeai.navigator.utils.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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
    val reviewManager: AppReviewManager,
    private val billingManager: BillingManager,
) : ViewModel() {

    val isPremium: StateFlow<Boolean> = billingManager.billingState
        .map { it.isPremium }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _uiState = MutableStateFlow(RouteUiState())
    val uiState: StateFlow<RouteUiState> = _uiState.asStateFlow()

    val recentJourneys: StateFlow<List<SavedJourneyEntity>> =
        repository.getRecentJourneys(5)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        checkLocation()
        applyPreferencesDefaults()
    }

    private fun applyPreferencesDefaults() {
        viewModelScope.launch {
            val stepFree = prefs.preferStepFree.first()
            val lessWalking = prefs.preferLessWalking.first()
            val defaultPref = when {
                stepFree -> RoutePreference.STEP_FREE
                lessWalking -> RoutePreference.LEAST_WALKING
                else -> RoutePreference.FASTEST
            }
            if (defaultPref != RoutePreference.FASTEST) {
                _uiState.value = _uiState.value.copy(selectedPreference = defaultPref)
            }
        }
    }

    private fun checkLocation(autoPopulateFrom: Boolean = true) {
        _uiState.value = _uiState.value.copy(isResolvingLocation = true)
        viewModelScope.launch {
            locationService.getCurrentLocation()
                .onSuccess { location ->
                    val lat = location.latitude
                    val lng = location.longitude
                    val inLondon = lat in 51.28..51.75 && lng in -0.65..0.40

                    // Always find nearest station (even outside London) for display purposes
                    val nearestStation = TubeData.getAllStationsSorted()
                        .minByOrNull { locationService.calculateDistance(lat, lng, it.latitude, it.longitude) }
                    val nearestDist = nearestStation?.let {
                        locationService.calculateDistance(lat, lng, it.latitude, it.longitude)
                    }
                    val nearestWalkMins = nearestDist?.let { km ->
                        (km * 12.0).toInt().coerceAtLeast(1)
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
                        fromIsAutoLocation = false,
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
                val suggestions = repository.searchPlaces(query).getOrElse { TubeData.searchStations(query) }.take(8)
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
                val suggestions = repository.searchPlaces(query).getOrElse { TubeData.searchStations(query) }.take(8)
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

    fun preSelectToStation(stationId: String) {
        val station = TubeData.getStationById(stationId) ?: return
        if (_uiState.value.toStation?.id == stationId) return
        selectToStation(station)
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

            // If "from" was "My Location" (null station + GPS coords), create a synthetic
            // place station so it can become a valid destination after swap.
            val effectiveFrom = current.fromStation
                ?: if (current.fromIsAutoLocation && current.userLat != null && current.userLng != null) {
                    Station(
                        id = "place:my-location",
                        name = "My Location",
                        lineIds = emptyList(),
                        zone = "",
                        latitude = current.userLat,
                        longitude = current.userLng,
                    )
                } else null

            _uiState.value = _uiState.value.copy(
                fromStation = current.toStation,
                toStation = effectiveFrom,
                fromQuery = current.toQuery,
                toQuery = effectiveFrom?.name ?: current.fromQuery,
                fromSuggestions = emptyList(),
                toSuggestions = emptyList(),
                isSearchingFrom = false,
                isSearchingTo = false,
                fromIsAutoLocation = false,
                routeCalculated = false,
                isSwapping = false,
            )
            tryCalculateRoute()
        }
    }

    fun selectPreference(preference: RoutePreference) {
        _uiState.value = _uiState.value.copy(selectedPreference = preference)
        val options = _uiState.value.routeOptions
        // Step-free requires a new TfL API call with accessibilityPreference param
        if (options.isEmpty() || preference == RoutePreference.STEP_FREE) {
            tryCalculateRoute()
            return
        }
        // Snap to matching option without full recalculation for other preferences
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
        repository.cacheRoute(selected.route)
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
        if (option == DepartureOption.LEAVE_NOW) tryCalculateRoute()
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
            ?: Station(
                id = journey.fromStationId,
                name = journey.fromStationName,
                lineIds = emptyList(),
                zone = "",
                latitude = journey.fromLat,
                longitude = journey.fromLng,
            )
        val to = TubeData.getStationById(journey.toStationId)
            ?: Station(
                id = journey.toStationId,
                name = journey.toStationName,
                lineIds = emptyList(),
                zone = "",
                latitude = journey.toLat,
                longitude = journey.toLng,
            )
        _uiState.value = _uiState.value.copy(
            fromStation = from,
            toStation = to,
            fromQuery = from.name,
            toQuery = to.name,
            fromSuggestions = emptyList(),
            toSuggestions = emptyList(),
            isSearchingFrom = false,
            isSearchingTo = false,
            fromIsAutoLocation = false,
            routeCalculated = false,
        )
        tryCalculateRoute()
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

            // Build TfL date/time/timeIs params from selected departure option
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, state.currentHour)
                set(Calendar.MINUTE, state.currentMinute)
            }
            val tflDate = when (state.departureOption) {
                DepartureOption.LEAVE_NOW -> null
                else -> SimpleDateFormat("yyyyMMdd", Locale.UK).format(cal.time)
            }
            val tflTime = when (state.departureOption) {
                DepartureOption.LEAVE_NOW -> null
                else -> "%02d%02d".format(state.currentHour, state.currentMinute)
            }
            val tflTimeIs = when (state.departureOption) {
                DepartureOption.ARRIVE_BY -> "arriving"
                DepartureOption.DEPART_AT -> "departing"
                else -> null
            }

            val toIsPlace = to.id.startsWith("place:")
            val fromIsPlace = from != null && from.id.startsWith("place:") && from.latitude != 0.0

            // Single TfL API call — TfL returns 3-5 options per request, no need for a second call
            val allRoutes = mutableListOf<JourneyRoute>()
            val tflPref = state.selectedPreference.name  // FASTEST, FEWEST_CHANGES, LEAST_WALKING, STEP_FREE

            val tflFastest = async {
                when {
                    useCoords -> repository.fetchAllJourneyRoutesFromTfl(
                        fromStationId = "my-location", toStationId = to.id, preference = tflPref,
                        fromLat = lat, fromLng = lng,
                        toLat = if (toIsPlace) to.latitude else null,
                        toLng = if (toIsPlace) to.longitude else null,
                        toDisplayName = if (toIsPlace) to.name else null,
                        departureDate = tflDate, departureTime = tflTime, timeIs = tflTimeIs,
                    ).getOrDefault(emptyList())
                    fromIsPlace -> repository.fetchAllJourneyRoutesFromTfl(
                        fromStationId = "my-location", toStationId = to.id, preference = tflPref,
                        fromLat = from!!.latitude, fromLng = from.longitude,
                        toLat = if (toIsPlace) to.latitude else null,
                        toLng = if (toIsPlace) to.longitude else null,
                        toDisplayName = if (toIsPlace) to.name else null,
                        departureDate = tflDate, departureTime = tflTime, timeIs = tflTimeIs,
                    ).getOrDefault(emptyList())
                    else -> repository.fetchAllJourneyRoutesFromTfl(
                        fromStationId = from!!.id, toStationId = to.id, preference = tflPref,
                        toLat = if (toIsPlace) to.latitude else null,
                        toLng = if (toIsPlace) to.longitude else null,
                        toDisplayName = if (toIsPlace) to.name else null,
                        departureDate = tflDate, departureTime = tflTime, timeIs = tflTimeIs,
                    ).getOrDefault(emptyList())
                }
            }

            allRoutes.addAll(tflFastest.await())

            // Fallback to local Dijkstra if TfL returned nothing
            if (allRoutes.isEmpty()) {
                val fallback = when {
                    useCoords -> buildLocalRouteWithWalking(lat!!, lng!!, to, "FASTEST")
                    fromIsPlace -> buildLocalRouteWithWalking(from!!.latitude, from.longitude, to, "FASTEST")
                    toIsPlace && from != null -> buildLocalRouteEndingAtCoords(from, to.latitude, to.longitude, to.name, "FASTEST")
                    from != null -> repository.findRoute(from.id, to.id, "FASTEST")
                    else -> null
                }
                fallback?.let { allRoutes.add(it) }
            }

            // Deduplicate by duration + interchanges, label them, sort by time
            val uniqueRoutes = allRoutes
                .distinctBy { "${it.totalDurationMinutes}-${it.totalInterchanges}-${it.legs.size}" }
                .sortedBy { it.totalDurationMinutes }
                .take(5)

            val options = uniqueRoutes.mapIndexed { idx, route ->
                val label = when {
                    idx == 0 -> "Fastest"
                    route.totalInterchanges == 0 -> "Direct"
                    route.totalInterchanges < (uniqueRoutes.firstOrNull()?.totalInterchanges ?: 99) -> "Fewer changes"
                    route.totalWalkingMinutes < (uniqueRoutes.firstOrNull()?.totalWalkingMinutes ?: 99) -> "Less walking"
                    else -> "Option ${idx + 1}"
                }
                RouteOption(
                    label = label,
                    description = buildOptionDescription(route),
                    route = route,
                    isRecommended = idx == 0,
                )
            }

            val crowdStationId = from?.id ?: to.id
            if (options.isNotEmpty()) {
                val primary = options.first()
                repository.cacheRoute(primary.route)
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
                    routeError = buildRouteError(useCoords, fromIsPlace, state.isInsideLondon, state.nearestStationName, state.nearestStationWalkMinutes, fromLat = if (fromIsPlace) from?.latitude else null, fromLng = if (fromIsPlace) from?.longitude else null),
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

        // If nearest tube station is too far away (user is well outside London),
        // the local Dijkstra fallback would produce nonsensical walking routes.
        // Return null so the caller can show a proper "outside London" error.
        if (distKm > 15.0) return null

        // Walking speed: ~5 km/h = ~83 m/min ≈ 12 min per km
        val walkMins = (distKm * 12.0).toInt().coerceAtLeast(1)
        val walkDistM = (distKm * 1000.0).toInt()

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
            walkingDirections = if (distKm < 1.0) "Head towards ${nearest.name} (${(distKm * 1000).toInt()}m)"
                                else "Head towards ${nearest.name} (${String.format("%.1f", distKm)} km)",
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
        fromIsPlace: Boolean,
        inLondon: Boolean,
        nearestStation: String?,
        walkMins: Int?,
        fromLat: Double? = null,
        fromLng: Double? = null,
    ): String {
        val fromOutsideLondon = fromLat != null && fromLng != null &&
            (fromLat < 51.28 || fromLat > 51.75 || fromLng < -0.65 || fromLng > 0.40)
        return when {
            useCoords && !inLondon ->
                "No route found from your location. Try entering a London station name to plan your journey."
            useCoords -> "Could not calculate route from your current location. Check your connection."
            fromIsPlace && fromOutsideLondon ->
                "Could not plan a route from this location. Ensure you're entering a valid UK station and try again."
            fromIsPlace -> "Could not find a route from this address. Try entering a nearby tube station instead."
            else -> "No route found between these stations. Try nearby alternatives."
        }
    }

    /**
     * Local fallback when TfL API is unavailable and GPS is the destination.
     * Routes from a tube station to the nearest tube station to GPS, then adds a walking leg.
     */
    private fun buildLocalRouteEndingAtCoords(
        fromStation: Station,
        destLat: Double,
        destLng: Double,
        destName: String,
        preference: String,
    ): com.londontubeai.navigator.data.model.JourneyRoute? {
        val nearest = TubeData.getAllStationsSorted()
            .minByOrNull { locationService.calculateDistance(destLat, destLng, it.latitude, it.longitude) }
            ?: return null
        if (nearest.id == fromStation.id) return null
        val distKm = locationService.calculateDistance(destLat, destLng, nearest.latitude, nearest.longitude)
        if (distKm > 15.0) return null
        val walkMins = (distKm * 12.0).toInt().coerceAtLeast(1)
        val walkDistM = (distKm * 1000.0).toInt()
        val tubeRoute = repository.findRoute(fromStation.id, nearest.id, preference) ?: return null
        val destination = Station(
            id = "my-location-dest", name = destName,
            lineIds = emptyList(), zone = "",
            latitude = destLat, longitude = destLng,
        )
        val walkLine = com.londontubeai.navigator.data.model.TubeLine(
            id = "walking", name = "Walking",
            color = androidx.compose.ui.graphics.Color(0xFF607D8B),
            stationIds = emptyList(),
        )
        val walkLeg = com.londontubeai.navigator.data.model.JourneyLeg(
            fromStation = nearest,
            toStation = destination,
            line = walkLine,
            durationMinutes = walkMins,
            direction = "Walk to $destName",
            intermediateStops = 0,
            mode = com.londontubeai.navigator.data.model.TransportMode.WALKING,
            walkingDistanceMeters = walkDistM,
            walkingDirections = if (distKm < 1.0) "Head towards $destName (${(distKm * 1000).toInt()}m)"
                               else "Head towards $destName (${String.format("%.1f", distKm)} km)",
        )
        return tubeRoute.copy(
            toStation = destination,
            legs = tubeRoute.legs + listOf(walkLeg),
            totalDurationMinutes = tubeRoute.totalDurationMinutes + walkMins,
            totalWalkingMinutes = tubeRoute.totalWalkingMinutes + walkMins,
        )
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
