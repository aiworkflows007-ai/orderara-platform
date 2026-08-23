package com.orderara.partner.data.repository

import android.util.Log
import com.orderara.partner.data.mock.PartnerMockData
import com.orderara.partner.data.models.RestaurantProfile
import com.orderara.partner.data.models.StaffMember
import com.orderara.partner.data.models.StaffRole
import com.orderara.partner.data.network.ApiConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class PartnerAuthRepository {
    private val _currentProfile = MutableStateFlow(PartnerMockData.initialProfile)
    val currentProfile: StateFlow<RestaurantProfile> = _currentProfile.asStateFlow()

    private val _currentStaff = MutableStateFlow(PartnerMockData.initialStaffList[0])
    val currentStaff: StateFlow<StaffMember> = _currentStaff.asStateFlow()

    private val _allStaff = MutableStateFlow(PartnerMockData.initialStaffList)
    val allStaff: StateFlow<List<StaffMember>> = _allStaff.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

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

    fun registerNewRestaurant(
        name: String,
        ownerName: String,
        phone: String,
        email: String,
        address: String,
        cuisines: List<String>,
        deliveryRadiusKm: Double,
        minOrderValue: Double,
        upiId: String
    ) {
        val newId = "rest_${UUID.randomUUID().toString().take(8)}"
        val newProfile = RestaurantProfile(
            id = newId,
            name = name,
            description = cuisines.joinToString(", "),
            phone = phone,
            email = email,
            address = address,
            deliveryRadiusKm = deliveryRadiusKm,
            minOrderValue = minOrderValue,
            isOpen = true,
            rating = 5.0,
            totalOrdersServed = 0,
            upiId = upiId,
            bankAccount = upiId
        )

        val ownerStaff = StaffMember(
            id = "staff_${UUID.randomUUID().toString().take(6)}",
            name = ownerName,
            role = StaffRole.OWNER,
            phone = phone
        )

        _currentProfile.value = newProfile
        _currentStaff.value = ownerStaff
        _allStaff.value = listOf(ownerStaff)
        _isLoggedIn.value = true

        // Sync with Live Backend API & WebSockets
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${ApiConfig.BASE_URL}/api/partner/register")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                conn.doOutput = true

                val jsonPayload = JSONObject().apply {
                    put("id", newId)
                    put("name", name)
                    put("description", cuisines.joinToString(", "))
                    put("phone", phone)
                    put("email", email)
                    put("address", address)
                    put("deliveryRadiusKm", deliveryRadiusKm)
                    put("minOrderValue", minOrderValue)
                    put("upiId", upiId)
                    put("isVegOnly", false)
                    put("cuisineTypes", JSONArray(cuisines))
                }

                OutputStreamWriter(conn.outputStream).use { writer ->
                    writer.write(jsonPayload.toString())
                    writer.flush()
                }

                val responseCode = conn.responseCode
                Log.d("PartnerAuthRepo", "Sync registration response: $responseCode")
                conn.disconnect()
            } catch (e: Exception) {
                Log.e("PartnerAuthRepo", "Failed to sync restaurant with server: ${e.message}")
            }
        }
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    fun login(emailOrPhone: String) {
        _isLoggedIn.value = true
    }
}
