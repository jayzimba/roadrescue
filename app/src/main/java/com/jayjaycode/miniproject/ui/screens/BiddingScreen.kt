package com.jayjaycode.miniproject.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jayjaycode.miniproject.data.MechanicBid
import com.jayjaycode.miniproject.ui.components.AppTopBar
import com.jayjaycode.miniproject.ui.components.PriceTag
import com.jayjaycode.miniproject.ui.components.StatChip
import com.jayjaycode.miniproject.ui.screens.auth.AuthErrorBanner
import com.jayjaycode.miniproject.ui.theme.AmberWarning
import com.jayjaycode.miniproject.ui.theme.OrangePrimary
import com.jayjaycode.miniproject.ui.theme.TextSecondary
import com.jayjaycode.miniproject.ui.viewmodel.RescueViewModel
import com.jayjaycode.miniproject.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiddingScreen(
    viewModel: RescueViewModel,
    onBidAccepted: () -> Unit,
    onBrowseApp: () -> Unit,
    onCancel: () -> Unit,
) {
    val bids by viewModel.bids.collectAsState()
    val secondsLeft by viewModel.secondsLeft.collectAsState()
    val acceptedJob by viewModel.acceptedJob.collectAsState()
    val activeRequest by viewModel.activeRequest.collectAsState()
    val actionError by viewModel.actionError.collectAsState()

    LaunchedEffect(acceptedJob) {
        if (acceptedJob != null) onBidAccepted()
    }

    val progress by animateFloatAsState(
        viewModel.biddingProgressFraction(activeRequest, secondsLeft),
        label = "timer",
    )
    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val timerText = "%02d:%02d".format(minutes, seconds)
    val autoAccept = activeRequest?.autoAcceptLowestBid == true

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Live bids",
                onBack = onBrowseApp,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(100.dp),
                            strokeWidth = 6.dp,
                            color = OrangePrimary,
                            trackColor = OrangePrimary.copy(alpha = 0.2f),
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Timer, null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
                            Text(timerText, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (secondsLeft > 0) "Time left to accept a bid" else "Bidding window closed",
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        "${bids.size} online shop${if (bids.size != 1) "s" else ""} responded",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape),
                        color = OrangePrimary,
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-accept lowest bid", fontWeight = FontWeight.Medium)
                            Text(
                                "When the timer ends, the cheapest bid is accepted automatically.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                            )
                        }
                        Switch(
                            checked = autoAccept,
                            onCheckedChange = { viewModel.setAutoAcceptLowestBid(it) },
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.extendBiddingTime() },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Add 2 minutes")
                        }
                        OutlinedButton(
                            onClick = onBrowseApp,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Browse app")
                        }
                    }
                }
            }

            actionError?.let {
                AuthErrorBanner(it)
                Spacer(Modifier.height(8.dp))
            }

            if (bids.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AmberWarning)
                        Spacer(Modifier.height(12.dp))
                        Text("Waiting for nearby shops…", color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(bids, key = { it.id }) { bid ->
                        BidCard(
                            bid = bid,
                            enabled = secondsLeft > 0 || bids.size == 1,
                            isLowest = bid == bids.firstOrNull(),
                            onAccept = { viewModel.acceptBid(bid) },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                OutlinedButton(
                    onClick = { viewModel.cancelRequest(onDone = onCancel) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Cancel request")
                }
            }
        }
    }
}

@Composable
private fun BidCard(
    bid: MechanicBid,
    enabled: Boolean,
    isLowest: Boolean,
    onAccept: () -> Unit,
) {
    Card(shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(bid.shopName, fontWeight = FontWeight.Bold)
                    Text("★ ${bid.shopRating}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    if (isLowest) {
                        Text("Lowest bid", style = MaterialTheme.typography.labelSmall, color = OrangePrimary)
                    }
                }
                PriceTag(bid.price)
            }
            Spacer(Modifier.height(8.dp))
            Text(bid.message, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip("ETA", "${bid.etaMinutes} min")
                StatChip("Distance", "${"%.1f".format(bid.distanceKm)} km")
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
            ) {
                Text("Accept — ${CurrencyFormatter.formatCompact(bid.price)}")
            }
        }
    }
}
