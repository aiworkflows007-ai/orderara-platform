/**
 * OrderAra unified backend.
 *
 * One server joins three products:
 *   - Customer app   (browse, order, track, chat, rate)
 *   - Partner app    (register, menu, orders, status, subscription)
 *   - Admin panel    (directory, live orders, subscriptions, suspension)
 *
 * State lives in ./store.js and is persisted to data.json.
 */

const express = require('express');
const http = require('http');
const path = require('path');
const fs = require('fs');
const crypto = require('crypto');
const cors = require('cors');
const { Server } = require('socket.io');

const store = require('./store');
const { partnerAuth, describeAuthMode } = require('./auth');
const { state, save, SUB_STATUS, ORDER_STATUS } = store;

const uid = (prefix) => `${prefix}_${crypto.randomUUID().split('-')[0]}`;

const app = express();
const server = http.createServer(app);
const io = new Server(server, {
  cors: { origin: '*', methods: ['GET', 'POST', 'PATCH', 'PUT', 'DELETE'] }
});

app.use(cors());
app.use(express.json({ limit: '2mb' }));

// Some Android HTTP clients refuse to send PATCH/DELETE. Those requests arrive
// as POST carrying the real verb in a header — restore it before routing.
app.use((req, res, next) => {
  const override = req.headers['x-http-method-override'];
  if (override && req.method === 'POST') req.method = String(override).toUpperCase();
  next();
});

// Partner routes carry the restaurant's identity. This attaches req.auth when a
// valid Supabase token is present; whether a missing token is fatal is decided
// by REQUIRE_PARTNER_AUTH so that enforcement can be switched on separately
// from shipping this code.
app.use('/api/partner', partnerAuth);

// ---------------------------------------------------------------------------
// Broadcast helpers — every mutation tells the other two apps about it
// ---------------------------------------------------------------------------

const roomRestaurant = id => `restaurant:${id}`;
const roomCustomer = id => `customer:${id}`;
const roomSubOrder = id => `suborder:${id}`;
const ROOM_ADMIN = 'admin';

function broadcastRestaurants() {
  const listed = state.restaurants.filter(store.isListed);
  io.emit('restaurants:updated', listed);
  io.to(ROOM_ADMIN).emit('admin:restaurants:updated', state.restaurants);
}

function findRestaurant(id) {
  return state.restaurants.find(r => r.id === id);
}

function findSubOrder(subOrderId) {
  for (const order of state.orders) {
    const sub = order.subOrders.find(s => s.subOrderId === subOrderId);
    if (sub) return { order, sub };
  }
  return { order: null, sub: null };
}

function menuFor(restaurantId) {
  return state.menuItems.filter(m => m.restaurantId === restaurantId);
}

// ---------------------------------------------------------------------------
// Health
// ---------------------------------------------------------------------------

app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    service: 'OrderAra Backend API',
    restaurants: state.restaurants.length,
    orders: state.orders.length,
    timestamp: new Date().toISOString()
  });
});

// ---------------------------------------------------------------------------
// Customer — discovery
// ---------------------------------------------------------------------------

// Only restaurants with a live subscription are visible to customers.
app.get('/api/restaurants', (req, res) => {
  res.json({ success: true, data: state.restaurants.filter(store.isListed) });
});

app.get('/api/restaurants/:id', (req, res) => {
  const rest = findRestaurant(req.params.id);
  if (!rest) return res.status(404).json({ success: false, message: 'Restaurant not found' });
  res.json({
    success: true,
    data: {
      restaurant: rest,
      menuItems: menuFor(rest.id),
      reviews: state.reviews.filter(r => r.restaurantId === rest.id)
    }
  });
});

// ---------------------------------------------------------------------------
// Partner — registration and profile
// ---------------------------------------------------------------------------

const BANNERS = [
  'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1552566626-52f8b828add9?w=800&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1514933651103-005eec06c04b?w=800&auto=format&fit=crop&q=80'
];

/**
 * Resolves the signed-in account to its restaurant.
 *
 * The Partner app calls this on launch: 200 means "go to the dashboard",
 * 404 means "this account has no restaurant yet, show onboarding".
 */
