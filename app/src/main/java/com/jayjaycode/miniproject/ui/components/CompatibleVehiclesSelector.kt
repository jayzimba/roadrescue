package com.jayjaycode.miniproject.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jayjaycode.miniproject.data.VehicleCompatibility
import com.jayjaycode.miniproject.data.ZambianVehicleCatalog
import com.jayjaycode.miniproject.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompatibleVehiclesSelector(
    selected: List<VehicleCompatibility>,
    onSelectionChanged: (List<VehicleCompatibility>) -> Unit,
    modifier: Modifier = Modifier,
    showError: Boolean = false,
) {
    var pendingMake by rememberSaveable { mutableStateOf("") }
    var pendingModel by rememberSaveable { mutableStateOf("") }

    val modelOptions = ZambianVehicleCatalog.modelOptionsFor(pendingMake)
    val canAdd = pendingMake.isNotBlank() &&
        (pendingMake == VehicleCompatibility.ALL_MAKES || pendingModel.isNotBlank())

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Compatible vehicles", fontWeight = FontWeight.SemiBold)
        Text(
            "Add make and model combinations this part fits. Use All makes or All models when it applies broadly.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )

        VehicleDropdown(
            label = "Make",
            options = ZambianVehicleCatalog.makeOptions(),
            selected = pendingMake,
            onSelected = { make ->
                pendingMake = make
                pendingModel = if (make == VehicleCompatibility.ALL_MAKES) {
                    VehicleCompatibility.ALL_MODELS
                } else {
                    ""
                }
            },
            placeholder = "Select make",
        )

        VehicleDropdown(
            label = "Model",
            options = modelOptions,
            selected = pendingModel,
            onSelected = { pendingModel = it },
            enabled = pendingMake.isNotBlank() && pendingMake != VehicleCompatibility.ALL_MAKES,
            placeholder = when {
                pendingMake.isBlank() -> "Select make first"
                pendingMake == VehicleCompatibility.ALL_MAKES -> VehicleCompatibility.ALL_MODELS
                else -> "Select model"
            },
        )

        TextButton(
            onClick = {
                val model = if (pendingMake == VehicleCompatibility.ALL_MAKES) {
                    VehicleCompatibility.ALL_MODELS
                } else {
                    pendingModel
                }
                if (pendingMake.isBlank() || model.isBlank()) return@TextButton

                val entry = VehicleCompatibility(pendingMake, model)
                if (entry.isUniversal) {
                    onSelectionChanged(listOf(entry))
                } else {
                    val withoutUniversal = selected.filterNot { it.isUniversal }
                    if (withoutUniversal.any { it.make == entry.make && it.model == entry.model }) return@TextButton
                    onSelectionChanged(withoutUniversal + entry)
                }
                pendingMake = ""
                pendingModel = ""
            },
            enabled = canAdd,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Add fitment")
        }

        if (selected.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                selected.forEach { fitment ->
                    InputChip(
                        selected = true,
                        onClick = {
                            onSelectionChanged(selected.filterNot { it == fitment })
                        },
                        label = { Text(fitment.displayLabel()) },
                    )
                }
            }
        }

        if (showError) {
            Text(
                "Add at least one make and model fitment.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
