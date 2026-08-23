package com.vigizoomato.customer.data.repository

import com.vigizoomato.customer.data.mock.MockDataProvider
import com.vigizoomato.customer.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class OrderRepository {

    private val _orders = MutableStateFlow(MockDataProvider.sampleActiveOrders)
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    fun getOrderById(orderId: String): Order? {
        return _orders.value.find { it.id == orderId }
    }

    fun placeOrder(
        cartSummary: CartSummary,
        deliveryAddress: Address,
        paymentMethod: String,
        deliveryInstructions: String
    ): Order {
        val orderId = "VZ-ORD-${(1000..9999).random()}"
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val subOrders = cartSummary.groups.mapIndexed { index, group ->
            SubOrder(
                subOrderId = "SUB-0${index + 1}",
                orderId = orderId,
                restaurantId = group.restaurantId,
                restaurantName = group.restaurantName,
                items = group.items,
                subTotal = group.subTotal,
                deliveryFee = group.deliveryFee,
                discount = group.discount,
                status = OrderStatus.PLACED,
                statusHistory = listOf(
                    StatusTimestamp(
                        status = OrderStatus.PLACED,
                        timeFormatted = timeFormat,
                        note = "Order placed and transmitted to ${group.restaurantName}"
                    )
                ),
                estimatedDeliveryMinutes = 35 + (index * 5),
                driverName = "Assigned Delivery Staff",
                driverPhone = "+91 98765 ${10000 + index}"
            )
        }

        val newOrder = Order(
            id = orderId,
            customerId = "user_101",
            subOrders = subOrders,
            itemsTotal = cartSummary.totalItemsPrice,
            totalDeliveryFee = cartSummary.totalDeliveryFee,
            taxesAndPlatformFee = cartSummary.taxesAndPackaging,
            totalDiscount = cartSummary.totalDiscount,
            grandTotal = cartSummary.grandTotal,
            paymentStatus = "PAID",
            paymentMethod = paymentMethod,
            transactionId = "TXN-${UUID.randomUUID().toString().take(12).uppercase()}",
            deliveryAddress = deliveryAddress,
            deliveryInstructions = deliveryInstructions,
            createdAt = "Just now"
        )

        _orders.value = listOf(newOrder) + _orders.value
        return newOrder
    }

    fun advanceSubOrderStatus(orderId: String, subOrderId: String, newStatus: OrderStatus) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        _orders.value = _orders.value.map { order ->
            if (order.id == orderId) {
                val updatedSubOrders = order.subOrders.map { sub ->
                    if (sub.subOrderId == subOrderId) {
                        val newHistory = sub.statusHistory + StatusTimestamp(
                            status = newStatus,
                            timeFormatted = timeFormat,
                            note = "Status updated to ${newStatus.label}"
                        )
                        sub.copy(status = newStatus, statusHistory = newHistory)
                    } else sub
                }
                order.copy(subOrders = updatedSubOrders)
            } else order
        }
    }

    fun rateSubOrder(orderId: String, subOrderId: String, ratingScore: Double) {
        _orders.value = _orders.value.map { order ->
            if (order.id == orderId) {
                val updatedSubOrders = order.subOrders.map { sub ->
                    if (sub.subOrderId == subOrderId) {
                        sub.copy(isRated = true, ratingScore = ratingScore)
                    } else sub
                }
                order.copy(subOrders = updatedSubOrders)
            } else order
        }
    }
}