app.get('/api/partner/me', (req, res) => {
  if (!req.auth) {
    return res.status(401).json({ success: false, message: 'Sign-in required' });
  }
  const mine = state.restaurants.find(r => r.ownerAuthSub === req.auth.sub);
  if (!mine) {
    return res.status(404).json({
      success: false,
      message: 'No restaurant linked to this account',
      account: { email: req.auth.email }
    });
  }
  return res.json({ success: true, data: mine });
});

/**
 * Attaches an existing, unclaimed restaurant to the signed-in account.
 *
 * Needed because the seeded demo restaurants predate sign-in and have no owner.
 * Deliberately refuses to touch one that is already claimed, so this cannot be
 * used to take over somebody else's shop.
 */
app.post('/api/partner/claim', (req, res) => {
  if (!req.auth) {
    return res.status(401).json({ success: false, message: 'Sign-in required' });
  }
  const { restaurantId } = req.body || {};
  const rest = findRestaurant(restaurantId);
  if (!rest) {
    return res.status(404).json({ success: false, message: 'Restaurant not found' });
  }
  if (rest.ownerAuthSub && rest.ownerAuthSub !== req.auth.sub) {
    return res.status(409).json({ success: false, message: 'Already claimed by another account' });
  }
  rest.ownerAuthSub = req.auth.sub;
  rest.ownerEmail = req.auth.email || rest.ownerEmail || '';
  save();
  return res.json({ success: true, data: rest });
});

app.post('/api/partner/register', (req, res) => {
  const {
    id, name, ownerName, description, phone, email, address,
    deliveryRadiusKm, minOrderValue, isVegOnly, cuisineTypes, upiId
  } = req.body;

  const restId = id || uid('rest');
  if (findRestaurant(restId)) {
    return res.status(409).json({ success: false, message: 'Restaurant already registered' });
  }

  const cuisines = Array.isArray(cuisineTypes) && cuisineTypes.length ? cuisineTypes : ['Multi-Cuisine'];

  const newRestaurant = store.withSubscriptionDefaults({
    id: restId,
    name: name || 'New Restaurant',
    ownerName: ownerName || '',
    description: description || cuisines.join(', '),
    bannerUrl: BANNERS[Math.floor(Math.random() * BANNERS.length)],
    rating: 5.0,
    totalRatings: 0,
    ratingSum: 0,
    deliveryTimeMinutes: 25,
    distanceKm: 2.1,
    deliveryRadiusKm: Number(deliveryRadiusKm) || 7.0,
    minOrderValue: Number(minOrderValue) || 199.0,
    isVegOnly: !!isVegOnly,
    isPromoted: true,
    discountOffer: 'Flat ₹50 OFF',
    cuisineTypes: cuisines,
    isOpen: true,
    phone: phone || '',
    email: email || '',
    address: address || '',
    upiId: upiId || 'pay@upi',
    // Who owns this restaurant, as far as Supabase is concerned. Set only when
    // the caller arrived with a verified token; stays empty for the seeded demo
    // restaurants, which is what lets them be claimed later.
    ownerAuthSub: (req.auth && req.auth.sub) || '',
    ownerEmail: (req.auth && req.auth.email) || email || '',
    subscriptionStatus: SUB_STATUS.TRIAL,
    joinedAt: new Date().toISOString()
  });

  const starterItems = [
    {
      id: `item_${restId}_1`,
      restaurantId: restId,
      name: `Signature ${newRestaurant.name} Special`,
      description: "Chef's recommended special, prepared fresh with the finest ingredients.",
      price: 260,
      category: 'Bestsellers',
      imageUrl: 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500&auto=format&fit=crop&q=80',
      isVeg: !!isVegOnly,
      isAvailable: true,
      isBestSeller: true,
      spicyLevel: 1,
      preparationTimeMinutes: 20
    },
    {
      id: `item_${restId}_2`,
      restaurantId: restId,
      name: 'Crispy Starter Delight',
      description: 'Crunchy appetizing platter served with house dips and garnish.',
      price: 180,
      category: 'Starters',
      imageUrl: 'https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=500&auto=format&fit=crop&q=80',
      isVeg: true,
      isAvailable: true,
      isBestSeller: false,
      spicyLevel: 1,
      preparationTimeMinutes: 15
    }
  ];

  state.menuItems.push(...starterItems);
  state.restaurants.unshift(newRestaurant);
  save();

  io.emit('restaurant:new', newRestaurant);
  broadcastRestaurants();

  console.log(`[api] restaurant registered: ${newRestaurant.name} (${newRestaurant.id})`);
  res.status(201).json({
    success: true,
    data: newRestaurant,
    menuItems: starterItems,
    subscription: store.subscriptionView(newRestaurant)
  });
});

