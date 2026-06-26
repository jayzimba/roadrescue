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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jayjaycode.miniproject.data.SparePart
import com.jayjaycode.miniproject.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartBottomSheet(
    cart: List<SparePart>,
    onDismiss: () -> Unit,
    onRemove: (SparePart) -> Unit,
    onCheckout: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val total = cart.sumOf { it.price }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text("Your cart", fontWeight = FontWeight.Bold)
            Text("${cart.size} item${if (cart.size != 1) "s" else ""}")
            Spacer(Modifier.height(16.dp))

            if (cart.isEmpty()) {
                Text("Your cart is empty")
                Spacer(Modifier.height(24.dp))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cart, key = { it.id }) { part ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(part.name, fontWeight = FontWeight.Medium)
                                Text(CurrencyFormatter.format(part.price))
                            }
                            TextButton(onClick = { onRemove(part) }) {
                                Text("Remove")
                            }
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
