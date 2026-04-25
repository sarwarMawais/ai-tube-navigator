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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import com.londontubeai.navigator.data.model.LineStatus
import com.londontubeai.navigator.data.model.Station
import com.londontubeai.navigator.data.model.RouteSource
import com.londontubeai.navigator.data.model.TransportMode
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material.icons.filled.WifiOff
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

private data class LiveTrainMarkerUi(
    val position: LatLng,
    val color: Color,
    val lineId: String,
    val lineName: String,
    val isEstimated: Boolean,
)

private enum class MapStyle(val label: String, val icon: ImageVector, val mapType: MapType) {
    NORMAL("Standard", Icons.Filled.Map, MapType.NORMAL),
    TERRAIN("Terrain", Icons.Filled.Terrain, MapType.TERRAIN),
    SATELLITE("Satellite", Icons.Filled.DarkMode, MapType.HYBRID);

    companion object {
        fun fromName(name: String): MapStyle = entries.firstOrNull { it.name == name } ?: NORMAL
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBack: () -> Unit = {},
    onStationClick: (String) -> Unit = {},
    onNavigateToRoute: (String?, String?, Double?, Double?) -> Unit = { _, _, _, _ -> },
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

    val cameraScope = androidx.compose.runtime.rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    var selectedStation by remember { mutableStateOf<Station?>(null) }
    var selectedLineFilter by remember { mutableStateOf<String?>(null) }
    var showLineFilters by remember { mutableStateOf(false) }
    // Phase D filter overlays
    var showDisruptedOnly by remember { mutableStateOf(false) }
    var showStepFreeOnly by remember { mutableStateOf(false) }
    // Null = all zones. "6" means zone 6 or higher (outer zones grouped).
    var selectedZone by remember { mutableStateOf<String?>(null) }
    // Follow-mode: camera keeps re-centering on the user's live position.
    var followMode by remember { mutableStateOf(false) }
    // Traffic overlay + 3D tilt — controllable from the map-style picker FAB.
    var trafficEnabled by remember { mutableStateOf(false) }
    var tilt3dEnabled by remember { mutableStateOf(false) }
    // Re-centre FAB cycle state: 0 = user, 1 = route bounds, 2 = fit all (user + route).
    var recenterMode by remember { mutableStateOf(0) }
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

    LaunchedEffect(uiState.mapStyleName) {
        mapStyle = MapStyle.fromName(uiState.mapStyleName)
    }

    LaunchedEffect(uiState.selectedLineFilter) {
        selectedLineFilter = uiState.selectedLineFilter
    }

    LaunchedEffect(uiState.selectedStationId, uiState.journeyRoute) {
        if (uiState.journeyRoute != null) {
            selectedStation = null
            longPressLatLng = null
            showSearch = false
            searchQuery = ""
            showMapStylePicker = false
        } else {
            selectedStation = uiState.selectedStationId?.let { TubeData.getStationById(it) }
        }
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

    // 3D-tilt toggle — animate camera to a slanted 55° view (or back to top-down 0°)
    // whenever the user flips the tilt FAB. `tilt3dInitialized` guards against
    // animating the camera on initial composition before the user has tapped.
    var tilt3dInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(tilt3dEnabled) {
        if (!tilt3dInitialized) { tilt3dInitialized = true; return@LaunchedEffect }
        try {
            val cur = cameraPositionState.position
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(cur.target)
                        .zoom(if (tilt3dEnabled) maxOf(cur.zoom, 16f) else cur.zoom)
                        .tilt(if (tilt3dEnabled) 55f else 0f)
                        .bearing(cur.bearing)
                        .build()
                ),
                700,
            )
        } catch (_: Exception) { /* camera ops can throw during gestures */ }
    }

