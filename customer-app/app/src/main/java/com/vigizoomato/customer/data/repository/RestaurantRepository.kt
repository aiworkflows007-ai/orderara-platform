package com.vigizoomato.customer.data.repository

import com.vigizoomato.customer.data.models.MenuItem
import com.vigizoomato.customer.data.models.Restaurant
import com.vigizoomato.customer.data.models.Review
import com.vigizoomato.customer.data.network.ApiClient
import com.vigizoomato.customer.data.network.ApiConfig
import com.vigizoomato.customer.data.network.RealtimeClient
import com.vigizoomato.customer.data.network.objects
import com.vigizoomato.customer.data.network.toStringList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * The restaurant feed and menus, straight from the backend.
 *
 * Everything a restaurant owner does in the Partner app — going live, adding a
 * dish, marking something out of stock, closing the store — shows up here.
 * Restaurants whose subscription is suspended are simply absent from the feed.
 */
class RestaurantRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _restaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants.asStateFlow()

    private val _menuItems = MutableStateFlow<Map<String, List<MenuItem>>>(emptyMap())
    val menuItems: StateFlow<Map<String, List<MenuItem>>> = _menuItems.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError: StateFlow<String?> = _connectionError.asStateFlow()

    /** Favourites are a per-phone preference, not server data. */
    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    init {
        // A stock toggle in the Partner app reaches the shelf immediately.
        RealtimeClient.on("menu:stock") { payload ->
            val restaurantId = payload.optString("restaurantId")
            val itemId = payload.optString("itemId")
            val available = payload.optBoolean("isAvailable", true)
            _menuItems.value = _menuItems.value.mapValues { (key, items) ->
                if (key != restaurantId) items
                else items.map { if (it.id == itemId) it.copy(isAvailable = available) else it }
            }
        }
        RealtimeClient.on("menu:updated") { payload ->
            refreshMenu(payload.optString("restaurantId"))
        }
        RealtimeClient.on("restaurants:updated") { refreshRestaurants() }
        RealtimeClient.on("restaurant:settings") { refreshRestaurants() }

        startLiveSync()
    }

    private fun startLiveSync() {
        scope.launch {
            while (isActive) {
                fetchRestaurants()
                delay(ApiConfig.POLL_INTERVAL_MS)
            }
        }
    }

    fun refreshRestaurants() {
        scope.launch { fetchRestaurants() }
    }

    private fun fetchRestaurants() {
        val res = ApiClient.get("/api/restaurants")
        if (res.isSuccess) {
            val favorites = _favoriteIds.value
            _restaurants.value = res.dataArray.objects().map { it.toRestaurant(favorites) }
            _connectionError.value = null
        } else if (res.code == -1) {
            _connectionError.value = "Can't reach OrderAra right now"
        }
        _isLoading.value = false
    }

    /**
     * Loads one restaurant's real menu. Called when its page opens, so the
     * customer always sees exactly what the restaurant published.
     */
    fun refreshMenu(restaurantId: String) {
        if (restaurantId.isBlank()) return
        scope.launch {
            val res = ApiClient.get("/api/restaurants/$restaurantId")
            val data = res.data ?: return@launch
            if (!res.isSuccess) return@launch

            data.optJSONObject("restaurant")?.let { obj ->
                val updated = obj.toRestaurant(_favoriteIds.value)
                _restaurants.value = _restaurants.value.map { if (it.id == updated.id) updated else it }
            }
            _menuItems.value = _menuItems.value + (restaurantId to data.optJSONArray("menuItems").objects().map { it.toMenuItem() })

            val remoteReviews = data.optJSONArray("reviews").objects().map { it.toReview() }
            _reviews.value = _reviews.value.filterNot { it.restaurantId == restaurantId } + remoteReviews
        }
    }

    fun getRestaurantById(id: String): Restaurant? = _restaurants.value.find { it.id == id }

    fun getMenuItemsForRestaurant(restaurantId: String): List<MenuItem> =
        _menuItems.value[restaurantId] ?: emptyList()

    fun toggleFavorite(restaurantId: String) {
        val current = _favoriteIds.value
        _favoriteIds.value = if (restaurantId in current) current - restaurantId else current + restaurantId
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

    fun getReviewsForRestaurant(restaurantId: String): List<Review> =
        _reviews.value.filter { it.restaurantId == restaurantId }

    /**
     * Posts a rating. The new average is what every other app then shows —
     * the customer feed, the restaurant's own analytics and the admin directory.
     */
    fun submitReview(
        restaurantId: String,
        subOrderId: String,
        customerName: String,
        rating: Double,
        comment: String,
        orderedDishes: List<String>,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        scope.launch {
            val res = ApiClient.post("/api/reviews", JSONObject().apply {
                put("restaurantId", restaurantId)
                put("subOrderId", subOrderId)
                put("customerId", ApiConfig.CUSTOMER_ID)
                put("customerName", customerName)
                put("rating", rating)
                put("comment", comment)
                put("orderedDishes", org.json.JSONArray(orderedDishes))
            })
            if (res.isSuccess) {
                res.data?.toReview()?.let { _reviews.value = listOf(it) + _reviews.value }
                res.body?.optJSONObject("restaurant")?.let { obj ->
                    val updated = obj.toRestaurant(_favoriteIds.value)
                    _restaurants.value = _restaurants.value.map { if (it.id == updated.id) updated else it }
                }
            }
            onResult(res.isSuccess, res.message.takeIf { !res.isSuccess })
        }
    }
}

internal fun JSONObject.toRestaurant(favorites: Set<String> = emptySet()): Restaurant {
    val id = optString("id")
    return Restaurant(
        id = id,
        name = optString("name", "Restaurant"),
        description = optString("description", ""),
        cuisineTypes = optJSONArray("cuisineTypes").toStringList().ifEmpty { listOf("Multi-Cuisine") },
        rating = optDouble("rating", 0.0),
        ratingCount = optInt("totalRatings", 0),
        deliveryTimeMinutes = optInt("deliveryTimeMinutes", 25),
        deliveryRadiusKm = optDouble("deliveryRadiusKm", 7.0),
        distanceKm = optDouble("distanceKm", 2.0),
        minOrderValue = optDouble("minOrderValue", 199.0),
        bannerUrl = optString("bannerUrl"),
        isVegOnly = optBoolean("isVegOnly", false),
        isPromoted = optBoolean("isPromoted", false),
        discountOffer = optString("discountOffer").takeIf { it.isNotBlank() },
        phoneNumber = optString("phone"),
        address = optString("address"),
        isOpen = optBoolean("isOpen", true),
        isFavorite = id in favorites
    )
}

internal fun JSONObject.toMenuItem(): MenuItem = MenuItem(
    id = optString("id"),
    restaurantId = optString("restaurantId"),
    name = optString("name"),
    description = optString("description"),
    price = optDouble("price", 0.0),
    category = optString("category", "Specials"),
    imageUrl = optString("imageUrl"),
    isVeg = optBoolean("isVeg", true),
    isAvailable = optBoolean("isAvailable", true),
    isBestSeller = optBoolean("isBestSeller", false),
    spicyLevel = optInt("spicyLevel", 1),
    preparationTimeMinutes = optInt("preparationTimeMinutes", 20)
)

internal fun JSONObject.toReview(): Review = Review(
    id = optString("id"),
    restaurantId = optString("restaurantId"),
    customerName = optString("customerName", "Customer"),
    rating = optDouble("rating", 5.0),
    comment = optString("comment"),
    date = optString("createdAt").take(10),
    orderedDishes = optJSONArray("orderedDishes").toStringList()
)
