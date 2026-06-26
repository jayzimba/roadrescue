package com.jayjaycode.miniproject.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.jayjaycode.miniproject.ui.components.AppTopBar
import com.jayjaycode.miniproject.ui.components.BreakdownPhotoStrip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jayjaycode.miniproject.data.BreakdownRequest
import com.jayjaycode.miniproject.data.CompletionParty
import com.jayjaycode.miniproject.data.OrderStatus
import com.jayjaycode.miniproject.data.PartOrder
import com.jayjaycode.miniproject.data.ProviderBidEntry
import com.jayjaycode.miniproject.data.ProviderBidOutcome
import com.jayjaycode.miniproject.data.RequestType
import com.jayjaycode.miniproject.data.ServiceBookingOrder
import com.jayjaycode.miniproject.data.ServicePackage
import com.jayjaycode.miniproject.data.SparePart
import com.jayjaycode.miniproject.ui.components.PriceTag
import com.jayjaycode.miniproject.ui.screens.auth.AuthErrorBanner
import com.jayjaycode.miniproject.ui.screens.auth.AuthSuccessBanner
import com.jayjaycode.miniproject.ui.theme.GreenAccent
import com.jayjaycode.miniproject.ui.theme.OrangePrimary
import com.jayjaycode.miniproject.ui.theme.TextSecondary
import com.jayjaycode.miniproject.ui.theme.formOutlinedTextFieldColors
import com.jayjaycode.miniproject.ui.viewmodel.ProviderViewModel
import com.jayjaycode.miniproject.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDashboardScreen(
    onBack: () -> Unit,
    onAddPart: () -> Unit,
    onAddService: () -> Unit,
    viewModel: ProviderViewModel = viewModel(),
) {
    val business by viewModel.myBusiness.collectAsState()
    val openJobs by viewModel.openJobs.collectAsState()
    val providerJobs by viewModel.providerJobs.collectAsState()
    val providerBidEntries by viewModel.providerBidEntries.collectAsState()
    val myParts by viewModel.myParts.collectAsState()
    val myServices by viewModel.myServices.collectAsState()
    val incomingOrders by viewModel.incomingOrders.collectAsState()
    val incomingBookings by viewModel.incomingBookings.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var bidTarget by remember { mutableStateOf<BreakdownRequest?>(null) }
    var showAddListingMenu by remember { mutableStateOf(false) }

    val tabs = listOf("Open jobs", "My jobs", "Listings", "Orders")

    Scaffold(
        topBar = { AppTopBar(title = "Provider dashboard", onBack = onBack) },
        floatingActionButton = {
            if (selectedTab == 2 && business != null) {
                Box {
                    FloatingActionButton(
                        onClick = { showAddListingMenu = true },
                        containerColor = OrangePrimary,
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add listing",
                            tint = androidx.compose.ui.graphics.Color.White,
                        )
                    }
                    DropdownMenu(
                        expanded = showAddListingMenu,
                        onDismissRequest = { showAddListingMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add spare part") },
                            onClick = {
                                showAddListingMenu = false
                                onAddPart()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Add service") },
                            onClick = {
                                showAddListingMenu = false
                                onAddService()
                            },
                        )
                    }
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, maxLines = 1) },
                    )
                }
            }

            errorMessage?.let {
                AuthErrorBanner(it)
                Spacer(Modifier.height(4.dp))
            }
            successMessage?.let {
                AuthSuccessBanner(it)
                Spacer(Modifier.height(4.dp))
            }

            if (business == null) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("Register a business from your profile first.")
                }
                return@Column
            }

            if (!business!!.isOnline && selectedTab == 0) {
                Text(
                    "Go online from your profile to place bids on open jobs.",
                    modifier = Modifier.padding(16.dp),
                    color = OrangePrimary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            when (selectedTab) {
                0 -> OpenJobsTab(openJobs, onBid = { bidTarget = it })
                1 -> ProviderJobsTab(
                    bidEntries = providerBidEntries,
                    activeJobs = providerJobs,
                    viewModel = viewModel,
                )
                2 -> ListingsTab(myParts, myServices, onToggleStock = { id, stock ->
                    viewModel.togglePartStock(id, stock)
                })
                3 -> OrdersTab(
                    orders = incomingOrders,
                    bookings = incomingBookings,
                    onConfirmOrder = { viewModel.updateOrderStatus(it, OrderStatus.CONFIRMED) },
                    onCompleteOrder = { viewModel.updateOrderStatus(it, OrderStatus.COMPLETED) },
                    onConfirmBooking = { viewModel.updateBookingStatus(it, OrderStatus.CONFIRMED) },
                    onCompleteBooking = { viewModel.updateBookingStatus(it, OrderStatus.COMPLETED) },
                )
            }
        }
    }

    bidTarget?.let { request ->
        PlaceBidDialog(
            request = request,
            onDismiss = { bidTarget = null },
            onSubmit = { price, eta, message ->
                viewModel.placeBid(request.id, price, eta, message)
                bidTarget = null
            },
        )
    }
}

