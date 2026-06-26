package com.jayjaycode.miniproject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jayjaycode.miniproject.data.CompletionParty
import com.jayjaycode.miniproject.data.RequestStatus
import com.jayjaycode.miniproject.data.RequestType
import com.jayjaycode.miniproject.ui.components.AppTopBar
import com.jayjaycode.miniproject.ui.components.BreakdownPhotoStrip
import com.jayjaycode.miniproject.ui.theme.GreenAccent
import com.jayjaycode.miniproject.ui.theme.OrangePrimary
import com.jayjaycode.miniproject.ui.theme.TextSecondary
import com.jayjaycode.miniproject.ui.viewmodel.BreakdownRequestDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val detailDateFormat = SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreakdownRequestDetailScreen(
    onBack: () -> Unit,
    viewModel: BreakdownRequestDetailViewModel,
) {
    val request by viewModel.request.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = when (request?.type) {
                    RequestType.TOWING -> "Towing request"
                    RequestType.MECHANIC -> "Mechanic request"
                    null -> "Request details"
                },
                onBack = onBack,
            )
        },
    ) { padding ->
        when (val current = request) {
            null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Loading request…", color = TextSecondary)
                }
            }
            else -> {
                val isTowing = current.type == RequestType.TOWING
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isTowing) Icons.Default.LocalShipping else Icons.Default.Build,
                            contentDescription = null,
                            tint = OrangePrimary,
                        )
                        Text(
                            if (isTowing) "Towing" else "Mechanic",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    StatusBadge(current.status)
                    current.completionRequestedBy?.let { party ->
                        Text(
                            when (party) {
                                CompletionParty.CUSTOMER -> "Customer requested completion — awaiting provider"
                                CompletionParty.PROVIDER -> "Provider requested completion — awaiting customer"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = OrangePrimary,
                        )
                    }
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Vehicle", fontWeight = FontWeight.SemiBold)
                            Text("${current.vehicle.make} ${current.vehicle.model} (${current.vehicle.year})")
                            Text("Plate: ${current.vehicle.plateNumber}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Breakdown", fontWeight = FontWeight.SemiBold)
                            Text(current.problemDescription)
                            if (current.damageDescription.isNotBlank()) {
                                Text(current.damageDescription, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Location", fontWeight = FontWeight.SemiBold)
                            Text(current.locationLabel)
                            Text(
                                detailDateFormat.format(Date(current.createdAtMillis)),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                            )
                        }
                    }
                    if (current.photoUris.isNotEmpty()) {
                        BreakdownPhotoStrip(photoUrls = current.photoUris)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: RequestStatus) {
    val (label, color) = when (status) {
        RequestStatus.BIDDING -> "Bidding" to OrangePrimary
        RequestStatus.ACCEPTED, RequestStatus.IN_PROGRESS -> "Active" to GreenAccent
        RequestStatus.COMPLETED -> "Completed" to GreenAccent
        RequestStatus.CANCELLED -> "Cancelled" to TextSecondary
        RequestStatus.DRAFT -> "Draft" to TextSecondary
    }
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.15f)) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
        )
    }
}
