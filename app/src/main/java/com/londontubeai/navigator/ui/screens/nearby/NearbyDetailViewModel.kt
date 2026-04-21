package com.londontubeai.navigator.ui.screens.nearby

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.londontubeai.navigator.data.local.entity.CachedLineStatusEntity
import com.londontubeai.navigator.data.model.LineStatus
import com.londontubeai.navigator.data.model.RouteSource
import com.londontubeai.navigator.data.model.Station
import com.londontubeai.navigator.data.model.StationArrivals
import com.londontubeai.navigator.data.model.TransportMode
import com.londontubeai.navigator.data.model.TubeData
import com.londontubeai.navigator.data.repository.TubeRepository
import com.londontubeai.navigator.utils.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

enum class NearbyDataSource {
    LIVE,
    CACHED,
    INFERRED,
}

enum class NearbySortMode(val label: String) {
    NEAREST("Nearest"),
    SOONEST_TRAIN("Soonest train"),
    STEP_FREE("Step-free"),
    LEAST_DISRUPTION("Least disruption"),
}

data class NearbyAccessOption(
    val label: String,
    val summary: String,
    val totalMinutes: Int,
    val busRouteNumber: String? = null,
    val source: NearbyDataSource = NearbyDataSource.LIVE,
)

data class NearbyStation(
    val station: Station,
    val distanceKm: Double,
    val walkingMinutes: Int,
    val lineStatuses: List<LineStatus>,
    val busRoutes: List<BusRoute>,
    val arrivals: List<StationArrivalInfo>,
    val isLoadingArrivals: Boolean = false,
    val tubeSource: NearbyDataSource = NearbyDataSource.LIVE,
    val busSource: NearbyDataSource = NearbyDataSource.LIVE,
    val arrivalsUpdatedAt: Long? = null,
    val accessOption: NearbyAccessOption? = null,
    /**
     * True once bus routes and access options have been fetched for this station.
     * Lazily populated on card expand for stations outside the top enrichment tier.
     */
    val enriched: Boolean = false,
    val isEnriching: Boolean = false,
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
    val sortMode: NearbySortMode = NearbySortMode.NEAREST,
    val overviewUpdatedAt: Long? = null,
    val overviewSource: NearbyDataSource = NearbyDataSource.LIVE,
)

