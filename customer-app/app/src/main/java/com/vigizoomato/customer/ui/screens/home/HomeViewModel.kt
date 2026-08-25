package com.vigizoomato.customer.ui.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector
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

/** One tile in the "What's on your mind?" rail. */
data class Cuisine(
    val name: String,
    val photoUrl: String? = null,
    val icon: ImageVector? = null
)

/**
 * Tiles render at 62dp; on a 3x screen that is ~190px, so 200px wide is the
 * smallest size that still looks crisp. Requesting a bigger crop would just
 * burn the customer's mobile data.
 */
private fun dishPhoto(id: String) =
    "https://images.unsplash.com/photo-$id?w=200&h=200&auto=format&fit=crop&q=80"

class HomeViewModel(
    private val restaurantRepository: RestaurantRepository = VigiZoomatoApp.container.restaurantRepository,
    private val cartRepository: CartRepository = VigiZoomatoApp.container.cartRepository,
    private val authRepository: AuthRepository = VigiZoomatoApp.container.authRepository
) : ViewModel() {

    private val _selectedCuisine = MutableStateFlow<String?>(null)
    private val _isVegOnly = MutableStateFlow(false)
    private val _sortBy = MutableStateFlow("Popular")

    // Dish photos, not emoji and not monochrome glyphs. A cuisine rail is asking
    // "what do you feel like eating" -- appetite appeal is the job, and a photo of
    // the actual dish does that far better than an abstract icon.
    // "All" deliberately keeps an icon: it is not a cuisine, it clears the filter,
    // so it should not look like one more dish to choose from.
    val cuisines: List<Cuisine> = listOf(
        Cuisine("All", icon = Icons.Filled.Restaurant),
        Cuisine("Biryani", photoUrl = dishPhoto("1563379091339-03b21ab4a4f8")),
        Cuisine("Pizza", photoUrl = dishPhoto("1574071318508-1cdbab80d002")),
        Cuisine("South Indian", photoUrl = dishPhoto("1589301760014-d929f3979dbc")),
        Cuisine("Burgers", photoUrl = dishPhoto("1568901346375-23c9450c58cd")),
        Cuisine("Chinese", photoUrl = dishPhoto("1585032226651-759b368d7246")),
        Cuisine("Desserts", photoUrl = dishPhoto("1551024506-0bccd828d307"))
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
