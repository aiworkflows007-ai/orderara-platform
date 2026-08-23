package com.vigizoomato.customer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
    val menuItem: MenuItem,
    val restaurantId: String,
    val restaurantName: String,
    val restaurantMinOrder: Double,
    val quantity: Int = 1,
    val specialInstructions: String = ""
) {
    val totalPrice: Double
        get() = menuItem.price * quantity
}

@Serializable
data class Coupon(
    val code: String,
    val restaurantId: String? = null, // null means platform wide
    val restaurantName: String? = null,
    val discountPercentage: Int = 0,
    val maxDiscount: Double = 100.0,
    val flatDiscount: Double = 0.0,
    val minOrderAmount: Double = 199.0,
    val description: String
) {
    fun calculateDiscount(orderAmount: Double): Double {
        if (orderAmount < minOrderAmount) return 0.0
        return if (discountPercentage > 0) {
            val percentageOff = orderAmount * (discountPercentage / 100.0)
            minOf(percentageOff, maxDiscount)
        } else {
            flatDiscount
        }
    }
}
