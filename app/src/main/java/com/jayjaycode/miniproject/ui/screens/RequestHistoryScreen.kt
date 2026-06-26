package com.jayjaycode.miniproject.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.jayjaycode.miniproject.ui.components.AppTopBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jayjaycode.miniproject.data.BreakdownRequest
import com.jayjaycode.miniproject.data.RequestStatus
import com.jayjaycode.miniproject.data.RequestType
import com.jayjaycode.miniproject.ui.theme.GreenAccent
import com.jayjaycode.miniproject.ui.theme.OrangePrimary
import com.jayjaycode.miniproject.ui.theme.TextSecondary
import com.jayjaycode.miniproject.ui.viewmodel.RequestHistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestHistoryScreen(
    onBack: () -> Unit,
    onOpenRequest: (String) -> Unit,
    viewModel: RequestHistoryViewModel = viewModel(),
) {
    val history by viewModel.history.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Request history",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (history.isEmpty() && !isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No requests yet", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Your towing and mechanic requests will appear here",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(history, key = { it.id }) { request ->
                        HistoryRequestCard(
                            request = request,
                            onClick = { onOpenRequest(request.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRequestCard(request: BreakdownRequest, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.getDefault())
    val isTowing = request.type == RequestType.TOWING

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
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
                StatusBadge(request.status)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${request.vehicle.make} ${request.vehicle.model} · ${request.vehicle.plateNumber}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                request.problemDescription,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                request.locationLabel,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                dateFormat.format(Date(request.createdAtMillis)),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
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
