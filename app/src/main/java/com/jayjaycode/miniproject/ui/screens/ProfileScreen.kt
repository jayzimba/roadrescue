package com.jayjaycode.miniproject.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import com.jayjaycode.miniproject.ui.components.AppTopBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jayjaycode.miniproject.ui.components.OnlineBadge
import com.jayjaycode.miniproject.ui.screens.auth.AuthErrorBanner
import com.jayjaycode.miniproject.ui.screens.auth.AuthSuccessBanner
import com.jayjaycode.miniproject.ui.screens.auth.AuthTextField
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import com.jayjaycode.miniproject.ui.theme.GreenAccent
import com.jayjaycode.miniproject.ui.theme.NavyDark
import com.jayjaycode.miniproject.ui.theme.OrangePrimary
import com.jayjaycode.miniproject.ui.theme.TextSecondary
import com.jayjaycode.miniproject.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userName: String,
    userEmail: String,
    onBack: () -> Unit,
    onRegisterBusiness: () -> Unit,
    onOpenDashboard: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
) {
    val profile by viewModel.userProfile.collectAsState()
    val business by viewModel.myBusiness.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var phone by rememberSaveable { mutableStateOf("") }
    val displayName = profile?.displayName?.takeIf { it.isNotBlank() } ?: userName
    val email = profile?.email?.takeIf { it.isNotBlank() } ?: userEmail

    Scaffold(
        topBar = { AppTopBar(onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(NavyDark),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            displayName.firstOrNull()?.uppercase() ?: "?",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Column {
                        Text(displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text(email, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Contact", fontWeight = FontWeight.SemiBold)
                    AuthTextField(
                        value = phone.ifBlank { profile?.phone.orEmpty() },
                        onValueChange = { phone = it; viewModel.clearMessages() },
                        label = "Phone number",
                        leadingIcon = Icons.Default.Phone,
                    )
                    Button(
                        onClick = { viewModel.updatePhone(phone.ifBlank { profile?.phone.orEmpty() }) },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Save phone")
                    }
                }
            }

            business?.let { shop ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Store, contentDescription = null, tint = OrangePrimary)
                                Text(shop.businessName, fontWeight = FontWeight.Bold)
                            }
                            OnlineBadge(isOnline = shop.isOnline)
                        }
                        Text(shop.businessType.label, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        Text(shop.description, style = MaterialTheme.typography.bodySmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Available for jobs", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = shop.isOnline,
                                onCheckedChange = { viewModel.setOnlineStatus(it) },
                            )
                        }
                        Button(
                            onClick = onOpenDashboard,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(8.dp))
                            Text("Provider dashboard")
                        }
                    }
                }
            } ?: Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Become a provider", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Register as an auto company, mobile mechanic, or auto shop to list parts & services, receive bids, and manage orders.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Button(onClick = onRegisterBusiness, modifier = Modifier.fillMaxWidth()) {
                        Text("Register business")
                    }
                }
            }

            errorMessage?.let { AuthErrorBanner(it) }
            successMessage?.let { AuthSuccessBanner(it) }

            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Sign out", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
