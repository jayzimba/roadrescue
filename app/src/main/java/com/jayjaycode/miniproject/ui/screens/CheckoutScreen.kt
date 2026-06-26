package com.jayjaycode.miniproject.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jayjaycode.miniproject.data.CartLineItem
import com.jayjaycode.miniproject.data.PaymentMethod
import com.jayjaycode.miniproject.ui.components.AppTopBar
import com.jayjaycode.miniproject.ui.components.DeliveryLocationSection
import com.jayjaycode.miniproject.ui.components.PriceTag
import com.jayjaycode.miniproject.ui.components.QuantityStepper
import com.jayjaycode.miniproject.ui.screens.auth.AuthErrorBanner
import com.jayjaycode.miniproject.ui.screens.auth.AuthTextField
import com.jayjaycode.miniproject.ui.theme.OrangePrimary
import com.jayjaycode.miniproject.ui.theme.TextSecondary
import com.jayjaycode.miniproject.ui.viewmodel.MarketplaceViewModel
import com.jayjaycode.miniproject.util.CurrencyFormatter
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CheckoutScreen(
    viewModel: MarketplaceViewModel,
    onBack: () -> Unit,
    onOrderPlaced: () -> Unit,
) {
    val cart by viewModel.cart.collectAsState()
    val cartTotal by viewModel.cartTotal.collectAsState()
    val cartItemCount by viewModel.cartItemCount.collectAsState()
    val parts by viewModel.parts.collectAsState()
    val isCheckingOut by viewModel.isCheckingOut.collectAsState()
    val checkoutError by viewModel.checkoutError.collectAsState()
    val savedPhone by viewModel.userPhone.collectAsState()

    var deliveryPhone by rememberSaveable { mutableStateOf("") }
    var deliveryAddress by rememberSaveable { mutableStateOf("") }
    var deliveryLatitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var deliveryLongitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var selectedPayment by rememberSaveable { mutableStateOf<PaymentMethod?>(null) }

    val paymentOptions = viewModel.availablePaymentMethods()
    val total = cartTotal

    LaunchedEffect(savedPhone) {
        if (deliveryPhone.isBlank() && savedPhone.isNotBlank()) {
            deliveryPhone = savedPhone
        }
    }

    LaunchedEffect(paymentOptions) {
        if (selectedPayment == null && paymentOptions.size == 1) {
            selectedPayment = paymentOptions.first()
        }
    }

    if (cart.isEmpty()) {
        Scaffold(topBar = { AppTopBar(title = "Checkout", onBack = onBack) }) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Your cart is empty", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Add spare parts from the marketplace first.", color = TextSecondary)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onBack) { Text("Back to marketplace") }
            }
        }
        return
    }

    val canPlaceOrder = deliveryPhone.isNotBlank() &&
        deliveryAddress.isNotBlank() &&
        deliveryLatitude != null &&
        deliveryLongitude != null &&
        selectedPayment != null &&
        !isCheckingOut

    Scaffold(
        topBar = { AppTopBar(title = "Checkout", onBack = onBack) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Total", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Text(
                            CurrencyFormatter.format(total),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary,
                        )
                    }
                    Text(
                        "${cartItemCount} item${if (cartItemCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        val payment = selectedPayment ?: return@Button
                        viewModel.checkout(
                            paymentMethod = payment,
                            deliveryPhone = deliveryPhone,
                            deliveryAddress = deliveryAddress,
                            deliveryLatitude = deliveryLatitude!!,
                            deliveryLongitude = deliveryLongitude!!,
                            onSuccess = onOrderPlaced,
                        )
                    },
                    enabled = canPlaceOrder,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (isCheckingOut) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text("Place order")
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text("Checkout", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Review your order and confirm delivery details.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }

            item {
                CheckoutSection(title = "Order summary") {
                    cart.forEach { line ->
                        CheckoutItemRow(
                            line = line,
                            maxQuantity = parts.find { it.id == line.part.id }
                                ?.let { viewModel.maxSelectableQuantity(it) },
                            onQuantityChange = { viewModel.updateCartQuantity(line.part.id, it) },
                        )
                        if (line != cart.last()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            )
                        }
                    }
                }
            }

            item {
                CheckoutSection(title = "Delivery details") {
                    AuthTextField(
                        value = deliveryPhone,
                        onValueChange = { deliveryPhone = it },
                        label = "Contact phone",
                        leadingIcon = Icons.Default.Phone,
                    )
                    Spacer(Modifier.height(10.dp))
                    DeliveryLocationSection(
                        address = deliveryAddress,
                        onAddressChange = { deliveryAddress = it },
                        latitude = deliveryLatitude,
                        longitude = deliveryLongitude,
                        onLocationSelected = { result ->
                            deliveryLatitude = result.latitude
                            deliveryLongitude = result.longitude
                            deliveryAddress = result.label
                        },
                    )
                }
            }

            item {
                CheckoutSection(title = "Payment method") {
                    if (paymentOptions.isEmpty()) {
                        Text(
                            "No payment methods available for this cart.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else {
                        Text(
                            "Choose how you will pay the seller.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            paymentOptions.forEach { method ->
                                FilterChip(
                                    selected = selectedPayment == method,
                                    onClick = { selectedPayment = method },
                                    label = { Text(method.label) },
                                )
                            }
                        }
                    }
                }
            }

            checkoutError?.let { error ->
                item { AuthErrorBanner(error) }
            }

            item { Spacer(Modifier.height(96.dp)) }
        }
    }
}

@Composable
private fun CheckoutSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun CheckoutItemRow(
    line: CartLineItem,
    maxQuantity: Int?,
    onQuantityChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val imageUrl = line.part.imageUrls.firstOrNull()
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = line.part.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(line.part.name, fontWeight = FontWeight.Medium)
            Text(
                "${line.part.category} · ${line.part.condition}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
            QuantityStepper(
                quantity = line.quantity,
                onQuantityChange = onQuantityChange,
                max = maxQuantity,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            PriceTag(line.lineTotal)
            Text(
                "${CurrencyFormatter.format(line.part.price)} each",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
    }
}
