package com.vigizoomato.customer.data.repository

import com.vigizoomato.customer.data.mock.MockDataProvider
import com.vigizoomato.customer.data.models.MenuItem
import com.vigizoomato.customer.data.models.Restaurant
import com.vigizoomato.customer.data.models.Review
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class RestaurantRepository {

    private val _restaurants = MutableStateFlow(MockDataProvider.sampleRestaurants)
    val restaurants: StateFlow<List<Restaurant>> = _restaurants.asStateFlow()

    private val _menuItems = MutableStateFlow(MockDataProvider.sampleMenuItems)
    val menuItems: StateFlow<Map<String, List<MenuItem>>> = _menuItems.asStateFlow()

    private val _reviews = MutableStateFlow(MockDataProvider.sampleReviews)
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    fun getRestaurantById(id: String): Restaurant? {
        return _restaurants.value.find { it.id == id }
    }

    fun getMenuItemsForRestaurant(restaurantId: String): List<MenuItem> {
        return _menuItems.value[restaurantId] ?: emptyList()
    }

    fun toggleFavorite(restaurantId: String) {
        _restaurants.value = _restaurants.value.map {
            if (it.id == restaurantId) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    fun searchRestaurants(query: String, vegOnly: Boolean = false, minRating: Double = 0.0): List<Restaurant> {
        return _restaurants.value.filter { rest ->
            val matchesQuery = query.isBlank() ||
                    rest.name.contains(query, ignoreCase = true) ||
                    rest.cuisineTypes.any { it.contains(query, ignoreCase = true) } ||
                    rest.description.contains(query, ignoreCase = true)

            val matchesVeg = !vegOnly || rest.isVegOnly
            val matchesRating = rest.rating >= minRating

            matchesQuery && matchesVeg && matchesRating
        }
    }

    fun getReviewsForRestaurant(restaurantId: String): List<Review> {
        return _reviews.value.filter { it.restaurantId == restaurantId }
    }

    fun addReview(restaurantId: String, customerName: String, rating: Double, comment: String, orderedDishes: List<String>) {
        val newReview = Review(
            id = "rev_${UUID.randomUUID().toString().take(8)}",
            restaurantId = restaurantId,
            customerName = customerName,
            rating = rating,
            comment = comment,
            date = "Just now",
            orderedDishes = orderedDishes
        )
        _reviews.value = listOf(newReview) + _reviews.value
    }
}