@Composable
private fun OpenJobsTab(jobs: List<BreakdownRequest>, onBid: (BreakdownRequest) -> Unit) {
    if (jobs.isEmpty()) {
        EmptyState("No open rescue requests right now.")
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(jobs, key = { it.id }) { job ->
            JobCard(job, actionLabel = "Place bid", onAction = { onBid(job) })
        }
    }
}

@Composable
private fun ProviderJobsTab(
    bidEntries: List<ProviderBidEntry>,
    activeJobs: List<BreakdownRequest>,
    viewModel: ProviderViewModel,
) {
    val pendingBids = bidEntries.filter { it.outcome == ProviderBidOutcome.PENDING }
    val wonBids = bidEntries.filter { it.outcome == ProviderBidOutcome.WON }
    val otherBids = bidEntries.filter {
        it.outcome == ProviderBidOutcome.LOST || it.outcome == ProviderBidOutcome.CLOSED
    }
    val activeJobIds = activeJobs.map { it.id }.toSet()
    val wonNotInActive = wonBids.filter { it.requestId !in activeJobIds }

    if (bidEntries.isEmpty() && activeJobs.isEmpty()) {
        EmptyState("No bids or jobs yet. Place bids from the Open jobs tab.")
        return
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (pendingBids.isNotEmpty()) {
            item {
                Text("My bids", fontWeight = FontWeight.SemiBold)
                Text(
                    "Waiting for the customer to accept",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            items(pendingBids, key = { "pending-${it.requestId}" }) { entry ->
                ProviderBidCard(entry)
            }
        }

        if (activeJobs.isNotEmpty()) {
            item {
                Spacer(Modifier.height(if (pendingBids.isNotEmpty()) 8.dp else 0.dp))
                Text("Active jobs", fontWeight = FontWeight.SemiBold)
            }
            items(activeJobs, key = { "active-${it.id}" }) { job ->
                ActiveProviderJobCard(job = job, viewModel = viewModel)
            }
        } else if (wonNotInActive.isNotEmpty()) {
            item {
                Spacer(Modifier.height(if (pendingBids.isNotEmpty()) 8.dp else 0.dp))
                Text("Won bids", fontWeight = FontWeight.SemiBold)
            }
            items(wonNotInActive, key = { "won-${it.requestId}" }) { entry ->
                ProviderBidCard(entry, viewModel = viewModel)
            }
        }

        if (otherBids.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text("Past bids", fontWeight = FontWeight.SemiBold, color = TextSecondary)
            }
            items(otherBids, key = { "past-${it.requestId}" }) { entry ->
                ProviderBidCard(entry)
            }
        }
    }
}

@Composable
private fun ActiveProviderJobCard(
    job: BreakdownRequest,
    viewModel: ProviderViewModel,
) {
    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            JobCard(job, actionLabel = null, onAction = {})
            ProviderCompletionActions(job = job, viewModel = viewModel)
        }
    }
}

