package com.vigizoomato.customer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "user_001",
    val name: String = "Ashok Sharma",
    val phone: String = "+91 98765 43210",
    val email: String = "ashok.sharma@example.com",
    val selectedAddressId: String = "addr_1",
    val savedAddresses: List<Address> = listOf(
        Address(
            id = "addr_1",
            title = "Home",
            street = "Flat 402, Sunshine Heights, 12th Main Road",
            landmark = "Near City Center Mall",
            city = "Bangalore",
            pincode = "560001",
            latitude = 12.9716,
            longitude = 77.5946,
            isDefault = true
        ),
        Address(
            id = "addr_2",
            title = "Work",
            street = "Building 4B, Tech Park, Outer Ring Road",
            landmark = "Opposite Metro Station",
            city = "Bangalore",
            pincode = "560103",
            latitude = 12.9352,
            longitude = 77.6245,
            isDefault = false
        )
    )
)

@Serializable
data class Address(
    val id: String,
    val title: String,
    val street: String,
    val landmark: String = "",
    val city: String,
    val pincode: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isDefault: Boolean = false
) {
    val fullAddress: String
        get() = if (landmark.isNotBlank()) "$street, $landmark, $city - $pincode" else "$street, $city - $pincode"
}
