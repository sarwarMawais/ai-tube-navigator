package com.londontubeai.navigator.ui.screens.home

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.londontubeai.navigator.data.model.AiInsight
import com.londontubeai.navigator.data.model.InsightType
import com.londontubeai.navigator.data.model.UserImpact
import com.londontubeai.navigator.data.model.LineStatus
import com.londontubeai.navigator.data.model.TubeData
import com.londontubeai.navigator.ui.components.TubeLineStatusCard
import com.londontubeai.navigator.ui.theme.StatusGood
import com.londontubeai.navigator.ui.theme.StatusMinor
import com.londontubeai.navigator.ui.theme.StatusSevere
import com.londontubeai.navigator.ui.theme.TubeAccent
import com.londontubeai.navigator.ui.theme.TubePrimary
import com.londontubeai.navigator.ui.theme.TubePrimaryLight
import com.londontubeai.navigator.ui.theme.brandGradient
import com.londontubeai.navigator.ui.theme.TubeSecondary
import com.londontubeai.navigator.ui.theme.Spacing
import com.londontubeai.navigator.ui.theme.cardPadding
import com.londontubeai.navigator.ui.theme.horizontalScreenPadding
import com.londontubeai.navigator.ui.screens.home.WeatherInfo
import com.londontubeai.navigator.ui.screens.home.NearbyArrival
import com.londontubeai.navigator.ui.screens.home.ImpactLevel
import com.londontubeai.navigator.utils.rememberPermissionsHandler
import com.londontubeai.navigator.utils.PermissionsState
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRoute: (String?, String?, Double?, Double?) -> Unit,
    onNavigateToStatus: () -> Unit,
    onNavigateToStations: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToNearby: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recentJourneys by viewModel.recentJourneys.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    // Gate the auto-refresh loop on this screen being attached.
    DisposableEffect(Unit) {
        viewModel.setScreenActive(true)
        onDispose { viewModel.setScreenActive(false) }
    }
    var selectedLineStatus by remember { mutableStateOf<LineStatus?>(null) }
    var selectedInsight by remember { mutableStateOf<AiInsight?>(null) }
    val openBlankRoute = remember(onNavigateToRoute) {
        { onNavigateToRoute(null, null, null, null) }
    }
    val openCommuteRoute = remember(onNavigateToRoute, uiState.commuteDestinationId, uiState.commuteDestinationName) {
        {
            onNavigateToRoute(
                uiState.commuteDestinationId,
                uiState.commuteDestinationName,
                null,
                null,
            )
        }
    }
    // Wrap commute open with a haptic so taps feel reactive
    val openCommuteRouteHaptic: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        openCommuteRoute()
    }
    val primaryAction = remember(
        uiState.severeDisruptionCount,
        uiState.crowdLevel,
        uiState.leaveNowAssistant,
        uiState.commuteDestinationId,
    ) {
        when {
            uiState.severeDisruptionCount > 0 -> HomePrimaryActionType.STATUS
            uiState.crowdLevel.percentage >= 0.8f &&
                uiState.leaveNowAssistant?.status !in setOf(
                    LeaveNowStatus.SOON,
                    LeaveNowStatus.NOW,
                    LeaveNowStatus.LATE,
                ) -> HomePrimaryActionType.MAP
            else -> HomePrimaryActionType.ROUTE
        }
    }
    
    var permissionsState by remember { mutableStateOf(PermissionsState()) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    val permissionsHandler = rememberPermissionsHandler { newState ->
        permissionsState = newState
        viewModel.onPermissionsChanged(newState)
    }
    
    LaunchedEffect(Unit) {
        val currentState = permissionsHandler.getPermissionsState()
        permissionsState = currentState
        // Only nudge the ViewModel when permission was *granted out-of-band*
        // (e.g. via system Settings while the app was backgrounded). Never call
        // it with `granted=false` on attach — that would wipe the in-flight
        // nearby load that ViewModel.init already kicked off, which is the
        // root cause of the regression "Nearby stations not loading on Home".
        if (currentState.locationPermissionGranted) {
            viewModel.onPermissionsChanged(currentState)
        } else {
            delay(1000)
            showPermissionDialog = true
        }
    }
    
    if (showPermissionDialog) {
        PermissionRequestDialog(
            onDismiss = { showPermissionDialog = false },
            onRequestPermissions = {
                permissionsHandler.requestAllPermissions()
                showPermissionDialog = false
            }
        )
    }

    // Compute contextual banner state OUTSIDE the LazyColumn so we only
    // add an `item {}` when there is actually something to show — otherwise
    // LazyColumn renders an empty slot that creates dead space above the hero.
    val commuteLineIds = remember(uiState.homeStationId, uiState.workStationId) {
        val ids = mutableSetOf<String>()
        listOfNotNull(uiState.homeStationId, uiState.workStationId).forEach { stationId ->
            TubeData.getStationById(stationId)?.lineIds?.let { ids.addAll(it) }
        }
        ids
    }
    val commuteDisruption = uiState.lineStatuses.firstOrNull {
        !it.isGoodService && commuteLineIds.isNotEmpty() && it.lineId in commuteLineIds && it.statusSeverity <= 6
    }
    val weather = uiState.weatherInfo
    val showWeatherHint = weather != null && (weather.impactLevel == ImpactLevel.HIGH || weather.impactLevel == ImpactLevel.EXTREME)

    PullToRefreshBox(
        isRefreshing = uiState.isLoadingStatus,
        onRefresh = { viewModel.refreshHomeData() },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            // ── Top Contextual Banner ── surfaces commute disruption / weather
            // warnings above the hero. Only added when there's something to show.
            if (commuteDisruption != null) {
                item {
                    TopContextBanner(
                        color = StatusSevere,
                        icon = Icons.Filled.Warning,
                        title = "Your commute is affected",
                        detail = "${commuteDisruption.statusDescription} on ${commuteDisruption.lineName}",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateToStatus()
                        },
                    )
                }
            } else if (showWeatherHint && weather != null) {
                item {
                    val (title, detail) = weatherHintText(weather)
                    TopContextBanner(
                        color = StatusMinor,
                        icon = when (weather.icon) {
                            "09d", "09n", "10d", "10n" -> Icons.Filled.Opacity
                            "13d", "13n" -> Icons.Filled.AcUnit
                            "11d", "11n" -> Icons.Filled.FlashOn
                            else -> Icons.Filled.Cloud
                        },
                        title = title,
                        detail = detail,
                        onClick = null,
                    )
                }
            }

            // ── Premium Hero Header ────────────────────────────────
            item {
                HeroHeader(
                    goodServiceCount = uiState.lineStatuses.count { it.isGoodService },
                    totalLines = uiState.lineStatuses.size,
                    onRefresh = { viewModel.refreshHomeData() },
                    stationCount = uiState.networkStationCount,
                    lineCount = uiState.networkLineCount,
                    stepFreeCount = uiState.stepFreeCount,
                    serviceQualityScore = uiState.serviceQualityScore,
                    weatherInfo = uiState.weatherInfo,
                    commuteTimeEstimate = uiState.commuteTimeEstimate,
                    nearbyArrivals = uiState.nearbyStationArrivals,
                    nearbyStatusMessage = uiState.nearbyStatusMessage,
                    isNearbyUsingFallback = uiState.isNearbyUsingFallback,
                    isNearbyLoading = uiState.isNearbyLoading,
                    isOutsideLondon = uiState.isOutsideLondon,
                    hasLocationPermission = permissionsState.locationPermissionGranted,
                    onRequestPermission = { showPermissionDialog = true },
                    onRetryLocation = { viewModel.retryNearbyArrivals() },
                    onSeeAllNearby = onNavigateToNearby,
                )
            }

            if (uiState.leaveNowAssistant != null) {
                item {
                    LeaveNowAssistantCard(
                        assistant = uiState.leaveNowAssistant!!,
                        onOpenRoute = openCommuteRoute,
                    )
                }
            }

            // ── Quick Commute Card (daily commuters) ─────────────
            if (uiState.homeStationName != null && uiState.workStationName != null) {
                item {
                    QuickCommuteCard(
                        homeStation = uiState.homeStationName!!,
                        workStation = uiState.workStationName!!,
                        commuteTime = uiState.commuteTimeEstimate,
                        disruptionCount = uiState.lineStatuses.count { !it.isGoodService },
                        snapshot = uiState.commuterSnapshot,
                        healthScore = uiState.commuteHealthScore,
                        extraDelayMinutes = uiState.commuteExtraDelayMinutes,
                        onStartCommute = openCommuteRoute,
                        onOpenStatus = onNavigateToStatus,
                    )
                }
            }

            if (uiState.fallbackRoutes.isNotEmpty()) {
                item {
                    FallbackRoutesCard(
                        options = uiState.fallbackRoutes,
                        onOpenRoute = openCommuteRoute,
                    )
                }
            }

            // ── Welcome Tips (first-time visitors) ───────────────
            if (uiState.homeStationName == null && uiState.workStationName == null && recentJourneys.isEmpty()) {
                item {
                    WelcomeTipsCard(
                        visitorGuides = uiState.visitorLandmarks,
                        onExploreStations = onNavigateToStations,
                        onPlanJourney = openBlankRoute,
                        onOpenNearby = onNavigateToNearby,
                    )
                }
            }

            // ── Quick Actions Grid ──────────────────────────────
            item {
                QuickActions(
                    primaryAction = primaryAction,
                    commuteDestinationName = uiState.commuteDestinationName,
                    disruptionCount = uiState.severeDisruptionCount,
                    crowdLabel = uiState.crowdLevel.label,
                    onRouteClick = if (uiState.commuteDestinationId != null) openCommuteRoute else openBlankRoute,
                    onStatusClick = onNavigateToStatus,
                    onStationsClick = onNavigateToStations,
                    onMapClick = onNavigateToMap,
                )
            }

            // ── Rich Data Widgets ───────────────────────────────
            item {
                RichDataWidgets(
                    serviceQualityScore = uiState.serviceQualityScore,
                    totalLines = uiState.lineStatuses.size,
                    goodServiceCount = uiState.lineStatuses.count { it.isGoodService },
                    minorDelayCount = uiState.minorDelayCount,
                    severeDisruptionCount = uiState.severeDisruptionCount,
                    crowdLevel = uiState.crowdLevel,
                )
            }

            // ── Live Alerts Banner (disruptions only) ────────────
            if (uiState.lineStatuses.any { !it.isGoodService }) {
                item {
                    val disruptedLines = uiState.lineStatuses.filter { !it.isGoodService }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = StatusSevere.copy(alpha = 0.08f),
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToStatus() }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(StatusSevere.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = StatusSevere,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${disruptedLines.size} Line${if (disruptedLines.size > 1) "s" else ""} Disrupted",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusSevere,
                                )
                                Text(
                                    text = disruptedLines.joinToString(", ") { it.lineName },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = StatusSevere.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }

            // ── AI Insights Carousel ────────────────────────────
            if (uiState.aiInsights.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Smart Insights",
                        icon = Icons.Filled.Lightbulb,
                        badge = uiState.aiInsights.size.toString(),
                    )
                }
                item {
                    val lazyListState = rememberLazyListState()
                    LazyRow(
                        state = lazyListState,
                        modifier = Modifier.horizontalScreenPadding(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        contentPadding = PaddingValues(vertical = Spacing.sm),
                    ) {
                        items(uiState.aiInsights.take(5)) { insight ->
                            AiInsightCard(
                                insight = insight,
                                modifier = Modifier.widthIn(min = 280.dp, max = 320.dp),
                                onInsightClick = { clickedInsight ->
                                    Log.d("HomeScreen", "Insight clicked: ${clickedInsight.title}")
                                    selectedInsight = clickedInsight
                                },
                            )
                        }
                    }
                    // Page indicator dots
                    val insightCount = uiState.aiInsights.take(5).size
                    val currentPage = remember {
                        androidx.compose.runtime.derivedStateOf {
                            val layoutInfo = lazyListState.layoutInfo
                            val visibleItems = layoutInfo.visibleItemsInfo
                            if (visibleItems.isEmpty()) 0
                            else visibleItems.minByOrNull { kotlin.math.abs(it.offset) }?.index ?: 0
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        repeat(insightCount) { index ->
                            val isActive = currentPage.value == index
                            val dotSize by animateFloatAsState(
                                targetValue = if (isActive) 8f else 5f,
                                animationSpec = tween(200),
                                label = "dot"
                            )
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = Spacing.xs)
                                    .size(dotSize.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isActive) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                    ),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.sm))
                }
            }

            // ── Live Network Status ─────────────────────────────
            item {
                SectionHeader(
                    title = "Live Network",
                    icon = Icons.Filled.Wifi,
                    trailing = uiState.lastUpdated,
                    badge = uiState.lineStatuses.count { !it.isGoodService }.toString(),
                )
            }

            if (uiState.isLoadingStatus && uiState.lineStatuses.isEmpty()) {
                item { StatusLoadingShimmer() }
            } else if (uiState.statusError != null && uiState.lineStatuses.isEmpty()) {
                item { 
                    StatusErrorCard(
                        error = uiState.statusError ?: "An error occurred.",
                        onRetry = { viewModel.refreshHomeData() }
                    ) 
                }
            } else {
                itemsIndexed(uiState.lineStatuses, key = { _, status -> status.lineId }) { _, status ->
                    // Avoid re-triggering entry animations on every auto-refresh \u2014 a stable key
                    // makes Lazy column reuse the same slot so contents fade in only once.
                    TubeLineStatusCard(
                        status = status,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        onClick = { selectedLineStatus = status },
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }

        selectedLineStatus?.let { lineStatus ->
            ModalBottomSheet(
                onDismissRequest = { selectedLineStatus = null },
            ) {
                LiveNetworkDetailSheet(
                    status = lineStatus,
                    onOpenStatus = {
                        selectedLineStatus = null
                        onNavigateToStatus()
                    },
                    onDismiss = { selectedLineStatus = null },
                )
            }
        }

        selectedInsight?.let { insight ->
             ModalBottomSheet(
                 onDismissRequest = { selectedInsight = null },
             ) {
                 InsightDetailSheet(
                     insight = insight,
                     onDismiss = { selectedInsight = null },
                     onTakeAction = {
                         selectedInsight = null
                         when {
                             insight.actionLabel?.contains("status", ignoreCase = true) == true -> onNavigateToStatus()
                             insight.actionLabel?.contains("crowd map", ignoreCase = true) == true -> onNavigateToMap()
                             insight.actionLabel?.contains("nearby", ignoreCase = true) == true -> onNavigateToNearby()
                             insight.actionLabel?.contains("station", ignoreCase = true) == true -> onNavigateToStations()
                             insight.type in setOf(
                                 InsightType.DELAY_WARNING,
                                 InsightType.DISRUPTION_IMPACT,
                                 InsightType.EVENT_ALERT,
                             ) -> onNavigateToStatus()
                             insight.type == InsightType.CROWD_ALERT -> onNavigateToMap()
                             insight.type in setOf(
                                 InsightType.CARRIAGE_TIP,
                                 InsightType.PERSONAL_ROUTE,
                                 InsightType.TIME_SAVING,
                                 InsightType.TIME_PREFERENCE,
                                 InsightType.SAVINGS_OPPORTUNITY,
                             ) -> if (uiState.commuteDestinationId != null) openCommuteRoute() else openBlankRoute()
                             else -> onNavigateToStations()
                         }
                     },
                 )
             }
         }
     }
 }

 private enum class HomePrimaryActionType {
     ROUTE,
     STATUS,
     MAP,
 }

 private data class HomeQuickActionSpec(
     val title: String,
     val subtitle: String,
     val icon: ImageVector,
     val color: Color,
     val onClick: () -> Unit,
 )

 // ═══════════════════════════════════════════════════════════════
 // HERO HEADER — Glassmorphism-inspired with health ring + live clock
 // ═══════════════════════════════════════════════════════════════

 @Composable
 private fun HeroHeader(
    goodServiceCount: Int,
    totalLines: Int,
    onRefresh: () -> Unit,
    stationCount: Int = 0,
    lineCount: Int = 0,
    stepFreeCount: Int = 0,
    serviceQualityScore: Float = 0f,
    weatherInfo: WeatherInfo? = null,
    commuteTimeEstimate: String? = null,
    nearbyArrivals: List<NearbyArrival> = emptyList(),
    nearbyStatusMessage: String = "Location permission required",
    isNearbyUsingFallback: Boolean = false,
    isNearbyLoading: Boolean = false,
    isOutsideLondon: Boolean = false,
    hasLocationPermission: Boolean = false,
    onRequestPermission: () -> Unit = {},
    onRetryLocation: () -> Unit = {},
    onSeeAllNearby: () -> Unit = {},
) {
    val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.UK)
    val today = dateFormat.format(Date())
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Good\nMorning"
        in 12..16 -> "Good\nAfternoon"
        else -> "Good\nEvening"
    }

    // Live clock
    var timeText by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            timeText = SimpleDateFormat("HH:mm", Locale.UK).format(Date())
            delay(30_000L)
        }
    }

    // Animated health percentage
    val healthFraction = if (totalLines > 0) goodServiceCount.toFloat() / totalLines else 0f
    val animatedHealth by animateFloatAsState(
        targetValue = healthFraction,
        animationSpec = tween(durationMillis = 1200),
        label = "health",
    )

    val heroGradient = brandGradient()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(heroGradient.vertical()),
    ) {
        // Subtle decorative circles
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.03f),
                radius = 200.dp.toPx(),
                center = Offset(size.width * 0.85f, size.height * 0.2f),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.02f),
                radius = 150.dp.toPx(),
                center = Offset(size.width * 0.1f, size.height * 0.9f),
            )
        }

        Column(
            modifier = Modifier.padding(top = 56.dp, bottom = 20.dp, start = 24.dp, end = 24.dp),
        ) {
            // Top bar: date/greeting + weather + clock + refresh
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = today,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 0.5.sp,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Weather widget
                    weatherInfo?.let { weather ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.1f),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = when (weather.icon) {
                                        "01d", "01n" -> Icons.Filled.WbSunny
                                        "02d", "02n" -> Icons.Filled.WbCloudy
                                        "03d", "03n" -> Icons.Filled.Cloud
                                        "04d", "04n" -> Icons.Filled.Cloud
                                        "09d", "09n" -> Icons.Filled.Opacity
                                        "10d", "10n" -> Icons.Filled.Opacity
                                        "11d", "11n" -> Icons.Filled.FlashOn
                                        "13d", "13n" -> Icons.Filled.AcUnit
                                        "50d", "50n" -> Icons.Filled.Cloud
                                        else -> Icons.Filled.WbSunny
                                    },
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${weather.temperature}°",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    
                    // Live clock pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.1f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(StatusGood),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = timeText,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Network Health Card (glassmorphism) ──────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.12f),
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Enhanced health ring with quality score
                        Box(
                            modifier = Modifier.size(72.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Background ring
                                drawArc(
                                    color = Color.White.copy(alpha = 0.1f),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
                                )
                                // Animated health ring
                                val ringColor = when {
                                    animatedHealth >= 0.9f -> StatusGood
                                    animatedHealth >= 0.6f -> StatusMinor
                                    else -> StatusSevere
                                }
                                drawArc(
                                    color = ringColor,
                                    startAngle = -90f,
                                    sweepAngle = animatedHealth * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${(animatedHealth * 100).toInt()}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                )
                                Text(
                                    text = "Quality",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 8.sp,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(18.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (totalLines == 0) "Connecting..."
                                else if (goodServiceCount == totalLines) "All Lines Running"
                                else "$goodServiceCount of $totalLines Good Service",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (totalLines > 0 && goodServiceCount < totalLines)
                                    "${totalLines - goodServiceCount} line${if (totalLines - goodServiceCount > 1) "s" else ""} disrupted"
                                else "No disruptions reported",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Live indicator with trend
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (totalLines > 0) StatusGood else StatusMinor),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (totalLines > 0) "Live from TfL" else "Offline mode",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Medium,
                                )
                                if (serviceQualityScore > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = if (serviceQualityScore >= 80) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = null,
                                        tint = if (serviceQualityScore >= 80) StatusGood else StatusMinor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Commute estimate and weather impact
                    if (commuteTimeEstimate != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            // Commute time
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.06f),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AccessTime,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "Commute",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 9.sp,
                                        )
                                        Text(
                                            text = commuteTimeEstimate,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                        )
                                    }
                                }
                            }

                            // Weather impact
                            weatherInfo?.let { weather ->
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    color = when (weather.impactLevel) {
                                        ImpactLevel.LOW -> StatusGood.copy(alpha = 0.1f)
                                        ImpactLevel.MEDIUM -> StatusMinor.copy(alpha = 0.1f)
                                        ImpactLevel.HIGH -> StatusSevere.copy(alpha = 0.1f)
                                        ImpactLevel.EXTREME -> StatusSevere.copy(alpha = 0.2f)
                                    },
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Cloud,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = "Impact",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontSize = 9.sp,
                                            )
                                            Text(
                                                text = weather.impactLevel.name.lowercase().replaceFirstChar { it.uppercase() },
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Nearby Arrivals Widget ───────────────────────
            Spacer(modifier = Modifier.height(14.dp))

            val nearbyStatusColor = when {
                isNearbyLoading -> TubeAccent
                !hasLocationPermission -> StatusMinor
                isNearbyUsingFallback -> StatusMinor
                nearbyArrivals.isNotEmpty() -> StatusGood
                else -> StatusSevere
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = nearbyStatusColor.copy(alpha = 0.16f),
            ) {
                Text(
                    text = nearbyStatusMessage,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            
            if (!hasLocationPermission) {
                LocationPermissionPrompt(onRequestPermission = onRequestPermission)
            } else if (isOutsideLondon) {
                OutsideLondonCard()
            } else if (isNearbyLoading) {
                NearbyLoadingCard()
            } else if (nearbyArrivals.isNotEmpty()) {
                NearbyArrivalsWidget(
                    arrivals = nearbyArrivals,
                    onRefresh = onRetryLocation,
                    onSeeAllNearby = onSeeAllNearby,
                )
            } else {
                LocationErrorPrompt(onRetry = onRetryLocation)
            }
        }
    }
}

