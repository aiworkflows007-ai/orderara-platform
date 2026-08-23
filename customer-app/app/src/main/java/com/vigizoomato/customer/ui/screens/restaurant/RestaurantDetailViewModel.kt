package com.vigizoomato.customer.ui.screens.restaurant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vigizoomato.customer.VigiZoomatoApp
import com.vigizoomato.customer.data.models.MenuItem
import com.vigizoomato.customer.data.models.Restaurant
import com.vigizoomato.customer.data.models.Review
import com.vigizoomato.customer.data.repository.CartRepository
import com.vigizoomato.customer.data.repository.CartSummary
import com.vigizoomato.customer.data.repository.RestaurantRepository
import kotlinx.coroutines.flow.*

data class MenuFilters(
    val selectedCategory: String? = null,
    val isVegOnly: Boolean = false
)

data class RestaurantDetailUiState(
    val restaurant: Restaurant? = null,
    val menuItems: List<MenuItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val isVegOnlyFilter: Boolean = false,
    val reviews: List<Review> = emptyList(),
    val cartSummary: CartSummary,
    val itemQuantities: Map<String, Int> = emptyMap()
)

class RestaurantDetailViewModel(
    private val restaurantRepository: RestaurantRepository = VigiZoomatoApp.container.restaurantRepository,
    private val cartRepository: CartRepository = VigiZoomatoApp.container.cartRepository
) : ViewModel() {

    private val _restaurantId = MutableStateFlow<String?>(null)
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _isVegOnlyFilter = MutableStateFlow(false)

    private val menuFilterFlow = combine(_selectedCategory, _isVegOnlyFilter) { cat, veg ->
        MenuFilters(cat, veg)
    }

    val uiState: StateFlow<RestaurantDetailUiState> = combine(
        _restaurantId,
        restaurantRepository.restaurants,
        restaurantRepository.menuItems,
        cartRepository.cartItems,
        menuFilterFlow
    ) { restId, allRests, allMenus, cart, filters ->
        val rest = allRests.find { it.id == restId }
        val items = if (restId != null) allMenus[restId] ?: emptyList() else emptyList()
        val cats = items.map { it.category }.distinct()

        val filteredItems = items.filter { item ->
            val matchesCat = filters.selectedCategory == null || item.category == filters.selectedCategory
            val matchesVeg = !filters.isVegOnly || item.isVeg
            matchesCat && matchesVeg
        }

        val quantities = cart.associate { it.menuItem.id to it.quantity }
        val reviews = if (restId != null) restaurantRepository.getReviewsForRestaurant(restId) else emptyList()

        RestaurantDetailUiState(
            restaurant = rest,
            menuItems = filteredItems,
            categories = cats,
            selectedCategory = filters.selectedCategory,
            isVegOnlyFilter = filters.isVegOnly,
            reviews = reviews,
            cartSummary = cartRepository.getCartSummary(),
            itemQuantities = quantities
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RestaurantDetailUiState(cartSummary = cartRepository.getCartSummary())
    )

    fun loadRestaurant(restaurantId: String) {
        _restaurantId.value = restaurantId
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun toggleVegOnly() {
        _isVegOnlyFilter.value = !_isVegOnlyFilter.value
    }

    fun addToCart(item: MenuItem) {
        val rest = uiState.value.restaurant ?: return
        cartRepository.addToCart(item, rest.name, rest.minOrderValue)
    }

    fun removeFromCart(itemId: String) {
        cartRepository.removeFromCart(itemId)
    }

    fun toggleFavorite() {
        val restId = _restaurantId.value ?: return
        restaurantRepository.toggleFavorite(restId)
    }
}
