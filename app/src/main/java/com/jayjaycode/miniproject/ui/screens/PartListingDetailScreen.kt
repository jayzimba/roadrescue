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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jayjaycode.miniproject.data.SparePart
import com.jayjaycode.miniproject.ui.components.AppTopBar
import com.jayjaycode.miniproject.ui.components.ListingImageCarousel
import com.jayjaycode.miniproject.ui.components.PaymentMethodChips
import com.jayjaycode.miniproject.ui.components.PriceTag
import com.jayjaycode.miniproject.ui.components.QuantityStepper
import com.jayjaycode.miniproject.ui.theme.GreenAccent
import com.jayjaycode.miniproject.ui.theme.TextSecondary
import com.jayjaycode.miniproject.ui.viewmodel.MarketplaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartListingDetailScreen(
    part: SparePart,
    cartQuantity: Int,
    availabilityLabel: String,
    maxQuantity: Int?,
    purchasable: Boolean,
    onBack: () -> Unit,
    onAddToCart: (Int) -> Unit,
    onUpdateQuantity: (Int) -> Unit,
    onRemoveFromCart: () -> Unit,
) {
    var selectedQuantity by rememberSaveable(part.id) { mutableIntStateOf(1) }

    LaunchedEffect(cartQuantity) {
        if (cartQuantity > 0) {
            selectedQuantity = cartQuantity
        }
    }

    LaunchedEffect(maxQuantity) {
        if (maxQuantity != null && selectedQuantity > maxQuantity) {
            selectedQuantity = maxQuantity.coerceAtLeast(1)
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = part.name, onBack = onBack) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Quantity", fontWeight = FontWeight.Medium)
                    QuantityStepper(
                        quantity = selectedQuantity,
                        onQuantityChange = { qty ->
                            selectedQuantity = qty
                            if (cartQuantity > 0) onUpdateQuantity(qty)
                        },
                        max = maxQuantity,
                        enabled = purchasable || cartQuantity > 0,
                    )
                }
                if (cartQuantity > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = onRemoveFromCart,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Remove from cart")
                        }
                        Button(
                            onClick = { onUpdateQuantity(selectedQuantity) },
                            enabled = purchasable,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Update cart")
                        }
                    }
                } else {
                    Button(
                        onClick = { onAddToCart(selectedQuantity) },
                        enabled = purchasable,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Add to cart")
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            ListingImageCarousel(
                imageUrls = part.imageUrls,
                contentDescription = part.name,
            )

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(part.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                PriceTag(part.price)

                Text(
                    availabilityLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (purchasable) GreenAccent else TextSecondary,
                )
                part.quantity?.let {
                    Text(
                        "Listed quantity: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                } ?: Text(
                    "Listed quantity: Unlimited",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                DetailRow(label = "Category", value = part.category)
                DetailRow(label = "Condition", value = part.condition)

                if (part.compatibleVehicles.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Compatible vehicles", fontWeight = FontWeight.SemiBold)
                        part.compatibleVehicles.forEach { fitment ->
                            Text("• ${fitment.displayLabel()}", color = TextSecondary)
                        }
                    }
                }

                if (part.paymentMethods.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Payment methods", fontWeight = FontWeight.SemiBold)
                        PaymentMethodChips(methods = part.paymentMethods)
                    }
                }

                Spacer(Modifier.height(80.dp))
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartListingDetailRoute(
    partId: String,
    viewModel: MarketplaceViewModel,
    onBack: () -> Unit,
) {
    val parts by viewModel.parts.collectAsState()
    val part = parts.find { it.id == partId }

    if (part == null) {
        Scaffold(
            topBar = { AppTopBar(title = "Part details", onBack = onBack) },
        ) { padding ->
            Text(
                "This listing is no longer available.",
                modifier = Modifier.padding(padding).padding(16.dp),
                color = TextSecondary,
            )
        }
        return
    }

    PartListingDetailScreen(
        part = part,
        cartQuantity = viewModel.cartQuantityFor(part.id),
        availabilityLabel = viewModel.availabilityLabel(part),
        maxQuantity = viewModel.maxSelectableQuantity(part),
        purchasable = viewModel.isPurchasable(part),
        onBack = onBack,
        onAddToCart = { qty -> viewModel.addToCart(part, qty) },
        onUpdateQuantity = { qty -> viewModel.updateCartQuantity(part.id, qty) },
        onRemoveFromCart = { viewModel.removeFromCart(part) },
    )
}
