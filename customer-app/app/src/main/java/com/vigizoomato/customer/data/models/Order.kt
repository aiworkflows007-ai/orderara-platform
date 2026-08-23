package com.vigizoomato.customer.data.models

import kotlinx.serialization.Serializable

@Serializable
enum class OrderStatus(val label: String, val stepIndex: Int) {
    PLACED("Order Placed", 0),
    ACCEPTED("Restaurant Accepted", 1),
    PREPARING("Preparing Food", 2),
    OUT_FOR_DELIVERY("Out for Delivery", 3),
    DELIVERED("Delivered", 4),
    CANCELLED("Cancelled", -1)
}

@Serializable
data class StatusTimestamp(
    val status: OrderStatus,
    val timeFormatted: String,
    val note: String = ""
)

@Serializable
data class SubOrder(
    val subOrderId: String,
    val orderId: String,
    val restaurantId: String,
    val restaurantName: String,
    val restaurantPhone: String = "+91 98765 00000",
    val items: List<CartItem>,
    val subTotal: Double,
    val deliveryFee: Double,
    val discount: Double = 0.0,
    val status: OrderStatus = OrderStatus.PLACED,
    val statusHistory: List<StatusTimestamp> = emptyList(),
    val estimatedDeliveryMinutes: Int = 30,
    val driverName: String = "Restaurant Staff (Raju)",
    val driverPhone: String = "+91 91234 56789",
    val isRated: Boolean = false,
    val ratingScore: Double = 0.0
) {
    val totalAmount: Double
        get() = subTotal + deliveryFee - discount
}

@Serializable
data class Order(
    val id: String,
    val customerId: String,
    val subOrders: List<SubOrder>,
    val itemsTotal: Double,
    val totalDeliveryFee: Double,
    val taxesAndPlatformFee: Double,
    val totalDiscount: Double,
    val grandTotal: Double,
    val paymentStatus: String = "PAID",
    val paymentMethod: String = "UPI (Google Pay)",
    val transactionId: String = "TXN_78912345",
    val deliveryAddress: Address,
    val deliveryInstructions: String = "",
    val createdAt: String = "Just now"
) {
    val isMultiRestaurant: Boolean
        get() = subOrders.size > 1

    val overallStatus: OrderStatus
        get() {
            if (subOrders.all { it.status == OrderStatus.DELIVERED }) return OrderStatus.DELIVERED
            if (subOrders.any { it.status == OrderStatus.OUT_FOR_DELIVERY }) return OrderStatus.OUT_FOR_DELIVERY
            if (subOrders.any { it.status == OrderStatus.PREPARING }) return OrderStatus.PREPARING
            if (subOrders.any { it.status == OrderStatus.ACCEPTED }) return OrderStatus.ACCEPTED
            return OrderStatus.PLACED
        }
}
