package com.londontubeai.navigator.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.londontubeai.navigator.ui.theme.StatusGood
import com.londontubeai.navigator.ui.theme.TubeAccent
import com.londontubeai.navigator.ui.theme.TubePrimary
import com.londontubeai.navigator.ui.theme.TubeSecondary
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.londontubeai.navigator.R
import com.londontubeai.navigator.data.model.Station
import com.londontubeai.navigator.data.model.TubeData
import com.londontubeai.navigator.ui.components.SystemBarsEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

// ─── Data Models ──────────────────────────────────────────────

private data class OnboardingPage(
    val icon: ImageVector,
    val accentColor: Color,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    @StringRes val descriptionRes: Int,
    val benefits: List<Benefit>,
    val gradientStart: Color,
    val gradientEnd: Color,
)

private data class Benefit(
    val icon: ImageVector,
    @StringRes val labelRes: Int,
    @StringRes val detailRes: Int,
)

// ─── Page Content ─────────────────────────────────────────────

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Filled.DirectionsSubway,
        accentColor = TubePrimary,
        titleRes = R.string.onboarding_welcome_title,
        subtitleRes = R.string.onboarding_welcome_sub,
        descriptionRes = R.string.onboarding_welcome_desc,
        benefits = listOf(
            Benefit(Icons.Filled.Route, R.string.benefit_ai_routes, R.string.benefit_ai_routes_desc),
            Benefit(Icons.Filled.Notifications, R.string.benefit_alerts, R.string.benefit_alerts_desc),
            Benefit(Icons.Filled.Lightbulb, R.string.benefit_smart_tips, R.string.benefit_smart_tips_desc),
        ),
        gradientStart = TubePrimary,
        gradientEnd = TubeAccent,
    ),
    OnboardingPage(
        icon = Icons.Filled.Route,
        accentColor = StatusGood,
        titleRes = R.string.onboarding_smart_title,
        subtitleRes = R.string.onboarding_smart_sub,
        descriptionRes = R.string.onboarding_smart_desc,
        benefits = listOf(
            Benefit(Icons.Filled.AccessTime, R.string.benefit_peak_avoidance, R.string.benefit_peak_avoidance_desc),
            Benefit(Icons.Filled.LocationOn, R.string.benefit_exit_guidance, R.string.benefit_exit_guidance_desc),
            Benefit(Icons.Filled.Timer, R.string.benefit_time_saved, R.string.benefit_time_saved_desc),
        ),
        gradientStart = StatusGood,
        gradientEnd = TubePrimary,
    ),
    OnboardingPage(
        icon = Icons.Filled.Map,
        accentColor = TubeSecondary,
        titleRes = R.string.onboarding_map_title,
        subtitleRes = R.string.onboarding_map_sub,
        descriptionRes = R.string.onboarding_map_desc,
        benefits = listOf(
            Benefit(Icons.Filled.Wifi, R.string.benefit_realtime, R.string.benefit_realtime_desc),
            Benefit(Icons.Filled.Info, R.string.benefit_station_info, R.string.benefit_station_info_desc),
            Benefit(Icons.Filled.FilterList, R.string.benefit_smart_filter, R.string.benefit_smart_filter_desc),
        ),
        gradientStart = TubeSecondary,
        gradientEnd = TubeAccent,
    ),
    OnboardingPage(
        icon = Icons.Filled.CloudOff,
        accentColor = Color(0xFF6C63FF),
        titleRes = R.string.onboarding_offline_title,
        subtitleRes = R.string.onboarding_offline_sub,
        descriptionRes = R.string.onboarding_offline_desc,
        benefits = listOf(
            Benefit(Icons.Filled.Storage, R.string.benefit_offline_data, R.string.benefit_offline_data_desc),
            Benefit(Icons.Filled.Sync, R.string.benefit_auto_switch, R.string.benefit_auto_switch_desc),
            Benefit(Icons.Filled.SignalWifiOff, R.string.benefit_tunnel_ready, R.string.benefit_tunnel_ready_desc),
        ),
        gradientStart = Color(0xFF6C63FF),
        gradientEnd = StatusGood,
    ),
)

// ─── Step model ───────────────────────────────────────────────
//
// We treat each onboarding slide as an `OnboardingStep` so we can mix
// passive info slides with interactive setup slides (commute capture,
// accessibility) without forking the navigation logic. Each step still
// carries the gradient + accent so the colour journey across the flow
// stays continuous.

private sealed class OnboardingStep(
    val accentColor: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
) {
    class Info(val page: OnboardingPage) :
        OnboardingStep(page.accentColor, page.gradientStart, page.gradientEnd)

    /** Pick a Home + Work station to unlock the Quick Commute card on Home. */
    object Commute : OnboardingStep(
        accentColor = TubePrimary,
        gradientStart = TubePrimary,
        gradientEnd = TubeSecondary,
    )

    /** Toggle step-free routing preference. Persists across the whole app. */
    object Accessibility : OnboardingStep(
        accentColor = StatusGood,
        gradientStart = StatusGood,
        gradientEnd = TubePrimary,
    )
}

