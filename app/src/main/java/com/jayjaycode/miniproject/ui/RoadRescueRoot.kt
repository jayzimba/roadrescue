package com.jayjaycode.miniproject.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jayjaycode.miniproject.data.FcmTokenRepository
import com.jayjaycode.miniproject.ui.navigation.AuthRoutes
import com.jayjaycode.miniproject.ui.screens.SplashScreen
import com.jayjaycode.miniproject.ui.screens.auth.ForgotPasswordScreen
import com.jayjaycode.miniproject.ui.screens.auth.LoginScreen
import com.jayjaycode.miniproject.ui.screens.auth.SignUpScreen
import com.jayjaycode.miniproject.ui.viewmodel.AuthUiState
import com.jayjaycode.miniproject.ui.viewmodel.AuthViewModel
import com.jayjaycode.miniproject.util.NotificationDeepLink
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RoadRescueRoot(
    notificationDeepLink: NotificationDeepLink? = null,
    onNotificationHandled: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel(),
) {
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var minSplashElapsed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1_800)
        minSplashElapsed = true
    }

    val showSplash = !minSplashElapsed || authState is AuthUiState.Loading

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            scope.launch { FcmTokenRepository.instance.registerCurrentToken() }
        }
    }

    if (showSplash) {
        SplashScreen()
        return
    }

    when (val state = authState) {
        AuthUiState.Loading -> Unit
        AuthUiState.Unauthenticated -> {
            AuthNavHost()
        }
        is AuthUiState.Authenticated -> {
            LaunchedEffect(state.user.uid) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    if (!granted) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                FcmTokenRepository.instance.registerCurrentToken()
            }
            RoadRescueApp(
                userName = state.user.displayName?.takeIf { it.isNotBlank() }
                    ?: state.user.email.orEmpty(),
                userEmail = state.user.email.orEmpty(),
                onSignOut = { authViewModel.signOut() },
                notificationDeepLink = notificationDeepLink,
                onNotificationHandled = onNotificationHandled,
            )
        }
    }
}

@Composable
private fun AuthNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AuthRoutes.LOGIN,
    ) {
        composable(AuthRoutes.LOGIN) {
            LoginScreen(
                onNavigateToSignUp = { navController.navigate(AuthRoutes.SIGN_UP) },
                onNavigateToForgotPassword = { navController.navigate(AuthRoutes.FORGOT_PASSWORD) },
                onLoginSuccess = { /* auth state flow handles navigation */ },
            )
        }
        composable(AuthRoutes.SIGN_UP) {
            SignUpScreen(
                onBack = { navController.popBackStack() },
                onSignUpSuccess = { /* Firebase signs in automatically; auth flow navigates to app */ },
            )
        }
        composable(AuthRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
