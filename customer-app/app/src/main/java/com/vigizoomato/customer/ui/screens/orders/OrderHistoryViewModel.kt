package com.vigizoomato.customer.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vigizoomato.customer.VigiZoomatoApp
import com.vigizoomato.customer.data.models.Order
import com.vigizoomato.customer.data.repository.CartRepository
import com.vigizoomato.customer.data.repository.OrderRepository
import kotlinx.coroutines.flow.*

data class OrderHistoryUiState(
    val orders: List<Order> = emptyList()
)

class OrderHistoryViewModel(
    private val orderRepository: OrderRepository = VigiZoomatoApp.container.orderRepository,
    private val cartRepository: CartRepository = VigiZoomatoApp.container.cartRepository
) : ViewModel() {

    val uiState: StateFlow<OrderHistoryUiState> = orderRepository.orders
        .map { OrderHistoryUiState(orders = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = OrderHistoryUiState()
        )

    fun reorderItems(order: Order) {
        order.subOrders.forEach { sub ->
            sub.items.forEach { item ->
                cartRepository.addToCart(item.menuItem, sub.restaurantName, 199.0)
            }
        }
    }
}
