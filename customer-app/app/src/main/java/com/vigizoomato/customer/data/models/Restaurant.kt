package com.vigizoomato.customer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class MenuItem(
    val id: String,
    val restaurantId: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val imageUrl: String = "",
    val isVeg: Boolean = true,
    val isAvailable: Boolean = true,
    val isBestSeller: Boolean = false,
    val spicyLevel: Int = 1, // 0 = None, 1 = Mild, 2 = Medium, 3 = Hot
    val preparationTimeMinutes: Int = 20
)

@Serializable
data class Restaurant(
    val id: String,
    val name: String,
    val description: String,
    val cuisineTypes: List<String>,
    val rating: Double,
    val ratingCount: Int,
    val deliveryTimeMinutes: Int,
    val deliveryRadiusKm: Double,
    val distanceKm: Double,
    val minOrderValue: Double,
    val bannerUrl: String = "",
    val logoUrl: String = "",
    val isVegOnly: Boolean = false,
    val isPromoted: Boolean = false,
    val discountOffer: String? = null,
    val phoneNumber: String = "+91 9988776655",
    val address: String = "Indiranagar, Bangalore",
    val isOpen: Boolean = true,
    val isFavorite: Boolean = false
) {
    val isWithinDeliveryRange: Boolean
        get() = distanceKm <= deliveryRadiusKm
}
