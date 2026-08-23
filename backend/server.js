const express = require('express');
const http = require('http');
const path = require('path');
const { Server } = require('socket.io');
const cors = require('cors');
const crypto = require('crypto');
const uuidv4 = () => crypto.randomUUID();

const app = express();
const server = http.createServer(app);
const io = new Server(server, {
  cors: {
    origin: '*',
    methods: ['GET', 'POST', 'PATCH', 'PUT', 'DELETE']
  }
});

app.use(cors());
app.use(express.json());

// Health check for Render
app.get('/health', (req, res) => {
  res.json({ status: 'ok', service: 'OrderAra Backend API', timestamp: new Date().toISOString() });
});

// In-Memory Database (Synced State across Customer and Partner Apps)
let restaurants = [
  {
    id: "rest_1",
    name: "Royal Biryani House",
    description: "Authentic Dum Biryani, Mughlai Gravies & Charcoal Kebabs",
    bannerUrl: "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=800&auto=format&fit=crop&q=80",
    rating: 4.6,
    totalRatings: 1420,
    deliveryTimeMinutes: 25,
    distanceKm: 2.4,
    deliveryRadiusKm: 7.0,
    minOrderValue: 199.0,
    isVegOnly: false,
    isPromoted: true,
    discountOffer: "50% OFF up to ₹100",
    cuisineTypes: ["Biryani", "Mughlai", "North Indian", "Kebabs"],
    isOpen: true,
    phone: "+91 98450 11223",
    email: "owner@royalbiryani.com",
    address: "Indiranagar 100ft Road, Bangalore"
  },
  {
    id: "rest_2",
    name: "Pizza Milano & Crust",
    description: "Hand-tossed Woodfired Neapolitan Pizzas & Pastas",
    bannerUrl: "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800&auto=format&fit=crop&q=80",
    rating: 4.5,
    totalRatings: 980,
    deliveryTimeMinutes: 20,
    distanceKm: 1.8,
    deliveryRadiusKm: 6.0,
    minOrderValue: 249.0,
    isVegOnly: false,
    isPromoted: false,
    discountOffer: "Flat ₹75 OFF above ₹399",
    cuisineTypes: ["Pizza", "Italian", "Pastas", "Desserts"],
    isOpen: true,
    phone: "+91 98450 22334",
    email: "manager@pizzamilano.com",
    address: "12th Main Road, HAL 2nd Stage, Bangalore"
  },
  {
    id: "rest_3",
    name: "Udupi Sri Krishna Sagar",
    description: "Authentic South Indian Tiffins, Filter Coffee & Thalis",
    bannerUrl: "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=800&auto=format&fit=crop&q=80",
    rating: 4.7,
    totalRatings: 3100,
    deliveryTimeMinutes: 18,
    distanceKm: 1.2,
    deliveryRadiusKm: 5.0,
    minOrderValue: 120.0,
    isVegOnly: true,
    isPromoted: false,
    discountOffer: "20% OFF above ₹150",
    cuisineTypes: ["South Indian", "Pure Veg", "Breakfast", "Thali"],
    isOpen: true,
    phone: "+91 98450 33445",
    email: "contact@udupisagar.com",
    address: "CMH Road, Indiranagar, Bangalore"
  },
  {
    id: "rest_4",
    name: "The Burger Garage",
    description: "Smash Burgers, Loaded Fries & Thick Shakes",
    bannerUrl: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800&auto=format&fit=crop&q=80",
    rating: 4.4,
    totalRatings: 850,
    deliveryTimeMinutes: 22,
    distanceKm: 3.1,
    deliveryRadiusKm: 8.0,
    minOrderValue: 180.0,
    isVegOnly: false,
    isPromoted: true,
    discountOffer: "Free Fries with any Burger",
    cuisineTypes: ["Burgers", "Fast Food", "Fries", "Shakes"],
    isOpen: true,
    phone: "+91 98450 44556",
    email: "hello@burgergarage.com",
    address: "80ft Road, Koramangala 4th Block, Bangalore"
  }
];

