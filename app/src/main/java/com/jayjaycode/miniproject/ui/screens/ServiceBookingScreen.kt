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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jayjaycode.miniproject.data.MockRepository
import com.jayjaycode.miniproject.data.ServicePackage
import com.jayjaycode.miniproject.ui.components.PriceTag
import com.jayjaycode.miniproject.ui.components.SectionTitle
import com.jayjaycode.miniproject.ui.theme.TextSecondary
import com.jayjaycode.miniproject.ui.theme.formOutlinedTextFieldColors
import com.jayjaycode.miniproject.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceBookingScreen() {
    var bookingPackage by remember { mutableStateOf<ServicePackage?>(null) }
    var vehicleNote by remember { mutableStateOf("") }
    var preferredDate by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Book Service") }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                SectionTitle("Maintenance packages", "Schedule servicing at partner garages")
            }
            items(MockRepository.servicePackages, key = { it.id }) { pkg ->
                ServicePackageCard(
                    pkg = pkg,
                    onBook = { bookingPackage = pkg },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }
        }
    }

    bookingPackage?.let { pkg ->
        AlertDialog(
            onDismissRequest = { bookingPackage = null },
            title = { Text("Book ${pkg.name}") },
            text = {
                Column {
                    OutlinedTextField(
                        vehicleNote,
                        { vehicleNote = it },
                        label = { Text("Vehicle (make, model, plate)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = formOutlinedTextFieldColors(),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        preferredDate,
                        { preferredDate = it },
                        label = { Text("Preferred date & time") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = formOutlinedTextFieldColors(),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("From ${CurrencyFormatter.format(pkg.priceFrom)} · ~${pkg.durationMinutes} min", color = TextSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmation = true
                        bookingPackage = null
                    },
                    enabled = vehicleNote.isNotBlank() && preferredDate.isNotBlank(),
                ) {
                    Text("Confirm booking")
                }
            },
            dismissButton = {
                TextButton(onClick = { bookingPackage = null }) { Text("Cancel") }
            },
        )
    }

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Booking confirmed") },
            text = { Text("Your service appointment has been scheduled. You'll receive a reminder before your slot.") },
            confirmButton = {
                Button(onClick = { showConfirmation = false }) { Text("OK") }
            },
        )
    }
}

@Composable
private fun ServicePackageCard(
    pkg: ServicePackage,
    onBook: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(pkg.name, fontWeight = FontWeight.Bold)
                PriceTag(pkg.priceFrom)
            }
            Text(pkg.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            pkg.includes.forEach { item ->
                Text("• $item", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(12.dp))
            Text("~${pkg.durationMinutes} minutes", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onBook, modifier = Modifier.fillMaxWidth()) {
                Text("Book now")
            }
        }
    }
}
