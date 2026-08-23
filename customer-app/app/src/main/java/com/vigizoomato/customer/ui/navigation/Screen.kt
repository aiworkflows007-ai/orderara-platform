package com.vigizoomato.customer.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Orders : Screen("orders")
    object Profile : Screen("profile")
    object Favorites : Screen("favorites")
    object AddressManager : Screen("addresses")
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    
    object RestaurantDetail : Screen("restaurant/{restaurantId}") {
        fun createRoute(restaurantId: String) = "restaurant/$restaurantId"
    }

    object OrderTracking : Screen("tracking/{orderId}") {
        fun createRoute(orderId: String) = "tracking/$orderId"
    }

    object OrderChat : Screen("chat/{subOrderId}/{restaurantName}") {
        fun createRoute(subOrderId: String, restaurantName: String) = "chat/$subOrderId/$restaurantName"
    }

    object RateOrder : Screen("rate/{orderId}/{subOrderId}") {
        fun createRoute(orderId: String, subOrderId: String) = "rate/$orderId/$subOrderId"
    }

    object Login : Screen("login")
    object OtpVerification : Screen("otp/{phoneNumber}") {
        fun createRoute(phoneNumber: String) = "otp/$phoneNumber"
    }
}
