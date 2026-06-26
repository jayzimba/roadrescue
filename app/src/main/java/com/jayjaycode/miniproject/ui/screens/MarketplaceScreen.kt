package com.jayjaycode.miniproject.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jayjaycode.miniproject.data.MarketplacePartFilterMatcher
import com.jayjaycode.miniproject.data.MarketplacePartFilters
import com.jayjaycode.miniproject.data.SparePart
import com.jayjaycode.miniproject.ui.components.CartBottomSheet
import com.jayjaycode.miniproject.ui.components.MarketplaceFilterSheet
import com.jayjaycode.miniproject.ui.components.PaymentMethodChips
import com.jayjaycode.miniproject.ui.components.PriceTag
import com.jayjaycode.miniproject.ui.components.SectionTitle
import com.jayjaycode.miniproject.ui.theme.GreenAccent
import com.jayjaycode.miniproject.ui.theme.TextSecondary
import com.jayjaycode.miniproject.ui.theme.formOutlinedTextFieldColors
import com.jayjaycode.miniproject.ui.viewmodel.MarketplaceViewModel
import com.jayjaycode.miniproject.util.CurrencyFormatter
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    onPartClick: (String) -> Unit,
    viewModel: MarketplaceViewModel = viewModel(),
) {
    val cart by viewModel.cart.collectAsState()
    val parts by viewModel.parts.collectAsState()
    val checkoutError by viewModel.checkoutError.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var appliedFilters by remember { mutableStateOf(MarketplacePartFilters()) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showCart by remember { mutableStateOf(false) }
    var showCheckoutConfirm by remember { mutableStateOf(false) }
    var orderSummary by remember { mutableStateOf(0 to 0.0) }

    val filtered = parts.filter { part ->
        MarketplacePartFilterMatcher.matches(part, searchQuery, appliedFilters)
    }
    val hasActiveSearchOrFilters = searchQuery.isNotBlank() || appliedFilters.isActive

    Column(modifier = Modifier.fillMaxSize()) {
        SectionTitle("Marketplace", "Buy parts from registered auto shops")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search parts, sellers, fitment…") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                singleLine = true,
                colors = formOutlinedTextFieldColors(),
            )
            IconButton(onClick = { showFilterSheet = true }) {
                BadgedBox(
                    badge = {
                        if (appliedFilters.isActive) {
                            Badge { Text("${appliedFilters.activeCount}") }
                        }
                    },
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                }
            }
            BadgedBox(
                badge = {
                    if (cart.isNotEmpty()) Badge { Text("${cart.size}") }
                },
            ) {
                IconButton(onClick = { showCart = true }) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                }
            }
        }

        if (hasActiveSearchOrFilters) {
            Spacer(Modifier.height(8.dp))
            Text(
                "${filtered.size} of ${parts.size} parts",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (filtered.isEmpty()) {
                item {
                    Text(
                        when {
                            parts.isEmpty() -> {
                                "No parts listed yet. Registered auto shops can add inventory from Profile → Provider dashboard."
                            }
                            hasActiveSearchOrFilters -> {
                                "No parts match your search or filters. Try different keywords or clear filters."
                            }
                            else -> {
                                "No parts available right now."
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            } else {
                items(filtered, key = { it.id }) { part ->
                    PartCard(
                        part = part,
                        inCart = cart.any { it.id == part.id },
                        onClick = { onPartClick(part.id) },
                        onAdd = { viewModel.addToCart(part) },
                        onRemove = { viewModel.removeFromCart(part) },
                    )
                }
            }
        }
    }

    if (showFilterSheet) {
        MarketplaceFilterSheet(
            initialFilters = appliedFilters,
            onDismiss = { showFilterSheet = false },
            onApply = { filters ->
                appliedFilters = filters
                showFilterSheet = false
            },
        )
    }

    if (showCart) {
        CartBottomSheet(
            cart = cart,
            onDismiss = { showCart = false },
            onRemove = { viewModel.removeFromCart(it) },
            onCheckout = {
                val itemCount = cart.size
                val total = cart.sumOf { it.price }
                showCart = false
                viewModel.checkout(
                    onSuccess = {
                        orderSummary = itemCount to total
                        showCheckoutConfirm = true
                    },
                )
            },
        )
    }

    if (showCheckoutConfirm) {
        AlertDialog(
            onDismissRequest = { showCheckoutConfirm = false },
            title = { Text("Order placed") },
            text = {
                Text(
                    "Your order of ${orderSummary.first} item(s) totaling ${CurrencyFormatter.format(orderSummary.second)} has been sent to the seller. They will confirm shortly.",
                )
            },
            confirmButton = {
                TextButton(onClick = { showCheckoutConfirm = false }) {
                    Text("OK")
                }
            },
        )
    }

    checkoutError?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearCheckoutError() },
            title = { Text("Checkout failed") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearCheckoutError()
                    showCart = true
                }) { Text("Back to cart") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PartCard(
    part: SparePart,
    inCart: Boolean,
    onClick: () -> Unit,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val imageUrl = part.imageUrls.firstOrNull()
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = part.name,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(part.name, fontWeight = FontWeight.SemiBold)
                Text(
                    "${part.category} · ${part.condition}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Text(
                    if (part.inStock) "In stock" else "Out of stock",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (part.inStock) GreenAccent else TextSecondary,
                )
                if (part.compatibleVehicles.isNotEmpty()) {
                    Text(
                        "Fits: ${part.compatibleVehicles.joinToString { it.displayLabel() }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                }
                if (part.paymentMethods.isNotEmpty()) {
                    PaymentMethodChips(methods = part.paymentMethods)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                PriceTag(part.price)
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = if (inCart) onRemove else onAdd,
                    enabled = part.inStock,
                    modifier = Modifier,
                ) {
                    Text(if (inCart) "Remove" else "Add")
                }
            }
        }
    }
}
