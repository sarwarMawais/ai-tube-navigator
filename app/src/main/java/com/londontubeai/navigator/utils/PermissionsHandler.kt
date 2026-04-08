package com.londontubeai.navigator.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

data class PermissionsState(
    val locationPermissionGranted: Boolean = false,
    val notificationPermissionGranted: Boolean = false,
    val shouldShowLocationRationale: Boolean = false,
    val shouldShowNotificationRationale: Boolean = false,
)

@Composable
fun rememberPermissionsHandler(
    onPermissionsResult: (PermissionsState) -> Unit
): PermissionsHandler {
    val context = LocalContext.current
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        onPermissionsResult(
            PermissionsState(
                locationPermissionGranted = locationGranted,
                notificationPermissionGranted = checkNotificationPermission(context),
            )
        )
    }
    
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onPermissionsResult(
            PermissionsState(
                locationPermissionGranted = checkLocationPermission(context),
                notificationPermissionGranted = granted,
            )
        )
    }
    
    return remember {
        PermissionsHandler(
            context = context,
            locationPermissionLauncher = { locationPermissionLauncher.launch(LOCATION_PERMISSIONS) },
            notificationPermissionLauncher = { 
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        )
    }
}

class PermissionsHandler(
    private val context: Context,
    private val locationPermissionLauncher: () -> Unit,
    private val notificationPermissionLauncher: () -> Unit,
) {
    fun requestLocationPermission() {
        locationPermissionLauncher()
    }
    
    fun requestNotificationPermission() {
        notificationPermissionLauncher()
    }
    
    fun requestAllPermissions() {
        if (!checkLocationPermission(context)) {
            locationPermissionLauncher()
        }
        if (!checkNotificationPermission(context)) {
            notificationPermissionLauncher()
        }
    }
    
    fun getPermissionsState(): PermissionsState {
        return PermissionsState(
            locationPermissionGranted = checkLocationPermission(context),
            notificationPermissionGranted = checkNotificationPermission(context),
        )
    }
}

private fun checkLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

private fun checkNotificationPermission(context: Context): Boolean {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)
