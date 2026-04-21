package com.londontubeai.navigator.ui.screens.station

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.AccessibleForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.londontubeai.navigator.data.model.CarriageRecommendation
import com.londontubeai.navigator.data.model.ConnectingStation
import com.londontubeai.navigator.data.model.CrowdHeatmapEntry
import com.londontubeai.navigator.data.model.CrowdLevel
import com.londontubeai.navigator.data.model.CrowdPrediction
import com.londontubeai.navigator.data.model.LineStatus
import com.londontubeai.navigator.data.model.NearbyStation
import com.londontubeai.navigator.data.model.Station
import com.londontubeai.navigator.data.model.StationExit
import com.londontubeai.navigator.data.model.TubeData
import com.londontubeai.navigator.ui.components.CarriageVisualizer
import com.londontubeai.navigator.ui.components.StationArrivalsCard
import com.londontubeai.navigator.ui.components.TubeLineChip
import com.londontubeai.navigator.ui.theme.Spacing
import com.londontubeai.navigator.ui.theme.StatusGood
import com.londontubeai.navigator.ui.theme.StatusMinor
import com.londontubeai.navigator.ui.theme.StatusSevere
import com.londontubeai.navigator.ui.theme.TubePrimary
import com.londontubeai.navigator.ui.theme.TubeSecondary
import com.londontubeai.navigator.ui.components.UnifiedHeader
import com.londontubeai.navigator.ml.CrowdPredictionEngine
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

// ════════════════════════════════════════════════════════════
//  Station List Screen
// ════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationListScreen(
    onStationClick: (String) -> Unit,
    viewModel: StationViewModel = hiltViewModel(),
) {
    val listState by viewModel.listState.collectAsStateWithLifecycle()
    val filteredStations = viewModel.getFilteredStations()
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Header ───────────────────────────────────────────
        ListHeader(
            totalCount = listState.totalCount,
            filteredCount = filteredStations.size,
            stepFreeCount = listState.stepFreeCount,
        )

        // ── Search + Sort ────────────────────────────────────
        ListSearchBar(
            query = listState.searchQuery,
            onQueryChange = { viewModel.searchStations(it) },
            onClear = { viewModel.searchStations(""); focusManager.clearFocus() },
            sort = listState.sort,
            onSortChange = { viewModel.setSort(it) },
            searchMode = listState.searchMode,
            onSearchModeChange = { viewModel.setSearchMode(it) },
        )

        // ── Filter Chips ─────────────────────────────────────
        ListFilterRow(
            state = listState,
            filteredCount = filteredStations.size,
            onFilterSelect = { viewModel.setFilter(it) },
            onZoneSelect = { viewModel.selectZone(it) },
            onLineSelect = { viewModel.selectLine(it) },
        )

        // ── Match count + Clear filters ──────────────────────
        val filtersActive = listState.filter != StationFilter.ALL ||
            listState.selectedZone != null ||
            listState.selectedLineId != null ||
            listState.searchQuery.isNotBlank()
        if (filtersActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screenHorizontal, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TubePrimary.copy(alpha = 0.1f),
                ) {
                    Text(
                        "${filteredStations.size} of ${listState.totalCount} stations",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TubePrimary,
                    )
                }
                Spacer(Modifier.weight(1f))
                Surface(
                    onClick = { viewModel.clearAllFilters(); focusManager.clearFocus() },
                    shape = RoundedCornerShape(8.dp),
                    color = StatusSevere.copy(alpha = 0.08f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            null,
                            tint = StatusSevere,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Clear filters",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = StatusSevere,
                        )
                    }
                }
            }
        }

        val statusMessage = listState.statusMessage
        if (statusMessage != null) {
            ListStatusBanner(
                message = statusMessage,
                isCached = listState.isUsingCachedStatuses,
            )
        }

        // ── Station List ─────────────────────────────────────
        LazyColumn(
            contentPadding = PaddingValues(top = Spacing.xs, bottom = Spacing.lg),
            modifier = Modifier.fillMaxSize(),
        ) {
            // Home / Work shortcuts
            if (listState.hasPersonalShortcuts && listState.searchQuery.isBlank() && listState.filter == StationFilter.ALL) {
                item {
                    PersonalShortcuts(
                        homeStation = listState.homeStation,
                        workStation = listState.workStation,
                        recentStations = listState.recentStations,
                        onStationClick = onStationClick,
                    )
                }
            }

            items(filteredStations, key = { it.id }) { station ->
                val lineStatuses = viewModel.getLineStatusForStation(station.id)
                StationListItem(
                    station = station,
                    lineStatuses = lineStatuses,
                    isFavourite = viewModel.isFavourite(station.id),
                    onClick = { onStationClick(station.id) },
                    onToggleFavourite = { viewModel.toggleFavouriteStation(station.id) },
                )
            }
            if (filteredStations.isEmpty()) {
                item { EmptyStationList(listState.filter) }
            }
        }
    }
}

@Composable
private fun ListStatusBanner(message: String, isCached: Boolean) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xs),
        shape = RoundedCornerShape(14.dp),
        color = if (isCached) StatusMinor.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isCached) Icons.Filled.Info else Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = if (isCached) StatusMinor else StatusGood,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

// ── Personal Shortcuts (Home / Work / Recent) ────────────────

