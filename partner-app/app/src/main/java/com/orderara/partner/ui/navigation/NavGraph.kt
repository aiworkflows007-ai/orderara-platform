package com.orderara.partner.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.orderara.partner.OrderAraPartnerApp
import com.orderara.partner.data.models.StaffRole
import com.orderara.partner.ui.components.PartnerBottomBar
import com.orderara.partner.ui.components.PartnerTab
import com.orderara.partner.ui.screens.analytics.AnalyticsScreen
import com.orderara.partner.ui.screens.chat.PartnerChatScreen
import com.orderara.partner.ui.screens.menu.MenuManagementScreen
import com.orderara.partner.ui.screens.orders.PartnerOrdersScreen
import com.orderara.partner.ui.screens.settings.SettingsScreen
import com.orderara.partner.ui.screens.subscription.SubscriptionScreen

@Composable
fun PartnerNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Orders.route

    val authRepo = OrderAraPartnerApp.instance.authRepository
    val currentStaff by authRepo.currentStaff.collectAsState()

    val showBottomBar = currentRoute in listOf(
        Screen.Orders.route,
        Screen.Menu.route,
        Screen.Analytics.route,
        Screen.Billing.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                PartnerBottomBar(
                    currentRoute = currentRoute,
                    currentRole = currentStaff.role,
                    onTabSelected = { tab ->
                        navController.navigate(tab.route) {
                            popUpTo(Screen.Orders.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Orders.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Orders.route) {
                PartnerOrdersScreen(
                    onNavigateToChat = { subOrderId, customerName ->
                        navController.navigate(Screen.Chat.createRoute(subOrderId, customerName))
                    }
                )
            }

            composable(Screen.Menu.route) {
                MenuManagementScreen()
            }

            composable(Screen.Analytics.route) {
                AnalyticsScreen()
            }

            composable(Screen.Billing.route) {
                SubscriptionScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            composable(
                route = Screen.Chat.route,
                arguments = listOf(
                    navArgument("subOrderId") { type = NavType.StringType },
                    navArgument("customerName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val subOrderId = backStackEntry.arguments?.getString("subOrderId") ?: ""
                val customerName = backStackEntry.arguments?.getString("customerName") ?: "Customer"
                PartnerChatScreen(
                    subOrderId = subOrderId,
                    customerName = customerName,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
