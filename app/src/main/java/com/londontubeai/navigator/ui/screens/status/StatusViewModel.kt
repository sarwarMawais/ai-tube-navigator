package com.londontubeai.navigator.ui.screens.status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.londontubeai.navigator.data.local.entity.CachedLineStatusEntity
import com.londontubeai.navigator.data.model.LineDetail
import com.londontubeai.navigator.data.model.LineStatus
import com.londontubeai.navigator.data.model.TransportMode
import com.londontubeai.navigator.data.model.TubeData
import com.londontubeai.navigator.data.preferences.AppPreferences
import com.londontubeai.navigator.data.repository.TubeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

// ── Data Models ─────────────────────────────────────────

data class UserProfile(
    val favoriteLines: List<String> = emptyList(),
    val frequentRoutes: List<String> = emptyList(),
    val preferredStations: List<String> = emptyList(),
    val accessibilityNeeds: AccessibilityOptions = AccessibilityOptions(),
    val notificationPreferences: NotificationSettings = NotificationSettings()
)

data class AccessibilityOptions(
    val stepFreeOnly: Boolean = false,
    val avoidStairs: Boolean = false,
    val avoidEscalators: Boolean = false
)

data class NotificationSettings(
    val disruptionAlerts: Boolean = true,
    val serviceResumed: Boolean = true,
    val severeDisruptions: Boolean = true,
    val frequentRoutesOnly: Boolean = false
)

data class NetworkAnalytics(
    val reliabilityScore: Float = 0f,
    val averageDisruptionTime: Int = 0,
    val peakDisruptionHours: List<Int> = emptyList(),
    val goodServicePercentage: Float = 0f,
    val totalDisruptions: Int = 0,
    val lastUpdated: String = ""
)

data class DisruptionAlert(
    val lineId: String,
    val severity: DisruptionSeverity,
    val impact: ImpactLevel,
    val routes: List<String>,
    val estimatedResolution: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class DisruptionSeverity(val value: Int) {
    MINOR(1), MODERATE(2), SEVERE(3), CRITICAL(4)
}

enum class ImpactLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}

data class HistoricalData(
    val lineId: String,
    val disruptionHistory: List<HistoricalDisruption>,
    val reliabilityTrend: List<Float>,
    val averageResolutionTime: Int
)

data class HistoricalDisruption(
    val timestamp: Long,
    val duration: Int,
    val severity: DisruptionSeverity,
    val reason: String
)

// ── Filters ─────────────────────────────────────────────

enum class StatusFilter(val label: String) {
    ALL("All"),
    DISRUPTED("Disrupted"),
    GOOD("Good Service"),
    FAVORITES("Favourites"),
}

enum class SortOption(val label: String) {
    SEVERITY("Severity"),
    LINE_NAME("Line Name"),
    LAST_UPDATED("Last Updated"),
    RELIABILITY("Reliability")
}

// ── UI State ────────────────────────────────────────────

data class StatusUiState(
    val lineStatuses: List<LineStatus> = emptyList(),
    val lineDetails: Map<String, LineDetail> = emptyMap(),
    val userProfile: UserProfile? = null,
    val commuteLineIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val filter: StatusFilter = StatusFilter.ALL,
    val searchQuery: String = "",
    val lastUpdated: String? = null,
    val isLiveConnection: Boolean = false,
    val isUsingCachedData: Boolean = false,
    val expandedCards: Set<String> = emptySet(),
) {
    val goodCount: Int get() = lineStatuses.count { it.isGoodService }
    val disruptedCount: Int get() = lineStatuses.size - goodCount
    val totalCount: Int get() = lineStatuses.size
    val healthPercent: Int get() = if (totalCount > 0) (goodCount * 100 / totalCount) else 0
}

data class LineStatusUiModel(
    val status: LineStatus,
    val lineDetail: LineDetail?,
    val isFavourite: Boolean,
    val affectsCommute: Boolean,
    val statusBadge: String,
    val humanStatus: String,
    val disruptionSummary: String?,
    val estimatedResolution: String?,
    val lineBadgeLabel: String,
)

// ── ViewModel ───────────────────────────────────────────

