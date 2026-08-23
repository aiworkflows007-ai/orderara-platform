package com.orderara.partner.ui.navigation

sealed class Screen(val route: String) {
    object Orders : Screen("orders")
    object Menu : Screen("menu")
    object Analytics : Screen("analytics")
    object Billing : Screen("billing")
    object Settings : Screen("settings")
    object Chat : Screen("chat/{subOrderId}/{customerName}") {
        fun createRoute(subOrderId: String, customerName: String) = "chat/$subOrderId/$customerName"
    }
}
