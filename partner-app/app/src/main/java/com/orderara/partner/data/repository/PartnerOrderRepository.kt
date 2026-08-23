package com.orderara.partner.data.repository

import com.orderara.partner.data.mock.PartnerMockData
import com.orderara.partner.data.models.IncomingSubOrder
import com.orderara.partner.data.models.PartnerOrderStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PartnerOrderRepository {
    private val _orders = MutableStateFlow<List<IncomingSubOrder>>(PartnerMockData.initialOrders.toList())
    val orders: StateFlow<List<IncomingSubOrder>> = _orders.asStateFlow()

    fun updateOrderStatus(subOrderId: String, newStatus: PartnerOrderStatus, assignedRider: String? = null) {
        _orders.update { current ->
            current.map { order ->
                if (order.subOrderId == subOrderId) {
                    order.copy(
                        status = newStatus,
                        assignedRiderName = assignedRider ?: order.assignedRiderName
                    )
                } else order
            }
        }
    }

    fun acceptOrder(subOrderId: String, prepMinutes: Int = 20) {
        _orders.update { current ->
            current.map { order ->
                if (order.subOrderId == subOrderId) {
                    order.copy(
                        status = PartnerOrderStatus.ACCEPTED,
                        estimatedPrepMinutes = prepMinutes
                    )
                } else order
            }
        }
    }

    fun rejectOrder(subOrderId: String) {
        _orders.update { current ->
            current.map { order ->
                if (order.subOrderId == subOrderId) {
                    order.copy(status = PartnerOrderStatus.REJECTED)
                } else order
            }
        }
    }

    fun getOrderById(subOrderId: String): IncomingSubOrder? {
        return _orders.value.find { it.subOrderId == subOrderId }
    }
}
