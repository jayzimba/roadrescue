package com.jayjaycode.miniproject.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jayjaycode.miniproject.data.BreakdownRequest
import com.jayjaycode.miniproject.data.MechanicBid
import com.jayjaycode.miniproject.data.RequestType
import com.jayjaycode.miniproject.ui.theme.OrangePrimary
import com.jayjaycode.miniproject.ui.theme.TextSecondary
import com.jayjaycode.miniproject.util.CurrencyFormatter

@Composable
fun BiddingFloatingCard(
    request: BreakdownRequest,
    secondsLeft: Int,
    bidCount: Int,
    lowestBid: MechanicBid?,
    progress: Float,
    expanded: Boolean,
    autoAcceptLowest: Boolean,
    onToggleExpanded: () -> Unit,
    onOpenBidding: () -> Unit,
    onExtendTime: () -> Unit,
    onAutoAcceptChanged: (Boolean) -> Unit,
    onAcceptLowest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timerText = formatCountdown(secondsLeft)
    val typeLabel = if (request.type == RequestType.TOWING) "Towing bids" else "Mechanic bids"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpanded),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = OrangePrimary)
                    Column {
                        Text(typeLabel, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                        Text(
                            "$timerText · $bidCount bid${if (bidCount != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
                IconButton(onClick = onToggleExpanded) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                color = OrangePrimary,
            )

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        request.locationLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    lowestBid?.let { bid ->
                        Text(
                            "Lowest: ${CurrencyFormatter.format(bid.price)} · ${bid.shopName}",
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } ?: Text(
                        if (secondsLeft > 0) "Waiting for shop bids…" else "Timer ended — accept a bid or extend",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Auto-accept lowest", style = MaterialTheme.typography.bodySmall)
                        Switch(
                            checked = autoAcceptLowest,
                            onCheckedChange = onAutoAcceptChanged,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = onExtendTime,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("+2 min")
                        }
                        TextButton(
                            onClick = onOpenBidding,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("View bids")
                        }
                    }

                    if (lowestBid != null) {
                        OutlinedButton(
                            onClick = onAcceptLowest,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Accept lowest — ${CurrencyFormatter.format(lowestBid.price)}")
                        }
                    }
                }
            }
        }
    }
}

private fun formatCountdown(secondsLeft: Int): String {
    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    return "%02d:%02d".format(minutes, seconds)
}
