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
    val paymentSuccess: Boolean = false,
    val errorMessage: String? = null
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
    private val _errorMessage = MutableStateFlow<String?>(null)

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
        paymentStateFlow,
        _errorMessage
    ) { _, user, pState, error ->
        val currentAddr = user?.savedAddresses?.find { it.id == user.selectedAddressId }
            ?: user?.savedAddresses?.firstOrNull()

        CheckoutUiState(
            cartSummary = cartRepository.getCartSummary(),
            deliveryAddress = currentAddr,
            selectedPaymentMethod = pState.selectedMethod,
            isProcessingPayment = pState.isProcessing,
            placedOrder = pState.order,
            paymentSuccess = pState.isSuccess,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CheckoutUiState(cartSummary = cartRepository.getCartSummary())
    )

    fun selectPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    /**
     * Pays, then sends the cart to the server where it is split into one
     * sub-order per restaurant. The order only counts as placed once the
     * server confirms it — a closed or suspended restaurant is reported here
     * instead of silently producing an order no kitchen will ever see.
     */
    fun executePaymentAndPlaceOrder(deliveryInstructions: String, onOrderPlaced: (String) -> Unit) {
        viewModelScope.launch {
            _errorMessage.value = null
            _isProcessing.value = true
            // Simulate payment gateway authentication and bank handshake
            delay(1800)

            val summary = cartRepository.getCartSummary()
            val addr = uiState.value.deliveryAddress
            val user = authRepository.currentUser.value
            if (addr == null) {
                _isProcessing.value = false
                _errorMessage.value = "Please add a delivery address first"
                return@launch
            }

            orderRepository.placeOrder(
                cartSummary = summary,
                deliveryAddress = addr,
                paymentMethod = _selectedPaymentMethod.value,
                deliveryInstructions = deliveryInstructions,
                customerName = user?.name ?: "Customer",
                customerPhone = user?.phone ?: ""
            ) { order, error ->
                _isProcessing.value = false
                if (order != null) {
                    cartRepository.clearCart()
                    _placedOrder.value = order
                    _paymentSuccess.value = true
                    onOrderPlaced(order.id)
                } else {
                    _errorMessage.value = error ?: "Could not place your order. Please try again."
                }
            }
        }
    }
}