// The Partner app calls this on every launch so it shows its OWN restaurant,
// menu, orders and subscription rather than built-in demo data.
app.get('/api/partner/profile/:id', (req, res) => {
  const rest = findRestaurant(req.params.id);
  if (!rest) return res.status(404).json({ success: false, message: 'Restaurant not found' });
  res.json({
    success: true,
    data: {
      restaurant: rest,
      menuItems: menuFor(rest.id),
      subscription: store.subscriptionView(rest),
      orders: state.orders
        .flatMap(o => o.subOrders.map(s => ({ ...s, parentOrderId: o.orderId, customerName: o.userName, customerPhone: o.userPhone, deliveryAddress: o.deliveryAddress, paymentMethod: o.paymentMethod, createdAt: o.createdAt })))
        .filter(s => s.restaurantId === rest.id)
    }
  });
});

// ---------------------------------------------------------------------------
// Partner — menu management
// ---------------------------------------------------------------------------

app.get('/api/partner/menu/:restaurantId', (req, res) => {
  res.json({ success: true, data: menuFor(req.params.restaurantId) });
});

app.post('/api/partner/menu', (req, res) => {
  const { restaurantId, name, description, price, category, imageUrl, isVeg, spicyLevel, preparationTimeMinutes } = req.body;
  if (!restaurantId || !findRestaurant(restaurantId)) {
    return res.status(400).json({ success: false, message: 'A valid restaurantId is required' });
  }
  if (!name || price === undefined) {
    return res.status(400).json({ success: false, message: 'name and price are required' });
  }

  const newItem = {
    id: uid('item'),
    restaurantId,
    name,
    description: description || '',
    price: Number(price),
    category: category || 'Specials',
    imageUrl: imageUrl || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500&auto=format&fit=crop&q=80',
    isVeg: Boolean(isVeg),
    isAvailable: true,
    isBestSeller: false,
    spicyLevel: Number(spicyLevel) || 1,
    preparationTimeMinutes: Number(preparationTimeMinutes) || 20
  };

  state.menuItems.push(newItem);
  save();

  io.emit('menu:updated', { restaurantId, item: newItem, action: 'added' });
  res.status(201).json({ success: true, data: newItem });
});

app.patch('/api/partner/menu/:itemId', (req, res) => {
  const item = state.menuItems.find(m => m.id === req.params.itemId);
  if (!item) return res.status(404).json({ success: false, message: 'Menu item not found' });

  const editable = ['name', 'description', 'category', 'imageUrl'];
  editable.forEach(k => { if (req.body[k] !== undefined) item[k] = req.body[k]; });
  if (req.body.price !== undefined) item.price = Number(req.body.price);
  if (req.body.isVeg !== undefined) item.isVeg = Boolean(req.body.isVeg);
  if (req.body.isAvailable !== undefined) item.isAvailable = Boolean(req.body.isAvailable);
  if (req.body.isBestSeller !== undefined) item.isBestSeller = Boolean(req.body.isBestSeller);
  if (req.body.spicyLevel !== undefined) item.spicyLevel = Number(req.body.spicyLevel);
  if (req.body.preparationTimeMinutes !== undefined) item.preparationTimeMinutes = Number(req.body.preparationTimeMinutes);
  save();

  io.emit('menu:updated', { restaurantId: item.restaurantId, item, action: 'updated' });
  res.json({ success: true, data: item });
});

app.patch('/api/partner/menu/:itemId/toggle-stock', (req, res) => {
  const item = state.menuItems.find(m => m.id === req.params.itemId);
  if (!item) return res.status(404).json({ success: false, message: 'Menu item not found' });

  item.isAvailable = req.body && req.body.isAvailable !== undefined
    ? Boolean(req.body.isAvailable)
    : !item.isAvailable;
  save();

  io.emit('menu:stock', { itemId: item.id, restaurantId: item.restaurantId, isAvailable: item.isAvailable });
  io.emit('menu:updated', { restaurantId: item.restaurantId, item, action: 'stock' });
  res.json({ success: true, data: item });
});

