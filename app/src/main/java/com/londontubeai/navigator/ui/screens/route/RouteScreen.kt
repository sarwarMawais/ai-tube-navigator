package com.londontubeai.navigator.ui.screens.route

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import android.content.Intent
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.londontubeai.navigator.data.model.CrowdLevel
import com.londontubeai.navigator.data.model.JourneyLeg
import com.londontubeai.navigator.data.model.JourneyRoute
import com.londontubeai.navigator.data.model.TransportMode
import com.londontubeai.navigator.data.model.TubeData
import com.londontubeai.navigator.ui.components.CarriageVisualizer
import com.londontubeai.navigator.ui.components.TubeLineChip
import com.londontubeai.navigator.ui.components.UnifiedHeader
import com.londontubeai.navigator.ui.theme.StatusGood
import com.londontubeai.navigator.ui.theme.StatusMinor
import com.londontubeai.navigator.ui.theme.StatusSevere
import com.londontubeai.navigator.ui.theme.TubeAccent
import com.londontubeai.navigator.ui.theme.TubePrimary
import com.londontubeai.navigator.ui.theme.TubeSecondary
import com.londontubeai.navigator.ml.CrowdPredictionEngine
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RouteScreen(
    viewModel: RouteViewModel = hiltViewModel(),
    onNavigateToMap: (String, String) -> Unit = { _, _ -> },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recentJourneys by viewModel.recentJourneys.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showTimePicker by remember { mutableStateOf(false) }

    val swapRotation by animateFloatAsState(
        targetValue = if (uiState.isSwapping) 180f else 0f,
        animationSpec = tween(300),
        label = "swap",
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
    ) { scaffoldPadding ->
    Column(modifier = Modifier.fillMaxSize().padding(scaffoldPadding)) {
        // ── Google Maps-Style Compact Search Header ──────────────────
        GMapsSearchHeader(
            uiState = uiState,
            swapRotation = swapRotation,
            onFromChange = { viewModel.updateFromQuery(it) },
            onToChange = { viewModel.updateToQuery(it) },
            onFromFocus = { viewModel.setFromFieldFocused(it) },
            onToFocus = { viewModel.setToFieldFocused(it) },
            onFromSelect = { s -> viewModel.selectFromStation(s); keyboardController?.hide(); focusManager.clearFocus() },
            onToSelect = { s -> viewModel.selectToStation(s); keyboardController?.hide(); focusManager.clearFocus() },
            onSwap = { viewModel.swapStations() },
            onUseLocation = { viewModel.useMyLocation() },
            onClearFrom = { viewModel.updateFromQuery("") },
            onClearTo = { viewModel.updateToQuery("") },
            onSelectPreference = { viewModel.selectPreference(it) },
            onSelectDeparture = { opt ->
                viewModel.selectDepartureOption(opt)
                if (opt != DepartureOption.LEAVE_NOW) showTimePicker = true
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            // ── Calculating ──────────────────────────────────────
            if (uiState.isCalculating) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Finding the best route...", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // ── Route Error ──────────────────────────────────────
            if (uiState.routeError != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = StatusSevere.copy(alpha = 0.08f),
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, null, tint = StatusSevere, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(uiState.routeError ?: "", style = MaterialTheme.typography.bodySmall, color = StatusSevere)
                        }
                    }
                }
            }

            // ── Location Banner ──────────────────────────────────
            if (uiState.locationChecked && !uiState.routeCalculated) {
                item {
                    if (uiState.isInsideLondon) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = StatusGood.copy(alpha = 0.08f),
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.MyLocation, null, tint = StatusGood, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = uiState.nearestStationWalkMinutes?.let {
                                        "${uiState.nearestStationName} · ~${it} min walk"
                                    } ?: "In London · Live data active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusGood,
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Surface(shape = RoundedCornerShape(6.dp), color = StatusGood.copy(alpha = 0.15f)) {
                                    Text("● Live", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = StatusGood)
                                }
                            }
                        }
                    } else if (uiState.nearestStationName != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = StatusMinor.copy(alpha = 0.08f),
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Public, null, tint = StatusMinor, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = uiState.nearestStationWalkMinutes?.let {
                                        "Outside London · ${uiState.nearestStationName} (~${it} min walk)"
                                    } ?: "Outside London · ${uiState.nearestStationName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusMinor,
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Surface(shape = RoundedCornerShape(6.dp), color = StatusMinor.copy(alpha = 0.15f)) {
                                    Text("General", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = StatusMinor)
                                }
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = StatusMinor.copy(alpha = 0.08f),
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Public, null, tint = StatusMinor, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Outside London · General route data shown", style = MaterialTheme.typography.labelSmall, color = StatusMinor)
                            }
                        }
                    }
                }
            }

            // ── Route Result ─────────────────────────────────────
            if (uiState.routeCalculated) {
                val route = uiState.journeyRoute

                // Google Maps-style multi-option cards
                item {
                    RouteOptionsSection(
                        options = uiState.routeOptions,
                        selectedIndex = uiState.selectedRouteIndex,
                        onSelectOption = { viewModel.selectRouteOption(it) },
                        onNavigateToMap = onNavigateToMap,
                        route = route,
                    )
                }

                // Google Maps-style step-by-step timeline
                if (route != null && route.legs.isNotEmpty()) {
                    item {
                        GoogleMapsTimelineCard(route = route, isInsideLondon = uiState.isInsideLondon)
                    }
                }

                // AI Carriage Tip
                val rec = uiState.carriageRecommendation
                if (rec != null) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = StatusGood.copy(alpha = 0.06f),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(StatusGood.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.Lightbulb, null, tint = StatusGood, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("AI Carriage Tip", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                        Text("Board carriage ${rec.carriageNumber} · saves ~${rec.timeSavedSeconds}s", style = MaterialTheme.typography.bodySmall, color = StatusGood)
                                    }
                                    Surface(shape = RoundedCornerShape(8.dp), color = StatusGood.copy(alpha = 0.15f)) {
                                        Text("AI", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = StatusGood)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                val destStation = uiState.toStation
                                val lineColor = destStation?.lineIds?.firstOrNull()?.let { TubeData.getLineById(it)?.color } ?: TubePrimary
                                CarriageVisualizer(totalCarriages = destStation?.totalCarriages ?: 8, recommendedCarriage = rec.carriageNumber, lineColor = lineColor)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(rec.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                            }
                        }
                    }
                }

                // Crowd Prediction
                val crowd = uiState.crowdPrediction
                if (crowd != null) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 3.dp,
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(crowdColor(crowd.crowdLevel).copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.Group, null, tint = crowdColor(crowd.crowdLevel), modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Crowd Prediction", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        CrowdPredictionEngine.DayType.entries.forEach { dayType ->
                                            val sel = uiState.selectedCrowdDayType == dayType
                                            val lbl = when (dayType) {
                                                CrowdPredictionEngine.DayType.AUTO -> "Auto"
                                                CrowdPredictionEngine.DayType.WEEKDAY -> "WD"
                                                CrowdPredictionEngine.DayType.WEEKEND -> "WE"
                                            }
                                            Surface(
                                                onClick = { viewModel.selectCrowdDayType(dayType) },
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (sel) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else Color.Transparent,
                                                border = BorderStroke(1.dp, if (sel) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                            ) {
                                                Text(lbl, modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                val animatedCrowdFill by animateFloatAsState(targetValue = crowd.percentageFull / 100f, animationSpec = tween(800), label = "crowdFill")
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(crowd.crowdLevel.label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = crowdColor(crowd.crowdLevel), modifier = Modifier.width(92.dp))
                                    Box(modifier = Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(5.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                                        Box(modifier = Modifier.fillMaxWidth(animatedCrowdFill).fillMaxHeight().clip(RoundedCornerShape(5.dp)).background(Brush.horizontalGradient(listOf(crowdColor(crowd.crowdLevel).copy(alpha = 0.7f), crowdColor(crowd.crowdLevel)))))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("${crowd.percentageFull}%", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = crowdColor(crowd.crowdLevel))
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(crowd.recommendation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                            }
                        }
                    }
                }

                // Action buttons
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ActionButton(
                            modifier = Modifier.weight(1f),
                            icon = if (uiState.isFavorite) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            label = if (uiState.isFavorite) "Saved" else "Save",
                            active = uiState.isFavorite,
                            onClick = { viewModel.toggleFavorite() },
                        )
                        ActionButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Filled.Share,
                            label = "Share",
                            onClick = {
                                val i = Intent().apply { action = Intent.ACTION_SEND; putExtra(Intent.EXTRA_TEXT, viewModel.getShareText()); type = "text/plain" }
                                context.startActivity(Intent.createChooser(i, "Share Journey"))
                            },
                        )
                        ActionButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Filled.Notifications,
                            label = if (uiState.reminderSet) "Set" else "Remind",
                            active = uiState.reminderSet,
                            onClick = { viewModel.setReminder(); scope.launch { snackbarHostState.showSnackbar("Reminder set for this journey") } },
                        )
                    }
                }
            }

            // ── Nearby Buses from Current Location ─────────────────
            if (uiState.isInsideLondon && (uiState.nearbyBusRoutes.isNotEmpty() || uiState.isFetchingBuses)) {
                item {
                    NearbyBusesSection(
                        busRoutes = uiState.nearbyBusRoutes,
                        isLoading = uiState.isFetchingBuses,
                    )
                }
            }

            // ── Recent Journeys ──────────────────────────────────
            if (!uiState.routeCalculated && recentJourneys.isNotEmpty()) {
                item {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.History, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recent Journeys", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
                items(recentJourneys) { journey ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp).clickable { viewModel.selectRecentJourney(journey) },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp,
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.DirectionsSubway, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${journey.fromStationName} → ${journey.toStationName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("Tap to plan again", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (journey.isFavourite) {
                                Icon(Icons.Filled.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // ── Empty State ──────────────────────────────────────
            if (!uiState.routeCalculated && !uiState.isCalculating && recentJourneys.isEmpty()
                && uiState.fromStation == null && uiState.toStation == null
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(modifier = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Route, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("Where to?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Enter start and destination above\nfor AI-powered route suggestions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                        )
                    }
                }
            }
        }
    }
    } // end Scaffold

    // ── Departure Time Picker Dialog ────────────────────────
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.currentHour,
            initialMinute = uiState.currentMinute,
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    viewModel.selectDepartureOption(
                        if (uiState.departureOption == DepartureOption.ARRIVE_BY) DepartureOption.ARRIVE_BY
                        else DepartureOption.DEPART_AT,
                    )
                    viewModel.updateDepartureTime(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            title = {
                Text(
                    text = if (uiState.departureOption == DepartureOption.ARRIVE_BY) "Arrive By" else "Depart At",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = { TimeInput(state = timePickerState) },
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// GOOGLE MAPS-STYLE COMPOSABLES
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GMapsSearchHeader(
    uiState: RouteUiState,
    swapRotation: Float,
    onFromChange: (String) -> Unit,
    onToChange: (String) -> Unit,
    onFromFocus: (Boolean) -> Unit,
    onToFocus: (Boolean) -> Unit,
    onFromSelect: (com.londontubeai.navigator.data.model.Station) -> Unit,
    onToSelect: (com.londontubeai.navigator.data.model.Station) -> Unit,
    onSwap: () -> Unit,
    onUseLocation: () -> Unit,
    onClearFrom: () -> Unit,
    onClearTo: () -> Unit,
    onSelectPreference: (RoutePreference) -> Unit,
    onSelectDeparture: (DepartureOption) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Column(modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)) {
            // ── From/To pill card ──────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Origin/Destination dot indicators
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(end = 10.dp, top = 2.dp, bottom = 2.dp),
                    ) {
                        if (uiState.fromIsAutoLocation) {
                            Icon(Icons.Filled.MyLocation, null, tint = StatusGood, modifier = Modifier.size(11.dp))
                        } else {
                            Box(
                                modifier = Modifier.size(11.dp).clip(CircleShape).background(Color(0xFF1A73E8))
                            )
                        }
                        repeat(4) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)))
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Box(
                            modifier = Modifier.size(11.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFEA4335))
                        )
                    }
                    // Search fields
                    Column(modifier = Modifier.weight(1f)) {
                        // From row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(46.dp),
                        ) {
                            BasicTextField(
                                value = uiState.fromQuery,
                                onValueChange = onFromChange,
                                singleLine = true,
                                modifier = Modifier.weight(1f).onFocusChanged { onFromFocus(it.isFocused) },
                                textStyle = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { inner ->
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.CenterStart,
                                    ) {
                                        if (uiState.fromQuery.isEmpty()) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (uiState.fromIsAutoLocation) {
                                                    Icon(Icons.Filled.MyLocation, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                Text(
                                                    text = when {
                                                        uiState.isResolvingLocation -> "Detecting location…"
                                                        uiState.fromIsAutoLocation -> "My Location"
                                                        else -> "Starting point"
                                                    },
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                                                    fontSize = 15.sp,
                                                )
                                            }
                                        }
                                        inner()
                                    }
                                },
                            )
                            if (uiState.isResolvingLocation) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            if (uiState.fromQuery.isNotEmpty()) {
                                IconButton(onClick = onClearFrom, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Filled.Close, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        // Thin divider
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)))
                        // To row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(46.dp),
                        ) {
                            BasicTextField(
                                value = uiState.toQuery,
                                onValueChange = onToChange,
                                singleLine = true,
                                modifier = Modifier.weight(1f).onFocusChanged { onToFocus(it.isFocused) },
                                textStyle = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                                cursorBrush = SolidColor(Color(0xFFEA4335)),
                                decorationBox = { inner ->
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.CenterStart,
                                    ) {
                                        if (uiState.toQuery.isEmpty()) {
                                            Text("Destination", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f), fontSize = 15.sp)
                                        }
                                        inner()
                                    }
                                },
                            )
                            if (uiState.toQuery.isNotEmpty()) {
                                IconButton(onClick = onClearTo, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Filled.Close, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    // Swap button
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        onClick = onSwap,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    ) {
                        Box(modifier = Modifier.padding(9.dp)) {
                            Icon(Icons.Filled.SwapVert, "Swap", modifier = Modifier.size(20.dp).rotate(swapRotation), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Use My Location
            AnimatedVisibility(
                visible = !uiState.fromIsAutoLocation && !uiState.isResolvingLocation && uiState.fromQuery.isEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onUseLocation() }.padding(vertical = 7.dp, horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.MyLocation, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Use my current location", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }

            // From suggestions
            AnimatedVisibility(visible = uiState.isSearchingFrom && uiState.fromSuggestions.isNotEmpty(), enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column {
                    uiState.fromSuggestions.forEach { s ->
                        StationSuggestionItem(stationName = s.name, zone = s.zone, lines = s.lineIds, isPlace = s.id.startsWith("place:"), onClick = { onFromSelect(s) })
                    }
                }
            }
            // To suggestions
            AnimatedVisibility(visible = uiState.isSearchingTo && uiState.toSuggestions.isNotEmpty(), enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column {
                    uiState.toSuggestions.forEach { s ->
                        StationSuggestionItem(stationName = s.name, zone = s.zone, lines = s.lineIds, isPlace = s.id.startsWith("place:"), onClick = { onToSelect(s) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Preference chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(0.dp)) {
                items(RoutePreference.entries.toList()) { pref ->
                    val sel = uiState.selectedPreference == pref
                    val icon = when (pref) {
                        RoutePreference.FASTEST -> Icons.Filled.FlashOn
                        RoutePreference.FEWEST_CHANGES -> Icons.Filled.SyncAlt
                        RoutePreference.LEAST_WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
                        RoutePreference.STEP_FREE -> Icons.AutoMirrored.Filled.Accessible
                    }
                    FilterChip(
                        selected = sel,
                        onClick = { onSelectPreference(pref) },
                        label = { Text(pref.label, style = MaterialTheme.typography.labelSmall, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = { Icon(icon, null, modifier = Modifier.size(13.dp)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), selectedLabelColor = MaterialTheme.colorScheme.primary, selectedLeadingIconColor = MaterialTheme.colorScheme.primary),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = sel, borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Departure chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(0.dp)) {
                items(DepartureOption.entries.toList()) { opt ->
                    val sel = uiState.departureOption == opt
                    FilterChip(
                        selected = sel,
                        onClick = { onSelectDeparture(opt) },
                        label = { Text(opt.label, style = MaterialTheme.typography.labelSmall, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = { Icon(when (opt) { DepartureOption.LEAVE_NOW -> Icons.Filled.FlashOn; DepartureOption.DEPART_AT -> Icons.Filled.Schedule; DepartureOption.ARRIVE_BY -> Icons.Outlined.Schedule }, null, modifier = Modifier.size(13.dp)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), selectedLabelColor = MaterialTheme.colorScheme.primary, selectedLeadingIconColor = MaterialTheme.colorScheme.primary),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = sel, borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteOptionsSection(
    options: List<RouteOption>,
    selectedIndex: Int,
    onSelectOption: (Int) -> Unit,
    onNavigateToMap: (String, String) -> Unit,
    route: com.londontubeai.navigator.data.model.JourneyRoute?,
) {
    val fastestDuration = options.firstOrNull()?.route?.totalDurationMinutes ?: 0

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp,
    ) {
        Column {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Route options",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${options.size} option${if (options.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)))

            if (options.isEmpty() && route != null) {
                // Fallback: show single route if options list is empty
                RouteOptionCard(
                    option = RouteOption("Fastest", route.totalInterchanges.let { c ->
                        if (c == 0) "Direct" else "$c change${if (c != 1) "s" else ""}"
                    }, route, isRecommended = true),
                    isSelected = true,
                    extraMins = 0,
                    onSelect = {},
                )
            } else {
                options.forEachIndexed { index, option ->
                    RouteOptionCard(
                        option = option,
                        isSelected = index == selectedIndex,
                        extraMins = option.route.totalDurationMinutes - fastestDuration,
                        onSelect = { onSelectOption(index) },
                    )
                    if (index < options.size - 1) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)))
                    }
                }
            }

            // View on Map button at the bottom of the card section
            if (route != null) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToMap(route.fromStation.id, route.toStation.id) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Filled.Place, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "View on Map",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteOptionCard(
    option: RouteOption,
    isSelected: Boolean,
    extraMins: Int,
    onSelect: () -> Unit,
) {
    val route = option.route
    val nowCal = remember { Calendar.getInstance() }
    val arrivalCal = remember(route.totalDurationMinutes) {
        Calendar.getInstance().apply { add(Calendar.MINUTE, route.totalDurationMinutes) }
    }
    val departureTime = String.format("%02d:%02d", nowCal.get(Calendar.HOUR_OF_DAY), nowCal.get(Calendar.MINUTE))
    val arrivalTime = String.format("%02d:%02d", arrivalCal.get(Calendar.HOUR_OF_DAY), arrivalCal.get(Calendar.MINUTE))
    val durationLabel = formatDuration(route.totalDurationMinutes)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.04f) else Color.Transparent)
            .clickable { onSelect() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        // ── Row 1: label + selection indicator ─────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    option.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
                if (option.isRecommended) {
                    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = if (isSelected) 0.15f else 0.08f)) {
                        Text("✓ Best", modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (extraMins > 0) {
                Text("+${extraMins} min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (isSelected) {
                Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Row 2: time range + AI badge + duration pill ────
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "$departureTime – $arrivalTime",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f),
            )
            if (route.aiTimePredictionMinutes > 0 && route.aiTimePredictionMinutes != route.totalDurationMinutes) {
                Surface(shape = RoundedCornerShape(6.dp), color = StatusMinor.copy(alpha = 0.1f)) {
                    Text("AI: ${route.aiTimePredictionMinutes}m", modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = StatusMinor, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(6.dp))
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
            ) {
                Text(durationLabel, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── Row 3: Compact single-line transit strip ─────────
        CompactTransitStrip(legs = route.legs)

        Spacer(modifier = Modifier.height(8.dp))

        // ── Row 4: Info chips ─────────────────────────────────
        val changes = route.totalInterchanges
        val infoItems = buildList {
            add(if (changes == 0) "Direct" else "$changes change${if (changes != 1) "s" else ""}")
            if (route.totalStops > 0) add("${route.totalStops} stops")
            if (route.totalWalkingMinutes > 0) add("${route.totalWalkingMinutes}m walk")
            if (route.co2SavedGrams > 0) add("${route.co2SavedGrams}g CO₂")
        }
        Text(
            infoItems.joinToString("  ·  "),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CompactTransitStrip(legs: List<com.londontubeai.navigator.data.model.JourneyLeg>) {
    val busRed = Color(0xFFE32017)
    val walkGrey = Color(0xFF607D8B)
    // Only show meaningful legs — skip zero-duration walking
    val displayLegs = legs.filter { it.mode != TransportMode.WALKING || it.durationMinutes >= 2 }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.horizontalScroll(rememberScrollState()),
    ) {
        displayLegs.forEachIndexed { index, leg ->
            when (leg.mode) {
                TransportMode.WALKING -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 2.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null, tint = walkGrey, modifier = Modifier.size(13.dp))
                        Text("${leg.durationMinutes}m", style = MaterialTheme.typography.labelSmall, color = walkGrey, fontWeight = FontWeight.Medium)
                    }
                }
                TransportMode.BUS -> {
                    Surface(shape = RoundedCornerShape(5.dp), color = busRed) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.DirectionsBus, null, tint = Color.White, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(leg.busRouteNumber.ifBlank { leg.line.name }.take(5), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }
                }
                TransportMode.TUBE -> {
                    val isLight = leg.line.color == com.londontubeai.navigator.ui.theme.TubeLineColors.Circle ||
                        leg.line.color == com.londontubeai.navigator.ui.theme.TubeLineColors.HammersmithCity ||
                        leg.line.color == com.londontubeai.navigator.ui.theme.TubeLineColors.WaterlooCity
                    Surface(shape = RoundedCornerShape(5.dp), color = leg.line.color) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Train, null, tint = if (isLight) Color.Black else Color.White, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                leg.line.name.split(" ").take(1).joinToString("").take(8),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isLight) Color.Black else Color.White,
                            )
                        }
                    }
                }
            }
            if (index < displayLegs.size - 1) {
                Text(" › ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun RouteTape(route: com.londontubeai.navigator.data.model.JourneyRoute) {
    val totalTime = route.legs.sumOf { it.durationMinutes }.coerceAtLeast(1)
    Column {
        // Colored bar segments
        Row(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))) {
            route.legs.forEachIndexed { idx, leg ->
                val frac = (leg.durationMinutes.toFloat() / totalTime).coerceAtLeast(0.04f)
                val color = when (leg.mode) {
                    TransportMode.WALKING -> Color(0xFFBDBDBD)
                    TransportMode.BUS -> Color(0xFFE53935)
                    TransportMode.TUBE -> leg.line.color
                }
                Box(modifier = Modifier.weight(frac).fillMaxHeight().background(color))
                if (idx < route.legs.size - 1) {
                    Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(MaterialTheme.colorScheme.background))
                }
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        // Line name labels
        Row(modifier = Modifier.fillMaxWidth()) {
            route.legs.filter { it.mode == TransportMode.TUBE }.forEach { leg ->
                val frac = (leg.durationMinutes.toFloat() / totalTime).coerceAtLeast(0.04f)
                Row(modifier = Modifier.weight(frac), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(leg.line.color))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(leg.line.name, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = leg.line.color, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun RouteStat(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(3.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatDivider() {
    Text(" · ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    active: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = if (active) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)) else null,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium, color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun GoogleMapsTimelineCard(route: com.londontubeai.navigator.data.model.JourneyRoute, isInsideLondon: Boolean) {
    val routeKey = "${route.fromStation.id}-${route.toStation.id}-${route.totalDurationMinutes}-${route.legs.size}"
    var expanded by remember(routeKey) { mutableStateOf(true) }
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
            Row(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Route, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Step-by-Step", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("${route.legs.size} legs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }

            AnimatedVisibility(visible = expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    // Origin station dot
                    TimelineDot(name = route.fromStation.name, dotColor = Color(0xFF1A73E8), isOrigin = true)

                    // Each leg
                    route.legs.forEachIndexed { idx, leg ->
                        val isLastLeg = idx == route.legs.size - 1
                        when (leg.mode) {
                            TransportMode.TUBE -> {
                                TimelineSegment(
                                    color = leg.line.color,
                                    label = leg.line.name,
                                    subLabel = "${leg.intermediateStops} stops · ${leg.durationMinutes} min",
                                    extras = buildList {
                                        if (leg.nextDepartureMinutes > 0) add("Next: ${leg.nextDepartureMinutes} min")
                                        if (leg.platformNumber.isNotEmpty()) add(leg.platformNumber)
                                        if (leg.direction.isNotEmpty()) add(leg.direction)
                                    },
                                )
                            }
                            TransportMode.WALKING -> {
                                TimelineSegment(
                                    color = Color(0xFF78909C),
                                    label = "Walk",
                                    subLabel = "${leg.durationMinutes} min${if (leg.walkingDistanceMeters > 0) " · ${leg.walkingDistanceMeters}m" else ""}",
                                    isDashed = true,
                                    extras = if (leg.walkingDirections.isNotEmpty()) listOf(leg.walkingDirections) else emptyList(),
                                )
                            }
                            TransportMode.BUS -> {
                                val busExtras = buildList {
                                    if (leg.busStopName.isNotEmpty()) add("Board: ${leg.busStopName}")
                                    if (leg.busAlightStopName.isNotEmpty()) add("Alight: ${leg.busAlightStopName}")
                                    if (leg.direction.isNotEmpty()) add(leg.direction)
                                    if (leg.intermediateStops > 0) add("${leg.intermediateStops} stops")
                                }
                                TimelineSegment(
                                    color = Color(0xFFE32017),
                                    label = "Bus ${leg.busRouteNumber.ifBlank { leg.line.name }}",
                                    subLabel = "${leg.durationMinutes} min",
                                    extras = busExtras,
                                )
                            }
                        }
                        if (!isLastLeg) {
                            val nextMode = route.legs.getOrNull(idx + 1)?.mode
                            val isInterchange = leg.mode == TransportMode.TUBE && nextMode == TransportMode.TUBE
                            TimelineDot(
                                name = leg.toStation.name,
                                dotColor = if (isInterchange) Color(0xFFFF9800) else MaterialTheme.colorScheme.primary,
                                badge = if (isInterchange) "Change" else null,
                            )
                        }
                    }

                    // Destination dot
                    TimelineDot(name = route.toStation.name, dotColor = Color(0xFFEA4335), isFinal = true)
                }
            }
        }
    }
}

@Composable
private fun TimelineDot(
    name: String,
    dotColor: Color,
    isOrigin: Boolean = false,
    isFinal: Boolean = false,
    badge: String? = null,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(if (isOrigin || isFinal) 13.dp else 10.dp).clip(CircleShape)
                .background(dotColor)
                .then(if (!isOrigin && !isFinal) Modifier.border(2.dp, dotColor.copy(alpha = 0.3f), CircleShape) else Modifier),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isOrigin || isFinal) FontWeight.Bold else FontWeight.SemiBold)
        if (badge != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFF9800).copy(alpha = 0.12f)) {
                Text(badge, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TimelineSegment(
    color: Color,
    label: String,
    subLabel: String,
    isDashed: Boolean = false,
    extras: List<String> = emptyList(),
) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        // Vertical line
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(13.dp)) {
            Box(
                modifier = Modifier.width(3.dp).height(if (extras.isEmpty()) 48.dp else 64.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isDashed) color.copy(alpha = 0.4f) else color),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        // Content
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            color = color.copy(alpha = 0.07f),
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(subLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (extras.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        extras.take(2).forEach { extra ->
                            Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.1f)) {
                                Text(extra, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// HELPER COMPOSABLES
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JourneyDetailsSection(route: JourneyRoute) {
    val primaryLeg = route.legs.firstOrNull()
    val serviceLine = primaryLeg?.line
    val arrivalMinutes = route.aiTimePredictionMinutes.takeIf { it > 0 } ?: route.totalDurationMinutes

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Journey Details",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            JourneyDetailChip(title = "Arrive in", value = "$arrivalMinutes min")
            JourneyDetailChip(
                title = "Boarding",
                value = if (route.totalInterchanges == 0) "Direct service" else "${route.totalInterchanges} change${if (route.totalInterchanges == 1) "" else "s"}",
            )
            if (route.totalWalkingMinutes > 0) {
                JourneyDetailChip(title = "Walking", value = "${route.totalWalkingMinutes} min")
            }
            val firstTubeLeg = route.legs.firstOrNull { it.mode == TransportMode.TUBE }
            if (firstTubeLeg != null && firstTubeLeg.nextDepartureMinutes > 0) {
                JourneyDetailChip(title = "Next train", value = "${firstTubeLeg.nextDepartureMinutes} min")
            }
            JourneyDetailChip(title = "Service every", value = serviceLine?.peakFrequencyMinutes?.let { "$it min" } ?: "Live")
        }

        if (serviceLine != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                JourneyMetaCard(modifier = Modifier.weight(1f), label = "First train", value = serviceLine.firstTrain)
                JourneyMetaCard(modifier = Modifier.weight(1f), label = "Last train", value = serviceLine.lastTrain)
                JourneyMetaCard(modifier = Modifier.weight(1f), label = "Peak gap", value = "${serviceLine.peakFrequencyMinutes}m")
            }
        }
    }
}

@Composable
private fun JourneyDetailChip(title: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun JourneyMetaCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RouteStatPill(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, maxLines = 1)
        }
    }
}

@Composable
private fun StationSuggestionItem(stationName: String, zone: String, lines: List<String> = emptyList(), isPlace: Boolean = false, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                isPressed = true
                onClick()
            }
            .padding(horizontal = 8.dp, vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isPressed) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (isPlace) Icons.Filled.Place else Icons.Filled.DirectionsSubway,
                    contentDescription = null, 
                    tint = if (isPlace) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary, 
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stationName, 
                    style = MaterialTheme.typography.bodyMedium, 
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isPlace) {
                    Text(
                        text = if (lines.isNotEmpty()) lines.take(2).joinToString(" · ") { it.replaceFirstChar { c -> c.uppercase() } } else "Bus / Rail stop",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else if (lines.isNotEmpty()) {
                    Text(
                        text = lines.take(3).joinToString(" · ") { it.replaceFirstChar { c -> c.uppercase() } },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (zone.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isPlace) Color(0xFF4CAF50).copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, if (isPlace) Color(0xFF4CAF50).copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                ) {
                    Text(
                        text = if (isPlace) "Place" else "Zone $zone",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPlace) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SimplifiedLegRow(leg: JourneyLeg) {
    val busColor = Color(0xFFE32017)
    val walkColor = Color(0xFF607D8B)
    val color = when (leg.mode) {
        TransportMode.WALKING -> walkColor
        TransportMode.BUS -> busColor
        else -> leg.line.color
    }
    val icon = when (leg.mode) {
        TransportMode.WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
        TransportMode.BUS -> Icons.Filled.DirectionsBus
        else -> Icons.Filled.Train
    }
    val isLightLine = leg.mode == TransportMode.TUBE && (
        leg.line.color == com.londontubeai.navigator.ui.theme.TubeLineColors.Circle ||
        leg.line.color == com.londontubeai.navigator.ui.theme.TubeLineColors.HammersmithCity ||
        leg.line.color == com.londontubeai.navigator.ui.theme.TubeLineColors.WaterlooCity
    )

    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.05f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            when (leg.mode) {
                TransportMode.BUS -> {
                    Surface(shape = RoundedCornerShape(8.dp), color = busColor) {
                        Text(
                            leg.busRouteNumber.ifBlank { "Bus" },
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                        )
                    }
                }
                TransportMode.TUBE -> {
                    Box(
                        modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(color),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            leg.line.name.take(1),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isLightLine) Color.Black else Color.White,
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(15.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                when (leg.mode) {
                    TransportMode.TUBE -> {
                        Text(
                            "${leg.line.name}  ·  ${leg.direction.removePrefix("Towards ").let { if (it.isNotBlank()) "Towards $it" else leg.direction }.ifBlank { "Towards ${leg.toStation.name}" }}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = color,
                        )
                        if (leg.platformNumber.isNotEmpty()) {
                            Text(
                                "${leg.fromStation.name}  ·  ${leg.platformNumber}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            Text(
                                "Board at ${leg.fromStation.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            "Alight at ${leg.toStation.name}  ·  ${leg.intermediateStops} stop${if (leg.intermediateStops != 1) "s" else ""}  ·  ${leg.durationMinutes} min",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TransportMode.BUS -> {
                        Text(
                            leg.direction.ifBlank { "Towards ${leg.toStation.name}" },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                        )
                        if (leg.busStopName.isNotEmpty()) {
                            Text(
                                "Board at ${leg.busStopName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (leg.busAlightStopName.isNotEmpty()) {
                            Text(
                                "Alight at ${leg.busAlightStopName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (leg.intermediateStops > 0) {
                            Text(
                                "${leg.intermediateStops} stop${if (leg.intermediateStops != 1) "s" else ""}  ·  ${leg.durationMinutes} min",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            Text("${leg.durationMinutes} min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    TransportMode.WALKING -> {
                        Text(
                            "Walk to ${leg.toStation.name}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = walkColor,
                        )
                        val walkDesc = leg.walkingDirections.ifBlank { "Follow station signs" }
                        Text(walkDesc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                        Text(
                            "~${leg.durationMinutes} min${if (leg.walkingDistanceMeters > 0) "  ·  ${leg.walkingDistanceMeters}m" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BusStopChip(label: String, stop: String, color: Color) {
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.08f)) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold, fontSize = 9.sp)
            Text(stop, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun JourneyLegRow(leg: JourneyLeg) {
    val walkingColor = Color(0xFF607D8B)

    when (leg.mode) {
        TransportMode.WALKING -> {
            // Walking interchange segment
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Dotted walking indicator
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(walkingColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = null,
                        tint = walkingColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = leg.walkingDirections.ifEmpty { "Walk to next platform" },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = walkingColor,
                    )
                    if (leg.walkingDistanceMeters > 0) {
                        Text(
                            text = "${leg.walkingDistanceMeters}m · ~${leg.durationMinutes} min walk",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = walkingColor.copy(alpha = 0.1f),
                ) {
                    Text(
                        text = "${leg.durationMinutes}m",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = walkingColor,
                    )
                }
            }
        }

        TransportMode.BUS -> {
            // Bus segment
            val busColor = Color(0xFFE32017)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                color = busColor.copy(alpha = 0.06f),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(busColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.DirectionsBus,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(6.dp), color = busColor) {
                                Text(
                                    text = leg.busRouteNumber.ifBlank { "Bus" },
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = leg.direction.ifBlank { "Towards ${leg.toStation.name}" },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Surface(shape = RoundedCornerShape(10.dp), color = busColor.copy(alpha = 0.12f)) {
                        Text(
                            text = "${leg.durationMinutes}m",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = busColor,
                        )
                    }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (leg.busStopName.isNotEmpty()) {
                            BusStopChip(label = "Board", stop = leg.busStopName, color = busColor)
                        }
                        if (leg.busAlightStopName.isNotEmpty()) {
                            BusStopChip(label = "Alight", stop = leg.busAlightStopName, color = busColor)
                        }
                    }
                    if (leg.intermediateStops > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${leg.intermediateStops} stop${if (leg.intermediateStops != 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        TransportMode.TUBE -> {
            // Tube segment with enhanced info
            val isLightLine = leg.line.color == com.londontubeai.navigator.ui.theme.TubeLineColors.Circle ||
                leg.line.color == com.londontubeai.navigator.ui.theme.TubeLineColors.HammersmithCity ||
                leg.line.color == com.londontubeai.navigator.ui.theme.TubeLineColors.WaterlooCity

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                color = leg.line.color.copy(alpha = 0.06f),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(leg.line.color),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = leg.line.name.take(1),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isLightLine) Color.Black else Color.White,
                                fontSize = 11.sp,
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${leg.fromStation.name} → ${leg.toStation.name}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = leg.line.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = leg.line.color,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = " · ${leg.intermediateStops} stops",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = leg.line.color.copy(alpha = 0.15f),
                        ) {
                            Text(
                                text = "${leg.durationMinutes}m",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = leg.line.color,
                            )
                        }
                    }

                    // Info chips row: next train, platform, direction
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (leg.nextDepartureMinutes > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StatusGood.copy(alpha = 0.12f),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Filled.Schedule, null, tint = StatusGood, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Next: ${leg.nextDepartureMinutes} min",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StatusGood,
                                    )
                                }
                            }
                        }
                        if (leg.platformNumber.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = leg.line.color.copy(alpha = 0.1f),
                            ) {
                                Text(
                                    text = leg.platformNumber,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = leg.line.color,
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        ) {
                            Text(
                                text = leg.direction,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NearbyBusesSection(
    busRoutes: List<NearbyBusRoute>,
    isLoading: Boolean,
) {
    val BusRed = Color(0xFFE32017)
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
                        .background(BusRed.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.DirectionsBus, null, tint = BusRed, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Buses from here", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text("Live departures near your location", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = BusRed)
                } else {
                    Surface(shape = RoundedCornerShape(6.dp), color = BusRed.copy(alpha = 0.1f)) {
                        Text(
                            "● Live",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BusRed,
                        )
                    }
                }
            }

            if (busRoutes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                busRoutes.forEach { bus ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(shape = RoundedCornerShape(8.dp), color = BusRed) {
                                Text(
                                    bus.busNumber,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(bus.direction, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(bus.stopName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Surface(shape = RoundedCornerShape(8.dp), color = when {
                                bus.estimatedMinutes <= 1 -> StatusGood.copy(alpha = 0.15f)
                                bus.estimatedMinutes <= 5 -> StatusMinor.copy(alpha = 0.15f)
                                else -> BusRed.copy(alpha = 0.1f)
                            }) {
                                Text(
                                    if (bus.estimatedMinutes <= 0) "Due" else "${bus.estimatedMinutes} min",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        bus.estimatedMinutes <= 1 -> StatusGood
                                        bus.estimatedMinutes <= 5 -> StatusMinor
                                        else -> BusRed
                                    },
                                )
                            }
                        }
                    }
                }
            } else if (!isLoading) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "No live buses found within 400m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0 -> "$m min"
        m == 0 -> "$h hr"
        else -> "$h hr $m min"
    }
}


@Composable
private fun crowdColor(level: CrowdLevel): Color = when (level) {
    CrowdLevel.LOW -> StatusGood
    CrowdLevel.MODERATE -> StatusGood
    CrowdLevel.HIGH -> StatusMinor
    CrowdLevel.VERY_HIGH -> StatusSevere
    CrowdLevel.EXTREME -> StatusSevere
}
