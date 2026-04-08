package com.londontubeai.navigator.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AccessibleForward
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsPhone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.londontubeai.navigator.data.model.TubeData
import com.londontubeai.navigator.ui.theme.StatusGood
import com.londontubeai.navigator.ui.theme.StatusSevere
import com.londontubeai.navigator.ui.theme.TubeAccent
import com.londontubeai.navigator.ui.theme.Spacing
import com.londontubeai.navigator.ui.theme.TubePrimary
import com.londontubeai.navigator.ui.theme.TubeSecondary
import com.londontubeai.navigator.ui.components.UnifiedHeader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToPremium: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onNavigateToLicenses: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val prefs by viewModel.prefsState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Station picker state
    var showStationPicker by remember { mutableStateOf(false) }
    var stationPickerTarget by remember { mutableStateOf("home") } // "home" or "work"

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
    ) { scaffoldPadding ->
    Column(modifier = Modifier.fillMaxSize().padding(scaffoldPadding)) {
        // ── Unified Header ────────────────────────────
        UnifiedHeader(
            title = "Settings",
            subtitle = "Personalise your experience",
            icon = Icons.Filled.SettingsPhone,
        )

        LazyColumn(
            contentPadding = PaddingValues(bottom = Spacing.lg),
        ) {
            // ── Upgrade to Premium banner ──────────────────────
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.xl, vertical = Spacing.md)
                        .clickable(onClick = onNavigateToPremium),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.Star, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Upgrade to Pro", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            Text("Unlock AI predictions, alerts & more", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f))
                        }
                        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
                    }
                }
            }

            item {
                SettingsOverviewCard(prefs = prefs)
            }

            // ═══════════════════════════════════════════════════
            // 1. ROUTE PREFERENCES
            // ═══════════════════════════════════════════════════
            item { SectionTitle("Route Preferences") }
            item {
                SettingsCard {
                    SettingsToggle(
                        title = "Fastest Route",
                        description = "Prioritise the quickest journey time",
                        icon = Icons.Filled.Speed,
                        iconColor = TubePrimary,
                        checked = prefs.preferFastest,
                        onToggle = { scope.launch { viewModel.setPreferFastest(it) } },
                    )
                    SettingsDivider()
                    SettingsToggle(
                        title = "Avoid Crowds",
                        description = "AI suggests less crowded routes and times",
                        icon = Icons.Filled.Group,
                        iconColor = TubeSecondary,
                        checked = prefs.preferLessCrowds,
                        onToggle = { scope.launch { viewModel.setPreferLessCrowds(it) } },
                    )
                    SettingsDivider()
                    SettingsToggle(
                        title = "Less Walking",
                        description = "Prefer routes with shorter walking distances",
                        icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                        iconColor = StatusGood,
                        checked = prefs.preferLessWalking,
                        onToggle = { scope.launch { viewModel.setPreferLessWalking(it) } },
                    )
                    SettingsDivider()
                    SettingsToggle(
                        title = "Step-Free Access",
                        description = "Only show step-free routes (lifts, ramps)",
                        icon = Icons.AutoMirrored.Filled.AccessibleForward,
                        iconColor = TubeAccent,
                        checked = prefs.preferStepFree,
                        onToggle = { scope.launch { viewModel.setPreferStepFree(it) } },
                    )
                }
            }
            // Advanced route sliders
            item {
                SettingsCard {
                    SettingsSlider(
                        title = "Max Walking Distance",
                        icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                        iconColor = StatusGood,
                        value = prefs.maxWalkingMetres.toFloat(),
                        valueRange = 100f..1500f,
                        steps = 13,
                        valueLabel = "${prefs.maxWalkingMetres}m",
                        onValueChangeFinished = { scope.launch { viewModel.setMaxWalkingMetres(it.toInt()) } },
                    )
                    SettingsDivider()
                    SettingsSlider(
                        title = "Max Interchanges",
                        icon = Icons.Filled.SwapHoriz,
                        iconColor = TubeSecondary,
                        value = prefs.maxInterchanges.toFloat(),
                        valueRange = 0f..5f,
                        steps = 4,
                        valueLabel = "${prefs.maxInterchanges}",
                        onValueChangeFinished = { scope.launch { viewModel.setMaxInterchanges(it.toInt()) } },
                    )
                }
            }

            // ═══════════════════════════════════════════════════
            // 2. YOUR COMMUTE
            // ═══════════════════════════════════════════════════
            item { SectionTitle("Your Commute") }
            item {
                SettingsCard {
                    SettingsNavItem(
                        title = "Home Station",
                        description = "Set your home station for quick routes",
                        icon = Icons.Filled.Home,
                        iconColor = StatusGood,
                        value = prefs.homeStationName ?: "Tap to set",
                        onClick = {
                            stationPickerTarget = "home"
                            showStationPicker = true
                        },
                    )
                    SettingsDivider()
                    SettingsNavItem(
                        title = "Work Station",
                        description = "Set your work station for commute insights",
                        icon = Icons.Filled.Work,
                        iconColor = TubePrimary,
                        value = prefs.workStationName ?: "Tap to set",
                        onClick = {
                            stationPickerTarget = "work"
                            showStationPicker = true
                        },
                    )
                }
            }

            // ═══════════════════════════════════════════════════
            // 3. APPEARANCE
            // ═══════════════════════════════════════════════════
            item { SectionTitle("Appearance") }
            item {
                SettingsCard {
                    // Theme selector
                    ThemeSelector(
                        current = prefs.darkMode,
                        onSelect = { scope.launch { viewModel.setDarkMode(it) } },
                    )
                    SettingsDivider()
                    SettingsToggle(
                        title = "High Contrast",
                        description = "Increase contrast for better visibility",
                        icon = Icons.Filled.Contrast,
                        iconColor = TubePrimary,
                        checked = prefs.highContrast,
                        onToggle = { scope.launch { viewModel.setHighContrast(it) } },
                    )
                    SettingsDivider()
                    SettingsToggle(
                        title = "Large Text",
                        description = "Increase font size throughout the app",
                        icon = Icons.Filled.FormatSize,
                        iconColor = TubeSecondary,
                        checked = prefs.largeText,
                        onToggle = { scope.launch { viewModel.setLargeText(it) } },
                    )
                }
            }

            // ═══════════════════════════════════════════════════
            // 4. NOTIFICATIONS
            // ═══════════════════════════════════════════════════
            item { SectionTitle("Notifications") }
            item {
                SettingsCard {
                    SettingsToggle(
                        title = "Disruption Alerts",
                        description = "Get notified when your lines are disrupted",
                        icon = Icons.Filled.Notifications,
                        iconColor = TubeAccent,
                        checked = prefs.pushDisruptions,
                        onToggle = { scope.launch { viewModel.setPushDisruptions(it) } },
                    )
                    SettingsDivider()
                    if (prefs.pushDisruptions) {
                        SettingsToggle(
                            title = "Severe Disruptions Only",
                            description = "Only notify for major service issues",
                            icon = Icons.Filled.Warning,
                            iconColor = StatusSevere,
                            checked = prefs.severeOnly,
                            onToggle = { scope.launch { viewModel.setSevereOnly(it) } },
                        )
                        SettingsDivider()
                    }
                    SettingsToggle(
                        title = "Commute Reminders",
                        description = "Time-to-leave and commute status alerts",
                        icon = Icons.Filled.NotificationsActive,
                        iconColor = TubeSecondary,
                        checked = prefs.pushCommute,
                        onToggle = { scope.launch { viewModel.setPushCommute(it) } },
                    )
                    SettingsDivider()
                    SettingsToggle(
                        title = "AI Tips & Insights",
                        description = "Personalised AI-powered travel suggestions",
                        icon = Icons.Filled.Lightbulb,
                        iconColor = Color(0xFFFFA000),
                        checked = prefs.pushAiTips,
                        onToggle = { scope.launch { viewModel.setPushAiTips(it) } },
                    )
                    if (prefs.pushDisruptions || prefs.pushCommute || prefs.pushAiTips) {
                        SettingsDivider()
                        SettingsToggle(
                            title = "Quiet Hours",
                            description = "No notifications between 22:00\u201307:00",
                            icon = Icons.Filled.DoNotDisturb,
                            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            checked = prefs.quietHours,
                            onToggle = { scope.launch { viewModel.setQuietHours(it) } },
                        )
                    }
                }
            }

            // ═══════════════════════════════════════════════════
            // 5. LOCATION
            // ═══════════════════════════════════════════════════
            item { SectionTitle("Location") }
            item {
                SettingsCard {
                    SettingsToggle(
                        title = "Live Location",
                        description = "Use GPS for nearby stations and real-time ETAs",
                        icon = Icons.Filled.LocationOn,
                        iconColor = TubePrimary,
                        checked = prefs.liveLocation,
                        onToggle = { scope.launch { viewModel.setLiveLocation(it) } },
                    )
                }
            }

            // ═══════════════════════════════════════════════════
            // 6. DATA & STORAGE
            // ═══════════════════════════════════════════════════
            item { SectionTitle("Data & Storage") }
            item {
                SettingsCard {
                    SettingsNavItem(
                        title = "Clear Cache",
                        description = "Free up storage by clearing cached data",
                        icon = Icons.Filled.DeleteSweep,
                        iconColor = StatusSevere,
                        value = "",
                        onClick = {
                            scope.launch {
                                viewModel.clearCache()
                                snackbarHostState.showSnackbar("Cache cleared successfully")
                            }
                        },
                    )
                    SettingsDivider()
                    SettingsNavItem(
                        title = "Reset Preferences",
                        description = "Restore all settings to defaults",
                        icon = Icons.Filled.SettingsPhone,
                        iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        value = "",
                        onClick = {
                            scope.launch {
                                viewModel.resetPreferences()
                                snackbarHostState.showSnackbar("Preferences reset to defaults")
                            }
                        },
                    )
                }
            }

            // ═══════════════════════════════════════════════════
            // 7. PRIVACY & ABOUT
            // ═══════════════════════════════════════════════════
            item { SectionTitle("Privacy & About") }
            item {
                SettingsCard {
                    SettingsNavItem(
                        title = "Privacy Policy",
                        description = "GDPR compliance & data handling",
                        icon = Icons.Filled.PrivacyTip,
                        iconColor = TubeSecondary,
                        value = "",
                        onClick = onNavigateToPrivacy,
                    )
                    SettingsDivider()
                    SettingsNavItem(
                        title = "Terms of Service",
                        description = "Usage terms and conditions",
                        icon = Icons.Filled.Code,
                        iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        value = "",
                        onClick = onNavigateToTerms,
                    )
                    SettingsDivider()
                    SettingsNavItem(
                        title = "Open-Source Licences",
                        description = "Third-party libraries used in this app",
                        icon = Icons.Filled.Code,
                        iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        value = "",
                        onClick = onNavigateToLicenses,
                    )
                    SettingsDivider()
                    Column(modifier = Modifier.padding(Spacing.lg)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(TubePrimary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.Info, null, tint = TubePrimary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("AI Tube Navigator v1.0.0", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("Powered by TfL Open API · On-device AI", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(Spacing.lg)) }
        }
    }
    } // end Scaffold

    // ── Station Picker Bottom Sheet ────────────────────────
    if (showStationPicker) {
        StationPickerSheet(
            title = if (stationPickerTarget == "home") "Select Home Station" else "Select Work Station",
            onDismiss = { showStationPicker = false },
            onStationSelected = { stationId ->
                scope.launch {
                    if (stationPickerTarget == "home") {
                        viewModel.setHomeStation(stationId)
                    } else {
                        viewModel.setWorkStation(stationId)
                    }
                }
                showStationPicker = false
            },
        )
    }
}