app.delete('/api/partner/menu/:itemId', (req, res) => {
  const idx = state.menuItems.findIndex(m => m.id === req.params.itemId);
  if (idx < 0) return res.status(404).json({ success: false, message: 'Menu item not found' });

  const [removed] = state.menuItems.splice(idx, 1);
  save();

  io.emit('menu:updated', { restaurantId: removed.restaurantId, item: removed, action: 'deleted' });
  res.json({ success: true, data: removed });
});

// ---------------------------------------------------------------------------
// Partner — store settings
// ---------------------------------------------------------------------------

app.patch('/api/partner/restaurant/:id/settings', (req, res) => {
  const rest = findRestaurant(req.params.id);
  if (!rest) return res.status(404).json({ success: false, message: 'Restaurant not found' });

  const { deliveryRadiusKm, minOrderValue, isOpen, name, description, phone, address, upiId, discountOffer } = req.body;
  if (deliveryRadiusKm !== undefined) rest.deliveryRadiusKm = Number(deliveryRadiusKm);
  if (minOrderValue !== undefined) rest.minOrderValue = Number(minOrderValue);
  if (isOpen !== undefined) rest.isOpen = Boolean(isOpen);
  if (name) rest.name = name;
  if (description) rest.description = description;
  if (phone) rest.phone = phone;
  if (address) rest.address = address;
  if (upiId) rest.upiId = upiId;
  if (discountOffer) rest.discountOffer = discountOffer;
  save();

  io.emit('restaurant:settings', rest);
  broadcastRestaurants();
  res.json({ success: true, data: rest });
});

// ---------------------------------------------------------------------------
// Orders
// ---------------------------------------------------------------------------

function normaliseItem(raw) {
  const src = raw.menuItem || raw;
  const quantity = Number(raw.quantity) || 1;
  const price = Number(src.price) || 0;
  return {
    menuItemId: src.id || src.menuItemId || '',
    name: src.name || 'Item',
    description: src.description || '',
    imageUrl: src.imageUrl || '',
    isVeg: Boolean(src.isVeg),
    price,
    quantity,
    totalPrice: raw.totalPrice !== undefined ? Number(raw.totalPrice) : price * quantity,
    specialNotes: raw.specialNotes || ''
  };
}

