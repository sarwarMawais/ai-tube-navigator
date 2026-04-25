package com.londontubeai.navigator.ui.screens.privacy

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.CloudDownload
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
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.londontubeai.navigator.ui.theme.Spacing
import com.londontubeai.navigator.ui.theme.StatusGood
import com.londontubeai.navigator.ui.theme.StatusSevere
import com.londontubeai.navigator.ui.theme.TubeAccent
import com.londontubeai.navigator.ui.theme.TubePrimary
import kotlinx.coroutines.launch

@Composable
fun PrivacyScreen(
    onBack: () -> Unit = {},
    viewModel: PrivacyViewModel = hiltViewModel(),
) {
    val analyticsEnabled by viewModel.analyticsEnabled.collectAsState(initial = false)
    val crashReportsEnabled by viewModel.crashReportsEnabled.collectAsState(initial = false)
    val personalisationEnabled by viewModel.personalisationEnabled.collectAsState(initial = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteConfirmed by remember { mutableStateOf(false) }
    LaunchedEffect(deleteConfirmed) {
        if (deleteConfirmed) {
            android.widget.Toast.makeText(
                context,
                "All local data erased",
                android.widget.Toast.LENGTH_LONG,
            ).show()
            deleteConfirmed = false
        }
    }

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
                    Text("Privacy Policy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("GDPR & Google Play Compliant", style = MaterialTheme.typography.labelSmall, color = StatusGood)
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = Spacing.xl, vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            // ── Reading-time estimate ──────────────────────────
            item {
                Text(
                    "⏱ 4 minute read",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.md),
                )
            }

            // ── TL;DR card — plain English summary ─────────────
            // Gives users the 15-second version of the policy so the
            // legal detail below is reassurance rather than homework.
            item { TldrCard() }
            item { Spacer(modifier = Modifier.height(Spacing.md)) }

            // ── Action buttons — export / delete / open online ─
            item {
                PrivacyActionsRow(
                    onExport = {
                        scope.launch {
                            val json = viewModel.exportAsJson(
                                currentAnalytics = analyticsEnabled,
                                currentCrashReports = crashReportsEnabled,
                                currentPersonalisation = personalisationEnabled,
                            )
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_SUBJECT, "My AI Tube Navigator data")
                                putExtra(Intent.EXTRA_TEXT, json)
                            }
                            runCatching {
                                context.startActivity(
                                    Intent.createChooser(sendIntent, "Export my data"),
                                )
                            }
                        }
                    },
                    onDelete = { showDeleteDialog = true },
                    onOpenOnline = {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://sarwarMawais.github.io/ai-tube-navigator/")),
                            )
                        }
                    },
                )
            }
            item { Spacer(modifier = Modifier.height(Spacing.md)) }

            // ── Opt-out toggles ────────────────────────────────
            item {
                OptOutToggles(
                    analytics = analyticsEnabled,
                    crashReports = crashReportsEnabled,
                    personalisation = personalisationEnabled,
                    onAnalyticsChange = { scope.launch { viewModel.setAnalytics(it) } },
                    onCrashReportsChange = { scope.launch { viewModel.setCrashReports(it) } },
                    onPersonalisationChange = { scope.launch { viewModel.setPersonalisation(it) } },
                )
            }
            item { Spacer(modifier = Modifier.height(Spacing.lg)) }

            item {
                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = "Effective Date: 11 April 2026",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = "AI Tube Navigator (\"we\", \"our\", or \"the App\") is committed to protecting your privacy. This Privacy Policy explains what information we collect, how we use it, and your rights when using the App. By downloading or using AI Tube Navigator, you agree to the practices described in this policy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                )
                Spacer(modifier = Modifier.height(Spacing.lg))
            }

            item {
                PrivacySection(
                    icon = Icons.Filled.Storage,
                    title = "1. Information We Collect",
                    description = "We collect only the minimum data required to provide the service.\n\n" +
                        "On-device only (never transmitted): journey search history, saved journeys, favourite stations, route preferences, notification settings, and app theme preferences. All stored locally via Android DataStore and Room database.\n\n" +
                        "We do NOT collect: your name, email address, contacts, photos, financial information, or any data that personally identifies you.",
                )
            }

            item {
                PrivacySection(
                    icon = Icons.Filled.MyLocation,
                    title = "2. Location Data",
                    description = "With your permission, the App uses your device's GPS to show nearby stations, calculate walking times, and pre-fill your journey origin. Location is processed on-device and is never stored on our servers or shared with third parties.\n\n" +
                        "You can revoke location permission at any time in your device Settings → Apps → AI Tube Navigator → Permissions.",
                )
            }

            item {
                PrivacySection(
                    icon = Icons.Filled.Share,
                    title = "3. Third-Party Services",
                    description = "The App communicates with the following external services:\n\n" +
                        "• TfL Open Data API (api.tfl.gov.uk) — to fetch live train arrivals, line statuses, and route planning. No personal data is sent; only station/line identifiers and your optional API key.\n\n" +
                        "• OpenWeatherMap API — to show current weather near your location. Only a coarse city-level coordinate is sent. Governed by OpenWeatherMap's Privacy Policy.\n\n" +
                        "• Google Maps SDK — to render the interactive tube map. Governed by Google's Privacy Policy.\n\n" +
                        "• Google Play Billing — for premium subscriptions, handled entirely by Google. We do not receive or store payment card details.",
                )
            }

            item {
                PrivacySection(
                    icon = Icons.Filled.Analytics,
                    title = "4. Analytics & Crash Reporting",
                    description = "The App does not currently use third-party analytics or crash-reporting SDKs. If this changes in a future version, this policy will be updated and users notified. Any future analytics will be aggregated and anonymised.",
                )
            }

            item {
                PrivacySection(
                    icon = Icons.Filled.Notifications,
                    title = "5. Notifications",
                    description = "With your permission, the App may send push notifications about service disruptions and commute reminders. You can manage or disable these at any time in your device Settings → Apps → AI Tube Navigator → Notifications, or within the App's Settings screen.",
                )
            }

            item {
                PrivacySection(
                    icon = Icons.Filled.Lock,
                    title = "6. Data Security",
                    description = "All on-device data is stored in Android's private app storage (inaccessible to other apps). All API requests use HTTPS/TLS encryption. The App's network security configuration prevents cleartext (HTTP) traffic. We follow Android security best practices for data handling.",
                )
            }

            item {
                PrivacySection(
                    icon = Icons.Filled.DeleteForever,
                    title = "7. Data Retention & Deletion",
                    description = "All data is stored locally on your device and is automatically deleted when you uninstall the App. You can also manually clear all cached data and preferences at any time via Settings → Data & Storage → Clear Cache or Reset Preferences. We do not hold any personal data on remote servers.",
                )
            }

            item {
                PrivacySection(
                    icon = Icons.Filled.ChildCare,
                    title = "8. Children's Privacy",
                    description = "AI Tube Navigator is intended for users aged 13 and over. We do not knowingly collect personal information from children under 13. If you believe a child under 13 has provided us with personal data, please contact us and we will take steps to remove such information.",
                )
            }

            item {
                PrivacySection(
                    icon = Icons.Filled.Language,
                    title = "9. International Data Transfers",
                    description = "All personal data (preferences, history) is stored locally on your device and is not transferred internationally. API requests to TfL, OpenWeatherMap, and Google are routed to their respective servers, which may be located outside the UK or EEA. These transfers are governed by each provider's data processing agreements.",
                )
            }

            item {
                PrivacySection(
                    icon = Icons.Filled.Update,
                    title = "10. Changes to This Policy",
                    description = "We may update this Privacy Policy from time to time. We will notify you of significant changes by updating the \"Effective Date\" above and, where appropriate, displaying an in-app notice. Continued use of the App after changes are posted constitutes your acceptance of the updated policy.",
                )
            }

            item {
                Spacer(modifier = Modifier.height(Spacing.md))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TubePrimary.copy(alpha = 0.06f)),
                ) {
                    Column(modifier = Modifier.padding(Spacing.lg)) {
                        Text("Your GDPR Rights (if applicable)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = "Under UK GDPR and the Data Protection Act 2018, UK residents have the right to: access their data, rectify inaccurate data, erase their data (\"right to be forgotten\"), data portability, withdraw consent at any time, object to processing, and lodge a complaint with the Information Commissioner's Office (ICO) at ico.org.uk.\n\nAs all data is stored locally on your device, you can exercise most of these rights directly by clearing app data or uninstalling the App.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp,
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
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

    // ── Delete confirmation dialog ─────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Filled.DeleteForever, null, tint = StatusSevere) },
            title = { Text("Delete all data?") },
            text = {
                Text(
                    "This will permanently erase:\n" +
                        "• All saved routes\n" +
                        "• Favourite stations\n" +
                        "• Preferences & settings\n" +
                        "• Recent searches\n\n" +
                        "This action cannot be undone.",
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.deleteAll()
                            deleteConfirmed = true
                            showDeleteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusSevere),
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

// ─── TL;DR card ───────────────────────────────────────────────────────────
@Composable
private fun TldrCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StatusGood.copy(alpha = 0.08f)),
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CheckCircle, null, tint = StatusGood, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "TL;DR",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = StatusGood,
                )
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = "We store everything locally on your device. We don't collect your name, email, contacts, or payment details. " +
                    "Location is processed on-device and never stored on our servers. " +
                    "You can export or delete your data at any time using the buttons below.",
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp,
            )
        }
    }
}

