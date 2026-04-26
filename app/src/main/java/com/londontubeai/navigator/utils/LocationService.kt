package com.londontubeai.navigator.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Fetch the device's current location with strict robustness guarantees:
     *
     *  1. First we try the last cached location via `lastLocation` — this returns
     *     almost instantly when available and unblocks the UI for returning users.
     *  2. If no cached fix exists we request a **fresh** fix via `getCurrentLocation`
     *     with BALANCED accuracy (not HIGH) so indoor / emulator users still get a
     *     reading instead of hanging forever.
     *  3. The whole call is wrapped in a 12-second `withTimeoutOrNull` — if nothing
     *     comes back by then we return a clear failure instead of hanging the Home
     *     screen indefinitely (which was the bug reported by users: "nearby
     *     stations never load").
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Result<Location> {
        // Step 1 — try cached location first. Quick and avoids GPS warm-up.
        runCatching { fetchLastLocation() }
            .getOrNull()
            ?.takeIf { it.isRecent() }
            ?.let { return Result.success(it) }

        // Step 2 — request a fresh fix, but with a timeout so we never hang.
        val fresh = withTimeoutOrNull(FRESH_FIX_TIMEOUT_MS) {
            runCatching { fetchFreshLocation() }.getOrNull()
        }
        if (fresh != null) return Result.success(fresh)

        // Step 3 — last-resort fallback: any cached fix, even an old one.
        val stale = runCatching { fetchLastLocation() }.getOrNull()
        return if (stale != null) {
            Result.success(stale)
        } else {
            Result.failure(IllegalStateException("Location unavailable — check GPS and permissions"))
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchLastLocation(): Location? = suspendCancellableCoroutine { cont ->
        fusedLocationClient.lastLocation
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchFreshLocation(): Location = suspendCancellableCoroutine { continuation ->
        val cancellationTokenSource = CancellationTokenSource()
        // PRIORITY_BALANCED_POWER_ACCURACY (~100m) is plenty for nearby-station
        // lookups and works reliably indoors / on emulators, unlike HIGH_ACCURACY.
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            cancellationTokenSource.token,
        ).addOnSuccessListener { location ->
            if (location != null) continuation.resume(location)
            else continuation.resumeWithException(IllegalStateException("Location is null"))
        }.addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
        continuation.invokeOnCancellation { cancellationTokenSource.cancel() }
    }

    /** Consider a cached location "fresh" if it was taken in the last 5 minutes. */
    private fun Location.isRecent(): Boolean =
        System.currentTimeMillis() - time < FRESH_LOCATION_WINDOW_MS

    companion object {
        private const val FRESH_FIX_TIMEOUT_MS = 12_000L
        private const val FRESH_LOCATION_WINDOW_MS = 5 * 60 * 1000L
    }

    /**
     * Calculate distance between two coordinates in kilometers using Haversine formula
     */
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
    ): Double {
        val earthRadius = 6371.0 // km
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }

    suspend fun reverseGeocode(latitude: Double, longitude: Double): String? = withContext(Dispatchers.IO) {
        runCatching {
            val geocoder = Geocoder(context, Locale.UK)
            val address = geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull() ?: return@runCatching null
            listOfNotNull(
                address.thoroughfare?.takeIf { it.isNotBlank() },
                address.subLocality?.takeIf { it.isNotBlank() },
                address.locality?.takeIf { it.isNotBlank() },
                address.adminArea?.takeIf { it.isNotBlank() },
            ).distinct().take(2).joinToString(", ").ifBlank { null }
        }.getOrNull()
    }
}
