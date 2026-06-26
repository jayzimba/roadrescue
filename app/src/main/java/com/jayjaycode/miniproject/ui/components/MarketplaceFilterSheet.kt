package com.jayjaycode.miniproject.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jayjaycode.miniproject.data.MarketplacePartFilters
import com.jayjaycode.miniproject.data.PaymentMethod
import com.jayjaycode.miniproject.data.SparePartCategories
import com.jayjaycode.miniproject.data.SparePartConditions
import com.jayjaycode.miniproject.data.ZambianVehicleCatalog
import com.jayjaycode.miniproject.ui.theme.TextSecondary

private const val ANY_CATEGORY = "Any category"
private const val ANY_CONDITION = "Any condition"
private const val ANY_MAKE = "Any make"
private const val ANY_MODEL = "Any model"
private const val ANY_PAYMENT = "Any payment method"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MarketplaceFilterSheet(
    initialFilters: MarketplacePartFilters,
    onDismiss: () -> Unit,
    onApply: (MarketplacePartFilters) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var categorySelection by remember(initialFilters) {
        mutableStateOf(initialFilters.category ?: ANY_CATEGORY)
    }
    var conditionSelection by remember(initialFilters) {
        mutableStateOf(initialFilters.condition ?: ANY_CONDITION)
    }
    var makeSelection by remember(initialFilters) {
        mutableStateOf(initialFilters.make ?: ANY_MAKE)
    }
    var modelSelection by remember(initialFilters) {
        mutableStateOf(initialFilters.model ?: ANY_MODEL)
    }
    var paymentSelection by remember(initialFilters) {
        mutableStateOf(initialFilters.paymentMethod?.label ?: ANY_PAYMENT)
    }
    var inStockOnly by remember(initialFilters) { mutableStateOf(initialFilters.inStockOnly) }

    val modelOptions = remember(makeSelection) {
        if (makeSelection == ANY_MAKE) {
            listOf(ANY_MODEL)
        } else {
            listOf(ANY_MODEL) + ZambianVehicleCatalog.modelsFor(makeSelection)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Filter parts", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(
                "Refine results alongside your search. All selected filters must match.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            VehicleDropdown(
                label = "Category",
                options = listOf(ANY_CATEGORY) + SparePartCategories.all,
                selected = categorySelection,
                onSelected = { categorySelection = it },
                placeholder = ANY_CATEGORY,
            )

            VehicleDropdown(
                label = "Condition",
                options = listOf(ANY_CONDITION) + SparePartConditions.all,
                selected = conditionSelection,
                onSelected = { conditionSelection = it },
                placeholder = ANY_CONDITION,
            )

            VehicleDropdown(
                label = "Vehicle make",
                options = listOf(ANY_MAKE) + ZambianVehicleCatalog.makes,
                selected = makeSelection,
                onSelected = { make ->
                    makeSelection = make
                    modelSelection = ANY_MODEL
                },
                placeholder = ANY_MAKE,
            )

            VehicleDropdown(
                label = "Vehicle model",
                options = modelOptions,
                selected = modelSelection,
                onSelected = { modelSelection = it },
                enabled = makeSelection != ANY_MAKE,
                placeholder = if (makeSelection == ANY_MAKE) "Select make first" else ANY_MODEL,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Payment method", fontWeight = FontWeight.SemiBold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = paymentSelection == ANY_PAYMENT,
                        onClick = { paymentSelection = ANY_PAYMENT },
                        label = { Text(ANY_PAYMENT) },
                    )
                    PaymentMethod.entries.forEach { method ->
                        FilterChip(
                            selected = paymentSelection == method.label,
                            onClick = { paymentSelection = method.label },
                            label = { Text(method.label) },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("In stock only", fontWeight = FontWeight.Medium)
                    Text(
                        "Hide out-of-stock listings",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                Switch(checked = inStockOnly, onCheckedChange = { inStockOnly = it })
            }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    onClick = {
                        categorySelection = ANY_CATEGORY
                        conditionSelection = ANY_CONDITION
                        makeSelection = ANY_MAKE
                        modelSelection = ANY_MODEL
                        paymentSelection = ANY_PAYMENT
                        inStockOnly = false
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Clear all")
                }
                Button(
                    onClick = {
                        val make = makeSelection.toFilterValue(ANY_MAKE)
                        onApply(
                            MarketplacePartFilters(
                                category = categorySelection.toFilterValue(ANY_CATEGORY),
                                condition = conditionSelection.toFilterValue(ANY_CONDITION),
                                make = make,
                                model = make?.let { modelSelection.toFilterValue(ANY_MODEL) },
                                paymentMethod = PaymentMethod.entries.find { it.label == paymentSelection }
                                    .takeUnless { paymentSelection == ANY_PAYMENT },
                                inStockOnly = inStockOnly,
                            ),
                        )
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Apply")
                }
            }
        }
    }
}

private fun String.toFilterValue(anyLabel: String): String? =
    if (this == anyLabel || isBlank()) null else this
