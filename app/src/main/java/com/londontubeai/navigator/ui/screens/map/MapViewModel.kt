package com.londontubeai.navigator.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.Color
import com.londontubeai.navigator.data.local.entity.CachedLineStatusEntity
import com.londontubeai.navigator.data.model.JourneyRoute
import com.londontubeai.navigator.data.model.LineStatus
import com.londontubeai.navigator.data.model.NaptanIds
import com.londontubeai.navigator.data.model.Station
import com.londontubeai.navigator.data.model.StationArrivals
import com.londontubeai.navigator.data.model.TubeData
import com.londontubeai.navigator.data.preferences.AppPreferences
import com.londontubeai.navigator.data.repository.TubeRepository
import com.londontubeai.navigator.utils.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class MapUiState(
    val userLat: Double? = null,
    val userLng: Double? = null,
    val nearestStationId: String? = null,
    val nearestStationDistanceKm: Double? = null,
    val journeyRoute: JourneyRoute? = null,
    val isLoadingJourney: Boolean = false,
    val journeyErrorMessage: String? = null,
    val selectedStationId: String? = null,
    val stationArrivals: StationArrivals? = null,
    val isLoadingArrivals: Boolean = false,
    val arrivalsError: Boolean = false,
    val lineStatuses: List<LineStatus> = emptyList(),
    val isLoadingStatuses: Boolean = false,
    val isUsingCachedStatuses: Boolean = false,
    val offlineMessage: String? = null,
    val nearbyArrivals: Map<String, StationArrivals> = emptyMap(),
    val nearbyArrivalsUpdatedAt: Long = 0L,
    val searchResults: List<Station> = emptyList(),
    val isSearchingPlaces: Boolean = false,
    val recentPlaces: List<Station> = emptyList(),
    val mapStyleName: String = "NORMAL",
    val selectedLineFilter: String? = null,
    val pinnedLocationKey: String? = null,
    val pinnedLocationLabel: String? = null,
    val isResolvingPinnedLabel: Boolean = false,
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: TubeRepository,
    private val prefs: AppPreferences,
    private val locationService: LocationService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var arrivalsJob: Job? = null
    private var nearbyArrivalsJob: Job? = null
    private var journeyJob: Job? = null
    private var searchJob: Job? = null
    private var isScreenActive = true

    companion object {
        private const val ARRIVALS_REFRESH_MS = 15_000L
        private const val STATUS_REFRESH_MS = 60_000L
        private const val SEARCH_DEBOUNCE_MS = 250L
        private const val MAX_RECENT_MAP_PLACES = 8
    }

    fun setActive(active: Boolean) {
        isScreenActive = active
    }

    init {
        loadCurrentLocation()
        loadLineStatuses()
        observeMapPreferences()
        viewModelScope.launch {
            while (true) {
                delay(ARRIVALS_REFRESH_MS)
                if (isScreenActive) loadNearbyArrivals()
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(STATUS_REFRESH_MS)
                if (isScreenActive) loadLineStatuses()
            }
        }
    }

    private fun observeMapPreferences() {
        viewModelScope.launch {
            prefs.mapStyle.collect { styleName ->
                _uiState.value = _uiState.value.copy(mapStyleName = styleName)
            }
        }
        viewModelScope.launch {
            prefs.mapLineFilter.collect { lineId ->
                _uiState.value = _uiState.value.copy(selectedLineFilter = lineId)
            }
        }
        viewModelScope.launch {
            prefs.recentMapPlaces.collect { raw ->
                _uiState.value = _uiState.value.copy(recentPlaces = decodeRecentPlaces(raw))
            }
        }
    }

    fun setMapStyle(styleName: String) {
        _uiState.value = _uiState.value.copy(mapStyleName = styleName)
        viewModelScope.launch { prefs.setMapStyle(styleName) }
    }

    fun setSelectedLineFilter(lineId: String?) {
        _uiState.value = _uiState.value.copy(selectedLineFilter = lineId)
        viewModelScope.launch { prefs.setMapLineFilter(lineId) }
    }

    fun updateSearchQuery(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                isSearchingPlaces = false,
            )
            return
        }
        _uiState.value = _uiState.value.copy(isSearchingPlaces = true)
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            val results = repository.searchPlaces(query)
                .getOrElse { TubeData.searchStations(query) }
                .take(8)
            _uiState.value = _uiState.value.copy(
                searchResults = results,
                isSearchingPlaces = false,
            )
        }
    }

    fun rememberMapPlace(place: Station) {
        val current = _uiState.value.recentPlaces.toMutableList()
        current.removeAll { it.id == place.id }
        current.add(0, place)
        val updated = current.take(MAX_RECENT_MAP_PLACES)
        _uiState.value = _uiState.value.copy(recentPlaces = updated)
        viewModelScope.launch {
            prefs.setRecentMapPlaces(encodeRecentPlaces(updated))
        }
    }

    fun resolvePinnedLocation(latitude: Double, longitude: Double, preferredLabel: String? = null) {
        val key = pinnedLocationKey(latitude, longitude)
        if (!preferredLabel.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                pinnedLocationKey = key,
                pinnedLocationLabel = preferredLabel,
                isResolvingPinnedLabel = false,
            )
            return
        }
        _uiState.value = _uiState.value.copy(
            pinnedLocationKey = key,
            pinnedLocationLabel = null,
            isResolvingPinnedLabel = true,
        )
        viewModelScope.launch {
            val resolved = locationService.reverseGeocode(latitude, longitude) ?: "Pinned location"
            if (_uiState.value.pinnedLocationKey != key) return@launch
            _uiState.value = _uiState.value.copy(
                pinnedLocationLabel = resolved,
                isResolvingPinnedLabel = false,
            )
        }
    }

    fun clearPinnedLocation() {
        _uiState.value = _uiState.value.copy(
            pinnedLocationKey = null,
            pinnedLocationLabel = null,
            isResolvingPinnedLabel = false,
        )
    }

    fun loadLineStatuses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingStatuses = true)
            repository.fetchLiveLineStatuses()
                .onSuccess { statuses ->
                    _uiState.value = _uiState.value.copy(
                        lineStatuses = statuses,
                        isLoadingStatuses = false,
                        isUsingCachedStatuses = false,
                        offlineMessage = null,
                    )
                }
                .onFailure {
                    val cached = repository.getCachedLineStatuses().first()
                    if (cached.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            lineStatuses = cached.map { it.toLineStatus() },
                            isLoadingStatuses = false,
                            isUsingCachedStatuses = true,
                            offlineMessage = "Offline · showing cached status",
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoadingStatuses = false,
                            isUsingCachedStatuses = false,
                            offlineMessage = "Could not load line statuses. Check your connection.",
                        )
                    }
                }
        }
    }

    fun loadCurrentLocation() {
        viewModelScope.launch {
            locationService.getCurrentLocation()
                .onSuccess { location ->
                    val allStations = TubeData.getAllStationsSorted()
                    val sorted = allStations.sortedBy {
                        locationService.calculateDistance(
                            location.latitude, location.longitude,
                            it.latitude, it.longitude,
                        )
                    }
                    val nearestStation = sorted.firstOrNull()
                    val nearestDistance = nearestStation?.let {
                        locationService.calculateDistance(
                            location.latitude, location.longitude,
                            it.latitude, it.longitude,
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        userLat = location.latitude,
                        userLng = location.longitude,
                        nearestStationId = nearestStation?.id,
                        nearestStationDistanceKm = nearestDistance,
                    )
                    loadNearbyArrivals()
                }
        }
    }

    fun loadNearbyArrivals() {
        val state = _uiState.value
        val userLat = state.userLat ?: return
        val userLng = state.userLng ?: return
        nearbyArrivalsJob?.cancel()
        nearbyArrivalsJob = viewModelScope.launch {
            val nearest = TubeData.getAllStationsSorted()
                .filter { NaptanIds.forStation(it.id) != null }
                .sortedBy { locationService.calculateDistance(userLat, userLng, it.latitude, it.longitude) }
                .take(5)
            val results = nearest
                .map { station ->
                    async {
                        val arrivals = repository.fetchStationArrivals(station.id).getOrNull()
                        station.id to arrivals
                    }
                }
                .awaitAll()
                .filter { (_, arrivals) -> arrivals != null }
                .associate { (id, arrivals) -> id to arrivals!! }
            if (_uiState.value.userLat != userLat || _uiState.value.userLng != userLng) return@launch
            _uiState.value = _uiState.value.copy(
                nearbyArrivals = results,
                nearbyArrivalsUpdatedAt = System.currentTimeMillis(),
            )
        }
    }

    fun selectStation(station: Station?) {
        arrivalsJob?.cancel()
        if (station == null) {
            _uiState.value = _uiState.value.copy(
                selectedStationId = null,
                stationArrivals = null,
                isLoadingArrivals = false,
                arrivalsError = false,
            )
            return
        }
        _uiState.value = _uiState.value.copy(
            selectedStationId = station.id,
            stationArrivals = null,
            isLoadingArrivals = NaptanIds.forStation(station.id) != null,
            arrivalsError = false,
        )
        if (NaptanIds.forStation(station.id) != null) {
            arrivalsJob = viewModelScope.launch {
                while (true) {
                    if (isScreenActive) {
                        repository.fetchStationArrivals(station.id)
                            .onSuccess { arrivals ->
                                if (_uiState.value.selectedStationId != station.id) return@onSuccess
                                _uiState.value = _uiState.value.copy(
                                    stationArrivals = arrivals,
                                    isLoadingArrivals = false,
                                    arrivalsError = false,
                                )
                            }
                            .onFailure {
                                if (_uiState.value.selectedStationId != station.id) return@onFailure
                                _uiState.value = _uiState.value.copy(
                                    isLoadingArrivals = false,
                                    arrivalsError = _uiState.value.stationArrivals == null,
                                )
                            }
                    }
                    delay(ARRIVALS_REFRESH_MS)
                }
            }
        }
    }

    private fun CachedLineStatusEntity.toLineStatus(): LineStatus {
        val tubeColor = TubeData.getLineById(lineId)?.color ?: Color.Gray
        return LineStatus(
            lineId = lineId,
            lineName = lineName,
            statusSeverity = statusSeverity,
            statusDescription = statusDescription,
            reason = reason,
            lineColor = tubeColor,
        )
    }

    fun loadJourney(fromId: String?, toId: String?) {
        journeyJob?.cancel()
        arrivalsJob?.cancel()
        if (fromId.isNullOrBlank() || toId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                journeyRoute = null,
                isLoadingJourney = false,
                journeyErrorMessage = null,
                selectedStationId = null,
                stationArrivals = null,
                isLoadingArrivals = false,
                arrivalsError = false,
            )
            return
        }
        val cached = repository.getLastJourneyRoute(fromId, toId)
        if (cached != null) {
            _uiState.value = _uiState.value.copy(
                journeyRoute = cached,
                isLoadingJourney = false,
                journeyErrorMessage = null,
                selectedStationId = null,
                stationArrivals = null,
                isLoadingArrivals = false,
                arrivalsError = false,
            )
            return
        }
        _uiState.value = _uiState.value.copy(
            journeyRoute = null,
            isLoadingJourney = true,
            journeyErrorMessage = null,
            selectedStationId = null,
            stationArrivals = null,
            isLoadingArrivals = false,
            arrivalsError = false,
        )
        journeyJob = viewModelScope.launch {
            val fromTfl = repository.fetchJourneyRouteFromTfl(fromId, toId).getOrNull()
            val route = fromTfl ?: run {
                // If TfL didn't return and both endpoints are known stations, fall back to the
                // local Dijkstra graph. Place/my-location endpoints can't be graph-routed, so
                // surface a helpful error in that case.
                val fromIsStation = !fromId.startsWith("place:") && !fromId.startsWith("my-location")
                val toIsStation = !toId.startsWith("place:") && !toId.startsWith("my-location")
                if (fromIsStation && toIsStation) repository.findRoute(fromId, toId) else null
            }
            val errorMessage = when {
                route != null -> null
                fromId.startsWith("place:") || fromId.startsWith("my-location") ||
                    toId.startsWith("place:") || toId.startsWith("my-location") ->
                    "Couldn't fetch a live route for this location. Try tapping a station instead."
                else -> "Unable to load route overlay"
            }
            _uiState.value = _uiState.value.copy(
                journeyRoute = route,
                isLoadingJourney = false,
                journeyErrorMessage = errorMessage,
                selectedStationId = null,
                stationArrivals = null,
                isLoadingArrivals = false,
                arrivalsError = false,
            )
        }
    }

    private fun encodeRecentPlaces(places: List<Station>): String {
        return JSONArray().apply {
            places.forEach { station ->
                put(JSONObject().apply {
                    put("id", station.id)
                    put("name", station.name)
                    put("zone", station.zone)
                    put("lat", station.latitude)
                    put("lng", station.longitude)
                    put("lineIds", JSONArray(station.lineIds))
                })
            }
        }.toString()
    }

    private fun decodeRecentPlaces(raw: String): List<Station> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { index ->
                val obj = arr.optJSONObject(index) ?: return@mapNotNull null
                Station(
                    id = obj.optString("id"),
                    name = obj.optString("name"),
                    lineIds = obj.optJSONArray("lineIds")?.let { lineIds ->
                        (0 until lineIds.length()).mapNotNull { lineIds.optString(it).takeIf(String::isNotBlank) }
                    } ?: emptyList(),
                    zone = obj.optString("zone"),
                    latitude = obj.optDouble("lat"),
                    longitude = obj.optDouble("lng"),
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun pinnedLocationKey(latitude: Double, longitude: Double): String {
        return "${String.format("%.5f", latitude)},${String.format("%.5f", longitude)}"
    }
}
