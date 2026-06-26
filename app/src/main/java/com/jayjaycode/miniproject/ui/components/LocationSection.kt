package com.jayjaycode.miniproject.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jayjaycode.miniproject.ui.theme.formOutlinedTextFieldColors
import com.jayjaycode.miniproject.util.LocationHelper
import com.jayjaycode.miniproject.util.LocationResult
import kotlinx.coroutines.launch

@Composable
fun LocationSection(
    locationLabel: String,
    onLocationLabelChange: (String) -> Unit,
    latitude: Double?,
    longitude: Double?,
    onLocationResolved: (LocationResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationHelper = remember { LocationHelper(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                        onLocationResolved(result)
                        onLocationLabelChange(result.label)
                    }
                    .onFailure { err: Throwable -> errorMessage = err.message ?: "Could not get location" }
                isLoading = false
            }
        } else {
            errorMessage = "Location permission denied"
        }
    }

    fun fetchLocation() {
        if (locationHelper.hasLocationPermission()) {
            scope.launch {
                isLoading = true
                errorMessage = null
                locationHelper.getCurrentLocation()
                    .onSuccess { result ->
                        onLocationResolved(result)
                        onLocationLabelChange(result.label)
                    }
                    .onFailure { err: Throwable -> errorMessage = err.message ?: "Could not get location" }
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

    Column(modifier = modifier) {
        Text("Pickup location", fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = locationLabel,
            onValueChange = onLocationLabelChange,
            label = { Text("Address or landmark") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            colors = formOutlinedTextFieldColors(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { fetchLocation() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.size(8.dp))
            Text(if (isLoading) "Getting GPS…" else "Use my current location")
        }
        if (latitude != null && longitude != null) {
            Spacer(Modifier.height(8.dp))
            CompactRescueMap(
                latitude = latitude,
                longitude = longitude,
                title = "Pickup point",
            )
        }
        errorMessage?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}
