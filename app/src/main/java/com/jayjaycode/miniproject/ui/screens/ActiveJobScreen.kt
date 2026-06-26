package com.jayjaycode.miniproject.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.jayjaycode.miniproject.data.CompletionParty
import com.jayjaycode.miniproject.data.RequestType
import com.jayjaycode.miniproject.ui.components.AppTopBar
import com.jayjaycode.miniproject.ui.components.BreakdownPhotoStrip
import com.jayjaycode.miniproject.ui.components.JobTrackingMap
import com.jayjaycode.miniproject.ui.components.PriceTag
import com.jayjaycode.miniproject.ui.components.RescueMap
import com.jayjaycode.miniproject.ui.components.StatChip
import com.jayjaycode.miniproject.ui.screens.auth.AuthErrorBanner
import com.jayjaycode.miniproject.ui.theme.GreenAccent
import com.jayjaycode.miniproject.ui.theme.OrangePrimary
import com.jayjaycode.miniproject.ui.theme.TextSecondary
import com.jayjaycode.miniproject.ui.viewmodel.RescueViewModel
import com.jayjaycode.miniproject.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveJobScreen(
    viewModel: RescueViewModel,
    onBrowseApp: () -> Unit,
) {
    val job by viewModel.acceptedJob.collectAsState()
    val actionError by viewModel.actionError.collectAsState()
    val activeJob = job ?: return

    val isTowing = activeJob.request.type == RequestType.TOWING
    val photoUrls = activeJob.request.photoUris.filter { it.isNotBlank() }
    val pickup = LatLng(activeJob.request.latitude, activeJob.request.longitude)
    val providerPosition = LatLng(
        pickup.latitude + 0.008,
        pickup.longitude + 0.008,
    )
    val request = activeJob.request
    val completionLabel = viewModel.customerCompletionActionLabel(request)
    val pendingMessage = viewModel.customerCompletionPendingMessage(request)

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (isTowing) "Towing job" else "Mechanic job",
                onBack = onBrowseApp,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GreenAccent.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = GreenAccent)
                    Column {
                        Text("Request accepted!", fontWeight = FontWeight.Bold)
                        Text(
                            "${activeJob.mechanicShop.name} is en route",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            RescueMap(
                pickupLocation = pickup,
                providerLocation = providerPosition,
                showMyLocation = false,
                height = 220.dp,
            )

            Spacer(Modifier.height(12.dp))

            JobTrackingMap(
                progress = null,
                providerName = activeJob.mechanicShop.name,
                etaMinutes = activeJob.acceptedBid.etaMinutes,
                isTowing = isTowing,
            )

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatChip("ETA", "${activeJob.acceptedBid.etaMinutes} min", highlight = true)
                StatChip("Distance", "${"%.1f".format(activeJob.acceptedBid.distanceKm)} km")
                StatChip("Price", CurrencyFormatter.formatCompact(activeJob.acceptedBid.price))
            }

            pendingMessage?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = OrangePrimary, style = MaterialTheme.typography.bodySmall)
            }

            actionError?.let {
                Spacer(Modifier.height(8.dp))
                AuthErrorBanner(it)
            }

            Spacer(Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsCar, null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            " ${activeJob.request.vehicle.make} ${activeJob.request.vehicle.model}",
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(activeJob.request.problemDescription, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = TextSecondary, modifier = Modifier.height(18.dp))
                        Text(
                            activeJob.request.locationLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }

            if (photoUrls.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                BreakdownPhotoStrip(photoUrls = photoUrls, height = 72.dp)
            }

            Spacer(Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(activeJob.mechanicShop.name, fontWeight = FontWeight.Bold)
                    PriceTag(activeJob.acceptedBid.price)
                    Text(activeJob.acceptedBid.message, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Call provider")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            when (completionLabel) {
                "Confirm completion" -> {
                    Button(
                        onClick = { viewModel.confirmJobCompletion() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Confirm job complete")
                    }
                }
                "Mark job complete" -> {
                    Button(
                        onClick = { viewModel.requestJobCompletion() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Mark job complete")
                    }
                }
                else -> {
                    if (request.completionRequestedBy == CompletionParty.CUSTOMER) {
                        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth(), enabled = false) {
                            Text("Waiting for provider confirmation")
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onBrowseApp, modifier = Modifier.fillMaxWidth()) {
                Text("Browse app")
            }
        }
    }
}