app.post('/api/orders', (req, res) => {
  const { userId, userName, userPhone, deliveryAddress, paymentMethod, deliveryInstructions, subOrders } = req.body;

  if (!Array.isArray(subOrders) || subOrders.length === 0) {
    return res.status(400).json({ success: false, message: 'Order must contain at least one sub-order' });
  }

  // Validate every restaurant BEFORE creating anything, so a rejected cart
  // never leaves half an order behind.
  for (const so of subOrders) {
    const rest = findRestaurant(so.restaurantId);
    if (!rest) {
      return res.status(404).json({ success: false, message: `Restaurant ${so.restaurantId} no longer exists` });
    }
    if (!store.isListed(rest)) {
      return res.status(409).json({ success: false, message: `${rest.name} is currently unavailable (subscription suspended)` });
    }
    if (!rest.isOpen) {
      return res.status(409).json({ success: false, message: `${rest.name} is closed right now` });
    }
    const subTotal = Number(so.subTotal) || 0;
    if (subTotal < rest.minOrderValue) {
      return res.status(409).json({
        success: false,
        message: `${rest.name} has a minimum order of ₹${rest.minOrderValue}. Add ₹${(rest.minOrderValue - subTotal).toFixed(0)} more.`
      });
    }
  }

  const orderId = store.nextOrderId();
  const createdAt = new Date().toISOString();

  const createdSubOrders = subOrders.map((so, idx) => {
    const rest = findRestaurant(so.restaurantId);
    return {
      // Globally unique: derived from the order id, so two customers ordering
      // at the same moment can never share a sub-order id.
      subOrderId: `${orderId}-S${idx + 1}`,
      parentOrderId: orderId,
      restaurantId: rest.id,
      restaurantName: rest.name,
      restaurantPhone: rest.phone,
      status: 'PLACED',
      statusHistory: [{ status: 'PLACED', at: createdAt, note: `Order sent to ${rest.name}` }],
      estimatedDeliveryMinutes: rest.deliveryTimeMinutes || 25,
      estimatedPrepMinutes: 20,
      driverName: 'Pending assignment',
      driverPhone: '',
      items: (so.items || []).map(normaliseItem),
      subTotal: Number(so.subTotal) || 0,
      deliveryFee: Number(so.deliveryFee) || 0,
      discount: Number(so.discount) || 0,
      specialInstructions: so.specialInstructions || '',
      isRated: false,
      ratingScore: 0
    };
  });

  const itemsTotal = createdSubOrders.reduce((s, so) => s + so.subTotal, 0);
  const totalDeliveryFee = createdSubOrders.reduce((s, so) => s + so.deliveryFee, 0);
  const totalDiscount = createdSubOrders.reduce((s, so) => s + so.discount, 0);
  const taxesAndFees = req.body.taxesAndFees !== undefined
    ? Number(req.body.taxesAndFees)
    : Number((itemsTotal * 0.05 + createdSubOrders.length * 15).toFixed(2));

  const newOrder = {
    orderId,
    userId: userId || 'user_101',
    userName: userName || 'Customer',
    userPhone: userPhone || '',
    deliveryAddress: deliveryAddress || '',
    deliveryInstructions: deliveryInstructions || '',
    paymentMethod: paymentMethod || 'Online UPI',
    paymentStatus: 'PAID',
    transactionId: `TXN-${crypto.randomUUID().split('-')[0].toUpperCase()}`,
    itemsTotal,
    totalDeliveryFee,
    taxesAndFees,
    totalDiscount,
    totalPaid: Number((itemsTotal + totalDeliveryFee + taxesAndFees - totalDiscount).toFixed(2)),
    createdAt,
    subOrders: createdSubOrders
  };

  state.orders.unshift(newOrder);
  save();

  // Ring only the restaurant that owns each sub-order, plus the admin monitor.
  createdSubOrders.forEach(so => {
    const payload = {
      orderId,
      subOrder: so,
      customerName: newOrder.userName,
      customerPhone: newOrder.userPhone,
      deliveryAddress: newOrder.deliveryAddress,
      paymentMethod: newOrder.paymentMethod,
      createdAt
    };
    io.to(roomRestaurant(so.restaurantId)).emit('order:new', payload);
    io.to(ROOM_ADMIN).emit('order:new', payload);
  });

  console.log(`[api] order ${orderId} placed — ${createdSubOrders.length} sub-order(s)`);
  res.status(201).json({ success: true, data: newOrder });
});

app.get('/api/orders', (req, res) => {
  res.json({ success: true, data: state.orders });
});

app.get('/api/orders/customer/:userId', (req, res) => {
  res.json({ success: true, data: state.orders.filter(o => o.userId === req.params.userId) });
});

app.get('/api/orders/:orderId', (req, res) => {
  const order = state.orders.find(o => o.orderId === req.params.orderId);
  if (!order) return res.status(404).json({ success: false, message: 'Order not found' });
  res.json({ success: true, data: order });
});

// Every sub-order for one restaurant — the Partner order dashboard.
app.get('/api/partner/orders/:restaurantId', (req, res) => {
  const rows = state.orders.flatMap(o =>
    o.subOrders
      .filter(s => s.restaurantId === req.params.restaurantId)
      .map(s => ({
        ...s,
        parentOrderId: o.orderId,
        customerName: o.userName,
        customerPhone: o.userPhone,
        deliveryAddress: o.deliveryAddress,
        paymentMethod: o.paymentMethod,
        paymentStatus: o.paymentStatus,
        createdAt: o.createdAt
      }))
  );
  res.json({ success: true, data: rows });
});

app.patch('/api/orders/sub-order/:subOrderId/status', (req, res) => {
  const { status, driverName, driverPhone, estimatedPrepMinutes, estimatedDeliveryMinutes, note } = req.body;
  if (!ORDER_STATUS.includes(status)) {
    return res.status(400).json({ success: false, message: `status must be one of ${ORDER_STATUS.join(', ')}` });
  }

  const { order, sub } = findSubOrder(req.params.subOrderId);
  if (!sub) return res.status(404).json({ success: false, message: 'Sub-order not found' });

  sub.status = status;
  sub.statusHistory = [...(sub.statusHistory || []), { status, at: new Date().toISOString(), note: note || `Status updated to ${status}` }];
  if (driverName) sub.driverName = driverName;
  if (driverPhone) sub.driverPhone = driverPhone;
  if (estimatedPrepMinutes) sub.estimatedPrepMinutes = Number(estimatedPrepMinutes);
  if (estimatedDeliveryMinutes) sub.estimatedDeliveryMinutes = Number(estimatedDeliveryMinutes);
  save();

  const payload = {
    orderId: order.orderId,
    subOrderId: sub.subOrderId,
    restaurantId: sub.restaurantId,
    status: sub.status,
    statusHistory: sub.statusHistory,
    driverName: sub.driverName,
    estimatedDeliveryMinutes: sub.estimatedDeliveryMinutes
  };
  io.to(roomCustomer(order.userId)).emit('order:status', payload);
  io.to(roomRestaurant(sub.restaurantId)).emit('order:status', payload);
  io.to(ROOM_ADMIN).emit('order:status', payload);

  res.json({ success: true, data: sub });
});