@HiltViewModel
class StatusViewModel @Inject constructor(
    private val repository: TubeRepository,
    private val prefs: AppPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatusUiState())
    val uiState: StateFlow<StatusUiState> = _uiState.asStateFlow()

    val filteredStatuses: StateFlow<List<LineStatusUiModel>> = combine(
        uiState,
        uiState.map { it.searchQuery }.debounce(250),
    ) { state, debouncedQuery ->
        buildFilteredStatuses(
            statuses = state.lineStatuses,
            lineDetails = state.lineDetails,
            filter = state.filter,
            query = debouncedQuery,
            favoriteLines = state.userProfile?.favoriteLines.orEmpty().toSet(),
            commuteLineIds = state.commuteLineIds,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    private var autoRefreshJob: Job? = null
    private val AUTO_REFRESH_INTERVAL = 30_000L

    init {
        loadStatuses()
        startAutoRefresh()
        loadUserProfile()
    }

    fun loadStatuses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.fetchLiveLineStatuses()
                .onSuccess { statuses ->
                    _uiState.value = _uiState.value.copy(
                        lineStatuses = statuses,
                        lineDetails = buildLineDetails(statuses),
                        isLoading = false,
                        lastUpdated = formatTimestamp(),
                        isLiveConnection = true,
                        isUsingCachedData = false,
                    )
                }
                .onFailure {
                    val cachedStatuses = repository.getCachedLineStatuses().first().map { it.toLineStatus() }
                    val cachedTimestamp = repository.getLastStatusUpdate()?.let { formatTimestamp(it) }

                    _uiState.value = _uiState.value.copy(
                        lineStatuses = if (cachedStatuses.isNotEmpty()) cachedStatuses else _uiState.value.lineStatuses,
                        lineDetails = if (cachedStatuses.isNotEmpty()) buildLineDetails(cachedStatuses) else _uiState.value.lineDetails,
                        isLoading = false,
                        error = if (cachedStatuses.isNotEmpty()) {
                            "Showing cached status data. Pull to refresh when you are back online."
                        } else {
                            "Could not load line statuses. Check your connection."
                        },
                        lastUpdated = cachedTimestamp ?: _uiState.value.lastUpdated,
                        isLiveConnection = false,
                        isUsingCachedData = cachedStatuses.isNotEmpty(),
                    )
                }
        }
    }

    // ── Auto-refresh ────────────────────────────────────

    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(AUTO_REFRESH_INTERVAL)
                loadStatuses()
            }
        }
    }

    private fun stopAutoRefresh() { autoRefreshJob?.cancel() }

    // ── Filtering / Search ──────────────────────────────

    fun setFilter(filter: StatusFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    // ── Card Expand / Favourite ─────────────────────────

    fun toggleCardExpansion(lineId: String) {
        val current = _uiState.value.expandedCards
        _uiState.value = _uiState.value.copy(
            expandedCards = if (lineId in current) current - lineId else current + lineId
        )
    }

    fun toggleFavourite(lineId: String) {
        val profile = _uiState.value.userProfile ?: UserProfile()
        val updated = if (lineId in profile.favoriteLines)
            profile.favoriteLines - lineId else profile.favoriteLines + lineId
        val newProfile = profile.copy(favoriteLines = updated)
        _uiState.value = _uiState.value.copy(userProfile = newProfile)
        viewModelScope.launch {
            prefs.setFavouriteLines(updated.joinToString(","))
            repository.saveUserProfile(newProfile)
        }
    }

    fun getLineDetail(lineId: String): LineDetail? = _uiState.value.lineDetails[lineId]

    // ── Helpers ─────────────────────────────────────────

    private fun loadUserProfile() {
        viewModelScope.launch {
            val raw = prefs.favouriteLines.first()
            val favLines = if (raw.isBlank()) emptyList() else raw.split(",").filter { it.isNotBlank() }
            val homeStationId = prefs.homeStationId.first()
            val workStationId = prefs.workStationId.first()
            val commuteLineIds = if (homeStationId != null && workStationId != null) {
                repository.findRoute(homeStationId, workStationId)
                    ?.legs
                    ?.filter { it.mode == TransportMode.TUBE }
                    ?.map { it.line.id }
                    ?.toSet()
                    .orEmpty()
            } else {
                emptySet()
            }
            val profile = UserProfile(favoriteLines = favLines)
            _uiState.value = _uiState.value.copy(
                userProfile = profile,
                commuteLineIds = commuteLineIds,
            )
        }
    }

    private fun buildLineDetails(statuses: List<LineStatus>): Map<String, LineDetail> {
        return statuses.associate { status ->
            status.lineId to TubeData.getLineDetail(status.lineId)
        }.filterValues { it != null }.mapValues { it.value!! }
    }

    private fun buildFilteredStatuses(
        statuses: List<LineStatus>,
        lineDetails: Map<String, LineDetail>,
        filter: StatusFilter,
        query: String,
        favoriteLines: Set<String>,
        commuteLineIds: Set<String>,
    ): List<LineStatusUiModel> {
        val searched = statuses.filter { status ->
            if (query.isBlank()) return@filter true
            val detail = lineDetails[status.lineId]
            status.lineName.contains(query, ignoreCase = true) ||
                status.statusDescription.contains(query, ignoreCase = true) ||
                (status.reason?.contains(query, ignoreCase = true) == true) ||
                (detail?.firstStation?.contains(query, ignoreCase = true) == true) ||
                (detail?.lastStation?.contains(query, ignoreCase = true) == true) ||
                (detail?.stationNames?.any { it.contains(query, ignoreCase = true) } == true)
        }

        val filtered = when (filter) {
            StatusFilter.ALL -> searched
            StatusFilter.DISRUPTED -> searched.filter { !it.isGoodService }
            StatusFilter.GOOD -> searched.filter { it.isGoodService }
            StatusFilter.FAVORITES -> searched.filter { it.lineId in favoriteLines }
        }

        return filtered
            .map { status ->
                val detail = lineDetails[status.lineId]
                val isFavourite = status.lineId in favoriteLines
                val affectsCommute = status.lineId in commuteLineIds
                LineStatusUiModel(
                    status = status,
                    lineDetail = detail,
                    isFavourite = isFavourite,
                    affectsCommute = affectsCommute,
                    statusBadge = statusBadge(status),
                    humanStatus = humanizeStatus(status),
                    disruptionSummary = disruptionSummary(status, detail, affectsCommute),
                    estimatedResolution = estimateResolution(status),
                    lineBadgeLabel = lineBadgeLabel(status.lineName),
                )
            }
            .sortedWith(
                compareBy<LineStatusUiModel> {
                    when {
                        it.affectsCommute && !it.status.isGoodService -> 0
                        it.isFavourite && !it.status.isGoodService -> 1
                        !it.status.isGoodService -> 2
                        it.affectsCommute -> 3
                        it.isFavourite -> 4
                        else -> 5
                    }
                }.thenBy {
                    if (it.status.isGoodService) Int.MAX_VALUE else it.status.statusSeverity
                }.thenBy { it.status.lineName }
            )
    }

    private fun statusBadge(status: LineStatus): String {
        return when {
            status.isGoodService -> "Good service"
            status.statusSeverity <= 3 -> "Critical"
            status.statusSeverity <= 5 -> "Severe"
            status.statusSeverity <= 8 -> "Minor"
            else -> status.statusDescription
        }
    }

    private fun humanizeStatus(status: LineStatus): String {
        val description = status.statusDescription
        return when {
            status.isGoodService -> "Good service across the full line"
            description.contains("planned closure", ignoreCase = true) -> "Planned closure affecting part of the line"
            description.contains("part closure", ignoreCase = true) -> "Part closure currently in effect"
            description.contains("suspended", ignoreCase = true) -> "Service suspended until further notice"
            description.contains("severe", ignoreCase = true) -> "Severe delays expected"
            description.contains("minor", ignoreCase = true) -> "Minor delays across the line"
            description.contains("delay", ignoreCase = true) -> "Delays reported on this line"
            else -> description
        }
    }

    private fun disruptionSummary(
        status: LineStatus,
        detail: LineDetail?,
        affectsCommute: Boolean,
    ): String? {
        return when {
            affectsCommute && !status.isGoodService -> "Affects your usual commute"
            !status.reason.isNullOrBlank() -> status.reason
            detail != null && !status.isGoodService -> "${detail.firstStation} → ${detail.lastStation} may be affected"
            else -> null
        }
    }

    private fun estimateResolution(status: LineStatus): String? {
        if (status.isGoodService) return null
        val description = status.statusDescription
        return when {
            description.contains("planned closure", ignoreCase = true) -> "Planned until later today"
            description.contains("part closure", ignoreCase = true) -> "Expected to ease later today"
            description.contains("suspended", ignoreCase = true) -> "No resolution estimate yet"
            status.statusSeverity <= 3 -> "Typical recovery ~45 mins"
            status.statusSeverity <= 5 -> "Typical recovery ~30 mins"
            else -> "Typical recovery ~15 mins"
        }
    }

    private fun lineBadgeLabel(lineName: String): String {
        val parts = lineName
            .replace("&", " ")
            .replace("-", " ")
            .split(" ")
            .filter { it.isNotBlank() }
        return when {
            parts.size >= 2 -> parts.take(2).joinToString("") { it.first().uppercase() }
            lineName.length <= 3 -> lineName.uppercase()
            else -> lineName.take(2).uppercase()
        }
    }

    private fun formatTimestamp(): String {
        val c = Calendar.getInstance()
        return String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
    }

    private fun formatTimestamp(epochMillis: Long): String {
        val c = Calendar.getInstance().apply { timeInMillis = epochMillis }
        return String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
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

    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
    }
}
