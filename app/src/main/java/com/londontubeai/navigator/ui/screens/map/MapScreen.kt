package com.londontubeai.navigator.ui.screens.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessibleForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.londontubeai.navigator.data.model.LineStatus
import com.londontubeai.navigator.data.model.Station
import com.londontubeai.navigator.data.model.TransportMode
import com.londontubeai.navigator.data.model.TubeData
import com.londontubeai.navigator.ui.theme.Spacing
import com.londontubeai.navigator.ui.theme.horizontalScreenPadding
import com.londontubeai.navigator.ui.theme.StatusGood
import com.londontubeai.navigator.ui.theme.StatusMinor
import com.londontubeai.navigator.ui.theme.StatusSevere
import com.londontubeai.navigator.ui.theme.TubePrimary
import com.londontubeai.navigator.ui.theme.TubeSecondary
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

private val LONDON_CENTER = LatLng(51.5074, -0.1278)
private const val DEFAULT_ZOOM = 12f
private const val CLUSTERING_ZOOM_THRESHOLD = 13f

data class StationCluster(
    val position: LatLng,
    val stations: List<Station>,
    val count: Int,
    val primaryLineId: String?
)

private enum class MapStyle(val label: String, val icon: ImageVector, val mapType: MapType) {
    NORMAL("Standard", Icons.Filled.Map, MapType.NORMAL),
    TERRAIN("Terrain", Icons.Filled.Terrain, MapType.TERRAIN),
    SATELLITE("Satellite", Icons.Filled.DarkMode, MapType.HYBRID),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBack: () -> Unit = {},
    onStationClick: (String) -> Unit = {},
    routeFromId: String? = null,
    routeToId: String? = null,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LONDON_CENTER, DEFAULT_ZOOM)
    }

    val stations = remember { TubeData.getAllStationsSorted() }
    val lines = remember { TubeData.lines }
    val connections = remember { TubeData.connections }

    var selectedStation by remember { mutableStateOf<Station?>(null) }
    var selectedLineFilter by remember { mutableStateOf<String?>(null) }
    var showLineFilters by remember { mutableStateOf(false) }
    var isMapLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var mapStyle by remember { mutableStateOf(MapStyle.NORMAL) }
    var showMapStylePicker by remember { mutableStateOf(false) }

    LaunchedEffect(routeFromId, routeToId) {
        viewModel.loadJourney(routeFromId, routeToId)
    }

    LaunchedEffect(uiState.journeyRoute?.fromStation?.id, uiState.journeyRoute?.toStation?.id) {
        uiState.journeyRoute?.let { route ->
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(route.fromStation.latitude, route.fromStation.longitude),
                    13f,
                )
            )
        }
    }

    LaunchedEffect(uiState.userLat, uiState.userLng, uiState.journeyRoute) {
        if (uiState.journeyRoute == null && uiState.userLat != null && uiState.userLng != null) {
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(uiState.userLat!!, uiState.userLng!!),
                    13.5f,
                )
            )
        }
    }

    // Search results
    val searchResults = remember(searchQuery) {
        if (searchQuery.length < 2) emptyList()
        else stations.filter { it.name.contains(searchQuery, ignoreCase = true) }.take(5)
    }

    // Filter stations by selected line
    val filteredStations = remember(selectedLineFilter) {
        if (selectedLineFilter == null) stations
        else stations.filter { it.lineIds.contains(selectedLineFilter) }
    }

    // Filter connections by selected line
    val filteredConnections = remember(selectedLineFilter) {
        if (selectedLineFilter == null) connections
        else connections.filter { it.lineId == selectedLineFilter }
    }

    // Cluster stations based on zoom level
    val clusteredStations = remember(cameraPositionState.position.zoom, filteredStations) {
        if (cameraPositionState.position.zoom < CLUSTERING_ZOOM_THRESHOLD) {
            val gridSize = if (cameraPositionState.position.zoom < 10f) 10 else 25
            val clusterGrid = mutableMapOf<String, MutableList<Station>>()
            filteredStations.forEach { station ->
                val gridKey = "${(station.latitude * gridSize).toInt()}_${(station.longitude * gridSize).toInt()}"
                clusterGrid.getOrPut(gridKey) { mutableListOf() }.add(station)
            }
            clusterGrid.values.map { stationsInCluster ->
                val avgLat = stationsInCluster.sumOf { it.latitude } / stationsInCluster.size
                val avgLng = stationsInCluster.sumOf { it.longitude } / stationsInCluster.size
                val primaryLineId = stationsInCluster
                    .flatMap { it.lineIds }.groupBy { it }
                    .maxByOrNull { it.value.size }?.key
                StationCluster(LatLng(avgLat, avgLng), stationsInCluster, stationsInCluster.size, primaryLineId)
            }
        } else {
            filteredStations.map { station ->
                StationCluster(LatLng(station.latitude, station.longitude), listOf(station), 1, station.lineIds.firstOrNull())
            }
        }
    }

    // Build a lineId → LineStatus map for quick lookup in the map content block
    val lineStatusMap = remember(uiState.lineStatuses) {
        uiState.lineStatuses.associateBy { it.lineId }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isMapLoading) MapLoadingOverlay()

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = mapStyle.mapType,
                isMyLocationEnabled = false,
                isBuildingEnabled = true,
                isIndoorEnabled = false,
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = false,
                mapToolbarEnabled = false,
                zoomGesturesEnabled = true,
                scrollGesturesEnabled = true,
                tiltGesturesEnabled = true,
                rotationGesturesEnabled = true,
            ),
            onMapLoaded = { isMapLoading = false },
            onMapClick = {
                selectedStation = null
                viewModel.selectStation(null)
                showSearch = false
                searchQuery = ""
                showMapStylePicker = false
            },
        ) {
            // Draw tube line polylines with live status-aware coloring
            filteredConnections.groupBy { it.lineId }.forEach { (lineId, conns) ->
                val line = TubeData.getLineById(lineId) ?: return@forEach
                val status = lineStatusMap[lineId]
                val isDisrupted = status != null && !status.isGoodService
                val polyColor = when {
                    status == null -> line.color
                    status.isGoodService -> line.color
                    status.statusSeverity <= 3 -> StatusSevere
                    status.statusSeverity <= 6 -> StatusMinor
                    else -> line.color
                }
                val polyWidth = when {
                    selectedLineFilter == lineId -> 11f
                    isDisrupted -> 7f
                    else -> 5f
                }
                conns.forEach { conn ->
                    val from = TubeData.getStationById(conn.fromStationId)
                    val to = TubeData.getStationById(conn.toStationId)
                    if (from != null && to != null) {
                        Polyline(
                            points = listOf(LatLng(from.latitude, from.longitude), LatLng(to.latitude, to.longitude)),
                            color = polyColor,
                            width = polyWidth,
                        )
                    }
                }
            }

            uiState.journeyRoute?.legs?.forEach { leg ->
                val routePoints = when (leg.mode) {
                    TransportMode.TUBE -> leg.stationIds.mapNotNull { stationId ->
                        TubeData.getStationById(stationId)?.let { LatLng(it.latitude, it.longitude) }
                    }.ifEmpty {
                        listOf(
                            LatLng(leg.fromStation.latitude, leg.fromStation.longitude),
                            LatLng(leg.toStation.latitude, leg.toStation.longitude),
                        )
                    }
                    else -> listOf(
                        LatLng(leg.fromStation.latitude, leg.fromStation.longitude),
                        LatLng(leg.toStation.latitude, leg.toStation.longitude),
                    )
                }
                if (routePoints.size >= 2) {
                    Polyline(
                        points = routePoints,
                        color = when (leg.mode) {
                            TransportMode.WALKING -> StatusGood
                            TransportMode.BUS -> StatusMinor
                            TransportMode.TUBE -> leg.line.color
                        },
                        width = when (leg.mode) {
                            TransportMode.WALKING -> 9f
                            TransportMode.BUS -> 10f
                            TransportMode.TUBE -> 14f
                        },
                        zIndex = 3f,
                    )
                }
            }

            uiState.journeyRoute?.let { journey ->
                Marker(
                    state = MarkerState(position = LatLng(journey.fromStation.latitude, journey.fromStation.longitude)),
                    title = "Start · ${journey.fromStation.name}",
                    snippet = "Journey origin",
                    zIndex = 5f,
                )
                Marker(
                    state = MarkerState(position = LatLng(journey.toStation.latitude, journey.toStation.longitude)),
                    title = "Destination · ${journey.toStation.name}",
                    snippet = "Journey destination",
                    zIndex = 5f,
                )
            }

            if (uiState.userLat != null && uiState.userLng != null) {
                Marker(
                    state = MarkerState(position = LatLng(uiState.userLat!!, uiState.userLng!!)),
                    title = "Your location",
                    snippet = uiState.nearestStationDistanceKm?.let { distance ->
                        val nearestName = uiState.nearestStationId?.let { TubeData.getStationById(it)?.name }
                        if (nearestName != null) {
                            "Nearest station: $nearestName · ${"%.1f".format(distance)} km"
                        } else {
                            null
                        }
                    },
                    zIndex = 4f,
                )
            }

            // Draw station markers
            clusteredStations.forEach { cluster ->
                val isCluster = cluster.count > 1
                val primaryLine = cluster.primaryLineId?.let { TubeData.getLineById(it) }
                val lineColor = primaryLine?.color ?: TubePrimary

                Marker(
                    state = MarkerState(position = cluster.position),
                    title = if (isCluster) "${cluster.count} stations" else cluster.stations.first().name,
                    snippet = if (isCluster) "Tap to zoom" else {
                        val s = cluster.stations.first()
                        "Zone ${s.zone} · ${s.lineIds.size} lines" +
                                if (s.hasStepFreeAccess) " · Step-free" else ""
                    },
                    alpha = 0.95f,
                    icon = if (isCluster) {
                        BitmapDescriptorFactory.fromBitmap(createClusterBitmap(cluster.count, lineColor))
                    } else {
                        BitmapDescriptorFactory.fromBitmap(
                            createStationBitmap(lineColor, cluster.stations.first().hasStepFreeAccess)
                        )
                    },
                    onClick = {
                        if (isCluster) {
                            cameraPositionState.move(
                                CameraUpdateFactory.newLatLngZoom(cluster.position, cameraPositionState.position.zoom + 2f)
                            )
                        } else {
                            val tapped = cluster.stations.first()
                            selectedStation = tapped
                            viewModel.selectStation(tapped)
                        }
                        false
                    },
                )
            }
        }

        // ── Top Search Bar ───────────────────────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.xxxl, start = Spacing.screenHorizontal, end = Spacing.screenHorizontal),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
            shadowElevation = 12.dp,
        ) {
            Column(modifier = Modifier.animateContentSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.xs, vertical = Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        onClick = onBack,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    ) {
                        Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    if (showSearch) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            modifier = Modifier.weight(1f).padding(vertical = Spacing.sm),
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { inner ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (searchQuery.isEmpty()) {
                                        Text("Search stations...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 16.sp)
                                    }
                                    inner()
                                }
                            },
                        )
                        IconButton(
                            onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    searchQuery = ""
                                } else {
                                    showSearch = false
                                    selectedStation = null
                                    viewModel.selectStation(null)
                                }
                            }
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                if (searchQuery.isNotEmpty()) "Clear" else "Close search",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Column(modifier = Modifier.weight(1f).clickable { showSearch = true }) {
                            Text(
                                text = "Tube Network Map",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "${filteredStations.size} stations" +
                                        if (selectedLineFilter != null) " · ${TubeData.getLineById(selectedLineFilter!!)?.name ?: ""}" else " · All lines",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    if (!showSearch) {
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Filled.Search, "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Surface(
                        onClick = { showLineFilters = !showLineFilters },
                        shape = CircleShape,
                        color = if (showLineFilters || selectedLineFilter != null)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                    ) {
                        Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Layers, "Filter",
                                tint = if (showLineFilters || selectedLineFilter != null) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }

                // Search results dropdown
                if (showSearch && searchResults.isNotEmpty()) {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)) {
                        Column(modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)) {
                            searchResults.forEach { station ->
                                val stationLineColors = remember(station.id) {
                                    station.lineIds.take(3).mapNotNull { TubeData.getLineById(it) }
                                }
                                val primaryColor = stationLineColors.firstOrNull()?.color ?: TubePrimary
                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xs),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.Transparent,
                                    onClick = {
                                        searchQuery = ""
                                        showSearch = false
                                        selectedStation = station
                                        cameraPositionState.move(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(station.latitude, station.longitude), 15f
                                            )
                                        )
                                    },
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = primaryColor.copy(alpha = 0.12f),
                                        ) {
                                            Box(modifier = Modifier.padding(8.dp)) {
                                                Icon(Icons.Filled.Train, null, tint = primaryColor, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(Spacing.sm))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(station.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                stationLineColors.forEach { line ->
                                                    Box(modifier = Modifier.padding(end = 4.dp).size(8.dp).clip(CircleShape).background(line.color))
                                                }
                                                Text("Zone ${station.zone}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                if (station.hasStepFreeAccess) {
                                                    Text(" · Step-free", style = MaterialTheme.typography.labelSmall, color = StatusGood)
                                                }
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = primaryColor.copy(alpha = 0.1f),
                                        ) {
                                            Text(
                                                "Z${station.zone}",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = primaryColor,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (showSearch && searchQuery.length >= 2) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                    ) {
                        Text(
                            text = "No stations found",
                            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.md),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        uiState.journeyRoute?.let { journeyRoute ->
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 104.dp, start = Spacing.screenHorizontal, end = Spacing.screenHorizontal),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                shadowElevation = 10.dp,
            ) {
                Column(modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.md)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.NearMe, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${journeyRoute.totalDurationMinutes} min · ${journeyRoute.totalInterchanges} change${if (journeyRoute.totalInterchanges == 1) "" else "s"}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(journeyRoute.legs) { leg ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = when (leg.mode) {
                                    TransportMode.WALKING -> StatusGood.copy(alpha = 0.1f)
                                    TransportMode.BUS -> StatusMinor.copy(alpha = 0.1f)
                                    TransportMode.TUBE -> leg.line.color.copy(alpha = 0.12f)
                                }
                            ) {
                                Text(
                                    text = when (leg.mode) {
                                        TransportMode.WALKING -> "Walk ${leg.durationMinutes}m"
                                        TransportMode.BUS -> "Bus ${leg.busRouteNumber.ifBlank { leg.line.name }} ${leg.durationMinutes}m"
                                        TransportMode.TUBE -> "${leg.line.name} ${leg.durationMinutes}m"
                                    },
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when (leg.mode) {
                                        TransportMode.WALKING -> StatusGood
                                        TransportMode.BUS -> StatusMinor
                                        TransportMode.TUBE -> if (isLightColor(leg.line.color)) Color.Black else leg.line.color
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        if (uiState.journeyRoute == null && uiState.nearestStationId != null) {
            val nearestStation = TubeData.getStationById(uiState.nearestStationId!!)
            nearestStation?.let { station ->
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 104.dp, start = Spacing.screenHorizontal, end = Spacing.screenHorizontal),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    shadowElevation = 8.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Train, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = station.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = uiState.nearestStationDistanceKm?.let { "Nearest station · ${"%.1f".format(it)} km away" } ?: "Nearest station",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(TubeData.getLinesForStation(station.id).take(3)) { line ->
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(line.color)
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showLineFilters,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = Spacing.xxxl),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().horizontalScreenPadding(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
                shadowElevation = 8.dp,
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    item {
                        FilterChip(
                            selected = selectedLineFilter == null,
                            onClick = { selectedLineFilter = null },
                            label = { Text("All") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    }
                    items(lines) { line ->
                        FilterChip(
                            selected = selectedLineFilter == line.id,
                            onClick = { selectedLineFilter = if (selectedLineFilter == line.id) null else line.id },
                            label = { Text(line.name, maxLines = 1) },
                            leadingIcon = {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(line.color))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = line.color.copy(alpha = 0.15f),
                                selectedLabelColor = if (isLightColor(line.color)) Color.Black else line.color,
                            ),
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = Spacing.screenHorizontal, bottom = if (selectedStation != null) Spacing.xxxl else Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            SmallFloatingActionButton(
                onClick = { showMapStylePicker = !showMapStylePicker },
                containerColor = MaterialTheme.colorScheme.surface,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            ) {
                Icon(mapStyle.icon, "Map style", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }

            SmallFloatingActionButton(
                onClick = {
                    if (uiState.userLat != null && uiState.userLng != null) {
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(uiState.userLat!!, uiState.userLng!!),
                                14f,
                            )
                        )
                    } else {
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LONDON_CENTER, DEFAULT_ZOOM))
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            ) {
                Icon(Icons.Filled.MyLocation, "Center", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }

        AnimatedVisibility(
            visible = showMapStylePicker,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = Spacing.xxl, bottom = if (selectedStation != null) Spacing.xxxl else Spacing.lg),
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp,
            ) {
                Column(modifier = Modifier.padding(Spacing.sm)) {
                    MapStyle.entries.forEach { style ->
                        Surface(
                            onClick = { mapStyle = style; showMapStylePicker = false },
                            shape = RoundedCornerShape(12.dp),
                            color = if (mapStyle == style) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(style.icon, null, tint = if (mapStyle == style) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(style.label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (mapStyle == style) FontWeight.Bold else FontWeight.Normal, color = if (mapStyle == style) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        // Network Status Strip — live disruption alerts
        AnimatedVisibility(
            visible = uiState.lineStatuses.any { !it.isGoodService },
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 88.dp),
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
        ) {
            NetworkStatusStrip(lineStatuses = uiState.lineStatuses)
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 28.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.93f),
            shadowElevation = 6.dp,
        ) {
            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                val badgeColor = selectedLineFilter?.let { TubeData.getLineById(it)?.color } ?: MaterialTheme.colorScheme.primary
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(badgeColor))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (uiState.journeyRoute != null) {
                        "${uiState.journeyRoute!!.legs.size} steps · active route"
                    } else {
                        "${filteredStations.size} stations" +
                            if (selectedLineFilter != null) " · ${TubeData.getLineById(selectedLineFilter!!)?.name ?: ""}" else ""
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        AnimatedVisibility(
            visible = selectedStation != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
        ) {
            selectedStation?.let { station ->
                StationInfoCard(
                    station = station,
                    arrivals = uiState.stationArrivals,
                    isLoadingArrivals = uiState.isLoadingArrivals,
                    arrivalsError = uiState.arrivalsError,
                    onClose = {
                        selectedStation = null
                        viewModel.selectStation(null)
                    },
                    onViewDetails = { onStationClick(station.id) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationInfoCard(
    station: Station,
    arrivals: com.londontubeai.navigator.data.model.StationArrivals?,
    isLoadingArrivals: Boolean,
    arrivalsError: Boolean,
    onClose: () -> Unit,
    onViewDetails: () -> Unit,
) {
    val stationLines = remember(station.id) { TubeData.getLinesForStation(station.id) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Handle bar
            Box(modifier = Modifier.align(Alignment.CenterHorizontally).width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Station icon with line color
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = (stationLines.firstOrNull()?.color ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.12f),
                ) {
                    Box(modifier = Modifier.padding(10.dp)) {
                        Icon(Icons.Filled.Train, null, tint = stationLines.firstOrNull()?.color ?: MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(station.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Zone ${station.zone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (station.hasStepFreeAccess) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(shape = RoundedCornerShape(6.dp), color = StatusGood.copy(alpha = 0.12f)) {
                                Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AccessibleForward, null, tint = StatusGood, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Step-free", style = MaterialTheme.typography.labelSmall, color = StatusGood, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, "Close", modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Line chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(stationLines) { line ->
                    Surface(shape = RoundedCornerShape(10.dp), color = line.color.copy(alpha = 0.12f)) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(line.color))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(line.name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = if (isLightColor(line.color)) Color.Black else line.color)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stats row
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    InfoItem("${station.lineIds.size}", "Lines")
                    InfoItem("${station.exits.size}", "Exits")
                    if (station.interchangeTimeMinutes > 0) InfoItem("${station.interchangeTimeMinutes}m", "Interchange")
                    if (station.annualPassengers > 0) InfoItem("${station.annualPassengers.toInt()}M", "Passengers")
                }
            }

            // Facilities row
            if (station.wifiAvailable || station.toiletsAvailable || station.hasStepFreeAccess) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (station.wifiAvailable) {
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.NearMe, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WiFi", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    if (station.toiletsAvailable) {
                        Surface(shape = RoundedCornerShape(8.dp), color = StatusMinor.copy(alpha = 0.08f)) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Place, null, tint = StatusMinor, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Toilets", style = MaterialTheme.typography.labelSmall, color = StatusMinor, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // ── Live Arrivals ────────────────────────────────────────────────
            if (isLoadingArrivals || arrivals != null || arrivalsError) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Live Arrivals",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (isLoadingArrivals) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else if (arrivals != null) {
                        Text(
                            "Live",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusGood,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                when {
                    arrivalsError -> {
                        Text(
                            "Could not load arrivals. Check your connection.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    arrivals != null && arrivals.arrivals.isEmpty() -> {
                        Text(
                            "No trains expected in the next 30 minutes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    arrivals != null -> {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            arrivals.arrivals
                                .sortedBy { it.timeToStationSeconds }
                                .take(5)
                                .forEach { arrival ->
                                    val lineColor = com.londontubeai.navigator.data.model.TubeData
                                        .getLineById(arrival.lineId)?.color ?: MaterialTheme.colorScheme.primary
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(lineColor)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    arrival.destination,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                                if (arrival.platform.isNotBlank()) {
                                                    Text(
                                                        arrival.platform,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                }
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = when {
                                                    arrival.timeToStationSeconds < 60 -> StatusSevere.copy(alpha = 0.12f)
                                                    arrival.timeToStationSeconds < 180 -> StatusMinor.copy(alpha = 0.12f)
                                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                                },
                                            ) {
                                                Text(
                                                    arrival.displayTime,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when {
                                                        arrival.timeToStationSeconds < 60 -> StatusSevere
                                                        arrival.timeToStationSeconds < 180 -> StatusMinor
                                                        else -> MaterialTheme.colorScheme.primary
                                                    },
                                                )
                                            }
                                        }
                                    }
                                }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // View details button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary,
                onClick = onViewDetails,
            ) {
                Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Train, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Station Details", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun InfoItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
    }
}

private fun colorToHue(color: Color): Float {
    val argb = color.toArgb()
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV((argb shr 16 and 0xFF), (argb shr 8 and 0xFF), (argb and 0xFF), hsv)
    return hsv[0]
}

private fun isLightColor(color: Color): Boolean {
    val argb = color.toArgb()
    val luminance = 0.299 * ((argb shr 16 and 0xFF) / 255.0) + 0.587 * ((argb shr 8 and 0xFF) / 255.0) + 0.114 * ((argb and 0xFF) / 255.0)
    return luminance > 0.6
}

private fun createStationBitmap(color: Color, stepFree: Boolean): android.graphics.Bitmap {
    val size = 44
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    // Outer ring
    val ringPaint = android.graphics.Paint().apply { this.color = color.toArgb(); isAntiAlias = true; style = android.graphics.Paint.Style.STROKE; strokeWidth = 4f }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 3f, ringPaint)

    // Inner fill
    val fillPaint = android.graphics.Paint().apply { this.color = Color.White.toArgb(); isAntiAlias = true }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 7f, fillPaint)

    // Center dot
    val dotPaint = android.graphics.Paint().apply { this.color = color.toArgb(); isAntiAlias = true }
    canvas.drawCircle(size / 2f, size / 2f, 6f, dotPaint)

    // Step-free indicator (small green dot at bottom-right)
    if (stepFree) {
        val sfPaint = android.graphics.Paint().apply { this.color = StatusGood.toArgb(); isAntiAlias = true }
        canvas.drawCircle(size - 8f, size - 8f, 5f, sfPaint)
    }
    return bitmap
}

private fun createClusterBitmap(count: Int, color: Color): android.graphics.Bitmap {
    val size = if (count < 10) 56 else if (count < 50) 64 else 72
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    // Shadow circle
    val shadowPaint = android.graphics.Paint().apply { this.color = android.graphics.Color.argb(40, 0, 0, 0); isAntiAlias = true }
    canvas.drawCircle(size / 2f + 1f, size / 2f + 2f, size / 2f - 1f, shadowPaint)

    // Main circle
    val paint = android.graphics.Paint().apply { this.color = color.toArgb(); isAntiAlias = true }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 3f, paint)

    // White ring
    val ring = android.graphics.Paint().apply { this.color = Color.White.toArgb(); isAntiAlias = true; style = android.graphics.Paint.Style.STROKE; strokeWidth = 3f }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 5f, ring)

    // Count text
    val textPaint = android.graphics.Paint().apply {
        this.color = if (isLightColor(color)) Color.Black.toArgb() else Color.White.toArgb()
        textSize = if (count < 10) 22f else if (count < 100) 18f else 14f
        isAntiAlias = true; textAlign = android.graphics.Paint.Align.CENTER; isFakeBoldText = true
    }
    canvas.drawText(count.toString(), size / 2f, size / 2f + textPaint.textSize / 3f, textPaint)
    return bitmap
}

@Composable
private fun NetworkStatusStrip(lineStatuses: List<LineStatus>) {
    val disrupted = lineStatuses.filter { !it.isGoodService }
    if (disrupted.isEmpty()) return
    val severe = disrupted.count { it.statusSeverity <= 3 }
    val badgeColor = if (severe > 0) StatusSevere else StatusMinor

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        shadowElevation = 8.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(badgeColor))
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "${disrupted.size} line${if (disrupted.size != 1) "s" else ""} disrupted",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(disrupted.take(6)) { status ->
                    val chipColor = if (status.statusSeverity <= 3) StatusSevere else StatusMinor
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = chipColor.copy(alpha = 0.1f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(status.lineColor))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = status.lineName,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = chipColor,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MapLoadingOverlay() {
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), MaterialTheme.colorScheme.surface))
        ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 4.dp)
            Text("Loading Tube Map...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Text("Preparing interactive network map", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