@Composable
private fun PersonalShortcuts(
    homeStation: Station?,
    workStation: Station?,
    recentStations: List<com.londontubeai.navigator.data.model.RecentStation>,
    onStationClick: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xs),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    ) {
        Column(Modifier.padding(horizontal = Spacing.md, vertical = Spacing.md)) {
            Text(
                "Your shortcuts",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(Spacing.xs))
        // Home + Work row
        if (homeStation != null || workStation != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                if (homeStation != null) {
                    ShortcutChip(
                        label = "Home",
                        stationName = homeStation.name,
                        icon = Icons.Filled.Home,
                        color = StatusGood,
                        modifier = Modifier.weight(1f),
                        onClick = { onStationClick(homeStation.id) },
                    )
                }
                if (workStation != null) {
                    ShortcutChip(
                        label = "Work",
                        stationName = workStation.name,
                        icon = Icons.Filled.Work,
                        color = TubePrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onStationClick(workStation.id) },
                    )
                }
            }
            Spacer(Modifier.height(Spacing.sm))
        }

        // Recent stations
        if (recentStations.isNotEmpty()) {
            Text("Recent", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                items(recentStations.size) { i ->
                    val rs = recentStations[i]
                    Surface(
                        modifier = Modifier.clickable { onStationClick(rs.stationId) },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f),
                    ) {
                        Row(Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.History, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text(rs.stationName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(rs.timeAgo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .6f), fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun ShortcutChip(
    label: String,
    stationName: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = .1f),
    ) {
        Row(Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(18.dp), tint = color)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
                Text(stationName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// ── List Header ──────────────────────────────────────────────

@Composable
private fun ListHeader(totalCount: Int, filteredCount: Int, stepFreeCount: Int) {
    UnifiedHeader(
        title = "Stations",
        subtitle = "$totalCount stations  •  $stepFreeCount step-free",
        icon = Icons.Filled.Place,
        trailingContent = {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.15f),
            ) {
                Text(
                    "$filteredCount",
                    Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        },
    )
}

// ── Search Bar with Sort ─────────────────────────────────────

@Composable
private fun ListSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    sort: StationSort,
    onSortChange: (StationSort) -> Unit,
    searchMode: SearchMode = SearchMode.ALL,
    onSearchModeChange: (SearchMode) -> Unit = {},
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Column {
        Row(
            Modifier.padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        when (searchMode) {
                            SearchMode.STATION -> "Search by station name..."
                            SearchMode.LANDMARK -> "Search by landmark or street..."
                            SearchMode.ALL -> "Search stations, landmarks, lines..."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                leadingIcon = {
                    Icon(Icons.Filled.Search, null, Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = onClear) {
                            Icon(Icons.Filled.Close, "Clear", Modifier.size(18.dp))
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TubePrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                ),
            )
            Spacer(Modifier.width(6.dp))
            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.Filled.SwapVert, "Sort", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                    StationSort.entries.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    when (option) {
                                        StationSort.NAME -> "Name (A-Z)"
                                        StationSort.ZONE -> "Zone"
                                        StationSort.LINES -> "Most Lines"
                                        StationSort.PASSENGERS -> "Busiest"
                                    },
                                    fontWeight = if (option == sort) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            onClick = { onSortChange(option); showSortMenu = false },
                        )
                    }
                }
            }
        }
        // Search mode chips — only visible when actively searching
        AnimatedVisibility(visible = query.isNotEmpty()) {
            Row(
                Modifier.padding(horizontal = Spacing.screenHorizontal).padding(bottom = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val modes = listOf(
                    SearchMode.ALL to "All",
                    SearchMode.STATION to "Station",
                    SearchMode.LANDMARK to "Landmark",
                )
                modes.forEach { (mode, label) ->
                    FilterChip(
                        selected = searchMode == mode,
                        onClick = { onSearchModeChange(mode) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(28.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TubeSecondary,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
            }
        }
    }
}

// ── Filter Row ───────────────────────────────────────────────

@Composable
private fun ListFilterRow(
    state: StationListUiState,
    filteredCount: Int,
    onFilterSelect: (StationFilter) -> Unit,
    onZoneSelect: (String) -> Unit,
    onLineSelect: (String) -> Unit,
) {
    Column {
        // Primary filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val filters = listOf(
                StationFilter.ALL to "All",
                StationFilter.FAVOURITES to "\u2764 Favourites",
                StationFilter.ZONE to "Zone",
                StationFilter.LINE to "Line",
                StationFilter.STEP_FREE to "Step-Free (${state.stepFreeCount})",
                StationFilter.INTERCHANGE to "Interchanges (${state.interchangeCount})",
            )
            items(filters.size) { i ->
                val (filter, label) = filters[i]
                FilterChip(
                    selected = state.filter == filter,
                    onClick = { onFilterSelect(filter) },
                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TubePrimary,
                        selectedLabelColor = Color.White,
                    ),
                )
            }
        }

        // Zone sub-chips
        AnimatedVisibility(visible = state.filter == StationFilter.ZONE) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(state.availableZones.size) { i ->
                    val zone = state.availableZones[i]
                    FilterChip(
                        selected = state.selectedZone == zone,
                        onClick = { onZoneSelect(zone) },
                        label = { Text("Zone $zone", style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TubeSecondary,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
            }
        }

        // Line sub-chips
        AnimatedVisibility(visible = state.filter == StationFilter.LINE) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(state.availableLines.size) { i ->
                    val line = state.availableLines[i]
                    FilterChip(
                        selected = state.selectedLineId == line.id,
                        onClick = { onLineSelect(line.id) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(8.dp).clip(CircleShape).background(line.color))
                                Spacer(Modifier.width(6.dp))
                                Text(line.name, style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = line.color.copy(alpha = .85f),
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
            }
        }
    }
}

// ── Station List Item ────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StationListItem(
    station: Station,
    lineStatuses: List<LineStatus>,
    isFavourite: Boolean,
    onClick: () -> Unit,
    onToggleFavourite: () -> Unit,
) {
    val hasDisruption = lineStatuses.any { !it.isGoodService }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xs)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Station icon with line color accent
                val primaryColor = station.lineIds.firstOrNull()
                    ?.let { TubeData.getLineById(it)?.color } ?: TubePrimary
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(primaryColor.copy(alpha = .12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.DirectionsSubway, null,
                        Modifier.size(22.dp), tint = primaryColor,
                    )
                }
                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            station.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (hasDisruption) {
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Filled.Warning, null, Modifier.size(14.dp), tint = StatusMinor)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        station.lineIds.forEach { lineId ->
                            val line = TubeData.getLineById(lineId)
                            if (line != null) {
                                Box(
                                    Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(line.color)
                                )
                            }
                        }
                        Text(
                            "${station.lineIds.size} line${if (station.lineIds.size > 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Right side: zone + step-free + favourite
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Zone ${station.zone}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (station.hasStepFreeAccess) {
                            Icon(
                                Icons.AutoMirrored.Filled.AccessibleForward, "Step-free",
                                Modifier.size(14.dp), tint = StatusGood,
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        IconButton(onClick = onToggleFavourite, modifier = Modifier.size(28.dp)) {
                            Icon(
                                if (isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                "Favourite", Modifier.size(16.dp),
                                tint = if (isFavourite) StatusSevere else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .4f),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xs))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                ) {
                    Text(
                        text = if (station.annualPassengers > 0) "${station.annualPassengers.toInt()}M riders/yr" else "Passenger data unavailable",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Live line status mini-row (only if disruption)
            if (hasDisruption) {
                Spacer(Modifier.height(Spacing.sm))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusMinor.copy(alpha = .08f))
                        .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    lineStatuses.filter { !it.isGoodService }.take(3).forEach { status ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(status.lineColor)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                status.statusDescription,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (status.statusSeverity < 5) StatusSevere else StatusMinor,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Empty State ──────────────────────────────────────────────

@Composable
private fun EmptyStationList(filter: StationFilter) {
    Box(Modifier.fillMaxSize().padding(Spacing.xxxl), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Place, null, Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .3f),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                when (filter) {
                    StationFilter.STEP_FREE -> "No step-free stations match"
                    StationFilter.NEARBY -> "No nearby stations found"
                    else -> "No stations match your search"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ════════════════════════════════════════════════════════════
//  Station Detail Screen
// ════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StationDetailScreen(
    stationId: String,
    onBack: () -> Unit,
    onStationClick: (String) -> Unit = {},
    viewModel: StationViewModel = hiltViewModel(),
) {
    val state by viewModel.detailState.collectAsStateWithLifecycle()
    val listState by viewModel.listState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(stationId) { viewModel.loadStation(stationId) }

    val station = state.station
    val arrivals = state.arrivals
    val isHome = station?.id != null && station.id == listState.homeStationId
    val isWork = station?.id != null && station.id == listState.workStationId

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Top bar with Share / Open-in-Maps / Home-Work / Favourite ──
        TopAppBar(
            title = { Text(station?.name ?: "Station", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            actions = {
                if (station != null) {
                    // Open in Maps — launches walking directions
                    IconButton(onClick = {
                        val uri = Uri.parse("geo:${station.latitude},${station.longitude}?q=${station.latitude},${station.longitude}(${Uri.encode(station.name)})")
                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        runCatching { context.startActivity(intent) }.onFailure {
                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                        }
                    }) {
                        Icon(
                            Icons.Filled.Map,
                            "Open in Maps",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    // Share
                    IconButton(onClick = {
                        val shareText = buildString {
                            append(station.name)
                            append(" — Zone ").append(station.zone)
                            append("\nLines: ").append(station.lineIds.mapNotNull { TubeData.getLineById(it)?.name }.joinToString(", "))
                            append("\nhttps://www.google.com/maps/search/?api=1&query=${station.latitude},${station.longitude}")
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, station.name)
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        runCatching { context.startActivity(Intent.createChooser(intent, "Share station")) }
                    }) {
                        Icon(
                            Icons.Filled.Share,
                            "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    // Set/unset Home
                    IconButton(onClick = {
                        viewModel.setHomeStation(if (isHome) null else station.id)
                    }) {
                        Icon(
                            Icons.Filled.Home,
                            "Set as Home",
                            tint = if (isHome) StatusGood else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    // Set/unset Work
                    IconButton(onClick = {
                        viewModel.setWorkStation(if (isWork) null else station.id)
                    }) {
                        Icon(
                            Icons.Filled.Work,
                            "Set as Work",
                            tint = if (isWork) TubePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { viewModel.toggleFavouriteStation(station.id) }) {
                        Icon(
                            if (state.isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            "Favourite",
                            tint = if (state.isFavourite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
        )

        if (station == null) return

        LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {

            // ── 1. Station Info Header ───────────────────────
            item { DetailHeaderCard(station = station) }

            // ── 2. Disruption Alert Banner ───────────────────
            if (state.disruptions.isNotEmpty()) {
                item { DisruptionBanner(state.disruptions) }
            }

            // ── 3. Smart Insights ────────────────────────────
            if (state.stationInsights.isNotEmpty()) {
                item { InsightsStrip(state.stationInsights) }
            }

            // ── 4. Quick Stats ───────────────────────────────────
            item { StationSnapshotCard(station = station) }

            // ── 4b. First / Last Train card per line ──────────────
            if (state.linesAtStation.isNotEmpty()) {
                item { DetailSectionTitle("First · Last Train", Icons.Filled.AccessTime) }
                item { FirstLastTrainCard(lines = state.linesAtStation) }
            }

            // ── 5. Line Status at this Station ───────────────
            if (state.lineStatuses.isNotEmpty()) {
                item { DetailSectionTitle("Line Status", Icons.Filled.Train) }
                item { LineStatusSection(statuses = state.lineStatuses) }
            }

            // ── 6. Platform Map ──────────────────────────────
            if (state.platformInfo.isNotEmpty()) {
                item { DetailSectionTitle("Platforms", Icons.Filled.Map) }
                item { PlatformMapSection(state.platformInfo) }
            }

            // ── 7. Live Arrivals ─────────────────────────────
            if (arrivals != null) {
                item {
                    StationArrivalsCard(
                        arrivals = arrivals,
                        isLoading = state.isLoadingArrivals,
                        onRefresh = { viewModel.refreshArrivals() },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = Spacing.sm),
                    )
                }
            } else if (state.isLoadingArrivals || state.arrivalError != null) {
                item { ArrivalsLoadingCard(state.isLoadingArrivals, state.arrivalError) }
            }

            // ── 8. Crowd Prediction + Heatmap ────────────────
            val crowd = state.crowdPrediction
            if (crowd != null) {
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(CrowdPredictionEngine.DayType.entries.toList()) { dayType ->
                            val selected = state.selectedCrowdDayType == dayType
                            val label = when (dayType) {
                                CrowdPredictionEngine.DayType.AUTO -> "Auto"
                                CrowdPredictionEngine.DayType.WEEKDAY -> "Weekday"
                                CrowdPredictionEngine.DayType.WEEKEND -> "Weekend"
                            }
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.selectCrowdDayType(dayType) },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TubePrimary.copy(alpha = 0.15f),
                                    selectedLabelColor = TubePrimary,
                                ),
                            )
                        }
                    }
                }
                item { CrowdCard(crowd) }
            }
            if (state.crowdHeatmap.isNotEmpty()) {
                item { CrowdHeatmapCard(state.crowdHeatmap) }
            }

            // ── 9. Journey Suggestions ───────────────────────
            if (state.journeySuggestions.isNotEmpty()) {
                item { DetailSectionTitle("Routes From Here", Icons.Filled.Navigation) }
                item { JourneySuggestionsSection(state.journeySuggestions, onStationClick) }
            }

            // ── 10. Facilities ───────────────────────────────
            if (station.facilities.isNotEmpty()) {
                item { DetailSectionTitle("Facilities", Icons.Filled.CheckCircle) }
                item { FacilitiesSection(station) }
            }

            // ── 11. Exits & AI Carriage Guide ────────────────
            if (station.exits.isNotEmpty()) {
                item { DetailSectionTitle("Exits & AI Carriage Guide", Icons.AutoMirrored.Filled.ExitToApp) }
                item { ExitChips(exits = station.exits, selectedId = state.selectedExitId, onSelect = { viewModel.selectExit(it) }) }

                val selectedExit = station.exits.find { it.id == state.selectedExitId }
                if (selectedExit != null) {
                    item {
                        ExitDetailCard(exit = selectedExit, station = station, recommendation = state.carriageRecommendation)
                    }
                }

                items(station.exits) { exit -> ExitListItem(exit = exit) }
            }

            // ── 12. Connecting Stations ──────────────────────
            if (state.connectingStations.isNotEmpty()) {
                item { DetailSectionTitle("Connecting Stations", Icons.Filled.DirectionsSubway) }
                item { ConnectingStationsSection(state.connectingStations, onStationClick) }
            }

            // ── 13. Nearby Stations ──────────────────────────
            if (state.nearbyStations.isNotEmpty()) {
                item { DetailSectionTitle("Nearby Stations", Icons.AutoMirrored.Filled.DirectionsWalk) }
                item { NearbyStationsSection(state.nearbyStations, onStationClick) }
            }

            // ── 14. Community Tips ───────────────────────────
            if (state.communityTips.isNotEmpty()) {
                item { DetailSectionTitle("Community Tips", Icons.Filled.Lightbulb) }
                item { CommunityTipsSection(state.communityTips) }
            }

            // ── 15. Station Reviews ──────────────────────────────
            if (state.stationReviews.isNotEmpty()) {
                val avgReviewRating = state.stationReviews.map { it.rating }.average().toFloat()
                item {
                    Row(
                        Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Star, null, Modifier.size(20.dp), tint = TubePrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Reviews", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFC107).copy(alpha = 0.22f)) {
                            Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, null, Modifier.size(13.dp), tint = Color(0xFFFFC107))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    String.format("%.1f", avgReviewRating),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100),
                                )
                                Text(
                                    " / 5",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
                item { StationReviewsSection(state.stationReviews) }
            }
        }
    }
}

// ── Detail Header Card ───────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailHeaderCard(station: Station) {
    val passengerLabel = when {
        station.annualPassengers >= 100 -> "${station.annualPassengers.toInt()}M/yr"
        station.annualPassengers > 0 -> "${String.format("%.1f", station.annualPassengers)}M/yr"
        else -> null
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.xl, vertical = Spacing.sm),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = TubePrimary.copy(alpha = 0.06f)),
    ) {
        Column(Modifier.padding(20.dp)) {
            // Zone + badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TubePrimary.copy(alpha = 0.12f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Place, null, Modifier.size(13.dp), tint = TubePrimary)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Zone ${station.zone}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TubePrimary,
                        )
                    }
                }
                if (station.hasStepFreeAccess) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StatusGood.copy(alpha = 0.1f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.AutoMirrored.Filled.AccessibleForward, null, Modifier.size(13.dp), tint = StatusGood)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Step-free",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = StatusGood,
                            )
                        }
                    }
                }
                if (passengerLabel != null) {
                    Spacer(Modifier.weight(1f))
                    Text(
                        "~$passengerLabel riders",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            // Line chips
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                station.lineIds.forEach { lineId ->
                    val line = TubeData.getLineById(lineId)
                    if (line != null) TubeLineChip(lineName = line.name, lineColor = line.color)
                }
            }
            // Facilities quick row
            if (station.facilities.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    station.facilities.take(4).forEach { f ->
                        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)) {
                            Text(
                                f.label,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (station.facilities.size > 4) {
                        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)) {
                            Text(
                                "+${station.facilities.size - 4} more",
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── First / Last Train Card ──────────────────────────────────

@Composable
private fun FirstLastTrainCard(lines: List<com.londontubeai.navigator.data.model.TubeLine>) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            lines.forEachIndexed { idx, line ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(line.color),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        line.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = line.color.copy(alpha = 0.1f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.AccessTime,
                                null,
                                tint = line.color,
                                modifier = Modifier.size(12.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${line.firstTrain} \u2013 ${line.lastTrain}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = line.color,
                            )
                        }
                    }
                }
                if (idx < lines.lastIndex) {
                    Spacer(Modifier.height(8.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Service hours shown are typical weekday. Night Tube and weekend schedules may differ.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 10.sp,
            )
        }
    }
}

// ── Section Title ────────────────────────────────────────────

@Composable
private fun DetailSectionTitle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, Modifier.size(20.dp), tint = TubePrimary)
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

// ── Line Status Section ──────────────────────────────────────

@Composable
private fun LineStatusSection(statuses: List<LineStatus>) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        statuses.forEach { status ->
            val statusColor = when {
                status.statusSeverity >= 10 -> StatusGood
                status.statusSeverity >= 5 -> StatusMinor
                else -> StatusSevere
            }
            val statusIcon = when {
                status.statusSeverity >= 10 -> Icons.Filled.CheckCircle
                status.statusSeverity >= 5 -> Icons.Filled.Warning
                else -> Icons.Filled.ErrorOutline
            }
            Surface(
                Modifier.fillMaxWidth().padding(vertical = 3.dp),
                shape = RoundedCornerShape(12.dp),
                color = statusColor.copy(alpha = .06f),
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(status.lineColor))
                    Spacer(Modifier.width(10.dp))
                    Text(status.lineName, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Icon(statusIcon, null, Modifier.size(14.dp), tint = statusColor)
                    Spacer(Modifier.width(6.dp))
                    Text(status.statusDescription, style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

// ── Arrivals Loading Card ────────────────────────────────────

@Composable
private fun ArrivalsLoadingCard(isLoading: Boolean, error: String?) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Live Arrivals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                if (isLoading) "Loading platforms and next trains..." else error ?: "Unavailable right now.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Crowd Prediction Card ────────────────────────────────────

@Composable
private fun CrowdCard(crowd: com.londontubeai.navigator.data.model.CrowdPrediction) {
    val crowdColor = when (crowd.crowdLevel) {
        CrowdLevel.LOW -> StatusGood
        CrowdLevel.MODERATE -> Color(0xFFF59E0B)
        CrowdLevel.HIGH -> StatusMinor
        CrowdLevel.VERY_HIGH -> StatusSevere
        CrowdLevel.EXTREME -> Color(0xFF7C3AED)
    }
    val animatedFill by animateFloatAsState(
        targetValue = crowd.percentageFull / 100f,
        animationSpec = tween(durationMillis = 700),
        label = "crowd_fill",
    )
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Group, null, Modifier.size(18.dp), tint = crowdColor)
                    Spacer(Modifier.width(6.dp))
                    Text("Crowd Forecast", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                Surface(shape = RoundedCornerShape(8.dp), color = crowdColor.copy(alpha = 0.12f)) {
                    Text(
                        "${crowd.crowdLevel.emoji} ${crowd.crowdLevel.label}",
                        Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = crowdColor,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        Modifier.fillMaxHeight().fillMaxWidth(animatedFill)
                            .clip(RoundedCornerShape(5.dp)).background(crowdColor),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "${crowd.percentageFull}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = crowdColor,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                crowd.recommendation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
            )
            if (crowd.bestAlternativeTime != null) {
                Spacer(Modifier.height(8.dp))
                Surface(shape = RoundedCornerShape(10.dp), color = StatusGood.copy(alpha = 0.1f)) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Schedule, null, tint = StatusGood, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Quieter around ${crowd.bestAlternativeTime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusGood,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                crowd.timeSlot,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        }
    }
}

// ── Facilities Section ───────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FacilitiesSection(station: Station) {
    FlowRow(
        Modifier.padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        station.facilities.forEach { facility ->
            Row(
                Modifier.clip(RoundedCornerShape(8.dp)).background(StatusGood.copy(alpha = 0.08f)).padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.CheckCircle, null, Modifier.size(12.dp), tint = StatusGood)
                Spacer(Modifier.width(4.dp))
                Text(facility.label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ── Exit Chips ───────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExitChips(exits: List<StationExit>, selectedId: String?, onSelect: (String) -> Unit) {
    FlowRow(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        exits.forEach { exit ->
            FilterChip(
                selected = selectedId == exit.id,
                onClick = { onSelect(exit.id) },
                label = { Text(exit.name, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TubePrimary, selectedLabelColor = Color.White),
            )
        }
    }
    Spacer(Modifier.height(8.dp))
}

// ── Exit Detail Card ─────────────────────────────────────────

@Composable
private fun ExitDetailCard(exit: StationExit, station: Station, recommendation: CarriageRecommendation?) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StatusGood.copy(alpha = 0.06f)),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(StatusGood.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Lightbulb, null, Modifier.size(18.dp), tint = StatusGood)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("${exit.name}: ${exit.description}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    if (recommendation != null) {
                        Text(
                            "Board carriage ${recommendation.carriageNumber} • Save ~${recommendation.timeSavedSeconds}s",
                            style = MaterialTheme.typography.bodySmall, color = StatusGood, fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
            if (recommendation != null) {
                Spacer(Modifier.height(16.dp))
                val lineColor = station.lineIds.firstOrNull()?.let { TubeData.getLineById(it)?.color } ?: TubePrimary
                CarriageVisualizer(totalCarriages = station.totalCarriages, recommendedCarriage = recommendation.carriageNumber, lineColor = lineColor)
            }
            if (exit.nearbyLandmarks.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("Nearby: ${exit.nearbyLandmarks.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
            }
            if (exit.streetName.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Place, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(exit.streetName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ── Connecting Stations Section ──────────────────────────────

@Composable
private fun ConnectingStationsSection(stations: List<ConnectingStation>, onStationClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(stations.size) { i ->
            val cs = stations[i]
            Surface(
                modifier = Modifier.width(140.dp).clickable { onStationClick(cs.station.id) },
                shape = RoundedCornerShape(14.dp),
                color = cs.lineColor.copy(alpha = .08f),
                tonalElevation = 1.dp,
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(cs.lineColor))
                        Spacer(Modifier.width(6.dp))
                        Text(cs.lineName, style = MaterialTheme.typography.labelSmall, color = cs.lineColor, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(Modifier.height(Spacing.sm))
                    Text(cs.station.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(2.dp))
                    Text("${cs.stops} stop${if (cs.stops > 1) "s" else ""} away", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (cs.station.hasStepFreeAccess) {
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.AccessibleForward, null, Modifier.size(10.dp), tint = StatusGood)
                            Spacer(Modifier.width(2.dp))
                            Text("Step-free", style = MaterialTheme.typography.labelSmall, color = StatusGood)
                        }
                    }
                }
            }
        }
    }
}

// ── Nearby Stations Section ──────────────────────────────────

@Composable
private fun NearbyStationsSection(nearby: List<NearbyStation>, onStationClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(nearby.size) { i ->
            val ns = nearby[i]
            Surface(
                modifier = Modifier.width(150.dp).clickable { onStationClick(ns.station.id) },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .3f),
                tonalElevation = 1.dp,
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(ns.station.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Text("${ns.displayDistance}  •  ~${ns.walkingMinutes} min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        ns.station.lineIds.take(5).forEach { lineId ->
                            val line = TubeData.getLineById(lineId)
                            if (line != null) {
                                Box(Modifier.size(8.dp).clip(CircleShape).background(line.color))
                            }
                        }
                    }
                    Text("Zone ${ns.station.zone}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ── Snapshot Card ────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StationSnapshotCard(station: Station) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Station Snapshot", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SnapshotPill("Lines", "${station.lineIds.size}")
                SnapshotPill("Interchange", "${station.interchangeTimeMinutes.coerceAtLeast(1)} min")
                SnapshotPill("Carriages", "${station.totalCarriages}")
                val passengerStr = when {
                    station.annualPassengers >= 100 -> "${station.annualPassengers.toInt()}M/yr"
                    station.annualPassengers > 0 -> "${String.format("%.1f", station.annualPassengers)}M/yr"
                    else -> "Local stop"
                }
                SnapshotPill("Traffic", passengerStr)
                if (station.wifiAvailable) SnapshotPill("WiFi", "Yes")
                if (station.toiletsAvailable) SnapshotPill("Toilets", "Yes")
            }
        }
    }
}

@Composable
private fun SnapshotPill(title: String, value: String) {
    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── Exit List Item ───────────────────────────────────────────

@Composable
private fun ExitListItem(exit: StationExit) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 3.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${exit.name}: ${exit.description}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text("${exit.walkingTimeSeconds}s walk", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (exit.isStepFree) {
                        Spacer(Modifier.width(12.dp))
                        Icon(Icons.AutoMirrored.Filled.AccessibleForward, "Step-free", Modifier.size(14.dp), tint = StatusGood)
                        Spacer(Modifier.width(4.dp))
                        Text("Step-free", style = MaterialTheme.typography.labelSmall, color = StatusGood)
                    }
                }
                if (exit.nearbyLandmarks.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(exit.nearbyLandmarks.joinToString(" • "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Text("Car ${exit.bestCarriagePosition}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = TubePrimary)
        }
    }
}

// ── Disruption Alert Banner ──────────────────────────────────

@Composable
private fun DisruptionBanner(disruptions: List<LineStatus>) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StatusSevere.copy(alpha = .08f)),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Warning, null, Modifier.size(20.dp), tint = StatusSevere)
                Spacer(Modifier.width(8.dp))
                Text("Service Disruptions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StatusSevere)
            }
            Spacer(Modifier.height(8.dp))
            disruptions.forEach { d ->
                Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(d.lineColor))
                    Spacer(Modifier.width(8.dp))
                    Text("${d.lineName}: ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text(d.statusDescription, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (d.reason != null) {
                    Text(d.reason, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .7f), modifier = Modifier.padding(start = 16.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

// ── Smart Insights Strip ─────────────────────────────────────

@Composable
private fun InsightsStrip(insights: List<com.londontubeai.navigator.data.model.StationInsight>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        items(insights.size) { i ->
            val insight = insights[i]
            val bgColor = when (insight.type) {
                com.londontubeai.navigator.data.model.StationInsightType.WARNING -> StatusMinor.copy(alpha = .1f)
                com.londontubeai.navigator.data.model.StationInsightType.ALERT -> StatusSevere.copy(alpha = .1f)
                com.londontubeai.navigator.data.model.StationInsightType.TIP -> StatusGood.copy(alpha = .1f)
                else -> TubePrimary.copy(alpha = .06f)
            }
            val iconTint = when (insight.type) {
                com.londontubeai.navigator.data.model.StationInsightType.WARNING -> StatusMinor
                com.londontubeai.navigator.data.model.StationInsightType.ALERT -> StatusSevere
                com.londontubeai.navigator.data.model.StationInsightType.TIP -> StatusGood
                else -> TubePrimary
            }
            val icon = when (insight.type) {
                com.londontubeai.navigator.data.model.StationInsightType.WARNING -> Icons.Filled.Warning
                com.londontubeai.navigator.data.model.StationInsightType.ALERT -> Icons.Filled.ErrorOutline
                com.londontubeai.navigator.data.model.StationInsightType.TIP -> Icons.Filled.Lightbulb
                else -> Icons.Filled.Info
            }
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = bgColor,
                modifier = Modifier.width(220.dp),
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, null, Modifier.size(16.dp), tint = iconTint)
                        Spacer(Modifier.width(6.dp))
                        Text(insight.title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = iconTint)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(insight.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp)
                }
            }
        }
    }
}

// ── Platform Map Section ─────────────────────────────────────

@Composable
private fun PlatformMapSection(platforms: List<com.londontubeai.navigator.data.model.PlatformInfo>) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        platforms.forEach { p ->
            Surface(
                Modifier.fillMaxWidth().padding(vertical = 2.dp),
                shape = RoundedCornerShape(10.dp),
                color = p.lineColor.copy(alpha = .06f),
            ) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(p.lineColor))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(p.platformName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Text("${p.lineName} • ${p.direction}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (p.nextTrainMinutes != null) {
                        Surface(shape = RoundedCornerShape(8.dp), color = p.lineColor.copy(alpha = .15f)) {
                            Text("${p.nextTrainMinutes} min", Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = p.lineColor)
                        }
                    }
                }
            }
        }
    }
}

// ── Crowd Heatmap Card ───────────────────────────────────────

@Composable
private fun CrowdHeatmapCard(heatmap: List<com.londontubeai.navigator.data.model.CrowdHeatmapEntry>) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Schedule, null, Modifier.size(18.dp), tint = TubePrimary)
                Spacer(Modifier.width(8.dp))
                Text("24h Crowd Forecast", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            // Heatmap bars
            val maxPct = heatmap.maxOfOrNull { it.percentageFull }?.toFloat()?.coerceAtLeast(1f) ?: 1f
            Row(
                Modifier.fillMaxWidth().height(60.dp),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                heatmap.sortedBy { it.hour }.forEach { entry ->
                    val barColor = when (entry.crowdLevel) {
                        CrowdLevel.LOW -> StatusGood
                        CrowdLevel.MODERATE -> Color(0xFFF59E0B)
                        CrowdLevel.HIGH -> StatusMinor
                        CrowdLevel.VERY_HIGH -> StatusSevere
                        CrowdLevel.EXTREME -> Color(0xFF7C3AED)
                    }
                    val targetFrac = (entry.percentageFull / maxPct).coerceAtLeast(0.04f)
                    val animatedFrac by animateFloatAsState(
                        targetValue = targetFrac,
                        animationSpec = tween(durationMillis = 600),
                        label = "heatmap_${entry.hour}",
                    )
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(animatedFrac)
                                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                .background(barColor.copy(alpha = 0.8f))
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            // Time labels
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("00", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.sp)
                Text("06", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.sp)
                Text("12", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.sp)
                Text("18", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.sp)
                Text("23", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.sp)
            }
            Spacer(Modifier.height(Spacing.sm))
            // Legend
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Quiet" to StatusGood, "Moderate" to Color(0xFFFFA726), "Busy" to StatusMinor, "Very Busy" to StatusSevere).forEach { (label, color) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(color))
                        Spacer(Modifier.width(3.dp))
                        Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ── Journey Suggestions Section ──────────────────────────────

@Composable
private fun JourneySuggestionsSection(suggestions: List<com.londontubeai.navigator.data.model.JourneySuggestion>, onStationClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(suggestions.size) { i ->
            val s = suggestions[i]
            val catColor = when (s.category) {
                com.londontubeai.navigator.data.model.SuggestionCategory.POPULAR -> TubePrimary
                com.londontubeai.navigator.data.model.SuggestionCategory.QUICK -> StatusGood
                com.londontubeai.navigator.data.model.SuggestionCategory.COMMUTE -> Color(0xFF2196F3)
                com.londontubeai.navigator.data.model.SuggestionCategory.NEARBY -> StatusMinor
            }
            Surface(
                modifier = Modifier.width(150.dp).clickable { onStationClick(s.destinationStation.id) },
                shape = RoundedCornerShape(14.dp),
                color = catColor.copy(alpha = .06f),
                tonalElevation = 1.dp,
            ) {
                Column(Modifier.padding(12.dp)) {
                    Surface(shape = RoundedCornerShape(6.dp), color = catColor.copy(alpha = .15f)) {
                        Text(s.category.label, Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = catColor, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    }
                    Spacer(Modifier.height(Spacing.sm))
                    Text(s.destinationStation.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Schedule, null, Modifier.size(10.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(3.dp))
                        Text("~${s.estimatedMinutes} min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (s.interchanges > 0) {
                            Text(" • ${s.interchanges} change", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    // Line dots for destination
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        s.destinationStation.lineIds.take(4).forEach { lineId ->
                            val line = TubeData.getLineById(lineId)
                            if (line != null) Box(Modifier.size(6.dp).clip(CircleShape).background(line.color))
                        }
                    }
                }
            }
        }
    }
}

// ── Community Tips Section ───────────────────────────────────

@Composable
private fun CommunityTipsSection(tips: List<com.londontubeai.navigator.data.model.CommunityTip>) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        tips.forEach { tip ->
            val catColor = when (tip.category) {
                com.londontubeai.navigator.data.model.TipCategory.CROWD_AVOIDANCE -> StatusMinor
                com.londontubeai.navigator.data.model.TipCategory.PLATFORM_TIP -> TubePrimary
                com.londontubeai.navigator.data.model.TipCategory.EXIT_TIP -> StatusGood
                com.londontubeai.navigator.data.model.TipCategory.SAFETY -> StatusSevere
                com.londontubeai.navigator.data.model.TipCategory.ACCESSIBILITY -> Color(0xFF2196F3)
                com.londontubeai.navigator.data.model.TipCategory.GENERAL -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Surface(
                Modifier.fillMaxWidth().padding(vertical = 3.dp),
                shape = RoundedCornerShape(12.dp),
                color = catColor.copy(alpha = .05f),
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(6.dp), color = catColor.copy(alpha = .15f)) {
                            Text(tip.category.label, Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = catColor, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Filled.ThumbUp, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .5f))
                        Spacer(Modifier.width(3.dp))
                        Text("${tip.upvotes}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .5f))
                    }
                    Spacer(Modifier.height(Spacing.sm))
                    Text(tip.tip, style = MaterialTheme.typography.bodySmall, lineHeight = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Person, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .5f))
                        Spacer(Modifier.width(4.dp))
                        Text(tip.author, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .6f))
                    }
                }
            }
        }
    }
}

// ── Station Reviews Section ──────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StationReviewsSection(reviews: List<com.londontubeai.navigator.data.model.StationReview>) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        // Average rating
        val avgRating = reviews.map { it.rating }.average().toFloat()
        Row(Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(String.format("%.1f", avgRating), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Row {
                repeat(5) { i ->
                    val starIcon = when {
                        i + 1 <= avgRating.toInt() -> Icons.Filled.Star
                        i + 0.5f <= avgRating -> Icons.AutoMirrored.Filled.StarHalf
                        else -> Icons.Filled.StarOutline
                    }
                    Icon(starIcon, null, Modifier.size(18.dp), tint = Color(0xFFFFC107))
                }
            }
            Spacer(Modifier.width(8.dp))
            Text("(${reviews.size} reviews)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        reviews.forEach { review ->
            Surface(
                Modifier.fillMaxWidth().padding(vertical = 3.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .3f),
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(28.dp).clip(CircleShape).background(TubePrimary.copy(alpha = .1f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(review.userName.first().toString(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TubePrimary)
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(review.userName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Row {
                                repeat(5) { i ->
                                    Icon(
                                        if (i < review.rating.toInt()) Icons.Filled.Star else Icons.Filled.StarOutline,
                                        null, Modifier.size(12.dp), tint = Color(0xFFFFC107),
                                    )
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ThumbUp, null, Modifier.size(11.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .4f))
                            Spacer(Modifier.width(3.dp))
                            Text("${review.helpful}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .4f))
                        }
                    }
                    Spacer(Modifier.height(Spacing.sm))
                    Text(review.comment, style = MaterialTheme.typography.bodySmall, lineHeight = 18.sp)
                    if (review.tags.isNotEmpty()) {
                        Spacer(Modifier.height(Spacing.sm))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            review.tags.forEach { tag ->
                                Surface(shape = RoundedCornerShape(6.dp), color = TubePrimary.copy(alpha = .08f)) {
                                    Text(tag, Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = TubePrimary, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

