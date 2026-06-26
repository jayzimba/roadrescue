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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jayjaycode.miniproject.data.ActiveJob
import com.jayjaycode.miniproject.data.CompletionParty
import com.jayjaycode.miniproject.data.RequestType
import com.jayjaycode.miniproject.ui.theme.GreenAccent
import com.jayjaycode.miniproject.ui.theme.OrangePrimary
import com.jayjaycode.miniproject.ui.theme.TextSecondary
import com.jayjaycode.miniproject.util.CurrencyFormatter

@Composable
fun ActiveJobFloatingCard(
    job: ActiveJob,
    expanded: Boolean,
    completionActionLabel: String?,
    completionPendingMessage: String?,
    onToggleExpanded: () -> Unit,
    onOpenJob: () -> Unit,
    onRequestCompletion: () -> Unit,
    onConfirmCompletion: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isTowing = job.request.type == RequestType.TOWING
    val typeLabel = if (isTowing) "Towing in progress" else "Mechanic on the way"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = GreenAccent.copy(alpha = 0.12f)),
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
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = GreenAccent)
                    Column {
                        Text(typeLabel, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                        Text(
                            "${job.mechanicShop.name} · ETA ${job.acceptedBid.etaMinutes} min",
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

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(job.request.locationLabel, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(
                        "${CurrencyFormatter.format(job.acceptedBid.price)} · ${job.request.vehicle.make} ${job.request.vehicle.model}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    completionPendingMessage?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = OrangePrimary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(onClick = onOpenJob, modifier = Modifier.weight(1f)) {
                            Text("View job")
                        }
                        when {
                            completionActionLabel == "Confirm completion" -> {
                                Button(
                                    onClick = onConfirmCompletion,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text("Confirm")
                                }
                            }
                            completionActionLabel != null -> {
                                Button(
                                    onClick = onRequestCompletion,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text("Complete")
                                }
                            }
                            job.request.completionRequestedBy == CompletionParty.CUSTOMER -> {
                                TextButton(onClick = {}, modifier = Modifier.weight(1f), enabled = false) {
                                    Text("Pending…")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
