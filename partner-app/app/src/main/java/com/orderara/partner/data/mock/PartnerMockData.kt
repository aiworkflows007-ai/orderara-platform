package com.orderara.partner.data.mock

import com.orderara.partner.data.models.*

object PartnerMockData {
    val initialProfile = RestaurantProfile(
        id = "rest_1",
        name = "Royal Biryani House",
        description = "Authentic Hyderabadi Dum Biryani & Kebabs",
        phone = "+91 98450 11223",
        email = "owner@royalbiryani.com",
        address = "Indiranagar 100ft Road, Bangalore",
        deliveryRadiusKm = 7.0,
        minOrderValue = 199.0,
        isOpen = true,
        rating = 4.6,
        totalOrdersServed = 1420,
        bankAccount = "HDFC Bank (•••• 4920)"
    )

    val initialStaffList = listOf(
        StaffMember("st_1", "Farhan Khan (Owner)", "+91 98450 11223", StaffRole.OWNER),
        StaffMember("st_2", "Praveen Kumar (Manager)", "+91 98450 22334", StaffRole.MANAGER),
        StaffMember("st_3", "Chef Imran (Kitchen)", "+91 98450 33445", StaffRole.KITCHEN_STAFF),
        StaffMember("st_4", "Ramesh (Delivery Staff)", "+91 98450 44556", StaffRole.KITCHEN_STAFF)
    )

    val initialMenuItems = mutableListOf(
        PartnerMenuItem(
            id = "menu_1",
            restaurantId = "rest_1",
            name = "Hyderabadi Chicken Dum Biryani",
            description = "Slow cooked fragrant long grain basmati rice layered with spiced chicken, caramelized onions & fresh herbs.",
            price = 320.0,
            category = "Biryani Specials",
            imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=800&auto=format&fit=crop&q=80",
            isVeg = false,
            isAvailable = true,
            isBestSeller = true,
            preparationTimeMinutes = 20
        ),
        PartnerMenuItem(
            id = "menu_2",
            restaurantId = "rest_1",
            name = "Royal Paneer Tikka Biryani",
            description = "Cottage cheese cubes marinated in tandoori spices, layered over aromatic saffron basmati rice.",
            price = 280.0,
            category = "Biryani Specials",
            imageUrl = "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=800&auto=format&fit=crop&q=80",
            isVeg = true,
            isAvailable = true,
            isBestSeller = true,
            preparationTimeMinutes = 18
        ),
        PartnerMenuItem(
            id = "menu_3",
            restaurantId = "rest_1",
            name = "Murgh Tangdi Kebab (4 Pcs)",
            description = "Juicy chicken drumsticks marinated in rich cashew paste and tandoori spices, roasted in clay oven.",
            price = 290.0,
            category = "Starters & Kebabs",
            imageUrl = "https://images.unsplash.com/photo-1599488615731-7e5c2823ff28?w=800&auto=format&fit=crop&q=80",
            isVeg = false,
            isAvailable = true,
            isBestSeller = false,
            preparationTimeMinutes = 15
        ),
        PartnerMenuItem(
            id = "menu_4",
            restaurantId = "rest_1",
            name = "Butter Garlic Naan & Dal Makhani Combo",
            description = "2 fluffy butter garlic naans served with slow simmered creamy black lentil dal makhani.",
            price = 220.0,
            category = "Mains & Breads",
            imageUrl = "https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=800&auto=format&fit=crop&q=80",
            isVeg = true,
            isAvailable = true,
            isBestSeller = false,
            preparationTimeMinutes = 15
        ),
        PartnerMenuItem(
            id = "menu_5",
            restaurantId = "rest_1",
            name = "Shahi Gulab Jamun with Rabri",
            description = "Warm golden khoya dumplings served in fragrant cardamom saffron syrup with chilled rabri.",
            price = 140.0,
            category = "Desserts",
            imageUrl = "https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?w=800&auto=format&fit=crop&q=80",
            isVeg = true,
            isAvailable = true,
            isBestSeller = false,
            preparationTimeMinutes = 5
        )
    )

    val initialOrders = mutableListOf(
        IncomingSubOrder(
            subOrderId = "SUB-01",
            parentOrderId = "VZ-ORD-9842",
            customerName = "Ashok Sharma",
            customerPhone = "+91 98765 43210",
            deliveryAddress = "Flat 402, Sunshine Heights, 12th Main Road, Indiranagar",
            items = listOf(
                OrderItemRecord(initialMenuItems[0], 1, "Please make it spicy!"),
                OrderItemRecord(initialMenuItems[2], 1, "Include extra green mint chutney")
            ),
            subTotal = 610.0,
            deliveryFee = 35.0,
            discount = 100.0,
            status = PartnerOrderStatus.PREPARING,
            orderTime = "18:25",
            estimatedPrepMinutes = 20,
            assignedRiderName = "Ramesh (Staff)",
            specialInstructions = "Please pack extra tissues and lime slices.",
            paymentStatus = "PAID (UPI Google Pay)"
        ),
        IncomingSubOrder(
            subOrderId = "SUB-04",
            parentOrderId = "VZ-ORD-9855",
            customerName = "Pooja Reddy",
            customerPhone = "+91 99887 76655",
            deliveryAddress = "Villa 12, Palm Meadows, Whitefield",
            items = listOf(
                OrderItemRecord(initialMenuItems[1], 2),
                OrderItemRecord(initialMenuItems[4], 2)
            ),
            subTotal = 840.0,
            deliveryFee = 45.0,
            discount = 0.0,
            status = PartnerOrderStatus.PLACED,
            orderTime = "18:44",
            estimatedPrepMinutes = 25,
            assignedRiderName = "Not Assigned",
            specialInstructions = "Please deliver before 7:30 PM.",
            paymentStatus = "PAID (Credit Card)"
        ),
        IncomingSubOrder(
            subOrderId = "SUB-00",
            parentOrderId = "VZ-ORD-9801",
            customerName = "Rohit Verma",
            customerPhone = "+91 91234 56789",
            deliveryAddress = "302 Greenview Apts, Koramangala",
            items = listOf(
                OrderItemRecord(initialMenuItems[0], 2)
            ),
            subTotal = 640.0,
            deliveryFee = 35.0,
            discount = 50.0,
            status = PartnerOrderStatus.DELIVERED,
            orderTime = "17:15",
            estimatedPrepMinutes = 20,
            assignedRiderName = "Sunil (Staff)",
            specialInstructions = "",
            paymentStatus = "PAID (UPI PhonePe)"
        )
    )

    val initialChatMessages = mutableListOf(
        PartnerChatMessage("c_1", "SUB-01", "Ashok Sharma", true, "Hi! Could you please ensure the biryani is freshly hot and spicy?", "18:26"),
        PartnerChatMessage("c_2", "SUB-01", "Chef Imran", false, "Sure sir! Our chef is preparing your Hyderabadi Dum Biryani with extra spices.", "18:27")
    )
}
