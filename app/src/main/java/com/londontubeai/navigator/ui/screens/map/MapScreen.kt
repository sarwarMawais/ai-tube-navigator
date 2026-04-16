package com.londontubeai.navigator.ui.screens.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.AccessibleForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.DisposableEffect
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
import kotlinx.coroutines.delay
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
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.Circle
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
    onNavigateToRoute: (String) -> Unit = {},
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
    var longPressLatLng by remember { mutableStateOf<com.google.android.gms.maps.model.LatLng?>(null) }

    DisposableEffect(Unit) {
        viewModel.setActive(true)
        onDispose { viewModel.setActive(false) }
    }

    LaunchedEffect(routeFromId, routeToId) {
        viewModel.loadJourney(routeFromId, routeToId)
    }

    LaunchedEffect(uiState.journeyRoute?.fromStation?.id, uiState.journeyRoute?.toStation?.id) {
        uiState.journeyRoute?.let { route ->
            val builder = LatLngBounds.Builder()
            builder.include(LatLng(route.fromStation.latitude, route.fromStation.longitude))
            builder.include(LatLng(route.toStation.latitude, route.toStation.longitude))
            route.legs.forEach { leg ->
                leg.stationIds.forEach { sid ->
                    TubeData.getStationById(sid)?.let { builder.include(LatLng(it.latitude, it.longitude)) }
                }
                leg.polylinePoints.forEach { (lat, lng) -> builder.include(LatLng(lat, lng)) }
            }
            delay(400)
            try {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(builder.build(), 160), 1200)
            } catch (e: Exception) {
                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LatLng(route.fromStation.latitude, route.fromStation.longitude), 12f))
            }
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
        if (searchQuery.isBlank()) emptyList()
        else stations.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }.take(6)
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

    // Bitmap cache — avoids recreating hundreds of bitmaps on every recomposition
    val bitmapCache = remember { HashMap<String, com.google.android.gms.maps.model.BitmapDescriptor>() }

    // Bucket zoom into 1-unit steps so clustering only recalculates at whole-number zoom changes
    val zoomBucket = (cameraPositionState.position.zoom).toInt()

    // Cluster stations based on zoom level
    val clusteredStations = remember(zoomBucket, filteredStations) {
        val zoom = cameraPositionState.position.zoom
        if (zoom < CLUSTERING_ZOOM_THRESHOLD) {
            val gridSize = if (zoom < 10f) 10 else 25
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

    // Pulsing animation for user location dot
    val pulseTransition = rememberInfiniteTransition(label = "userPulse")
    val pulseRadius by pulseTransition.animateFloat(
        initialValue = 25f, targetValue = 120f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Restart),
        label = "pulseRadius",
    )
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.45f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Restart),
        label = "pulseAlpha",
    )

    // Per-second clock drives smooth train position interpolation
    var currentTimeMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { delay(1000L); currentTimeMs = System.currentTimeMillis() } }

    // Live train positions derived from nearby arrivals
    val liveTrains = remember(uiState.nearbyArrivals, uiState.nearbyArrivalsUpdatedAt, currentTimeMs, connections) {
        val elapsedSec = ((currentTimeMs - uiState.nearbyArrivalsUpdatedAt) / 1000f).coerceAtLeast(0f)
        buildList {
            uiState.nearbyArrivals.forEach { (stationId, arrivals) ->
                val destStation = TubeData.getStationById(stationId) ?: return@forEach
                arrivals.arrivals
                    .filter { it.timeToStationSeconds in 0..720 }
                    .forEach { arrival ->
                        // Prefer connection where train is physically travelling TO stationId
                        val conn = connections.firstOrNull { c ->
                            c.toStationId == stationId && c.lineId == arrival.lineId
                        } ?: connections.firstOrNull { c ->
                            (c.toStationId == stationId || c.fromStationId == stationId) &&
                            c.lineId == arrival.lineId
                        } ?: return@forEach
                        val otherStationId = conn.fromStationId
                        val otherStation = TubeData.getStationById(otherStationId) ?: return@forEach
                        val adjustedSecs = (arrival.timeToStationSeconds - elapsedSec).coerceAtLeast(0f)
                        val segmentSecs = (conn.travelTimeMinutes * 60).coerceAtLeast(60).toFloat()
                        val fraction = (1f - adjustedSecs / segmentSecs).coerceIn(0.04f, 0.96f)
                        val trainLat = otherStation.latitude + fraction * (destStation.latitude - otherStation.latitude)
                        val trainLng = otherStation.longitude + fraction * (destStation.longitude - otherStation.longitude)
                        add(Triple(LatLng(trainLat, trainLng), arrival.lineColor, arrival.lineId to arrival.lineName))
                    }
            }
        }
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
                longPressLatLng = null
            },
            onMapLongClick = { latLng ->
                longPressLatLng = latLng
                selectedStation = null
                viewModel.selectStation(null)
            },
        ) {
            // Draw tube line polylines — hidden when a journey route is active for clarity
            if (uiState.journeyRoute == null) filteredConnections.groupBy { it.lineId }.forEach { (lineId, conns) ->
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

            // ── Active Journey Route Polylines ───────────────────────────────
            uiState.journeyRoute?.legs?.forEach { leg ->
                val routePoints = when {
                    leg.polylinePoints.isNotEmpty() ->
                        leg.polylinePoints.map { (lat, lng) -> LatLng(lat, lng) }
                    leg.mode == TransportMode.TUBE ->
                        leg.stationIds.mapNotNull { TubeData.getStationById(it)?.let { s -> LatLng(s.latitude, s.longitude) } }
                            .ifEmpty { listOf(LatLng(leg.fromStation.latitude, leg.fromStation.longitude), LatLng(leg.toStation.latitude, leg.toStation.longitude)) }
                    else -> listOf(LatLng(leg.fromStation.latitude, leg.fromStation.longitude), LatLng(leg.toStation.latitude, leg.toStation.longitude))
                }
                if (routePoints.size >= 2) {
                    Polyline(
                        points = routePoints,
                        color = when (leg.mode) {
                            TransportMode.WALKING -> StatusGood
                            TransportMode.BUS -> Color(0xFFE32017)
                            TransportMode.TUBE -> leg.line.color
                        },
                        width = when (leg.mode) {
                            TransportMode.WALKING -> 8f
                            TransportMode.BUS -> 10f
                            TransportMode.TUBE -> 16f
                        },
                        pattern = when (leg.mode) {
                            TransportMode.WALKING -> listOf(Dash(18f), Gap(10f))
                            TransportMode.BUS -> listOf(Dot(), Gap(8f))
                            TransportMode.TUBE -> null
                        },
                        zIndex = 3f,
                        geodesic = true,
                    )
                    // Shadow/outline for tube lines
                    if (leg.mode == TransportMode.TUBE) {
                        Polyline(
                            points = routePoints,
                            color = Color.Black.copy(alpha = 0.25f),
                            width = 20f,
                            zIndex = 2f,
                            geodesic = true,
                        )
                    }
                }
                // Intermediate stop dots on tube legs
                if (leg.mode == TransportMode.TUBE) {
                    leg.stationIds.drop(1).dropLast(1).forEach { stopId ->
                        val s = TubeData.getStationById(stopId) ?: return@forEach
                        Circle(
                            center = LatLng(s.latitude, s.longitude),
                            radius = 60.0,
                            strokeColor = leg.line.color,
                            fillColor = Color.White,
                            strokeWidth = 3f,
                            zIndex = 4f,
                        )
                    }
                }
            }

            // ── Journey Start / End Markers ───────────────────────────────────
            uiState.journeyRoute?.let { journey ->
                val startPos = LatLng(journey.fromStation.latitude, journey.fromStation.longitude)
                val endPos = LatLng(journey.toStation.latitude, journey.toStation.longitude)
                Circle(center = startPos, radius = 60.0, strokeColor = StatusGood, fillColor = StatusGood.copy(alpha = 0.3f), strokeWidth = 4f, zIndex = 5f)
                Marker(
                    state = MarkerState(position = startPos),
                    title = "🟢 Start · ${journey.fromStation.name}",
                    snippet = "Journey origin",
                    icon = bitmapCache.getOrPut("start") {
                        BitmapDescriptorFactory.fromBitmap(createRouteEndpointBitmap(android.graphics.Color.rgb(76, 175, 80), "A"))
                    },
                    zIndex = 5f,
                )
                Circle(center = endPos, radius = 60.0, strokeColor = Color(0xFFE53935), fillColor = Color(0xFFE53935).copy(alpha = 0.3f), strokeWidth = 4f, zIndex = 5f)
                Marker(
                    state = MarkerState(position = endPos),
                    title = "🔴 Destination · ${journey.toStation.name}",
                    snippet = "Journey destination",
                    icon = bitmapCache.getOrPut("end") {
                        BitmapDescriptorFactory.fromBitmap(createRouteEndpointBitmap(android.graphics.Color.rgb(229, 57, 53), "B"))
                    },
                    zIndex = 5f,
                )
            }

            // ── Animated User Location ────────────────────────────────────────
            if (uiState.userLat != null && uiState.userLng != null) {
                val userPos = LatLng(uiState.userLat!!, uiState.userLng!!)
                // Outer pulsing ring (scaled to meters so it’s visible at zoom 13-15)
                Circle(
                    center = userPos,
                    radius = (pulseRadius * 2.5f).toDouble().coerceIn(80.0, 350.0),
                    strokeColor = Color(0xFF1565C0).copy(alpha = pulseAlpha * 0.8f),
                    fillColor = Color(0xFF1E88E5).copy(alpha = pulseAlpha * 0.15f),
                    strokeWidth = 3f,
                    zIndex = 9f,
                )
                // Solid accuracy dot (~80m radius)
                Circle(
                    center = userPos,
                    radius = 80.0,
                    strokeColor = Color.White,
                    fillColor = Color(0xFF1E88E5).copy(alpha = 0.2f),
                    strokeWidth = 2f,
                    zIndex = 9f,
                )
                // Prominent marker on top
                Marker(
                    state = MarkerState(position = userPos),
                    title = "📍 You are here",
                    snippet = uiState.nearestStationId?.let { id ->
                        TubeData.getStationById(id)?.let { s ->
                            "Nearest: ${s.name} · ${"%.1f".format(uiState.nearestStationDistanceKm ?: 0.0)} km"
                        }
                    },
                    icon = bitmapCache.getOrPut("user_loc") {
                        BitmapDescriptorFactory.fromBitmap(createUserLocationBitmap())
                    },
                    zIndex = 11f,
                    flat = false,
                )
            }

            // ── Live Train Markers ────────────────────────────────────────────
            liveTrains.forEachIndexed { index, (position, color, lineInfo) ->
                val (lineId, lineName) = lineInfo
                val key = "train_${lineId}_${color.toArgb()}"
                val shortName = lineName.split(" ").firstOrNull()?.take(3)?.uppercase() ?: lineId.take(3).uppercase()
                Marker(
                    state = MarkerState(position = position),
                    title = "🚂 $lineName",
                    snippet = "Live train approaching",
                    icon = bitmapCache.getOrPut(key) {
                        BitmapDescriptorFactory.fromBitmap(createTrainBitmap(color, shortName))
                    },
                    zIndex = 7f,
                    alpha = 0.95f,
                )
            }

            // Draw station markers — hidden when a route is active
            if (uiState.journeyRoute == null) clusteredStations.forEach { cluster ->
                val isCluster = cluster.count > 1
                val primaryLine = cluster.primaryLineId?.let { TubeData.getLineById(it) }
                val lineColor = primaryLine?.color ?: TubePrimary
                val colorArgb = lineColor.toArgb()

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
                        val key = "c_${cluster.count}_$colorArgb"
                        bitmapCache.getOrPut(key) {
                            BitmapDescriptorFactory.fromBitmap(createClusterBitmap(cluster.count, lineColor))
                        }
                    } else {
                        val s = cluster.stations.first()
                        val isSelected = selectedStation?.id == s.id
                        val key = "s_${colorArgb}_${s.hasStepFreeAccess}_$isSelected"
                        bitmapCache.getOrPut(key) {
                            BitmapDescriptorFactory.fromBitmap(
                                createStationBitmap(lineColor, s.hasStepFreeAccess, selected = isSelected)
                            )
                        }
                    },
                    zIndex = if (selectedStation?.id == cluster.stations.firstOrNull()?.id) 4f else 1f,
                    onClick = {
                        if (isCluster) {
                            cameraPositionState.move(
                                CameraUpdateFactory.newLatLngZoom(cluster.position, cameraPositionState.position.zoom + 2f)
                            )
                            true
                        } else {
                            val station = cluster.stations.first()
                            selectedStation = station
                            viewModel.selectStation(station)
                            true
                        }
                    },
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = Spacing.sm, start = Spacing.screenHorizontal, end = Spacing.screenHorizontal),
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
                                        viewModel.selectStation(station)
                                        cameraPositionState.move(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(station.latitude, station.longitude), 15.5f
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
                                                Spacer(modifier = Modifier.width(4.dp))
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
                } else if (showSearch && searchQuery.isNotBlank()) {
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

        if (uiState.journeyRoute == null && uiState.nearestStationId != null && !showSearch) {
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
            modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(end = Spacing.screenHorizontal, bottom = if (selectedStation != null) 300.dp else Spacing.lg),
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
            modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(end = Spacing.xxl, bottom = if (selectedStation != null) 300.dp else Spacing.lg),
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

        // Network Status Strip + station badge — stacked in a column to prevent overlap
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(start = 16.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AnimatedVisibility(
                visible = uiState.lineStatuses.any { !it.isGoodService },
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
            ) {
                NetworkStatusStrip(lineStatuses = uiState.lineStatuses)
            }
            Surface(
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
        }

        // ── Offline / cached-status banner ───────────────────────────────
        AnimatedVisibility(
            visible = uiState.offlineMessage != null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 80.dp, start = Spacing.screenHorizontal, end = Spacing.screenHorizontal),
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (uiState.isUsingCachedStatuses) StatusMinor.copy(alpha = 0.92f) else StatusSevere.copy(alpha = 0.92f),
                shadowElevation = 6.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (uiState.isUsingCachedStatuses) Icons.Filled.Info else Icons.Filled.Info,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        uiState.offlineMessage ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        // ── Long-press contextual action sheet ───────────────────────────
        AnimatedVisibility(
            visible = longPressLatLng != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Box(modifier = Modifier.align(Alignment.CenterHorizontally).width(36.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Map location", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    longPressLatLng?.let {
                        Text(
                            "${"%,.5f".format(it.latitude)}, ${"%,.5f".format(it.longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            onClick = {
                                // Capture before clearing
                                val lp = longPressLatLng
                                longPressLatLng = null
                                if (lp != null) {
                                    val nearest = TubeData.getAllStationsSorted().minByOrNull {
                                        val dlat = it.latitude - lp.latitude
                                        val dlng = it.longitude - lp.longitude
                                        dlat * dlat + dlng * dlng
                                    }
                                    nearest?.let { onNavigateToRoute(it.id) }
                                }
                            },
                        ) {
                            Row(modifier = Modifier.padding(11.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.DirectionsSubway, null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Route from here", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            onClick = { longPressLatLng = null },
                        ) {
                            Row(modifier = Modifier.padding(11.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Close, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Dismiss", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        // ── Station bottom sheet ─────────────────────────────────────────
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
                    lineStatuses = uiState.lineStatuses,
                    arrivalsUpdatedAt = uiState.nearbyArrivalsUpdatedAt,
                    onClose = {
                        selectedStation = null
                        viewModel.selectStation(null)
                    },
                    onViewDetails = { onStationClick(station.id) },
                    onPlanRoute = { onNavigateToRoute(station.id) },
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
    lineStatuses: List<LineStatus> = emptyList(),
    arrivalsUpdatedAt: Long = 0L,
    onClose: () -> Unit,
    onViewDetails: () -> Unit,
    onPlanRoute: () -> Unit = {},
) {
    val freshnessLabel = remember(arrivalsUpdatedAt) {
        if (arrivalsUpdatedAt == 0L) null
        else {
            val ageSeconds = ((System.currentTimeMillis() - arrivalsUpdatedAt) / 1000).toInt()
            when {
                ageSeconds < 30 -> "Just now"
                ageSeconds < 90 -> "${ageSeconds}s ago"
                else -> "${ageSeconds / 60}m ago"
            }
        }
    }
    val stationLines = remember(station.id) { TubeData.getLinesForStation(station.id) }
    val stationLineStatusMap = remember(lineStatuses, station.id) {
        lineStatuses.filter { ls -> station.lineIds.contains(ls.lineId) }.associateBy { it.lineId }
    }

    Card(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
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
                                    Icon(Icons.AutoMirrored.Filled.AccessibleForward, contentDescription = null, tint = StatusGood, modifier = Modifier.size(12.dp))
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

            // Line chips with live status badge
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(stationLines) { line ->
                    val ls = stationLineStatusMap[line.id]
                    val statusColor = when {
                        ls == null -> line.color
                        ls.isGoodService -> StatusGood
                        ls.statusSeverity <= 3 -> StatusSevere
                        else -> StatusMinor
                    }
                    Surface(shape = RoundedCornerShape(10.dp), color = line.color.copy(alpha = 0.12f)) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(line.color))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(line.name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = if (isLightColor(line.color)) Color.Black else line.color)
                            if (ls != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(statusColor))
                            }
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StatusGood))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                freshnessLabel?.let { "Live · $it" } ?: "Live",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusGood,
                                fontWeight = FontWeight.Bold,
                            )
                        }
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
                                .take(6)
                                .forEach { arrival ->
                                    val lineColor = com.londontubeai.navigator.data.model.TubeData
                                        .getLineById(arrival.lineId)?.color ?: MaterialTheme.colorScheme.primary
                                    val lineName = com.londontubeai.navigator.data.model.TubeData
                                        .getLineById(arrival.lineId)?.name ?: arrival.lineName
                                    val urgency = when {
                                        arrival.timeToStationSeconds < 60 -> StatusSevere
                                        arrival.timeToStationSeconds < 180 -> StatusMinor
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = lineColor.copy(alpha = 0.07f),
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            // Line color pill
                                            Surface(shape = RoundedCornerShape(6.dp), color = lineColor.copy(alpha = 0.18f)) {
                                                Text(
                                                    lineName.take(3).uppercase(),
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isLightColor(lineColor)) Color.Black else lineColor,
                                                    fontSize = 9.sp,
                                                )
                                            }
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
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Filled.Schedule, null, modifier = Modifier.size(9.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            arrival.platform,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        )
                                                    }
                                                }
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = urgency.copy(alpha = 0.12f),
                                            ) {
                                                Text(
                                                    arrival.displayTime,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = urgency,
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

            // Action buttons row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary,
                    onClick = onViewDetails,
                ) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Train, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Station Details", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    onClick = onPlanRoute,
                ) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Plan Route", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    }
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

private fun createStationBitmap(color: Color, stepFree: Boolean, selected: Boolean = false): android.graphics.Bitmap {
    val size = if (selected) 56 else 46
    val cx = size / 2f
    val cy = size / 2f
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    // Drop shadow
    val shadowPaint = android.graphics.Paint().apply { this.color = android.graphics.Color.argb(50, 0, 0, 0); isAntiAlias = true; maskFilter = android.graphics.BlurMaskFilter(4f, android.graphics.BlurMaskFilter.Blur.NORMAL) }
    canvas.drawCircle(cx + 1f, cy + 2f, cx - 4f, shadowPaint)

    // White background circle
    val bgPaint = android.graphics.Paint().apply { this.color = Color.White.toArgb(); isAntiAlias = true }
    canvas.drawCircle(cx, cy, cx - 3f, bgPaint)

    // Colored outer ring — thick, TfL roundel style
    val ringWidth = if (selected) 6f else 5f
    val ringPaint = android.graphics.Paint().apply { this.color = color.toArgb(); isAntiAlias = true; style = android.graphics.Paint.Style.STROKE; strokeWidth = ringWidth }
    canvas.drawCircle(cx, cy, cx - 3f - ringWidth / 2f, ringPaint)

    // Horizontal bar through center (roundel bar)
    val barH = if (selected) 7f else 6f
    val barPaint = android.graphics.Paint().apply { this.color = color.toArgb(); isAntiAlias = true }
    val barRect = android.graphics.RectF(3f, cy - barH / 2f, size - 3f, cy + barH / 2f)
    canvas.drawRoundRect(barRect, barH / 2f, barH / 2f, barPaint)

    // Step-free indicator — small green dot at bottom-right
    if (stepFree) {
        val sfBg = android.graphics.Paint().apply { this.color = Color.White.toArgb(); isAntiAlias = true }
        canvas.drawCircle(size - 7f, size - 7f, 6f, sfBg)
        val sfPaint = android.graphics.Paint().apply { this.color = StatusGood.toArgb(); isAntiAlias = true }
        canvas.drawCircle(size - 7f, size - 7f, 4.5f, sfPaint)
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

private fun createUserLocationBitmap(): android.graphics.Bitmap {
    val size = 52
    val cx = size / 2f
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    // Drop shadow
    val shadowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(80, 0, 0, 0)
        maskFilter = android.graphics.BlurMaskFilter(6f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    canvas.drawCircle(cx + 1.5f, cx + 2f, cx - 5f, shadowPaint)
    // Outer white ring
    val whitePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE; style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(cx, cx, cx - 3f, whitePaint)
    // Blue fill
    val bluePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(25, 118, 210); style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(cx, cx, cx - 7f, bluePaint)
    // Inner white dot
    val dotPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE; style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(cx, cx, 5.5f, dotPaint)
    // Directional arrow hint (top wedge)
    val arrowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(25, 118, 210); style = android.graphics.Paint.Style.FILL
    }
    val path = android.graphics.Path().apply {
        moveTo(cx, 2f); lineTo(cx - 6f, 12f); lineTo(cx + 6f, 12f); close()
    }
    canvas.drawPath(path, whitePaint)
    canvas.drawPath(path, arrowPaint)
    return bitmap
}

private fun createRouteEndpointBitmap(argbColor: Int, label: String): android.graphics.Bitmap {
    val size = 52
    val cx = size / 2f
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val shadowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(60, 0, 0, 0)
        maskFilter = android.graphics.BlurMaskFilter(5f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    canvas.drawCircle(cx + 1.5f, cx + 2f, cx - 4f, shadowPaint)
    val fillPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = argbColor }
    canvas.drawCircle(cx, cx, cx - 4f, fillPaint)
    val strokePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE; style = android.graphics.Paint.Style.STROKE; strokeWidth = 3.5f
    }
    canvas.drawCircle(cx, cx, cx - 4f, strokePaint)
    val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE; textSize = 18f; isFakeBoldText = true
        textAlign = android.graphics.Paint.Align.CENTER
    }
    canvas.drawText(label, cx, cx + textPaint.textSize / 3f, textPaint)
    return bitmap
}

private fun createTrainBitmap(color: Color, shortLabel: String = ""): android.graphics.Bitmap {
    val w = 80; val h = 60
    val bitmap = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    // Glow/shadow behind the body
    val glowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color.toArgb()
        maskFilter = android.graphics.BlurMaskFilter(9f, android.graphics.BlurMaskFilter.Blur.NORMAL)
        alpha = 110
    }
    canvas.drawRoundRect(android.graphics.RectF(4f, 6f, 76f, 40f), 11f, 11f, glowPaint)

    // Main body
    val bodyPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { this.color = color.toArgb() }
    canvas.drawRoundRect(android.graphics.RectF(2f, 4f, 78f, 42f), 11f, 11f, bodyPaint)

    // White border outline
    val outlinePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.WHITE; style = android.graphics.Paint.Style.STROKE; strokeWidth = 2.5f
    }
    canvas.drawRoundRect(android.graphics.RectF(2f, 4f, 78f, 42f), 11f, 11f, outlinePaint)

    // Line name label on the body (draw a white semi-transparent band first)
    if (shortLabel.isNotEmpty()) {
        val labelBgPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            this.color = android.graphics.Color.argb(140, 0, 0, 0)
        }
        canvas.drawRoundRect(android.graphics.RectF(8f, 10f, 72f, 28f), 6f, 6f, labelBgPaint)
        val textColor = if (isLightColor(color)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        val labelPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            this.color = android.graphics.Color.WHITE
            textSize = 13f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.drawText(shortLabel, 40f, 23f, labelPaint)
    } else {
        // Fallback: windows row
        val winPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            this.color = android.graphics.Color.argb(200, 220, 240, 255)
        }
        listOf(8f, 24f, 40f, 56f).forEach { x ->
            canvas.drawRoundRect(android.graphics.RectF(x, 10f, x + 13f, 24f), 3f, 3f, winPaint)
        }
    }

    // Headlight on right end
    val headPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.argb(230, 255, 255, 180)
    }
    canvas.drawRoundRect(android.graphics.RectF(66f, 8f, 76f, 22f), 3f, 3f, headPaint)

    // Wheels
    val wheelPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { this.color = android.graphics.Color.rgb(40, 40, 40) }
    val wheelShine = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { this.color = android.graphics.Color.rgb(130, 130, 130) }
    listOf(15f, 40f, 65f).forEach { x ->
        canvas.drawCircle(x, 50f, 7.5f, wheelPaint)
        canvas.drawCircle(x, 50f, 3.5f, wheelShine)
    }

    // Rail bar
    val railPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.rgb(80, 80, 80); strokeWidth = 3f; style = android.graphics.Paint.Style.STROKE
    }
    canvas.drawLine(4f, 50f, 76f, 50f, railPaint)

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
