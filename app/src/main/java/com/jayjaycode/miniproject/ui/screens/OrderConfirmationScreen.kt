package com.jayjaycode.miniproject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jayjaycode.miniproject.data.PartOrder
import com.jayjaycode.miniproject.ui.components.AppTopBar
import com.jayjaycode.miniproject.ui.theme.GreenAccent
import com.jayjaycode.miniproject.ui.theme.TextSecondary
import com.jayjaycode.miniproject.ui.viewmodel.MarketplaceViewModel
import com.jayjaycode.miniproject.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderConfirmationScreen(
    viewModel: MarketplaceViewModel,
    onDone: () -> Unit,
) {
    val order by viewModel.lastPlacedOrder.collectAsState()

    if (order == null) {
        Scaffold(topBar = { AppTopBar(title = "Order confirmation", onBack = onDone) }) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("No order found", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onDone) { Text("Back to marketplace") }
            }
        }
        return
    }

    val placedOrder = order ?: return

    Scaffold(topBar = { AppTopBar(title = "Order placed") }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = GreenAccent,
                modifier = Modifier.size(72.dp),
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "Order placed",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Your order has been sent to the seller. They will confirm shortly.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            OrderSummaryCard(order = placedOrder)
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    viewModel.clearLastPlacedOrder()
                    onDone()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Continue shopping")
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(order: PartOrder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SummaryRow("Order ID", order.id.take(8).uppercase())
            SummaryRow("Items", "${order.items.sumOf { it.quantity }}")
            order.items.forEach { line ->
                SummaryRow(
                    line.name,
                    "${line.quantity} × ${CurrencyFormatter.format(line.unitPrice)}",
                )
            }
            SummaryRow("Total", CurrencyFormatter.format(order.totalPrice))
            order.paymentMethod?.let { SummaryRow("Payment", it.label) }
            if (order.deliveryPhone.isNotBlank()) {
                SummaryRow("Phone", order.deliveryPhone)
            }
            if (order.deliveryAddress.isNotBlank()) {
                SummaryRow("Delivery", order.deliveryAddress)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(value, fontWeight = FontWeight.Medium)
    }
}
