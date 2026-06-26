package com.jayjaycode.miniproject.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jayjaycode.miniproject.data.RequestType
import com.jayjaycode.miniproject.ui.navigation.NavRoutes
import com.jayjaycode.miniproject.ui.screens.ActiveJobScreen
import com.jayjaycode.miniproject.ui.screens.BiddingScreen
import com.jayjaycode.miniproject.ui.screens.HomeScreen
import com.jayjaycode.miniproject.ui.screens.MarketplaceScreen
import com.jayjaycode.miniproject.ui.screens.RequestFormScreen
import com.jayjaycode.miniproject.ui.screens.RequestHistoryScreen
import com.jayjaycode.miniproject.ui.screens.ServiceBookingScreen

private val bottomNavRoutes = setOf(NavRoutes.HOME, NavRoutes.MARKETPLACE, NavRoutes.SERVICE_BOOKING)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadRescueApp(
    userName: String,
    onSignOut: () -> Unit,
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route?.substringBefore("/")
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        topBar = {
            if (showBottomBar) {
                TopAppBar(
                    title = {
                        Text(
                            if (userName.contains("@")) "RoadRescue" else "Hi, ${userName.substringBefore(" ")}",
                        )
                    },
                    actions = {
                        IconButton(onClick = onSignOut) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign out")
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == NavRoutes.HOME,
                        onClick = {
                            navController.navigate(NavRoutes.HOME) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Rescue") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == NavRoutes.MARKETPLACE,
                        onClick = {
                            navController.navigate(NavRoutes.MARKETPLACE) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                        label = { Text("Parts") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == NavRoutes.SERVICE_BOOKING,
                        onClick = {
                            navController.navigate(NavRoutes.SERVICE_BOOKING) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Build, contentDescription = null) },
                        label = { Text("Service") },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.HOME,
            modifier = Modifier.padding(padding),
        ) {
            composable(NavRoutes.HOME) {
                HomeScreen(
                    onRequestTowing = { navController.navigate(NavRoutes.requestForm("towing")) },
                    onRequestMechanic = { navController.navigate(NavRoutes.requestForm("mechanic")) },
                    onOpenHistory = { navController.navigate(NavRoutes.REQUEST_HISTORY) },
                    onOpenMarketplace = {
                        navController.navigate(NavRoutes.MARKETPLACE) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    onBookService = {
                        navController.navigate(NavRoutes.SERVICE_BOOKING) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(
                route = NavRoutes.REQUEST_FORM,
                arguments = listOf(navArgument("type") { type = NavType.StringType }),
            ) { entry ->
                val type = when (entry.arguments?.getString("type")) {
                    "towing" -> RequestType.TOWING
                    else -> RequestType.MECHANIC
                }
                RequestFormScreen(
                    requestType = type,
                    onBack = { navController.popBackStack() },
                    onSubmitted = {
                        navController.navigate(NavRoutes.BIDDING) {
                            popUpTo(NavRoutes.HOME)
                        }
                    },
                )
            }

            composable(NavRoutes.BIDDING) {
                BiddingScreen(
                    onBidAccepted = {
                        navController.navigate(NavRoutes.ACTIVE_JOB) {
                            popUpTo(NavRoutes.HOME)
                        }
                    },
                    onCancel = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.HOME) { inclusive = true }
                        }
                    },
                )
            }

            composable(NavRoutes.ACTIVE_JOB) {
                ActiveJobScreen(
                    onDone = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.HOME) { inclusive = true }
                        }
                    },
                )
            }

            composable(NavRoutes.REQUEST_HISTORY) {
                RequestHistoryScreen(onBack = { navController.popBackStack() })
            }

            composable(NavRoutes.MARKETPLACE) { MarketplaceScreen() }
            composable(NavRoutes.SERVICE_BOOKING) { ServiceBookingScreen() }
        }
    }
}
