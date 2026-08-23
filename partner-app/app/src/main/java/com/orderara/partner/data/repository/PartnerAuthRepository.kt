package com.orderara.partner.data.repository

import com.orderara.partner.data.mock.PartnerMockData
import com.orderara.partner.data.models.RestaurantProfile
import com.orderara.partner.data.models.StaffMember
import com.orderara.partner.data.models.StaffRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PartnerAuthRepository {
    private val _currentProfile = MutableStateFlow(PartnerMockData.initialProfile)
    val currentProfile: StateFlow<RestaurantProfile> = _currentProfile.asStateFlow()

    private val _currentStaff = MutableStateFlow(PartnerMockData.initialStaffList[0])
    val currentStaff: StateFlow<StaffMember> = _currentStaff.asStateFlow()

    private val _allStaff = MutableStateFlow(PartnerMockData.initialStaffList)
    val allStaff: StateFlow<List<StaffMember>> = _allStaff.asStateFlow()

    fun switchStaffRole(role: StaffRole) {
        _currentStaff.update { current ->
            current.copy(role = role)
        }
    }

    fun toggleStoreOpen() {
        _currentProfile.update { current ->
            current.copy(isOpen = !current.isOpen)
        }
    }

    fun updateStoreSettings(radiusKm: Double, minOrder: Double) {
        _currentProfile.update { current ->
            current.copy(deliveryRadiusKm = radiusKm, minOrderValue = minOrder)
        }
    }
}
