package com.vigizoomato.customer.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vigizoomato.customer.VigiZoomatoApp
import com.vigizoomato.customer.data.models.Restaurant
import com.vigizoomato.customer.data.repository.CartRepository
import com.vigizoomato.customer.data.repository.CartSummary
import com.vigizoomato.customer.data.repository.RestaurantRepository
import kotlinx.coroutines.flow.*

data class SearchUiState(
    val query: String = "",
    val vegOnly: Boolean = false,
    val minRating: Double = 0.0,
    val searchResults: List<Restaurant> = emptyList(),
    val popularSearches: List<String> = listOf("Biryani", "Pizza", "Dosa", "Burger", "Chinese", "Ice Cream"),
    val cartSummary: CartSummary
)

class SearchViewModel(
    private val restaurantRepository: RestaurantRepository = VigiZoomatoApp.container.restaurantRepository,
    private val cartRepository: CartRepository = VigiZoomatoApp.container.cartRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _vegOnly = MutableStateFlow(false)
    private val _minRating = MutableStateFlow(0.0)

    val uiState: StateFlow<SearchUiState> = combine(
        _query,
        _vegOnly,
        _minRating,
        restaurantRepository.restaurants,
        cartRepository.cartItems
    ) { q, veg, rating, allRests, cart ->
        val results = if (q.isBlank() && !veg && rating == 0.0) {
            allRests
        } else {
            restaurantRepository.searchRestaurants(q, veg, rating)
        }
        SearchUiState(
            query = q,
            vegOnly = veg,
            minRating = rating,
            searchResults = results,
            cartSummary = cartRepository.getCartSummary()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState(cartSummary = cartRepository.getCartSummary())
    )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun toggleVegOnly() {
        _vegOnly.value = !_vegOnly.value
    }

    fun setMinRating(rating: Double) {
        _minRating.value = if (_minRating.value == rating) 0.0 else rating
    }

    fun toggleFavorite(restaurantId: String) {
        restaurantRepository.toggleFavorite(restaurantId)
    }
}
