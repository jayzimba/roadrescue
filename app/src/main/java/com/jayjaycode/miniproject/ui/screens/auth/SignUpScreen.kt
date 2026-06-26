package com.jayjaycode.miniproject.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
fun SignUpScreen(
    onBack: () -> Unit,
    onSignUpSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel(),
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val passwordsMatch = password == confirmPassword
    val showPasswordMismatch = confirmPassword.isNotEmpty() && !passwordsMatch
    val canSubmit = name.isNotBlank() && email.isNotBlank() &&
        password.length >= 6 && passwordsMatch && !isLoading

    AuthScreenLayout(
        title = "Create your account",
        subtitle = "Join thousands getting fast roadside help across Zambia.",
        onBack = onBack,
        formContent = {
            AuthTextField(
                value = name,
                onValueChange = { name = it; viewModel.clearError() },
                label = "Full name",
                leadingIcon = Icons.Default.Person,
            )
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
                supportingText = if (password.isNotEmpty() && password.length < 6) {
                    "At least 6 characters required"
                } else null,
                isError = password.isNotEmpty() && password.length < 6,
            )
            if (password.isNotEmpty()) {
                PasswordStrengthHint(password)
            }
            AuthTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; viewModel.clearError() },
                label = "Confirm password",
                leadingIcon = Icons.Default.Lock,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isPassword = true,
                isError = showPasswordMismatch,
                supportingText = if (showPasswordMismatch) "Passwords do not match" else null,
            )
            errorMessage?.let { AuthErrorBanner(it) }
            Spacer(Modifier.height(4.dp))
            AuthPrimaryButton(
                text = "Create account",
                onClick = { viewModel.signUp(name, email, password, onSignUpSuccess) },
                enabled = canSubmit,
                isLoading = isLoading,
            )
        },
        footerContent = {
            AuthFooterLink(
                prompt = "Already registered?",
                action = "Sign in",
                onClick = onBack,
            )
        },
    )
}
