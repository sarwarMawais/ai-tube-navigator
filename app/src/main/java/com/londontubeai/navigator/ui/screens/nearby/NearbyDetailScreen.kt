package com.londontubeai.navigator.ui.screens.nearby

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.AccessibleForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.londontubeai.navigator.data.model.LineStatus
import com.londontubeai.navigator.data.model.TubeData
import com.londontubeai.navigator.ui.components.UnifiedHeader
import com.londontubeai.navigator.ui.theme.Spacing
import com.londontubeai.navigator.ui.theme.cardPadding
import com.londontubeai.navigator.ui.theme.StatusGood
import com.londontubeai.navigator.ui.theme.StatusMinor
import com.londontubeai.navigator.ui.theme.StatusSevere
import com.londontubeai.navigator.ui.theme.TubeAccent
import com.londontubeai.navigator.ui.theme.TubePrimary
import com.londontubeai.navigator.ui.theme.TubeSecondary
import com.londontubeai.navigator.ui.theme.brandGradient
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector

private val BusRed = Color(0xFFE32017)
private val WalkGreen = Color(0xFF00A651)
private val TubeBlue = Color(0xFF003688)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyDetailScreen(
    onBack: () -> Unit,
    onStationClick: (String) -> Unit,
    onNavigateToRoute: (String?, String?, Double?, Double?) -> Unit = { _, _, _, _ -> },
    viewModel: NearbyDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        UnifiedHeader(
            title = "Nearby Stations",
            subtitle = if (!uiState.isLoading && uiState.nearbyStations.isNotEmpty()) {
                "${uiState.nearbyStations.size} stations with live nearby transport"
            } else {
                "Live trains, buses, and walking context around you"
            },
            icon = Icons.Filled.NearMe,
            trailingContent = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }

                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Filled.Refresh, "Refresh", tint = Color.White)
                }
            },
        )

        when {
            uiState.isLoading && uiState.nearbyStations.isEmpty() -> LoadingState()
            uiState.error != null -> ErrorState(uiState.error!!, onRetry = { viewModel.refresh() })
            uiState.nearbyStations.isEmpty() -> EmptyState()
            else -> androidx.compose.material3.pulltorefresh.PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars),
                    contentPadding = PaddingValues(
                        start = Spacing.screenHorizontal,
                        end = Spacing.screenHorizontal,
                        top = Spacing.md,
                        bottom = 100.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    item {
                        NearbyOverviewCard(uiState = uiState)
                    }

                    item {
                        NearbySortRow(
                            selected = uiState.sortMode,
                            onSelect = viewModel::setSortMode,
                        )
                    }

                    // Journey mode legend
                    item {
                        JourneyModeLegend()
                    }

                    itemsIndexed(uiState.nearbyStations) { index, nearbyStation ->
                        NearbyStationCard(
                            nearbyStation = nearbyStation,
                            index = index,
                            onStationClick = { onStationClick(nearbyStation.station.id) },
                            onNavigateToRoute = {
                                onNavigateToRoute(
                                    nearbyStation.station.id,
                                    nearbyStation.station.name,
                                    nearbyStation.station.latitude,
                                    nearbyStation.station.longitude,
                                )
                            },
                            onExpand = { viewModel.onStationExpanded(nearbyStation.station.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NearbySortRow(
    selected: NearbySortMode,
    onSelect: (NearbySortMode) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(NearbySortMode.entries) { sortMode ->
            val selectedChip = selected == sortMode
            Surface(
                modifier = Modifier.clickable { onSelect(sortMode) },
                shape = RoundedCornerShape(12.dp),
                color = if (selectedChip) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            ) {
                Text(
                    text = sortMode.label,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selectedChip) FontWeight.Bold else FontWeight.Medium,
                    color = if (selectedChip) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun NearbyOverviewCard(uiState: NearbyDetailUiState) {
    val stationCount = uiState.nearbyStations.size
    val liveArrivalCount = uiState.nearbyStations.sumOf { it.arrivals.size }
    val busCount = uiState.nearbyStations.sumOf { it.busRoutes.size }
    val closestStation = uiState.nearbyStations.minByOrNull { it.distanceKm }
    val disruptedLines = uiState.nearbyStations
        .flatMap { it.lineStatuses }
        .filter { !it.isGoodService }
        .distinctBy { it.lineId }
        .size

    val gradient = brandGradient()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(gradient.linear()),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.12f),
                ) {
                    Box(modifier = Modifier.padding(10.dp)) {
                        Icon(Icons.Filled.MyLocation, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Live around you",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        text = closestStation?.let {
                            "Nearest: ${it.station.name} · ${if (it.distanceKm < 1.0) "${(it.distanceKm * 1000).toInt()}m" else "${"%.1f".format(it.distanceKm)}km"}"
                        } ?: "Stations within 4.8 km radius",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    uiState.overviewUpdatedAt?.let { updatedAt ->
                        SourcePill(
                            label = "${sourceLabel(uiState.overviewSource)} · ${formatFreshness(updatedAt)}",
                            source = uiState.overviewSource,
                        )
                    }
                    if (disruptedLines > 0) {
                        Surface(shape = RoundedCornerShape(8.dp), color = StatusSevere.copy(alpha = 0.2f)) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(androidx.compose.foundation.shape.CircleShape).background(StatusSevere))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$disruptedLines disrupted", style = MaterialTheme.typography.labelSmall, color = StatusSevere, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Surface(shape = RoundedCornerShape(8.dp), color = StatusGood.copy(alpha = 0.2f)) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(androidx.compose.foundation.shape.CircleShape).background(StatusGood))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Good service", style = MaterialTheme.typography.labelSmall, color = StatusGood, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OverviewStatChip(
                    icon = Icons.Filled.Train,
                    label = "Stations",
                    value = stationCount.toString(),
                    accentColor = Color.White,
                    modifier = Modifier.weight(1f),
                )
                OverviewStatChip(
                    icon = Icons.Filled.Schedule,
                    label = "Live trains",
                    value = liveArrivalCount.toString(),
                    accentColor = Color(0xFF82B1FF),
                    modifier = Modifier.weight(1f),
                )
                OverviewStatChip(
                    icon = Icons.Filled.DirectionsBus,
                    label = "Bus routes",
                    value = busCount.toString(),
                    accentColor = Color(0xFFFF8A80),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun OverviewStatChip(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.08f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, null, tint = accentColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = accentColor)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
        }
    }
}

@Composable
private fun JourneyModeLegend() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            LegendItem(Icons.AutoMirrored.Filled.DirectionsWalk, "Walk", WalkGreen)
            LegendItem(Icons.Filled.DirectionsBus, "Bus", BusRed)
            LegendItem(Icons.Filled.Train, "Tube", TubeBlue)
        }
    }
}

@Composable
private fun LegendItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = color.copy(alpha = 0.12f)) {
            Box(modifier = Modifier.padding(Spacing.sm)) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.width(Spacing.sm))
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun LineStatusStrip(statuses: List<LineStatus>) {
    if (statuses.isEmpty()) return
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(statuses) { status ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = status.lineColor.copy(alpha = 0.12f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (status.isGoodService) StatusGood else StatusMinor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${status.lineName} · ${status.statusDescription}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = status.lineColor,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NearbyStationCard(
    nearbyStation: NearbyStation,
    index: Int,
    onStationClick: () -> Unit,
    onNavigateToRoute: () -> Unit,
    onExpand: () -> Unit = {},
) {
    var expanded by remember(nearbyStation.station.id) { mutableStateOf(index == 0) }
    // Fire a lazy enrichment request the first time the card opens.
    androidx.compose.runtime.LaunchedEffect(expanded, nearbyStation.enriched, nearbyStation.isEnriching) {
        if (expanded && !nearbyStation.enriched && !nearbyStation.isEnriching) onExpand()
    }
    val station = nearbyStation.station
    val stationLines = remember(station.id) { TubeData.getLinesForStation(station.id) }
    val distColor = when {
        nearbyStation.distanceKm < 0.5 -> WalkGreen
        nearbyStation.distanceKm < 1.5 -> StatusMinor
        else -> MaterialTheme.colorScheme.primary
    }
    val primaryLineColor = stationLines.firstOrNull()?.color ?: MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(tween(300)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = { expanded = !expanded },
    ) {
        Column {
            // ── Colored top strip (line color) ─────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = stationLines.take(3).map { it.color }.let { c ->
                                when {
                                    c.size >= 2 -> c
                                    c.size == 1 -> listOf(c[0], c[0])
                                    else -> listOf(primaryLineColor, primaryLineColor)
                                }
                            }
                        )
                    )
            )

            Column(modifier = Modifier.padding(Spacing.lg)) {
                // ── Header Row ──────────────────────────────────
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    // Index number badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = distColor.copy(alpha = 0.12f),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = if (nearbyStation.distanceKm < 1.0) "${(nearbyStation.distanceKm * 1000).toInt()}m"
                                       else "${"%.1f".format(nearbyStation.distanceKm)}km",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = distColor,
                            )
                            Text(
                                text = "${nearbyStation.walkingMinutes} min",
                                style = MaterialTheme.typography.labelSmall,
                                color = distColor.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(station.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            ) {
                                Text(
                                    "Zone ${station.zone}",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (station.hasStepFreeAccess) {
                                Spacer(modifier = Modifier.width(Spacing.sm))
                                Surface(shape = RoundedCornerShape(6.dp), color = StatusGood.copy(alpha = 0.1f)) {
                                    Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.AutoMirrored.Filled.AccessibleForward, null, tint = StatusGood, modifier = Modifier.size(10.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("Step-free", style = MaterialTheme.typography.labelSmall, color = StatusGood, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            stationLines.take(5).forEach { line ->
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(line.color))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SourcePill(
                                label = buildString {
                                    append(sourceLabel(nearbyStation.tubeSource))
                                    nearbyStation.arrivalsUpdatedAt?.let {
                                        append(" · ")
                                        append(formatFreshness(it))
                                    }
                                },
                                source = nearbyStation.tubeSource,
                            )
                            SourcePill(
                                label = "Bus ${sourceLabel(nearbyStation.busSource)}",
                                source = nearbyStation.busSource,
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        if (nearbyStation.arrivals.isNotEmpty()) {
                            Surface(shape = RoundedCornerShape(8.dp), color = TubeBlue.copy(alpha = 0.1f)) {
                                Row(modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(StatusGood))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${nearbyStation.arrivals.size} trains", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TubeBlue, fontSize = 10.sp)
                                }
                            }
                        }
                        if (nearbyStation.busRoutes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Surface(shape = RoundedCornerShape(8.dp), color = BusRed.copy(alpha = 0.1f)) {
                                Row(modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.DirectionsBus, null, tint = BusRed, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("${nearbyStation.busRoutes.size} buses", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BusRed, fontSize = 10.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(
                            if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                // ── Quick transport chips ────────────────────────
                Spacer(modifier = Modifier.height(Spacing.md))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    JourneyOptionChip(
                        icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                        label = "${nearbyStation.walkingMinutes} min walk",
                        color = WalkGreen,
                        modifier = Modifier.weight(1f),
                    )
                    if (nearbyStation.busRoutes.isNotEmpty()) {
                        val bus = nearbyStation.busRoutes.first()
                        JourneyOptionChip(
                            icon = Icons.Filled.DirectionsBus,
                            label = "Bus ${bus.busNumber} · ${if (bus.estimatedMinutes == 0) "Due" else "${bus.estimatedMinutes}m"}",
                            color = BusRed,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        JourneyOptionChip(
                            icon = Icons.Filled.Train,
                            label = "${nearbyStation.arrivals.firstOrNull()?.let { "${it.minutesUntil}m" } ?: "Check board"}",
                            color = TubeBlue,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                nearbyStation.accessOption?.let { accessOption ->
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                    ) {
                        Column(modifier = Modifier.padding(horizontal = Spacing.md, vertical = 10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.NearMe, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    accessOption.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f),
                                )
                                SourcePill(
                                    label = sourceLabel(accessOption.source),
                                    source = accessOption.source,
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                accessOption.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                if (nearbyStation.lineStatuses.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.md))
                    LineStatusStrip(nearbyStation.lineStatuses)
                }

                // ── Expanded Content ─────────────────────────────
                AnimatedVisibility(visible = expanded) {
                    Column {
                        Spacer(modifier = Modifier.height(Spacing.md))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(Spacing.md))

                        if (nearbyStation.arrivals.isNotEmpty() || nearbyStation.isLoadingArrivals) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Train, null, tint = TubeBlue, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tube Arrivals", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TubeBlue, modifier = Modifier.weight(1f))
                                SourcePill(label = sourceLabel(nearbyStation.tubeSource), source = nearbyStation.tubeSource)
                                Spacer(modifier = Modifier.width(8.dp))
                                if (nearbyStation.isLoadingArrivals) {
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = TubeBlue)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (nearbyStation.arrivals.isEmpty()) {
                                Text("Loading arrivals...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                nearbyStation.arrivals.forEach { arrival ->
                                    ArrivalRow(arrival)
                                    Spacer(modifier = Modifier.height(Spacing.sm))
                                }
                            }
                            Spacer(modifier = Modifier.height(Spacing.md))
                        } else if (!nearbyStation.isLoadingArrivals) {
                            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) {
                                Row(modifier = Modifier.fillMaxWidth().padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Train, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("No live tube arrivals", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(modifier = Modifier.height(Spacing.md))
                        }

                        if (nearbyStation.busRoutes.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.DirectionsBus, null, tint = BusRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Bus Connections", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = BusRed, modifier = Modifier.weight(1f))
                                SourcePill(label = sourceLabel(nearbyStation.busSource), source = nearbyStation.busSource)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            nearbyStation.busRoutes.forEach { bus ->
                                BusRouteRow(bus)
                                Spacer(modifier = Modifier.height(Spacing.sm))
                            }
                            Spacer(modifier = Modifier.height(Spacing.md))
                        } else if (nearbyStation.accessOption?.busRouteNumber != null) {
                            Surface(shape = RoundedCornerShape(12.dp), color = BusRed.copy(alpha = 0.06f)) {
                                Row(modifier = Modifier.fillMaxWidth().padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.DirectionsBus, null, tint = BusRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "TfL journey planner suggests bus ${nearbyStation.accessOption.busRouteNumber} on the way here",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(Spacing.md))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                onClick = onNavigateToRoute,
                            ) {
                                Row(
                                    modifier = Modifier.padding(Spacing.md),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(Spacing.sm))
                                    Text("Route here", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primary,
                                onClick = onStationClick,
                            ) {
                                Row(
                                    modifier = Modifier.padding(Spacing.md),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Filled.Train, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(Spacing.sm))
                                    Text("Full details", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = Color.White)
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
private fun JourneyOptionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun BusRouteRow(bus: BusRoute) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = BusRed.copy(alpha = 0.06f),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Bus number badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = BusRed,
            ) {
                Text(
                    bus.busNumber,
                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(bus.direction, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                Text("from ${bus.stopName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (bus.estimatedMinutes <= 1) StatusGood.copy(alpha = 0.15f)
                       else if (bus.estimatedMinutes <= 5) StatusMinor.copy(alpha = 0.15f)
                       else BusRed.copy(alpha = 0.12f),
            ) {
                Text(
                    if (bus.estimatedMinutes <= 0) "Due" else "${bus.estimatedMinutes} min",
                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (bus.estimatedMinutes <= 1) StatusGood
                           else if (bus.estimatedMinutes <= 5) StatusMinor
                           else BusRed,
                )
            }
        }
    }
}

@Composable
private fun ArrivalRow(arrival: StationArrivalInfo) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Line color strip
            Box(modifier = Modifier.width(4.dp).height(28.dp).clip(RoundedCornerShape(2.dp)).background(arrival.lineColor ?: Color.Gray))
            Spacer(modifier = Modifier.width(10.dp))

            // Time badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = when {
                    arrival.minutesUntil <= 1 -> StatusGood.copy(alpha = 0.15f)
                    arrival.minutesUntil <= 3 -> StatusMinor.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
            ) {
                Text(
                    text = if (arrival.minutesUntil <= 0) "Due" else "${arrival.minutesUntil}m",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        arrival.minutesUntil <= 1 -> StatusGood
                        arrival.minutesUntil <= 3 -> StatusMinor
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                )
            }

            Spacer(modifier = Modifier.width(Spacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(arrival.lineName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Text(arrival.destination, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (arrival.platform != null) {
                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                    Text("P${arrival.platform}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun SourcePill(
    label: String,
    source: NearbyDataSource,
) {
    val color = when (source) {
        NearbyDataSource.LIVE -> StatusGood
        NearbyDataSource.CACHED -> StatusMinor
        NearbyDataSource.INFERRED -> TubeAccent
    }
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.12f)) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun sourceLabel(source: NearbyDataSource): String {
    return when (source) {
        NearbyDataSource.LIVE -> "Live"
        NearbyDataSource.CACHED -> "Cached"
        NearbyDataSource.INFERRED -> "Inferred"
    }
}

private fun formatFreshness(updatedAt: Long): String {
    val ageMinutes = ((System.currentTimeMillis() - updatedAt) / 60_000L).coerceAtLeast(0L)
    return when {
        ageMinutes == 0L -> "Updated just now"
        ageMinutes < 60L -> "Updated ${ageMinutes}m ago"
        else -> "Updated ${ageMinutes / 60L}h ago"
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp))
                    .background(brandGradient().linear()),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
            }
            Text("Finding nearby transport...", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text("Getting your location and live data", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(horizontal = 40.dp)) {
            Surface(shape = RoundedCornerShape(20.dp), color = StatusSevere.copy(alpha = 0.1f)) {
                Box(modifier = Modifier.padding(20.dp)) {
                    Icon(Icons.Filled.LocationOn, null, tint = StatusSevere, modifier = Modifier.size(40.dp))
                }
            }
            Text("Location Unavailable", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primary, onClick = onRetry) {
                Row(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Refresh, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Try Again", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.NearMe, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
            }
            Text("No Stations Nearby", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("No tube stations found within 4.8 km of your location", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