let menuItems = [
  {
    id: "menu_1",
    restaurantId: "rest_1",
    name: "Hyderabadi Chicken Dum Biryani",
    description: "Slow cooked fragrant basmati rice layered with spiced chicken, caramelized onions & fresh herbs.",
    price: 320.0,
    category: "Biryani Specials",
    imageUrl: "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=800&auto=format&fit=crop&q=80",
    isVeg: false,
    isAvailable: true,
    isBestSeller: true,
    spicyLevel: 2
  },
  {
    id: "menu_2",
    restaurantId: "rest_1",
    name: "Royal Paneer Tikka Biryani",
    description: "Cottage cheese cubes marinated in tandoori spices, layered over aromatic saffron basmati rice.",
    price: 280.0,
    category: "Biryani Specials",
    imageUrl: "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=800&auto=format&fit=crop&q=80",
    isVeg: true,
    isAvailable: true,
    isBestSeller: true,
    spicyLevel: 1
  },
  {
    id: "menu_3",
    restaurantId: "rest_1",
    name: "Murgh Tangdi Kebab (4 Pcs)",
    description: "Juicy chicken drumsticks marinated in rich cashew paste and tandoori spices, roasted in clay oven.",
    price: 290.0,
    category: "Starters & Kebabs",
    imageUrl: "https://images.unsplash.com/photo-1599488615731-7e5c2823ff28?w=800&auto=format&fit=crop&q=80",
    isVeg: false,
    isAvailable: true,
    isBestSeller: false,
    spicyLevel: 2
  },
  {
    id: "menu_4",
    restaurantId: "rest_1",
    name: "Butter Garlic Naan & Dal Makhani Combo",
    description: "2 fluffy butter garlic naans served with slow simmered creamy black lentil dal makhani.",
    price: 220.0,
    category: "Mains & Breads",
    imageUrl: "https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=800&auto=format&fit=crop&q=80",
    isVeg: true,
    isAvailable: true,
    isBestSeller: false,
    spicyLevel: 0
  },
  {
    id: "menu_5",
    restaurantId: "rest_1",
    name: "Shahi Gulab Jamun with Rabri",
    description: "Warm golden khoya dumplings served in fragrant cardamom saffron syrup with chilled rabri.",
    price: 140.0,
    category: "Desserts",
    imageUrl: "https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?w=800&auto=format&fit=crop&q=80",
    isVeg: true,
    isAvailable: true,
    isBestSeller: false,
    spicyLevel: 0
  }
];

let orders = [
  {
    orderId: "VZ-ORD-9842",
    userId: "user_101",
    userName: "Ashok Sharma",
    userPhone: "+91 98765 43210",
    deliveryAddress: "Flat 402, Sunshine Heights, 12th Main Road, Indiranagar",
    totalPaid: 773.0,
    paymentMethod: "UPI (Google Pay)",
    createdAt: new Date(Date.now() - 15 * 60000).toISOString(),
    subOrders: [
      {
        subOrderId: "SUB-01",
        restaurantId: "rest_1",
        restaurantName: "Royal Biryani House",
        restaurantPhone: "+91 98450 11223",
        status: "PREPARING",
        estimatedDeliveryMinutes: 20,
        driverName: "Ramesh (Staff)",
        driverPhone: "+91 98450 44556",
        items: [
          { menuItem: menuItems[0], quantity: 1, totalPrice: 320.0 },
          { menuItem: menuItems[2], quantity: 1, totalPrice: 290.0 }
        ],
        subTotal: 610.0,
        deliveryFee: 35.0,
        discount: 100.0,
        specialInstructions: "Please pack extra tissues and lime slices."
      }
    ]
  }
];

let chatMessages = [
  {
    id: "msg_1",
    subOrderId: "SUB-01",
    senderName: "Ashok Sharma",
    isFromCustomer: true,
    text: "Hi! Could you please ensure the biryani is freshly hot and spicy?",
    timestamp: "18:26"
  },
  {
    id: "msg_2",
    subOrderId: "SUB-01",
    senderName: "Chef Imran (Kitchen)",
    isFromCustomer: false,
    text: "Sure sir! Our chef is preparing your Hyderabadi Dum Biryani with extra spices.",
    timestamp: "18:27"
  }
];

// --- REST API ENDPOINTS ---

