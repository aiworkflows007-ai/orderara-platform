package com.vigizoomato.customer.data.mock

import com.vigizoomato.customer.data.models.*

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

    val sampleRestaurants = listOf(
        Restaurant(
            id = "rest_1",
            name = "Royal Biryani House",
            description = "Authentic Dum Biryani, Mughlai Gravies & Charcoal Kebabs",
            cuisineTypes = listOf("Biryani", "Mughlai", "North Indian", "Kebabs"),
            rating = 4.6,
            ratingCount = 1420,
            deliveryTimeMinutes = 35,
            deliveryRadiusKm = 7.0,
            distanceKm = 2.4,
            minOrderValue = 199.0,
            bannerUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=800&auto=format&fit=crop&q=80",
            isVegOnly = false,
            isPromoted = true,
            discountOffer = "50% OFF up to ₹100",
            phoneNumber = "+91 98450 11223",
            address = "Indiranagar 100ft Road, Bangalore",
            isOpen = true,
            isFavorite = true
        ),
        Restaurant(
            id = "rest_2",
            name = "Pizza Milano & Crust",
            description = "Woodfired Artisanal Pizzas, Fresh Pasta & Garlic Breads",
            cuisineTypes = listOf("Pizza", "Italian", "Pastas", "Desserts"),
            rating = 4.5,
            ratingCount = 980,
            deliveryTimeMinutes = 25,
            deliveryRadiusKm = 5.5,
            distanceKm = 1.8,
            minOrderValue = 249.0,
            bannerUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800&auto=format&fit=crop&q=80",
            isVegOnly = false,
            isPromoted = false,
            discountOffer = "FLAT ₹75 OFF on ₹299",
            phoneNumber = "+91 98450 44556",
            address = "Koramangala 5th Block, Bangalore",
            isOpen = true,
            isFavorite = false
        ),
        Restaurant(
            id = "rest_3",
            name = "Udupi Sri Krishna Sagar",
            description = "Crispy Ghee Dosas, Fluffy Idlis, Filter Coffee & South Thalis",
            cuisineTypes = listOf("South Indian", "Pure Veg", "Breakfast", "Thali"),
            rating = 4.7,
            ratingCount = 3100,
            deliveryTimeMinutes = 20,
            deliveryRadiusKm = 6.0,
            distanceKm = 1.2,
            minOrderValue = 120.0,
            bannerUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=800&auto=format&fit=crop&q=80",
            isVegOnly = true,
            isPromoted = false,
            discountOffer = "20% OFF above ₹150",
            phoneNumber = "+91 98450 77889",
            address = "MG Road, Bangalore",
            isOpen = true,
            isFavorite = true
        ),
        Restaurant(
            id = "rest_4",
            name = "The Burger Garage",
            description = "Gourmet Smashed Burgers, Loaded Cheesy Fries & Thick Shakes",
            cuisineTypes = listOf("Burgers", "Fast Food", "Fries", "Beverages"),
            rating = 4.4,
            ratingCount = 850,
            deliveryTimeMinutes = 30,
            deliveryRadiusKm = 5.0,
            distanceKm = 3.1,
            minOrderValue = 180.0,
            bannerUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800&auto=format&fit=crop&q=80",
            isVegOnly = false,
            isPromoted = true,
            discountOffer = "Free Fries with any Burger",
            phoneNumber = "+91 98450 99001",
            address = "HSR Layout Sector 3, Bangalore",
            isOpen = true,
            isFavorite = false
        ),
        Restaurant(
            id = "rest_5",
            name = "Wok & Dragon Chinese",
            description = "Sizzling Hakka Noodles, Manchurian, Dimsums & Dragon Chicken",
            cuisineTypes = listOf("Chinese", "Asian", "Dimsums", "Noodles"),
            rating = 4.3,
            ratingCount = 670,
            deliveryTimeMinutes = 32,
            deliveryRadiusKm = 6.0,
            distanceKm = 2.9,
            minOrderValue = 199.0,
            bannerUrl = "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=800&auto=format&fit=crop&q=80",
            isVegOnly = false,
            isPromoted = false,
            discountOffer = "30% OFF up to ₹80",
            phoneNumber = "+91 98450 33221",
            address = "Church Street, Bangalore",
            isOpen = true,
            isFavorite = false
        ),
        Restaurant(
            id = "rest_6",
            name = "Sweet Treats & Waffles",
            description = "Belgian Waffles, Pancakes, Nutella Sundaes & Artisanal Ice Creams",
            cuisineTypes = listOf("Desserts", "Waffles", "Ice Cream", "Bakery"),
            rating = 4.8,
            ratingCount = 1200,
            deliveryTimeMinutes = 20,
            deliveryRadiusKm = 4.5,
            distanceKm = 1.5,
            minOrderValue = 149.0,
            bannerUrl = "https://images.unsplash.com/photo-1565958011703-44f9829ba187?w=800&auto=format&fit=crop&q=80",
            isVegOnly = true,
            isPromoted = false,
            discountOffer = "Buy 1 Get 1 on Waffles",
            phoneNumber = "+91 98450 55443",
            address = "Lavelle Road, Bangalore",
            isOpen = true,
            isFavorite = false
        )
    )

    val sampleMenuItems = mapOf(
        "rest_1" to listOf(
            MenuItem(
                id = "item_101",
                restaurantId = "rest_1",
                name = "Hyderabadi Chicken Dum Biryani",
                description = "Fragrant long-grain basmati rice layered with spiced tender chicken and saffron, served with mirchi ka salan and raita.",
                price = 320.0,
                category = "Biryani Specials",
                imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=500&auto=format&fit=crop&q=80",
                isVeg = false,
                isBestSeller = true,
                spicyLevel = 2
            ),
            MenuItem(
                id = "item_102",
                restaurantId = "rest_1",
                name = "Royal Paneer Tikka Biryani",
                description = "Marinated grilled paneer cubes infused with rich spices and dum cooked with aromatic rice.",
                price = 270.0,
                category = "Biryani Specials",
                imageUrl = "https://images.unsplash.com/photo-1589302168068-964664d93dc0?w=500&auto=format&fit=crop&q=80",
                isVeg = true,
                isBestSeller = false,
                spicyLevel = 1
            ),
            MenuItem(
                id = "item_103",
                restaurantId = "rest_1",
                name = "Murgh Tangdi Kebab (4 Pcs)",
                description = "Succulent chicken drumsticks marinated in cream, roasted spices, and cooked to perfection in tandoor.",
                price = 290.0,
                category = "Starters & Kebabs",
                imageUrl = "https://images.unsplash.com/photo-1599488615731-7e5c2823ff28?w=500&auto=format&fit=crop&q=80",
                isVeg = false,
                isBestSeller = true,
                spicyLevel = 2
            ),
            MenuItem(
                id = "item_104",
                restaurantId = "rest_1",
                name = "Butter Naan & Dal Makhani Combo",
                description = "Two soft butter naans served with slow-cooked creamy black lentils topped with white butter.",
                price = 220.0,
                category = "Mains & Combos",
                imageUrl = "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=500&auto=format&fit=crop&q=80",
                isVeg = true,
                isBestSeller = false,
                spicyLevel = 1
            ),
            MenuItem(
                id = "item_105",
                restaurantId = "rest_1",
                name = "Gulab Jamun (2 Pcs)",
                description = "Warm golden milk dumplings soaked in cardamom flavored sugar syrup.",
                price = 70.0,
                category = "Desserts",
                imageUrl = "https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?w=500&auto=format&fit=crop&q=80",
                isVeg = true,
                isBestSeller = false,
                spicyLevel = 0
            )
        ),
        "rest_2" to listOf(
            MenuItem(
                id = "item_201",
                restaurantId = "rest_2",
                name = "Margherita Gourmet Pizza (10\")",
                description = "Classic San Marzano tomato sauce, fresh buffalo mozzarella, fresh basil, and extra virgin olive oil.",
                price = 299.0,
                category = "Woodfired Pizzas",
                imageUrl = "https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=500&auto=format&fit=crop&q=80",
                isVeg = true,
                isBestSeller = true,
                spicyLevel = 0
            ),
            MenuItem(
                id = "item_202",
                restaurantId = "rest_2",
                name = "Smoky BBQ Chicken Pizza (10\")",
                description = "Grilled BBQ chicken breast, red onions, pickled jalapenos, mozzarella, and smoky barbecue glaze.",
                price = 389.0,
                category = "Woodfired Pizzas",
                imageUrl = "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=500&auto=format&fit=crop&q=80",
                isVeg = false,
                isBestSeller = true,
                spicyLevel = 2
            ),
            MenuItem(
                id = "item_203",
                restaurantId = "rest_2",
                name = "Creamy Alfredo Penne Pasta",
                description = "Penne in rich parmesan cream sauce with sauteed mushrooms and roasted bell peppers.",
                price = 280.0,
                category = "Pasta & Sides",
                imageUrl = "https://images.unsplash.com/photo-1621996346565-e3d5d6281084?w=500&auto=format&fit=crop&q=80",
                isVeg = true,
                isBestSeller = false,
                spicyLevel = 0
            ),
            MenuItem(
                id = "item_204",
                restaurantId = "rest_2",
                name = "Cheesy Garlic Stuffed Bread",
                description = "Freshly baked artisan bread loaf stuffed with melted mozzarella, roasted garlic butter, and herbs.",
                price = 160.0,
                category = "Pasta & Sides",
                imageUrl = "https://images.unsplash.com/photo-1619895092538-128341789043?w=500&auto=format&fit=crop&q=80",
                isVeg = true,
                isBestSeller = true,
                spicyLevel = 1
            )
        ),
        "rest_3" to listOf(
            MenuItem(
                id = "item_301",
                restaurantId = "rest_3",
                name = "Special Masala Ghee Roast Dosa",
                description = "Crispy golden crepe roasted in pure desi ghee, filled with seasoned spiced potato mash and served with 3 chutneys & sambar.",
                price = 110.0,
                category = "Dosas & Idlis",
                imageUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=500&auto=format&fit=crop&q=80",
                isVeg = true,
                isBestSeller = true,
                spicyLevel = 1
            ),
            MenuItem(
                id = "item_302",
                restaurantId = "rest_3",
                name = "Steamed Button Idli (4 Pcs) + Medu Vada (1 Pc)",
                description = "Melt-in-mouth steamed rice cakes and crisp lentil donut, served hot with piping sambar.",
                price = 95.0,
                category = "Dosas & Idlis",
                imageUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=500&auto=format&fit=crop&q=80",
                isVeg = true,
                isBestSeller = true,
                spicyLevel = 0
            ),
            MenuItem(
                id = "item_303",
                restaurantId = "rest_3",
                name = "South Indian Mini Executive Meals",
                description = "Steamed rice, sambar, rasam, kootu, curd, papad, pickle, and payasam dessert.",
                price = 160.0,
                category = "Thali & Meals",
                imageUrl = "https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=500&auto=format&fit=crop&q=80",
                isVeg = true,
                isBestSeller = false,
                spicyLevel = 1
            ),
            MenuItem(
                id = "item_304",
                restaurantId = "rest_3",
                name = "Authentic Kumbakonam Degree Coffee",
                description = "Rich freshly brewed chicory-infused filter coffee with frothed full-cream milk.",
                price = 45.0,
                category = "Beverages",
                imageUrl = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=500&auto=format&fit=crop&q=80",
                isVeg = true,
                isBestSeller = true,
                spicyLevel = 0
            )
        ),
        "rest_4" to listOf(
            MenuItem(
                id = "item_401",
                restaurantId = "rest_4",
                name = "Double Smashed Cheeseburger",
                description = "Two juicy grilled patties, double cheddar cheese, caramelized onions, secret sauce on brioche bun.",
                price = 249.0,
                category = "Signature Burgers",
                imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&auto=format&fit=crop&q=80",
                isVeg = false,
                isBestSeller = true,
                spicyLevel = 1
            ),
            MenuItem(
                id = "item_402",
                restaurantId = "rest_4",
                name = "Crispy Peri-Peri Paneer Burger",
                description = "Crunchy spiced paneer patty, lettuce, jalapeño mayo, and peri-peri seasoning.",
                price = 199.0,
                category = "Signature Burgers",
                imageUrl = "https://images.unsplash.com/photo-1550547660-d9450f859349?w=500&auto=format&fit=crop&q=80",
                isVeg = true,
                isBestSeller = false,
                spicyLevel = 2
            ),
            MenuItem(
                id = "item_403",
                restaurantId = "rest_4",
                name = "Loaded Cheesy Bacon Fries",
                description = "Crispy golden french fries drizzled with warm cheddar sauce, crispy bits, and spring onions.",
                price = 179.0,
                category = "Fries & Sides",
                imageUrl = "https://images.unsplash.com/photo-1586190848861-99aa4a171e90?w=500&auto=format&fit=crop&q=80",
                isVeg = false,
                isBestSeller = true,
                spicyLevel = 1
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

    val sampleReviews = listOf(
        Review(
            id = "rev_1",
            restaurantId = "rest_1",
            customerName = "Rahul Verma",
            rating = 5.0,
            comment = "Best Hyderabadi Dum Biryani in town! Rice was so fragrant and chicken was cooked to absolute tenderness.",
            date = "Yesterday",
            orderedDishes = listOf("Hyderabadi Chicken Dum Biryani", "Murgh Tangdi Kebab")
        ),
        Review(
            id = "rev_2",
            restaurantId = "rest_1",
            customerName = "Pooja Hegde",
            rating = 4.5,
            comment = "Portion size was generous and food arrived super hot. Mirchi ka salan is spicy and flavorful!",
            date = "3 days ago",
            orderedDishes = listOf("Royal Paneer Tikka Biryani")
        ),
        Review(
            id = "rev_3",
            restaurantId = "rest_2",
            customerName = "Vikram Iyer",
            rating = 5.0,
            comment = "The crust on Margherita pizza was airy and crispy. Real Neapolitan style!",
            date = "5 days ago",
            orderedDishes = listOf("Margherita Gourmet Pizza", "Cheesy Garlic Stuffed Bread")
        )
    )

    val sampleActiveOrders = listOf(
        Order(
            id = "VZ-ORD-9842",
            customerId = "user_101",
            subOrders = listOf(
                SubOrder(
                    subOrderId = "SUB-01",
                    orderId = "VZ-ORD-9842",
                    restaurantId = "rest_1",
                    restaurantName = "Royal Biryani House",
                    restaurantPhone = "+91 98450 11223",
                    items = listOf(
                        CartItem(
                            menuItem = sampleMenuItems["rest_1"]!![0],
                            restaurantId = "rest_1",
                            restaurantName = "Royal Biryani House",
                            restaurantMinOrder = 199.0,
                            quantity = 1
                        ),
                        CartItem(
                            menuItem = sampleMenuItems["rest_1"]!![2],
                            restaurantId = "rest_1",
                            restaurantName = "Royal Biryani House",
                            restaurantMinOrder = 199.0,
                            quantity = 1
                        )
                    ),
                    subTotal = 610.0,
                    deliveryFee = 35.0,
                    discount = 100.0,
                    status = OrderStatus.PREPARING,
                    statusHistory = listOf(
                        StatusTimestamp(OrderStatus.PLACED, "18:05", "Order received by system"),
                        StatusTimestamp(OrderStatus.ACCEPTED, "18:07", "Accepted by restaurant kitchen"),
                        StatusTimestamp(OrderStatus.PREPARING, "18:10", "Chef is cooking your biryani & kebabs")
                    ),
                    estimatedDeliveryMinutes = 20,
                    driverName = "Restaurant Rider (Sunil)",
                    driverPhone = "+91 98765 11223"
                ),
                SubOrder(
                    subOrderId = "SUB-02",
                    orderId = "VZ-ORD-9842",
                    restaurantId = "rest_2",
                    restaurantName = "Pizza Milano & Crust",
                    restaurantPhone = "+91 98450 44556",
                    items = listOf(
                        CartItem(
                            menuItem = sampleMenuItems["rest_2"]!![3],
                            restaurantId = "rest_2",
                            restaurantName = "Pizza Milano & Crust",
                            restaurantMinOrder = 249.0,
                            quantity = 1
                        )
                    ),
                    subTotal = 160.0,
                    deliveryFee = 30.0,
                    discount = 0.0,
                    status = OrderStatus.OUT_FOR_DELIVERY,
                    statusHistory = listOf(
                        StatusTimestamp(OrderStatus.PLACED, "18:05", "Order received"),
                        StatusTimestamp(OrderStatus.ACCEPTED, "18:06", "Restaurant confirmed"),
                        StatusTimestamp(OrderStatus.PREPARING, "18:08", "Baked fresh in woodfire oven"),
                        StatusTimestamp(OrderStatus.OUT_FOR_DELIVERY, "18:18", "Rider Ramesh is on the way")
                    ),
                    estimatedDeliveryMinutes = 8,
                    driverName = "Restaurant Staff (Ramesh)",
                    driverPhone = "+91 98765 44556"
                )
            ),
            itemsTotal = 770.0,
            totalDeliveryFee = 65.0,
            taxesAndPlatformFee = 38.5,
            totalDiscount = 100.0,
            grandTotal = 773.5,
            paymentStatus = "PAID",
            paymentMethod = "UPI (Google Pay)",
            transactionId = "UPI/2026/88492048",
            deliveryAddress = sampleUser.savedAddresses[0],
            deliveryInstructions = "Please ring doorbell and leave with security if unreachable.",
            createdAt = "15 mins ago"
        )
    )

    val sampleChatMessages = listOf(
        ChatMessage(
            id = "msg_1",
            orderId = "VZ-ORD-9842",
            subOrderId = "SUB-01",
            restaurantName = "Royal Biryani House",
            senderType = SenderType.SYSTEM,
            senderName = "System",
            messageText = "Order confirmed! Royal Biryani House has started preparing your food.",
            timestamp = "18:07"
        ),
        ChatMessage(
            id = "msg_2",
            orderId = "VZ-ORD-9842",
            subOrderId = "SUB-01",
            restaurantName = "Royal Biryani House",
            senderType = SenderType.CUSTOMER,
            senderName = "Ashok",
            messageText = "Hi, please make the biryani extra spicy and pack extra salan if possible!",
            timestamp = "18:09"
        ),
        ChatMessage(
            id = "msg_3",
            orderId = "VZ-ORD-9842",
            subOrderId = "SUB-01",
            restaurantName = "Royal Biryani House",
            senderType = SenderType.RESTAURANT_STAFF,
            senderName = "Chef Imran",
            messageText = "Sure sir! We have added extra salan and spice level is dialed up for you.",
            timestamp = "18:11"
        )
    )
}
