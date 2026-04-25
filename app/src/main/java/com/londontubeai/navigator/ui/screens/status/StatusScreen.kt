package com.londontubeai.navigator.ui.screens.status

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Intent
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.londontubeai.navigator.data.model.AiInsight
import com.londontubeai.navigator.data.model.LineBranchDetail
import com.londontubeai.navigator.data.model.LineDetail
import com.londontubeai.navigator.data.model.LineStationStop
import com.londontubeai.navigator.data.model.LineStatus
import com.londontubeai.navigator.ui.theme.Spacing
import com.londontubeai.navigator.ui.theme.StatusGood
import com.londontubeai.navigator.ui.theme.StatusMinor
import com.londontubeai.navigator.ui.theme.StatusSevere
import com.londontubeai.navigator.ui.theme.TubeLineColors
import com.londontubeai.navigator.ui.theme.TubePrimary
import com.londontubeai.navigator.ui.components.UnifiedHeader
import java.util.Calendar

// ════════════════════════════════════════════════════════════
//  Status Screen
// ════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StatusScreen(viewModel: StatusViewModel = hiltViewModel()) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredStatuses by viewModel.filteredStatuses.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val bannerMessage = when {
        uiState.error != null -> uiState.error
        uiState.isUsingCachedData -> "Showing cached data while offline."
        else -> null
    }

    // Pulsing dot animation
    val pulse = rememberInfiniteTransition(label = "pulse")
    val liveAlpha by pulse.animateFloat(
        initialValue = .35f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "liveAlpha",
    )

    Column(Modifier.fillMaxSize()) {

        // ── Header ──────────────────────────────────────────
        StatusHeader(uiState = uiState, liveAlpha = liveAlpha, onRefresh = { viewModel.loadStatuses() })

        // ── List ────────────────────────────────────────────
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.loadStatuses() },
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                uiState.error != null && uiState.lineStatuses.isEmpty() ->
                    ErrorState(uiState.error!!) { viewModel.loadStatuses() }

                uiState.isLoading && uiState.lineStatuses.isEmpty() ->
                    LoadingState()

                else -> {
                    val disruptedStatuses = filteredStatuses.filter { !it.status.isGoodService }
                    val goodStatuses = filteredStatuses.filter { it.status.isGoodService }
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        stickyHeader(key = "controls") {
                            StatusControlsStickyHeader(
                                query = uiState.searchQuery,
                                onQueryChange = { viewModel.updateSearchQuery(it) },
                                onClear = { viewModel.updateSearchQuery(""); focusManager.clearFocus() },
                                onSearch = { focusManager.clearFocus() },
                                selected = uiState.filter,
                                disruptedCount = uiState.disruptedCount,
                                onSelect = { viewModel.setFilter(it) },
                                bannerMessage = bannerMessage,
                                isCached = uiState.isUsingCachedData,
                                lastUpdated = uiState.lastUpdated,
                                onRetry = { viewModel.loadStatuses() },
                            )
                        }

                        // Network health + AI insights — only shown on the ALL filter
                        // so it does not distract from focused views (disrupted / favourites).
                        if (uiState.filter == StatusFilter.ALL && uiState.totalCount > 0) {
                            item(key = "network-health") {
                                NetworkHealthInsightsCard(
                                    healthPercent = uiState.healthPercent,
                                    disruptedCount = uiState.disruptedCount,
                                    totalCount = uiState.totalCount,
                                    insights = uiState.aiInsights.take(3),
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                        .animateItem(),
                                )
                            }
                        }

                        if (filteredStatuses.isEmpty()) {
                            item(key = "empty") {
                                EmptyState(
                                    when (uiState.filter) {
                                        StatusFilter.DISRUPTED -> "All lines running smoothly"
                                        StatusFilter.FAVORITES -> "Tap ♡ on any line to add favourites"
                                        else -> "No lines match your search"
                                    },
                                    uiState.filter == StatusFilter.DISRUPTED,
                                )
                            }
                        }

                        if (disruptedStatuses.isNotEmpty()) {
                            stickyHeader(key = "disrupted-header") {
                                SectionHeader(
                                    label = "NEEDS ATTENTION",
                                    count = disruptedStatuses.size,
                                    accentColor = StatusSevere,
                                )
                            }
                            itemsIndexed(disruptedStatuses, key = { _, s -> "d_${s.status.lineId}" }) { _, status ->
                                LineStatusCard(
                                    uiModel = status,
                                    isExpanded = status.status.lineId == uiState.expandedLineId,
                                    onToggleExpand = { viewModel.toggleCardExpansion(status.status.lineId) },
                                    onToggleFavourite = { viewModel.toggleFavourite(status.status.lineId) },
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                        .animateItem(),
                                )
                            }
                        }

                        if (goodStatuses.isNotEmpty()) {
                            stickyHeader(key = "good-header") {
                                SectionHeader(
                                    label = "GOOD SERVICE",
                                    count = goodStatuses.size,
                                    accentColor = StatusGood,
                                )
                            }
                            itemsIndexed(goodStatuses, key = { _, s -> "g_${s.status.lineId}" }) { _, status ->
                                LineStatusCard(
                                    uiModel = status,
                                    isExpanded = status.status.lineId == uiState.expandedLineId,
                                    onToggleExpand = { viewModel.toggleCardExpansion(status.status.lineId) },
                                    onToggleFavourite = { viewModel.toggleFavourite(status.status.lineId) },
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                        .animateItem(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusControlsStickyHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearch: () -> Unit,
    selected: StatusFilter,
    disruptedCount: Int,
    onSelect: (StatusFilter) -> Unit,
    bannerMessage: String?,
    isCached: Boolean,
    lastUpdated: String?,
    onRetry: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(top = 4.dp, bottom = 6.dp),
        ) {
            SearchBar(
                query = query,
                onQueryChange = onQueryChange,
                onClear = onClear,
                onSearch = onSearch,
            )
            FilterRow(
                selected = selected,
                disruptedCount = disruptedCount,
                onSelect = onSelect,
            )
            if (bannerMessage != null) {
                DataSourceBanner(
                    message = bannerMessage,
                    isCached = isCached,
                    lastUpdated = lastUpdated,
                    onRetry = onRetry,
                )
            }
        }
    }
}

@Composable
private fun DataSourceBanner(
    message: String,
    isCached: Boolean,
    lastUpdated: String?,
    onRetry: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (isCached) StatusMinor.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isCached) Icons.Filled.Info else Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = if (isCached) StatusMinor else StatusGood,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isCached) "Offline snapshot" else "Status update",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (lastUpdated != null) "$message Last updated $lastUpdated." else message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

// ════════════════════════════════════════════════════════════
//  Header
// ════════════════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatusHeader(
    uiState: StatusUiState,
    liveAlpha: Float,
    onRefresh: () -> Unit,
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val title = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Network status"
        in 17..21 -> "Evening commute"
        else -> "Late-night status"
    }
    UnifiedHeader(
        title = title,
        subtitle = when {
            uiState.isUsingCachedData && uiState.lastUpdated != null -> "Offline snapshot from ${uiState.lastUpdated}"
            uiState.disruptedCount > 0 && uiState.lastUpdated != null -> "${uiState.disruptedCount} lines need attention · Updated ${uiState.lastUpdated}"
            uiState.lastUpdated != null -> "All clear · Updated ${uiState.lastUpdated}"
            else -> "Loading live line health"
        },
        icon = Icons.Filled.Speed,
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (uiState.isLiveConnection) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(StatusGood.copy(alpha = liveAlpha))
                    )
                    Spacer(Modifier.width(8.dp))
                }
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Filled.Refresh, "Refresh", tint = Color.White.copy(alpha = 0.7f))
                }
            }
        },
        bottomContent = {
            if (uiState.totalCount > 0) {
                Spacer(Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatBadge(
                        label = "${uiState.goodCount} Good",
                        color = StatusGood,
                    )
                    if (uiState.disruptedCount > 0) {
                        StatBadge(
                            label = "${uiState.disruptedCount} Need attention",
                            color = StatusSevere,
                        )
                    }
                    StatBadge(
                        label = "Impact: ${systemImpactLabel(uiState)}",
                        color = when {
                            uiState.healthPercent >= 80 -> StatusGood
                            uiState.healthPercent >= 60 -> StatusMinor
                            else -> StatusSevere
                        },
                    )
                }
            }
        },
    )
}

