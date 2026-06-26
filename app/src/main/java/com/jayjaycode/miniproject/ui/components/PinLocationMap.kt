package com.jayjaycode.miniproject.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.jayjaycode.miniproject.ui.theme.TextSecondary
import com.jayjaycode.miniproject.util.LocationHelper
import com.jayjaycode.miniproject.util.LocationResult
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun PinLocationMap(
    latitude: Double?,
    longitude: Double?,
    onLocationPinned: (LocationResult) -> Unit,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 200.dp,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationHelper = remember { LocationHelper(context) }

    val initialPosition = if (latitude != null && longitude != null) {
        LatLng(latitude, longitude)
    } else {
        LusakaCenter
    }

    val markerState = rememberMarkerState(position = initialPosition)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 15f)
    }

    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) {
            val target = LatLng(latitude, longitude)
            if (markerState.position != target) {
                markerState.position = target
            }
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(target, 15f))
        }
    }

    var isResolving by remember { mutableStateOf(false) }
    var skipNextDrag by remember { mutableStateOf(false) }

    fun resolvePin(position: LatLng) {
        scope.launch {
            isResolving = true
            val label = locationHelper.reverseGeocode(position.latitude, position.longitude)
            onLocationPinned(
                LocationResult(
                    latitude = position.latitude,
                    longitude = position.longitude,
                    label = label,
                ),
            )
            isResolving = false
        }
    }

    LaunchedEffect(markerState) {
        snapshotFlow { markerState.position }
            .distinctUntilChanged { old, new ->
                old.latitude == new.latitude && old.longitude == new.longitude
            }
            .debounce(500)
            .collect { position ->
                if (skipNextDrag) {
                    skipNextDrag = false
                    return@collect
                }
                val matchesExternal = latitude != null && longitude != null &&
                    kotlin.math.abs(position.latitude - latitude) < 0.00001 &&
                    kotlin.math.abs(position.longitude - longitude) < 0.00001
                if (!matchesExternal) {
                    resolvePin(position)
                }
            }
    }

    Column(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxWidth(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = false),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false,
                ),
                onMapClick = { latLng ->
                    skipNextDrag = true
                    markerState.position = latLng
                    resolvePin(latLng)
                },
            ) {
                Marker(
                    state = markerState,
                    title = "Delivery point",
                    draggable = true,
                )
            }
        }

        Text(
            if (isResolving) "Updating address…" else "Tap the map or drag the pin to set your delivery point.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
