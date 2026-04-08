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
                modifier = Modifier.padding(start = 4.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
                Column {
                    Text("Terms of Service", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Last updated: 1 March 2026", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    text = "Please read these Terms of Service (\"Terms\") carefully before using AI Tube Navigator (\"the App\"). By downloading, installing, or using the App, you agree to be bound by these Terms. If you do not agree, do not use the App.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                )
                Spacer(modifier = Modifier.height(Spacing.lg))
            }

            // 1. Acceptance
            item {
                TermsSection(
                    icon = Icons.Filled.Gavel,
                    title = "1. Acceptance of Terms",
                    description = "By accessing or using the App, you confirm that you are at least 13 years old and agree to comply with and be bound by these Terms. We reserve the right to modify these Terms at any time. Your continued use of the App following changes constitutes acceptance of the updated Terms.",
                )
            }

            // 2. Description of Service
            item {
                TermsSection(
                    icon = Icons.Filled.Info,
                    title = "2. Description of Service",
                    description = "AI Tube Navigator provides real-time London Underground information, including live line status, station data, nearby stations, route planning, crowd predictions, and AI-powered commute insights.\n\n" +
                            "The App uses data from Transport for London (TfL) Unified API and Open-Meteo weather API. We are not affiliated with, endorsed by, or officially connected to TfL or any transport authority.",
                )
            }

            // 3. User Responsibilities
            item {
                TermsSection(
                    icon = Icons.Filled.VerifiedUser,
                    title = "3. User Responsibilities",
                    description = "You agree to:\n" +
                            "- Use the App only for its intended purpose\n" +
                            "- Not attempt to reverse-engineer, decompile, or modify the App\n" +
                            "- Not use the App for any unlawful purpose\n" +
                            "- Not interfere with the App's operation or servers\n" +
                            "- Verify travel information independently for safety-critical decisions",
                )
            }

            // 4. Disclaimers
            item {
                TermsSection(
                    icon = Icons.Filled.Warning,
                    title = "4. Disclaimers & Limitations",
                    description = "THE APP IS PROVIDED \"AS IS\" AND \"AS AVAILABLE\" WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED.\n\n" +
                            "- Real-time data depends on TfL API availability and accuracy. We do not guarantee the accuracy, completeness, or timeliness of any information.\n" +
                            "- AI predictions and crowd levels are estimates based on historical patterns and should not be relied upon as the sole basis for travel decisions.\n" +
                            "- Bus route suggestions are approximate and may not reflect real-time bus schedules.\n" +
                            "- Weather information is provided for informational purposes only.",
                )
            }

            // 5. Limitation of Liability
            item {
                TermsSection(
                    icon = Icons.Filled.Security,
                    title = "5. Limitation of Liability",
                    description = "To the maximum extent permitted by applicable law, AI Tube Navigator and its developers shall not be liable for any indirect, incidental, special, consequential, or punitive damages, including but not limited to loss of profits, data, or use, arising from or related to your use of the App.\n\n" +
                            "In no event shall our total liability exceed the amount you paid for the App or any premium subscription in the twelve (12) months preceding the claim.",
                )
            }

            // 6. Premium Subscription
            item {
                TermsSection(
                    icon = Icons.Filled.Payment,
                    title = "6. Premium Subscription",
                    description = "- Premium features are available via subscription through Google Play.\n" +
                            "- Payment is charged to your Google Play account at confirmation of purchase.\n" +
                            "- Subscriptions automatically renew unless cancelled at least 24 hours before the end of the current period.\n" +
                            "- You can manage or cancel subscriptions in your Google Play account settings.\n" +
                            "- Refunds are handled according to Google Play's refund policy.\n" +
                            "- We reserve the right to change subscription pricing with reasonable notice.",
                )
            }

            // 7. Intellectual Property
            item {
                TermsSection(
                    icon = Icons.Filled.Copyright,
                    title = "7. Intellectual Property",
                    description = "All content, features, and functionality of the App — including but not limited to text, graphics, logos, icons, and software — are the exclusive property of AI Tube Navigator and are protected by copyright and other intellectual property laws.\n\n" +
                            "TfL data is used under TfL's Open Data policy. The London Underground roundel is a registered trademark of Transport for London.",
                )
            }

            // 8. Termination
            item {
                TermsSection(
                    icon = Icons.Filled.Block,
                    title = "8. Termination",
                    description = "We may suspend or terminate your access to the App at any time, without prior notice, for conduct that we believe violates these Terms or is harmful to other users or third parties.\n\n" +
                            "You may stop using the App at any time by uninstalling it. Upon termination, all locally stored data will be deleted.",
                )
            }

            // 9. Governing Law
            item {
                TermsSection(
                    icon = Icons.Filled.AccountBalance,
                    title = "9. Governing Law",
                    description = "These Terms shall be governed by and construed in accordance with the laws of England and Wales, without regard to conflict of law provisions. Any disputes arising from these Terms shall be subject to the exclusive jurisdiction of the courts of England and Wales.",
                )
            }

            // Agreement Card
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
                            text = "By using AI Tube Navigator, you acknowledge that you have read, understood, and agree to be bound by these Terms of Service and our Privacy Policy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp,
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = "Contact: legal@aitubenavigator.com",
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
                    text = "Last updated: 1 March 2026 · AI Tube Navigator v1.0.0",
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
