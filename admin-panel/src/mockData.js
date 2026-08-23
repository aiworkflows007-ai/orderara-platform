export const initialRestaurants = [
  {
    id: "rest_1",
    name: "Royal Biryani House",
    owner: "Farhan Khan",
    phone: "+91 98450 11223",
    email: "owner@royalbiryani.com",
    address: "Indiranagar 100ft Road, Bangalore",
    rating: 4.6,
    totalOrders: 1420,
    deliveryRadiusKm: 7.0,
    minOrderValue: 199.0,
    subscriptionStatus: "ACTIVE_TRIAL",
    trialDaysRemaining: 12,
    plan: "Unlimited Monthly (₹999/mo)",
    joinedDate: "10 Aug 2026",
    isOnline: true,
    bankAccount: "HDFC Bank (•••• 4920)"
  },
  {
    id: "rest_2",
    name: "Pizza Milano & Crust",
    owner: "Marco Rossi",
    phone: "+91 98450 22334",
    email: "manager@pizzamilano.com",
    address: "12th Main Road, HAL 2nd Stage, Bangalore",
    rating: 4.5,
    totalOrders: 980,
    deliveryRadiusKm: 6.0,
    minOrderValue: 249.0,
    subscriptionStatus: "ACTIVE_PAID",
    trialDaysRemaining: 0,
    plan: "Unlimited Monthly (₹999/mo)",
    joinedDate: "15 Jul 2026",
    isOnline: true,
    bankAccount: "ICICI Bank (•••• 8112)"
  },
  {
    id: "rest_3",
    name: "Udupi Sri Krishna Sagar",
    owner: "Venkatesh Rao",
    phone: "+91 98450 33445",
    email: "contact@udupisagar.com",
    address: "CMH Road, Indiranagar, Bangalore",
    rating: 4.7,
    totalOrders: 3100,
    deliveryRadiusKm: 5.0,
    minOrderValue: 120.0,
    subscriptionStatus: "ACTIVE_PAID",
    trialDaysRemaining: 0,
    plan: "Unlimited Monthly (₹999/mo)",
    joinedDate: "01 Jun 2026",
    isOnline: true,
    bankAccount: "Canara Bank (•••• 3340)"
  },
  {
    id: "rest_4",
    name: "The Burger Garage",
    owner: "Siddharth Sen",
    phone: "+91 98450 44556",
    email: "hello@burgergarage.com",
    address: "80ft Road, Koramangala, Bangalore",
    rating: 4.4,
    totalOrders: 850,
    deliveryRadiusKm: 8.0,
    minOrderValue: 180.0,
    subscriptionStatus: "ACTIVE_TRIAL",
    trialDaysRemaining: 4,
    plan: "Unlimited Monthly (₹999/mo)",
    joinedDate: "18 Aug 2026",
    isOnline: true,
    bankAccount: "Axis Bank (•••• 9901)"
  },
  {
    id: "rest_5",
    name: "Wok & Dragon Chinese",
    owner: "Chen Wei",
    phone: "+91 98450 55667",
    email: "chen@wokdragon.com",
    address: "Koramangala 5th Block, Bangalore",
    rating: 4.3,
    totalOrders: 670,
    deliveryRadiusKm: 6.5,
    minOrderValue: 199.0,
    subscriptionStatus: "ACTIVE_PAID",
    trialDaysRemaining: 0,
    plan: "Unlimited Monthly (₹999/mo)",
    joinedDate: "02 May 2026",
    isOnline: false,
    bankAccount: "State Bank of India (•••• 1209)"
  },
  {
    id: "rest_6",
    name: "Sweet Treats & Waffles",
    owner: "Ananya Deshmukh",
    phone: "+91 98450 66778",
    email: "ananya@sweettreats.com",
    address: "100ft Road, Indiranagar, Bangalore",
    rating: 4.8,
    totalOrders: 1200,
    deliveryRadiusKm: 4.5,
    minOrderValue: 149.0,
    subscriptionStatus: "ACTIVE_TRIAL",
    trialDaysRemaining: 1,
    plan: "Unlimited Monthly (₹999/mo)",
    joinedDate: "08 Aug 2026",
    isOnline: true,
    bankAccount: "Kotak Mahindra Bank (•••• 7762)"
  },
  {
    id: "rest_7",
    name: "Midnight Shawarma & Grills",
    owner: "Tariq Ali",
    phone: "+91 98450 77889",
    email: "tariq@midnightshawarma.com",
    address: "Tavarekere Main Rd, BTM Layout, Bangalore",
    rating: 4.1,
    totalOrders: 420,
    deliveryRadiusKm: 5.0,
    minOrderValue: 150.0,
    subscriptionStatus: "SUSPENDED",
    trialDaysRemaining: 0,
    plan: "Unlimited Monthly (₹999/mo)",
    joinedDate: "12 Mar 2026",
    isOnline: false,
    bankAccount: "HDFC Bank (•••• 0034)"
  }
];

export const liveOrders = [
  {
    id: "VZ-ORD-9842",
    customer: "Ashok Sharma",
    phone: "+91 98765 43210",
    address: "Indiranagar, Bangalore",
    time: "18:25",
    totalAmount: 773,
    paymentMethod: "Online UPI (GPay)",
    subOrders: [
      { id: "SUB-01", restaurant: "Royal Biryani House", status: "PREPARING", items: "1x Hyderabadi Biryani, 1x Tangdi Kebab", amount: 545 },
      { id: "SUB-02", restaurant: "Pizza Milano & Crust", status: "OUT_FOR_DELIVERY", items: "1x Garlic Stuffed Bread", amount: 228 }
    ]
  },
  {
    id: "VZ-ORD-9855",
    customer: "Pooja Reddy",
    phone: "+91 99887 76655",
    address: "Whitefield, Bangalore",
    time: "18:44",
    totalAmount: 885,
    paymentMethod: "Credit Card",
    subOrders: [
      { id: "SUB-04", restaurant: "Royal Biryani House", status: "PLACED", items: "2x Paneer Tikka Biryani, 2x Gulab Jamun", amount: 885 }
    ]
  },
  {
    id: "VZ-ORD-9830",
    customer: "Kiran Kumar",
    phone: "+91 98112 33445",
    address: "Koramangala, Bangalore",
    time: "18:10",
    totalAmount: 430,
    paymentMethod: "PhonePe UPI",
    subOrders: [
      { id: "SUB-09", restaurant: "The Burger Garage", status: "DELIVERED", items: "2x Garage Smash Burger, 1x Choco Shake", amount: 430 }
    ]
  }
];

export const monthlyRevenueStats = [
  { month: "Mar", mrr: 8991, restaurants: 9 },
  { month: "Apr", mrr: 12987, restaurants: 13 },
  { month: "May", mrr: 15984, restaurants: 16 },
  { month: "Jun", mrr: 17982, restaurants: 18 },
  { month: "Jul", mrr: 20979, restaurants: 21 },
  { month: "Aug", mrr: 23976, restaurants: 24 }
];

export const platformCoupons = [
  { code: "ARAWELCOME", discount: "Flat ₹50 OFF", minOrder: 199, appliesTo: "All Restaurants (Platform-wide)", active: true },
  { code: "FESTIVE100", discount: "Flat ₹100 OFF", minOrder: 499, appliesTo: "All Restaurants", active: true },
  { code: "FREEDEL", discount: "Free Delivery", minOrder: 299, appliesTo: "Orders above ₹299", active: false }
];
