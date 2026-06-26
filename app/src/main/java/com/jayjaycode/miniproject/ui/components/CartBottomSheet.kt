package com.jayjaycode.miniproject.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jayjaycode.miniproject.data.CartLineItem
import com.jayjaycode.miniproject.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartBottomSheet(
    cart: List<CartLineItem>,
    onDismiss: () -> Unit,
    onRemove: (String) -> Unit,
    onQuantityChange: (String, Int) -> Unit,
    maxQuantityFor: (String) -> Int?,
    onCheckout: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val total = cart.sumOf { it.lineTotal }
    val itemCount = cart.sumOf { it.quantity }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text("Your cart", fontWeight = FontWeight.Bold)
            Text("$itemCount item${if (itemCount != 1) "s" else ""}")
            Spacer(Modifier.height(16.dp))

            if (cart.isEmpty()) {
                Text("Your cart is empty")
                Spacer(Modifier.height(24.dp))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cart, key = { it.part.id }) { line ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(line.part.name, fontWeight = FontWeight.Medium)
                                Text(
                                    "${CurrencyFormatter.format(line.part.price)} each · ${CurrencyFormatter.format(line.lineTotal)}",
                                )
                            }
                            QuantityStepper(
                                quantity = line.quantity,
                                onQuantityChange = { onQuantityChange(line.part.id, it) },
                                max = maxQuantityFor(line.part.id),
                            )
                        }
                        TextButton(onClick = { onRemove(line.part.id) }) {
                            Text("Remove")
                        }
                        HorizontalDivider()
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Total", fontWeight = FontWeight.Bold)
                    Text(CurrencyFormatter.format(total), fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth()) {
                    Text("Proceed to checkout")
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
