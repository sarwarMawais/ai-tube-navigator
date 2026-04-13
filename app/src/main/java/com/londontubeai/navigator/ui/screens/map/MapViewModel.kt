package com.londontubeai.navigator.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.londontubeai.navigator.data.model.JourneyRoute
import com.londontubeai.navigator.data.model.LineStatus
import com.londontubeai.navigator.data.model.NaptanIds
import com.londontubeai.navigator.data.model.Station
import com.londontubeai.navigator.data.model.StationArrivals
import com.londontubeai.navigator.data.model.TubeData
import com.londontubeai.navigator.data.repository.TubeRepository
import com.londontubeai.navigator.utils.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MapUiState(
    val userLat: Double? = null,
    val userLng: Double? = null,
    val nearestStationId: String? = null,
    val nearestStationDistanceKm: Double? = null,
    val journeyRoute: JourneyRoute? = null,
    val selectedStationId: String? = null,
    val stationArrivals: StationArrivals? = null,
    val isLoadingArrivals: Boolean = false,
    val arrivalsError: Boolean = false,
    val lineStatuses: List<LineStatus> = emptyList(),
    val isLoadingStatuses: Boolean = false,
    val nearbyArrivals: Map<String, StationArrivals> = emptyMap(),
    val nearbyArrivalsUpdatedAt: Long = 0L,
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: TubeRepository,
    private val locationService: LocationService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var arrivalsJob: Job? = null

    companion object {
        private const val ARRIVALS_REFRESH_MS = 15_000L
    }

    init {
        loadCurrentLocation()
        loadLineStatuses()
        viewModelScope.launch {
            while (true) {
                delay(ARRIVALS_REFRESH_MS)
                loadNearbyArrivals()
            }
        }
    }

    fun loadLineStatuses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingStatuses = true)
            repository.fetchLiveLineStatuses()
                .onSuccess { statuses ->
                    _uiState.value = _uiState.value.copy(
                        lineStatuses = statuses,
                        isLoadingStatuses = false,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoadingStatuses = false)
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
        viewModelScope.launch {
            val nearest = TubeData.getAllStationsSorted()
                .filter { NaptanIds.forStation(it.id) != null }
                .sortedBy { locationService.calculateDistance(userLat, userLng, it.latitude, it.longitude) }
                .take(5)
            val results = mutableMapOf<String, StationArrivals>()
            nearest.forEach { station ->
                repository.fetchStationArrivals(station.id)
                    .onSuccess { arrivals -> results[station.id] = arrivals }
            }
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
                    repository.fetchStationArrivals(station.id)
                        .onSuccess { arrivals ->
                            _uiState.value = _uiState.value.copy(
                                stationArrivals = arrivals,
                                isLoadingArrivals = false,
                                arrivalsError = false,
                            )
                        }
                        .onFailure {
                            _uiState.value = _uiState.value.copy(
                                isLoadingArrivals = false,
                                arrivalsError = _uiState.value.stationArrivals == null,
                            )
                        }
                    delay(ARRIVALS_REFRESH_MS)
                }
            }
        }
    }

    fun loadJourney(fromId: String?, toId: String?) {
        if (fromId.isNullOrBlank() || toId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(journeyRoute = null)
            return
        }
        // Use the cached route from RouteScreen first (avoids re-fetching and works for place: IDs)
        val cached = repository.getLastJourneyRoute(fromId, toId)
        if (cached != null) {
            _uiState.value = _uiState.value.copy(journeyRoute = cached)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                journeyRoute = repository.fetchJourneyRouteFromTfl(fromId, toId)
                    .getOrNull()
                    ?: repository.findRoute(fromId, toId),
            )
        }
    }
}