private val onboardingSteps: List<OnboardingStep> = listOf(
    OnboardingStep.Info(pages[0]),  // Welcome
    OnboardingStep.Info(pages[1]),  // Smart Routes
    OnboardingStep.Info(pages[2]),  // Live Map
    OnboardingStep.Commute,
    OnboardingStep.Accessibility,
    OnboardingStep.Info(pages[3]),  // Works Underground (closing slide)
)

// ─── Main Composable ──────────────────────────────────────────

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    var currentPage by remember { mutableIntStateOf(0) }
    var dragOffset by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()
    val step = onboardingSteps[currentPage]

    // Onboarding renders dark gradient pages → keep status & nav bar icons
    // light so the system clock / battery / signal stay readable.
    SystemBarsEffect(lightBackground = false)

    val progress by animateFloatAsState(
        targetValue = (currentPage + 1).toFloat() / onboardingSteps.size,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "progress",
    )

    /**
     * Saves any captured setup choices then exits. Always called when the
     * user finishes onboarding (final "Continue" or any "Skip"), so we
     * persist whatever's been entered up to that point.
     */
    val finishOnboarding: () -> Unit = {
        scope.launch {
            viewModel.saveSetup()
            onComplete()
        }
        Unit
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        step.gradientStart.copy(alpha = 0.92f),
                        step.gradientEnd.copy(alpha = 0.75f),
                        Color(0xFF0A1628),
                    ),
                ),
            )
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (abs(dragOffset) > 80) {
                            if (dragOffset < 0 && currentPage < onboardingSteps.size - 1) currentPage++
                            else if (dragOffset > 0 && currentPage > 0) currentPage--
                        }
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { _, delta -> dragOffset += delta },
                )
            },
    ) {
        // Subtle decorative circles
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Color.White.copy(alpha = 0.03f), 180.dp.toPx(), Offset(size.width * 0.82f, size.height * 0.12f))
            drawCircle(Color.White.copy(alpha = 0.02f), 120.dp.toPx(), Offset(size.width * 0.12f, size.height * 0.78f))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── Progress bar + counter ────────────────────────
            ProgressSection(progress = progress, current = currentPage, total = onboardingSteps.size)

            Spacer(modifier = Modifier.weight(1f))

            // ── Animated step content ─────────────────────────
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    (slideInHorizontally { it / 2 } + fadeIn(tween(350))) togetherWith
                            (slideOutHorizontally { -it / 2 } + fadeOut(tween(250)))
                },
                label = "step",
            ) { idx ->
                when (val s = onboardingSteps[idx]) {
                    is OnboardingStep.Info -> PageContent(page = s.page)
                    OnboardingStep.Commute -> CommuteSetupContent(viewModel)
                    OnboardingStep.Accessibility -> AccessibilitySetupContent(viewModel)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Navigation ────────────────────────────────────
            NavigationSection(
                currentPage = currentPage,
                totalPages = onboardingSteps.size,
                accentColor = step.accentColor,
                onNext = {
                    if (currentPage < onboardingSteps.size - 1) currentPage++ else finishOnboarding()
                },
                onBack = { if (currentPage > 0) currentPage-- },
                onSkip = finishOnboarding,
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// ─── Progress ─────────────────────────────────────────────────

@Composable
private fun ProgressSection(progress: Float, current: Int, total: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "${current + 1} of $total",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.15f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White),
            )
        }
    }
}

// ─── Page Content ─────────────────────────────────────────────

@Composable
private fun PageContent(page: OnboardingPage) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(page.accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(page.accentColor.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(page.subtitleRes),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(page.descriptionRes),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.65f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Benefits
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            page.benefits.forEachIndexed { i, b ->
                BenefitRow(benefit = b, accentColor = page.accentColor, delayMs = i * 80)
            }
        }
    }
}

// ─── Benefit Row ──────────────────────────────────────────────

@Composable
private fun BenefitRow(benefit: Benefit, accentColor: Color, delayMs: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(delayMs.toLong()); visible = true }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500),
        label = "bAlpha",
    )
    val slide by animateFloatAsState(
        targetValue = if (visible) 0f else 16f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "bSlide",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = slide.dp)
            .background(Color.White.copy(alpha = 0.08f * alpha), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = benefit.icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = alpha),
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = stringResource(benefit.labelRes),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = alpha),
            )
            Text(
                text = stringResource(benefit.detailRes),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f * alpha),
            )
        }
    }
}

// ─── Navigation ───────────────────────────────────────────────

