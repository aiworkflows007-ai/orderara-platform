package com.vigizoomato.customer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: String,
    val restaurantId: String,
    val customerName: String,
    val rating: Double,
    val comment: String,
    val date: String,
    val orderedDishes: List<String> = emptyList()
)

@Serializable
enum class SenderType {
    CUSTOMER,
    RESTAURANT_STAFF,
    SYSTEM
}

@Serializable
data class ChatMessage(
    val id: String,
    val orderId: String,
    val subOrderId: String,
    val restaurantName: String,
    val senderType: SenderType,
    val senderName: String,
    val messageText: String,
    val timestamp: String,
    val isDelivered: Boolean = true
)
