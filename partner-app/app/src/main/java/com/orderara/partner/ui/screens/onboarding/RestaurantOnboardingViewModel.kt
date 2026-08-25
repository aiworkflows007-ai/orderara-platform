package com.orderara.partner.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import com.orderara.partner.OrderAraPartnerApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class OnboardingFormState(
    val currentStep: Int = 1,
    // Step 1: Details
    val restaurantName: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val email: String = "",
    val selectedCuisines: Set<String> = setOf("Biryani", "North Indian"),
    val isVegOnly: Boolean = false,
    // Step 2: Location
    val address: String = "",
    val city: String = "Bangalore",
    val deliveryRadiusKm: Double = 7.0,
    val minOrderValue: Double = 199.0,
    // Step 3: UPI Payouts
    val upiId: String = "",
    val upiHolderName: String = "",
    // State
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

class RestaurantOnboardingViewModel : ViewModel() {
    private val authRepo = OrderAraPartnerApp.instance.authRepository

    private val _uiState = MutableStateFlow(OnboardingFormState())
    val uiState: StateFlow<OnboardingFormState> = _uiState.asStateFlow()

    fun updateRestaurantName(name: String) = _uiState.update { it.copy(restaurantName = name) }
    fun updateOwnerName(owner: String) = _uiState.update { it.copy(ownerName = owner) }
    fun updatePhone(phone: String) = _uiState.update { it.copy(phone = phone) }
    fun updateEmail(email: String) = _uiState.update { it.copy(email = email) }
    fun toggleCuisine(cuisine: String) {
        _uiState.update { state ->
            val set = state.selectedCuisines.toMutableSet()
            if (set.contains(cuisine)) set.remove(cuisine) else set.add(cuisine)
            state.copy(selectedCuisines = set)
        }
    }
    fun updateVegOnly(veg: Boolean) = _uiState.update { it.copy(isVegOnly = veg) }

    fun updateAddress(address: String) = _uiState.update { it.copy(address = address) }
    fun updateCity(city: String) = _uiState.update { it.copy(city = city) }
    fun updateRadius(radius: Double) = _uiState.update { it.copy(deliveryRadiusKm = radius) }
    fun updateMinOrder(minOrder: Double) = _uiState.update { it.copy(minOrderValue = minOrder) }

    fun updateUpiId(upi: String) = _uiState.update { it.copy(upiId = upi.trim()) }
    fun updateUpiHolderName(name: String) = _uiState.update { it.copy(upiHolderName = name) }
    fun appendUpiHandle(handle: String) {
        _uiState.update { state ->
            val clean = state.upiId.split("@")[0]
            state.copy(upiId = "$clean$handle")
        }
    }

    fun nextStep(): Boolean {
        val s = _uiState.value
        when (s.currentStep) {
            1 -> {
                if (s.restaurantName.isBlank() || s.ownerName.isBlank() || s.phone.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Please fill in restaurant name, owner, and phone") }
                    return false
                }
            }
            2 -> {
                if (s.address.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Please enter store street address") }
                    return false
                }
            }
            3 -> {
                if (s.upiId.isBlank() || !s.upiId.contains("@")) {
                    _uiState.update { it.copy(errorMessage = "Please enter a valid UPI ID (e.g. yourname@okhdfcbank)") }
                    return false
                }
            }
        }
        _uiState.update { it.copy(currentStep = it.currentStep + 1, errorMessage = null) }
        return true
    }

    fun previousStep() {
        if (_uiState.value.currentStep > 1) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1, errorMessage = null) }
        }
    }

    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }

    /**
     * Registers with the backend and only reports success once the server has
     * the restaurant. That is the moment it becomes visible in the Customer app
     * and the Admin panel.
     */
    fun submitRegistration(onSuccess: () -> Unit) {
        val s = _uiState.value
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        val finalUpi = s.upiId.ifEmpty { "${s.phone.replace("+", "").replace(" ", "")}@upi" }

        authRepo.registerNewRestaurant(
            name = s.restaurantName.trim().ifEmpty { "My Restaurant" },
            ownerName = s.ownerName.trim().ifEmpty { "Owner" },
            phone = s.phone.trim(),
            email = s.email.trim(),
            address = "${s.address.trim()}, ${s.city.trim()}",
            cuisines = s.selectedCuisines.toList().ifEmpty { listOf("Multi-Cuisine") },
            deliveryRadiusKm = s.deliveryRadiusKm,
            minOrderValue = s.minOrderValue,
            upiId = finalUpi,
            isVegOnly = s.isVegOnly
        ) { success, message ->
            _uiState.update { it.copy(isSubmitting = false, errorMessage = if (success) null else message) }
            if (success) onSuccess()
        }
    }
}