@Composable
private fun NavigationSection(
    currentPage: Int,
    totalPages: Int,
    accentColor: Color,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit,
) {
    val isLast = currentPage == totalPages - 1

    // Page dots
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(),
    ) {
        repeat(totalPages) { i ->
            val w by animateDpAsState(
                targetValue = if (i == currentPage) 24.dp else 8.dp,
                animationSpec = spring(dampingRatio = 0.7f),
                label = "dot$i",
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .height(8.dp)
                    .width(w)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (i == currentPage) Color.White else Color.White.copy(alpha = 0.25f))
                    .clickable {
                        // Allow tapping dots to navigate (no-op here, kept for visual)
                    },
            )
        }
    }

    Spacer(modifier = Modifier.height(28.dp))

    // Main action button
    Button(
        onClick = onNext,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = accentColor,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
    ) {
        Text(
            text = if (isLast) stringResource(R.string.action_get_started) else stringResource(R.string.action_continue),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = if (isLast) Icons.Filled.Check else Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Skip / Back row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        if (currentPage > 0) {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.action_back), color = Color.White.copy(alpha = 0.6f))
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        if (!isLast) {
            TextButton(onClick = onSkip) {
                Text(stringResource(R.string.action_skip), color = Color.White.copy(alpha = 0.6f))
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Commute Setup Step
// ═══════════════════════════════════════════════════════════════

@Composable
private fun CommuteSetupContent(viewModel: OnboardingViewModel) {
    val homeStation = viewModel.homeStationId?.let { TubeData.getStationById(it) }
    val workStation = viewModel.workStationId?.let { TubeData.getStationById(it) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(TubePrimary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(38.dp),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.onboarding_commute_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.onboarding_commute_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Home picker
        StationPickerRow(
            label = stringResource(R.string.onboarding_home_label),
            icon = Icons.Filled.Home,
            selected = homeStation,
            accent = TubePrimary,
            onSelect = { viewModel.setHome(it) },
            onClear = { viewModel.setHome(null) },
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Work picker
        StationPickerRow(
            label = stringResource(R.string.onboarding_work_label),
            icon = Icons.Filled.Business,
            selected = workStation,
            accent = TubeAccent,
            onSelect = { viewModel.setWork(it) },
            onClear = { viewModel.setWork(null) },
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.onboarding_commute_optional),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun StationPickerRow(
    label: String,
    icon: ImageVector,
    selected: Station?,
    accent: Color,
    onSelect: (String) -> Unit,
    onClear: () -> Unit,
) {
    var pickerOpen by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { pickerOpen = true },
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = if (selected != null) 0.14f else 0.08f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected != null) accent.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.2f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.55f),
                    letterSpacing = 0.5.sp,
                )
                Text(
                    text = selected?.name ?: "Tap to choose",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (selected != null) Color.White else Color.White.copy(alpha = 0.8f),
                )
            }
            if (selected != null) {
                IconButton(onClick = onClear) {
                    Icon(
                        Icons.Filled.Clear,
                        contentDescription = "Clear $label",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            } else {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }

    if (pickerOpen) {
        StationPickerSheet(
            label = label,
            accent = accent,
            onDismiss = { pickerOpen = false },
            onPick = { id ->
                onSelect(id)
                pickerOpen = false
            },
        )
    }
}

@Composable
private fun StationPickerSheet(
    label: String,
    accent: Color,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val results: List<Station> by remember(query) {
        derivedStateOf {
            if (query.isBlank()) TubeData.getAllStationsSorted().take(50)
            else TubeData.searchStations(query).take(50)
        }
    }

    // Full-screen modal styled like the rest of onboarding
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A1628).copy(alpha = 0.94f))
            .clickable(onClick = {}), // swallow clicks behind
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 56.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.onboarding_choose_label_station, label),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Clear, "Close", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(R.string.onboarding_search_hint), color = Color.White.copy(alpha = 0.5f)) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = Color.White.copy(alpha = 0.7f)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = accent,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            // weight(1f) gives the LazyColumn the *remaining* bounded height
            // of the enclosing Column. Using fillMaxSize() here crashes with
            // "Vertically scrollable component was measured with an infinity
            // maximum height constraints" because Column gives its children
            // unbounded height by default.
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
            ) {
                items(results, key = { it.id }) { station ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onPick(station.id) },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.06f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.LocationOn,
                                null,
                                tint = accent,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    station.name,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                if (station.zone.isNotBlank()) {
                                    Text(
                                        "Zone ${station.zone}",
                                        color = Color.White.copy(alpha = 0.55f),
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                        }
                    }
                }
                if (results.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.onboarding_no_stations_match, query),
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Accessibility Setup Step
// ═══════════════════════════════════════════════════════════════

@Composable
private fun AccessibilitySetupContent(viewModel: OnboardingViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(StatusGood.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Accessible,
                null,
                tint = Color.White,
                modifier = Modifier.size(38.dp),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.onboarding_accessibility_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.onboarding_accessibility_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.setStepFree(!viewModel.stepFreeEnabled) },
            shape = RoundedCornerShape(18.dp),
            color = Color.White.copy(alpha = if (viewModel.stepFreeEnabled) 0.16f else 0.08f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (viewModel.stepFreeEnabled) StatusGood.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.2f),
            ),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(StatusGood.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Accessible,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.onboarding_accessibility_prefer),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        stringResource(R.string.onboarding_accessibility_prefer_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                }
                Switch(
                    checked = viewModel.stepFreeEnabled,
                    onCheckedChange = { viewModel.setStepFree(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = StatusGood,
                        uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.18f),
                        uncheckedBorderColor = Color.White.copy(alpha = 0.3f),
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.onboarding_accessibility_change),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f),
        )
    }
}
