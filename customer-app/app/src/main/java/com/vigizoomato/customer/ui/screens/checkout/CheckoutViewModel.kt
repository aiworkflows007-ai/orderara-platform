package com.vigizoomato.customer.ui.screens.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vigizoomato.customer.VigiZoomatoApp
import com.vigizoomato.customer.data.models.Address
import com.vigizoomato.customer.data.models.Order
import com.vigizoomato.customer.data.repository.AuthRepository
import com.vigizoomato.customer.data.repository.CartRepository
import com.vigizoomato.customer.data.repository.CartSummary
import com.vigizoomato.customer.data.repository.OrderRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PaymentState(
    val selectedMethod: String = "UPI (Google Pay)",
    val isProcessing: Boolean = false,
    val isSuccess: Boolean = false,
    val order: Order? = null
)

data class CheckoutUiState(
    val cartSummary: CartSummary,
    val deliveryAddress: Address? = null,
    val selectedPaymentMethod: String = "UPI (Google Pay)",
    val isProcessingPayment: Boolean = false,
    val placedOrder: Order? = null,
    val paymentSuccess: Boolean = false
)

class CheckoutViewModel(
    private val cartRepository: CartRepository = VigiZoomatoApp.container.cartRepository,
    private val orderRepository: OrderRepository = VigiZoomatoApp.container.orderRepository,
    private val authRepository: AuthRepository = VigiZoomatoApp.container.authRepository
) : ViewModel() {

    private val _selectedPaymentMethod = MutableStateFlow("UPI (Google Pay)")
    private val _isProcessing = MutableStateFlow(false)
    private val _placedOrder = MutableStateFlow<Order?>(null)
    private val _paymentSuccess = MutableStateFlow(false)

    private val paymentStateFlow = combine(
        _selectedPaymentMethod,
        _isProcessing,
        _placedOrder,
        _paymentSuccess
    ) { method, isProc, order, success ->
        PaymentState(method, isProc, success, order)
    }

    val uiState: StateFlow<CheckoutUiState> = combine(
        cartRepository.cartItems,
        authRepository.currentUser,
        paymentStateFlow
    ) { _, user, pState ->
        val currentAddr = user?.savedAddresses?.find { it.id == user.selectedAddressId }
            ?: user?.savedAddresses?.firstOrNull()

        CheckoutUiState(
            cartSummary = cartRepository.getCartSummary(),
            deliveryAddress = currentAddr,
            selectedPaymentMethod = pState.selectedMethod,
            isProcessingPayment = pState.isProcessing,
            placedOrder = pState.order,
            paymentSuccess = pState.isSuccess
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CheckoutUiState(cartSummary = cartRepository.getCartSummary())
    )

    fun selectPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }

    fun executePaymentAndPlaceOrder(deliveryInstructions: String, onOrderPlaced: (String) -> Unit) {
        viewModelScope.launch {
            _isProcessing.value = true
            // Simulate payment gateway authentication and bank handshake
            delay(1800)
            val summary = cartRepository.getCartSummary()
            val addr = uiState.value.deliveryAddress ?: return@launch

            val order = orderRepository.placeOrder(
                cartSummary = summary,
                deliveryAddress = addr,
                paymentMethod = _selectedPaymentMethod.value,
                deliveryInstructions = deliveryInstructions
            )

            // Clear Cart after successful checkout
            cartRepository.clearCart()

            _placedOrder.value = order
            _paymentSuccess.value = true
            _isProcessing.value = false

            delay(600)
            onOrderPlaced(order.id)
        }
    }
}
