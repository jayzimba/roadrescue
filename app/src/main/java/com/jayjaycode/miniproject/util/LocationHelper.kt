package com.jayjaycode.miniproject.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val label: String,
)

class LocationHelper(private val context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getCurrentLocation(): Result<LocationResult> {
        if (!hasLocationPermission()) {
            return Result.failure(SecurityException("Location permission not granted"))
        }
        return try {
            val location = getLastKnownLocation()
                ?: return Result.failure(Exception("Could not get GPS location"))

            val label = reverseGeocode(location.latitude, location.longitude)
            Result.success(
                LocationResult(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    label = label,
                ),
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getLastKnownLocation(): Location? = suspendCancellableCoroutine { cont ->
        val token = CancellationTokenSource()
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token.token)
            .addOnSuccessListener { loc -> cont.resume(loc) }
            .addOnFailureListener { e ->
                if (cont.isActive) cont.resumeWithException(e)
            }
    }

    @Suppress("DEPRECATION")
    private fun reverseGeocode(lat: Double, lng: Double): String {
        return try {
            if (!Geocoder.isPresent()) return formatCoords(lat, lng)
            val addresses = Geocoder(context, Locale.getDefault()).getFromLocation(lat, lng, 1)
            val address = addresses?.firstOrNull()
            when {
                address != null && !address.getAddressLine(0).isNullOrBlank() ->
                    address.getAddressLine(0)!!
                address?.locality != null -> address.locality!!
                else -> formatCoords(lat, lng)
            }
        } catch (_: Exception) {
            formatCoords(lat, lng)
        }
    }

    private fun formatCoords(lat: Double, lng: Double) =
        "GPS: ${"%.5f".format(lat)}, ${"%.5f".format(lng)}"
}