@Composable
private fun SettingsOverviewCard(prefs: SettingsPrefsState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xl, vertical = Spacing.xs),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                text = "Current setup",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                OverviewPill(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Home,
                    label = "Home",
                    value = prefs.homeStationName ?: "Not set",
                    tint = StatusGood,
                )
                OverviewPill(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Work,
                    label = "Work",
                    value = prefs.workStationName ?: "Not set",
                    tint = TubePrimary,
                )
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                OverviewPill(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Palette,
                    label = "Theme",
                    value = prefs.darkMode.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                    tint = TubeSecondary,
                )
                OverviewPill(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Notifications,
                    label = "Alerts",
                    value = if (prefs.pushDisruptions) {
                        if (prefs.severeOnly) "Severe only" else "All disruptions"
                    } else {
                        "Off"
                    },
                    tint = TubeAccent,
                )
            }
        }
    }
}

@Composable
private fun OverviewPill(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationPickerSheet(
    title: String,
    onDismiss: () -> Unit,
    onStationSelected: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    val stations = remember { TubeData.stations.values.sortedBy { it.name } }
    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) stations
        else stations.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, "Close")
                }
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search stations...", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = TubePrimary, modifier = Modifier.size(20.dp)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TubePrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                ),
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            LazyColumn(
                modifier = Modifier.height(400.dp),
            ) {
                items(filtered, key = { it.id }) { station ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStationSelected(station.id) }
                            .padding(vertical = Spacing.md, horizontal = Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(TubePrimary.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.DirectionsSubway,
                                null,
                                tint = TubePrimary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = station.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = "Zone ${station.zone} · ${station.lineIds.size} line${if (station.lineIds.size > 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// REUSABLE COMPOSABLES
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xl, vertical = Spacing.xs),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = Spacing.screenHorizontal),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    )
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = Spacing.xl, end = Spacing.xl, top = Spacing.lg, bottom = Spacing.sm),
    )
}