// ---------------------------------------------------------------------------
// Chat (per sub-order, both directions)
// ---------------------------------------------------------------------------

app.get('/api/chat/:subOrderId', (req, res) => {
  res.json({ success: true, data: state.chatMessages.filter(m => m.subOrderId === req.params.subOrderId) });
});

app.post('/api/chat/:subOrderId', (req, res) => {
  const { subOrderId } = req.params;
  const { senderName, isFromCustomer, text } = req.body;
  if (!text || !String(text).trim()) {
    return res.status(400).json({ success: false, message: 'Message text is required' });
  }

  const now = new Date();
  const newMsg = {
    id: uid('msg'),
    subOrderId,
    senderName: senderName || (isFromCustomer ? 'Customer' : 'Restaurant'),
    isFromCustomer: Boolean(isFromCustomer),
    text: String(text),
    timestamp: `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`,
    sentAt: now.toISOString()
  };

  state.chatMessages.push(newMsg);
  save();

  const { order, sub } = findSubOrder(subOrderId);
  io.to(roomSubOrder(subOrderId)).emit('chat:new', newMsg);
  if (sub) io.to(roomRestaurant(sub.restaurantId)).emit('chat:new', newMsg);
  if (order) io.to(roomCustomer(order.userId)).emit('chat:new', newMsg);

  res.status(201).json({ success: true, data: newMsg });
});

// ---------------------------------------------------------------------------
// Reviews
// ---------------------------------------------------------------------------

app.get('/api/reviews/:restaurantId', (req, res) => {
  res.json({ success: true, data: state.reviews.filter(r => r.restaurantId === req.params.restaurantId) });
});

app.post('/api/reviews', (req, res) => {
  const { restaurantId, subOrderId, customerId, customerName, rating, comment, orderedDishes } = req.body;
  const rest = findRestaurant(restaurantId);
  if (!rest) return res.status(404).json({ success: false, message: 'Restaurant not found' });

  const score = Number(rating);
  if (!(score >= 1 && score <= 5)) {
    return res.status(400).json({ success: false, message: 'rating must be between 1 and 5' });
  }

  const review = {
    id: uid('rev'),
    restaurantId,
    subOrderId: subOrderId || '',
    customerId: customerId || 'user_101',
    customerName: customerName || 'Customer',
    rating: score,
    comment: comment || '',
    orderedDishes: Array.isArray(orderedDishes) ? orderedDishes : [],
    createdAt: new Date().toISOString()
  };
  state.reviews.unshift(review);

  // Recompute the restaurant's running average.
  rest.ratingSum = (rest.ratingSum || 0) + score;
  rest.totalRatings = (rest.totalRatings || 0) + 1;
  rest.rating = Number((rest.ratingSum / rest.totalRatings).toFixed(2));

  if (subOrderId) {
    const { sub } = findSubOrder(subOrderId);
    if (sub) {
      sub.isRated = true;
      sub.ratingScore = score;
    }
  }
  save();

  io.emit('review:new', { restaurantId, review, rating: rest.rating, totalRatings: rest.totalRatings });
  broadcastRestaurants();
  res.status(201).json({ success: true, data: review, restaurant: rest });
});

// ---------------------------------------------------------------------------
// Subscriptions
// ---------------------------------------------------------------------------

app.get('/api/partner/subscription/:restaurantId', (req, res) => {
  const rest = findRestaurant(req.params.restaurantId);
  if (!rest) return res.status(404).json({ success: false, message: 'Restaurant not found' });
  res.json({ success: true, data: store.subscriptionView(rest) });
});

