package com.londontubeai.navigator.ui.screens.station

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.londontubeai.navigator.data.local.entity.CachedLineStatusEntity
import com.londontubeai.navigator.data.model.CarriageRecommendation
import com.londontubeai.navigator.data.model.CommunityTip
import com.londontubeai.navigator.data.model.ConnectingStation
import com.londontubeai.navigator.data.model.CrowdHeatmapEntry
import com.londontubeai.navigator.data.model.CrowdPrediction
import com.londontubeai.navigator.data.model.JourneySuggestion
import com.londontubeai.navigator.data.model.LineStatus
import com.londontubeai.navigator.data.model.NearbyStation
import com.londontubeai.navigator.data.model.PlatformInfo
import com.londontubeai.navigator.data.model.RecentStation
import com.londontubeai.navigator.data.model.Station
import com.londontubeai.navigator.data.model.StationArrivals
import com.londontubeai.navigator.data.model.StationInsight
import com.londontubeai.navigator.data.model.StationReview
import com.londontubeai.navigator.data.model.TubeData
import com.londontubeai.navigator.data.model.TubeLine
import com.londontubeai.navigator.data.preferences.AppPreferences
import com.londontubeai.navigator.data.repository.TubeRepository
import com.londontubeai.navigator.ml.CrowdPredictionEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

// ── List screen state ────────────────────────────────────────

enum class StationFilter { ALL, ZONE, LINE, STEP_FREE, INTERCHANGE, NEARBY, FAVOURITES }
enum class StationSort { NAME, ZONE, LINES, PASSENGERS }
enum class SearchMode { STATION, LANDMARK, ALL }

data class StationListUiState(
    val allStations: List<Station> = emptyList(),
    val searchQuery: String = "",
    val searchMode: SearchMode = SearchMode.ALL,
    val filter: StationFilter = StationFilter.ALL,
    val selectedZone: String? = null,
    val selectedLineId: String? = null,
    val sort: StationSort = StationSort.NAME,
    val favoriteStationIds: Set<String> = emptySet(),
    val lineStatuses: List<LineStatus> = emptyList(),
    val nearbyStations: List<NearbyStation> = emptyList(),
    val recentStations: List<RecentStation> = emptyList(),
    val homeStationId: String? = null,
    val workStationId: String? = null,
    val isLoadingLocation: Boolean = false,
    val isLoadingStatuses: Boolean = true,
    val statusMessage: String? = null,
    val isUsingCachedStatuses: Boolean = false,
) {
    val availableZones: List<String> get() = TubeData.allZones
    val availableLines: List<TubeLine> get() = TubeData.lines
    val totalCount: Int get() = allStations.size
    val stepFreeCount: Int get() = allStations.count { it.hasStepFreeAccess }
    val interchangeCount: Int get() = allStations.count { it.lineIds.size >= 2 }
    val homeStation: Station? get() = homeStationId?.let { TubeData.getStationById(it) }
    val workStation: Station? get() = workStationId?.let { TubeData.getStationById(it) }
    val hasPersonalShortcuts: Boolean get() = homeStationId != null || workStationId != null || recentStations.isNotEmpty()
}

// ── Detail screen state ──────────────────────────────────────

data class StationDetailUiState(
    val station: Station? = null,
    val carriageRecommendation: CarriageRecommendation? = null,
    val crowdPrediction: CrowdPrediction? = null,
    val selectedCrowdDayType: CrowdPredictionEngine.DayType = CrowdPredictionEngine.DayType.AUTO,
    val selectedExitId: String? = null,
    val arrivals: StationArrivals? = null,
    val isLoadingArrivals: Boolean = false,
    val arrivalError: String? = null,
    val isFavourite: Boolean = false,
    val lineStatuses: List<LineStatus> = emptyList(),
    val nearbyStations: List<NearbyStation> = emptyList(),
    val connectingStations: List<ConnectingStation> = emptyList(),
    val linesAtStation: List<TubeLine> = emptyList(),
    val crowdHeatmap: List<CrowdHeatmapEntry> = emptyList(),
    val journeySuggestions: List<JourneySuggestion> = emptyList(),
    val communityTips: List<CommunityTip> = emptyList(),
    val stationReviews: List<StationReview> = emptyList(),
    val stationInsights: List<StationInsight> = emptyList(),
    val platformInfo: List<PlatformInfo> = emptyList(),
    val disruptions: List<LineStatus> = emptyList(),
)

