package com.vigizoomato.customer.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.vigizoomato.customer.VigiZoomatoApp
import com.vigizoomato.customer.ui.components.CustomerBottomBar
import com.vigizoomato.customer.ui.screens.auth.LoginScreen
import com.vigizoomato.customer.ui.screens.auth.OtpVerificationScreen
import com.vigizoomato.customer.ui.screens.cart.MultiRestaurantCartScreen
import com.vigizoomato.customer.ui.screens.checkout.CheckoutScreen
import com.vigizoomato.customer.ui.screens.chat.OrderChatScreen
import com.vigizoomato.customer.ui.screens.favorites.FavoritesScreen
import com.vigizoomato.customer.ui.screens.home.HomeScreen
import com.vigizoomato.customer.ui.screens.orders.OrderHistoryScreen
import com.vigizoomato.customer.ui.screens.profile.AddressManagerScreen
import com.vigizoomato.customer.ui.screens.profile.ProfileScreen
import com.vigizoomato.customer.ui.screens.restaurant.RestaurantDetailScreen
import com.vigizoomato.customer.ui.screens.review.RateOrderScreen
import com.vigizoomato.customer.ui.screens.search.SearchScreen
import com.vigizoomato.customer.ui.screens.tracking.OrderTrackingScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val cartRepo = VigiZoomatoApp.container.cartRepository
    val cartItems by cartRepo.cartItems.collectAsState()
    val totalCartItems = cartItems.sumOf { it.quantity }

    val isBottomBarVisible = currentRoute in listOf(
        Screen.Home.route,
        Screen.Search.route,
        Screen.Orders.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                CustomerBottomBar(
                    currentRoute = currentRoute ?: Screen.Home.route,
                    onTabSelected = { tab ->
                        navController.navigate(tab.route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    cartItemCount = totalCartItems,
                    onCartClick = {
                        navController.navigate(Screen.Cart.route)
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isBottomBarVisible) innerPadding else androidx.compose.foundation.layout.PaddingValues())
        ) {
            // Home Feed
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onNavigateToRestaurant = { id -> navController.navigate(Screen.RestaurantDetail.createRoute(id)) },
                    onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                    onNavigateToAddresses = { navController.navigate(Screen.AddressManager.route) }
                )
            }

            // Search Screen
            composable(Screen.Search.route) {
                SearchScreen(
                    onNavigateToRestaurant = { id -> navController.navigate(Screen.RestaurantDetail.createRoute(id)) },
                    onNavigateToCart = { navController.navigate(Screen.Cart.route) }
                )
            }

            // Orders Screen
            composable(Screen.Orders.route) {
                OrderHistoryScreen(
                    onNavigateToTracking = { id -> navController.navigate(Screen.OrderTracking.createRoute(id)) },
                    onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                    onNavigateToRate = { oId, sId -> navController.navigate(Screen.RateOrder.createRoute(oId, sId)) }
                )
            }

            // Profile Screen
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToAddresses = { navController.navigate(Screen.AddressManager.route) },
                    onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                    onNavigateToOrders = { navController.navigate(Screen.Orders.route) },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Favorites Screen
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onNavigateToRestaurant = { id -> navController.navigate(Screen.RestaurantDetail.createRoute(id)) }
                )
            }

            // Address Manager Screen
            composable(Screen.AddressManager.route) {
                AddressManagerScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Restaurant Detail
            composable(
                route = Screen.RestaurantDetail.route,
                arguments = listOf(navArgument("restaurantId") { type = NavType.StringType })
            ) { backStackEntry ->
                val restId = backStackEntry.arguments?.getString("restaurantId") ?: ""
                RestaurantDetailScreen(
                    restaurantId = restId,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToCart = { navController.navigate(Screen.Cart.route) }
                )
            }

            // Multi-Restaurant Cart
            composable(Screen.Cart.route) {
                MultiRestaurantCartScreen(
                    onBackClick = { navController.popBackStack() },
                    onNavigateToCheckout = { navController.navigate(Screen.Checkout.route) },
                    onNavigateToAddresses = { navController.navigate(Screen.AddressManager.route) },
                    onExploreRestaurants = { navController.navigate(Screen.Home.route) }
                )
            }

            // Checkout
            composable(Screen.Checkout.route) {
                CheckoutScreen(
                    onBackClick = { navController.popBackStack() },
                    onOrderPlaced = { orderId ->
                        navController.navigate(Screen.OrderTracking.createRoute(orderId)) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }

            // Order Tracking
            composable(
                route = Screen.OrderTracking.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                OrderTrackingScreen(
                    orderId = orderId,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateToChat = { sId, rName -> navController.navigate(Screen.OrderChat.createRoute(sId, rName)) },
                    onNavigateToRate = { oId, sId -> navController.navigate(Screen.RateOrder.createRoute(oId, sId)) }
                )
            }

            // Order Chat
            composable(
                route = Screen.OrderChat.route,
                arguments = listOf(
                    navArgument("subOrderId") { type = NavType.StringType },
                    navArgument("restaurantName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val subOrderId = backStackEntry.arguments?.getString("subOrderId") ?: ""
                val restaurantName = backStackEntry.arguments?.getString("restaurantName") ?: ""
                OrderChatScreen(
                    subOrderId = subOrderId,
                    restaurantName = restaurantName,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Rate Order
            composable(
                route = Screen.RateOrder.route,
                arguments = listOf(
                    navArgument("orderId") { type = NavType.StringType },
                    navArgument("subOrderId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                val subOrderId = backStackEntry.arguments?.getString("subOrderId") ?: ""
                RateOrderScreen(
                    orderId = orderId,
                    subOrderId = subOrderId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Auth: Login
            composable(Screen.Login.route) {
                LoginScreen(
                    onOtpSent = { phone -> navController.navigate(Screen.OtpVerification.createRoute(phone)) }
                )
            }

            // Auth: OTP
            composable(
                route = Screen.OtpVerification.route,
                arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
            ) { backStackEntry ->
                val phone = backStackEntry.arguments?.getString("phoneNumber") ?: ""
                OtpVerificationScreen(
                    phoneNumber = phone,
                    onBackClick = { navController.popBackStack() },
                    onVerificationSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
