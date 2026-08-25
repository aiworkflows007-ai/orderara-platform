package com.vigizoomato.customer.ui.screens.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vigizoomato.customer.VigiZoomatoApp
import com.vigizoomato.customer.data.models.Order
import com.vigizoomato.customer.data.repository.OrderRepository
import kotlinx.coroutines.flow.*

data class OrderTrackingUiState(
    val order: Order? = null
)

class OrderTrackingViewModel(
    private val orderRepository: OrderRepository = VigiZoomatoApp.container.orderRepository
) : ViewModel() {

    private val _orderId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<OrderTrackingUiState> = combine(
        _orderId,
        orderRepository.orders
    ) { id, orders ->
        val ord = if (id != null) orders.find { it.id == id } else orders.firstOrNull()
        OrderTrackingUiState(order = ord)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OrderTrackingUiState()
    )

    fun loadOrder(orderId: String) {
        _orderId.value = orderId
    }

}