@Composable
private fun LocationPermissionPrompt(onRequestPermission: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.12f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(TubeAccent.copy(alpha = 0.3f), TubePrimary.copy(alpha = 0.3f))
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Enable Location for Nearby Arrivals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "Get real-time arrivals for stations near you",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onRequestPermission),
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.15f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enable Location",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationErrorPrompt(onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.15f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = StatusSevere,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Location Services Issue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "Unable to get nearby arrivals. Check GPS or try again.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onRetry),
                shape = RoundedCornerShape(14.dp),
                color = StatusSevere.copy(alpha = 0.2f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = StatusSevere,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Retry",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = StatusSevere,
                    )
                }
            }
        }
    }
}

@Composable
private fun OutsideLondonCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.12f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(StatusMinor.copy(alpha = 0.3f), TubeAccent.copy(alpha = 0.3f))
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Explore,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "You're Not in London",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Nearby station arrivals are only available within the London transport network. You can still browse all stations and check live line status.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
            )
        }
    }
}

@Composable
private fun NearbyLoadingCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.12f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Finding stations near you…",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun NearbyArrivalsWidget(
    arrivals: List<NearbyArrival>,
    onRefresh: () -> Unit = {},
    onArrivalClick: ((NearbyArrival) -> Unit)? = null,
    onSeeAllNearby: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.12f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Nearby Arrivals",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = arrivals.first().stationName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // See All button
                    Surface(
                        onClick = onSeeAllNearby,
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.15f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "See All",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.9f),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "See All",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(12.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        modifier = Modifier
                            .size(28.dp)
                            .clickable(onClick = onRefresh),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.1f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            arrivals.take(4).forEachIndexed { index, arrival ->
                if (index > 0) Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (onArrivalClick != null) Modifier.clickable { onArrivalClick(arrival) }
                            else Modifier
                        ),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.04f),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Line color indicator strip
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(36.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(arrival.lineColor ?: Color.White.copy(alpha = 0.3f))
                        )
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        // Time indicator
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                arrival.minutesUntil <= 2 -> StatusGood.copy(alpha = 0.2f)
                                arrival.minutesUntil <= 5 -> StatusMinor.copy(alpha = 0.2f)
                                else -> Color.White.copy(alpha = 0.1f)
                            },
                        ) {
                            Text(
                                text = if (arrival.minutesUntil <= 0) "Due" else "${arrival.minutesUntil} min",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    arrival.minutesUntil <= 2 -> StatusGood
                                    arrival.minutesUntil <= 5 -> StatusMinor
                                    else -> Color.White
                                },
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = arrival.lineName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = arrival.destination,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false),
                                )
                                // Distance badge
                                arrival.distanceKm?.let { dist ->
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (dist < 1.0) "${(dist * 1000).toInt()}m" else "${"%.1f".format(dist)}km",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 9.sp,
                                    )
                                }
                            }
                        }
                        
                        if (arrival.platform != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White.copy(alpha = 0.1f),
                            ) {
                                Text(
                                    text = "Plat ${arrival.platform}",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f),
                                )
                            }
                        }
                        
                        // Chevron for clickable
                        if (onArrivalClick != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPill(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.06f),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// RICH DATA WIDGETS — Premium analytics cards
// ═══════════════════════════════════════════════════════════════

@Composable
private fun RichDataWidgets(
    serviceQualityScore: Float,
    totalLines: Int,
    goodServiceCount: Int,
    minorDelayCount: Int,
    severeDisruptionCount: Int,
    crowdLevel: CrowdLevel,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Service Performance Widget
        ServicePerformanceWidget(
            qualityScore = serviceQualityScore,
            totalLines = totalLines,
            goodServiceCount = goodServiceCount,
            minorDelayCount = minorDelayCount,
            severeDisruptionCount = severeDisruptionCount,
        )
        
        // Crowd Levels Widget
        CrowdLevelsWidget(crowdLevel = crowdLevel)
    }
}