@Composable
private fun ProviderCompletionActions(
    job: BreakdownRequest,
    viewModel: ProviderViewModel,
) {
    val pendingMessage = viewModel.providerCompletionPendingMessage(job)
    val actionLabel = viewModel.providerCompletionActionLabel(job)
    pendingMessage?.let {
        Text(it, style = MaterialTheme.typography.bodySmall, color = OrangePrimary)
    }
    when (actionLabel) {
        "Confirm completion" -> {
            Button(
                onClick = { viewModel.confirmJobCompletion(job.id) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Confirm job complete")
            }
        }
        "Mark job complete" -> {
            Button(
                onClick = { viewModel.requestJobCompletion(job.id) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Mark job complete")
            }
        }
        else -> {
            if (job.completionRequestedBy == CompletionParty.PROVIDER) {
                OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth(), enabled = false) {
                    Text("Waiting for customer confirmation")
                }
            }
        }
    }
}

@Composable
private fun ProviderBidCard(
    entry: ProviderBidEntry,
    viewModel: ProviderViewModel? = null,
) {
    val request = entry.request
    val outcomeLabel = when (entry.outcome) {
        ProviderBidOutcome.PENDING -> "Awaiting customer"
        ProviderBidOutcome.WON -> "Bid accepted"
        ProviderBidOutcome.LOST -> "Not selected"
        ProviderBidOutcome.CLOSED -> "Closed"
    }
    val outcomeColor = when (entry.outcome) {
        ProviderBidOutcome.PENDING -> OrangePrimary
        ProviderBidOutcome.WON -> GreenAccent
        ProviderBidOutcome.LOST, ProviderBidOutcome.CLOSED -> TextSecondary
    }

    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    request?.let { if (it.type == RequestType.TOWING) "Towing" else "Mechanic" }
                        ?: "Rescue request",
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary,
                )
                Text(outcomeLabel, style = MaterialTheme.typography.labelSmall, color = outcomeColor)
            }
            request?.let {
                Text(it.locationLabel, fontWeight = FontWeight.Medium)
                Text(
                    "${it.vehicle.make} ${it.vehicle.model} (${it.vehicle.year})",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(it.problemDescription, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                if (it.photoUris.isNotEmpty()) {
                    BreakdownPhotoStrip(photoUrls = it.photoUris)
                }
            } ?: Text(
                "Loading request details…",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Your bid", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    PriceTag(entry.bid.price)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("ETA ${entry.bid.etaMinutes} min", style = MaterialTheme.typography.bodySmall)
                    Text(entry.bid.message, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            if (viewModel != null && entry.outcome == ProviderBidOutcome.WON) {
                entry.request?.let { request ->
                    ProviderCompletionActions(job = request, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun JobCard(job: BreakdownRequest, actionLabel: String?, onAction: () -> Unit) {
    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    if (job.type == RequestType.TOWING) "Towing" else "Mechanic",
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary,
                )
                Text(job.status.name, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Text(job.locationLabel, fontWeight = FontWeight.Medium)
            Text(
                "${job.vehicle.make} ${job.vehicle.model} (${job.vehicle.year})",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(job.problemDescription, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            if (job.photoUris.isNotEmpty()) {
                BreakdownPhotoStrip(photoUrls = job.photoUris)
            }
            actionLabel?.let {
                Button(onClick = onAction, modifier = Modifier.fillMaxWidth()) {
                    Text(it)
                }
            }
        }
    }
}

@Composable
private fun ListingsTab(
    parts: List<SparePart>,
    services: List<ServicePackage>,
    onToggleStock: (String, Boolean) -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Spare parts", fontWeight = FontWeight.SemiBold) }
        if (parts.isEmpty()) {
            item { Text("No parts listed yet.", color = TextSecondary, style = MaterialTheme.typography.bodySmall) }
        } else {
            items(parts, key = { it.id }) { part ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(part.name, fontWeight = FontWeight.Medium)
                            Text(part.category, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            if (part.compatibleVehicles.isNotEmpty()) {
                                Text(
                                    "Fits: ${part.compatibleVehicles.joinToString { it.displayLabel() }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                )
                            }
                            Text(
                                when (val qty = part.quantity) {
                                    null -> "Stock: Unlimited"
                                    else -> "Listed qty: $qty"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                            )
                            PriceTag(part.price)
                        }
                        TextButton(onClick = { onToggleStock(part.id, !part.inStock) }) {
                            Text(if (part.inStock) "Mark out of stock" else "Mark in stock")
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)); Text("Services", fontWeight = FontWeight.SemiBold) }
        if (services.isEmpty()) {
            item { Text("No services listed yet.", color = TextSecondary, style = MaterialTheme.typography.bodySmall) }
        } else {
            items(services, key = { it.id }) { service ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(service.name, fontWeight = FontWeight.Medium)
                        Text(service.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        PriceTag(service.priceFrom)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrdersTab(
    orders: List<PartOrder>,
    bookings: List<ServiceBookingOrder>,
    onConfirmOrder: (String) -> Unit,
    onCompleteOrder: (String) -> Unit,
    onConfirmBooking: (String) -> Unit,
    onCompleteBooking: (String) -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Part orders", fontWeight = FontWeight.SemiBold) }
        if (orders.isEmpty()) {
            item { Text("No orders yet.", color = TextSecondary, style = MaterialTheme.typography.bodySmall) }
        } else {
            items(orders, key = { it.id }) { order ->
                OrderCard(
                    title = "${order.items.sumOf { it.quantity }} item(s) · ${CurrencyFormatter.format(order.totalPrice)}",
                    subtitle = order.buyerEmail,
                    status = order.status.name,
                    onConfirm = if (order.status == OrderStatus.PENDING) {{ onConfirmOrder(order.id) }} else null,
                    onComplete = if (order.status == OrderStatus.CONFIRMED) {{ onCompleteOrder(order.id) }} else null,
                )
            }
        }
        item { Spacer(Modifier.height(8.dp)); Text("Service bookings", fontWeight = FontWeight.SemiBold) }
        if (bookings.isEmpty()) {
            item { Text("No bookings yet.", color = TextSecondary, style = MaterialTheme.typography.bodySmall) }
        } else {
            items(bookings, key = { it.id }) { booking ->
                OrderCard(
                    title = booking.serviceName,
                    subtitle = "${booking.vehicleNote} · ${booking.preferredDate}",
                    status = booking.status.name,
                    onConfirm = if (booking.status == OrderStatus.PENDING) {{ onConfirmBooking(booking.id) }} else null,
                    onComplete = if (booking.status == OrderStatus.CONFIRMED) {{ onCompleteBooking(booking.id) }} else null,
                )
            }
        }
    }
}

@Composable
private fun OrderCard(
    title: String,
    subtitle: String,
    status: String,
    onConfirm: (() -> Unit)?,
    onComplete: (() -> Unit)?,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(title, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text(status, color = GreenAccent, style = MaterialTheme.typography.labelSmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                onConfirm?.let { Button(onClick = it) { Text("Confirm") } }
                onComplete?.let { Button(onClick = it) { Text("Complete") } }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    BoxCentered(message)
}

@Composable
private fun BoxCentered(message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, color = TextSecondary)
    }
}

@Composable
private fun PlaceBidDialog(
    request: BreakdownRequest,
    onDismiss: () -> Unit,
    onSubmit: (Double, Int, String) -> Unit,
) {
    var priceText by rememberSaveable { mutableStateOf("") }
    var etaText by rememberSaveable { mutableStateOf("15") }
    var message by rememberSaveable { mutableStateOf("We can help — available now.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bid on ${request.type.name.lowercase()} request") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(request.locationLabel, style = MaterialTheme.typography.bodySmall)
                Text(
                    "${request.vehicle.make} ${request.vehicle.model} (${request.vehicle.year})",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                if (request.photoUris.isNotEmpty()) {
                    BreakdownPhotoStrip(photoUrls = request.photoUris, height = 72.dp)
                }
                OutlinedTextField(
                    priceText,
                    { priceText = it },
                    label = { Text("Price (ZMW)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = formOutlinedTextFieldColors(),
                )
                OutlinedTextField(
                    etaText,
                    { etaText = it },
                    label = { Text("ETA (minutes)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = formOutlinedTextFieldColors(),
                )
                OutlinedTextField(
                    message,
                    { message = it },
                    label = { Text("Message") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = formOutlinedTextFieldColors(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: return@Button
                    val eta = etaText.toIntOrNull() ?: return@Button
                    onSubmit(price, eta, message)
                },
                enabled = priceText.toDoubleOrNull() != null,
            ) { Text("Submit bid") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
