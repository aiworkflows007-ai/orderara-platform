package com.vigizoomato.customer.data.repository

import com.vigizoomato.customer.data.mock.MockDataProvider
import com.vigizoomato.customer.data.models.CartItem
import com.vigizoomato.customer.data.models.Coupon
import com.vigizoomato.customer.data.models.MenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

data class RestaurantCartGroup(
    val restaurantId: String,
    val restaurantName: String,
    val minOrderValue: Double,
    val items: List<CartItem>,
    val subTotal: Double,
    val deliveryFee: Double,
    val discount: Double,
    val isMinOrderMet: Boolean,
    val amountNeededForMinOrder: Double
)

data class CartSummary(
    val groups: List<RestaurantCartGroup>,
    val totalItemCount: Int,
    val totalItemsPrice: Double,
    val totalDeliveryFee: Double,
    val taxesAndPackaging: Double,
    val totalDiscount: Double,
    val grandTotal: Double,
    val appliedCoupons: List<Coupon>,
    val allMinOrdersMet: Boolean
)

class CartRepository {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _appliedCoupons = MutableStateFlow<List<Coupon>>(emptyList())
    val appliedCoupons: StateFlow<List<Coupon>> = _appliedCoupons.asStateFlow()

    val availableCoupons: List<Coupon> = MockDataProvider.sampleCoupons

    fun addToCart(menuItem: MenuItem, restaurantName: String, restaurantMinOrder: Double) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.menuItem.id == menuItem.id }
        if (index >= 0) {
            val existing = currentList[index]
            currentList[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            currentList.add(
                CartItem(
                    menuItem = menuItem,
                    restaurantId = menuItem.restaurantId,
                    restaurantName = restaurantName,
                    restaurantMinOrder = restaurantMinOrder,
                    quantity = 1
                )
            )
        }
        _cartItems.value = currentList
    }

    fun removeFromCart(menuItemId: String) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.menuItem.id == menuItemId }
        if (index >= 0) {
            val existing = currentList[index]
            if (existing.quantity > 1) {
                currentList[index] = existing.copy(quantity = existing.quantity - 1)
            } else {
                currentList.removeAt(index)
            }
        }
        _cartItems.value = currentList
    }

    fun removeEntireItem(menuItemId: String) {
        _cartItems.value = _cartItems.value.filter { it.menuItem.id != menuItemId }
    }

    fun getItemQuantity(menuItemId: String): Int {
        return _cartItems.value.find { it.menuItem.id == menuItemId }?.quantity ?: 0
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _appliedCoupons.value = emptyList()
    }

    fun applyCoupon(coupon: Coupon): Boolean {
        val groups = getCartSummary().groups
        // Check if coupon is valid for any restaurant currently in cart or platform-wide
        val isValid = coupon.restaurantId == null || groups.any { it.restaurantId == coupon.restaurantId }
        if (isValid) {
            if (_appliedCoupons.value.none { it.code == coupon.code }) {
                _appliedCoupons.value = _appliedCoupons.value + coupon
                return true
            }
        }
        return false
    }

    fun removeCoupon(couponCode: String) {
        _appliedCoupons.value = _appliedCoupons.value.filter { it.code != couponCode }
    }

    fun getCartSummary(): CartSummary {
        val items = _cartItems.value
        val coupons = _appliedCoupons.value

        val grouped = items.groupBy { it.restaurantId }
        val groups = grouped.map { (restaurantId, itemsForRest) ->
            val restaurantName = itemsForRest.firstOrNull()?.restaurantName ?: "Restaurant"
            val minOrder = itemsForRest.firstOrNull()?.restaurantMinOrder ?: 0.0
            val subTotal = itemsForRest.sumOf { it.totalPrice }
            val deliveryFee = 35.0 // Delivery fee per restaurant

            // Calculate coupon discount for this restaurant
            val restCoupon = coupons.find { it.restaurantId == restaurantId }
            val discount = restCoupon?.calculateDiscount(subTotal) ?: 0.0

            val isMinMet = subTotal >= minOrder
            val needed = if (isMinMet) 0.0 else minOrder - subTotal

            RestaurantCartGroup(
                restaurantId = restaurantId,
                restaurantName = restaurantName,
                minOrderValue = minOrder,
                items = itemsForRest,
                subTotal = subTotal,
                deliveryFee = deliveryFee,
                discount = discount,
                isMinOrderMet = isMinMet,
                amountNeededForMinOrder = needed
            )
        }

        val totalItemsPrice = groups.sumOf { it.subTotal }
        val totalDeliveryFee = groups.sumOf { it.deliveryFee }
        val restDiscounts = groups.sumOf { it.discount }

        // Platform-wide coupon discount
        val platformCoupon = coupons.find { it.restaurantId == null }
        val platformDiscount = platformCoupon?.calculateDiscount(totalItemsPrice) ?: 0.0

        val totalDiscount = restDiscounts + platformDiscount
        val taxesAndPackaging = if (totalItemsPrice > 0) totalItemsPrice * 0.05 + (groups.size * 15.0) else 0.0
        val grandTotal = maxOf(0.0, totalItemsPrice + totalDeliveryFee + taxesAndPackaging - totalDiscount)
        val allMinOrdersMet = groups.all { it.isMinOrderMet }

        return CartSummary(
            groups = groups,
            totalItemCount = items.sumOf { it.quantity },
            totalItemsPrice = totalItemsPrice,
            totalDeliveryFee = totalDeliveryFee,
            taxesAndPackaging = taxesAndPackaging,
            totalDiscount = totalDiscount,
            grandTotal = grandTotal,
            appliedCoupons = coupons,
            allMinOrdersMet = allMinOrdersMet
        )
    }
}
