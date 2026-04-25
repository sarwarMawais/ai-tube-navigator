package com.londontubeai.navigator.ui.screens.terms

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Copyright
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.londontubeai.navigator.ui.theme.Spacing
import com.londontubeai.navigator.ui.theme.StatusGood
import com.londontubeai.navigator.ui.theme.TubeAccent
import com.londontubeai.navigator.ui.theme.TubePrimary

@Composable
fun TermsScreen(onBack: () -> Unit = {}) {
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier.statusBarsPadding().padding(start = 4.dp, end = 20.dp, top = 4.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
                Column {
                    Text("Terms of Service", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Last updated: 11 April 2026", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = Spacing.xl, vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            // ── TfL Attribution Banner ──────────────────────────
            item { TflAttributionBanner(context) }

            // ── Search Field ────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(Spacing.md))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search terms...", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                )
                Spacer(modifier = Modifier.height(Spacing.md))
            }
            item {
                Text(
                    text = "Terms of Service",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = "Please read these Terms of Service carefully before using AI Tube Navigator. By downloading or using the App, you agree to be bound by these Terms. If you do not agree to any part of these Terms, please do not use the App.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                )
                Spacer(modifier = Modifier.height(Spacing.lg))
            }

            val filteredTerms = if (searchQuery.isBlank()) {
                termsSections
            } else {
                termsSections.filter { section ->
                    section.title.contains(searchQuery, ignoreCase = true) ||
                    section.description.contains(searchQuery, ignoreCase = true)
                }
            }

            items(filteredTerms) { section ->
                TermsSection(
                    icon = section.icon,
                    title = section.title,
                    description = section.description,
                )
            }

            item {
                Spacer(modifier = Modifier.height(Spacing.md))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusGood.copy(alpha = 0.06f)),
                ) {
                    Column(modifier = Modifier.padding(Spacing.lg)) {
                        Text("Agreement", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = "By using AI Tube Navigator you acknowledge that you have read, understood, and agree to these Terms of Service and our Privacy Policy. These documents together form the entire agreement between you and the developer regarding the App.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp,
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = "Contact: londontubenavigator@gmail.com",
                            style = MaterialTheme.typography.labelMedium,
                            color = TubePrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(Spacing.lg))
                Text(
                    text = "Last updated: 11 April 2026 · AI Tube Navigator v1.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(Spacing.xxxl))
            }
        }
    }
}

@Composable
private fun TermsSection(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(icon, null, tint = TubePrimary, modifier = Modifier.size(22.dp).padding(top = 2.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
        }
    }
}

// ─── TfL Attribution Banner ─────────────────────────────────────────────────
@Composable
private fun TflAttributionBanner(context: android.content.Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TubeAccent.copy(alpha = 0.1f)),
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "TfL Open Data",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = TubeAccent,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Contains data from",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = "This app uses Transport for London (TfL) Open Data under the TfL Open Data Licence. " +
                    "The data is provided \"as is\" and TfL gives no warranty as to its accuracy or completeness. " +
                    "This app is not affiliated with, endorsed by, or connected to Transport for London.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp,
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://tfl.gov.uk/info-for/open-data-users/"))
                    runCatching { context.startActivity(intent) }
                },
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.OpenInNew,
                    null,
                    tint = TubeAccent,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "View TfL Open Data Licence",
                    style = MaterialTheme.typography.labelSmall,
                    color = TubeAccent,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ─── Terms sections data ─────────────────────────────────────────────────────
private data class TermsSectionData(
    val icon: ImageVector,
    val title: String,
    val description: String,
)

private val termsSections = listOf(
    TermsSectionData(
        Icons.Filled.Gavel,
        "1. Acceptance of Terms",
        "By using AI Tube Navigator you confirm that you are at least 13 years of age. If you are under 18, you confirm that you have obtained parental or guardian consent. We may update these Terms at any time; continued use of the App after updates constitutes acceptance. We will notify you of material changes via an in-app notice or Google Play update notes.",
    ),
    TermsSectionData(
        Icons.Filled.Info,
        "2. Description of Service",
        "AI Tube Navigator is an independent third-party app that provides London Underground journey planning, live service status, and station information using the TfL Open Data API. The App is not affiliated with, endorsed by, or connected to Transport for London (TfL), the Mayor of London, or any transport operator. Live data is provided by TfL under the TfL Open Data Licence and may not always be accurate or up to date.",
    ),
    TermsSectionData(
        Icons.Filled.VerifiedUser,
        "3. User Responsibilities",
        "You agree to use AI Tube Navigator only for lawful purposes and in accordance with these Terms. You must not: (a) reverse engineer, decompile, or disassemble any part of the App; (b) use the App in any way that could damage, disable, or impair its operation; (c) attempt to gain unauthorised access to any part of the App's infrastructure; (d) rely solely on the App for safety-critical travel decisions without independent verification. For journeys involving medical appointments, flights, or other time-sensitive situations, always verify information with official sources.",
    ),
    TermsSectionData(
        Icons.Filled.Warning,
        "4. Disclaimers & Limitations",
        "The App is provided on an \"as is\" and \"as available\" basis without warranties of any kind. We do not guarantee the accuracy, completeness, or timeliness of real-time data, AI-generated predictions, crowd-level estimates, or route suggestions. Live information is sourced from TfL's API and may be subject to delays, errors, or outages outside our control. Always check official TfL announcements for safety-critical decisions.",
    ),
    TermsSectionData(
        Icons.Filled.Security,
        "5. Limitation of Liability",
        "To the maximum extent permitted by applicable law, AI Tube Navigator and its developers shall not be liable for any indirect, incidental, special, consequential, or punitive damages arising from your use of or inability to use the App, including but not limited to missed journeys, delay costs, or reliance on inaccurate data. Our total liability, if any, shall not exceed the amount you paid for the App or its subscription in the twelve months preceding the claim.",
    ),
    TermsSectionData(
        Icons.Filled.Payment,
        "6. Premium Subscription",
        "AI Tube Navigator offers an optional Premium subscription that unlocks additional features. Subscriptions are billed through Google Play and subject to Google Play's payment and refund policies. Subscriptions auto-renew unless cancelled at least 24 hours before the end of the current billing period. You can manage or cancel your subscription at any time via Google Play → Subscriptions. We will provide reasonable notice of any price changes before they take effect.",
    ),
    TermsSectionData(
        Icons.Filled.Copyright,
        "7. Intellectual Property",
        "All original content, design, code, and AI models within AI Tube Navigator are the intellectual property of the developer and are protected by copyright law. The App uses TfL Open Data under the TfL Open Data Licence; TfL retains all rights to its data. Map functionality is powered by the Google Maps SDK under Google's Terms of Service. Roundel-style iconography is an original design inspired by, but not reproducing, Transport for London's trademarked roundel.",
    ),
    TermsSectionData(
        Icons.Filled.Block,
        "8. Termination",
        "We reserve the right to suspend or terminate access to AI Tube Navigator at any time if you violate these Terms. You may stop using the App at any time by uninstalling it. Upon uninstallation, all locally stored data (preferences, history, cached data) is permanently deleted from your device. Active premium subscriptions must be cancelled separately via Google Play.",
    ),
    TermsSectionData(
        Icons.Filled.AccountBalance,
        "9. Governing Law",
        "These Terms of Service are governed by and construed in accordance with the laws of England and Wales. Any disputes arising from or related to these Terms or the App shall be subject to the exclusive jurisdiction of the courts of England and Wales, except where mandatory consumer protection laws in your country of residence provide otherwise.",
    ),
)