private fun systemImpactLabel(uiState: StatusUiState): String {
    return when {
        uiState.disruptedCount == 0 -> "Low"
        uiState.disruptedCount <= 2 -> "Moderate"
        uiState.disruptedCount <= 4 -> "High"
        else -> "Severe"
    }
}

@Composable
private fun StatBadge(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.16f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

// ════════════════════════════════════════════════════════════
//  Network Health Ring + AI Insights Card
// ════════════════════════════════════════════════════════════

@Composable
private fun NetworkHealthInsightsCard(
    healthPercent: Int,
    disruptedCount: Int,
    totalCount: Int,
    insights: List<AiInsight>,
    modifier: Modifier = Modifier,
) {
    val ringColor = when {
        healthPercent >= 80 -> StatusGood
        healthPercent >= 60 -> StatusMinor
        else -> StatusSevere
    }
    val animatedHealth by animateFloatAsState(
        targetValue = healthPercent / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "healthRing",
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        ),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Circular health ring
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = ringColor.copy(alpha = 0.15f),
                        strokeWidth = 6.dp,
                        trackColor = Color.Transparent,
                    )
                    CircularProgressIndicator(
                        progress = { animatedHealth },
                        modifier = Modifier.fillMaxSize(),
                        color = ringColor,
                        strokeWidth = 6.dp,
                        trackColor = Color.Transparent,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$healthPercent%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = ringColor,
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Network health",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        if (disruptedCount == 0) "All $totalCount lines in good service"
                        else "$disruptedCount of $totalCount lines disrupted",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ringColor.copy(alpha = 0.12f),
                    ) {
                        Text(
                            when {
                                healthPercent >= 90 -> "Running smoothly"
                                healthPercent >= 70 -> "Mostly on time"
                                healthPercent >= 50 -> "Some disruption"
                                else -> "Significant disruption"
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ringColor,
                        )
                    }
                }
            }

            // AI insights list
            if (insights.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                androidx.compose.material3.HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Lightbulb,
                        null,
                        tint = TubePrimary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "AI insights",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TubePrimary,
                    )
                }
                Spacer(Modifier.height(8.dp))
                insights.forEach { insight ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(TubePrimary),
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                insight.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                insight.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════
//  Search Bar
// ════════════════════════════════════════════════════════════

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearch: () -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search lines or stations...") },
        leadingIcon = { Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Filled.Close, "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f),
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
    )
}

