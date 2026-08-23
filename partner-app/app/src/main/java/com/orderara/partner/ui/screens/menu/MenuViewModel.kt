package com.orderara.partner.ui.screens.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orderara.partner.OrderAraPartnerApp
import com.orderara.partner.data.models.PartnerMenuItem
import kotlinx.coroutines.flow.*

data class MenuUiState(
    val menuItems: List<PartnerMenuItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = ""
)

class MenuViewModel : ViewModel() {
    private val menuRepo = OrderAraPartnerApp.instance.menuRepository

    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<MenuUiState> = combine(
        menuRepo.menuItems,
        _selectedCategory,
        _searchQuery
    ) { items, cat, query ->
        val categories = items.map { it.category }.distinct()
        val filtered = items.filter { item ->
            val matchCategory = cat == null || item.category == cat
            val matchQuery = query.isBlank() || item.name.contains(query, ignoreCase = true) || item.category.contains(query, ignoreCase = true)
            matchCategory && matchQuery
        }
        MenuUiState(
            menuItems = filtered,
            categories = categories,
            selectedCategory = cat,
            searchQuery = query
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MenuUiState())

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleStockAvailability(itemId: String) {
        menuRepo.toggleItemAvailability(itemId)
    }

    fun addDish(name: String, category: String, price: Double, description: String, isVeg: Boolean, imageUrl: String) {
        menuRepo.addMenuItem(name, category, price, description, isVeg, imageUrl)
    }

    fun updateDish(item: PartnerMenuItem) {
        menuRepo.updateMenuItem(item)
    }

    fun deleteDish(itemId: String) {
        menuRepo.deleteMenuItem(itemId)
    }
}
