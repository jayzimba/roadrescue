package com.jayjaycode.miniproject.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jayjaycode.miniproject.data.RequestType
import com.jayjaycode.miniproject.data.VehicleInfo
import com.jayjaycode.miniproject.data.ZambianVehicleCatalog
import com.jayjaycode.miniproject.ui.screens.auth.AuthErrorBanner
import com.jayjaycode.miniproject.ui.theme.formOutlinedTextFieldColors
import com.jayjaycode.miniproject.ui.components.AppTopBar
import com.jayjaycode.miniproject.ui.components.LocationSection
import com.jayjaycode.miniproject.ui.components.PhotoPickerSection
import com.jayjaycode.miniproject.ui.components.SpannerLoader
import com.jayjaycode.miniproject.ui.components.VehicleDropdown
import com.jayjaycode.miniproject.ui.viewmodel.RescueViewModel
import com.jayjaycode.miniproject.util.LocationResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestFormScreen(
    requestType: RequestType,
    onBack: () -> Unit,
    onSubmitted: () -> Unit,
    viewModel: RescueViewModel = viewModel(),
) {
    var make by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf("") }
    var year by rememberSaveable { mutableStateOf("") }
    var plate by rememberSaveable { mutableStateOf("") }
    var color by rememberSaveable { mutableStateOf("") }
    var problem by rememberSaveable { mutableStateOf("") }
    var damage by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var latitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var longitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var photoUris by rememberSaveable { mutableStateOf(listOf<String>()) }

    val photoUriObjects = photoUris.mapNotNull { runCatching { Uri.parse(it) }.getOrNull() }

    val availableModels = if (make.isNotBlank()) ZambianVehicleCatalog.modelsFor(make) else emptyList()

    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val submitError by viewModel.submitError.collectAsState()
    val title = if (requestType == RequestType.TOWING) "Request Towing" else "Request Mechanic"
    val canSubmit = make.isNotBlank() && model.isNotBlank() && year.isNotBlank() &&
        problem.isNotBlank() && location.isNotBlank() &&
        latitude != null && longitude != null && !isSubmitting

    Scaffold(
        topBar = { AppTopBar(onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                "Describe your breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Online shops nearby will bid. Pick the best offer before the timer ends.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(16.dp))

            Text("Vehicle", fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            VehicleDropdown(
                label = "Make",
                options = ZambianVehicleCatalog.makes,
                selected = make,
                onSelected = { selected ->
                    make = selected
                    model = ""
                },
                placeholder = "Select make",
            )
            Spacer(Modifier.height(8.dp))
            VehicleDropdown(
                label = "Model",
                options = availableModels,
                selected = model,
                onSelected = { model = it },
                enabled = make.isNotBlank(),
                placeholder = if (make.isBlank()) "Select make first" else "Select model",
            )
            Spacer(Modifier.height(8.dp))
            VehicleDropdown(
                label = "Year",
                options = ZambianVehicleCatalog.years,
                selected = year,
                onSelected = { year = it },
                placeholder = "Select year",
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                plate,
                { plate = it },
                label = { Text("Plate number") },
                modifier = Modifier.fillMaxWidth(),
                colors = formOutlinedTextFieldColors(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                color,
                { color = it },
                label = { Text("Color (optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = formOutlinedTextFieldColors(),
            )

            Spacer(Modifier.height(16.dp))
            Text("Breakdown details", fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                problem,
                { problem = it },
                label = { Text("What's the problem?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = formOutlinedTextFieldColors(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                damage,
                { damage = it },
                label = { Text("Damage description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = formOutlinedTextFieldColors(),
            )

            Spacer(Modifier.height(16.dp))
            PhotoPickerSection(
                photoUris = photoUriObjects,
                onPhotosChanged = { uris -> photoUris = uris.map { it.toString() } },
            )

            Spacer(Modifier.height(16.dp))
            LocationSection(
                locationLabel = location,
                onLocationLabelChange = { location = it },
                latitude = latitude,
                longitude = longitude,
                onLocationResolved = { result: LocationResult ->
                    latitude = result.latitude
                    longitude = result.longitude
                },
            )

            submitError?.let { msg ->
                AuthErrorBanner(msg)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    viewModel.submitRequest(
                        type = requestType,
                        vehicle = VehicleInfo(make, model, year, plate, color),
                        problem = problem,
                        damage = damage,
                        location = location,
                        latitude = latitude!!,
                        longitude = longitude!!,
                        photoUris = photoUriObjects,
                        onSuccess = onSubmitted,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canSubmit,
            ) {
                if (isSubmitting) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SpannerLoader(size = 22.dp)
                        Text("Submitting request…")
                    }
                } else {
                    Text("Submit & start bidding")
                }
            }
            if (latitude == null) {
                Text(
                    "Tap \"Use my current location\" to enable GPS pickup",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
