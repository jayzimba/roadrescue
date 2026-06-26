package com.jayjaycode.miniproject.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.jayjaycode.miniproject.ui.theme.TextSecondary

/** Default map center: Lusaka, Zambia */
val LusakaCenter = LatLng(-15.3875, 28.3228)

data class MapMarker(
    val position: LatLng,
    val title: String,
    val snippet: String = "",
)

@Composable
fun RescueMap(
    modifier: Modifier = Modifier,
    userLocation: LatLng? = null,
    pickupLocation: LatLng? = null,
    providerLocation: LatLng? = null,
    showMyLocation: Boolean = true,
    height: androidx.compose.ui.unit.Dp = 200.dp,
) {
    val markers = buildList {
        userLocation?.let { add(MapMarker(it, "You", "Your location")) }
        pickupLocation?.let { add(MapMarker(it, "Pickup", "Breakdown location")) }
        providerLocation?.let { add(MapMarker(it, "Provider", "On the way")) }
    }

    val center = pickupLocation ?: userLocation ?: LusakaCenter
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, 14f)
    }

    LaunchedEffect(center, markers.size) {
        if (markers.size > 1) {
            val builder = com.google.android.gms.maps.model.LatLngBounds.builder()
            markers.forEach { builder.include(it.position) }
            val bounds = builder.build()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 80))
        } else {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(center, 14f))
        }
    }

    var mapLoaded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = showMyLocation),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = showMyLocation,
                ),
                onMapLoaded = { mapLoaded = true },
            ) {
                markers.forEach { marker ->
                    Marker(
                        state = MarkerState(position = marker.position),
                        title = marker.title,
                        snippet = marker.snippet,
                    )
                }
            }
            if (!mapLoaded) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Loading map…", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun CompactRescueMap(
    latitude: Double,
    longitude: Double,
    title: String = "Location",
    modifier: Modifier = Modifier,
) {
    RescueMap(
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        pickupLocation = LatLng(latitude, longitude),
        showMyLocation = false,
        height = 160.dp,
    )
}
