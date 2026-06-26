package com.jayjaycode.miniproject.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.jayjaycode.miniproject.ui.components.AppTopBar
import com.jayjaycode.miniproject.ui.components.ActiveJobFloatingCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.jayjaycode.miniproject.ui.screens.AddPartListingScreen
import com.jayjaycode.miniproject.ui.screens.AddServiceListingScreen
import com.jayjaycode.miniproject.ui.components.BiddingFloatingCard
import com.jayjaycode.miniproject.ui.screens.BiddingScreen
import com.jayjaycode.miniproject.ui.screens.BreakdownRequestDetailScreen
import com.jayjaycode.miniproject.ui.screens.CheckoutScreen
import com.jayjaycode.miniproject.ui.screens.HomeScreen
import com.jayjaycode.miniproject.ui.screens.MarketplaceScreen
import com.jayjaycode.miniproject.ui.screens.MyOrdersScreen
import com.jayjaycode.miniproject.ui.screens.OrderConfirmationScreen
import com.jayjaycode.miniproject.ui.screens.PartListingDetailRoute
import com.jayjaycode.miniproject.ui.screens.PartOrderDetailScreen
import com.jayjaycode.miniproject.ui.screens.ProfileScreen
import com.jayjaycode.miniproject.ui.screens.ProviderDashboardScreen
import com.jayjaycode.miniproject.ui.screens.RegisterBusinessScreen
import com.jayjaycode.miniproject.ui.screens.RequestFormScreen
import com.jayjaycode.miniproject.ui.screens.RequestHistoryScreen
import com.jayjaycode.miniproject.ui.screens.ServiceBookingDetailScreen
import com.jayjaycode.miniproject.ui.screens.ServiceBookingScreen
import com.jayjaycode.miniproject.ui.viewmodel.BreakdownRequestDetailViewModel
import com.jayjaycode.miniproject.ui.viewmodel.MarketplaceViewModel
import com.jayjaycode.miniproject.ui.viewmodel.PartOrderDetailViewModel
import com.jayjaycode.miniproject.ui.viewmodel.RescueViewModel
import com.jayjaycode.miniproject.ui.viewmodel.ServiceBookingDetailViewModel
import com.jayjaycode.miniproject.util.NotificationDeepLink