@Composable
private fun SettingsNavItem(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
            )
        }
        if (value.isNotEmpty()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = TubePrimary,
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            Icons.Filled.ChevronRight,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedTrackColor = TubePrimary,
                checkedThumbColor = Color.White,
            ),
        )
    }
}

@Composable
private fun ThemeSelector(
    current: String,
    onSelect: (String) -> Unit,
) {
    Column(Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(TubePrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Palette, null, tint = TubePrimary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text("Theme", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("Choose app appearance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }
        Spacer(modifier = Modifier.height(Spacing.md))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            val options = listOf(
                Triple("system", "System", Icons.Filled.SettingsPhone),
                Triple("light", "Light", Icons.Filled.LightMode),
                Triple("dark", "Dark", Icons.Filled.DarkMode),
            )
            options.forEach { (key, label, icon) ->
                FilterChip(
                    selected = current == key,
                    onClick = { onSelect(key) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, null, Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TubePrimary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                    ),
                )
            }
        }
    }
}

@Composable
private fun SettingsSlider(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueLabel: String,
    onValueChangeFinished: (Float) -> Unit,
) {
    var sliderValue by remember(value) { mutableFloatStateOf(value) }
    Column(Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Surface(shape = RoundedCornerShape(8.dp), color = iconColor.copy(alpha = 0.1f)) {
                Text(valueLabel, Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = iconColor)
            }
        }
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onValueChangeFinished(sliderValue) },
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = iconColor,
                activeTrackColor = iconColor,
                inactiveTrackColor = iconColor.copy(alpha = 0.15f),
            ),
        )
    }
}