@Composable
private fun ServicePerformanceWidget(
    qualityScore: Float,
    totalLines: Int,
    goodServiceCount: Int,
    minorDelayCount: Int,
    severeDisruptionCount: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Service Performance",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                
                // Quality score badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        qualityScore >= 90 -> StatusGood.copy(alpha = 0.1f)
                        qualityScore >= 70 -> StatusMinor.copy(alpha = 0.1f)
                        else -> StatusSevere.copy(alpha = 0.1f)
                    },
                ) {
                    Text(
                        text = "${qualityScore.toInt()}%",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            qualityScore >= 90 -> StatusGood
                            qualityScore >= 70 -> StatusMinor
                            else -> StatusSevere
                        },
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Performance bars — using real data from TfL API
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PerformanceBar(
                    label = "Good Service",
                    value = goodServiceCount,
                    total = totalLines,
                    color = StatusGood,
                )
                PerformanceBar(
                    label = "Minor Delays",
                    value = minorDelayCount,
                    total = totalLines,
                    color = StatusMinor,
                )
                PerformanceBar(
                    label = "Severe Disruptions",
                    value = severeDisruptionCount,
                    total = totalLines,
                    color = StatusSevere,
                )
            }
        }
    }
}

@Composable
private fun PerformanceBar(
    label: String,
    value: Int,
    total: Int,
    color: Color,
) {
    val percentage = if (total > 0) value.toFloat() / total else 0f
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 800),
        label = "performance",
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp),
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedPercentage)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color),
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$value",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun CrowdLevelsWidget(crowdLevel: CrowdLevel) {
    val crowdColor = when {
        crowdLevel.percentage >= 0.75f -> StatusSevere
        crowdLevel.percentage >= 0.40f -> StatusMinor
        else -> StatusGood
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = TubeAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Crowd Levels",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = crowdColor.copy(alpha = 0.1f),
                ) {
                    Text(
                        text = crowdLevel.level,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = crowdColor,
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Current crowd level — dynamically based on time of day
            CrowdLevelItem(
                timeRange = crowdLevel.label,
                level = crowdLevel.level,
                percentage = crowdLevel.percentage,
                color = crowdColor,
                isActive = true,
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Show upcoming period forecast
            val hour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
            val upcoming = remember(hour) { getUpcomingPeriods(hour) }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                upcoming.forEach { period ->
                    val pColor = when {
                        period.percentage >= 0.75f -> StatusSevere
                        period.percentage >= 0.40f -> StatusMinor
                        else -> StatusGood
                    }
                    CrowdLevelItem(
                        timeRange = period.label,
                        level = period.level,
                        percentage = period.percentage,
                        color = pColor,
                        isActive = false,
                    )
                }
            }
        }
    }
}

