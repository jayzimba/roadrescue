package com.jayjaycode.miniproject.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jayjaycode.miniproject.data.OrderStatus
import com.jayjaycode.miniproject.ui.theme.GreenAccent
import com.jayjaycode.miniproject.ui.theme.OrangePrimary
import com.jayjaycode.miniproject.ui.theme.TextSecondary

fun OrderStatus.displayLabel(): String = when (this) {
    OrderStatus.PENDING -> "Pending"
    OrderStatus.CONFIRMED -> "Confirmed"
    OrderStatus.COMPLETED -> "Completed"
    OrderStatus.CANCELLED -> "Cancelled"
}

@Composable
fun OrderStatusBadge(status: OrderStatus, modifier: Modifier = Modifier) {
    val color = when (status) {
        OrderStatus.PENDING -> OrangePrimary
        OrderStatus.CONFIRMED -> MaterialTheme.colorScheme.primary
        OrderStatus.COMPLETED -> GreenAccent
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }
    Text(
        text = status.displayLabel(),
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = color,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
fun OrderStatusTracker(
    status: OrderStatus,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Order status", fontWeight = FontWeight.SemiBold)
            if (status == OrderStatus.CANCELLED) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text(
                        "This order was cancelled.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                val steps = listOf(
                    OrderStatus.PENDING to "Placed",
                    OrderStatus.CONFIRMED to "Confirmed",
                    OrderStatus.COMPLETED to "Completed",
                )
                val currentIndex = steps.indexOfFirst { it.first == status }.coerceAtLeast(0)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    steps.forEachIndexed { index, (_, label) ->
                        val isDone = index < currentIndex
                        val isCurrent = index == currentIndex
                        val dotColor = when {
                            isDone || isCurrent -> GreenAccent
                            else -> TextSecondary.copy(alpha = 0.35f)
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(dotColor.copy(alpha = if (isCurrent) 1f else 0.85f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isDone) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp),
                                    )
                                } else {
                                    Text(
                                        "${index + 1}",
                                        color = if (isCurrent) Color.White else TextSecondary,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                            Text(
                                label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCurrent) MaterialTheme.colorScheme.onSurface else TextSecondary,
                                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        }
                    }
                }
                val hint = when (status) {
                    OrderStatus.PENDING -> "Waiting for the seller to confirm your order."
                    OrderStatus.CONFIRMED -> "Seller confirmed — your order is being prepared."
                    OrderStatus.COMPLETED -> "This order has been completed."
                    OrderStatus.CANCELLED -> ""
                }
                if (hint.isNotBlank()) {
                    Text(hint, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
    }
}