// ════════════════════════════════════════════════════════════
//  Section Header
// ════════════════════════════════════════════════════════════

@Composable
private fun SectionHeader(label: String, count: Int, accentColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp,
            )
            Text(
                text = "$count ${if (count == 1) "line" else "lines"}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = accentColor,
            )
        }
    }
}

// ════════════════════════════════════════════════════════════
//  Filter Row
// ════════════════════════════════════════════════════════════

@Composable
private fun FilterRow(
    selected: StatusFilter,
    disruptedCount: Int,
    onSelect: (StatusFilter) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 4.dp),
    ) {
        itemsIndexed(StatusFilter.entries.toList()) { _, filter ->
            val label = if (filter == StatusFilter.DISRUPTED && disruptedCount > 0)
                "${filter.label} ($disruptedCount)" else filter.label
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = {
                    Text(label, style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected == filter) FontWeight.Bold else FontWeight.Normal)
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TubePrimary,
                    selectedLabelColor = Color.White,
                ),
            )
        }
    }
}

// ════════════════════════════════════════════════════════════
//  Line Status Card
// ════════════════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LineStatusCard(
    uiModel: LineStatusUiModel,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleFavourite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val status = uiModel.status
    val lineDetail = uiModel.lineDetail
    val statusColor by animateColorAsState(
        when {
            status.statusSeverity >= 10 -> StatusGood
            status.statusSeverity >= 5 -> StatusMinor
            else -> StatusSevere
        }, label = "statusColor",
    )
    val favouriteScale by animateFloatAsState(
        targetValue = if (uiModel.isFavourite) 1.15f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "favouriteScale",
    )
    val textOnLineColor = if (
        status.lineColor == TubeLineColors.Circle ||
        status.lineColor == TubeLineColors.WaterlooCity
    ) Color.Black else Color.White
    var showAllStations by remember { mutableStateOf(false) }
    // Branch picker index — defaults to the first branch when the line has
    // multiple routes (e.g. Northern: via-Bank vs via-Charing-Cross).
    var selectedBranchIndex by remember(status.lineId) { mutableStateOf(0) }
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (isExpanded) 4.dp else 1.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        ),
    ) {
        Column(Modifier.fillMaxWidth()) {
            // ── Tap row ─────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleExpand()
                    }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Line badge circle
                Box(
                    Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .border(2.dp, status.lineColor, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        uiModel.lineBadgeLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = status.lineColor,
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        status.lineName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                    if (status.isGoodService) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(StatusGood))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Good service",
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusGood,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    } else {
                        StatusBadgePill(uiModel.statusBadge, statusColor)
                    }
                    // ML-predicted delay chip — only surface when meaningful (≥2 min)
                    if (uiModel.expectedDelayMinutes >= 2) {
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = StatusSevere.copy(alpha = 0.12f),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Filled.Warning, null, tint = StatusSevere, modifier = Modifier.size(11.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "AI forecast: ~+${uiModel.expectedDelayMinutes}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusSevere,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                )
                            }
                        }
                    }
                }
                // Commute / favourite context
                if (uiModel.affectsCommute || uiModel.isFavourite) {
                    Column(horizontalAlignment = Alignment.End) {
                        if (uiModel.affectsCommute) {
                            StatusContextPill("Commute", TubePrimary)
                        }
                        if (uiModel.isFavourite) {
                            Spacer(Modifier.height(4.dp))
                            StatusContextPill("Saved", StatusMinor)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                }
                IconButton(onClick = onToggleFavourite, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (uiModel.isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favourite",
                        modifier = Modifier.size(20.dp).scale(favouriteScale),
                        tint = if (uiModel.isFavourite) StatusSevere else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // ── Disruption box (collapsed) ───────────────────
            if (!status.isGoodService && !isExpanded) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = StatusSevere.copy(alpha = 0.10f),
                ) {
                    Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                "! ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = StatusSevere,
                            )
                            Text(
                                uiModel.disruptionSummary ?: uiModel.humanStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusSevere,
                                lineHeight = 18.sp,
                            )
                        }
                        if (uiModel.estimatedResolution != null) {
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AccessTime, null, Modifier.size(12.dp), tint = StatusMinor)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    uiModel.estimatedResolution,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusMinor,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }

            // ── Expanded detail ──────────────────────────────
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)),
                exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium)),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // DISRUPTION section
                    if (!status.isGoodService) {
                        Column {
                            SectionLabel("DISRUPTION")
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = StatusSevere.copy(alpha = 0.10f),
                            ) {
                                Text(
                                    text = if (!status.reason.isNullOrBlank()) status.reason
                                           else uiModel.disruptionSummary ?: uiModel.humanStatus,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusSevere,
                                    lineHeight = 20.sp,
                                )
                            }
                            if (uiModel.estimatedResolution != null) {
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AccessTime, null, Modifier.size(14.dp), tint = StatusMinor)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "Resuming · ${uiModel.estimatedResolution}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StatusMinor,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }

                    // ROUTE section
                    if (lineDetail != null) {
                        Column {
                            SectionLabel("ROUTE")
                            Spacer(Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    lineDetail.firstStation,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(Modifier.width(8.dp))
                                // Colored line bar
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(status.lineColor),
                                )
                                Icon(
                                    Icons.Filled.SyncAlt,
                                    null,
                                    Modifier.size(14.dp),
                                    tint = status.lineColor,
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(status.lineColor),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    lineDetail.lastStation,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${lineDetail.stationCount} stations · ${lineDetail.totalLengthKm} km",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // TIMINGS section
                        Column {
                            SectionLabel("TIMINGS")
                            Spacer(Modifier.height(8.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                TimingPill(
                                    value = "${lineDetail.firstTrain}–${lineDetail.lastTrain}",
                                    label = "Operating hours",
                                    modifier = Modifier.weight(1f),
                                )
                                TimingPill(
                                    value = "${lineDetail.peakFrequencyMin} / ${lineDetail.offPeakFrequencyMin} min",
                                    label = "Peak / off-peak",
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            // Accessibility pill — surfaces step-free station coverage
                            // which is critical info for wheelchair users and buggies.
                            if (lineDetail.stepFreeStationCount > 0) {
                                Spacer(Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = StatusGood.copy(alpha = 0.12f),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            Icons.Filled.Accessible, null,
                                            tint = StatusGood, modifier = Modifier.size(16.dp),
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "${lineDetail.stepFreeStationCount} of ${lineDetail.stationCount} stations are step-free",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = StatusGood,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                        }

                        // CONNECTS WITH section
                        if (lineDetail.connectingLines.isNotEmpty()) {
                            Column {
                                SectionLabel("CONNECTS WITH")
                                Spacer(Modifier.height(8.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    lineDetail.connectingLines.forEach { conn ->
                                        ConnectingLinePill(name = conn.name, color = conn.color)
                                    }
                                }
                            }
                        }

                        // VIEW ALL STATIONS toggle button
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAllStations = !showAllStations }
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    if (showAllStations) "Hide stations" else "View all ${lineDetail.stationCount} stations",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    if (showAllStations) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        AnimatedVisibility(visible = showAllStations) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 8.dp)
                                    .padding(bottom = 8.dp),
                            ) {
                                // Branch picker — only rendered when the line has
                                // 2+ routes. Clicking a chip swaps the station list
                                // in-place so users can inspect each terminal.
                                if (lineDetail.branches.size >= 2) {
                                    Text(
                                        "ROUTES",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 0.8.sp,
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        lineDetail.branches.forEachIndexed { idx, branch ->
                                            val selected = idx == selectedBranchIndex
                                            Surface(
                                                modifier = Modifier.clickable {
                                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    selectedBranchIndex = idx
                                                },
                                                shape = RoundedCornerShape(999.dp),
                                                color = if (selected) status.lineColor.copy(alpha = 0.18f)
                                                        else MaterialTheme.colorScheme.surfaceVariant,
                                                border = if (selected) androidx.compose.foundation.BorderStroke(
                                                    1.5.dp, status.lineColor,
                                                ) else null,
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Box(
                                                        Modifier.size(8.dp).clip(CircleShape).background(status.lineColor),
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        branch.branchName,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (selected) status.lineColor
                                                                else MaterialTheme.colorScheme.onSurface,
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        "${branch.orderedStations.size}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(12.dp))
                                }

                                // Render either the selected branch's stations or
                                // fall back to the full flat list.
                                val stationsToShow: List<LineStationStop> =
                                    if (lineDetail.branches.size >= 2) {
                                        lineDetail.branches
                                            .getOrNull(selectedBranchIndex)
                                            ?.orderedStations
                                            ?: lineDetail.orderedStations
                                    } else lineDetail.orderedStations

                                OrderedStationsHierarchy(
                                    stations = stationsToShow,
                                    lineColor = status.lineColor,
                                )
                            }
                        }

                        // Share status — lets users quickly forward the current
                        // line state to colleagues or family via any share target
                        // (Messages, WhatsApp, email, etc.). Modern expectation.
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val body = buildString {
                                        append("🚇 ${status.lineName} line — ")
                                        append(if (status.isGoodService) "Good service" else uiModel.statusBadge)
                                        status.reason?.takeIf { it.isNotBlank() }?.let {
                                            append("\n\n$it")
                                        }
                                        append("\n\nShared from AI Tube Navigator")
                                    }
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "${status.lineName} line status")
                                        putExtra(Intent.EXTRA_TEXT, body)
                                    }
                                    runCatching {
                                        context.startActivity(
                                            Intent.createChooser(sendIntent, "Share line status")
                                        )
                                    }
                                }
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Filled.Share, null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Share status",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.8.sp,
    )
}

@Composable
private fun TimingPill(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ConnectingLinePill(name: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            Text(name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun StatusBadgePill(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

@Composable
private fun StatusContextPill(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OrderedStationsHierarchy(
    stations: List<LineStationStop>,
    lineColor: Color,
    compact: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp)) {
        stations.forEachIndexed { index, station ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(if (compact) 10.dp else 12.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    station.isTerminal -> lineColor
                                    station.isInterchange -> lineColor.copy(alpha = 0.85f)
                                    else -> lineColor.copy(alpha = 0.32f)
                                }
                            ),
                    )
                    if (index < stations.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(if (compact) 20.dp else 24.dp)
                                .background(lineColor.copy(alpha = 0.28f)),
                        )
                    }
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = station.stationName,
                            modifier = Modifier.weight(1f),
                            style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                            fontWeight = if (station.isTerminal || station.isInterchange) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (station.hasStepFreeAccess) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "♿",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }

                    Spacer(Modifier.height(2.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        StationMetaPill(
                            text = if (station.isTerminal) "Terminal" else "Zone ${station.zone}",
                            color = if (station.isTerminal) getReadableTextColor(lineColor) else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (station.isInterchange) {
                            StationMetaPill(
                                text = station.connectingLineNames.joinToString(", "),
                                color = getReadableTextColor(lineColor),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StationMetaPill(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.12f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun getReadableTextColor(lineColor: Color): Color {
    // For light-colored lines (Circle, Hammersmith & City, etc.), use darker text
    // For dark-colored lines, use the line color itself
    return when {
        lineColor == TubeLineColors.Circle -> Color(0xFFCC9900) // Darker yellow
        lineColor == TubeLineColors.HammersmithCity -> Color(0xFFCC6699) // Darker pink  
        lineColor == TubeLineColors.Victoria -> Color(0xFF0076A3) // Darker blue
        lineColor == TubeLineColors.WaterlooCity -> Color(0xFF6BA894) // Darker teal
        lineColor == TubeLineColors.Overground -> Color(0xFFCC6600) // Darker orange
        else -> lineColor
    }
}

// ════════════════════════════════════════════════════════════
//  Reusable detail helpers
// ════════════════════════════════════════════════════════════

@Composable
private fun DetailSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun InfoPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f),
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(6.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}

// ════════════════════════════════════════════════════════════
//  State screens
// ════════════════════════════════════════════════════════════

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
            Icon(Icons.Filled.ErrorOutline, null, Modifier.size(56.dp), tint = StatusSevere.copy(.4f))
            Spacer(Modifier.height(16.dp))
            Text("Something went wrong", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Button(onClick = onRetry, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Filled.Refresh, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(6) { SkeletonCard() }
    }
}

@Composable
private fun SkeletonCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "shimmerAlpha",
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                Modifier.size(46.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = shimmerAlpha))
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.width(140.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = shimmerAlpha)))
                Box(Modifier.width(90.dp).height(11.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = shimmerAlpha * 0.7f)))
            }
        }
    }
}

@Composable
private fun EmptyState(message: String, isAllClear: Boolean = false) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 56.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = if (isAllClear) StatusGood.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(72.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isAllClear) Icons.Filled.CheckCircle else Icons.Filled.Search,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = if (isAllClear) StatusGood else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (isAllClear) "All lines running smoothly" else message,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (isAllClear) "No disruptions reported across the network" else message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