app.post('/api/partner/subscription/:restaurantId/activate', (req, res) => {
  const rest = findRestaurant(req.params.restaurantId);
  if (!rest) return res.status(404).json({ success: false, message: 'Restaurant not found' });

  store.activatePaidPlan(rest);
  const view = store.subscriptionView(rest);

  io.to(roomRestaurant(rest.id)).emit('subscription:updated', view);
  io.to(ROOM_ADMIN).emit('subscription:updated', { restaurantId: rest.id, ...view });
  broadcastRestaurants();

  res.json({ success: true, data: view });
});

// ---------------------------------------------------------------------------
// Admin
// ---------------------------------------------------------------------------

app.get('/api/admin/restaurants', (req, res) => {
  const rows = state.restaurants.map(r => ({
    ...r,
    subscription: store.subscriptionView(r),
    menuItemCount: menuFor(r.id).length,
    totalOrders: state.orders.reduce((n, o) => n + o.subOrders.filter(s => s.restaurantId === r.id).length, 0)
  }));
  res.json({ success: true, data: rows });
});

app.patch('/api/admin/restaurants/:id/subscription', (req, res) => {
  const rest = findRestaurant(req.params.id);
  if (!rest) return res.status(404).json({ success: false, message: 'Restaurant not found' });

  const updated = store.setSubscriptionStatus(rest, req.body.status, req.body.reason);
  if (!updated) {
    return res.status(400).json({ success: false, message: `status must be one of ${Object.values(SUB_STATUS).join(', ')}` });
  }

  const view = store.subscriptionView(rest);
  io.to(roomRestaurant(rest.id)).emit('subscription:updated', view);
  io.to(ROOM_ADMIN).emit('subscription:updated', { restaurantId: rest.id, ...view });
  broadcastRestaurants(); // suspended restaurants drop out of the customer feed

  console.log(`[api] subscription for ${rest.name} set to ${rest.subscriptionStatus}`);
  res.json({ success: true, data: { restaurant: rest, subscription: view } });
});

app.get('/api/admin/stats', (req, res) => {
  const byStatus = s => state.restaurants.filter(r => r.subscriptionStatus === s).length;
  const activePaid = byStatus(SUB_STATUS.PAID);
  const trials = byStatus(SUB_STATUS.TRIAL);
  const todayKey = new Date().toISOString().slice(0, 10);

  const allSubOrders = state.orders.flatMap(o => o.subOrders);
  const todayOrders = state.orders.filter(o => o.createdAt.slice(0, 10) === todayKey);

  res.json({
    success: true,
    data: {
      totalRestaurants: state.restaurants.length,
      activePaid,
      activeTrials: trials,
      overdue: byStatus(SUB_STATUS.OVERDUE),
      suspended: byStatus(SUB_STATUS.SUSPENDED),
      currentMRR: activePaid * store.MONTHLY_PRICE,
      projectedMRR: (activePaid + trials) * store.MONTHLY_PRICE,
      monthlyPrice: store.MONTHLY_PRICE,
      planName: store.PLAN_NAME,
      trialDays: store.TRIAL_DAYS,
      graceDays: store.GRACE_DAYS,
      totalOrders: state.orders.length,
      totalSubOrders: allSubOrders.length,
      ordersToday: todayOrders.length,
      gmvToday: Number(todayOrders.reduce((s, o) => s + o.totalPaid, 0).toFixed(2)),
      liveSubOrders: allSubOrders.filter(s => ['PLACED', 'ACCEPTED', 'PREPARING', 'OUT_FOR_DELIVERY'].includes(s.status)).length,
      deliveredToday: allSubOrders.filter(s => s.status === 'DELIVERED').length,
      totalReviews: state.reviews.length
    }
  });
});

// ---------------------------------------------------------------------------
// Partner analytics (computed from real orders)
// ---------------------------------------------------------------------------

