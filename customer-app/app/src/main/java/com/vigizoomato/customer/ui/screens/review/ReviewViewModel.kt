package com.vigizoomato.customer.ui.screens.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vigizoomato.customer.VigiZoomatoApp
import com.vigizoomato.customer.data.models.Order
import com.vigizoomato.customer.data.models.SubOrder
import com.vigizoomato.customer.data.repository.AuthRepository
import com.vigizoomato.customer.data.repository.OrderRepository
import com.vigizoomato.customer.data.repository.RestaurantRepository
import kotlinx.coroutines.flow.*

data class ReviewFormState(
    val rating: Double = 5.0,
    val comment: String = "",
    val selectedTags: Set<String> = emptySet(),
    val isSubmitted: Boolean = false
)

data class ReviewUiState(
    val orderId: String = "",
    val subOrderId: String = "",
    val subOrder: SubOrder? = null,
    val rating: Double = 5.0,
    val comment: String = "",
    val selectedTags: Set<String> = emptySet(),
    val isSubmitted: Boolean = false
)

class ReviewViewModel(
    private val orderRepository: OrderRepository = VigiZoomatoApp.container.orderRepository,
    private val restaurantRepository: RestaurantRepository = VigiZoomatoApp.container.restaurantRepository,
    private val authRepository: AuthRepository = VigiZoomatoApp.container.authRepository
) : ViewModel() {

    private val _orderId = MutableStateFlow("")
    private val _subOrderId = MutableStateFlow("")
    private val _rating = MutableStateFlow(5.0)
    private val _comment = MutableStateFlow("")
    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    private val _isSubmitted = MutableStateFlow(false)

    val complimentTags = listOf("Great Taste 😋", "Super Hot Food 🔥", "Fast Delivery ⚡", "Neat Packaging 📦", "Generous Portion 🍲")

    private val reviewFormFlow = combine(
        _rating,
        _comment,
        _selectedTags,
        _isSubmitted
    ) { rating, comment, tags, submitted ->
        ReviewFormState(rating, comment, tags, submitted)
    }

    val uiState: StateFlow<ReviewUiState> = combine(
        _orderId,
        _subOrderId,
        reviewFormFlow,
        orderRepository.orders
    ) { oId, sId, form, orders ->
        val order = orders.find { it.id == oId }
        val sub = order?.subOrders?.find { it.subOrderId == sId }

        ReviewUiState(
            orderId = oId,
            subOrderId = sId,
            subOrder = sub,
            rating = form.rating,
            comment = form.comment,
            selectedTags = form.selectedTags,
            isSubmitted = form.isSubmitted
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReviewUiState()
    )

    fun initialize(orderId: String, subOrderId: String) {
        _orderId.value = orderId
        _subOrderId.value = subOrderId
    }

    fun onRatingChanged(newRating: Double) {
        _rating.value = newRating
    }

    fun onCommentChanged(text: String) {
        _comment.value = text
    }

    fun toggleTag(tag: String) {
        val current = _selectedTags.value
        _selectedTags.value = if (current.contains(tag)) current - tag else current + tag
    }

    fun submitReview(onSuccess: () -> Unit) {
        val sub = uiState.value.subOrder ?: return
        val userName = authRepository.currentUser.value?.name ?: "Valued Customer"
        val dishNames = sub.items.map { it.menuItem.name }

        // Posted to the server so the new average shows up in the customer
        // feed, the restaurant's own analytics and the admin directory.
        restaurantRepository.submitReview(
            restaurantId = sub.restaurantId,
            subOrderId = _subOrderId.value,
            customerName = userName,
            rating = _rating.value,
            comment = _comment.value,
            orderedDishes = dishNames
        ) { _, _ -> }

        orderRepository.rateSubOrder(_orderId.value, _subOrderId.value, _rating.value)
        _isSubmitted.value = true
        onSuccess()
    }
}
