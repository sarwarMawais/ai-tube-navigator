package com.londontubeai.navigator.ui.screens.licenses

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.londontubeai.navigator.ui.theme.Spacing
import com.londontubeai.navigator.ui.theme.TubePrimary

private data class OssLibrary(
    val name: String,
    val author: String,
    val license: String,
    val url: String,
)

private val libraries = listOf(
    OssLibrary("Jetpack Compose", "Google / Android Open Source Project", "Apache License 2.0", "https://developer.android.com/jetpack/compose"),
    OssLibrary("Jetpack Compose Material 3", "Google / Android Open Source Project", "Apache License 2.0", "https://developer.android.com/jetpack/compose"),
    OssLibrary("AndroidX Core KTX", "Google / Android Open Source Project", "Apache License 2.0", "https://developer.android.com/jetpack/androidx"),
    OssLibrary("AndroidX Lifecycle", "Google / Android Open Source Project", "Apache License 2.0", "https://developer.android.com/jetpack/androidx/releases/lifecycle"),
    OssLibrary("AndroidX Navigation Compose", "Google / Android Open Source Project", "Apache License 2.0", "https://developer.android.com/jetpack/compose/navigation"),
    OssLibrary("AndroidX Room", "Google / Android Open Source Project", "Apache License 2.0", "https://developer.android.com/jetpack/androidx/releases/room"),
    OssLibrary("AndroidX DataStore", "Google / Android Open Source Project", "Apache License 2.0", "https://developer.android.com/topic/libraries/architecture/datastore"),
    OssLibrary("Hilt (Dagger)", "Google", "Apache License 2.0", "https://dagger.dev/hilt/"),
    OssLibrary("Retrofit", "Square, Inc.", "Apache License 2.0", "https://square.github.io/retrofit/"),
    OssLibrary("OkHttp", "Square, Inc.", "Apache License 2.0", "https://square.github.io/okhttp/"),
    OssLibrary("Moshi", "Square, Inc.", "Apache License 2.0", "https://github.com/square/moshi"),
    OssLibrary("Google Maps SDK for Android", "Google", "Google Maps Platform Terms of Service", "https://developers.google.com/maps/documentation/android-sdk"),
    OssLibrary("Google Play Billing Library", "Google", "Android Software Development Kit License", "https://developer.android.com/google/play/billing"),
    OssLibrary("Kotlin Coroutines", "JetBrains", "Apache License 2.0", "https://github.com/Kotlin/kotlinx.coroutines"),
    OssLibrary("Kotlin Standard Library", "JetBrains", "Apache License 2.0", "https://kotlinlang.org/"),
    OssLibrary("Accompanist Permissions", "Google", "Apache License 2.0", "https://google.github.io/accompanist/permissions/"),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LicensesScreen(onBack: () -> Unit = {}) {
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    var copyToastShown by remember { mutableStateOf(false) }

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
                    Text("Open-Source Licenses", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${libraries.size} libraries used", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search libraries...", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
            }

            item {
                Text(
                    text = "AI Tube Navigator is built with the following open-source libraries. We are grateful to the developers and communities behind these projects.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = Spacing.sm),
                )
            }

            val filteredLibraries = if (searchQuery.isBlank()) {
                libraries
            } else {
                libraries.filter { lib ->
                    lib.name.contains(searchQuery, ignoreCase = true) ||
                    lib.author.contains(searchQuery, ignoreCase = true) ||
                    lib.license.contains(searchQuery, ignoreCase = true)
                }
            }

            items(filteredLibraries) { lib ->
                LicenseCard(
                    library = lib,
                    onUrlClick = {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(lib.url)))
                        } catch (e: Exception) {
                            // Handle invalid URL
                        }
                    },
                    onLongPress = {
                        val licenseText = "${lib.name}\n${lib.author}\nLicense: ${lib.license}\n${lib.url}"
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("License", licenseText))
                        copyToastShown = true
                        android.widget.Toast.makeText(context, "License copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                    },
                )
            }

            item {
                Spacer(modifier = Modifier.height(Spacing.xxxl))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LicenseCard(
    library: OssLibrary,
    onUrlClick: () -> Unit,
    onLongPress: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .combinedClickable(
                onClick = { expanded = !expanded },
                onLongClick = onLongPress,
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Code, null, tint = TubePrimary, modifier = Modifier.padding(end = Spacing.sm))
                Column(modifier = Modifier.weight(1f)) {
                    Text(library.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(library.author, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                ) {
                    Column(modifier = Modifier.padding(Spacing.sm)) {
                        Row {
                            Text("License: ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text(library.license, style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.combinedClickable(
                                onClick = onUrlClick,
                                onLongClick = onLongPress,
                            ),
                        ) {
                            Text(library.url, style = MaterialTheme.typography.labelSmall, color = TubePrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Filled.ContentCopy, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }
    }
}