app.get('/api/partner/analytics/:restaurantId', (req, res) => {
  const restId = req.params.restaurantId;
  const rows = state.orders.flatMap(o =>
    o.subOrders.filter(s => s.restaurantId === restId).map(s => ({ ...s, createdAt: o.createdAt }))
  );
  const paid = rows.filter(s => s.status !== 'REJECTED' && s.status !== 'CANCELLED');
  const todayKey = new Date().toISOString().slice(0, 10);
  const today = paid.filter(s => s.createdAt.slice(0, 10) === todayKey);

  const itemTally = {};
  paid.forEach(s => s.items.forEach(i => { itemTally[i.name] = (itemTally[i.name] || 0) + i.quantity; }));
  const topSellingItems = Object.entries(itemTally).sort((a, b) => b[1] - a[1]).slice(0, 5)
    .map(([name, count]) => ({ name, count }));

  const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  const weeklyRevenueTrend = [];
  for (let i = 6; i >= 0; i--) {
    const d = new Date(Date.now() - i * 86400000);
    const key = d.toISOString().slice(0, 10);
    const revenue = paid.filter(s => s.createdAt.slice(0, 10) === key)
      .reduce((sum, s) => sum + s.subTotal + s.deliveryFee - s.discount, 0);
    weeklyRevenueTrend.push({ day: dayNames[d.getDay()], revenue: Number(revenue.toFixed(2)) });
  }

  res.json({
    success: true,
    data: {
      todayRevenue: Number(today.reduce((sum, s) => sum + s.subTotal + s.deliveryFee - s.discount, 0).toFixed(2)),
      todayOrdersCount: today.length,
      lifetimeOrdersCount: paid.length,
      lifetimeRevenue: Number(paid.reduce((sum, s) => sum + s.subTotal + s.deliveryFee - s.discount, 0).toFixed(2)),
      avgPrepTimeMinutes: paid.length
        ? Math.round(paid.reduce((sum, s) => sum + (s.estimatedPrepMinutes || 20), 0) / paid.length)
        : 20,
      topSellingItems,
      weeklyRevenueTrend
    }
  });
});

// ---------------------------------------------------------------------------
// Static admin panel (when deployed as one service)
// ---------------------------------------------------------------------------

const distCandidates = [
  path.join(__dirname, '../admin-panel/dist'),
  path.join(__dirname, 'dist')
];
const distPath = distCandidates.find(p => fs.existsSync(p));

if (distPath) {
  app.use(express.static(distPath));
  app.use((req, res, next) => {
    if (req.path.startsWith('/api') || req.path === '/health') return next();
    res.sendFile(path.join(distPath, 'index.html'));
  });
} else {
  app.get('/', (req, res) => {
    res.json({
      service: 'OrderAra Backend',
      status: 'active',
      endpoints: ['/api/restaurants', '/api/orders', '/api/admin/stats', '/health']
    });
  });
}

// ---------------------------------------------------------------------------
// Realtime — each client joins only the rooms it needs
// ---------------------------------------------------------------------------

io.on('connection', (socket) => {
  socket.on('join', (payload = {}) => {
    const { role, restaurantId, userId, subOrderId } = payload;
    if (role === 'partner' && restaurantId) socket.join(roomRestaurant(restaurantId));
    if (role === 'customer' && userId) socket.join(roomCustomer(userId));
    if (role === 'admin') socket.join(ROOM_ADMIN);
    if (subOrderId) socket.join(roomSubOrder(subOrderId));
    socket.emit('joined', { role, restaurantId, userId, subOrderId });
    console.log(`[socket] ${socket.id} joined as ${role || 'guest'}${restaurantId ? ` (${restaurantId})` : ''}`);
  });

  socket.on('watch:suborder', ({ subOrderId }) => {
    if (subOrderId) socket.join(roomSubOrder(subOrderId));
  });

  socket.on('disconnect', () => {});
});

// ---------------------------------------------------------------------------
// Subscription clock
// ---------------------------------------------------------------------------

function runSubscriptionSweep() {
  const changed = store.sweepSubscriptions();
  if (changed.length) {
    changed.forEach(rest => {
      const view = store.subscriptionView(rest);
      io.to(roomRestaurant(rest.id)).emit('subscription:updated', view);
      io.to(ROOM_ADMIN).emit('subscription:updated', { restaurantId: rest.id, ...view });
      console.log(`[subscriptions] ${rest.name} -> ${rest.subscriptionStatus}`);
    });
    broadcastRestaurants();
  }
}

runSubscriptionSweep();
setInterval(runSubscriptionSweep, 60 * 60 * 1000);

const PORT = process.env.PORT || 8080;
server.listen(PORT, '0.0.0.0', () => {
  console.log(`OrderAra backend listening on ${PORT} (data: ${store.DATA_FILE})`);
  console.log(describeAuthMode());
});
