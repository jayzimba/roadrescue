package com.jayjaycode.miniproject.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jayjaycode.miniproject.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel(),
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val canSubmit = email.isNotBlank() && password.length >= 6 && !isLoading

    AuthScreenLayout(
        title = "Welcome back",
        subtitle = "Sign in to request towing or a mobile mechanic near you.",
        formContent = {
            AuthFeatureChips()
            Spacer(Modifier.height(4.dp))
            AuthTextField(
                value = email,
                onValueChange = { email = it; viewModel.clearError() },
                label = "Email address",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            AuthTextField(
                value = password,
                onValueChange = { password = it; viewModel.clearError() },
                label = "Password",
                leadingIcon = Icons.Default.Lock,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isPassword = true,
            )
            errorMessage?.let { AuthErrorBanner(it) }
            AuthPrimaryButton(
                text = "Sign in",
                onClick = { viewModel.signIn(email, password, onLoginSuccess) },
                enabled = canSubmit,
                isLoading = isLoading,
            )
        },
        footerContent = {
            AuthFooterLink(
                prompt = "New to RoadRescue?",
                action = "Create account",
                onClick = onNavigateToSignUp,
            )
        },
    )
}
