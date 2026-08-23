package com.vigizoomato.customer.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vigizoomato.customer.VigiZoomatoApp
import com.vigizoomato.customer.data.models.Address
import com.vigizoomato.customer.data.models.Coupon
import com.vigizoomato.customer.data.models.Restaurant
import com.vigizoomato.customer.data.repository.AuthRepository
import com.vigizoomato.customer.data.repository.CartRepository
import com.vigizoomato.customer.data.repository.CartSummary
import com.vigizoomato.customer.data.repository.RestaurantRepository
import kotlinx.coroutines.flow.*

data class HomeFilters(
    val selectedCuisine: String? = null,
    val isVegOnly: Boolean = false,
    val sortBy: String = "Popular"
)

data class HomeUiState(
    val currentAddress: Address? = null,
    val selectedCuisine: String? = null,
    val isVegOnly: Boolean = false,
    val sortBy: String = "Popular",
    val coupons: List<Coupon> = emptyList(),
    val filteredRestaurants: List<Restaurant> = emptyList(),
    val cartSummary: CartSummary
)

class HomeViewModel(
    private val restaurantRepository: RestaurantRepository = VigiZoomatoApp.container.restaurantRepository,
    private val cartRepository: CartRepository = VigiZoomatoApp.container.cartRepository,
    private val authRepository: AuthRepository = VigiZoomatoApp.container.authRepository
) : ViewModel() {

    private val _selectedCuisine = MutableStateFlow<String?>(null)
    private val _isVegOnly = MutableStateFlow(false)
    private val _sortBy = MutableStateFlow("Popular")

    val cuisines = listOf(
        "All" to "🍽️",
        "Biryani" to "🍗",
        "Pizza" to "🍕",
        "South Indian" to "🥞",
        "Burgers" to "🍔",
        "Chinese" to "🥢",
        "Desserts" to "🍰"
    )

    private val filtersFlow = combine(_selectedCuisine, _isVegOnly, _sortBy) { c, v, s ->
        HomeFilters(c, v, s)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        restaurantRepository.restaurants,
        cartRepository.cartItems,
        authRepository.currentUser,
        filtersFlow
    ) { rests, _, user, filters ->
        val currentAddr = user?.savedAddresses?.find { it.id == user.selectedAddressId }
            ?: user?.savedAddresses?.firstOrNull()

        var filtered = rests.filter { rest ->
            val matchCuisine = filters.selectedCuisine == null || filters.selectedCuisine == "All" ||
                    rest.cuisineTypes.any { it.equals(filters.selectedCuisine, ignoreCase = true) }
            val matchVeg = !filters.isVegOnly || rest.isVegOnly
            matchCuisine && matchVeg
        }

        filtered = when (filters.sortBy) {
            "Rating" -> filtered.sortedByDescending { it.rating }
            "DeliveryTime" -> filtered.sortedBy { it.deliveryTimeMinutes }
            "MinOrder" -> filtered.sortedBy { it.minOrderValue }
            else -> filtered
        }

        HomeUiState(
            currentAddress = currentAddr,
            selectedCuisine = filters.selectedCuisine,
            isVegOnly = filters.isVegOnly,
            sortBy = filters.sortBy,
            coupons = cartRepository.availableCoupons,
            filteredRestaurants = filtered,
            cartSummary = cartRepository.getCartSummary()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(cartSummary = cartRepository.getCartSummary())
    )

    fun selectCuisine(cuisine: String?) {
        _selectedCuisine.value = if (_selectedCuisine.value == cuisine) null else cuisine
    }

    fun toggleVegOnly() {
        _isVegOnly.value = !_isVegOnly.value
    }

    fun setSortBy(sort: String) {
        _sortBy.value = sort
    }

    fun toggleFavorite(restaurantId: String) {
        restaurantRepository.toggleFavorite(restaurantId)
    }

    fun applyCouponToCart(coupon: Coupon) {
        cartRepository.applyCoupon(coupon)
    }
}
