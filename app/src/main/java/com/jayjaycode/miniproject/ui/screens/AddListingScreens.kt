package com.jayjaycode.miniproject.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jayjaycode.miniproject.data.PaymentMethod
import com.jayjaycode.miniproject.data.SparePartCategories
import com.jayjaycode.miniproject.data.SparePartConditions
import com.jayjaycode.miniproject.data.VehicleCompatibility
import com.jayjaycode.miniproject.ui.components.AppTopBar
import com.jayjaycode.miniproject.ui.components.CompatibleVehiclesSelector
import com.jayjaycode.miniproject.ui.components.PartCategorySelector
import com.jayjaycode.miniproject.ui.components.PaymentMethodSelector
import com.jayjaycode.miniproject.ui.components.PhotoPickerSection
import com.jayjaycode.miniproject.ui.components.VehicleDropdown
import com.jayjaycode.miniproject.ui.screens.auth.AuthErrorBanner
import com.jayjaycode.miniproject.ui.screens.auth.AuthPrimaryButton
import com.jayjaycode.miniproject.ui.screens.auth.AuthTextField
import com.jayjaycode.miniproject.ui.viewmodel.ProviderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPartListingScreen(
    onBack: () -> Unit,
    viewModel: ProviderViewModel = viewModel(),
) {
    val context = LocalContext.current

    var name by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("") }
    var customCategory by rememberSaveable { mutableStateOf("") }
    var priceText by rememberSaveable { mutableStateOf("") }
    var condition by rememberSaveable { mutableStateOf(SparePartConditions.default) }
    var compatibleVehicles by remember { mutableStateOf<List<VehicleCompatibility>>(emptyList()) }
    var photoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var quantityText by rememberSaveable { mutableStateOf("") }
    var selectedPayments by remember { mutableStateOf(setOf<PaymentMethod>()) }
    var showPhotoError by remember { mutableStateOf(false) }
    var showPaymentError by remember { mutableStateOf(false) }
    var showCategoryError by remember { mutableStateOf(false) }
    var showFitmentError by remember { mutableStateOf(false) }

    val resolvedCategory = SparePartCategories.resolveCategory(selectedCategory, customCategory)
    val categoryValid = selectedCategory.isNotBlank() &&
        (selectedCategory != SparePartCategories.OTHER || customCategory.isNotBlank())

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val canSubmit = name.isNotBlank() &&
        categoryValid &&
        compatibleVehicles.isNotEmpty() &&
        priceText.toDoubleOrNull() != null &&
        photoUris.isNotEmpty() &&
        selectedPayments.isNotEmpty() &&
        !isLoading

    Scaffold(
        topBar = { AppTopBar(title = "Add part listing", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "Add details buyers need to find and purchase this part.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )

            AuthTextField(name, { name = it }, "Part name", Icons.Default.Inventory)
            PartCategorySelector(
                selectedCategory = selectedCategory,
                onCategorySelected = {
                    selectedCategory = it
                    showCategoryError = false
                },
                customCategory = customCategory,
                onCustomCategoryChanged = {
                    customCategory = it
                    showCategoryError = false
                },
                showCustomError = showCategoryError,
            )
            AuthTextField(priceText, { priceText = it }, "Price (ZMW)", Icons.Default.Inventory)
            AuthTextField(
                value = quantityText,
                onValueChange = { quantityText = it.filter { ch -> ch.isDigit() } },
                label = "Quantity in stock (optional)",
                leadingIcon = Icons.Default.Inventory,
            )
            Text(
                "Leave blank for unlimited stock.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            VehicleDropdown(
                label = "Condition",
                options = SparePartConditions.all,
                selected = condition,
                onSelected = { condition = it },
                placeholder = "Select condition",
            )
            CompatibleVehiclesSelector(
                selected = compatibleVehicles,
                onSelectionChanged = {
                    compatibleVehicles = it
                    showFitmentError = false
                },
                showError = showFitmentError,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Text("Part photos", fontWeight = FontWeight.SemiBold)
            PhotoPickerSection(
                photoUris = photoUris,
                onPhotosChanged = {
                    photoUris = it
                    showPhotoError = false
                },
                maxPhotos = 4,
                title = "Photos",
                hint = "At least 1 photo required (${photoUris.size}/4)",
                emptyHint = "Add a clear photo of the spare part",
            )
            if (showPhotoError) {
                Text(
                    "Add at least one photo before publishing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            PaymentMethodSelector(
                selected = selectedPayments,
                onSelectionChanged = {
                    selectedPayments = it
                    showPaymentError = false
                },
                isError = showPaymentError,
            )

            errorMessage?.let { AuthErrorBanner(it) }

            AuthPrimaryButton(
                text = "Publish listing",
                onClick = {
                    if (!categoryValid) {
                        showCategoryError = true
                        return@AuthPrimaryButton
                    }
                    if (compatibleVehicles.isEmpty()) {
                        showFitmentError = true
                        return@AuthPrimaryButton
                    }
                    if (photoUris.isEmpty()) {
                        showPhotoError = true
                        return@AuthPrimaryButton
                    }
                    if (selectedPayments.isEmpty()) {
                        showPaymentError = true
                        return@AuthPrimaryButton
                    }
                    val price = priceText.toDoubleOrNull() ?: return@AuthPrimaryButton
                    val quantity = quantityText.toIntOrNull()
                    if (quantity != null && quantity < 1) return@AuthPrimaryButton
                    viewModel.addPart(
                        name = name,
                        category = resolvedCategory,
                        price = price,
                        condition = condition,
                        compatibleVehicles = compatibleVehicles,
                        photoUris = photoUris,
                        paymentMethods = selectedPayments,
                        context = context,
                        quantity = quantity,
                        onSuccess = onBack,
                    )
                },
                enabled = canSubmit,
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceListingScreen(
    onBack: () -> Unit,
    viewModel: ProviderViewModel = viewModel(),
) {
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var durationText by rememberSaveable { mutableStateOf("") }
    var priceText by rememberSaveable { mutableStateOf("") }
    var includesText by rememberSaveable { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val canSubmit = name.isNotBlank() &&
        description.isNotBlank() &&
        durationText.toIntOrNull() != null &&
        priceText.toDoubleOrNull() != null &&
        !isLoading

    Scaffold(
        topBar = { AppTopBar(title = "Add service listing", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "Describe the service your shop offers.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )

            AuthTextField(name, { name = it }, "Service name", Icons.Default.Inventory)
            AuthTextField(description, { description = it }, "Description", Icons.Default.Inventory)
            AuthTextField(durationText, { durationText = it }, "Duration (minutes)", Icons.Default.Inventory)
            AuthTextField(priceText, { priceText = it }, "Price from (ZMW)", Icons.Default.Inventory)
            AuthTextField(
                includesText,
                { includesText = it },
                "What's included (comma-separated)",
                Icons.Default.Inventory,
            )

            errorMessage?.let { AuthErrorBanner(it) }

            AuthPrimaryButton(
                text = "Publish service",
                onClick = {
                    val duration = durationText.toIntOrNull() ?: return@AuthPrimaryButton
                    val price = priceText.toDoubleOrNull() ?: return@AuthPrimaryButton
                    viewModel.addService(
                        name = name,
                        description = description,
                        durationMinutes = duration,
                        price = price,
                        includesText = includesText,
                        onSuccess = onBack,
                    )
                },
                enabled = canSubmit,
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