private val bottomNavRoutes = setOf(NavRoutes.HOME, NavRoutes.MARKETPLACE, NavRoutes.SERVICE_BOOKING)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadRescueApp(
    userName: String,
    userEmail: String,
    onSignOut: () -> Unit,
    notificationDeepLink: NotificationDeepLink? = null,
    onNotificationHandled: () -> Unit = {},
) {
    val navController = rememberNavController()
    val rescueViewModel: RescueViewModel = viewModel()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route?.substringBefore("/")

    val isBiddingActive by rescueViewModel.isBiddingActive.collectAsState()
    val activeRequest by rescueViewModel.activeRequest.collectAsState()
    val biddingSecondsLeft by rescueViewModel.secondsLeft.collectAsState()
    val bids by rescueViewModel.bids.collectAsState()
    val lowestBid by rescueViewModel.lowestBid.collectAsState()
    val biddingOverlayExpanded by rescueViewModel.biddingOverlayExpanded.collectAsState()
    val acceptedJob by rescueViewModel.acceptedJob.collectAsState()
    val activeJobOverlayExpanded by rescueViewModel.activeJobOverlayExpanded.collectAsState()
    val isActiveJobVisible by rescueViewModel.isActiveJobVisible.collectAsState()

    LaunchedEffect(notificationDeepLink) {
        val link = notificationDeepLink ?: return@LaunchedEffect
        when (link.type) {
            "new_bid", "bidding_ended" -> {
                navController.navigate(NavRoutes.BIDDING) { launchSingleTop = true }
            }
            "bid_accepted", "bid_won" -> {
                navController.navigate(NavRoutes.ACTIVE_JOB) { launchSingleTop = true }
            }
            "open_job", "new_part_order", "new_service_booking", "bid_lost" -> {
                navController.navigate(NavRoutes.PROVIDER_DASHBOARD) { launchSingleTop = true }
            }
            "part_order_update" -> {
                val destination = link.orderId?.let { NavRoutes.partOrderDetail(it) } ?: NavRoutes.MY_ORDERS
                navController.navigate(destination) { launchSingleTop = true }
            }
            "service_booking_update" -> {
                val destination = link.bookingId?.let { NavRoutes.serviceBookingDetail(it) } ?: NavRoutes.MY_ORDERS
                navController.navigate(destination) { launchSingleTop = true }
            }
        }
        onNotificationHandled()
    }

    val showBottomBar = currentRoute in bottomNavRoutes
    val showMainTopBar = showBottomBar
    val mainTopBarTitle = when (currentRoute) {
        NavRoutes.HOME -> "Rescue"
        NavRoutes.MARKETPLACE -> "Marketplace"
        NavRoutes.SERVICE_BOOKING -> "Book service"
        else -> ""
    }

    Scaffold(
        topBar = {
            if (showMainTopBar) {
                AppTopBar(
                    title = mainTopBarTitle,
                    actions = {
                        IconButton(onClick = { navController.navigate(NavRoutes.PROFILE) }) {
                            Icon(Icons.Default.Person, contentDescription = "Profile")
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
        Box(modifier = Modifier.padding(padding)) {
            NavHost(
                navController = navController,
                startDestination = NavRoutes.HOME,
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
                    viewModel = rescueViewModel,
                    onBack = { navController.popBackStack() },
                    onSubmitted = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.HOME) { inclusive = true }
                        }
                    },
                )
            }

            composable(NavRoutes.BIDDING) {
                BiddingScreen(
                    viewModel = rescueViewModel,
                    onBidAccepted = { },
                    onBrowseApp = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
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
                    viewModel = rescueViewModel,
                    onBrowseApp = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }

            composable(NavRoutes.REQUEST_HISTORY) {
                RequestHistoryScreen(
                    onBack = { navController.popBackStack() },
                    onOpenRequest = { navController.navigate(NavRoutes.requestDetail(it)) },
                )
            }

            composable(
                route = NavRoutes.REQUEST_DETAIL,
                arguments = listOf(navArgument("requestId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val requestId = backStackEntry.arguments?.getString("requestId").orEmpty()
                val detailViewModel: BreakdownRequestDetailViewModel = viewModel(
                    factory = BreakdownRequestDetailViewModel.factory(requestId),
                )
                BreakdownRequestDetailScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = detailViewModel,
                )
            }

            composable(NavRoutes.MARKETPLACE) {
                MarketplaceScreen(
                    onPartClick = { partId ->
                        navController.navigate(NavRoutes.partListing(partId))
                    },
                    onCheckout = {
                        navController.navigate(NavRoutes.CHECKOUT)
                    },
                )
            }

            composable(NavRoutes.CHECKOUT) {
                val parentEntry = remember {
                    navController.getBackStackEntry(NavRoutes.MARKETPLACE)
                }
                val marketplaceViewModel: MarketplaceViewModel = viewModel(parentEntry)
                CheckoutScreen(
                    viewModel = marketplaceViewModel,
                    onBack = { navController.popBackStack() },
                    onOrderPlaced = {
                        navController.navigate(NavRoutes.ORDER_CONFIRMATION) {
                            popUpTo(NavRoutes.CHECKOUT) { inclusive = true }
                        }
                    },
                )
            }

            composable(NavRoutes.ORDER_CONFIRMATION) {
                val parentEntry = remember {
                    navController.getBackStackEntry(NavRoutes.MARKETPLACE)
                }
                val marketplaceViewModel: MarketplaceViewModel = viewModel(parentEntry)
                OrderConfirmationScreen(
                    viewModel = marketplaceViewModel,
                    onDone = {
                        navController.popBackStack(NavRoutes.MARKETPLACE, inclusive = false)
                    },
                )
            }

            composable(
                route = NavRoutes.PART_LISTING_DETAIL,
                arguments = listOf(navArgument("partId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val partId = backStackEntry.arguments?.getString("partId").orEmpty()
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavRoutes.MARKETPLACE)
                }
                val marketplaceViewModel: MarketplaceViewModel = viewModel(parentEntry)
                PartListingDetailRoute(
                    partId = partId,
                    viewModel = marketplaceViewModel,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(NavRoutes.SERVICE_BOOKING) { ServiceBookingScreen() }

            composable(NavRoutes.PROFILE) {
                ProfileScreen(
                    userName = userName,
                    userEmail = userEmail,
                    onBack = { navController.popBackStack() },
                    onRegisterBusiness = { navController.navigate(NavRoutes.REGISTER_BUSINESS) },
                    onOpenDashboard = { navController.navigate(NavRoutes.PROVIDER_DASHBOARD) },
                    onOpenMyOrders = { navController.navigate(NavRoutes.MY_ORDERS) },
                    onOpenPartOrder = { navController.navigate(NavRoutes.partOrderDetail(it)) },
                    onOpenServiceBooking = { navController.navigate(NavRoutes.serviceBookingDetail(it)) },
                    onSignOut = onSignOut,
                )
            }

            composable(NavRoutes.MY_ORDERS) {
                MyOrdersScreen(
                    onBack = { navController.popBackStack() },
                    onOpenPartOrder = { navController.navigate(NavRoutes.partOrderDetail(it)) },
                    onOpenServiceBooking = { navController.navigate(NavRoutes.serviceBookingDetail(it)) },
                )
            }

            composable(
                route = NavRoutes.PART_ORDER_DETAIL,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId").orEmpty()
                val detailViewModel: PartOrderDetailViewModel = viewModel(
                    factory = PartOrderDetailViewModel.factory(orderId),
                )
                PartOrderDetailScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = detailViewModel,
                )
            }

            composable(
                route = NavRoutes.SERVICE_BOOKING_DETAIL,
                arguments = listOf(navArgument("bookingId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId").orEmpty()
                val detailViewModel: ServiceBookingDetailViewModel = viewModel(
                    factory = ServiceBookingDetailViewModel.factory(bookingId),
                )
                ServiceBookingDetailScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = detailViewModel,
                )
            }

            composable(NavRoutes.REGISTER_BUSINESS) {
                RegisterBusinessScreen(
                    onBack = { navController.popBackStack() },
                    onRegistered = { navController.popBackStack() },
                )
            }

            composable(NavRoutes.PROVIDER_DASHBOARD) {
                ProviderDashboardScreen(
                    onBack = { navController.popBackStack() },
                    onAddPart = { navController.navigate(NavRoutes.ADD_PART_LISTING) },
                    onAddService = { navController.navigate(NavRoutes.ADD_SERVICE_LISTING) },
                )
            }

            composable(NavRoutes.ADD_PART_LISTING) {
                AddPartListingScreen(onBack = { navController.popBackStack() })
            }

            composable(NavRoutes.ADD_SERVICE_LISTING) {
                AddServiceListingScreen(onBack = { navController.popBackStack() })
            }
            }

            if (isBiddingActive && activeRequest != null && currentRoute != NavRoutes.BIDDING) {
                BiddingFloatingCard(
                    request = activeRequest!!,
                    secondsLeft = biddingSecondsLeft,
                    bidCount = bids.size,
                    lowestBid = lowestBid,
                    progress = rescueViewModel.biddingProgressFraction(activeRequest, biddingSecondsLeft),
                    expanded = biddingOverlayExpanded,
                    autoAcceptLowest = activeRequest!!.autoAcceptLowestBid,
                    onToggleExpanded = { rescueViewModel.toggleBiddingOverlay() },
                    onOpenBidding = {
                        navController.navigate(NavRoutes.BIDDING) {
                            launchSingleTop = true
                        }
                    },
                    onExtendTime = { rescueViewModel.extendBiddingTime() },
                    onAutoAcceptChanged = { rescueViewModel.setAutoAcceptLowestBid(it) },
                    onAcceptLowest = { rescueViewModel.acceptLowestBid() },
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }

            if (isActiveJobVisible && acceptedJob != null && currentRoute != NavRoutes.ACTIVE_JOB) {
                val job = acceptedJob!!
                ActiveJobFloatingCard(
                    job = job,
                    expanded = activeJobOverlayExpanded,
                    completionActionLabel = rescueViewModel.customerCompletionActionLabel(job.request),
                    completionPendingMessage = rescueViewModel.customerCompletionPendingMessage(job.request),
                    onToggleExpanded = { rescueViewModel.toggleActiveJobOverlay() },
                    onOpenJob = {
                        navController.navigate(NavRoutes.ACTIVE_JOB) {
                            launchSingleTop = true
                        }
                    },
                    onRequestCompletion = { rescueViewModel.requestJobCompletion() },
                    onConfirmCompletion = { rescueViewModel.confirmJobCompletion() },
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }
    }
}
