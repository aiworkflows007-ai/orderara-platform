package com.orderara.partner.data.repository

import com.orderara.partner.data.mock.PartnerMockData
import com.orderara.partner.data.models.PartnerMenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class PartnerMenuRepository {
    private val _menuItems = MutableStateFlow<List<PartnerMenuItem>>(PartnerMockData.initialMenuItems.toList())
    val menuItems: StateFlow<List<PartnerMenuItem>> = _menuItems.asStateFlow()

    fun toggleItemAvailability(itemId: String) {
        _menuItems.update { current ->
            current.map { item ->
                if (item.id == itemId) item.copy(isAvailable = !item.isAvailable) else item
            }
        }
    }

    fun addMenuItem(
        name: String,
        category: String,
        price: Double,
        description: String,
        isVeg: Boolean,
        imageUrl: String = ""
    ) {
        val newItem = PartnerMenuItem(
            id = "menu_${UUID.randomUUID().toString().take(6)}",
            restaurantId = "rest_1",
            name = name,
            category = category,
            price = price,
            description = description,
            isVeg = isVeg,
            imageUrl = if (imageUrl.isBlank()) "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=800&auto=format&fit=crop&q=80" else imageUrl,
            isAvailable = true,
            isBestSeller = false
        )
        _menuItems.update { it + newItem }
    }

    fun updateMenuItem(updatedItem: PartnerMenuItem) {
        _menuItems.update { current ->
            current.map { if (it.id == updatedItem.id) updatedItem else it }
        }
    }

    fun deleteMenuItem(itemId: String) {
        _menuItems.update { current ->
            current.filterNot { it.id == itemId }
        }
    }
}
