package com.londontubeai.navigator.ui.screens.premium

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.londontubeai.navigator.ui.components.SystemBarsEffect
import com.londontubeai.navigator.ui.theme.Spacing
import com.londontubeai.navigator.ui.theme.StatusGood
import com.londontubeai.navigator.ui.theme.TubeAccent
import com.londontubeai.navigator.ui.theme.TubePrimary
import com.londontubeai.navigator.ui.theme.TubeSecondary

// ── Brand tokens ───────────────────────────────────────────────────────
private val PremiumGold = Color(0xFFFFD700)
private val PremiumGoldDark = Color(0xFFC5A000)

// Billing periods visible in the pill selector. Lifetime is framed as a
// one-off "never pay again" option which converts well for power users.
private enum class BillingPeriod(val label: String, val badge: String? = null) {
    MONTHLY("Monthly"),
    ANNUAL("Annual", badge = "SAVE 44%"),
    LIFETIME("Lifetime", badge = "BEST VALUE"),
}

@Composable
fun PremiumScreen(
    onBack: () -> Unit = {},
    viewModel: PremiumViewModel = hiltViewModel(),
) {
    val billingState by viewModel.billingState.collectAsState()
    val activity = LocalContext.current as? Activity

    // Default to Annual — the value tier we want most users on.
    var selectedPeriod by remember { mutableStateOf(BillingPeriod.ANNUAL) }

    // Hero is a dark navy gradient → keep status bar icons light so the
    // system clock / battery / signal stay readable when scrolled to top.
    SystemBarsEffect(lightBackground = false)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 40.dp),
    ) {
        // ── Hero ────────────────────────────────────────────
        item { PremiumHero(onBack = onBack, isAlreadyPremium = billingState.isPremium) }

        // Already-subscribed state short-circuits the rest of the screen.
        if (billingState.isPremium) {
            item { Spacer(modifier = Modifier.height(Spacing.xl)) }
            item { AlreadyPremiumCard() }
            item { Spacer(modifier = Modifier.height(Spacing.xl)) }
            item { RestoreAndManageRow(onRestore = viewModel::restorePurchases) }
            return@LazyColumn
        }

        // ── Social proof strip ──────────────────────────────
        item { Spacer(modifier = Modifier.height(Spacing.lg)) }
        item { SocialProofStrip() }

        // ── Billing period selector ─────────────────────────
        item { Spacer(modifier = Modifier.height(Spacing.xl)) }
        item {
            BillingPeriodSelector(
                selected = selectedPeriod,
                onSelect = { selectedPeriod = it },
            )
        }

        // ── Pricing card for selected period ────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = Spacing.md)) {
                val (price, period, trialText) = when (selectedPeriod) {
                    BillingPeriod.MONTHLY -> Triple(
                        billingState.monthlyPrice ?: "£2.99",
                        "per month",
                        "7-day free trial · cancel any time",
                    )
                    BillingPeriod.ANNUAL -> Triple(
                        billingState.annualPrice ?: "£19.99",
                        "per year  ·  just £1.67/mo",
                        "7-day free trial · then billed yearly",
                    )
                    BillingPeriod.LIFETIME -> Triple(
                        billingState.lifetimePrice ?: "£49.99",
                        "one-time · never pay again",
                        "Pay once, own it forever · no subscription",
                    )
                }
                PrimaryPricingCard(
                    price = price,
                    period = period,
                    trialText = trialText,
                    ctaLabel = when (selectedPeriod) {
                        BillingPeriod.LIFETIME -> "Unlock for life"
                        else -> "Start 7-day free trial"
                    },
                    onClick = {
                        activity?.let {
                            when (selectedPeriod) {
                                BillingPeriod.MONTHLY -> viewModel.purchaseMonthly(it)
                                BillingPeriod.ANNUAL -> viewModel.purchaseAnnual(it)
                                BillingPeriod.LIFETIME -> viewModel.purchaseLifetime(it)
                            }
                        }
                    },
                )
            }
        }

        // ── Trust signals strip ────────────────────────────
        item { TrustSignalsStrip() }

        // ── Everything-you-get feature list ────────────────
        item { Spacer(modifier = Modifier.height(Spacing.xl)) }
        item {
            Text(
                "Everything you get",
                modifier = Modifier.padding(horizontal = 20.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
        }
        items(premiumFeatures) { feature ->
            PremiumFeatureRow(feature)
        }

        // ── Free vs Pro comparison ─────────────────────────
        item { Spacer(modifier = Modifier.height(Spacing.xl)) }
        item { FreeVsProTable() }

        // ── FAQ ────────────────────────────────────────────
        item { Spacer(modifier = Modifier.height(Spacing.xl)) }
        item {
            Text(
                "Frequently asked",
                modifier = Modifier.padding(horizontal = 20.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
        }
        items(faq) { (q, a) -> FaqRow(question = q, answer = a) }

        // ── Restore purchases + footer ─────────────────────
        item { Spacer(modifier = Modifier.height(Spacing.xl)) }
        item { RestoreAndManageRow(onRestore = viewModel::restorePurchases) }
        item {
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text = "Subscriptions auto-renew. Cancel in Google Play → Subscriptions at least 24 h before renewal. " +
                    "Prices shown in GBP and may vary by region. Terms apply.",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
            )
            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}

// ─── Hero ────────────────────────────────────────────────────────────────
@Composable
private fun PremiumHero(onBack: () -> Unit, isAlreadyPremium: Boolean) {
    Box(
        modifier = Modifier.fillMaxWidth().background(
            Brush.verticalGradient(
                colors = listOf(Color(0xFF0A1628), Color(0xFF0D2240), TubePrimary),
            ),
        ),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(Color.White.copy(alpha = 0.03f), 160.dp.toPx(), Offset(size.width * 0.85f, size.height * 0.3f))
            drawCircle(PremiumGold.copy(alpha = 0.04f), 100.dp.toPx(), Offset(size.width * 0.1f, size.height * 0.7f))
        }
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 12.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
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
                        text = if (isAlreadyPremium) "ACTIVE" else "PRO",
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
                Icon(Icons.Filled.Diamond, null, tint = PremiumGold, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(Spacing.xl))
            Text(
                text = if (isAlreadyPremium) "You're on Pro" else "Commute like a Londoner",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = if (isAlreadyPremium)
                    "Thanks for supporting the app. Every Pro feature is unlocked."
                else
                    "Unlimited saved routes, offline maps, push alerts for your lines, and more.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.75f),
                lineHeight = 22.sp,
            )
        }
    }
}

// ─── Already premium state ──────────────────────────────────────────────
@Composable
private fun AlreadyPremiumCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StatusGood.copy(alpha = 0.08f)),
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CheckCircle, null, tint = StatusGood, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("All Pro features unlocked", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Manage or cancel any time via Google Play → Subscriptions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ─── Social proof strip ─────────────────────────────────────────────────
@Composable
private fun SocialProofStrip() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        SocialStat("50K+", "Londoners", modifier = Modifier.weight(1f))
        SocialStat("4.8★", "App rating", modifier = Modifier.weight(1f))
        SocialStat("1M+", "Journeys planned", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SocialStat(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = TubePrimary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─── Billing period pill selector ───────────────────────────────────────
@Composable
private fun BillingPeriodSelector(
    selected: BillingPeriod,
    onSelect: (BillingPeriod) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            BillingPeriod.values().forEach { period ->
                val isSelected = period == selected
                Surface(
                    modifier = Modifier.weight(1f).clickable { onSelect(period) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            period.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (period.badge != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                period.badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = PremiumGoldDark,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Primary pricing card ───────────────────────────────────────────────
@Composable
private fun PrimaryPricingCard(
    price: String,
    period: String,
    trialText: String,
    ctaLabel: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = TubePrimary),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = price,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = period,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = trialText,
                style = MaterialTheme.typography.bodySmall,
                color = PremiumGold,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(Spacing.lg))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PremiumGold,
                    contentColor = Color.Black,
                ),
            ) {
                Text(ctaLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

// ─── Trust signals (cancel/refund/privacy) ──────────────────────────────
@Composable
private fun TrustSignalsStrip() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TrustChip(Icons.Filled.Block, "Cancel\nany time")
        TrustChip(Icons.Filled.Lock, "Privacy-first\n(on-device)")
        TrustChip(Icons.Filled.Refresh, "Full refund\n< 14 days")
    }
}

@Composable
private fun TrustChip(icon: ImageVector, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp),
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(StatusGood.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = StatusGood, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp,
            fontSize = 10.sp,
        )
    }
}

// ─── Features list ──────────────────────────────────────────────────────
private data class PremiumFeature(
    val icon: ImageVector,
    val iconColor: Color,
    val title: String,
    val description: String,
)

private val premiumFeatures = listOf(
    PremiumFeature(
        Icons.Filled.Block, TubePrimary,
        "No ads, ever",
        "A clean, distraction-free experience — navigation should never be interrupted.",
    ),
    PremiumFeature(
        Icons.Filled.Bookmark, TubeAccent,
        "Unlimited saved routes",
        "Free users get 3. Pro users save as many commute shortcuts as they like.",
    ),
    PremiumFeature(
        Icons.Filled.CloudDownload, TubeSecondary,
        "Offline map of Greater London",
        "Download the full tube network for underground, plane mode, or travel abroad.",
    ),
    PremiumFeature(
        Icons.Filled.Notifications, StatusGood,
        "Push alerts for your commute lines",
        "Severe delays on Victoria? We'll ping you before you leave home.",
    ),
    PremiumFeature(
        Icons.Filled.Widgets, TubeAccent,
        "Home-screen widgets",
        "1×1 next-train + 2×2 full-commute widgets right on your launcher.",
    ),
    PremiumFeature(
        Icons.Filled.Insights, TubePrimary,
        "Historic reliability stats",
        "See which lines have been the most / least reliable over the last 30 days.",
    ),
    PremiumFeature(
        Icons.Filled.Train, TubeSecondary,
        "Smart carriage recommendations",
        "Board the carriage closest to your exit at the destination — save 90 seconds every journey.",
    ),
    PremiumFeature(
        Icons.Filled.FamilyRestroom, StatusGood,
        "Family plan (up to 2 people)",
        "Share Pro with your partner or a family member at no extra cost.",
    ),
    PremiumFeature(
        Icons.Filled.SupportAgent, PremiumGold,
        "Priority support",
        "Questions? Pro users get replies within 24 hours.",
    ),
)

@Composable
private fun PremiumFeatureRow(feature: PremiumFeature) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(11.dp))
                .background(feature.iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(feature.icon, null, tint = feature.iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(feature.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp,
            )
        }
    }
}

// ─── Free vs Pro comparison ────────────────────────────────────────────
@Composable
private fun FreeVsProTable() {
    val rows = listOf(
        "Live arrivals" to Pair(true, true),
        "Journey planning" to Pair(true, true),
        "Line status" to Pair(true, true),
        "Saved routes" to Pair(false, true), // 3 vs unlimited — nuanced
        "Offline maps" to Pair(false, true),
        "Push alerts" to Pair(false, true),
        "Widgets" to Pair(false, true),
        "Carriage tips" to Pair(false, true),
        "Reliability stats" to Pair(false, true),
        "No ads" to Pair(false, true),
    )
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    ) {
        Column(modifier = Modifier.padding(vertical = Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Text("Feature", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text("Free", modifier = Modifier.width(56.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("Pro", modifier = Modifier.width(56.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = PremiumGoldDark)
            }
            rows.forEach { (name, flags) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    Box(modifier = Modifier.width(56.dp), contentAlignment = Alignment.Center) {
                        if (flags.first) Icon(Icons.Filled.CheckCircle, null, tint = StatusGood, modifier = Modifier.size(16.dp))
                        else Text("—", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(modifier = Modifier.width(56.dp), contentAlignment = Alignment.Center) {
                        if (flags.second) Icon(Icons.Filled.CheckCircle, null, tint = PremiumGold, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// ─── FAQ ───────────────────────────────────────────────────────────────
private val faq = listOf(
    "Can I cancel any time?" to
        "Yes. Cancel in Google Play → Subscriptions at least 24 hours before the next renewal. You keep Pro until the end of the current billing period.",
    "Is there a free trial?" to
        "Monthly and Annual plans include a 7-day free trial. Lifetime is a one-time purchase with no trial but a 14-day refund window via Google Play.",
    "Can I share Pro with my family?" to
        "Yes — the Family plan lets you share Pro with one additional person (typically your partner) via Google Play Family.",
    "Will you raise the price?" to
        "If we do, we'll give you 30 days notice in-app. Lifetime purchasers are locked in at the price they paid, forever.",
    "What about my data?" to
        "Everything is stored on your device. Payments are handled entirely by Google Play — we never see your card details.",
)

@Composable
private fun FaqRow(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Filled.HelpOutline, null,
                    tint = TubePrimary, modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    question,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp, start = 28.dp)) {
                    Text(
                        answer,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                    )
                }
            }
        }
    }
}

// ─── Restore / manage row ──────────────────────────────────────────────
@Composable
private fun RestoreAndManageRow(onRestore: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        TextButton(
            onClick = onRestore,
            modifier = Modifier.weight(1f).height(44.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
        ) {
            Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Restore purchases", style = MaterialTheme.typography.labelLarge)
        }
    }
}
