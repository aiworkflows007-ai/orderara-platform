package com.orderara.partner.data.repository

import com.orderara.partner.data.models.PartnerMenuItem
import com.orderara.partner.data.network.ApiClient
import com.orderara.partner.data.network.ApiConfig
import com.orderara.partner.data.network.objects
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * The restaurant's live menu, held on the server so the Customer app sees every
 * change (new dish, price edit, out-of-stock) within seconds.
 */
class PartnerMenuRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var syncJob: Job? = null
    private var restaurantId: String? = null

    private val _menuItems = MutableStateFlow<List<PartnerMenuItem>>(emptyList())
    val menuItems: StateFlow<List<PartnerMenuItem>> = _menuItems.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun start(restaurantId: String) {
        if (this.restaurantId == restaurantId && syncJob?.isActive == true) return
        this.restaurantId = restaurantId
        syncJob?.cancel()
        syncJob = scope.launch {
            while (isActive) {
                fetch(restaurantId)
                delay(ApiConfig.POLL_INTERVAL_MS)
            }
        }
    }

    fun stop() {
        syncJob?.cancel()
        restaurantId = null
        _menuItems.value = emptyList()
    }

    fun refresh() {
        restaurantId?.let { id -> scope.launch { fetch(id) } }
    }

    private fun fetch(restaurantId: String) {
        val res = ApiClient.get("/api/partner/menu/$restaurantId")
        if (res.isSuccess) {
            _menuItems.value = res.dataArray.objects().map { it.toMenuItem() }
            _error.value = null
        }
    }

    fun toggleItemAvailability(itemId: String, onResult: (Boolean) -> Unit = {}) {
        val previous = _menuItems.value
        // Optimistic flip so the switch responds instantly.
        _menuItems.value = previous.map { if (it.id == itemId) it.copy(isAvailable = !it.isAvailable) else it }

        scope.launch {
            val res = ApiClient.patch("/api/partner/menu/$itemId/toggle-stock")
            if (res.isSuccess) {
                res.data?.let { updated -> replaceLocally(updated.toMenuItem()) }
            } else {
                _menuItems.value = previous
                _error.value = res.message
            }
            withContext(Dispatchers.Main) { onResult(res.isSuccess) }
        }
    }

    fun addMenuItem(
        name: String,
        category: String,
        price: Double,
        description: String,
        isVeg: Boolean,
        imageUrl: String = "",
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val id = restaurantId
        if (id == null) {
            onResult(false, "Restaurant not registered yet")
            return
        }
        scope.launch {
            val res = ApiClient.post("/api/partner/menu", JSONObject().apply {
                put("restaurantId", id)
                put("name", name)
                put("category", category)
                put("price", price)
                put("description", description)
                put("isVeg", isVeg)
                if (imageUrl.isNotBlank()) put("imageUrl", imageUrl)
            })
            if (res.isSuccess) {
                res.data?.let { item -> _menuItems.value = _menuItems.value + item.toMenuItem() }
                _error.value = null
            } else {
                _error.value = res.message
            }
            withContext(Dispatchers.Main) { onResult(res.isSuccess, res.message.takeIf { !res.isSuccess }) }
        }
    }

    fun updateMenuItem(updatedItem: PartnerMenuItem, onResult: (Boolean) -> Unit = {}) {
        scope.launch {
            val res = ApiClient.patch("/api/partner/menu/${updatedItem.id}", JSONObject().apply {
                put("name", updatedItem.name)
                put("description", updatedItem.description)
                put("price", updatedItem.price)
                put("category", updatedItem.category)
                put("isVeg", updatedItem.isVeg)
                put("isAvailable", updatedItem.isAvailable)
                put("preparationTimeMinutes", updatedItem.preparationTimeMinutes)
            })
            if (res.isSuccess) {
                res.data?.let { replaceLocally(it.toMenuItem()) }
            } else {
                _error.value = res.message
            }
            withContext(Dispatchers.Main) { onResult(res.isSuccess) }
        }
    }

    fun deleteMenuItem(itemId: String, onResult: (Boolean) -> Unit = {}) {
        val previous = _menuItems.value
        _menuItems.value = previous.filterNot { it.id == itemId }
        scope.launch {
            val res = ApiClient.delete("/api/partner/menu/$itemId")
            if (!res.isSuccess) {
                _menuItems.value = previous
                _error.value = res.message
            }
            withContext(Dispatchers.Main) { onResult(res.isSuccess) }
        }
    }

    fun clearError() { _error.value = null }

    private fun replaceLocally(item: PartnerMenuItem) {
        _menuItems.value = _menuItems.value.map { if (it.id == item.id) item else it }
    }
}

internal fun JSONObject.toMenuItem(): PartnerMenuItem = PartnerMenuItem(
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
    preparationTimeMinutes = optInt("preparationTimeMinutes", 20)
)
