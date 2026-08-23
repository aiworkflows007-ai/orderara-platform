package com.vigizoomato.customer.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vigizoomato.customer.VigiZoomatoApp
import com.vigizoomato.customer.data.models.Address
import com.vigizoomato.customer.data.models.Coupon
import com.vigizoomato.customer.data.models.MenuItem
import com.vigizoomato.customer.data.repository.AuthRepository
import com.vigizoomato.customer.data.repository.CartRepository
import com.vigizoomato.customer.data.repository.CartSummary
import kotlinx.coroutines.flow.*

data class CouponFormState(
    val input: String = "",
    val message: String? = null,
    val isSuccess: Boolean = false
)

data class CartUiState(
    val cartSummary: CartSummary,
    val deliveryAddress: Address? = null,
    val deliveryInstructions: String = "",
    val couponInputText: String = "",
    val couponMessage: String? = null,
    val isCouponSuccess: Boolean = false,
    val availableCoupons: List<Coupon> = emptyList()
)

class CartViewModel(
    private val cartRepository: CartRepository = VigiZoomatoApp.container.cartRepository,
    private val authRepository: AuthRepository = VigiZoomatoApp.container.authRepository
) : ViewModel() {

    private val _couponInput = MutableStateFlow("")
    private val _couponMessage = MutableStateFlow<String?>(null)
    private val _isCouponSuccess = MutableStateFlow(false)
    private val _deliveryInstructions = MutableStateFlow("")

    private val couponStateFlow = combine(_couponInput, _couponMessage, _isCouponSuccess) { input, msg, success ->
        CouponFormState(input, msg, success)
    }

    val uiState: StateFlow<CartUiState> = combine(
        cartRepository.cartItems,
        authRepository.currentUser,
        couponStateFlow,
        _deliveryInstructions
    ) { _, user, couponState, instructions ->
        val currentAddr = user?.savedAddresses?.find { it.id == user.selectedAddressId }
            ?: user?.savedAddresses?.firstOrNull()

        CartUiState(
            cartSummary = cartRepository.getCartSummary(),
            deliveryAddress = currentAddr,
            deliveryInstructions = instructions,
            couponInputText = couponState.input,
            couponMessage = couponState.message,
            isCouponSuccess = couponState.isSuccess,
            availableCoupons = cartRepository.availableCoupons
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CartUiState(
            cartSummary = cartRepository.getCartSummary(),
            availableCoupons = cartRepository.availableCoupons
        )
    )

    fun onCouponInputChanged(code: String) {
        _couponInput.value = code
        _couponMessage.value = null
    }

    fun applyCoupon(coupon: Coupon) {
        val success = cartRepository.applyCoupon(coupon)
        if (success) {
            _couponMessage.value = "Coupon '${coupon.code}' applied successfully!"
            _isCouponSuccess.value = true
        } else {
            _couponMessage.value = "Coupon '${coupon.code}' is not applicable for your current cart."
            _isCouponSuccess.value = false
        }
    }

    fun applyEnteredCoupon() {
        val code = _couponInput.value.trim().uppercase()
        val found = cartRepository.availableCoupons.find { it.code.equals(code, ignoreCase = true) }
        if (found != null) {
            applyCoupon(found)
        } else {
            _couponMessage.value = "Invalid coupon code '$code'"
            _isCouponSuccess.value = false
        }
    }

    fun removeCoupon(couponCode: String) {
        cartRepository.removeCoupon(couponCode)
        _couponMessage.value = null
    }

    fun onInstructionsChanged(text: String) {
        _deliveryInstructions.value = text
    }

    fun addToCart(item: MenuItem, restaurantName: String, minOrder: Double) {
        cartRepository.addToCart(item, restaurantName, minOrder)
    }

    fun removeFromCart(itemId: String) {
        cartRepository.removeFromCart(itemId)
    }

    fun clearCart() {
        cartRepository.clearCart()
    }
}
