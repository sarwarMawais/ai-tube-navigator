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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material3.MaterialTheme
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
import kotlinx.coroutines.delay
import kotlin.math.abs

// ─── Data Models ──────────────────────────────────────────────

private data class OnboardingPage(
    val icon: ImageVector,
    val accentColor: Color,
    val title: String,
    val subtitle: String,
    val description: String,
    val benefits: List<Benefit>,
    val gradientStart: Color,
    val gradientEnd: Color,
)

private data class Benefit(
    val icon: ImageVector,
    val label: String,
    val detail: String,
)

// ─── Page Content ─────────────────────────────────────────────

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Filled.DirectionsSubway,
        accentColor = TubePrimary,
        title = "Welcome",
        subtitle = "Your Intelligent Underground Companion",
        description = "Navigate London's Tube smarter with AI-powered routing, real-time updates, and personalised journey planning.",
        benefits = listOf(
            Benefit(Icons.Filled.Route, "AI Routes", "Fastest path with live data"),
            Benefit(Icons.Filled.Notifications, "Alerts", "Disruption notifications"),
            Benefit(Icons.Filled.Lightbulb, "Smart Tips", "Carriage & exit advice"),
        ),
        gradientStart = TubePrimary,
        gradientEnd = TubeAccent,
    ),
    OnboardingPage(
        icon = Icons.Filled.Route,
        accentColor = StatusGood,
        title = "Smart Route Planning",
        subtitle = "AI That Learns Your Preferences",
        description = "Graph-based pathfinding with real interchange times, crowd-aware routing, and time predictions that adapt to peak hours.",
        benefits = listOf(
            Benefit(Icons.Filled.AccessTime, "Peak Avoidance", "Skip busy connections"),
            Benefit(Icons.Filled.LocationOn, "Exit Guidance", "Optimal carriage position"),
            Benefit(Icons.Filled.Timer, "Time Saved", "Up to 5 min per trip"),
        ),
        gradientStart = StatusGood,
        gradientEnd = TubePrimary,
    ),
    OnboardingPage(
        icon = Icons.Filled.Map,
        accentColor = TubeSecondary,
        title = "Live Network Map",
        subtitle = "The Entire Tube at a Glance",
        description = "Interactive map with real-time status overlays, station details, and line filtering. Tap any station for instant information.",
        benefits = listOf(
            Benefit(Icons.Filled.Wifi, "Real-time", "Live status overlays"),
            Benefit(Icons.Filled.Info, "Station Info", "Tap for instant details"),
            Benefit(Icons.Filled.FilterList, "Smart Filter", "By line or status"),
        ),
        gradientStart = TubeSecondary,
        gradientEnd = TubeAccent,
    ),
    OnboardingPage(
        icon = Icons.Filled.CloudOff,
        accentColor = Color(0xFF6C63FF),
        title = "Works Underground",
        subtitle = "No Signal? No Problem",
        description = "Full offline routing, cached predictions, and station data in tunnels. The app detects connectivity and switches seamlessly.",
        benefits = listOf(
            Benefit(Icons.Filled.Storage, "Offline Data", "Routes cached locally"),
            Benefit(Icons.Filled.Sync, "Auto Switch", "Online ↔ offline seamless"),
            Benefit(Icons.Filled.SignalWifiOff, "Tunnel Ready", "Works without signal"),
        ),
        gradientStart = Color(0xFF6C63FF),
        gradientEnd = StatusGood,
    ),
)

// ─── Main Composable ──────────────────────────────────────────

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
) {
    var currentPage by remember { mutableIntStateOf(0) }
    var dragOffset by remember { mutableStateOf(0f) }
    val page = pages[currentPage]

    val progress by animateFloatAsState(
        targetValue = (currentPage + 1).toFloat() / pages.size,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "progress",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        page.gradientStart.copy(alpha = 0.92f),
                        page.gradientEnd.copy(alpha = 0.75f),
                        Color(0xFF0A1628),
                    ),
                ),
            )
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (abs(dragOffset) > 80) {
                            if (dragOffset < 0 && currentPage < pages.size - 1) currentPage++
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
            ProgressSection(progress = progress, current = currentPage, total = pages.size)

            Spacer(modifier = Modifier.weight(1f))

            // ── Animated page content ─────────────────────────
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    (slideInHorizontally { it / 2 } + fadeIn(tween(350))) togetherWith
                            (slideOutHorizontally { -it / 2 } + fadeOut(tween(250)))
                },
                label = "page",
            ) { idx ->
                PageContent(page = pages[idx])
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Navigation ────────────────────────────────────
            NavigationSection(
                currentPage = currentPage,
                totalPages = pages.size,
                accentColor = page.accentColor,
                onNext = { if (currentPage < pages.size - 1) currentPage++ else onComplete() },
                onBack = { if (currentPage > 0) currentPage-- },
                onSkip = onComplete,
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
            text = page.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = page.subtitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = page.description,
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
                text = benefit.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = alpha),
            )
            Text(
                text = benefit.detail,
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
            text = if (isLast) "Get Started" else "Continue",
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
                Text("Back", color = Color.White.copy(alpha = 0.6f))
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        if (!isLast) {
            TextButton(onClick = onSkip) {
                Text("Skip", color = Color.White.copy(alpha = 0.6f))
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }
    }
}
