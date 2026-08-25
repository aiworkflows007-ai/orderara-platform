package com.orderara.partner.data.mock

import com.orderara.partner.data.models.RestaurantProfile
import com.orderara.partner.data.models.StaffMember
import com.orderara.partner.data.models.StaffRole

/**
 * Placeholders shown only before this phone has registered a restaurant.
 *
 * Menu, orders, chat, analytics and subscription all come from the backend now
 * — a partner's own data is never invented locally.
 */
object PartnerMockData {

    val initialProfile = RestaurantProfile(
        id = "",
        name = "Your Restaurant",
        description = "Complete registration to start receiving orders",
        isOpen = false
    )

    val initialStaffList = listOf(
        StaffMember("st_owner", "Owner", "", StaffRole.OWNER)
    )

    /** Cuisine choices offered during onboarding. */
    val cuisineOptions = listOf(
        "Biryani", "North Indian", "South Indian", "Chinese", "Pizza",
        "Burgers", "Rolls", "Desserts", "Beverages", "Street Food",
        "Bakery", "Seafood", "Thali", "Pure Veg"
    )
}
