package com.orderara.partner.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orderara.partner.OrderAraPartnerApp
import com.orderara.partner.data.models.IncomingSubOrder
import com.orderara.partner.data.models.PartnerOrderStatus
import com.orderara.partner.data.models.RestaurantProfile
import com.orderara.partner.data.models.StaffRole
import kotlinx.coroutines.flow.*

data class OrdersUiState(
    val orders: List<IncomingSubOrder> = emptyList(),
    val profile: RestaurantProfile = RestaurantProfile(),
    val currentRole: StaffRole = StaffRole.OWNER,
    val selectedStatusFilter: PartnerOrderStatus? = null
)

class PartnerOrdersViewModel : ViewModel() {
    private val orderRepo = OrderAraPartnerApp.instance.orderRepository
    private val authRepo = OrderAraPartnerApp.instance.authRepository

    private val _selectedStatusFilter = MutableStateFlow<PartnerOrderStatus?>(null)

    val uiState: StateFlow<OrdersUiState> = combine(
        orderRepo.orders,
        authRepo.currentProfile,
        authRepo.currentStaff,
        _selectedStatusFilter
    ) { orders, profile, staff, filter ->
        val filtered = if (filter != null) {
            orders.filter { it.status == filter }
        } else {
            orders
        }
        OrdersUiState(
            orders = filtered,
            profile = profile,
            currentRole = staff.role,
            selectedStatusFilter = filter
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OrdersUiState())

    fun selectFilter(status: PartnerOrderStatus?) {
        _selectedStatusFilter.value = status
    }

    fun acceptOrder(subOrderId: String) {
        orderRepo.acceptOrder(subOrderId)
    }

    fun rejectOrder(subOrderId: String) {
        orderRepo.rejectOrder(subOrderId)
    }

    fun updateStatus(subOrderId: String, newStatus: PartnerOrderStatus) {
        orderRepo.updateOrderStatus(subOrderId, newStatus)
    }

    fun toggleStoreOpen() {
        authRepo.toggleStoreOpen()
    }

    fun switchRole(role: StaffRole) {
        authRepo.switchStaffRole(role)
    }
}
