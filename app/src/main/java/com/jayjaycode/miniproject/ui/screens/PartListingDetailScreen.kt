package com.jayjaycode.miniproject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jayjaycode.miniproject.data.SparePart
import com.jayjaycode.miniproject.ui.components.AppTopBar
import com.jayjaycode.miniproject.ui.components.ListingImageCarousel
import com.jayjaycode.miniproject.ui.components.PaymentMethodChips
import com.jayjaycode.miniproject.ui.components.PriceTag
import com.jayjaycode.miniproject.ui.theme.GreenAccent
import com.jayjaycode.miniproject.ui.theme.TextSecondary
import com.jayjaycode.miniproject.ui.viewmodel.MarketplaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartListingDetailScreen(
    part: SparePart,
    inCart: Boolean,
    onBack: () -> Unit,
    onAddToCart: () -> Unit,
    onRemoveFromCart: () -> Unit,
) {
    Scaffold(
        topBar = { AppTopBar(onBack = onBack) },
        bottomBar = {
            Button(
                onClick = if (inCart) onRemoveFromCart else onAddToCart,
                enabled = part.inStock,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(if (inCart) "Remove from cart" else "Add to cart")
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
                    if (part.inStock) "In stock" else "Out of stock",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (part.inStock) GreenAccent else TextSecondary,
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                DetailRow(label = "Category", value = part.category)
                DetailRow(label = "Condition", value = part.condition)

                if (part.compatibleVehicles.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Compatible vehicles", fontWeight = FontWeight.SemiBold)
                        part.compatibleVehicles.forEach { fitment ->
                            Text(
                                "• ${fitment.displayLabel()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                            )
                        }
                    }
                }

                if (part.paymentMethods.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Accepted payment", fontWeight = FontWeight.SemiBold)
                        PaymentMethodChips(methods = part.paymentMethods)
                    }
                }

                Spacer(Modifier.height(72.dp))
            }
        }
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
    val cart by viewModel.cart.collectAsState()
    val part = parts.find { it.id == partId }

    if (part == null) {
        Scaffold(
            topBar = { AppTopBar(onBack = onBack) },
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
        inCart = cart.any { it.id == part.id },
        onBack = onBack,
        onAddToCart = { viewModel.addToCart(part) },
        onRemoveFromCart = { viewModel.removeFromCart(part) },
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}
