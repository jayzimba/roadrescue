package com.jayjaycode.miniproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jayjaycode.miniproject.ui.components.SpannerLoader
import com.jayjaycode.miniproject.ui.navigation.AuthRoutes
import com.jayjaycode.miniproject.ui.screens.auth.LoginScreen
import com.jayjaycode.miniproject.ui.screens.auth.SignUpScreen
import com.jayjaycode.miniproject.ui.theme.NavyDark
import com.jayjaycode.miniproject.ui.theme.NavySurface
import com.jayjaycode.miniproject.ui.theme.SurfaceLight
import com.jayjaycode.miniproject.ui.viewmodel.AuthUiState
import com.jayjaycode.miniproject.ui.viewmodel.AuthViewModel

@Composable
fun RoadRescueRoot(
    authViewModel: AuthViewModel = viewModel(),
) {
    val authState by authViewModel.authState.collectAsState()

    when (val state = authState) {
        AuthUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(NavyDark, NavySurface, SurfaceLight))),
                contentAlignment = Alignment.Center,
            ) {
                SpannerLoader(size = 40.dp, tint = Color.White)
            }
        }
        AuthUiState.Unauthenticated -> {
            AuthNavHost()
        }
        is AuthUiState.Authenticated -> {
            RoadRescueApp(
                userName = state.user.displayName?.takeIf { it.isNotBlank() }
                    ?: state.user.email.orEmpty(),
                onSignOut = { authViewModel.signOut() },
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
                onLoginSuccess = { /* auth state flow handles navigation */ },
            )
        }
        composable(AuthRoutes.SIGN_UP) {
            SignUpScreen(
                onBack = { navController.popBackStack() },
                onSignUpSuccess = { /* Firebase signs in automatically; auth flow navigates to app */ },
            )
        }
    }
}