// 1. Get All Restaurants (for Customer Explore Feed)
app.get('/api/restaurants', (req, res) => {
  res.json({ success: true, data: restaurants });
});

// 2. Get Single Restaurant Detail with Menu
app.get('/api/restaurants/:id', (req, res) => {
  const rest = restaurants.find(r => r.id === req.params.id);
  if (!rest) return res.status(404).json({ success: false, message: 'Restaurant not found' });
  const items = menuItems.filter(m => m.restaurantId === req.params.id);
  res.json({ success: true, data: { restaurant: rest, menuItems: items } });
});

// 3. Toggle Stock Availability (from Partner App -> reflects immediately in Customer App)
app.patch('/api/partner/menu/:itemId/toggle-stock', (req, res) => {
  const item = menuItems.find(m => m.id === req.params.itemId);
  if (!item) return res.status(404).json({ success: false, message: 'Menu item not found' });
  
  item.isAvailable = !item.isAvailable;
  
  // Real-time broadcast to all customer apps
  io.emit('menu_item_stock_updated', { itemId: item.id, isAvailable: item.isAvailable, restaurantId: item.restaurantId });
  
  res.json({ success: true, data: item });
});

// 4. Add or Update Menu Item (from Partner App)
app.post('/api/partner/menu', (req, res) => {
  const { restaurantId, name, description, price, category, imageUrl, isVeg } = req.body;
  const newItem = {
    id: `menu_${uuidv4().substring(0, 8)}`,
    restaurantId: restaurantId || "rest_1",
    name,
    description: description || "",
    price: Number(price),
    category: category || "Specials",
    imageUrl: imageUrl || "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=800&auto=format&fit=crop&q=80",
    isVeg: Boolean(isVeg),
    isAvailable: true,
    isBestSeller: false,
    spicyLevel: 1
  };
  menuItems.push(newItem);
  io.emit('menu_updated', { restaurantId: newItem.restaurantId });
  res.json({ success: true, data: newItem });
});

// 5. Update Store Settings (Radius, Min Order, Open/Closed)
app.patch('/api/partner/restaurant/:id/settings', (req, res) => {
  const rest = restaurants.find(r => r.id === req.params.id);
  if (!rest) return res.status(404).json({ success: false, message: 'Restaurant not found' });

  const { deliveryRadiusKm, minOrderValue, isOpen } = req.body;
  if (deliveryRadiusKm !== undefined) rest.deliveryRadiusKm = Number(deliveryRadiusKm);
  if (minOrderValue !== undefined) rest.minOrderValue = Number(minOrderValue);
  if (isOpen !== undefined) rest.isOpen = Boolean(isOpen);

  // Broadcast to customer apps
  io.emit('restaurant_settings_updated', rest);
  res.json({ success: true, data: rest });
});

// 6. Create Multi-Restaurant Order (from Customer App -> splits into Sub-Orders -> rings Partner App)
app.post('/api/orders', (req, res) => {
  const { userId, userName, userPhone, deliveryAddress, paymentMethod, subOrders } = req.body;
  const orderId = `VZ-ORD-${Math.floor(1000 + Math.random() * 9000)}`;

  const createdSubOrders = subOrders.map((so, idx) => ({
    subOrderId: `SUB-0${idx + 1}`,
    restaurantId: so.restaurantId,
    restaurantName: so.restaurantName,
    restaurantPhone: "+91 98450 11223",
    status: "PLACED",
    estimatedDeliveryMinutes: 25,
    driverName: "Pending Assignment",
    driverPhone: "",
    items: so.items,
    subTotal: so.subTotal,
    deliveryFee: so.deliveryFee,
    discount: so.discount || 0,
    specialInstructions: so.specialInstructions || ""
  }));

  const totalPaid = createdSubOrders.reduce((sum, so) => sum + so.subTotal + so.deliveryFee - so.discount, 0);

  const newOrder = {
    orderId,
    userId: userId || "user_101",
    userName: userName || "Ashok Sharma",
    userPhone: userPhone || "+91 98765 43210",
    deliveryAddress: deliveryAddress || "Indiranagar, Bangalore",
    totalPaid,
    paymentMethod: paymentMethod || "Online UPI",
    createdAt: new Date().toISOString(),
    subOrders: createdSubOrders
  };

  orders.unshift(newOrder);

  // Emit real-time notification to Partner Apps for each sub-order
  createdSubOrders.forEach(so => {
    io.emit(`new_sub_order_${so.restaurantId}`, {
      orderId,
      subOrder: so,
      customerName: newOrder.userName,
      customerPhone: newOrder.userPhone,
      deliveryAddress: newOrder.deliveryAddress
    });
  });

  res.json({ success: true, data: newOrder });
});

