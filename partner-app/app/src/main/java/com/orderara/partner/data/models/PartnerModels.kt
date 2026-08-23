package com.orderara.partner.data.models

import kotlinx.serialization.Serializable

@Serializable
enum class StaffRole(val title: String, val badgeColorHex: Long) {
    OWNER("Owner (Full Access)", 0xFFFF521B),
    MANAGER("Store Manager", 0xFF10B981),
    KITCHEN_STAFF("Kitchen Staff (KDS)", 0xFF3B82F6)
}

@Serializable
enum class PartnerOrderStatus(val label: String, val stepIndex: Int) {
    PLACED("New Order", 0),
    ACCEPTED("Accepted", 1),
    PREPARING("Preparing", 2),
    OUT_FOR_DELIVERY("Out for Delivery", 3),
    DELIVERED("Delivered", 4),
    REJECTED("Rejected", -1)
}

@Serializable
data class PartnerMenuItem(
    val id: String,
    val restaurantId: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val imageUrl: String = "",
    val isVeg: Boolean = true,
    val isAvailable: Boolean = true,
    val isBestSeller: Boolean = false,
    val preparationTimeMinutes: Int = 20
)

@Serializable
data class OrderItemRecord(
    val menuItem: PartnerMenuItem,
    val quantity: Int = 1,
    val specialNotes: String = ""
) {
    val totalPrice: Double
        get() = menuItem.price * quantity
}

@Serializable
data class IncomingSubOrder(
    val subOrderId: String,
    val parentOrderId: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val items: List<OrderItemRecord>,
    val subTotal: Double,
    val deliveryFee: Double,
    val discount: Double = 0.0,
    val status: PartnerOrderStatus = PartnerOrderStatus.PLACED,
    val orderTime: String = "18:30",
    val estimatedPrepMinutes: Int = 25,
    val assignedRiderName: String = "Sunil (Staff Rider)",
    val specialInstructions: String = "",
    val paymentStatus: String = "PAID (Online UPI)"
) {
    val netEarnings: Double
        get() = subTotal + deliveryFee - discount
}

@Serializable
data class RestaurantProfile(
    val id: String = "rest_1",
    val name: String = "Royal Biryani House",
    val description: String = "Authentic Dum Biryani & Charcoal Kebabs",
    val phone: String = "+91 98450 11223",
    val email: String = "owner@royalbiryani.com",
    val address: String = "Indiranagar 100ft Road, Bangalore",
    val deliveryRadiusKm: Double = 7.0,
    val minOrderValue: Double = 199.0,
    val isOpen: Boolean = true,
    val rating: Double = 4.6,
    val totalOrdersServed: Int = 1420,
    val bankAccount: String = "HDFC Bank •••• 4920"
)

@Serializable
data class SubscriptionInfo(
    val planName: String = "OrderAra Unlimited Partner Plan",
    val priceMonthly: Double = 999.0,
    val isTrialActive: Boolean = true,
    val trialDaysRemaining: Int = 12,
    val trialTotalDays: Int = 14,
    val nextBillingDate: String = "05 Sept 2026",
    val status: String = "ACTIVE_TRIAL",
    val invoices: List<InvoiceRecord> = listOf(
        InvoiceRecord("INV-2026-001", "14-Day Free Trial", "₹0.00", "ACTIVE", "22 Aug 2026")
    )
)

@Serializable
data class InvoiceRecord(
    val id: String,
    val title: String,
    val amount: String,
    val status: String,
    val date: String
)

@Serializable
data class StaffMember(
    val id: String,
    val name: String,
    val phone: String,
    val role: StaffRole
)

@Serializable
data class PartnerChatMessage(
    val id: String,
    val subOrderId: String,
    val senderName: String,
    val isFromCustomer: Boolean,
    val text: String,
    val timestamp: String
)

data class PartnerDailyAnalytics(
    val todayRevenue: Double = 4850.0,
    val todayOrdersCount: Int = 14,
    val avgPrepTimeMinutes: Int = 21,
    val topSellingItems: List<Pair<String, Int>> = listOf(
        "Hyderabadi Chicken Dum Biryani" to 28,
        "Murgh Tangdi Kebab (4 Pcs)" to 19,
        "Royal Paneer Tikka Biryani" to 14,
        "Butter Naan & Dal Makhani Combo" to 11
    ),
    val weeklyRevenueTrend: List<Pair<String, Double>> = listOf(
        "Mon" to 3200.0,
        "Tue" to 3900.0,
        "Wed" to 4100.0,
        "Thu" to 3600.0,
        "Fri" to 5800.0,
        "Sat" to 7200.0,
        "Sun" to 6900.0
    )
)
