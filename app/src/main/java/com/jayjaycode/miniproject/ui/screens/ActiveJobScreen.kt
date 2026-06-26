package com.jayjaycode.miniproject.ui.screens

import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.jayjaycode.miniproject.data.RequestType
import com.google.android.gms.maps.model.LatLng
import com.jayjaycode.miniproject.ui.components.JobTrackingMap
import com.jayjaycode.miniproject.ui.components.PriceTag
import com.jayjaycode.miniproject.ui.components.RescueMap
import com.jayjaycode.miniproject.ui.components.StatChip
import com.jayjaycode.miniproject.ui.theme.GreenAccent
import com.jayjaycode.miniproject.ui.theme.TextSecondary
import com.jayjaycode.miniproject.ui.viewmodel.RescueViewModel
import com.jayjaycode.miniproject.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveJobScreen(
    onDone: () -> Unit,
    viewModel: RescueViewModel = viewModel(),
) {
    val job by viewModel.acceptedJob.collectAsState()
    val trackingProgress by viewModel.trackingProgress.collectAsState()
    val remainingEta by viewModel.remainingEta.collectAsState()

    LaunchedEffect(job) {
        if (job == null) onDone()
    }

    val activeJob = job ?: return
    val isTowing = activeJob.request.type == RequestType.TOWING
    val displayEta = if (remainingEta > 0) remainingEta else activeJob.acceptedBid.etaMinutes
    val photoUris = activeJob.request.photoUris.mapNotNull { runCatching { Uri.parse(it) }.getOrNull() }
    val pickup = LatLng(activeJob.request.latitude, activeJob.request.longitude)
    val approachFactor = 1f - trackingProgress
    val providerPosition = LatLng(
        pickup.latitude + approachFactor * 0.015,
        pickup.longitude + approachFactor * 0.015,
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (isTowing) "Tow truck en route" else "Mechanic en route") })
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
                            "${activeJob.mechanicShop.name} is on the way",
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
                progress = trackingProgress,
                providerName = activeJob.mechanicShop.name,
                etaMinutes = displayEta,
                isTowing = isTowing,
            )

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatChip("ETA", "$displayEta min", highlight = true)
                StatChip("Distance", "${"%.1f".format(activeJob.acceptedBid.distanceKm)} km")
                StatChip("Price", CurrencyFormatter.formatCompact(activeJob.acceptedBid.price))
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
                    if (activeJob.request.damageDescription.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            activeJob.request.damageDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
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

            if (photoUris.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("Your photos", fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    items(photoUris, key = { it.toString() }) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "Breakdown photo",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(activeJob.mechanicShop.name, fontWeight = FontWeight.Bold)
                    Text("★ ${activeJob.mechanicShop.rating} · ${activeJob.mechanicShop.reviewCount} reviews")
                    Spacer(Modifier.height(8.dp))
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

            Button(
                onClick = {
                    viewModel.completeJobAndClear()
                    onDone()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Back to home")
            }
        }
    }
}
