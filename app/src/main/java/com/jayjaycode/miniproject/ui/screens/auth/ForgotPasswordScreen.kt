package com.jayjaycode.miniproject.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jayjaycode.miniproject.ui.theme.OrangePrimary
import com.jayjaycode.miniproject.ui.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = viewModel(),
) {
    var email by rememberSaveable { mutableStateOf("") }

    val isLoading by viewModel.isResetLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.resetSuccessMessage.collectAsState()
    val canSubmit = email.isNotBlank() && !isLoading && successMessage == null

    AuthScreenLayout(
        title = "Reset password",
        subtitle = "Enter your account email and we'll send you a link to create a new password.",
        onBack = onBack,
        formContent = {
            AuthTextField(
                value = email,
                onValueChange = {
                    email = it
                    viewModel.clearResetState()
                },
                label = "Email address",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            errorMessage?.let { AuthErrorBanner(it) }
            successMessage?.let { AuthSuccessBanner(it) }
            AuthPrimaryButton(
                text = if (successMessage != null) "Email sent" else "Send reset link",
                onClick = { viewModel.sendPasswordReset(email) },
                enabled = canSubmit,
                isLoading = isLoading,
            )
            if (successMessage != null) {
                Text(
                    "Didn't receive it? Wait a minute, check spam, or tap below to try again.",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(
                    onClick = {
                        viewModel.clearResetState()
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text("Send again", color = OrangePrimary)
                }
            }
        },
        footerContent = {
            AuthFooterLink(
                prompt = "Remember your password?",
                action = "Sign in",
                onClick = onBack,
            )
        },
    )
}