// 7. Update Sub-Order Status (from Partner App -> triggers instant live stepper update on Customer App)
app.patch('/api/orders/sub-order/:subOrderId/status', (req, res) => {
  const { subOrderId } = req.params;
  const { status, driverName, estimatedDeliveryMinutes } = req.body;

  let matchedSubOrder = null;
  let parentOrderId = null;

  for (const order of orders) {
    const found = order.subOrders.find(so => so.subOrderId === subOrderId);
    if (found) {
      found.status = status;
      if (driverName) found.driverName = driverName;
      if (estimatedDeliveryMinutes) found.estimatedDeliveryMinutes = estimatedDeliveryMinutes;
      matchedSubOrder = found;
      parentOrderId = order.orderId;
      break;
    }
  }

  if (!matchedSubOrder) {
    return res.status(404).json({ success: false, message: 'Sub-Order not found' });
  }

  // Real-time broadcast to Customer App
  io.emit('sub_order_status_updated', {
    orderId: parentOrderId,
    subOrderId,
    status: matchedSubOrder.status,
    driverName: matchedSubOrder.driverName,
    estimatedDeliveryMinutes: matchedSubOrder.estimatedDeliveryMinutes
  });

  res.json({ success: true, data: matchedSubOrder });
});

// 8. In-App Chat Endpoint (Instant bidirectional sync)
app.get('/api/chat/:subOrderId', (req, res) => {
  const msgs = chatMessages.filter(m => m.subOrderId === req.params.subOrderId);
  res.json({ success: true, data: msgs });
});

app.post('/api/chat/:subOrderId', (req, res) => {
  const { subOrderId } = req.params;
  const { senderName, isFromCustomer, text } = req.body;
  const now = new Date();
  const timeStr = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;

  const newMsg = {
    id: `msg_${Date.now()}`,
    subOrderId,
    senderName,
    isFromCustomer: Boolean(isFromCustomer),
    text,
    timestamp: timeStr
  };

  chatMessages.push(newMsg);

  // Broadcast to both Customer and Partner apps
  io.emit(`chat_message_${subOrderId}`, newMsg);
  res.json({ success: true, data: newMsg });
});

// Static Admin Panel Serving (when deployed as unified service)
const adminDistPath = path.join(__dirname, '../admin-panel/dist');
const localDistPath = path.join(__dirname, 'dist');
if (require('fs').existsSync(adminDistPath)) {
  app.use(express.static(adminDistPath));
  app.get('*', (req, res, next) => {
    if (req.path.startsWith('/api')) return next();
    res.sendFile(path.join(adminDistPath, 'index.html'));
  });
} else if (require('fs').existsSync(localDistPath)) {
  app.use(express.static(localDistPath));
  app.get('*', (req, res, next) => {
    if (req.path.startsWith('/api')) return next();
    res.sendFile(path.join(localDistPath, 'index.html'));
  });
} else {
  app.get('/', (req, res) => {
    res.json({
      service: 'OrderAra Backend Engine',
      status: 'Active',
      apiDocs: {
        restaurants: '/api/restaurants',
        orders: '/api/orders',
        health: '/health'
      }
    });
  });
}

// --- WEBSOCKET CONNECTION HANDLING ---
io.on('connection', (socket) => {
  console.log(`[Socket] Client connected: ${socket.id}`);

  socket.on('disconnect', () => {
    console.log(`[Socket] Client disconnected: ${socket.id}`);
  });
});

const PORT = process.env.PORT || 8080;
server.listen(PORT, '0.0.0.0', () => {
  console.log(`⚡ OrderAra Unified Real-time Server running on port ${PORT}`);
});
