package com.londontubeai.navigator.ui.screens.nearby

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.londontubeai.navigator.data.model.LineStatus
import com.londontubeai.navigator.data.model.Station
import com.londontubeai.navigator.data.model.TubeData
import com.londontubeai.navigator.data.repository.TubeRepository
import com.londontubeai.navigator.utils.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

data class NearbyStation(
    val station: Station,
    val distanceKm: Double,
    val walkingMinutes: Int,
    val lineStatuses: List<LineStatus>,
    val busRoutes: List<BusRoute>,
    val arrivals: List<StationArrivalInfo>,
    val isLoadingArrivals: Boolean = false,
)

data class BusRoute(
    val busNumber: String,
    val direction: String,
    val estimatedMinutes: Int,
    val stopName: String,
)

data class StationArrivalInfo(
    val lineName: String,
    val lineColor: Color?,
    val destination: String,
    val minutesUntil: Int,
    val platform: String?,
)

data class NearbyDetailUiState(
    val isLoading: Boolean = true,
    val nearbyStations: List<NearbyStation> = emptyList(),
    val error: String? = null,
    val userLatitude: Double = 0.0,
    val userLongitude: Double = 0.0,
)

@HiltViewModel
class NearbyDetailViewModel @Inject constructor(
    private val repository: TubeRepository,
    private val locationService: LocationService,
) : ViewModel() {

    private val nearbyRadiusKm = 4.8
    private val maxNearbyStations = 12
    private val busStopRadiusMeters = 250

    private val _uiState = MutableStateFlow(NearbyDetailUiState())
    val uiState: StateFlow<NearbyDetailUiState> = _uiState.asStateFlow()

    init {
        loadNearbyStations()
    }

    fun refresh() {
        loadNearbyStations()
    }

    private fun loadNearbyStations() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            locationService.getCurrentLocation()
                .onSuccess { location ->
                    val allLineStatuses = repository.fetchLiveLineStatuses().getOrDefault(emptyList())
                    _uiState.value = _uiState.value.copy(
                        userLatitude = location.latitude,
                        userLongitude = location.longitude,
                    )

                    val allStations = TubeData.getAllStationsSorted()
                    val nearestStations = allStations
                        .map { station ->
                            val distance = locationService.calculateDistance(
                                location.latitude, location.longitude,
                                station.latitude, station.longitude,
                            )
                            station to distance
                        }
                        .sortedBy { it.second }
                        .filter { it.second <= nearbyRadiusKm }
                        .take(maxNearbyStations)

                    val nearbyList = nearestStations.map { (station, distance) ->
                        val walkMinutes = estimateWalkingTime(distance)
                        NearbyStation(
                            station = station,
                            distanceKm = distance,
                            walkingMinutes = walkMinutes,
                            lineStatuses = allLineStatuses.filter { it.lineId in station.lineIds },
                            busRoutes = emptyList(),
                            arrivals = emptyList(),
                            isLoadingArrivals = true,
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        nearbyStations = nearbyList,
                        isLoading = false,
                    )

                    // Fetch live arrivals for each station
                    nearbyList.forEachIndexed { index, nearbyStation ->
                        launch {
                            val arrivals = fetchArrivalsForStation(nearbyStation.station)
                            val busRoutes = fetchBusRoutesForStation(nearbyStation.station)
                            val updated = _uiState.value.nearbyStations.toMutableList()
                            if (index < updated.size) {
                                updated[index] = updated[index].copy(
                                    busRoutes = busRoutes,
                                    arrivals = arrivals,
                                    isLoadingArrivals = false,
                                )
                                _uiState.value = _uiState.value.copy(nearbyStations = updated)
                            }
                        }
                    }
                }
                .onFailure { error ->
                    Log.e("NearbyDetail", "Location failed: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Could not get your location. Please check location permissions.",
                    )
                }
        }
    }

    private suspend fun fetchArrivalsForStation(station: Station): List<StationArrivalInfo> {
        return try {
            repository.fetchStationArrivals(station.id)
                .getOrNull()?.arrivals
                ?.filter { arrival ->
                    arrival.lineId in station.lineIds && arrival.timeToStationMinutes >= 0
                }
                ?.groupBy { arrival -> arrival.lineId }
                ?.values
                ?.mapNotNull { lineArrivals ->
                    lineArrivals.minByOrNull { arrival -> arrival.timeToStationSeconds }
                }
                ?.sortedBy { arrival -> arrival.timeToStationSeconds }
                ?.take(4)
                ?.map { arrival ->
                    StationArrivalInfo(
                        lineName = arrival.lineName,
                        lineColor = arrival.lineColor,
                        destination = arrival.destination,
                        minutesUntil = arrival.timeToStationMinutes,
                        platform = arrival.platform,
                    )
                } ?: emptyList()
        } catch (e: Exception) {
            Log.e("NearbyDetail", "Failed to fetch arrivals for ${station.name}: ${e.message}")
            emptyList()
        }
    }

    private fun estimateWalkingTime(distanceKm: Double): Int {
        return (distanceKm * 12.5).roundToInt().coerceAtLeast(1)
    }

    private suspend fun fetchBusRoutesForStation(station: Station): List<BusRoute> {
        return try {
            repository.fetchNearbyBusStopPoints(
                latitude = station.latitude,
                longitude = station.longitude,
                radiusMeters = busStopRadiusMeters,
            ).getOrDefault(emptyList())
                .take(2)
                .flatMap { stopPoint ->
                    repository.fetchStopPointArrivals(stopPoint.id)
                        .getOrDefault(emptyList())
                        .filter { arrival ->
                            arrival.timeToStationMinutes >= 0 && arrival.timeToStationMinutes <= 30
                        }
                        .sortedBy { arrival -> arrival.timeToStationSeconds }
                        .take(2)
                        .map { arrival ->
                            BusRoute(
                                busNumber = arrival.lineName,
                                direction = stopPoint.towards ?: arrival.destination,
                                estimatedMinutes = arrival.timeToStationMinutes.coerceAtLeast(0),
                                stopName = listOfNotNull(stopPoint.name, stopPoint.indicator?.takeIf { it.isNotBlank() })
                                    .joinToString(" · "),
                            )
                        }
                }
                .distinctBy { bus -> "${bus.busNumber}-${bus.direction}-${bus.stopName}" }
                .sortedBy { bus -> bus.estimatedMinutes }
                .take(3)
        } catch (e: Exception) {
            Log.e("NearbyDetail", "Failed to fetch bus data for ${station.name}: ${e.message}")
            emptyList()
        }
    }
}
