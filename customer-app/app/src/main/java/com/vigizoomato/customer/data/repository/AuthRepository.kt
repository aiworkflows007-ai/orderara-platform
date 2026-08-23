package com.vigizoomato.customer.data.repository

import com.vigizoomato.customer.data.mock.MockDataProvider
import com.vigizoomato.customer.data.models.Address
import com.vigizoomato.customer.data.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(MockDataProvider.sampleUser)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun loginWithPhone(phone: String): Boolean {
        // Mock OTP send logic
        return phone.length >= 10
    }

    fun verifyOtp(phone: String, otp: String): Boolean {
        if (otp == "1234" || otp.length == 4) {
            _isLoggedIn.value = true
            if (_currentUser.value == null) {
                _currentUser.value = MockDataProvider.sampleUser.copy(phone = phone)
            }
            return true
        }
        return false
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    fun selectAddress(addressId: String) {
        _currentUser.value = _currentUser.value?.copy(
            selectedAddressId = addressId,
            savedAddresses = _currentUser.value?.savedAddresses?.map {
                it.copy(isDefault = it.id == addressId)
            } ?: emptyList()
        )
    }

    fun addAddress(title: String, street: String, landmark: String, city: String, pincode: String) {
        val newAddress = Address(
            id = "addr_${UUID.randomUUID().toString().take(6)}",
            title = title,
            street = street,
            landmark = landmark,
            city = city,
            pincode = pincode,
            isDefault = _currentUser.value?.savedAddresses.isNullOrEmpty()
        )
        val updatedList = (_currentUser.value?.savedAddresses ?: emptyList()) + newAddress
        _currentUser.value = _currentUser.value?.copy(savedAddresses = updatedList)
    }

    fun updateProfile(name: String, email: String) {
        _currentUser.value = _currentUser.value?.copy(name = name, email = email)
    }
}
