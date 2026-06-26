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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.jayjaycode.miniproject.data.ServicePackage
import com.jayjaycode.miniproject.ui.components.PriceTag
import com.jayjaycode.miniproject.ui.components.SectionTitle
import com.jayjaycode.miniproject.ui.theme.TextSecondary
import com.jayjaycode.miniproject.ui.theme.formOutlinedTextFieldColors
import com.jayjaycode.miniproject.ui.viewmodel.ServiceBookingViewModel
import com.jayjaycode.miniproject.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceBookingScreen(viewModel: ServiceBookingViewModel = viewModel()) {
    val services by viewModel.services.collectAsState()
    val isBooking by viewModel.isBooking.collectAsState()
    val bookingError by viewModel.bookingError.collectAsState()

    var bookingPackage by remember { mutableStateOf<ServicePackage?>(null) }
    var vehicleNote by remember { mutableStateOf("") }
    var preferredDate by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            SectionTitle("Maintenance packages", "Book services from registered auto shops")
        }
        if (services.isEmpty()) {
            item {
                Text(
                    "No services listed yet. Providers can add packages from Profile → Provider dashboard.",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
        } else {
            items(services, key = { it.id }) { pkg ->
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
                    if (pkg.shopName.isNotBlank()) {
                        Text("Provider: ${pkg.shopName}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Spacer(Modifier.height(8.dp))
                    }
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
                        viewModel.bookService(pkg, vehicleNote, preferredDate) {
                            showConfirmation = true
                            bookingPackage = null
                            vehicleNote = ""
                            preferredDate = ""
                        }
                    },
                    enabled = vehicleNote.isNotBlank() && preferredDate.isNotBlank() && !isBooking,
                ) {
                    if (isBooking) CircularProgressIndicator(strokeWidth = 2.dp)
                    else Text("Confirm booking")
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
            text = { Text("Your service appointment has been sent to the provider. They will confirm your slot shortly.") },
            confirmButton = {
                Button(onClick = { showConfirmation = false }) { Text("OK") }
            },
        )
    }

    bookingError?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearBookingError() },
            title = { Text("Booking failed") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearBookingError()
                    bookingPackage = null
                }) { Text("OK") }
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(pkg.name, fontWeight = FontWeight.Bold)
                    if (pkg.shopName.isNotBlank()) {
                        Text(pkg.shopName, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
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
