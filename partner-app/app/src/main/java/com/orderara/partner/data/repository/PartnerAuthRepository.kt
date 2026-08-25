package com.orderara.partner.data.repository

import com.orderara.partner.data.mock.PartnerMockData
import com.orderara.partner.data.models.RestaurantProfile
import com.orderara.partner.data.models.StaffMember
import com.orderara.partner.data.models.StaffRole
import com.orderara.partner.data.network.ApiClient
import com.orderara.partner.data.network.ApiConfig
import com.orderara.partner.data.network.PartnerSession
import com.orderara.partner.data.network.RealtimeClient
import com.orderara.partner.data.network.toStringList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Owns this phone's restaurant identity.
 *
 * Everything else in the Partner app (menu, orders, subscription, analytics) is
 * keyed on [restaurantId] — that id is what ties this app to the Customer app
 * and the Admin panel through the backend.
 */
class PartnerAuthRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _restaurantId = MutableStateFlow<String?>(null)
    val restaurantId: StateFlow<String?> = _restaurantId.asStateFlow()

    private val _currentProfile = MutableStateFlow(PartnerMockData.initialProfile)
    val currentProfile: StateFlow<RestaurantProfile> = _currentProfile.asStateFlow()

    private val _currentStaff = MutableStateFlow(PartnerMockData.initialStaffList[0])
    val currentStaff: StateFlow<StaffMember> = _currentStaff.asStateFlow()

    private val _allStaff = MutableStateFlow(PartnerMockData.initialStaffList)
    val allStaff: StateFlow<List<StaffMember>> = _allStaff.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    /** Restores the saved restaurant on app launch and starts live sync. */
    fun bootstrap() {
        val savedId = PartnerSession.restaurantId ?: return
        _restaurantId.value = savedId
        _isLoggedIn.value = true
        if (PartnerSession.ownerName.isNotBlank()) {
            _currentStaff.update { it.copy(name = PartnerSession.ownerName, phone = PartnerSession.ownerPhone) }
            _allStaff.value = listOf(_currentStaff.value)
        }
        RealtimeClient.connect(savedId)
        startProfileSync(savedId)
    }

    private fun startProfileSync(restaurantId: String) {
        scope.launch {
            while (isActive && _restaurantId.value == restaurantId) {
                fetchProfile(restaurantId)
                delay(ApiConfig.POLL_INTERVAL_MS)
            }
        }
    }

    fun refreshProfile() {
        val id = _restaurantId.value ?: return
        scope.launch { fetchProfile(id) }
    }

    private fun fetchProfile(restaurantId: String) {
        val res = ApiClient.get("/api/partner/profile/$restaurantId")
        if (res.isSuccess) {
            res.data?.optJSONObject("restaurant")?.let { _currentProfile.value = it.toProfile() }
            _syncError.value = null
        } else if (res.code == 404) {
            // The restaurant no longer exists on the server (data reset) — force
            // the owner back through onboarding rather than showing stale data.
            _syncError.value = "This restaurant is no longer registered on the server."
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
        upiId: String,
        isVegOnly: Boolean = false,
        onResult: (success: Boolean, message: String?) -> Unit = { _, _ -> }
    ) {
        _isSyncing.value = true
        _syncError.value = null

        scope.launch {
            val payload = JSONObject().apply {
                put("name", name)
                put("ownerName", ownerName)
                put("description", cuisines.joinToString(", "))
                put("phone", phone)
                put("email", email)
                put("address", address)
                put("deliveryRadiusKm", deliveryRadiusKm)
                put("minOrderValue", minOrderValue)
                put("upiId", upiId)
                put("isVegOnly", isVegOnly)
                put("cuisineTypes", JSONArray(cuisines))
            }

            val res = ApiClient.post("/api/partner/register", payload)
            _isSyncing.value = false

            if (res.isSuccess) {
                val profile = res.data!!.toProfile()
                val newId = profile.id

                PartnerSession.restaurantId = newId
                PartnerSession.ownerName = ownerName
                PartnerSession.ownerPhone = phone

                val ownerStaff = StaffMember(
                    id = "staff_${newId.takeLast(6)}",
                    name = ownerName,
                    role = StaffRole.OWNER,
                    phone = phone
                )

                _currentProfile.value = profile
                _currentStaff.value = ownerStaff
                _allStaff.value = listOf(ownerStaff)
                _restaurantId.value = newId
                _isLoggedIn.value = true

                RealtimeClient.connect(newId)
                startProfileSync(newId)

                withContext(Dispatchers.Main) { onResult(true, null) }
            } else {
                val msg = if (res.code == -1) {
                    "Could not reach the server. Check your connection and try again."
                } else {
                    res.message
                }
                _syncError.value = msg
                withContext(Dispatchers.Main) { onResult(false, msg) }
            }
        }
    }

    /** Open/closed toggle — customers see the change within seconds. */
    fun toggleStoreOpen(onResult: (Boolean) -> Unit = {}) {
        val id = _restaurantId.value ?: return
        val next = !_currentProfile.value.isOpen
        _currentProfile.update { it.copy(isOpen = next) } // optimistic
        scope.launch {
            val res = ApiClient.patch(
                "/api/partner/restaurant/$id/settings",
                JSONObject().put("isOpen", next)
            )
            if (res.isSuccess) {
                res.data?.let { _currentProfile.value = it.toProfile() }
            } else {
                _currentProfile.update { it.copy(isOpen = !next) } // roll back
                _syncError.value = res.message
            }
            withContext(Dispatchers.Main) { onResult(res.isSuccess) }
        }
    }

    fun updateStoreSettings(
        radiusKm: Double,
        minOrder: Double,
        onResult: (Boolean) -> Unit = {}
    ) {
        val id = _restaurantId.value ?: return
        scope.launch {
            val res = ApiClient.patch(
                "/api/partner/restaurant/$id/settings",
                JSONObject().apply {
                    put("deliveryRadiusKm", radiusKm)
                    put("minOrderValue", minOrder)
                }
            )
            if (res.isSuccess) {
                res.data?.let { _currentProfile.value = it.toProfile() }
            } else {
                _syncError.value = res.message
            }
            withContext(Dispatchers.Main) { onResult(res.isSuccess) }
        }
    }

    fun updateUpiId(upiId: String, onResult: (Boolean) -> Unit = {}) {
        val id = _restaurantId.value ?: return
        scope.launch {
            val res = ApiClient.patch(
                "/api/partner/restaurant/$id/settings",
                JSONObject().put("upiId", upiId)
            )
            if (res.isSuccess) res.data?.let { _currentProfile.value = it.toProfile() }
            withContext(Dispatchers.Main) { onResult(res.isSuccess) }
        }
    }

    fun switchStaffRole(role: StaffRole) {
        _currentStaff.update { it.copy(role = role) }
    }

    fun clearSyncError() { _syncError.value = null }

    fun logout() {
        _isLoggedIn.value = false
        _restaurantId.value = null
        RealtimeClient.disconnect()
        PartnerSession.clear()
    }

    fun login(emailOrPhone: String) {
        _isLoggedIn.value = true
    }
}

/** Maps the server's restaurant JSON onto the app's profile model. */
internal fun JSONObject.toProfile(): RestaurantProfile = RestaurantProfile(
    id = optString("id"),
    name = optString("name", "My Restaurant"),
    description = optString("description", ""),
    phone = optString("phone", ""),
    email = optString("email", ""),
    address = optString("address", ""),
    deliveryRadiusKm = optDouble("deliveryRadiusKm", 7.0),
    minOrderValue = optDouble("minOrderValue", 199.0),
    isOpen = optBoolean("isOpen", true),
    rating = optDouble("rating", 5.0),
    totalOrdersServed = optInt("totalRatings", 0),
    upiId = optString("upiId", ""),
    bankAccount = optString("upiId", ""),
    cuisineTypes = optJSONArray("cuisineTypes").toStringList()
)