    // Follow-mode: animate the camera each time the user moves and a route isn't active.
    LaunchedEffect(followMode, uiState.userLat, uiState.userLng) {
        if (followMode && uiState.userLat != null && uiState.userLng != null && uiState.journeyRoute == null) {
            try {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(uiState.userLat!!, uiState.userLng!!),
                        16f,
                    ),
                    600,
                )
            } catch (_: Exception) {
                // ignore — camera ops can throw during rapid gesture interruptions
            }
        }
    }

    val searchResults = uiState.searchResults
    val recentPlaces = uiState.recentPlaces.take(6)

    // Filter stations by selected line + optional step-free + zone overlays.
    val filteredStations = remember(selectedLineFilter, showStepFreeOnly, selectedZone) {
        val base = if (selectedLineFilter == null) stations
        else stations.filter { it.lineIds.contains(selectedLineFilter) }
        val afterStepFree = if (showStepFreeOnly) base.filter { it.hasStepFreeAccess } else base
        when (val z = selectedZone) {
            null -> afterStepFree
            "6" -> afterStepFree.filter { (it.zone.toIntOrNull() ?: 0) >= 6 }
            else -> afterStepFree.filter { it.zone == z }
        }
    }

    // Filter connections by selected line + optional "disrupted only" overlay.
    val disruptedLineIds = remember(uiState.lineStatuses) {
        uiState.lineStatuses.filter { !it.isGoodService }.map { it.lineId }.toSet()
    }
    val filteredConnections = remember(selectedLineFilter, showDisruptedOnly, disruptedLineIds) {
        val base = if (selectedLineFilter == null) connections
        else connections.filter { it.lineId == selectedLineFilter }
        if (showDisruptedOnly) base.filter { it.lineId in disruptedLineIds } else base
    }

    // Bitmap cache — avoids recreating hundreds of bitmaps on every recomposition. Capped via
    // LinkedHashMap in access order so the least-recently-used entries are evicted once the
    // map exceeds MAX_BITMAP_CACHE.
    val bitmapCache = remember {
        object : java.util.LinkedHashMap<String, com.google.android.gms.maps.model.BitmapDescriptor>(
            128, 0.75f, true,
        ) {
            override fun removeEldestEntry(
                eldest: MutableMap.MutableEntry<String, com.google.android.gms.maps.model.BitmapDescriptor>?,
            ): Boolean = size > 256
        }
    }

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

    // 3-second tick drives train-position interpolation. Live arrivals refresh every 15s, so
    // interpolating any faster than 3s is pure CPU churn with no perceivable visual gain.
    var currentTimeMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { delay(3_000L); currentTimeMs = System.currentTimeMillis() } }

    // ─── Active-route animation state ────────────────────────────────────
    // Flatten every leg's polyline into one ordered list of LatLng so we can
    // (a) animate chevron markers flowing along the full journey, and
    // (b) place a single "you are here" dot based on time-elapsed fraction.
    val routeFlatPath: List<LatLng> = remember(uiState.journeyRoute) {
        val route = uiState.journeyRoute ?: return@remember emptyList()
        buildList {
            route.legs.forEach { leg ->
                val pts = when {
                    leg.polylinePoints.isNotEmpty() ->
                        leg.polylinePoints.map { (la, lo) -> LatLng(la, lo) }
                    leg.stationIds.isNotEmpty() ->
                        leg.stationIds.mapNotNull { sid ->
                            TubeData.getStationById(sid)?.let { LatLng(it.latitude, it.longitude) }
                        }
                    else -> listOf(
                        LatLng(leg.fromStation.latitude, leg.fromStation.longitude),
                        LatLng(leg.toStation.latitude, leg.toStation.longitude),
                    )
                }.filter { it.latitude != 0.0 && it.longitude != 0.0 }
                if (pts.isNotEmpty()) {
                    if (isNotEmpty() && last() == pts.first()) addAll(pts.drop(1)) else addAll(pts)
                }
            }
        }
    }
    // Cumulative arc length per vertex — used to sample positions at a 0..1 fraction.
    val routeCumKm: FloatArray = remember(routeFlatPath) {
        if (routeFlatPath.size < 2) floatArrayOf(0f)
        else FloatArray(routeFlatPath.size).also { arr ->
            var acc = 0f
            arr[0] = 0f
            for (i in 1 until routeFlatPath.size) {
                val a = routeFlatPath[i - 1]; val b = routeFlatPath[i]
                acc += haversineKm(a.latitude, a.longitude, b.latitude, b.longitude).toFloat()
                arr[i] = acc
            }
        }
    }

    // 1-second tick for chevron flow + you-are-here progress. Cheap enough.
    var journeyTickMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(uiState.journeyRoute) {
        while (uiState.journeyRoute != null) { delay(1_000L); journeyTickMs = System.currentTimeMillis() }
    }

    // Normalised animation phase for chevrons (0..1 looping every 4s).
    val chevronPhase = ((journeyTickMs / 4_000f) % 1f + 1f) % 1f

    // Sample chevron positions at regularly spaced fractions along the route.
    // 8 chevrons feels dense enough without cluttering the map at typical zooms.
    val chevronMarkers: List<LatLng> = remember(routeFlatPath, routeCumKm, chevronPhase) {
        if (routeFlatPath.size < 2 || routeCumKm.last() <= 0.01f) emptyList()
        else {
            val count = 8
            (0 until count).map { i ->
                val frac = (i.toFloat() / count + chevronPhase) % 1f
                sampleAlongPath(routeFlatPath, routeCumKm, frac)
            }
        }
    }

    // "You are here" progress along the route — driven by the scheduled
    // departure + arrival epoch on the route. Before departure → at start,
    // after arrival → at end, otherwise linear fraction.
    val progressFraction: Float? = remember(uiState.journeyRoute, journeyTickMs) {
        val r = uiState.journeyRoute ?: return@remember null
        val dep = r.scheduledDepartureEpochMs.takeIf { it > 0L } ?: return@remember null
        val arr = r.scheduledArrivalEpochMs.takeIf { it > 0L } ?: return@remember null
        if (arr <= dep) return@remember null
        val now = System.currentTimeMillis()
        when {
            now <= dep -> 0f
            now >= arr -> 1f
            else -> ((now - dep).toFloat() / (arr - dep).toFloat()).coerceIn(0f, 1f)
        }
    }
    val youArePos: LatLng? = remember(routeFlatPath, routeCumKm, progressFraction) {
        if (progressFraction == null || routeFlatPath.size < 2) null
        else sampleAlongPath(routeFlatPath, routeCumKm, progressFraction)
    }

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
                        add(
                            LiveTrainMarkerUi(
                                position = LatLng(trainLat, trainLng),
                                color = arrival.lineColor,
                                lineId = arrival.lineId,
                                lineName = arrival.lineName,
                                isEstimated = arrivals.isCached || elapsedSec > 20f,
                            )
                        )
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
                // 3D buildings + tilt — when enabled, central London gets that
                // dramatic slanted-view "Google Maps 3D" look.
                isBuildingEnabled = true,
                isIndoorEnabled = false,
                // Live Google traffic overlay — respects user toggle.
                isTrafficEnabled = trafficEnabled,
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                // Enable the native compass — surfaces only after the user
                // rotates the map, and tapping resets bearing to north.
                compassEnabled = true,
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
                viewModel.updateSearchQuery("")
                showMapStylePicker = false
                longPressLatLng = null
                viewModel.clearPinnedLocation()
            },
            onMapLongClick = { latLng ->
                longPressLatLng = latLng
                selectedStation = null
                viewModel.selectStation(null)
                showSearch = false
                searchQuery = ""
                viewModel.updateSearchQuery("")
                showMapStylePicker = false
                viewModel.resolvePinnedLocation(latLng.latitude, latLng.longitude)
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
            // Premium triple-stack rendering: soft dark shadow UNDER the coloured
            // core, and a thin lighter highlight ON TOP — gives the route a
            // lifted 3D "Google Maps Live Navigation" look on every leg type.
            uiState.journeyRoute?.legs?.forEach { leg ->
                // Coordinate validity gate — a coord pair is only trustworthy
                // when neither value is (0,0); this stops buggy 0-lat/lng fallbacks
                // from rendering a diagonal line across the globe.
                fun LatLng.isReal() = latitude != 0.0 && longitude != 0.0
                val rawPoints = when {
                    leg.polylinePoints.isNotEmpty() ->
                        leg.polylinePoints.map { (lat, lng) -> LatLng(lat, lng) }
                    leg.mode == TransportMode.TUBE ->
                        leg.stationIds.mapNotNull { TubeData.getStationById(it)?.let { s -> LatLng(s.latitude, s.longitude) } }
                            .ifEmpty { listOf(LatLng(leg.fromStation.latitude, leg.fromStation.longitude), LatLng(leg.toStation.latitude, leg.toStation.longitude)) }
                    else -> listOf(LatLng(leg.fromStation.latitude, leg.fromStation.longitude), LatLng(leg.toStation.latitude, leg.toStation.longitude))
                }
                val routePoints = rawPoints.filter { it.isReal() }
                if (routePoints.size >= 2) {
                    val coreColor = when (leg.mode) {
                        TransportMode.WALKING -> StatusGood
                        TransportMode.BUS -> Color(0xFFE32017)
                        TransportMode.TUBE -> leg.line.color
                    }
                    val coreWidth = when (leg.mode) {
                        TransportMode.WALKING -> 9f
                        TransportMode.BUS -> 12f
                        TransportMode.TUBE -> 18f
                    }
                    // 1/3 — Soft dark shadow underlay for lift
                    Polyline(
                        points = routePoints,
                        color = Color.Black.copy(alpha = 0.22f),
                        width = coreWidth + 6f,
                        zIndex = 2f,
                        geodesic = true,
                    )
                    // 2/3 — Main coloured stroke
                    Polyline(
                        points = routePoints,
                        color = coreColor,
                        width = coreWidth,
                        pattern = when (leg.mode) {
                            TransportMode.WALKING -> listOf(Dash(22f), Gap(12f))
                            TransportMode.BUS -> listOf(Dash(14f), Gap(6f))
                            TransportMode.TUBE -> null
                        },
                        zIndex = 3f,
                        geodesic = true,
                        jointType = com.google.android.gms.maps.model.JointType.ROUND,
                    )
                    // 3/3 — Thin inner highlight centreline (tube only) —
                    // mimics Google Maps Live Navigation's glossy route.
                    if (leg.mode == TransportMode.TUBE) {
                        Polyline(
                            points = routePoints,
                            color = Color.White.copy(alpha = 0.35f),
                            width = coreWidth * 0.28f,
                            zIndex = 4f,
                            geodesic = true,
                            jointType = com.google.android.gms.maps.model.JointType.ROUND,
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

            // ── Animated chevrons flowing along the active route ─────────────
            // Tiny directional arrows that re-sample every 1s along the
            // flattened polyline — creates a live "flow" effect showing
            // travel direction at a glance.
            if (uiState.journeyRoute != null && chevronMarkers.isNotEmpty()) {
                val chevColor = android.graphics.Color.rgb(25, 118, 210)
                val chevIcon = bitmapCache.getOrPut("chev_$chevColor") {
                    BitmapDescriptorFactory.fromBitmap(createChevronBitmap(chevColor))
                }
                chevronMarkers.forEachIndexed { idx, pos ->
                    Marker(
                        state = MarkerState(position = pos),
                        icon = chevIcon,
                        anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                        flat = true,
                        alpha = 0.9f,
                        zIndex = 4.2f,
                        title = null,
                    )
                }
            }

            // ── "You are here" live progress marker ──────────────────────────
            // Interpolated along the route based on elapsed fraction of the
            // scheduled departure → arrival window. Only shows once the journey
            // is in progress (0 < fraction < 1).
            if (youArePos != null && progressFraction != null && progressFraction > 0f && progressFraction < 1f) {
                val youIcon = bitmapCache.getOrPut("you_on_route") {
                    BitmapDescriptorFactory.fromBitmap(createYouArePinBitmap())
                }
                Marker(
                    state = MarkerState(position = youArePos),
                    icon = youIcon,
                    anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                    flat = true,
                    zIndex = 10f,
                    title = "📍 You are here",
                    snippet = "${(progressFraction * 100).toInt()}% through your journey",
                )
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
            liveTrains.forEach { train ->
                val key = "train_${train.lineId}_${train.color.toArgb()}_${train.isEstimated}"
                val shortName = train.lineName.split(" ").firstOrNull()?.take(3)?.uppercase() ?: train.lineId.take(3).uppercase()
                Marker(
                    state = MarkerState(position = train.position),
                    title = "🚂 ${train.lineName}",
                    snippet = if (train.isEstimated) "Estimated from nearby arrivals" else "Live train estimate",
                    icon = bitmapCache.getOrPut(key) {
                        BitmapDescriptorFactory.fromBitmap(createTrainBitmap(train.color, shortName, train.isEstimated))
                    },
                    zIndex = 7f,
                    alpha = if (train.isEstimated) 0.72f else 0.95f,
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
                            longPressLatLng = null
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
                            onValueChange = {
                                searchQuery = it
                                viewModel.updateSearchQuery(it)
                            },
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
                                        Text("Search stations, landmarks or addresses...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 16.sp)
                                    }
                                    inner()
                                }
                            },
                        )
                        IconButton(
                            onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    searchQuery = ""
                                    viewModel.updateSearchQuery("")
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
                if (showSearch && (uiState.isSearchingPlaces || searchResults.isNotEmpty() || (searchQuery.isBlank() && recentPlaces.isNotEmpty()))) {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)) {
                        Column(modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)) {
                            if (searchQuery.isBlank() && recentPlaces.isNotEmpty()) {
                                Text(
                                    text = "Recent places",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = Spacing.xs),
                                )
                            }
                            if (uiState.isSearchingPlaces) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Searching London…", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            (if (searchQuery.isBlank()) recentPlaces else searchResults).forEach { station ->
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
                                        viewModel.updateSearchQuery("")
                                        showSearch = false
                                        longPressLatLng = null
                                        viewModel.rememberMapPlace(station)
                                        if (station.id.startsWith("place:")) {
                                            selectedStation = null
                                            viewModel.selectStation(null)
                                            longPressLatLng = com.google.android.gms.maps.model.LatLng(station.latitude, station.longitude)
                                            viewModel.resolvePinnedLocation(station.latitude, station.longitude, station.name)
                                            cameraPositionState.move(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(station.latitude, station.longitude), 15.5f
                                                )
                                            )
                                        } else {
                                            selectedStation = station
                                            viewModel.selectStation(station)
                                            cameraPositionState.move(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(station.latitude, station.longitude), 15.5f
                                                )
                                            )
                                        }
                                    },
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Surface(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(primaryColor.copy(alpha = 0.12f)),
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Icon(if (station.id.startsWith("place:")) Icons.Filled.Place else Icons.Filled.Train, null, tint = primaryColor, modifier = Modifier.size(16.dp))
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.NearMe, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${journeyRoute.totalDurationMinutes} min · ${journeyRoute.totalInterchanges} change${if (journeyRoute.totalInterchanges == 1) "" else "s"}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        val (srcLabel, srcColor) = when (journeyRoute.source) {
                            RouteSource.TFL_API -> "Live" to StatusGood
                            RouteSource.CACHE -> "Cached" to StatusMinor
                            RouteSource.LOCAL_DIJKSTRA -> "Offline" to StatusSevere
                        }
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                            color = srcColor.copy(alpha = 0.12f),
                        ) {
                            Text(
                                srcLabel,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = srcColor,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
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
                    onClick = { onNavigateToRoute(station.id, null, null, null) },
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
                                text = uiState.nearestStationDistanceKm?.let { "Nearest station · ${"%.1f".format(it)} km · Tap to plan route" } ?: "Nearest station · Tap to plan route",
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Filled.DirectionsSubway, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showLineFilters,
            // 104.dp matches the header offset used by the journey/nearest-station
            // cards above so the filter row lines up flush beneath the search bar
            // rather than overlapping it (old value Spacing.xxxl = 32.dp collided).
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 104.dp),
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
                            onClick = { viewModel.setSelectedLineFilter(null) },
                            label = { Text("All") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    }
                    item {
                        FilterChip(
                            selected = showDisruptedOnly,
                            onClick = { showDisruptedOnly = !showDisruptedOnly },
                            label = { Text("⚠ Disrupted") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusSevere.copy(alpha = 0.15f),
                                selectedLabelColor = StatusSevere,
                            ),
                        )
                    }
                    item {
                        FilterChip(
                            selected = showStepFreeOnly,
                            onClick = { showStepFreeOnly = !showStepFreeOnly },
                            label = { Text("♿ Step-free") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusGood.copy(alpha = 0.15f),
                                selectedLabelColor = StatusGood,
                            ),
                        )
                    }
                    // Zone filters — tap again to clear.
                    items(listOf("1", "2", "3", "4", "5", "6")) { z ->
                        val label = if (z == "6") "Z6+" else "Z$z"
                        FilterChip(
                            selected = selectedZone == z,
                            onClick = { selectedZone = if (selectedZone == z) null else z },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                            ),
                        )
                    }
                    items(lines) { line ->
                        FilterChip(
                            selected = selectedLineFilter == line.id,
                            onClick = {
                                viewModel.setSelectedLineFilter(
                                    if (selectedLineFilter == line.id) null else line.id
                                )
                            },
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

        // ── Live route-progress bottom card ───────────────────────────────
        // Shows active journey status, ETA clock, current leg, and a
        // dismissible × button. Tapping anywhere expands the leg breakdown.
        var progressCardExpanded by remember { mutableStateOf(false) }
        uiState.journeyRoute?.let { route ->
            val fraction = progressFraction
            val nowMs = journeyTickMs
            // Identify the "current leg" by finding the first leg that hasn't
            // finished yet. Falls back to the last leg if the journey is over.
            val currentLegIndex = remember(fraction, nowMs, route) {
                val idx = route.legs.indexOfFirst { it.scheduledArrivalEpochMs > nowMs }
                if (idx < 0) route.legs.size - 1 else idx
            }
            val currentLeg = route.legs.getOrNull(currentLegIndex)
            val nextLeg = route.legs.getOrNull(currentLegIndex + 1)
            // Real-wall-clock ETA — prefer the TfL-scheduled arrival; fall back
            // to now + remaining duration when we don't have scheduled times.
            val etaLabel: String? = remember(route, fraction, nowMs) {
                val arrMs = when {
                    route.scheduledArrivalEpochMs > 0L -> route.scheduledArrivalEpochMs
                    else -> nowMs + route.totalDurationMinutes * 60_000L
                }
                val cal = Calendar.getInstance().apply { timeInMillis = arrMs }
                "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
            }
            val title = when {
                fraction == null -> "${route.totalDurationMinutes} min journey"
                fraction <= 0f -> {
                    val minsToDep = ((route.scheduledDepartureEpochMs - nowMs) / 60_000L).toInt().coerceAtLeast(0)
                    "Leave in $minsToDep min"
                }
                fraction >= 1f -> "Arrived 🎉"
                else -> {
                    val remMin = ((route.scheduledArrivalEpochMs - nowMs) / 60_000L).toInt().coerceAtLeast(0)
                    "$remMin min to go · ${(fraction * 100).toInt()}%"
                }
            }
            val subtitle = when {
                fraction == null -> "${route.legs.size} step${if (route.legs.size != 1) "s" else ""} · plan ready"
                fraction >= 1f -> "${route.toStation.name} · journey complete"
                currentLeg == null -> "Heading to ${route.toStation.name}"
                else -> when (currentLeg.mode) {
                    TransportMode.WALKING -> "Walk to ${currentLeg.toStation.name}"
                    TransportMode.BUS -> "Bus to ${currentLeg.toStation.name}"
                    TransportMode.TUBE -> "${currentLeg.line.name} line → ${currentLeg.toStation.name}"
                }
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(start = Spacing.screenHorizontal, bottom = Spacing.lg, end = 90.dp)
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        progressCardExpanded = !progressCardExpanded
                    },
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 10.dp,
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(10.dp).clip(CircleShape)
                                .background(
                                    when {
                                        fraction == null || fraction <= 0f -> StatusMinor
                                        fraction >= 1f -> StatusGood
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            if (etaLabel != null) {
                                Text(
                                    "Arriving at $etaLabel",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        // Expand/collapse chevron
                        Icon(
                            if (progressCardExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            if (progressCardExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                        // Dismiss — tears down the active journey and returns
                        // the map to free-roam mode.
                        Surface(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.loadJourney(null, null)
                                progressCardExpanded = false
                            },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(start = 6.dp),
                        ) {
                            Box(modifier = Modifier.padding(6.dp), contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.Close,
                                    "Stop navigation",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    // Thin progress bar
                    if (fraction != null && fraction in 0f..1f) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                    // Expandable leg breakdown — appears when user taps the card.
                    AnimatedVisibility(visible = progressCardExpanded) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)))
                            Spacer(modifier = Modifier.height(8.dp))
                            route.legs.forEachIndexed { idx, leg ->
                                val isCurrent = idx == currentLegIndex && fraction != null && fraction in 0f..1f
                                val isDone = fraction != null && idx < currentLegIndex
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier.size(18.dp).clip(CircleShape).background(
                                            when {
                                                isDone -> StatusGood.copy(alpha = 0.2f)
                                                isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            when (leg.mode) {
                                                TransportMode.WALKING -> Icons.Filled.DirectionsWalk
                                                TransportMode.BUS -> Icons.Filled.DirectionsBus
                                                TransportMode.TUBE -> Icons.Filled.DirectionsSubway
                                            },
                                            null,
                                            modifier = Modifier.size(12.dp),
                                            tint = when {
                                                isDone -> StatusGood
                                                isCurrent -> MaterialTheme.colorScheme.primary
                                                leg.mode == TransportMode.TUBE -> leg.line.color
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            when (leg.mode) {
                                                TransportMode.WALKING -> "Walk to ${leg.toStation.name}"
                                                TransportMode.BUS -> "Bus to ${leg.toStation.name}"
                                                TransportMode.TUBE -> "${leg.line.name} · ${leg.fromStation.name} → ${leg.toStation.name}"
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant
                                            else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        val legTime = if (leg.scheduledDepartureEpochMs > 0L) {
                                            val c = Calendar.getInstance().apply { timeInMillis = leg.scheduledDepartureEpochMs }
                                            "%02d:%02d · ${leg.durationMinutes} min".format(
                                                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)
                                            )
                                        } else "${leg.durationMinutes} min"
                                        Text(
                                            legTime,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp,
                                        )
                                    }
                                    if (isCurrent) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        ) {
                                            Text(
                                                "NOW",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                            )
                                        }
                                    } else if (isDone) {
                                        Icon(
                                            Icons.Filled.CheckCircle,
                                            null,
                                            tint = StatusGood,
                                            modifier = Modifier.size(14.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Near-destination banner ───────────────────────────────────────
        // Celebrates when the user's GPS is within ~500 m of the journey's
        // final station. Uses haversine against the already-active journey
        // route so we don't spam the banner when idle.
        uiState.journeyRoute?.let { route ->
            val userLat = uiState.userLat
            val userLng = uiState.userLng
            if (userLat != null && userLng != null && progressFraction != null && progressFraction in 0f..1f) {
                val distKm = haversineKm(userLat, userLng, route.toStation.latitude, route.toStation.longitude)
                if (distKm < 0.5) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(top = 80.dp, start = 16.dp, end = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = StatusGood,
                        shadowElevation = 8.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle, null,
                                tint = Color.White, modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Almost there!",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                                Text(
                                    "${(distKm * 1000).toInt()} m to ${route.toStation.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                )
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(end = Spacing.screenHorizontal, bottom = if (selectedStation != null) 300.dp else Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            SmallFloatingActionButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showMapStylePicker = !showMapStylePicker
                },
                containerColor = MaterialTheme.colorScheme.surface,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            ) {
                Icon(mapStyle.icon, "Map style", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }

            // Cycling re-centre FAB —
            //   Tap 1 → centre on user
            //   Tap 2 → fit the active route (falls through to #3 if no route)
            //   Tap 3 → fit EVERYTHING (user + route) if both exist
            SmallFloatingActionButton(
                onClick = {
                    val route = uiState.journeyRoute
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val userLat = uiState.userLat; val userLng = uiState.userLng
                    val nextMode = (recenterMode + 1) % 3
                    recenterMode = nextMode
                    cameraScope.launch {
                        try {
                            when (nextMode) {
                                0 -> {
                                    if (userLat != null && userLng != null) {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(LatLng(userLat, userLng), 15f),
                                            600,
                                        )
                                    } else cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LONDON_CENTER, DEFAULT_ZOOM))
                                }
                                1 -> {
                                    if (route != null && routeFlatPath.size >= 2) {
                                        val b = LatLngBounds.Builder()
                                        routeFlatPath.forEach(b::include)
                                        cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(b.build(), 160), 800)
                                    } else if (userLat != null && userLng != null) {
                                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(userLat, userLng), 15f), 600)
                                    }
                                }
                                2 -> {
                                    val b = LatLngBounds.Builder()
                                    var has = false
                                    if (route != null) routeFlatPath.forEach { b.include(it); has = true }
                                    if (userLat != null && userLng != null) { b.include(LatLng(userLat, userLng)); has = true }
                                    if (has) cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(b.build(), 180), 900)
                                }
                            }
                        } catch (_: Exception) { /* swallow rare camera exceptions */ }
                    }
                },
                containerColor = when (recenterMode) {
                    1 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    2 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    else -> MaterialTheme.colorScheme.surface
                },
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            ) {
                Icon(
                    when (recenterMode) {
                        1 -> Icons.Filled.NearMe
                        2 -> Icons.Filled.Layers
                        else -> Icons.Filled.MyLocation
                    },
                    when (recenterMode) {
                        1 -> "Fit route"
                        2 -> "Fit all"
                        else -> "My location"
                    },
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Phase D: follow-mode toggle. When active, camera re-centers on user each time
            // their GPS moves.
            SmallFloatingActionButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    followMode = !followMode
                },
                containerColor = if (followMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            ) {
                Icon(
                    Icons.Filled.NearMe,
                    if (followMode) "Following active" else "Follow me",
                    tint = if (followMode) Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Traffic layer toggle — overlays live Google traffic colours.
            SmallFloatingActionButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    trafficEnabled = !trafficEnabled
                },
                containerColor = if (trafficEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            ) {
                Icon(
                    Icons.Filled.DirectionsBus,
                    if (trafficEnabled) "Traffic on" else "Traffic off",
                    tint = if (trafficEnabled) Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }

            // 3D tilt toggle — slants the camera to a 55° perspective for the
            // dramatic Google-Maps-3D look.
            SmallFloatingActionButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    tilt3dEnabled = !tilt3dEnabled
                },
                containerColor = if (tilt3dEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            ) {
                Icon(
                    Icons.Filled.Terrain,
                    if (tilt3dEnabled) "3D on" else "3D off",
                    tint = if (tilt3dEnabled) Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
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
                            onClick = {
                                viewModel.setMapStyle(style.name)
                                showMapStylePicker = false
                            },
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

        // Network Status Strip + station badge — stacked in a column to prevent overlap.
        // When an active journey is showing, lift the column by ~96dp so it sits ABOVE
        // the route-progress card (which lives at BottomStart) and add end-padding so
        // the station-count pill never collides with the FAB column at BottomEnd.
        val bottomStartLift = if (uiState.journeyRoute != null) 96.dp else 12.dp
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 96.dp, bottom = bottomStartLift),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AnimatedVisibility(
                visible = uiState.lineStatuses.any { !it.isGoodService },
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
            ) {
                NetworkStatusStrip(
                    lineStatuses = uiState.lineStatuses,
                    focusedLineId = selectedLineFilter,
                )
            }
            AnimatedVisibility(
                visible = uiState.isLoadingJourney || uiState.journeyErrorMessage != null,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (uiState.journeyErrorMessage != null) StatusSevere.copy(alpha = 0.92f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
                    shadowElevation = 8.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (uiState.isLoadingJourney) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Icon(Icons.Filled.Info, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.isLoadingJourney) "Loading route overlay…" else (uiState.journeyErrorMessage ?: ""),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
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
                // 160.dp places the banner under the search header + filters/cards
                // row rather than colliding with them.
                .padding(top = 160.dp, start = Spacing.screenHorizontal, end = Spacing.screenHorizontal),
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
                        if (uiState.isUsingCachedStatuses) Icons.Filled.Schedule else Icons.Filled.WifiOff,
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
                    Text(uiState.pinnedLocationLabel ?: "Map location", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    longPressLatLng?.let {
                        Text(
                            if (uiState.isResolvingPinnedLabel) "Resolving street or area…" else "${"%,.5f".format(it.latitude)}, ${"%,.5f".format(it.longitude)}",
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
                                    onNavigateToRoute(
                                        null,
                                        uiState.pinnedLocationLabel ?: "Pinned location",
                                        lp.latitude,
                                        lp.longitude,
                                    )
                                }
                            },
                        ) {
                            Row(modifier = Modifier.padding(11.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.DirectionsSubway, null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Route to here", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            onClick = {
                                longPressLatLng = null
                                viewModel.clearPinnedLocation()
                            },
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
                    arrivalsUpdatedAt = uiState.stationArrivals?.lastUpdated ?: 0L,
                    onClose = {
                        selectedStation = null
                        viewModel.selectStation(null)
                    },
                    onViewDetails = { onStationClick(station.id) },
                    onPlanRoute = { onNavigateToRoute(station.id, null, null, null) },
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
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(stationLines.firstOrNull()?.color?.copy(alpha = 0.12f) ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                        ls.statusSeverity in 1..6 -> StatusSevere
                        ls.statusSeverity in 7..9 -> StatusMinor
                        else -> StatusGood
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
                                Icon(Icons.Filled.WifiTethering, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
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
                        val freshnessColor = if (arrivals.isCached) StatusMinor else StatusGood
                        val freshnessPrefix = if (arrivals.isCached) "Cached" else "Live"
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(freshnessColor))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                freshnessLabel?.let { "$freshnessPrefix · $it" } ?: freshnessPrefix,
                                style = MaterialTheme.typography.labelSmall,
                                color = freshnessColor,
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
                        Icon(Icons.Filled.DirectionsSubway, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
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
    val dotPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { this.color = android.graphics.Color.WHITE; style = android.graphics.Paint.Style.FILL }
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
    // Teardrop-style pin with coloured core, glowing aura, and bold letter —
    // matches Google Maps' destination-pin feel.
    val size = 72
    val cx = size / 2f
    val cy = 32f // pin body centre (pointer protrudes downward)
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    // Aura glow — premium, airy feel
    val auraPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = argbColor
        alpha = 70
        maskFilter = android.graphics.BlurMaskFilter(10f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    canvas.drawCircle(cx, cy, 24f, auraPaint)

    // Soft drop shadow
    val shadowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(80, 0, 0, 0)
        maskFilter = android.graphics.BlurMaskFilter(5f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    canvas.drawCircle(cx + 1.5f, cy + 3f, 21f, shadowPaint)

    // Pointer (small wedge descending from the circle)
    val pointerPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = argbColor; style = android.graphics.Paint.Style.FILL
    }
    val pointerPath = android.graphics.Path().apply {
        moveTo(cx - 8f, cy + 15f)
        lineTo(cx + 8f, cy + 15f)
        lineTo(cx, cy + 32f)
        close()
    }
    canvas.drawPath(pointerPath, pointerPaint)

    // White outer ring halo
    val whiteRing = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE; style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, 22f, whiteRing)

    // Coloured inner disc
    val fillPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = argbColor; style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, 18.5f, fillPaint)

    // Subtle inner highlight arc for a glossy 3D feel
    val glossPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(55, 255, 255, 255); style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(cx - 5f, cy - 5f, 8f, glossPaint)

    // Letter label (A / B)
    val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE; textSize = 22f; isFakeBoldText = true
        textAlign = android.graphics.Paint.Align.CENTER
    }
    canvas.drawText(label, cx, cy + textPaint.textSize / 3f, textPaint)
    return bitmap
}

// ── Geometry helpers for chevron-flow + "you-are-here" ──────────────────
private fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
        Math.sin(dLng / 2) * Math.sin(dLng / 2)
    return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

/** Sample a point along a polyline at a given [0..1] fraction of total length. */
private fun sampleAlongPath(path: List<LatLng>, cumKm: FloatArray, frac: Float): LatLng {
    if (path.isEmpty()) return LONDON_CENTER
    if (path.size == 1) return path[0]
    val total = cumKm.last()
    if (total <= 0f) return path[0]
    val target = (frac.coerceIn(0f, 1f) * total)
    var i = 1
    while (i < cumKm.size && cumKm[i] < target) i++
    if (i >= cumKm.size) return path.last()
    val segLen = (cumKm[i] - cumKm[i - 1]).coerceAtLeast(0.0001f)
    val t = ((target - cumKm[i - 1]) / segLen).coerceIn(0f, 1f)
    val a = path[i - 1]; val b = path[i]
    return LatLng(
        a.latitude + t * (b.latitude - a.latitude),
        a.longitude + t * (b.longitude - a.longitude),
    )
}

private fun createChevronBitmap(argbColor: Int): android.graphics.Bitmap {
    // Small directional arrow — rendered at each sampled chevron position to
    // indicate direction of travel along the route.
    val size = 22
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val whitePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE; style = android.graphics.Paint.Style.STROKE; strokeWidth = 4f
        strokeCap = android.graphics.Paint.Cap.ROUND; strokeJoin = android.graphics.Paint.Join.ROUND
    }
    val fillPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = argbColor; style = android.graphics.Paint.Style.STROKE; strokeWidth = 2.2f
        strokeCap = android.graphics.Paint.Cap.ROUND; strokeJoin = android.graphics.Paint.Join.ROUND
    }
    val path = android.graphics.Path().apply {
        moveTo(5f, 4f); lineTo(16f, 11f); lineTo(5f, 18f)
    }
    canvas.drawPath(path, whitePaint)
    canvas.drawPath(path, fillPaint)
    return bitmap
}

private fun createYouArePinBitmap(): android.graphics.Bitmap {
    // Pulsing-style blue dot with a crisp white ring — used to represent the
    // user's live progress along an active journey route.
    val size = 46; val cx = size / 2f
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val glow = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(33, 150, 243); alpha = 100
        maskFilter = android.graphics.BlurMaskFilter(7f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    canvas.drawCircle(cx, cx, cx - 6f, glow)
    val white = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE }
    canvas.drawCircle(cx, cx, cx - 6f, white)
    val blue = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(25, 118, 210) }
    canvas.drawCircle(cx, cx, cx - 11f, blue)
    val dot = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE }
    canvas.drawCircle(cx, cx, 4.5f, dot)
    return bitmap
}

private fun createTrainBitmap(color: Color, shortLabel: String = "", isEstimated: Boolean = false): android.graphics.Bitmap {
    // Elite, modern tube-train marker — sleek capsule with a TfL-style
    // centre bar, LED aura, gradient gloss and a tiny front windshield.
    // Dropped the toy-train wheels / rail — this is a map marker, not an
    // illustration.
    val w = 96; val h = 62
    val bitmap = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    val bodyRect = android.graphics.RectF(6f, 10f, 90f, 44f)
    val cornerR = 17f

    // 1 — Aura glow (line-coloured, airy halo)
    val auraPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color.toArgb()
        alpha = if (isEstimated) 70 else 130
        maskFilter = android.graphics.BlurMaskFilter(12f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    canvas.drawRoundRect(bodyRect, cornerR, cornerR, auraPaint)

    // 2 — Soft drop shadow
    val shadowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.argb(70, 0, 0, 0)
        maskFilter = android.graphics.BlurMaskFilter(5f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    val shadowRect = android.graphics.RectF(bodyRect).apply { offset(1f, 3f) }
    canvas.drawRoundRect(shadowRect, cornerR, cornerR, shadowPaint)

    // 3 — Main body with a subtle vertical gradient (lighter top, richer bottom)
    val bodyArgb = color.toArgb()
    val darker = android.graphics.Color.rgb(
        (android.graphics.Color.red(bodyArgb) * 0.78f).toInt().coerceIn(0, 255),
        (android.graphics.Color.green(bodyArgb) * 0.78f).toInt().coerceIn(0, 255),
        (android.graphics.Color.blue(bodyArgb) * 0.78f).toInt().coerceIn(0, 255),
    )
    val bodyPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        shader = android.graphics.LinearGradient(
            0f, bodyRect.top, 0f, bodyRect.bottom,
            bodyArgb, darker, android.graphics.Shader.TileMode.CLAMP,
        )
    }
    canvas.drawRoundRect(bodyRect, cornerR, cornerR, bodyPaint)

    // 4 — Crisp white stroke (lemon-tinted for estimated positions)
    val outlinePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        this.color = if (isEstimated) android.graphics.Color.argb(230, 255, 244, 180) else android.graphics.Color.WHITE
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = if (isEstimated) 2.2f else 2.8f
    }
    canvas.drawRoundRect(bodyRect, cornerR, cornerR, outlinePaint)

    // 5 — Top gloss highlight (thin white ellipse for 3D sheen)
    val glossPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.argb(65, 255, 255, 255)
    }
    canvas.drawRoundRect(
        android.graphics.RectF(bodyRect.left + 6f, bodyRect.top + 3f, bodyRect.right - 6f, bodyRect.top + 12f),
        6f, 6f, glossPaint,
    )

    // 6 — Centre label bar: short line code on a dark roundel
    if (shortLabel.isNotEmpty()) {
        val labelBg = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            this.color = android.graphics.Color.argb(170, 0, 0, 0)
        }
        canvas.drawRoundRect(android.graphics.RectF(18f, 18f, 68f, 36f), 9f, 9f, labelBg)
        val labelPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            this.color = if (isEstimated) android.graphics.Color.rgb(255, 244, 180) else android.graphics.Color.WHITE
            textSize = 13f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        canvas.drawText(shortLabel, 43f, 32f, labelPaint)
    }

    // 7 — Front windshield (right side) — bright cyan tint
    val windshield = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.argb(210, 180, 230, 255)
    }
    canvas.drawRoundRect(android.graphics.RectF(74f, 17f, 86f, 37f), 5f, 5f, windshield)

    // 8 — LED headlight dot on the leading edge
    val ledPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.argb(255, 255, 245, 200)
    }
    canvas.drawCircle(88f, 27f, 2.2f, ledPaint)

    // 9 — EST flag for stale / interpolated positions
    if (isEstimated) {
        val flagBg = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            this.color = android.graphics.Color.argb(220, 120, 90, 0)
        }
        canvas.drawRoundRect(android.graphics.RectF(34f, 46f, 62f, 58f), 4f, 4f, flagBg)
        val estPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            this.color = android.graphics.Color.rgb(255, 244, 180)
            textSize = 9.5f; isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.drawText("EST", 48f, 55f, estPaint)
    }
    return bitmap
}

@Composable
private fun NetworkStatusStrip(lineStatuses: List<LineStatus>, focusedLineId: String? = null) {
    val disrupted = lineStatuses
        .filter { !it.isGoodService }
        .let { statuses ->
            focusedLineId?.let { lineId ->
                val focused = statuses.filter { it.lineId == lineId }
                if (focused.isNotEmpty()) focused + statuses.filter { it.lineId != lineId }
                else statuses
            } ?: statuses
        }
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