@HiltViewModel
class NearbyDetailViewModel @Inject constructor(
    private val repository: TubeRepository,
    private val locationService: LocationService,
) : ViewModel() {

    private val nearbyRadiusKm = 4.8
    private val maxNearbyStations = 12
    private val busStopRadiusMeters = 400
    /** Only the top-N nearest stations get the heavy bus+access enrichment up front. */
    private val enrichNearCount = 6
    private var loadJob: Job? = null
    private var loadGeneration: Int = 0

    private val _uiState = MutableStateFlow(NearbyDetailUiState())
    val uiState: StateFlow<NearbyDetailUiState> = _uiState.asStateFlow()

    init {
        loadNearbyStations()
    }

    fun refresh() {
        loadNearbyStations()
    }

    /**
     * Called when the user expands a station card. If bus routes & access options haven't
     * been fetched yet (i.e. it wasn't in the top enrichment tier), fetch them lazily now.
     */
    fun onStationExpanded(stationId: String) {
        val target = _uiState.value.nearbyStations.firstOrNull { it.station.id == stationId } ?: return
        if (target.enriched || target.isEnriching) return
        applyStationPatch(stationId) { it.copy(isEnriching = true) }
        val userLat = _uiState.value.userLatitude
        val userLng = _uiState.value.userLongitude
        val generation = loadGeneration
        viewModelScope.launch {
            val busRoutes = fetchBusRoutesForStation(target.station)
            val accessOption = fetchAccessOptionForStation(userLat, userLng, target.station)
            if (generation != loadGeneration) return@launch
            applyStationPatch(stationId) { state ->
                state.copy(
                    busRoutes = busRoutes,
                    accessOption = accessOption,
                    busSource = when {
                        busRoutes.isNotEmpty() -> NearbyDataSource.LIVE
                        accessOption?.busRouteNumber != null -> accessOption.source
                        else -> NearbyDataSource.INFERRED
                    },
                    enriched = true,
                    isEnriching = false,
                )
            }
        }
    }

    private fun applyStationPatch(stationId: String, patch: (NearbyStation) -> NearbyStation) {
        val updated = _uiState.value.nearbyStations.map { stationState ->
            if (stationState.station.id == stationId) patch(stationState) else stationState
        }
        _uiState.value = _uiState.value.copy(
            nearbyStations = sortStations(updated, _uiState.value.sortMode),
            overviewUpdatedAt = System.currentTimeMillis(),
            overviewSource = when {
                updated.any { it.tubeSource == NearbyDataSource.CACHED || it.busSource == NearbyDataSource.CACHED } -> NearbyDataSource.CACHED
                updated.any { it.busSource == NearbyDataSource.INFERRED } -> NearbyDataSource.INFERRED
                else -> _uiState.value.overviewSource
            },
        )
    }

    fun setSortMode(sortMode: NearbySortMode) {
        _uiState.value = _uiState.value.copy(
            sortMode = sortMode,
            nearbyStations = sortStations(_uiState.value.nearbyStations, sortMode),
        )
    }

    private fun loadNearbyStations() {
        loadJob?.cancel()
        loadGeneration += 1
        val generation = loadGeneration
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        loadJob = viewModelScope.launch {
            locationService.getCurrentLocation()
                .onSuccess { location ->
                    var usedCachedStatuses = false
                    val allLineStatuses = repository.fetchLiveLineStatuses().getOrElse {
                        usedCachedStatuses = true
                        loadCachedStatuses()
                    }
                    if (generation != loadGeneration) return@onSuccess
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
                            tubeSource = if (usedCachedStatuses) NearbyDataSource.CACHED else NearbyDataSource.LIVE,
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        nearbyStations = sortStations(nearbyList, _uiState.value.sortMode),
                        isLoading = false,
                        overviewUpdatedAt = System.currentTimeMillis(),
                        overviewSource = if (usedCachedStatuses) NearbyDataSource.CACHED else NearbyDataSource.LIVE,
                    )

                    // Two-tier enrichment:
                    //  \u2022 Tier 1 (all nearby): tube arrivals \u2014 cheap single-endpoint call.
                    //  \u2022 Tier 2 (top enrichNearCount by distance): bus routes + access options
                    //    \u2014 each triggers multiple sub-calls so we keep these bounded.
                    //  \u2022 Remaining stations fetch bus/access lazily on expand.
                    val enrichIds = nearbyList.take(enrichNearCount).map { it.station.id }.toSet()
                    nearbyList.forEach { nearbyStation ->
                        launch {
                            val arrivalsDeferred = async { fetchArrivalsForStation(nearbyStation.station) }
                            val shouldEagerEnrich = nearbyStation.station.id in enrichIds
                            val busRoutesDeferred = if (shouldEagerEnrich) {
                                async { fetchBusRoutesForStation(nearbyStation.station) }
                            } else null
                            val accessDeferred = if (shouldEagerEnrich) {
                                async {
                                    fetchAccessOptionForStation(
                                        userLat = location.latitude,
                                        userLng = location.longitude,
                                        station = nearbyStation.station,
                                    )
                                }
                            } else null
                            val stationArrivals = arrivalsDeferred.await()
                            val arrivals = mapStationArrivals(nearbyStation.station, stationArrivals)
                            val busRoutes = busRoutesDeferred?.await() ?: emptyList()
                            val accessOption = accessDeferred?.await()
                            if (generation != loadGeneration) return@launch
                            applyStationPatch(nearbyStation.station.id) { stationState ->
                                stationState.copy(
                                    busRoutes = busRoutes,
                                    arrivals = arrivals,
                                    isLoadingArrivals = false,
                                    tubeSource = if (stationArrivals?.isCached == true) NearbyDataSource.CACHED else NearbyDataSource.LIVE,
                                    busSource = when {
                                        !shouldEagerEnrich -> NearbyDataSource.INFERRED
                                        busRoutes.isNotEmpty() -> NearbyDataSource.LIVE
                                        accessOption?.busRouteNumber != null -> accessOption.source
                                        else -> NearbyDataSource.INFERRED
                                    },
                                    arrivalsUpdatedAt = stationArrivals?.lastUpdated,
                                    accessOption = accessOption,
                                    enriched = shouldEagerEnrich,
                                )
                            }
                        }
                    }
                }
                .onFailure { error ->
                    Log.e("NearbyDetail", "Location failed: ${error.message}")
                    if (generation != loadGeneration) return@onFailure
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Could not get your location. Please check location permissions.",
                    )
                }
        }
    }

    private suspend fun loadCachedStatuses(): List<LineStatus> {
        return repository.getCachedLineStatuses().first().map { it.toLineStatus() }
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
            isGoodService = statusSeverity >= 10,
        )
    }

    private suspend fun fetchArrivalsForStation(station: Station): StationArrivals? {
        return try {
            repository.fetchStationArrivals(station.id)
                .getOrNull()
        } catch (e: Exception) {
            Log.e("NearbyDetail", "Failed to fetch arrivals for ${station.name}: ${e.message}")
            null
        }
    }

    private fun mapStationArrivals(station: Station, stationArrivals: StationArrivals?): List<StationArrivalInfo> {
        return stationArrivals?.arrivals
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
    }

    private fun estimateWalkingTime(distanceKm: Double): Int {
        // 5 km/h average walking pace => 12 min per km.
        return (distanceKm * 12.0).roundToInt().coerceAtLeast(1)
    }

    private fun sortStations(
        stations: List<NearbyStation>,
        sortMode: NearbySortMode,
    ): List<NearbyStation> {
        return when (sortMode) {
            NearbySortMode.NEAREST -> stations.sortedBy { it.distanceKm }
            NearbySortMode.SOONEST_TRAIN -> stations.sortedWith(
                compareBy<NearbyStation> { it.arrivals.minOfOrNull { arrival -> arrival.minutesUntil } ?: Int.MAX_VALUE }
                    .thenBy { it.distanceKm }
            )
            NearbySortMode.STEP_FREE -> stations.sortedWith(
                compareBy<NearbyStation> { !it.station.hasStepFreeAccess }
                    .thenBy { it.distanceKm }
            )
            NearbySortMode.LEAST_DISRUPTION -> stations.sortedWith(
                // Penalise stations with zero tube lines so they don't win the tie at 0 disrupted.
                compareBy<NearbyStation> { it.lineStatuses.isEmpty() }
                    .thenBy { it.lineStatuses.count { status -> !status.isGoodService } }
                    .thenBy { it.distanceKm }
            )
        }
    }

    private suspend fun fetchAccessOptionForStation(
        userLat: Double,
        userLng: Double,
        station: Station,
    ): NearbyAccessOption? {
        return try {
            val routes = repository.fetchAllJourneyRoutesFromTfl(
                fromStationId = "my-location",
                toStationId = station.id,
                preference = "LEAST_WALKING",
                fromLat = userLat,
                fromLng = userLng,
            ).getOrDefault(emptyList())
            val chosenRoute = routes.firstOrNull { route -> route.legs.any { leg -> leg.mode == TransportMode.BUS } }
                ?: routes.firstOrNull()
                ?: return null
            val busLeg = chosenRoute.legs.firstOrNull { leg -> leg.mode == TransportMode.BUS }
            NearbyAccessOption(
                label = if (busLeg != null) {
                    "Walk · Bus ${busLeg.busRouteNumber.ifBlank { busLeg.line.name.removePrefix("Bus ") }} · Walk"
                } else {
                    "Walk to station"
                },
                summary = if (busLeg != null) {
                    "${chosenRoute.totalDurationMinutes} min total · board at ${busLeg.busStopName.ifBlank { "nearby stop" }}"
                } else {
                    "${chosenRoute.totalDurationMinutes} min on foot from your location"
                },
                totalMinutes = chosenRoute.totalDurationMinutes,
                busRouteNumber = busLeg?.busRouteNumber?.ifBlank { busLeg.line.name.removePrefix("Bus ") },
                source = when (chosenRoute.source) {
                    RouteSource.TFL_API -> NearbyDataSource.LIVE
                    RouteSource.CACHE -> NearbyDataSource.CACHED
                    RouteSource.LOCAL_DIJKSTRA -> NearbyDataSource.INFERRED
                },
            )
        } catch (e: Exception) {
            Log.e("NearbyDetail", "Failed to fetch access option for ${station.name}: ${e.message}")
            null
        }
    }

    private suspend fun fetchBusRoutesForStation(station: Station): List<BusRoute> {
        return try {
            repository.fetchNearbyBusStopPoints(
                latitude = station.latitude,
                longitude = station.longitude,
                radiusMeters = busStopRadiusMeters,
            ).getOrDefault(emptyList())
                .take(4)
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
