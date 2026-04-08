package com.londontubeai.navigator.ui.screens.privacy

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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
fun PrivacyScreen(onBack: () -> Unit = {}) {
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
                    Text("Privacy Policy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("GDPR & Google Play Compliant", style = MaterialTheme.typography.labelSmall, color = StatusGood)
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = Spacing.xl, vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            item {
                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = "Effective Date: 1 March 2026",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = "AI Tube Navigator (\"we\", \"our\", \"the App\") is committed to protecting your privacy. This Privacy Policy explains what information we collect, how we use it, and your rights regarding your data. By using the App you agree to the practices described below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                )
                Spacer(modifier = Modifier.height(Spacing.lg))
            }

            // 1. Data Collection
            item {
                PrivacySection(
                    icon = Icons.Filled.Storage,
                    title = "1. Information We Collect",
                    description = "We follow a local-first architecture. All AI predictions, route calculations, and crowd analysis run entirely on your device.\n\n" +
                            "Data stored locally on your device:\n" +
                            "- Saved home/work stations and favourites\n" +
                            "- Journey search history\n" +
                            "- App preferences and settings\n" +
                            "- Cached TfL line status data\n\n" +
                            "We do NOT collect or store:\n" +
                            "- Your name, email, or personal identity\n" +
                            "- Contacts, photos, or files\n" +
                            "- Payment information (handled by Google Play)\n" +
                            "- Browsing history or cross-app data",
                )
            }

            // 2. Location
            item {
                PrivacySection(
                    icon = Icons.Filled.MyLocation,
                    title = "2. Location Data",
                    description = "Location data is used solely for nearby station detection and live arrival information. Location is accessed only when the App is in the foreground and you have granted permission.\n\n" +
                            "- Location is never stored permanently, transmitted to our servers, or shared with third parties.\n" +
                            "- You may revoke location permission at any time via your device Settings without losing core App functionality.\n" +
                            "- If location is unavailable, the App falls back to showing central London stations.",
                )
            }

            // 3. Third-Party Services
            item {
                PrivacySection(
                    icon = Icons.Filled.Share,
                    title = "3. Third-Party Services",
                    description = "The App communicates with the following external services:\n\n" +
                            "- Transport for London (TfL) Unified API: To fetch live line status, arrival predictions, and station data. No personal data is sent. TfL's privacy policy applies to their services.\n" +
                            "- Open-Meteo Weather API: To display weather conditions. Only device coordinates are sent; no user identity.\n" +
                            "- Google Maps SDK: To render maps. Subject to Google's Privacy Policy.\n" +
                            "- Google Play Billing: For premium subscription processing. Managed entirely by Google.\n\n" +
                            "We do not use advertising SDKs, social media trackers, or sell data to any third party.",
                )
            }

            // 4. Analytics
            item {
                PrivacySection(
                    icon = Icons.Filled.Analytics,
                    title = "4. Analytics & Crash Reporting",
                    description = "Anonymous, aggregated usage analytics may be collected to improve app quality. No personally identifiable information is included.\n\n" +
                            "- Analytics can be opted out of at any time in Settings.\n" +
                            "- Crash reports contain only technical device information (OS version, device model) and stack traces, with no personal data.",
                )
            }

            // 5. Notifications
            item {
                PrivacySection(
                    icon = Icons.Filled.Notifications,
                    title = "5. Notifications",
                    description = "Push notifications are used for disruption alerts and commute reminders only. Notification preferences are fully customisable and can be disabled per channel or entirely in your device Settings.",
                )
            }

            // 6. Data Security
            item {
                PrivacySection(
                    icon = Icons.Filled.Lock,
                    title = "6. Data Security",
                    description = "- All local data is stored in your device's private app storage, inaccessible to other apps.\n" +
                            "- All network communication uses HTTPS/TLS encryption.\n" +
                            "- The App enforces a strict network security configuration that blocks cleartext (HTTP) traffic.\n" +
                            "- We follow OWASP Mobile Security best practices.",
                )
            }

            // 7. Data Retention & Deletion
            item {
                PrivacySection(
                    icon = Icons.Filled.DeleteForever,
                    title = "7. Data Retention & Deletion",
                    description = "- All data is stored locally on your device and can be deleted at any time by clearing the App's data or uninstalling the App.\n" +
                            "- Cached TfL data is automatically refreshed and old entries are purged.\n" +
                            "- We do not retain any data on remote servers.",
                )
            }

            // 8. Children's Privacy
            item {
                PrivacySection(
                    icon = Icons.Filled.ChildCare,
                    title = "8. Children's Privacy",
                    description = "The App is not directed at children under 13. We do not knowingly collect personal information from children. If you believe a child has provided us with data, please contact us and we will take steps to remove it.",
                )
            }

            // 9. International Transfers
            item {
                PrivacySection(
                    icon = Icons.Filled.Language,
                    title = "9. International Data",
                    description = "API requests to TfL and Open-Meteo may be processed on servers outside your country. These requests contain no personal data. No personal information leaves your device.",
                )
            }

            // 10. Policy Changes
            item {
                PrivacySection(
                    icon = Icons.Filled.Update,
                    title = "10. Changes to This Policy",
                    description = "We may update this Privacy Policy from time to time. Changes will be posted within the App and the \"Effective Date\" above will be updated. Continued use of the App after changes constitutes acceptance of the revised policy.",
                )
            }

            // GDPR Rights Card
            item {
                Spacer(modifier = Modifier.height(Spacing.md))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TubePrimary.copy(alpha = 0.06f)),
                ) {
                    Column(modifier = Modifier.padding(Spacing.lg)) {
                        Text("Your Rights Under GDPR", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = "If you are located in the European Economic Area (EEA) or the United Kingdom, you have the following rights under the General Data Protection Regulation:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp,
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        listOf(
                            "Right of access — request a copy of your data",
                            "Right to rectification — correct inaccurate data",
                            "Right to erasure — delete all stored data",
                            "Right to data portability — receive data in a portable format",
                            "Right to withdraw consent — at any time",
                            "Right to object — to data processing",
                            "Right to lodge a complaint — with a supervisory authority",
                        ).forEach { right ->
                            Text("- $right", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                        }
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = "Since all data is stored locally on your device, you can exercise these rights by clearing the App's data or uninstalling the App. For any enquiries:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp,
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = "Contact: privacy@aitubenavigator.com",
                            style = MaterialTheme.typography.labelMedium,
                            color = TubePrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // Google Play Data Safety summary
            item {
                Spacer(modifier = Modifier.height(Spacing.md))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusGood.copy(alpha = 0.06f)),
                ) {
                    Column(modifier = Modifier.padding(Spacing.lg)) {
                        Text("Google Play Data Safety Summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        listOf(
                            "No data shared with third parties",
                            "No personal data collected",
                            "Location data accessed but not stored",
                            "Data encrypted in transit (HTTPS)",
                            "You can request data deletion",
                        ).forEach { item ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                Icon(Icons.Filled.Policy, null, tint = StatusGood, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(Spacing.sm))
                                Text(item, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
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
private fun PrivacySection(icon: ImageVector, title: String, description: String) {
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
