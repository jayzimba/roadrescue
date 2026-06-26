package com.jayjaycode.miniproject.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jayjaycode.miniproject.data.OrderStatus
import com.jayjaycode.miniproject.data.PartOrder
import com.jayjaycode.miniproject.data.ServiceBookingOrder
import com.jayjaycode.miniproject.ui.components.AppTopBar
import com.jayjaycode.miniproject.ui.components.OrderStatusBadge
import com.jayjaycode.miniproject.ui.components.OrderStatusTracker
import com.jayjaycode.miniproject.ui.components.PriceTag
import com.jayjaycode.miniproject.ui.theme.TextSecondary
import com.jayjaycode.miniproject.ui.viewmodel.MyOrdersViewModel
import com.jayjaycode.miniproject.ui.viewmodel.PartOrderDetailViewModel
import com.jayjaycode.miniproject.ui.viewmodel.ServiceBookingDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class BuyerOrderListItem {
    abstract val id: String
    abstract val createdAtMillis: Long
    abstract val status: OrderStatus
    abstract val title: String
    abstract val subtitle: String
    abstract val amount: Double

    data class Part(val order: PartOrder) : BuyerOrderListItem() {
        override val id = order.id
        override val createdAtMillis = order.createdAtMillis
        override val status = order.status
        override val title = "${order.items.sumOf { it.quantity }} item(s) from ${order.shopName}"
        override val subtitle = order.deliveryAddress.ifBlank { order.shopName }
        override val amount = order.totalPrice
    }

    data class Service(val booking: ServiceBookingOrder) : BuyerOrderListItem() {
        override val id = booking.id
        override val createdAtMillis = booking.createdAtMillis
        override val status = booking.status
        override val title = booking.serviceName
        override val subtitle = "${booking.shopName} · ${booking.preferredDate}"
        override val amount = booking.price
    }
}

private val orderDateFormat = SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.getDefault())

private fun formatOrderDate(millis: Long): String =
    if (millis > 0L) orderDateFormat.format(Date(millis)) else "—"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    onBack: () -> Unit,
    onOpenPartOrder: (String) -> Unit,
    onOpenServiceBooking: (String) -> Unit,
    viewModel: MyOrdersViewModel = viewModel(),
) {
    val orders by viewModel.allOrders.collectAsState()

    Scaffold(topBar = { AppTopBar(title = "My orders", onBack = onBack) }) { padding ->
        if (orders.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("No orders yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Part orders and service bookings will appear here.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Text("My orders", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "Track status of your marketplace orders and service bookings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(8.dp))
                }
                items(orders, key = { "${it::class.simpleName}-${it.id}" }) { item ->
                    BuyerOrderCard(
                        item = item,
                        onClick = {
                            when (item) {
                                is BuyerOrderListItem.Part -> onOpenPartOrder(item.id)
                                is BuyerOrderListItem.Service -> onOpenServiceBooking(item.id)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun BuyerOrderCard(
    item: BuyerOrderListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    when (item) {
                        is BuyerOrderListItem.Part -> "Part order"
                        is BuyerOrderListItem.Service -> "Service booking"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
                OrderStatusBadge(status = item.status)
            }
            Text(item.title, fontWeight = FontWeight.Medium)
            Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(formatOrderDate(item.createdAtMillis), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                PriceTag(item.amount)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartOrderDetailScreen(
    onBack: () -> Unit,
    viewModel: PartOrderDetailViewModel = viewModel(),
) {
    val order by viewModel.order.collectAsState()

    Scaffold(topBar = { AppTopBar(title = "Order details", onBack = onBack) }) { padding ->
        when (val current = order) {
            null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Loading order…", color = TextSecondary)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        Text("Part order", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Order #${current.id.take(8).uppercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                    item { OrderStatusTracker(status = current.status) }
                    item {
                        OrderDetailSection(title = "Seller") {
                            DetailRow("Shop", current.shopName)
                            DetailRow("Placed", formatOrderDate(current.createdAtMillis))
                        }
                    }
                    item {
                        OrderDetailSection(title = "Items") {
                            current.items.forEach { line ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(line.name, fontWeight = FontWeight.Medium)
                                        Text(
                                            "${line.category} · Qty ${line.quantity}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                        )
                                    }
                                    PriceTag(line.lineTotal)
                                }
                                if (line != current.items.last()) Spacer(Modifier.height(10.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Total", fontWeight = FontWeight.SemiBold)
                                PriceTag(current.totalPrice)
                            }
                        }
                    }
                    item {
                        OrderDetailSection(title = "Delivery & payment") {
                            current.paymentMethod?.let { DetailRow("Payment", it.label) }
                            if (current.deliveryPhone.isNotBlank()) DetailRow("Phone", current.deliveryPhone)
                            if (current.deliveryAddress.isNotBlank()) DetailRow("Address", current.deliveryAddress)
                            if (current.deliveryLatitude != null && current.deliveryLongitude != null) {
                                DetailRow(
                                    "Coordinates",
                                    "${"%.5f".format(current.deliveryLatitude)}, ${"%.5f".format(current.deliveryLongitude)}",
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceBookingDetailScreen(
    onBack: () -> Unit,
    viewModel: ServiceBookingDetailViewModel = viewModel(),
) {
    val booking by viewModel.booking.collectAsState()

    Scaffold(topBar = { AppTopBar(title = "Booking details", onBack = onBack) }) { padding ->
        when (val current = booking) {
            null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Loading booking…", color = TextSecondary)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        Text("Service booking", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Booking #${current.id.take(8).uppercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                    item { OrderStatusTracker(status = current.status) }
                    item {
                        OrderDetailSection(title = "Service") {
                            DetailRow("Service", current.serviceName)
                            DetailRow("Provider", current.shopName)
                            DetailRow("Vehicle", current.vehicleNote)
                            DetailRow("Preferred date", current.preferredDate)
                            DetailRow("Placed", formatOrderDate(current.createdAtMillis))
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Price from", fontWeight = FontWeight.SemiBold)
                                PriceTag(current.price)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderDetailSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(value, fontWeight = FontWeight.Medium)
    }
}
