package com.londontubeai.navigator.ui.screens.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.londontubeai.navigator.ui.theme.TubeAccent
import com.londontubeai.navigator.ui.theme.TubePrimary
import kotlinx.coroutines.delay

@Composable
fun AnimatedSplashScreen(
    onSplashComplete: () -> Unit,
    iconGradientStart: Color = Color(0xFF0A1628),
    iconGradientEnd: Color = TubePrimary,
) {
    var animationPhase by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        delay(100)
        animationPhase = 1          // roundel draws in
        delay(400)
        animationPhase = 2          // text fades in
        delay(300)
        animationPhase = 3          // loading dots
        delay(400)
        onSplashComplete()
    }

    // ── Logo animations ──────────────────────────────────────────
    val logoScale by animateFloatAsState(
        targetValue = if (animationPhase >= 1) 1f else 0.3f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 180f),
        label = "logoScale",
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (animationPhase >= 1) 1f else 0f,
        animationSpec = tween(500),
        label = "logoAlpha",
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (animationPhase >= 2) 1f else 0f,
        animationSpec = tween(500),
        label = "textAlpha",
    )

    // ── Subtle background pulse ──────────────────────────────────
    val infinite = rememberInfiniteTransition(label = "bg")
    val pulse by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        iconGradientStart,
                        iconGradientStart.copy(alpha = 0.85f).let {
                            Color(
                                red = (it.red + iconGradientEnd.red) / 2f,
                                green = (it.green + iconGradientEnd.green) / 2f,
                                blue = (it.blue + iconGradientEnd.blue) / 2f,
                                alpha = 1f,
                            )
                        },
                        iconGradientEnd.copy(alpha = 0.92f),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Decorative background elements
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Soft pulsing orbs
            drawCircle(
                color = Color.White.copy(alpha = 0.02f + pulse * 0.015f),
                radius = 220.dp.toPx(),
                center = Offset(size.width * 0.8f, size.height * 0.2f),
            )
            drawCircle(
                color = TubeAccent.copy(alpha = 0.03f + pulse * 0.02f),
                radius = 160.dp.toPx(),
                center = Offset(size.width * 0.15f, size.height * 0.8f),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // ── TfL Roundel-style logo ───────────────────────────
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 7.dp.toPx()
                    // Outer circle
                    drawCircle(
                        color = Color.White,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round),
                    )
                    // Horizontal bar (extends beyond circle edges a bit)
                    val barH = 14.dp.toPx()
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(4.dp.toPx(), center.y - barH / 2),
                        size = Size(size.width - 8.dp.toPx(), barH),
                        cornerRadius = CornerRadius(barH / 2),
                    )
                }
                // "AI" text inside the bar
                Text(
                    text = "AI",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TubePrimary,
                    fontSize = 28.sp,
                    letterSpacing = 2.sp,
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── App title ────────────────────────────────────────
            Text(
                text = "London Tube AI",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(textAlpha),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Tagline
            Text(
                text = "Your Intelligent Underground Companion",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.65f),
                modifier = Modifier.alpha(textAlpha),
                textAlign = TextAlign.Center,
                letterSpacing = 0.3.sp,
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading dots
            if (animationPhase >= 3) {
                PulsingDots()
            }
        }
    }
}

@Composable
private fun PulsingDots() {
    val infinite = rememberInfiniteTransition(label = "dots")
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(16.dp),
    ) {
        repeat(3) { i ->
            val scale by infinite.animateFloat(
                initialValue = 0.6f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, delayMillis = i * 150),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot$i",
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f)),
            )
        }
    }
}
