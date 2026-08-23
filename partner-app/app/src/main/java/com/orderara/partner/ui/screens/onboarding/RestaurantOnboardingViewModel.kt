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
    // Step 3: Bank Payouts
    val accountHolder: String = "",
    val bankName: String = "HDFC Bank",
    val accountNumber: String = "",
    val ifscCode: String = "",
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

    fun updateAccountHolder(holder: String) = _uiState.update { it.copy(accountHolder = holder) }
    fun updateBankName(bank: String) = _uiState.update { it.copy(bankName = bank) }
    fun updateAccountNumber(num: String) = _uiState.update { it.copy(accountNumber = num) }
    fun updateIfsc(ifsc: String) = _uiState.update { it.copy(ifscCode = ifsc) }

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
                if (s.accountNumber.isBlank() || s.accountHolder.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Please enter bank account details for daily payouts") }
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

    fun submitRegistration(onSuccess: () -> Unit) {
        val s = _uiState.value
        _uiState.update { it.copy(isSubmitting = true) }

        val bankSummary = "${s.bankName} (•••• ${s.accountNumber.takeLast(4).ifEmpty { "1234" }})"

        authRepo.registerNewRestaurant(
            name = s.restaurantName.ifEmpty { "My Gourmet Restaurant" },
            ownerName = s.ownerName.ifEmpty { "Partner Owner" },
            phone = s.phone.ifEmpty { "+91 98450 11223" },
            email = s.email.ifEmpty { "owner@restaurant.com" },
            address = "${s.address.ifEmpty { "100ft Road, Indiranagar" }}, ${s.city}",
            cuisines = s.selectedCuisines.toList().ifEmpty { listOf("Multi-Cuisine") },
            deliveryRadiusKm = s.deliveryRadiusKm,
            minOrderValue = s.minOrderValue,
            bankDetails = bankSummary
        )

        onSuccess()
    }
}
