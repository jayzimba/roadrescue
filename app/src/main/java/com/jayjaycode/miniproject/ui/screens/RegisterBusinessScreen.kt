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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.jayjaycode.miniproject.ui.components.AppTopBar
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
import com.jayjaycode.miniproject.data.BusinessType
import com.jayjaycode.miniproject.ui.components.BusinessTypeSelector
import com.jayjaycode.miniproject.ui.components.RegistrationCertificatePicker
import com.jayjaycode.miniproject.ui.screens.auth.AuthErrorBanner
import com.jayjaycode.miniproject.ui.screens.auth.AuthPrimaryButton
import com.jayjaycode.miniproject.ui.screens.auth.AuthTextField
import com.jayjaycode.miniproject.ui.theme.TextSecondary
import com.jayjaycode.miniproject.ui.viewmodel.ProviderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterBusinessScreen(
    onBack: () -> Unit,
    onRegistered: () -> Unit,
    viewModel: ProviderViewModel = viewModel(),
) {
    val context = LocalContext.current

    var businessType by rememberSaveable { mutableStateOf(BusinessType.AUTO_SHOP) }
    var businessName by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var servicesText by rememberSaveable { mutableStateOf("") }
    var certificateUri by remember { mutableStateOf<Uri?>(null) }
    var certificateFileName by rememberSaveable { mutableStateOf("") }
    var showCertificateError by remember { mutableStateOf(false) }
    var pickerError by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val hasCertificate = certificateUri != null
    val canSubmit = businessName.isNotBlank() &&
        phone.isNotBlank() &&
        address.isNotBlank() &&
        hasCertificate &&
        !isLoading

    Scaffold(
        topBar = { AppTopBar(title = "Register business", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                "Tell us about your business so customers can find and trust your listings.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            BusinessTypeSelector(
                selected = businessType,
                onSelected = { businessType = it },
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Text("Business details", fontWeight = FontWeight.SemiBold)

            AuthTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = "Business name",
                leadingIcon = Icons.Default.Business,
            )
            AuthTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Business phone",
                leadingIcon = Icons.Default.Phone,
            )
            AuthTextField(
                value = address,
                onValueChange = { address = it },
                label = "Address / area",
                leadingIcon = Icons.Default.LocationOn,
            )
            AuthTextField(
                value = description,
                onValueChange = { description = it },
                label = "About your business",
                leadingIcon = Icons.Default.Description,
            )
            AuthTextField(
                value = servicesText,
                onValueChange = { servicesText = it },
                label = "Services (comma-separated)",
                leadingIcon = Icons.Default.Description,
                supportingText = "e.g. Towing, Battery jump, Oil change",
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            RegistrationCertificatePicker(
                certificateUri = certificateUri,
                certificateFileName = certificateFileName,
                onCertificateSelected = { uri, name ->
                    certificateUri = uri
                    certificateFileName = name
                    showCertificateError = false
                    pickerError = null
                    viewModel.clearMessages()
                },
                onCertificateCleared = {
                    certificateUri = null
                    certificateFileName = ""
                },
                isError = showCertificateError,
                onInvalidFile = { pickerError = it },
            )

            pickerError?.let { AuthErrorBanner(it) }
            errorMessage?.let { AuthErrorBanner(it) }

            AuthPrimaryButton(
                text = "Submit registration",
                onClick = {
                    val uri = certificateUri
                    if (uri == null) {
                        showCertificateError = true
                        return@AuthPrimaryButton
                    }
                    viewModel.registerBusiness(
                        businessType = businessType,
                        businessName = businessName,
                        description = description,
                        phone = phone,
                        address = address,
                        servicesText = servicesText,
                        certificateUri = uri,
                        certificateFileName = certificateFileName,
                        context = context,
                        onSuccess = onRegistered,
                    )
                },
                enabled = canSubmit,
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