private fun getUpcomingPeriods(currentHour: Int): List<CrowdLevel> {
    val allPeriods = listOf(
        7 to CrowdLevel("Morning Rush (7-9)", "High", 0.85f),
        10 to CrowdLevel("Late Morning (10-11)", "Moderate", 0.45f),
        12 to CrowdLevel("Midday (12-14)", "Low", 0.30f),
        15 to CrowdLevel("Afternoon (15-16)", "Moderate", 0.50f),
        17 to CrowdLevel("Evening Rush (17-19)", "Very High", 0.92f),
        20 to CrowdLevel("Evening (20-22)", "Low", 0.25f),
    )
    return allPeriods
        .filter { it.first > currentHour }
        .take(2)
        .map { it.second }
}

@Composable
private fun CrowdLevelItem(
    timeRange: String,
    level: String,
    percentage: Float,
    color: Color,
    isActive: Boolean = false,
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 1000),
        label = "crowd",
    )
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(StatusGood),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = if (isActive) "Now · $timeRange" else timeRange,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isActive) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = level,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedPercentage)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(3.dp))
                    .background(color),
            )
        }
    }
}

@Composable
private fun CommuteInsightPill(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// QUICK COMMUTE CARD — One-tap commute for daily users
// ═══════════════════════════════════════════════════════════════

@Composable
private fun QuickCommuteCard(
    homeStation: String,
    workStation: String,
    commuteTime: String?,
    disruptionCount: Int,
    snapshot: CommuterSnapshot?,
    healthScore: Int? = null,
    extraDelayMinutes: Int? = null,
    onStartCommute: () -> Unit,
    onOpenStatus: () -> Unit,
) {
    val hour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val isEvening = hour >= 15
    val fromLabel = if (isEvening) workStation else homeStation
    val toLabel = if (isEvening) homeStation else workStation
    val direction = if (isEvening) "Head Home" else "Go to Work"
    val disruptionLabel = when {
        (snapshot?.disruptedFavoriteLines ?: 0) > 0 -> "${snapshot?.disruptedFavoriteLines} of your regular lines disrupted"
        disruptionCount > 0 -> "$disruptionCount network disruption${if (disruptionCount > 1) "s" else ""} active"
        else -> "Your regular corridor looks clear"
    }
    val disruptionColor = if ((snapshot?.disruptedFavoriteLines ?: 0) > 0 || disruptionCount > 0) StatusSevere else StatusGood

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onStartCommute() },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(TubePrimary.copy(alpha = 0.15f), TubeAccent.copy(alpha = 0.15f))
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isEvening) Icons.Filled.Place else Icons.Filled.DirectionsSubway,
                        contentDescription = null,
                        tint = TubePrimary,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = direction,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "$fromLabel → $toLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(disruptionColor),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = disruptionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = disruptionColor,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                commuteTime?.let {
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TubePrimary.copy(alpha = 0.1f),
                        ) {
                            Text(
                                text = it,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = TubePrimary,
                            )
                        }
                        // Phase D: Commute health score pill \u2014 at-a-glance score 0\u2013100.
                        healthScore?.let { score ->
                            val (label, color) = when {
                                score >= 80 -> "Healthy" to StatusGood
                                score >= 55 -> "OK" to StatusMinor
                                else -> "Strained" to StatusSevere
                            }
                            val delaySuffix = extraDelayMinutes?.takeIf { d -> d > 0 }?.let { d -> " · +${d}m" } ?: ""
                            Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.12f)) {
                                Text(
                                    text = "$score\u202f\u00b7\u202f$label$delaySuffix",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = color,
                                )
                            }
                        }
                    }
                }
            }
            if (snapshot != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    snapshot.preferredDepartureLabel?.let {
                        CommuteInsightPill(
                            icon = Icons.Outlined.Schedule,
                            title = "Usual time",
                            value = it,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    snapshot.weeklyTripsLabel?.let {
                        CommuteInsightPill(
                            icon = Icons.Filled.Route,
                            title = "Routine",
                            value = it,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                if (snapshot.topRouteLabel != null || snapshot.favoriteLineNames.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        snapshot.topRouteLabel?.let {
                            CommuteInsightPill(
                                icon = Icons.Filled.MyLocation,
                                title = "Top route",
                                value = it,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (snapshot.favoriteLineNames.isNotEmpty()) {
                            CommuteInsightPill(
                                icon = Icons.Filled.Star,
                                title = "Your lines",
                                value = snapshot.favoriteLineNames.joinToString(", "),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                if (snapshot.disruptedFavoriteLines > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenStatus() },
                        shape = RoundedCornerShape(12.dp),
                        color = StatusSevere.copy(alpha = 0.08f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = StatusSevere,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Check the lines affecting your usual trip",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = StatusSevere,
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = StatusSevere,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaveNowAssistantCard(
    assistant: LeaveNowAssistant,
    onOpenRoute: () -> Unit,
) {
    val accent = when (assistant.status) {
        LeaveNowStatus.WAIT -> TubePrimary
        LeaveNowStatus.SOON -> StatusMinor
        LeaveNowStatus.NOW -> StatusGood
        LeaveNowStatus.LATE -> StatusSevere
    }
    val icon = when (assistant.status) {
        LeaveNowStatus.WAIT -> Icons.Filled.AccessTime
        LeaveNowStatus.SOON -> Icons.Filled.Notifications
        LeaveNowStatus.NOW -> Icons.Filled.PlayArrow
        LeaveNowStatus.LATE -> Icons.Filled.Warning
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onOpenRoute() },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = assistant.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = assistant.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accent.copy(alpha = 0.12f),
            ) {
                Text(
                    text = assistant.actionLabel,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                )
            }
        }
    }
}

@Composable
private fun FallbackRoutesCard(
    options: List<FallbackRouteOption>,
    onOpenRoute: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(StatusMinor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Route, null, tint = StatusMinor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fallback commute routes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Backup options if your usual corridor is delayed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            options.forEachIndexed { index, option ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenRoute() },
                    shape = RoundedCornerShape(14.dp),
                    color = if (option.usesDisruptedLines) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    } else {
                        StatusGood.copy(alpha = 0.08f)
                    },
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = option.title,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "${option.durationMinutes} min",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = TubePrimary,
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = option.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = option.reason,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (option.usesDisruptedLines) StatusMinor else StatusGood,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                if (index < options.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// WELCOME TIPS CARD — Onboarding nudge for first-time visitors
// ═══════════════════════════════════════════════════════════════

@Composable
private fun WelcomeTipsCard(
    visitorGuides: List<VisitorLandmarkGuide>,
    onExploreStations: () -> Unit,
    onPlanJourney: () -> Unit,
    onOpenNearby: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TubeAccent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Explore, null, tint = TubeAccent, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Welcome to London Tube",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Start with nearby stations, route planning, and a few local habits",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            val tips = listOf(
                "Tap your Oyster or contactless card on the yellow reader",
                "Stand on the right on escalators — walk on the left",
                "Check the dot-matrix boards for live platform info",
            )
            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(TubeAccent),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                    )
                }
            }

            if (visitorGuides.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Popular landmarks",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                visitorGuides.take(3).forEachIndexed { index, guide ->
                    VisitorLandmarkRow(guide)
                    if (index < 2 && index < visitorGuides.take(3).lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlanJourney() },
                shape = RoundedCornerShape(12.dp),
                color = TubePrimary,
            ) {
                Text(
                    text = "Plan a Journey",
                    modifier = Modifier.padding(vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenNearby() },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = "Nearby Stations",
                        modifier = Modifier.padding(vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onExploreStations() },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = "Explore Stations",
                        modifier = Modifier.padding(vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun VisitorLandmarkRow(guide: VisitorLandmarkGuide) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TubeAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.LocationOn, null, tint = TubeAccent, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = guide.landmark,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${guide.stationName} · ${guide.exitName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (guide.lineNames.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = guide.lineNames.joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = TubePrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = guide.tip,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// QUICK ACTIONS — Elevated card grid with subtle shadows
// ═══════════════════════════════════════════════════════════════

@Composable
private fun QuickActions(
    primaryAction: HomePrimaryActionType,
    commuteDestinationName: String?,
    disruptionCount: Int,
    crowdLabel: String,
    onRouteClick: () -> Unit,
    onStatusClick: () -> Unit,
    onStationsClick: () -> Unit,
    onMapClick: () -> Unit = {},
) {
    val primaryCard = when (primaryAction) {
        HomePrimaryActionType.STATUS -> HomeQuickActionSpec(
            title = "Check disruptions",
            subtitle = if (disruptionCount > 0) "$disruptionCount severe network issue${if (disruptionCount == 1) "" else "s"} active right now" else "See the latest live line status updates",
            icon = Icons.Filled.Wifi,
            color = StatusSevere,
            onClick = onStatusClick,
        )
        HomePrimaryActionType.MAP -> HomeQuickActionSpec(
            title = "Open the map",
            subtitle = "$crowdLabel conditions are building — explore live trains and hotspots",
            icon = Icons.Filled.Map,
            color = TubeAccent,
            onClick = onMapClick,
        )
        HomePrimaryActionType.ROUTE -> HomeQuickActionSpec(
            title = commuteDestinationName?.let { "Commute to $it" } ?: "Where to?",
            subtitle = commuteDestinationName?.let { "Jump straight into your usual trip in one tap" } ?: "Start a fast journey plan with AI guidance",
            icon = Icons.Filled.Route,
            color = TubePrimary,
            onClick = onRouteClick,
        )
    }
    val secondaryActions = when (primaryAction) {
        HomePrimaryActionType.STATUS -> listOf(
            HomeQuickActionSpec("Route", "Plan", Icons.Filled.Route, TubePrimary, onRouteClick),
            HomeQuickActionSpec("Map", "Crowding", Icons.Filled.Map, TubeAccent, onMapClick),
            HomeQuickActionSpec("Stations", "Browse", Icons.Filled.MyLocation, TubeSecondary, onStationsClick),
        )
        HomePrimaryActionType.MAP -> listOf(
            HomeQuickActionSpec("Route", "Plan", Icons.Filled.Route, TubePrimary, onRouteClick),
            HomeQuickActionSpec("Status", "Live now", Icons.Filled.Wifi, StatusGood, onStatusClick),
            HomeQuickActionSpec("Stations", "Browse", Icons.Filled.MyLocation, TubeSecondary, onStationsClick),
        )
        HomePrimaryActionType.ROUTE -> listOf(
            HomeQuickActionSpec("Status", "Live now", Icons.Filled.Wifi, StatusGood, onStatusClick),
            HomeQuickActionSpec("Map", "Explore", Icons.Filled.Map, TubeAccent, onMapClick),
            HomeQuickActionSpec("Stations", "Browse", Icons.Filled.MyLocation, TubeSecondary, onStationsClick),
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PrimaryJourneyCard(
            title = primaryCard.title,
            subtitle = primaryCard.subtitle,
            icon = primaryCard.icon,
            accentColor = primaryCard.color,
            onClick = primaryCard.onClick,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            secondaryActions.forEach { action ->
                QuickActionCard(
                    title = action.title,
                    subtitle = action.subtitle,
                    icon = action.icon,
                    color = action.color,
                    modifier = Modifier.weight(1f),
                    onClick = action.onClick,
                )
            }
        }
    }
}

@Composable
private fun PrimaryJourneyCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = accentColor.copy(alpha = 0.10f),
                spotColor = accentColor.copy(alpha = 0.12f),
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.12f).compositeOver(MaterialTheme.colorScheme.surface),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = accentColor,
            ) {
                Text(
                    text = if (primaryActionForLabel(title)) "Go" else "Open",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

private fun primaryActionForLabel(title: String): Boolean {
    return title.contains("Commute", ignoreCase = true) || title.contains("Where to", ignoreCase = true)
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale",
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 2.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = color.copy(alpha = 0.1f),
                spotColor = color.copy(alpha = 0.12f),
            )
            .clickable {
                isPressed = true
                onClick()
                isPressed = false
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(color.copy(alpha = 0.15f), color.copy(alpha = 0.05f)),
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// AI INSIGHT CARDS — Premium horizontal scroll cards
// ═══════════════════════════════════════════════════════════════

@Composable
private fun AiInsightCard(
    insight: AiInsight,
    modifier: Modifier = Modifier,
    onInsightClick: (AiInsight) -> Unit = {},
    onActionClick: ((AiInsight) -> Unit)? = null,
) {
    val freshnessLabel = insight.metadata["freshnessLabel"] as? String
    val (iconTint, bgColor) = when (insight.type) {
        InsightType.CROWD_ALERT -> StatusSevere to StatusSevere.copy(alpha = 0.06f)
        InsightType.DELAY_WARNING -> StatusMinor to StatusMinor.copy(alpha = 0.06f)
        InsightType.TIME_SAVING -> StatusGood to StatusGood.copy(alpha = 0.06f)
        InsightType.CARRIAGE_TIP -> TubePrimary to TubePrimary.copy(alpha = 0.06f)
        InsightType.WEATHER_IMPACT -> StatusMinor to StatusMinor.copy(alpha = 0.06f)
        InsightType.EVENT_ALERT -> TubePrimary to TubePrimary.copy(alpha = 0.06f)
        InsightType.GENERAL -> TubeSecondary to TubeSecondary.copy(alpha = 0.06f)
        
        // Enhanced types
        InsightType.PERSONAL_ROUTE -> StatusGood to StatusGood.copy(alpha = 0.08f)
        InsightType.STATION_HABIT -> TubePrimary to TubePrimary.copy(alpha = 0.08f)
        InsightType.TIME_PREFERENCE -> StatusGood to StatusGood.copy(alpha = 0.06f)
        InsightType.DISRUPTION_IMPACT -> StatusSevere to StatusSevere.copy(alpha = 0.08f)
        InsightType.SAVINGS_OPPORTUNITY -> StatusGood to StatusGood.copy(alpha = 0.06f)
        InsightType.SOCIAL_INSIGHT -> TubePrimary to TubePrimary.copy(alpha = 0.06f)
        InsightType.LEARNING_PATTERN -> TubeSecondary to TubeSecondary.copy(alpha = 0.06f)
    }

    val icon = when (insight.type) {
        InsightType.CROWD_ALERT -> Icons.AutoMirrored.Filled.TrendingUp
        InsightType.DELAY_WARNING -> Icons.Outlined.Schedule
        InsightType.TIME_SAVING -> Icons.Filled.AccessTime
        InsightType.CARRIAGE_TIP -> Icons.Filled.Lightbulb
        InsightType.WEATHER_IMPACT -> Icons.Outlined.Schedule
        InsightType.EVENT_ALERT -> Icons.Filled.Notifications
        InsightType.GENERAL -> Icons.Filled.Star
        
        // Enhanced icons
        InsightType.PERSONAL_ROUTE -> Icons.Filled.DirectionsSubway
        InsightType.STATION_HABIT -> Icons.Filled.Place
        InsightType.TIME_PREFERENCE -> Icons.Filled.AccessTime
        InsightType.DISRUPTION_IMPACT -> Icons.Filled.PriorityHigh
        InsightType.SAVINGS_OPPORTUNITY -> Icons.Filled.Star
        InsightType.SOCIAL_INSIGHT -> Icons.Filled.People
        InsightType.LEARNING_PATTERN -> Icons.Filled.Lightbulb
    }

    // Enhanced card with animations and interactions
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "cardScale"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = if (insight.isPersonalized) 8.dp else 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = iconTint.copy(alpha = 0.08f),
                spotColor = iconTint.copy(alpha = 0.1f),
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onInsightClick(insight)
                    }
                )
            },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with enhanced indicators
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor),
                    contentAlignment = Alignment.Center,
                ) {
                    // Live indicator for real-time insights
                    if (insight.isLive) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(StatusGood, CircleShape)
                                .offset(x = 12.dp, y = (-12).dp)
                        )
                    }
                    
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = insight.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Personalized badge
                        if (insight.isPersonalized) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                                color = TubePrimary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "For You",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TubePrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (insight.confidence > 0) {
                            Text(
                                text = "${(insight.confidence * 100).toInt()}% confidence",
                                style = MaterialTheme.typography.labelSmall,
                                color = iconTint.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                            )
                        }
                        if (freshnessLabel != null) {
                            if (insight.confidence > 0) Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = freshnessLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                            )
                        }
                        
                        // Priority indicator
                        if (insight.userImpact == UserImpact.HIGH || insight.userImpact == UserImpact.CRITICAL) {
                            if (insight.confidence > 0 || freshnessLabel != null) Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Filled.PriorityHigh,
                                contentDescription = "High Priority",
                                tint = if (insight.userImpact == UserImpact.CRITICAL) StatusSevere else StatusMinor,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        
                        // Live indicator text
                        if (insight.isLive) {
                            if (insight.confidence > 0 || freshnessLabel != null || insight.userImpact >= UserImpact.HIGH) Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusGood,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = insight.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            if (insight.actionLabel != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    modifier = Modifier.clickable { (onActionClick ?: onInsightClick).invoke(insight) },
                    shape = RoundedCornerShape(10.dp),
                    color = iconTint.copy(alpha = 0.08f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = insight.actionLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = iconTint,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveNetworkDetailSheet(
    status: LineStatus,
    onOpenStatus: () -> Unit,
    onDismiss: () -> Unit,
) {
    val statusColor = when {
        status.statusSeverity >= 10 -> StatusGood
        status.statusSeverity >= 5 -> StatusMinor
        else -> StatusSevere
    }

    val lineColor = status.lineColor
    val tubeLine = getTubeLineDetails(status.lineId)
    val keyStations = getNearbyStationsForLine(status.lineId)
    val connections = getConnectionOptions(status.lineId)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
    ) {
        // ── Header: line color bar + name + status ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(lineColor)
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.DirectionsSubway,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = status.lineName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                    Text(
                        text = status.statusDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.2f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (status.isGoodService) StatusGood else statusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Live",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // ── Quick stats row ──
            tubeLine?.let { line ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SheetStatCard(
                        modifier = Modifier.weight(1f),
                        value = "${line.totalStations}",
                        label = "Stations",
                        color = lineColor,
                    )
                    SheetStatCard(
                        modifier = Modifier.weight(1f),
                        value = "${line.peakFrequency} min",
                        label = "Frequency",
                        color = lineColor,
                    )
                    SheetStatCard(
                        modifier = Modifier.weight(1f),
                        value = line.firstTrain,
                        label = "First",
                        color = lineColor,
                    )
                    SheetStatCard(
                        modifier = Modifier.weight(1f),
                        value = line.lastTrain,
                        label = "Last",
                        color = lineColor,
                    )
                }

                // ── Route: start → end ──
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = lineColor.copy(alpha = 0.06f),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("From", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(line.startStation, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(2.dp)
                                .background(lineColor.copy(alpha = 0.3f))
                        )
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text("To", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(line.endStation, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End)
                        }
                    }
                }
            }

            // ── Disruption reason ──
            if (!status.reason.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = statusColor.copy(alpha = 0.08f),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, null, tint = statusColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Disruption", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = statusColor)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(status.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                    }
                }
            }

            // ── Key stations ──
            if (keyStations.isNotEmpty()) {
                Text("Key Stations", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                keyStations.forEach { station ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(lineColor))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(station.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Text("Zone ${station.zone}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (station.hasStepFreeAccess) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.Accessible, null, tint = StatusGood, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // ── Connections ──
            if (connections.isNotEmpty()) {
                Text("Interchange Lines", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                connections.forEach { conn ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(conn.color))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(conn.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(conn.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(conn.time, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // ── Action buttons ──
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    modifier = Modifier.weight(1f).clickable(onClick = onDismiss),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ) {
                    Text(
                        "Close",
                        modifier = Modifier.padding(vertical = 14.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                Surface(
                    modifier = Modifier.weight(1f).clickable(onClick = onOpenStatus),
                    shape = RoundedCornerShape(14.dp),
                    color = lineColor,
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Full Status", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetStatCard(modifier: Modifier = Modifier, value: String, label: String, color: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// Helper data classes and functions for enhanced bottom sheet
data class TubeLineDetails(
    val startStation: String,
    val endStation: String,
    val totalStations: Int,
    val peakFrequency: Int,
    val offPeakFrequency: Int,
    val firstTrain: String,
    val lastTrain: String,
)

data class NearbyStation(
    val name: String,
    val distanceKm: Double,
    val zone: String,
    val hasStepFreeAccess: Boolean = false,
)

data class ConnectionOption(
    val name: String,
    val description: String,
    val time: String,
    val color: Color,
)

private fun getTubeLineDetails(lineId: String): TubeLineDetails? {
    val line = TubeData.getLineById(lineId) ?: return null
    val firstStation = line.stationIds.firstOrNull()?.let { TubeData.getStationById(it) }
    val lastStation = line.stationIds.lastOrNull()?.let { TubeData.getStationById(it) }
    return TubeLineDetails(
        startStation = firstStation?.name ?: "Unknown",
        endStation = lastStation?.name ?: "Unknown",
        totalStations = line.stationIds.size,
        peakFrequency = line.peakFrequencyMinutes,
        offPeakFrequency = line.offPeakFrequencyMinutes,
        firstTrain = line.firstTrain,
        lastTrain = line.lastTrain,
    )
}

private fun getNearbyStationsForLine(lineId: String): List<NearbyStation> {
    val line = TubeData.getLineById(lineId) ?: return emptyList()
    val stationIds = line.stationIds
    if (stationIds.isEmpty()) return emptyList()

    // Pick key stations: first, a middle one, and last
    val keyIndices = when {
        stationIds.size <= 2 -> stationIds.indices.toList()
        stationIds.size <= 5 -> listOf(0, stationIds.size / 2, stationIds.size - 1)
        else -> listOf(0, stationIds.size / 3, 2 * stationIds.size / 3, stationIds.size - 1)
    }.distinct()

    return keyIndices.mapNotNull { idx ->
        val station = TubeData.getStationById(stationIds[idx])
        station?.let {
            NearbyStation(
                name = it.name,
                distanceKm = 0.0,
                zone = it.zone,
                hasStepFreeAccess = it.hasStepFreeAccess,
            )
        }
    }.take(4)
}

private fun getConnectionOptions(lineId: String): List<ConnectionOption> {
    val line = TubeData.getLineById(lineId) ?: return emptyList()
    val interchangeLines = mutableMapOf<String, Pair<String, Int>>() 
    for (stationId in line.stationIds) {
        val station = TubeData.getStationById(stationId) ?: continue
        for (otherLineId in station.lineIds) {
            if (otherLineId != lineId && otherLineId !in interchangeLines) {
                interchangeLines[otherLineId] = station.name to station.interchangeTimeMinutes
            }
        }
        if (interchangeLines.size >= 3) break
    }
    return interchangeLines.entries.take(3).mapNotNull { (otherLineId, stationInfo) ->
        val otherLine = TubeData.getLineById(otherLineId) ?: return@mapNotNull null
        val stationName = stationInfo.first
        val interchangeMinutes = stationInfo.second
        ConnectionOption(
            name = "${otherLine.name} Line",
            description = "Interchange at $stationName",
            time = if (interchangeMinutes > 0) "~${interchangeMinutes} min" else "Interchange",
            color = otherLine.color,
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// INSIGHT DETAIL BOTTOM SHEET
// ═══════════════════════════════════════════════════════════════

@Composable
private fun InsightDetailSheet(
    insight: AiInsight,
    onTakeAction: () -> Unit,
    onDismiss: () -> Unit,
) {
    // Use consistent blue theme for all insights
    val blueColor = Color(0xFF1976D2)
    val iconTint = blueColor
    val bgColor = blueColor.copy(alpha = 0.22f)

    val icon = when (insight.type) {
        InsightType.CROWD_ALERT -> Icons.AutoMirrored.Filled.TrendingUp
        InsightType.DELAY_WARNING -> Icons.Outlined.Schedule
        InsightType.TIME_SAVING -> Icons.Filled.AccessTime
        InsightType.CARRIAGE_TIP -> Icons.Filled.Lightbulb
        InsightType.WEATHER_IMPACT -> Icons.Filled.Cloud
        InsightType.EVENT_ALERT -> Icons.Filled.Notifications
        InsightType.GENERAL -> Icons.Filled.Star
        InsightType.PERSONAL_ROUTE -> Icons.Filled.DirectionsSubway
        InsightType.STATION_HABIT -> Icons.Filled.Place
        InsightType.TIME_PREFERENCE -> Icons.Filled.AccessTime
        InsightType.DISRUPTION_IMPACT -> Icons.Filled.PriorityHigh
        InsightType.SAVINGS_OPPORTUNITY -> Icons.Filled.Star
        InsightType.SOCIAL_INSIGHT -> Icons.Filled.People
        InsightType.LEARNING_PATTERN -> Icons.Filled.Lightbulb
    }

    val impactLabel = when (insight.userImpact) {
        UserImpact.LOW -> "Low Impact"
        UserImpact.MEDIUM -> "Medium Impact"
        UserImpact.HIGH -> "High Impact"
        UserImpact.CRITICAL -> "Critical"
    }

    val impactColor = when (insight.userImpact) {
        UserImpact.LOW -> StatusGood
        UserImpact.MEDIUM -> TubeSecondary
        UserImpact.HIGH -> StatusMinor
        UserImpact.CRITICAL -> StatusSevere
    }

    val typeLabel = insight.type.name.replace("_", " ").lowercase()
        .replaceFirstChar { it.uppercase() }

    var feedbackGiven by remember { mutableStateOf<Boolean?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 600.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            // Header with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                blueColor.copy(alpha = 0.2f),
                                blueColor.copy(alpha = 0.05f),
                            ),
                            startY = 0f,
                            endY = 300f
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(
                                    width = 2.dp,
                                    color = blueColor,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = blueColor,
                                modifier = Modifier.size(28.dp),
                            )
                        }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = blueColor.copy(alpha = 0.1f),
                    ) {
                        Text(
                            text = typeLabel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = blueColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    if (insight.isPersonalized) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = TubePrimary.copy(alpha = 0.1f),
                        ) {
                            Text(
                                text = "For You",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = TubePrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    if (insight.isLive) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = StatusGood.copy(alpha = 0.1f),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(StatusGood)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "LIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusGood,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }

                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 0.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Full description
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp,
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis,
                )

                // Impact + confidence row
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = blueColor.copy(alpha = 0.08f),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Impact",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = blueColor.copy(alpha = 0.12f),
                    ) {
                        Text(
                            text = impactLabel,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = blueColor,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                if (insight.confidence > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Confidence",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${(insight.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${insight.priority}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

                // Feedback row
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (feedbackGiven != null) "Thanks for your feedback!" else "Was this helpful?",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        modifier = Modifier.clickable { feedbackGiven = true },
                        shape = RoundedCornerShape(8.dp),
                        color = if (feedbackGiven == true) StatusGood.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Helpful",
                                tint = if (feedbackGiven == true) StatusGood else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Yes",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (feedbackGiven == true) StatusGood else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier.clickable { feedbackGiven = false },
                        shape = RoundedCornerShape(8.dp),
                        color = if (feedbackGiven == false) StatusSevere.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Not helpful",
                                tint = if (feedbackGiven == false) StatusSevere else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "No",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (feedbackGiven == false) StatusSevere else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onDismiss),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ) {
                Text(
                    text = "Close",
                    modifier = Modifier.padding(vertical = 12.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onTakeAction),
                shape = RoundedCornerShape(12.dp),
                color = blueColor.copy(alpha = 0.12f),
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = insight.actionLabel ?: "View Details",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = blueColor,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = blueColor,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// SECTION HEADER — With optional badge
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    trailing: String? = null,
    badge: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        if (badge != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            ) {
                Text(
                    text = badge,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        if (trailing != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ) {
                Text(
                    text = trailing,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// PERMISSION REQUEST DIALOG
// ═══════════════════════════════════════════════════════════════

@Composable
private fun PermissionRequestDialog(
    onDismiss: () -> Unit,
    onRequestPermissions: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(TubePrimary, TubeAccent)
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = "Enable Location Services",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "London Tube AI uses your location to show:",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PermissionFeatureItem(
                        icon = Icons.Filled.MyLocation,
                        text = "Live arrivals at nearby stations",
                        color = StatusGood,
                    )
                    PermissionFeatureItem(
                        icon = Icons.Filled.DirectionsSubway,
                        text = "Walking distance to stations",
                        color = TubePrimary,
                    )
                    PermissionFeatureItem(
                        icon = Icons.Filled.NotificationsActive,
                        text = "Disruption alerts for your area",
                        color = TubeAccent,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your location is only used while the app is open and is never stored or shared. You can change this at any time in Settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = onRequestPermissions,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enable Location",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Maybe Later",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    )
}

@Composable
private fun PermissionFeatureItem(
    icon: ImageVector,
    text: String,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// LOADING & ERROR STATES
// ═══════════════════════════════════════════════════════════════

@Composable
private fun StatusLoadingShimmer() {
    var shimmerAlpha by remember { mutableStateOf(0.3f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            shimmerAlpha = 0.3f
            delay(800)
            shimmerAlpha = 0.6f
            delay(800)
        }
    }
    
    val animatedAlpha by animateFloatAsState(
        targetValue = shimmerAlpha,
        animationSpec = tween(durationMillis = 800),
        label = "shimmer",
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(4) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = animatedAlpha),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.4f)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusErrorCard(error: String, onRetry: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = StatusSevere.copy(alpha = 0.06f),
        ),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(StatusSevere.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = StatusSevere,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Connection Issue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                    )
                }
            }
            
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onRetry),
                    shape = RoundedCornerShape(12.dp),
                    color = StatusSevere.copy(alpha = 0.1f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = StatusSevere,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Try Again",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = StatusSevere,
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Top contextual banner — shown above the hero when the user's
// commute is disrupted or weather impact is high. Tappable when
// onClick is provided so users can drill into status detail.
// ═══════════════════════════════════════════════════════════════

@Composable
private fun TopContextBanner(
    color: Color,
    icon: ImageVector,
    title: String,
    detail: String,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.10f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.30f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                )
                Text(
                    detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (onClick != null) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    null,
                    tint = color.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

/**
 * Generates a (title, detail) tuple for weather hints. We surface
 * actionable advice tied to London tube context: rain → tube preferred,
 * snow → expect delays, fog → check status, thunder → take shelter.
 */
private fun weatherHintText(weather: WeatherInfo): Pair<String, String> = when (weather.icon) {
    "09d", "09n", "10d", "10n" -> "Rain expected" to "Tube preferred — leave a few minutes earlier"
    "13d", "13n" -> "Snow forecast" to "Expect delays · check line status before travelling"
    "11d", "11n" -> "Thunderstorm warning" to "Consider waiting if your route involves walking"
    "50d", "50n" -> "Foggy conditions" to "Visibility low · allow extra time"
    else -> "${weather.condition} · ${weather.temperature}°" to "Check status before heading out"
}
