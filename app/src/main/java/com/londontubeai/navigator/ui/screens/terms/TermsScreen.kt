package com.londontubeai.navigator.ui.screens.terms

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Copyright
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.londontubeai.navigator.ui.theme.Spacing
import com.londontubeai.navigator.ui.theme.StatusGood
import com.londontubeai.navigator.ui.theme.TubePrimary

@Composable
fun TermsScreen(onBack: () -> Unit = {}) {
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

            item {
                TermsSection(
                    icon = Icons.Filled.Gavel,
                    title = "1. Acceptance of Terms",
                    description = "By using AI Tube Navigator you confirm that you are at least 13 years of age. If you are under 18, you confirm that you have obtained parental or guardian consent. We may update these Terms at any time; continued use of the App after updates constitutes acceptance. We will notify you of material changes via an in-app notice or Google Play update notes.",
                )
            }

            item {
                TermsSection(
                    icon = Icons.Filled.Info,
                    title = "2. Description of Service",
                    description = "AI Tube Navigator is an independent third-party app that provides London Underground journey planning, live service status, and station information using the TfL Open Data API. The App is not affiliated with, endorsed by, or connected to Transport for London (TfL), the Mayor of London, or any transport operator. Live data is provided by TfL under the TfL Open Data Licence and may not always be accurate or up to date.",
                )
            }

            item {
                TermsSection(
                    icon = Icons.Filled.VerifiedUser,
                    title = "3. User Responsibilities",
                    description = "You agree to use AI Tube Navigator only for lawful purposes and in accordance with these Terms. You must not: (a) reverse engineer, decompile, or disassemble any part of the App; (b) use the App in any way that could damage, disable, or impair its operation; (c) attempt to gain unauthorised access to any part of the App's infrastructure; (d) rely solely on the App for safety-critical travel decisions without independent verification. For journeys involving medical appointments, flights, or other time-sensitive situations, always verify information with official sources.",
                )
            }

            item {
                TermsSection(
                    icon = Icons.Filled.Warning,
                    title = "4. Disclaimers & Limitations",
                    description = "The App is provided on an \"as is\" and \"as available\" basis without warranties of any kind. We do not guarantee the accuracy, completeness, or timeliness of real-time data, AI-generated predictions, crowd-level estimates, or route suggestions. Live information is sourced from TfL's API and may be subject to delays, errors, or outages outside our control. Always check official TfL announcements for safety-critical decisions.",
                )
            }

            item {
                TermsSection(
                    icon = Icons.Filled.Security,
                    title = "5. Limitation of Liability",
                    description = "To the maximum extent permitted by applicable law, AI Tube Navigator and its developers shall not be liable for any indirect, incidental, special, consequential, or punitive damages arising from your use of or inability to use the App, including but not limited to missed journeys, delay costs, or reliance on inaccurate data. Our total liability, if any, shall not exceed the amount you paid for the App or its subscription in the twelve months preceding the claim.",
                )
            }

            item {
                TermsSection(
                    icon = Icons.Filled.Payment,
                    title = "6. Premium Subscription",
                    description = "AI Tube Navigator offers an optional Premium subscription that unlocks additional features. Subscriptions are billed through Google Play and subject to Google Play's payment and refund policies. Subscriptions auto-renew unless cancelled at least 24 hours before the end of the current billing period. You can manage or cancel your subscription at any time via Google Play → Subscriptions. We will provide reasonable notice of any price changes before they take effect.",
                )
            }

            item {
                TermsSection(
                    icon = Icons.Filled.Copyright,
                    title = "7. Intellectual Property",
                    description = "All original content, design, code, and AI models within AI Tube Navigator are the intellectual property of the developer and are protected by copyright law. The App uses TfL Open Data under the TfL Open Data Licence; TfL retains all rights to its data. Map functionality is powered by the Google Maps SDK under Google's Terms of Service. Roundel-style iconography is an original design inspired by, but not reproducing, Transport for London's trademarked roundel.",
                )
            }

            item {
                TermsSection(
                    icon = Icons.Filled.Block,
                    title = "8. Termination",
                    description = "We reserve the right to suspend or terminate access to AI Tube Navigator at any time if you violate these Terms. You may stop using the App at any time by uninstalling it. Upon uninstallation, all locally stored data (preferences, history, cached data) is permanently deleted from your device. Active premium subscriptions must be cancelled separately via Google Play.",
                )
            }

            item {
                TermsSection(
                    icon = Icons.Filled.AccountBalance,
                    title = "9. Governing Law",
                    description = "These Terms of Service are governed by and construed in accordance with the laws of England and Wales. Any disputes arising from or related to these Terms or the App shall be subject to the exclusive jurisdiction of the courts of England and Wales, except where mandatory consumer protection laws in your country of residence provide otherwise.",
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
                            text = "Contact: legal@aitubenavigator.app",
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
