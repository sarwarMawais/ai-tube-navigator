package com.londontubeai.navigator.ui.screens.premium

import android.app.Activity
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.londontubeai.navigator.ui.theme.StatusGood
import com.londontubeai.navigator.ui.theme.TubeAccent
import com.londontubeai.navigator.ui.theme.Spacing
import com.londontubeai.navigator.ui.theme.TubePrimary
import com.londontubeai.navigator.ui.theme.TubeSecondary

private val PremiumGold = Color(0xFFFFD700)
private val PremiumGoldDark = Color(0xFFC5A000)

@Composable
fun PremiumScreen(
    onBack: () -> Unit = {},
    viewModel: PremiumViewModel = hiltViewModel(),
) {
    val billingState by viewModel.billingState.collectAsState()
    val activity = LocalContext.current as? Activity
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 40.dp),
    ) {
        // ── Hero header ──────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0A1628), Color(0xFF0D2240), TubePrimary),
                        )
                    ),
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(Color.White.copy(alpha = 0.03f), 160.dp.toPx(), Offset(size.width * 0.85f, size.height * 0.3f))
                    drawCircle(PremiumGold.copy(alpha = 0.04f), 100.dp.toPx(), Offset(size.width * 0.1f, size.height * 0.7f))
                }

                Column(
                    modifier = Modifier.padding(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White.copy(alpha = 0.7f))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PremiumGold.copy(alpha = 0.15f),
                        ) {
                            Text(
                                text = "PRO",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = PremiumGold,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.xl))

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(PremiumGold.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Star, null, tint = PremiumGold, modifier = Modifier.size(32.dp))
                    }

                    Spacer(modifier = Modifier.height(Spacing.xl))

                    Text(
                        text = "Upgrade to Pro",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = "Unlock the full power of AI navigation",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                }
            }
        }

        // ── Features list ────────────────────────────────────
        item { Spacer(modifier = Modifier.height(Spacing.xl)) }

        item {
            PremiumFeatureCard(
                icon = Icons.Filled.Bolt,
                iconColor = PremiumGold,
                title = "Advanced AI Predictions",
                description = "ML-powered crowd density and delay predictions with 90%+ accuracy. Get precise minute-by-minute forecasts.",
            )
        }
        item {
            PremiumFeatureCard(
                icon = Icons.Filled.Notifications,
                iconColor = TubeAccent,
                title = "Smart Push Alerts",
                description = "Proactive disruption alerts for your commute lines. AI suggests alternative routes before delays hit.",
            )
        }
        item {
            PremiumFeatureCard(
                icon = Icons.Filled.SignalWifiOff,
                iconColor = TubeSecondary,
                title = "Full Offline Mode",
                description = "Download entire station data packs for tunnel use. Offline routing, predictions, and station maps.",
            )
        }
        item {
            PremiumFeatureCard(
                icon = Icons.Filled.Timeline,
                iconColor = StatusGood,
                title = "Personal Commute AI",
                description = "The app learns your patterns and adapts. Time-to-leave alerts, favourite route shortcuts, and commute analytics.",
            )
        }
        item {
            PremiumFeatureCard(
                icon = Icons.Filled.Map,
                iconColor = TubePrimary,
                title = "Ad-Free Experience",
                description = "Clean, distraction-free interface. Premium users get early access to new features and priority support.",
            )
        }

        // ── Pricing ──────────────────────────────────────────
        item { Spacer(modifier = Modifier.height(Spacing.xxl)) }

        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // Monthly
                PricingCard(
                    title = "Monthly",
                    price = billingState.monthlyPrice ?: "£2.99",
                    period = "/month",
                    highlight = false,
                    onClick = { activity?.let { viewModel.purchaseMonthly(it) } },
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                // Annual (recommended)
                PricingCard(
                    title = "Annual",
                    price = billingState.annualPrice ?: "£19.99",
                    period = "/year",
                    badge = "SAVE 44%",
                    highlight = true,
                    onClick = { activity?.let { viewModel.purchaseAnnual(it) } },
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                // Lifetime
                PricingCard(
                    title = "Lifetime",
                    price = billingState.lifetimePrice ?: "£49.99",
                    period = "one-time",
                    highlight = false,
                    onClick = { activity?.let { viewModel.purchaseLifetime(it) } },
                )
            }
        }

        // ── Footer ───────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Cancel anytime · Restore purchases · Terms apply",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PremiumFeatureCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            Icons.Filled.CheckCircle,
            null,
            tint = StatusGood.copy(alpha = 0.6f),
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp),
        )
    }
}

@Composable
private fun PricingCard(
    title: String,
    price: String,
    period: String,
    badge: String? = null,
    highlight: Boolean = false,
    onClick: () -> Unit,
) {
    val containerColor = if (highlight) TubePrimary else MaterialTheme.colorScheme.surface
    val contentColor = if (highlight) Color.White else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(if (highlight) 6.dp else 1.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (highlight) PremiumGold else PremiumGold.copy(alpha = 0.15f),
                        ) {
                            Text(
                                text = badge,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (highlight) Color.Black else PremiumGoldDark,
                                fontSize = 9.sp,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Full access to all Pro features",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.6f),
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = price,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor,
                )
                Text(
                    text = period,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.5f),
                )
            }
        }
    }
}
