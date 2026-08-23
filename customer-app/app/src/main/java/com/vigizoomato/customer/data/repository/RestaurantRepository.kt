package com.vigizoomato.customer.data.repository

import android.util.Log
import com.vigizoomato.customer.data.mock.MockDataProvider
import com.vigizoomato.customer.data.models.MenuItem
import com.vigizoomato.customer.data.models.Restaurant
import com.vigizoomato.customer.data.models.Review
import com.vigizoomato.customer.data.network.ApiConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class RestaurantRepository {

    private val _restaurants = MutableStateFlow(MockDataProvider.sampleRestaurants)
    val restaurants: StateFlow<List<Restaurant>> = _restaurants.asStateFlow()

    private val _menuItems = MutableStateFlow(MockDataProvider.sampleMenuItems)
    val menuItems: StateFlow<Map<String, List<MenuItem>>> = _menuItems.asStateFlow()

    private val _reviews = MutableStateFlow(MockDataProvider.sampleReviews)
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    init {
        // Start continuous live synchronization with VPS backend
        startLiveSync()
    }

    fun startLiveSync() {
        CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                fetchLiveRestaurants()
                delay(4000) // Poll every 4 seconds for instant real-time reflection
            }
        }
    }

    fun refreshRestaurants() {
        CoroutineScope(Dispatchers.IO).launch {
            fetchLiveRestaurants()
        }
    }

    private fun fetchLiveRestaurants() {
        try {
            val url = URL("${ApiConfig.BASE_URL}/api/restaurants")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            if (conn.responseCode == 200) {
                val responseText = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val rootJson = JSONObject(responseText)
                if (rootJson.optBoolean("success", false)) {
                    val dataArray = rootJson.getJSONArray("data")
                    val remoteList = mutableListOf<Restaurant>()
                    val remoteMenuMap = _menuItems.value.toMutableMap()

                    for (i in 0 until dataArray.length()) {
                        val obj = dataArray.getJSONObject(i)
                        val id = obj.getString("id")
                        val cuisineList = mutableListOf<String>()
                        val cuisineArr = obj.optJSONArray("cuisineTypes")
                        if (cuisineArr != null) {
                            for (c in 0 until cuisineArr.length()) {
                                cuisineList.add(cuisineArr.getString(c))
                            }
                        } else {
                            cuisineList.add("Multi-Cuisine")
                        }

                        val rest = Restaurant(
                            id = id,
                            name = obj.optString("name", "Restaurant"),
                            description = obj.optString("description", "Authentic Cuisines"),
                            cuisineTypes = cuisineList,
                            rating = obj.optDouble("rating", 4.8),
                            ratingCount = obj.optInt("totalRatings", 100),
                            deliveryTimeMinutes = obj.optInt("deliveryTimeMinutes", 25),
                            deliveryRadiusKm = obj.optDouble("deliveryRadiusKm", 7.0),
                            distanceKm = obj.optDouble("distanceKm", 2.0),
                            minOrderValue = obj.optDouble("minOrderValue", 199.0),
                            bannerUrl = obj.optString("bannerUrl", "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&auto=format&fit=crop&q=80"),
                            isVegOnly = obj.optBoolean("isVegOnly", false),
                            isPromoted = obj.optBoolean("isPromoted", false),
                            discountOffer = obj.optString("discountOffer", "Flat ₹50 OFF"),
                            phoneNumber = obj.optString("phone", "+91 9988776655"),
                            address = obj.optString("address", "Bangalore"),
                            isOpen = obj.optBoolean("isOpen", true)
                        )
                        remoteList.add(rest)

                        // If menu items don't exist for this new restaurant, create starter dishes
                        if (!remoteMenuMap.containsKey(id)) {
                            remoteMenuMap[id] = listOf(
                                MenuItem(
                                    id = "menu_${id}_1",
                                    restaurantId = id,
                                    name = "Signature ${rest.name} Special",
                                    description = "Chef's recommended gourmet special recipe prepared fresh with finest ingredients",
                                    price = 260.0,
                                    category = "Bestsellers",
                                    imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=800&auto=format&fit=crop&q=80",
                                    isVeg = rest.isVegOnly,
                                    isAvailable = true,
                                    isBestSeller = true
                                ),
                                MenuItem(
                                    id = "menu_${id}_2",
                                    restaurantId = id,
                                    name = "Crispy Starter Delight",
                                    description = "Crunchy appetizing platter served with artisanal dips and garnish",
                                    price = 180.0,
                                    category = "Starters",
                                    imageUrl = "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=800&auto=format&fit=crop&q=80",
                                    isVeg = true,
                                    isAvailable = true,
                                    isBestSeller = false
                                )
                            )
                        }
                    }

                    if (remoteList.isNotEmpty()) {
                        _restaurants.value = remoteList
                        _menuItems.value = remoteMenuMap
                    }
                }
            }
            conn.disconnect()
        } catch (e: Exception) {
            Log.e("RestaurantRepo", "Error fetching live restaurants: ${e.message}")
        }
    }

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
