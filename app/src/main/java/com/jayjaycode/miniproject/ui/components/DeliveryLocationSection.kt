package com.jayjaycode.miniproject.ui.components

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.jayjaycode.miniproject.ui.theme.TextSecondary
import com.jayjaycode.miniproject.ui.theme.formOutlinedTextFieldColors
import com.jayjaycode.miniproject.util.LocationHelper
import com.jayjaycode.miniproject.util.LocationResult
import kotlinx.coroutines.launch

@Composable
fun DeliveryLocationSection(
    address: String,
    onAddressChange: (String) -> Unit,
    latitude: Double?,
    longitude: Double?,
    onLocationSelected: (LocationResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationHelper = remember { LocationHelper(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val placesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val data = result.data ?: return@rememberLauncherForActivityResult
        val place = Autocomplete.getPlaceFromIntent(data)
        val latLng = place.latLng ?: run {
            errorMessage = "Selected place has no map location"
            return@rememberLauncherForActivityResult
        }
        val label = place.address?.takeIf { it.isNotBlank() }
            ?: place.name?.takeIf { it.isNotBlank() }
            ?: "Selected location"
        onLocationSelected(LocationResult(latLng.latitude, latLng.longitude, label))
        onAddressChange(label)
        errorMessage = null
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val granted = grants.values.any { it }
        if (granted) {
            scope.launch {
                isLoading = true
                errorMessage = null
                locationHelper.getCurrentLocation()
                    .onSuccess { result ->
                        onLocationSelected(result)
                        onAddressChange(result.label)
                    }
                    .onFailure { err -> errorMessage = err.message ?: "Could not get location" }
                isLoading = false
            }
        } else {
            errorMessage = "Location permission denied"
        }
    }

    fun openPlacesSearch() {
        errorMessage = null
        val fields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS,
        )
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .setCountries(listOf("ZM"))
            .build(context)
        placesLauncher.launch(intent)
    }

    fun useCurrentLocation() {
        if (locationHelper.hasLocationPermission()) {
            scope.launch {
                isLoading = true
                errorMessage = null
                locationHelper.getCurrentLocation()
                    .onSuccess { result ->
                        onLocationSelected(result)
                        onAddressChange(result.label)
                    }
                    .onFailure { err -> errorMessage = err.message ?: "Could not get location" }
                isLoading = false
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = { Text("Delivery address") },
            placeholder = { Text("Search, pin on map, or use GPS") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = false,
            minLines = 2,
            colors = formOutlinedTextFieldColors(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = { openPlacesSearch() },
                modifier = Modifier.weight(1f),
                enabled = !isLoading,
            ) {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text("Search")
            }
            OutlinedButton(
                onClick = { useCurrentLocation() },
                modifier = Modifier.weight(1f),
                enabled = !isLoading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.size(6.dp))
                Text("GPS")
            }
        }

        Text("Pin delivery location", fontWeight = FontWeight.Medium)
        PinLocationMap(
            latitude = latitude,
            longitude = longitude,
            onLocationPinned = { result ->
                onLocationSelected(result)
                onAddressChange(result.label)
                errorMessage = null
            },
        )

        if (latitude != null && longitude != null) {
            Text(
                "Pinned: ${"%.5f".format(latitude)}, ${"%.5f".format(longitude)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }

        errorMessage?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}
