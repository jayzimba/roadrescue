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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jayjaycode.miniproject.data.SparePart
import com.jayjaycode.miniproject.ui.components.CartBottomSheet
import com.jayjaycode.miniproject.ui.components.PriceTag
import com.jayjaycode.miniproject.ui.components.SectionTitle
import com.jayjaycode.miniproject.ui.theme.GreenAccent
import com.jayjaycode.miniproject.ui.theme.TextSecondary
import com.jayjaycode.miniproject.ui.viewmodel.MarketplaceViewModel
import com.jayjaycode.miniproject.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(viewModel: MarketplaceViewModel = viewModel()) {
    val cart by viewModel.cart.collectAsState()
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showCart by remember { mutableStateOf(false) }
    var showCheckoutConfirm by remember { mutableStateOf(false) }
    val categories = viewModel.parts.map { it.category }.distinct()
    val filtered = if (selectedCategory == null) viewModel.parts
    else viewModel.parts.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spare Parts") },
                actions = {
                    BadgedBox(
                        badge = {
                            if (cart.isNotEmpty()) Badge { Text("${cart.size}") }
                        },
                    ) {
                        TextButton(onClick = { showCart = true }) {
                            Text("Cart")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SectionTitle("Marketplace", "Buy parts from verified sellers")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All") },
                )
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(filtered, key = { it.id }) { part ->
                    PartCard(
                        part = part,
                        inCart = cart.any { it.id == part.id },
                        onAdd = { viewModel.addToCart(part) },
                        onRemove = { viewModel.removeFromCart(part) },
                    )
                }
            }
        }
    }

    if (showCart) {
        CartBottomSheet(
            cart = cart,
            onDismiss = { showCart = false },
            onRemove = { viewModel.removeFromCart(it) },
            onCheckout = {
                showCart = false
                showCheckoutConfirm = true
            },
        )
    }

    if (showCheckoutConfirm) {
        AlertDialog(
            onDismissRequest = { showCheckoutConfirm = false },
            title = { Text("Order placed") },
            text = {
                Text(
                    "Your order of ${cart.size} item(s) totaling ${CurrencyFormatter.format(cart.sumOf { it.price })} has been placed. Payment integration coming soon.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearCart()
                    showCheckoutConfirm = false
                }) {
                    Text("OK")
                }
            },
        )
    }
}

@Composable
private fun PartCard(
    part: SparePart,
    inCart: Boolean,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(part.name, fontWeight = FontWeight.SemiBold)
                Text("${part.category} · ${part.condition}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("Seller: ${part.seller}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(
                    if (part.inStock) "In stock" else "Out of stock",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (part.inStock) GreenAccent else TextSecondary,
                )
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                PriceTag(part.price)
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = if (inCart) onRemove else onAdd,
                    enabled = part.inStock,
                ) {
                    Text(if (inCart) "Remove" else "Add")
                }
            }
        }
    }
}
