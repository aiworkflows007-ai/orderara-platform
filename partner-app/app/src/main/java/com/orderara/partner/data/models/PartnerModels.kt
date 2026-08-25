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
    REJECTED("Rejected", -1),
    CANCELLED("Cancelled", -1);

    companion object {
        /** Maps the status string the backend sends onto this enum. */
        fun fromApi(value: String?): PartnerOrderStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PLACED
    }
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

/**
 * One line on an incoming order. Kept flat so it maps straight onto the JSON
 * the backend sends — the dish may have been edited or deleted since the
 * customer ordered it, so the order stores its own copy of the details.
 */
@Serializable
data class OrderItemRecord(
    val menuItemId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val isVeg: Boolean = true,
    val imageUrl: String = "",
    val specialNotes: String = ""
) {
    val totalPrice: Double
        get() = price * quantity
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
    val orderTime: String = "",
    val estimatedPrepMinutes: Int = 25,
    val assignedRiderName: String = "",
    val specialInstructions: String = "",
    val paymentStatus: String = "PAID (Online UPI)"
) {
    val netEarnings: Double
        get() = subTotal + deliveryFee - discount
}

@Serializable
data class RestaurantProfile(
    val id: String = "",
    val name: String = "My Restaurant",
    val description: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val deliveryRadiusKm: Double = 7.0,
    val minOrderValue: Double = 199.0,
    val isOpen: Boolean = true,
    val rating: Double = 5.0,
    val totalOrdersServed: Int = 0,
    val upiId: String = "",
    val bankAccount: String = "",
    val cuisineTypes: List<String> = emptyList()
)

@Serializable
data class SubscriptionInfo(
    val restaurantId: String = "",
    val planName: String = "Restaurant Unlimited Partner Plan",
    val priceMonthly: Double = 999.0,
    val isTrialActive: Boolean = true,
    val trialDaysRemaining: Int = 14,
    val trialTotalDays: Int = 14,
    val daysUntilDue: Int = 14,
    /** ACTIVE_TRIAL | ACTIVE_PAID | OVERDUE | SUSPENDED */
    val status: String = "ACTIVE_TRIAL",
    val nextBillingDate: String = "",
    val graceEndsAt: String? = null,
    val suspendedReason: String? = null,
    val invoices: List<InvoiceRecord> = emptyList()
) {
    val isSuspended: Boolean get() = status == "SUSPENDED"
    val isOverdue: Boolean get() = status == "OVERDUE"
    val isPaid: Boolean get() = status == "ACTIVE_PAID"

    /** Plain-language line shown at the top of the subscription screen. */
    val headline: String
        get() = when (status) {
            "ACTIVE_PAID" -> "Subscription active"
            "ACTIVE_TRIAL" -> "Free trial — $trialDaysRemaining days left"
            "OVERDUE" -> "Payment overdue — pay now to stay listed"
            "SUSPENDED" -> "Listing suspended"
            else -> status
        }
}

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
    val todayRevenue: Double = 0.0,
    val todayOrdersCount: Int = 0,
    val lifetimeOrdersCount: Int = 0,
    val lifetimeRevenue: Double = 0.0,
    val avgPrepTimeMinutes: Int = 20,
    val topSellingItems: List<Pair<String, Int>> = emptyList(),
    val weeklyRevenueTrend: List<Pair<String, Double>> = emptyList()
)
