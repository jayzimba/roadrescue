package com.jayjaycode.miniproject.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jayjaycode.miniproject.data.PaymentMethod
import com.jayjaycode.miniproject.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaymentMethodSelector(
    selected: Set<PaymentMethod>,
    onSelectionChanged: (Set<PaymentMethod>) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Accepted payment methods", fontWeight = FontWeight.SemiBold)
        Text(
            "Choose how buyers can pay for this listing. Select at least one.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PaymentMethod.entries.forEach { method ->
                FilterChip(
                    selected = method in selected,
                    onClick = {
                        val updated = if (method in selected) {
                            selected - method
                        } else {
                            selected + method
                        }
                        onSelectionChanged(updated)
                    },
                    label = { Text(method.label) },
                )
            }
        }
        if (isError) {
            Text(
                "Select at least one payment method.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaymentMethodChips(
    methods: List<PaymentMethod>,
    modifier: Modifier = Modifier,
) {
    if (methods.isEmpty()) return
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        methods.forEach { method ->
            FilterChip(
                selected = false,
                onClick = {},
                enabled = false,
                label = { Text(method.label, style = MaterialTheme.typography.labelSmall) },
            )
        }
    }
}
