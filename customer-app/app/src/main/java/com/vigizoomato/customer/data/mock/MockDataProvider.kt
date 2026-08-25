package com.vigizoomato.customer.data.mock

import com.vigizoomato.customer.data.models.*

/**
 * The little that is genuinely local to this phone.
 *
 * Restaurants, menus, orders, chat and reviews all come from the backend now —
 * see the repositories in data/repository. Only the signed-in user profile and
 * the promo codes live here.
 */
object MockDataProvider {

    val sampleUser = User(
        id = "user_101",
        name = "Ashok Sharma",
        phone = "+91 98765 43210",
        email = "ashok.sharma@orderara.com",
        selectedAddressId = "addr_1",
        savedAddresses = listOf(
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
                street = "Building 4B, Cyber Park, Outer Ring Road",
                landmark = "Opposite Metro Station",
                city = "Bangalore",
                pincode = "560103",
                latitude = 12.9352,
                longitude = 77.6245,
                isDefault = false
            ),
            Address(
                id = "addr_3",
                title = "Other",
                street = "Plot 88, Green Glen Layout, Bellandur",
                landmark = "Beside Apollo Clinic",
                city = "Bangalore",
                pincode = "560103",
                latitude = 12.9279,
                longitude = 77.6743,
                isDefault = false
            )
        )
    )


    val sampleCoupons = listOf(
        Coupon(
            code = "ROYAL50",
            restaurantId = "rest_1",
            restaurantName = "Royal Biryani House",
            discountPercentage = 50,
            maxDiscount = 100.0,
            minOrderAmount = 199.0,
            description = "50% OFF up to ₹100 on Royal Biryani House"
        ),
        Coupon(
            code = "PIZZA75",
            restaurantId = "rest_2",
            restaurantName = "Pizza Milano & Crust",
            flatDiscount = 75.0,
            minOrderAmount = 299.0,
            description = "Flat ₹75 OFF on orders above ₹299 at Pizza Milano"
        ),
        Coupon(
            code = "UDUPI20",
            restaurantId = "rest_3",
            restaurantName = "Udupi Sri Krishna Sagar",
            discountPercentage = 20,
            maxDiscount = 60.0,
            minOrderAmount = 150.0,
            description = "20% OFF on all South Indian breakfasts"
        ),
        Coupon(
            code = "ARAWELCOME",
            restaurantId = null,
            restaurantName = "All Restaurants",
            flatDiscount = 50.0,
            minOrderAmount = 199.0,
            description = "Welcome Offer: Flat ₹50 OFF across OrderAra"
        )
    )
}
