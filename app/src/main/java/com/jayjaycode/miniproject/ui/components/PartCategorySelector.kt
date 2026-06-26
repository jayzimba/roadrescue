package com.jayjaycode.miniproject.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jayjaycode.miniproject.data.SparePartCategories
import com.jayjaycode.miniproject.ui.screens.auth.AuthTextField
import com.jayjaycode.miniproject.ui.theme.TextSecondary

@Composable
fun PartCategorySelector(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    customCategory: String,
    onCustomCategoryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    showCustomError: Boolean = false,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Category", fontWeight = FontWeight.SemiBold)
        Text(
            "Choose the type of spare part you are listing.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
        VehicleDropdown(
            label = "Part category",
            options = SparePartCategories.all,
            selected = selectedCategory,
            onSelected = onCategorySelected,
            placeholder = "Select category",
        )
        if (selectedCategory == SparePartCategories.OTHER) {
            AuthTextField(
                value = customCategory,
                onValueChange = onCustomCategoryChanged,
                label = "Specify category",
                leadingIcon = Icons.Default.Category,
            )
            if (showCustomError) {
                Text(
                    "Enter a category name for your part.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