// ─── Action buttons row ────────────────────────────────────────────────────
@Composable
private fun PrivacyActionsRow(
    onExport: () -> Unit,
    onDelete: () -> Unit,
    onOpenOnline: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        ActionChip(Icons.Filled.CloudDownload, "Export", TubePrimary, onExport, Modifier.weight(1f))
        ActionChip(Icons.Filled.DeleteForever, "Delete all", StatusSevere, onDelete, Modifier.weight(1f))
        ActionChip(Icons.AutoMirrored.Filled.OpenInNew, "Online", TubeAccent, onOpenOnline, Modifier.weight(1f))
    }
}

@Composable
private fun ActionChip(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(44.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = tint.copy(alpha = 0.1f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = tint)
        }
    }
}

// ─── Opt-out toggles ──────────────────────────────────────────────────────
@Composable
private fun OptOutToggles(
    analytics: Boolean,
    crashReports: Boolean,
    personalisation: Boolean,
    onAnalyticsChange: (Boolean) -> Unit,
    onCrashReportsChange: (Boolean) -> Unit,
    onPersonalisationChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                "Data-sharing preferences",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            ToggleRow(
                icon = Icons.Filled.Analytics,
                label = "Anonymous analytics",
                description = "Help us improve the app (no personal data)",
                checked = analytics,
                onCheckedChange = onAnalyticsChange,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = Spacing.xs))
            ToggleRow(
                icon = Icons.Filled.BugReport,
                label = "Crash reports",
                description = "Send anonymous crash logs to help fix bugs",
                checked = crashReports,
                onCheckedChange = onCrashReportsChange,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = Spacing.xs))
            ToggleRow(
                icon = Icons.Filled.Tune,
                label = "Personalisation",
                description = "Learn your commute habits to suggest better routes",
                checked = personalisation,
                onCheckedChange = onPersonalisationChange,
            )
        }
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = TubePrimary, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
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