// ── ViewModel ────────────────────────────────────────────────

@HiltViewModel
class StationViewModel @Inject constructor(
    private val repository: TubeRepository,
    private val prefs: AppPreferences,
) : ViewModel() {

    private val _listState = MutableStateFlow(
        StationListUiState(allStations = TubeData.getAllStationsSorted())
    )
    val listState: StateFlow<StationListUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow(StationDetailUiState())
    val detailState: StateFlow<StationDetailUiState> = _detailState.asStateFlow()

    private var searchJob: Job? = null
    private var arrivalsRefreshJob: Job? = null
    private val SEARCH_DEBOUNCE_MS = 250L
    private val ARRIVALS_REFRESH_INTERVAL = 30_000L

    init {
        loadLineStatuses()
        loadFavorites()
        loadPersonalisation()
    }

    // ── List screen ──────────────────────────────────────────

    fun searchStations(query: String) {
        searchJob?.cancel()
        _listState.value = _listState.value.copy(searchQuery = query)
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            // trigger recompose
            _listState.value = _listState.value.copy(searchQuery = query)
        }
    }

    fun setSearchMode(mode: SearchMode) {
        _listState.value = _listState.value.copy(searchMode = mode)
    }

    fun setFilter(filter: StationFilter) {
        _listState.value = _listState.value.copy(
            filter = filter,
            searchQuery = "",
            selectedZone = if (filter == StationFilter.ZONE) _listState.value.selectedZone ?: TubeData.allZones.firstOrNull() else _listState.value.selectedZone,
            selectedLineId = if (filter == StationFilter.LINE) _listState.value.selectedLineId ?: TubeData.lines.firstOrNull()?.id else _listState.value.selectedLineId,
        )
    }

    fun selectZone(zone: String) {
        _listState.value = _listState.value.copy(selectedZone = zone, filter = StationFilter.ZONE)
    }

    fun selectLine(lineId: String) {
        _listState.value = _listState.value.copy(selectedLineId = lineId, filter = StationFilter.LINE)
    }

    fun setSort(sort: StationSort) {
        _listState.value = _listState.value.copy(sort = sort)
    }

    fun getFilteredStations(): List<Station> {
        val s = _listState.value
        val base = when (s.filter) {
            StationFilter.ALL -> TubeData.getAllStationsSorted()
            StationFilter.ZONE -> s.selectedZone?.let { TubeData.getStationsByZone(it) } ?: TubeData.getAllStationsSorted()
            StationFilter.LINE -> s.selectedLineId?.let { TubeData.getStationsForLine(it) } ?: TubeData.getAllStationsSorted()
            StationFilter.STEP_FREE -> TubeData.getStepFreeStations()
            StationFilter.INTERCHANGE -> TubeData.getAllStationsSorted().filter { it.lineIds.size >= 2 }
            StationFilter.NEARBY -> s.nearbyStations.map { it.station }
            StationFilter.FAVOURITES -> s.favoriteStationIds.mapNotNull { TubeData.getStationById(it) }
        }

        val searched = if (s.searchQuery.isBlank()) base
        else when (s.searchMode) {
            SearchMode.STATION -> base.filter { it.name.contains(s.searchQuery, ignoreCase = true) }
            SearchMode.LANDMARK -> TubeData.searchByLandmark(s.searchQuery)
            SearchMode.ALL -> TubeData.advancedSearch(s.searchQuery)
        }

        return when (s.sort) {
            StationSort.NAME -> searched.sortedBy { it.name }
            StationSort.ZONE -> searched.sortedBy { it.zone.replace("-", ".").split(".").first().toIntOrNull() ?: 99 }
            StationSort.LINES -> searched.sortedByDescending { it.lineIds.size }
            StationSort.PASSENGERS -> searched.sortedByDescending { it.annualPassengers }
        }
    }

    fun toggleFavouriteStation(stationId: String) {
        val current = _listState.value.favoriteStationIds
        val updated = if (stationId in current) current - stationId else current + stationId
        _listState.value = _listState.value.copy(favoriteStationIds = updated)
        if (_detailState.value.station?.id == stationId) {
            _detailState.value = _detailState.value.copy(isFavourite = stationId in updated)
        }
        viewModelScope.launch {
            prefs.setFavouriteStations(updated.joinToString(","))
        }
    }

    fun isFavourite(stationId: String): Boolean = stationId in _listState.value.favoriteStationIds

    fun setHomeStation(stationId: String?) {
        _listState.value = _listState.value.copy(homeStationId = stationId)
        viewModelScope.launch {
            prefs.setHomeStationId(stationId)
        }
    }

    fun setWorkStation(stationId: String?) {
        _listState.value = _listState.value.copy(workStationId = stationId)
        viewModelScope.launch {
            prefs.setWorkStationId(stationId)
        }
    }

    fun addRecentStation(stationId: String) {
        val station = TubeData.getStationById(stationId) ?: return
        val current = _listState.value.recentStations.toMutableList()
        current.removeAll { it.stationId == stationId }
        current.add(0, RecentStation(stationId, station.name))
        val updated = current.take(10)
        _listState.value = _listState.value.copy(recentStations = updated)
        viewModelScope.launch {
            prefs.setRecentStations(updated.joinToString(",") { it.stationId })
        }
    }

    fun loadNearbyStations(lat: Double, lng: Double) {
        _listState.value = _listState.value.copy(isLoadingLocation = true)
        viewModelScope.launch {
            val nearby = TubeData.getNearbyStations(lat, lng, radiusKm = 2.0, limit = 15)
            _listState.value = _listState.value.copy(
                nearbyStations = nearby,
                isLoadingLocation = false,
                filter = StationFilter.NEARBY,
            )
        }
    }

    fun getLineStatusForStation(stationId: String): List<LineStatus> {
        val station = TubeData.getStationById(stationId) ?: return emptyList()
        return _listState.value.lineStatuses.filter { it.lineId in station.lineIds }
    }

    fun getLineColor(lineId: String): Color = TubeData.getLineById(lineId)?.color ?: Color.Gray

    fun refreshLineStatuses() { loadLineStatuses() }

    private fun loadLineStatuses() {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoadingStatuses = true, statusMessage = null)
            repository.fetchLiveLineStatuses()
                .onSuccess { statuses ->
                    _listState.value = _listState.value.copy(
                        lineStatuses = statuses,
                        isLoadingStatuses = false,
                        statusMessage = null,
                        isUsingCachedStatuses = false,
                    )
                }
                .onFailure {
                    val cachedStatuses = repository.getCachedLineStatuses().first().map { it.toLineStatus() }
                    _listState.value = _listState.value.copy(
                        lineStatuses = if (cachedStatuses.isNotEmpty()) cachedStatuses else _listState.value.lineStatuses,
                        isLoadingStatuses = false,
                        statusMessage = if (cachedStatuses.isNotEmpty()) {
                            "Line status is currently using cached data."
                        } else {
                            "Live line status is unavailable right now."
                        },
                        isUsingCachedStatuses = cachedStatuses.isNotEmpty(),
                    )
                }
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            val raw = prefs.favouriteStations.first()
            val ids = if (raw.isBlank()) emptySet() else raw.split(",").toSet()
            _listState.value = _listState.value.copy(favoriteStationIds = ids)
        }
    }

    private fun loadPersonalisation() {
        viewModelScope.launch {
            val homeId = prefs.homeStationId.first()
            val workId = prefs.workStationId.first()
            val recentRaw = prefs.recentStations.first()
            val recentList = if (recentRaw.isBlank()) emptyList() else {
                recentRaw.split(",").mapNotNull { id ->
                    TubeData.getStationById(id)?.let { RecentStation(id, it.name) }
                }
            }
            _listState.value = _listState.value.copy(
                homeStationId = homeId,
                workStationId = workId,
                recentStations = recentList,
            )
        }
    }

    // ── Detail screen ────────────────────────────────────────

    fun loadStation(stationId: String) {
        val station = TubeData.getStationById(stationId) ?: return
        val bestExit = repository.getBestExitForStation(station)
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val selectedCrowdDayType = _detailState.value.selectedCrowdDayType
        val crowd = repository.predictCrowding(stationId, hour, selectedCrowdDayType)
        val selectedExitId = station.exits.find { it.name == bestExit?.exitName }?.id
            ?: station.exits.firstOrNull()?.id

        val linesAtStation = station.lineIds.mapNotNull { TubeData.getLineById(it) }
        val lineStatuses = _listState.value.lineStatuses.filter { it.lineId in station.lineIds }
        val disruptions = lineStatuses.filter { !it.isGoodService }
        val nearby = TubeData.getNearbyStations(station.latitude, station.longitude, radiusKm = 1.0, limit = 6)
            .filter { it.station.id != stationId }
        val connecting = TubeData.getConnectingStations(stationId, maxStops = 3)
        val heatmap = TubeData.generateCrowdHeatmap(stationId, station.peakCrowdMultiplier)
        val suggestions = TubeData.getJourneySuggestions(stationId)
        val tips = TubeData.generateCommunityTips(stationId)
        val reviews = TubeData.generateStationReviews(stationId)
        val insights = TubeData.generateStationInsights(stationId, hour)
        val platforms = TubeData.getPlatformInfo(stationId)

        _detailState.value = StationDetailUiState(
            station = station,
            carriageRecommendation = bestExit,
            crowdPrediction = crowd,
            selectedCrowdDayType = selectedCrowdDayType,
            selectedExitId = selectedExitId,
            isLoadingArrivals = true,
            isFavourite = isFavourite(stationId),
            lineStatuses = lineStatuses,
            nearbyStations = nearby,
            connectingStations = connecting,
            linesAtStation = linesAtStation,
            crowdHeatmap = heatmap,
            journeySuggestions = suggestions,
            communityTips = tips,
            stationReviews = reviews,
            stationInsights = insights,
            platformInfo = platforms,
            disruptions = disruptions,
        )

        addRecentStation(stationId)
        refreshArrivals()
        startArrivalsAutoRefresh()
    }

    fun selectCrowdDayType(dayType: CrowdPredictionEngine.DayType) {
        val station = _detailState.value.station ?: return
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        _detailState.value = _detailState.value.copy(
            selectedCrowdDayType = dayType,
            crowdPrediction = repository.predictCrowding(station.id, hour, dayType),
        )
    }

    fun selectExit(exitId: String) {
        val station = _detailState.value.station ?: return
        val rec = repository.getCarriageRecommendation(station, exitId)
        _detailState.value = _detailState.value.copy(
            selectedExitId = exitId,
            carriageRecommendation = rec,
        )
    }

    fun refreshArrivals() {
        val station = _detailState.value.station ?: return
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isLoadingArrivals = true, arrivalError = null)
            repository.fetchStationArrivals(station.id)
                .onSuccess { arrivals ->
                    _detailState.value = _detailState.value.copy(
                        arrivals = arrivals, isLoadingArrivals = false, arrivalError = null,
                    )
                }
                .onFailure {
                    _detailState.value = _detailState.value.copy(
                        isLoadingArrivals = false,
                        arrivalError = "Live arrivals are unavailable right now.",
                    )
                }
        }
    }

    private fun startArrivalsAutoRefresh() {
        arrivalsRefreshJob?.cancel()
        arrivalsRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(ARRIVALS_REFRESH_INTERVAL)
                refreshArrivals()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        arrivalsRefreshJob?.cancel()
    }

    private fun CachedLineStatusEntity.toLineStatus(): LineStatus {
        val tubeColor = TubeData.getLineById(lineId)?.color ?: Color.Gray
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
