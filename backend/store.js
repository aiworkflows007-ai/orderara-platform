/**
 * OrderAra shared data store.
 *
 * Holds every entity the three apps share (restaurants, menus, orders, chat,
 * reviews) and persists it to a JSON file so a server restart or redeploy does
 * not wipe registered restaurants, live orders or subscription records.
 */

const fs = require('fs');
const path = require('path');

const DATA_FILE = process.env.DATA_FILE || path.join(__dirname, 'data.json');

const TRIAL_DAYS = Number(process.env.TRIAL_DAYS || 14);
const GRACE_DAYS = Number(process.env.GRACE_DAYS || 3);
const MONTHLY_PRICE = Number(process.env.MONTHLY_PRICE || 999);
const PLAN_NAME = 'Restaurant Unlimited Partner Plan';

const DAY_MS = 24 * 60 * 60 * 1000;

const SUB_STATUS = {
  TRIAL: 'ACTIVE_TRIAL',
  PAID: 'ACTIVE_PAID',
  OVERDUE: 'OVERDUE',
  SUSPENDED: 'SUSPENDED'
};

const ORDER_STATUS = [
  'PLACED',
  'ACCEPTED',
  'PREPARING',
  'OUT_FOR_DELIVERY',
  'DELIVERED',
  'REJECTED',
  'CANCELLED'
];

// ---------------------------------------------------------------------------
// Seed data (first boot only — after that data.json wins)
// ---------------------------------------------------------------------------

function seedRestaurants() {
  const now = Date.now();
  const base = [
    {
      id: 'rest_1',
      name: 'Royal Biryani House',
      ownerName: 'Farhan Khan',
      description: 'Authentic Dum Biryani, Mughlai Gravies & Charcoal Kebabs',
      bannerUrl: 'https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=800&auto=format&fit=crop&q=80',
      rating: 4.6,
      totalRatings: 1420,
      deliveryTimeMinutes: 25,
      distanceKm: 2.4,
      deliveryRadiusKm: 7.0,
      minOrderValue: 199.0,
      isVegOnly: false,
      isPromoted: true,
      discountOffer: '50% OFF up to ₹100',
      cuisineTypes: ['Biryani', 'Mughlai', 'North Indian', 'Kebabs'],
      isOpen: true,
      phone: '+91 98450 11223',
      email: 'owner@royalbiryani.com',
      address: 'Indiranagar 100ft Road, Bangalore',
      upiId: 'royalbiryani@okhdfcbank',
      subscriptionStatus: SUB_STATUS.TRIAL,
      joinedAt: new Date(now - 4 * DAY_MS).toISOString()
    },
    {
      id: 'rest_2',
      name: 'Pizza Milano & Crust',
      ownerName: 'Marco Rossi',
      description: 'Hand-tossed Woodfired Neapolitan Pizzas & Pastas',
      bannerUrl: 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800&auto=format&fit=crop&q=80',
      rating: 4.5,
      totalRatings: 980,
      deliveryTimeMinutes: 20,
      distanceKm: 1.8,
      deliveryRadiusKm: 6.0,
      minOrderValue: 249.0,
      isVegOnly: false,
      isPromoted: false,
      discountOffer: 'Flat ₹75 OFF above ₹399',
      cuisineTypes: ['Pizza', 'Italian', 'Pastas', 'Desserts'],
      isOpen: true,
      phone: '+91 98450 22334',
      email: 'manager@pizzamilano.com',
      address: '12th Main Road, HAL 2nd Stage, Bangalore',
      upiId: 'pizzamilano@okicici',
      subscriptionStatus: SUB_STATUS.PAID,
      joinedAt: new Date(now - 40 * DAY_MS).toISOString()
    },
    {
      id: 'rest_3',
      name: 'Udupi Sri Krishna Sagar',
      ownerName: 'Venkatesh Rao',
      description: 'Authentic South Indian Tiffins, Filter Coffee & Thalis',
      bannerUrl: 'https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=800&auto=format&fit=crop&q=80',
      rating: 4.7,
      totalRatings: 3100,
      deliveryTimeMinutes: 18,
      distanceKm: 1.2,
      deliveryRadiusKm: 5.0,
      minOrderValue: 120.0,
      isVegOnly: true,
      isPromoted: false,
      discountOffer: '20% OFF above ₹150',
      cuisineTypes: ['South Indian', 'Pure Veg', 'Breakfast', 'Thali'],
      isOpen: true,
      phone: '+91 98450 33445',
      email: 'contact@udupisagar.com',
      address: 'CMH Road, Indiranagar, Bangalore',
      upiId: 'udupisagar@okaxis',
      subscriptionStatus: SUB_STATUS.PAID,
      joinedAt: new Date(now - 80 * DAY_MS).toISOString()
    },
    {
      id: 'rest_4',
      name: 'The Burger Garage',
      ownerName: 'Siddharth Sen',
      description: 'Smash Burgers, Loaded Fries & Thick Shakes',
      bannerUrl: 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800&auto=format&fit=crop&q=80',
      rating: 4.4,
      totalRatings: 850,
      deliveryTimeMinutes: 22,
      distanceKm: 3.1,
      deliveryRadiusKm: 8.0,
      minOrderValue: 180.0,
      isVegOnly: false,
      isPromoted: true,
      discountOffer: 'Free Fries with any Burger',
      cuisineTypes: ['Burgers', 'Fast Food', 'Fries', 'Shakes'],
      isOpen: true,
      phone: '+91 98450 44556',
      email: 'hello@burgergarage.com',
      address: '80ft Road, Koramangala 4th Block, Bangalore',
      upiId: 'burgergarage@okhdfcbank',
      subscriptionStatus: SUB_STATUS.TRIAL,
      joinedAt: new Date(now - 11 * DAY_MS).toISOString()
    }
  ];

  return base.map(r => withSubscriptionDefaults(r));
}

function seedMenuItems() {
  const img = u => `https://images.unsplash.com/${u}?w=500&auto=format&fit=crop&q=80`;
  return [
    // Royal Biryani House
    { id: 'item_101', restaurantId: 'rest_1', name: 'Hyderabadi Chicken Dum Biryani', description: 'Fragrant long-grain basmati rice layered with spiced tender chicken and saffron, served with mirchi ka salan and raita.', price: 320, category: 'Biryani Specials', imageUrl: img('photo-1563379091339-03b21ab4a4f8'), isVeg: false, isAvailable: true, isBestSeller: true, spicyLevel: 2, preparationTimeMinutes: 20 },
    { id: 'item_102', restaurantId: 'rest_1', name: 'Royal Paneer Tikka Biryani', description: 'Marinated grilled paneer cubes infused with rich spices and dum cooked with aromatic rice.', price: 270, category: 'Biryani Specials', imageUrl: img('photo-1589302168068-964664d93dc0'), isVeg: true, isAvailable: true, isBestSeller: false, spicyLevel: 1, preparationTimeMinutes: 18 },
    { id: 'item_103', restaurantId: 'rest_1', name: 'Murgh Tangdi Kebab (4 Pcs)', description: 'Succulent chicken drumsticks marinated in cream, roasted spices, and cooked to perfection in tandoor.', price: 290, category: 'Starters & Kebabs', imageUrl: img('photo-1599488615731-7e5c2823ff28'), isVeg: false, isAvailable: true, isBestSeller: true, spicyLevel: 2, preparationTimeMinutes: 22 },
    { id: 'item_104', restaurantId: 'rest_1', name: 'Butter Naan & Dal Makhani Combo', description: 'Two soft butter naans served with slow-cooked creamy black lentils topped with white butter.', price: 220, category: 'Mains & Combos', imageUrl: img('photo-1546833999-b9f581a1996d'), isVeg: true, isAvailable: true, isBestSeller: false, spicyLevel: 1, preparationTimeMinutes: 15 },
    { id: 'item_105', restaurantId: 'rest_1', name: 'Gulab Jamun (2 Pcs)', description: 'Warm golden milk dumplings soaked in cardamom flavored sugar syrup.', price: 70, category: 'Desserts', imageUrl: img('photo-1593701461250-d7b22dfd3a77'), isVeg: true, isAvailable: true, isBestSeller: false, spicyLevel: 0, preparationTimeMinutes: 5 },

    // Pizza Milano & Crust
    { id: 'item_201', restaurantId: 'rest_2', name: 'Margherita Gourmet Pizza (10")', description: 'Classic San Marzano tomato sauce, fresh buffalo mozzarella, fresh basil, and extra virgin olive oil.', price: 299, category: 'Woodfired Pizzas', imageUrl: img('photo-1574071318508-1cdbab80d002'), isVeg: true, isAvailable: true, isBestSeller: true, spicyLevel: 0, preparationTimeMinutes: 18 },
    { id: 'item_202', restaurantId: 'rest_2', name: 'Smoky BBQ Chicken Pizza (10")', description: 'Grilled BBQ chicken breast, red onions, pickled jalapenos, mozzarella, and smoky barbecue glaze.', price: 389, category: 'Woodfired Pizzas', imageUrl: img('photo-1565299624946-b28f40a0ae38'), isVeg: false, isAvailable: true, isBestSeller: true, spicyLevel: 2, preparationTimeMinutes: 20 },
    { id: 'item_203', restaurantId: 'rest_2', name: 'Creamy Alfredo Penne Pasta', description: 'Penne in rich parmesan cream sauce with sauteed mushrooms and roasted bell peppers.', price: 280, category: 'Pasta & Sides', imageUrl: img('photo-1621996346565-e3d5d6281084'), isVeg: true, isAvailable: true, isBestSeller: false, spicyLevel: 0, preparationTimeMinutes: 16 },
    { id: 'item_204', restaurantId: 'rest_2', name: 'Cheesy Garlic Stuffed Bread', description: 'Freshly baked artisan bread loaf stuffed with melted mozzarella, roasted garlic butter, and herbs.', price: 160, category: 'Pasta & Sides', imageUrl: img('photo-1619895092538-128341789043'), isVeg: true, isAvailable: true, isBestSeller: true, spicyLevel: 1, preparationTimeMinutes: 12 },

    // Udupi Sri Krishna Sagar
    { id: 'item_301', restaurantId: 'rest_3', name: 'Special Masala Ghee Roast Dosa', description: 'Crispy golden crepe roasted in pure desi ghee, filled with seasoned spiced potato mash and served with 3 chutneys & sambar.', price: 110, category: 'Dosas & Idlis', imageUrl: img('photo-1589301760014-d929f3979dbc'), isVeg: true, isAvailable: true, isBestSeller: true, spicyLevel: 1, preparationTimeMinutes: 12 },
    { id: 'item_302', restaurantId: 'rest_3', name: 'Steamed Button Idli (4 Pcs) + Medu Vada (1 Pc)', description: 'Melt-in-mouth steamed rice cakes and crisp lentil donut, served hot with piping sambar.', price: 95, category: 'Dosas & Idlis', imageUrl: img('photo-1589301760014-d929f3979dbc'), isVeg: true, isAvailable: true, isBestSeller: true, spicyLevel: 0, preparationTimeMinutes: 10 },
    { id: 'item_303', restaurantId: 'rest_3', name: 'South Indian Mini Executive Meals', description: 'Steamed rice, sambar, rasam, kootu, curd, papad, pickle, and payasam dessert.', price: 160, category: 'Thali & Meals', imageUrl: img('photo-1610057099443-fde8c4d50f91'), isVeg: true, isAvailable: true, isBestSeller: false, spicyLevel: 1, preparationTimeMinutes: 15 },
    { id: 'item_304', restaurantId: 'rest_3', name: 'Authentic Kumbakonam Degree Coffee', description: 'Rich freshly brewed chicory-infused filter coffee with frothed full-cream milk.', price: 45, category: 'Beverages', imageUrl: img('photo-1514432324607-a09d9b4aefdd'), isVeg: true, isAvailable: true, isBestSeller: true, spicyLevel: 0, preparationTimeMinutes: 5 },

    // The Burger Garage
    { id: 'item_401', restaurantId: 'rest_4', name: 'Double Smashed Cheeseburger', description: 'Two juicy grilled patties, double cheddar cheese, caramelized onions, secret sauce on brioche bun.', price: 249, category: 'Signature Burgers', imageUrl: img('photo-1568901346375-23c9450c58cd'), isVeg: false, isAvailable: true, isBestSeller: true, spicyLevel: 1, preparationTimeMinutes: 14 },
    { id: 'item_402', restaurantId: 'rest_4', name: 'Crispy Peri-Peri Paneer Burger', description: 'Crunchy spiced paneer patty, lettuce, jalapeño mayo, and peri-peri seasoning.', price: 199, category: 'Signature Burgers', imageUrl: img('photo-1550547660-d9450f859349'), isVeg: true, isAvailable: true, isBestSeller: false, spicyLevel: 2, preparationTimeMinutes: 14 },
    { id: 'item_403', restaurantId: 'rest_4', name: 'Loaded Cheesy Bacon Fries', description: 'Crispy golden french fries drizzled with warm cheddar sauce, crispy bits, and spring onions.', price: 179, category: 'Fries & Sides', imageUrl: img('photo-1586190848861-99aa4a171e90'), isVeg: false, isAvailable: true, isBestSeller: true, spicyLevel: 1, preparationTimeMinutes: 10 }
  ];
}

function seed() {
  return {
    restaurants: seedRestaurants(),
    menuItems: seedMenuItems(),
    orders: [],
    chatMessages: [],
    reviews: [],
    counters: { order: 9842 }
  };
}

// ---------------------------------------------------------------------------
// Subscription helpers
// ---------------------------------------------------------------------------

/** Fills in trial/billing fields for a restaurant that does not have them yet. */
function withSubscriptionDefaults(rest) {
  const joinedAt = rest.joinedAt || new Date().toISOString();
  const joinedMs = new Date(joinedAt).getTime();

  if (!rest.subscriptionStatus) rest.subscriptionStatus = SUB_STATUS.TRIAL;
  rest.joinedAt = joinedAt;
  rest.planName = rest.planName || PLAN_NAME;
  rest.priceMonthly = rest.priceMonthly === undefined ? MONTHLY_PRICE : rest.priceMonthly;
  rest.trialEndsAt = rest.trialEndsAt || new Date(joinedMs + TRIAL_DAYS * DAY_MS).toISOString();

  if (!rest.dueAt) {
    rest.dueAt = rest.subscriptionStatus === SUB_STATUS.PAID
      ? new Date(Date.now() + 30 * DAY_MS).toISOString()
      : rest.trialEndsAt;
  }
  if (!rest.invoices) {
    rest.invoices = [{
      id: `INV-${String(joinedMs).slice(-6)}`,
      title: `${TRIAL_DAYS}-Day Free Trial`,
      amount: '₹0.00',
      status: 'ACTIVE',
      date: joinedAt
    }];
  }
  if (rest.ratingSum === undefined) {
    rest.ratingSum = (rest.rating || 0) * (rest.totalRatings || 0);
  }
  return rest;
}

/** True when customers are allowed to see and order from this restaurant. */
function isListed(rest) {
  return rest.subscriptionStatus !== SUB_STATUS.SUSPENDED;
}

/**
 * Moves trial/paid restaurants through OVERDUE and into SUSPENDED as their due
 * dates pass. Returns the restaurants whose status actually changed.
 */
function sweepSubscriptions(now = Date.now()) {
  const changed = [];
  for (const rest of state.restaurants) {
    const before = rest.subscriptionStatus;
    const dueMs = new Date(rest.dueAt).getTime();

    if ((before === SUB_STATUS.TRIAL || before === SUB_STATUS.PAID) && now > dueMs) {
      rest.subscriptionStatus = SUB_STATUS.OVERDUE;
      rest.graceEndsAt = new Date(dueMs + GRACE_DAYS * DAY_MS).toISOString();
    } else if (before === SUB_STATUS.OVERDUE) {
      const graceMs = new Date(rest.graceEndsAt || dueMs + GRACE_DAYS * DAY_MS).getTime();
      if (now > graceMs) {
        rest.subscriptionStatus = SUB_STATUS.SUSPENDED;
        rest.suspendedReason = 'Auto-suspended: subscription payment overdue past grace period';
        rest.suspendedAt = new Date(now).toISOString();
      }
    }

    if (rest.subscriptionStatus !== before) changed.push(rest);
  }
  if (changed.length) save();
  return changed;
}

/** The subscription view the Partner app renders. */
function subscriptionView(rest) {
  const now = Date.now();
  const trialEndsMs = new Date(rest.trialEndsAt).getTime();
  const dueMs = new Date(rest.dueAt).getTime();
  return {
    restaurantId: rest.id,
    restaurantName: rest.name,
    planName: rest.planName,
    priceMonthly: rest.priceMonthly,
    status: rest.subscriptionStatus,
    isTrialActive: rest.subscriptionStatus === SUB_STATUS.TRIAL,
    trialTotalDays: TRIAL_DAYS,
    trialDaysRemaining: Math.max(0, Math.ceil((trialEndsMs - now) / DAY_MS)),
    daysUntilDue: Math.ceil((dueMs - now) / DAY_MS),
    nextBillingDate: rest.dueAt,
    graceEndsAt: rest.graceEndsAt || null,
    suspendedReason: rest.suspendedReason || null,
    invoices: rest.invoices || []
  };
}

/** Records a paid month and pushes the due date out by 30 days. */
function activatePaidPlan(rest) {
  const now = Date.now();
  rest.subscriptionStatus = SUB_STATUS.PAID;
  rest.dueAt = new Date(now + 30 * DAY_MS).toISOString();
  rest.graceEndsAt = null;
  rest.suspendedReason = null;
  rest.suspendedAt = null;
  rest.invoices = [
    {
      id: `INV-${String(now).slice(-6)}`,
      title: `${rest.planName} — 1 month`,
      amount: `₹${Number(rest.priceMonthly).toFixed(2)}`,
      status: 'PAID',
      date: new Date(now).toISOString()
    },
    ...(rest.invoices || [])
  ];
  save();
  return rest;
}

/** Admin override. Suspending hides the restaurant from customers immediately. */
function setSubscriptionStatus(rest, status, reason) {
  const now = Date.now();
  if (!Object.values(SUB_STATUS).includes(status)) return null;

  if (status === SUB_STATUS.SUSPENDED) {
    rest.suspendedReason = reason || 'Suspended by platform admin';
    rest.suspendedAt = new Date(now).toISOString();
  } else {
    rest.suspendedReason = null;
    rest.suspendedAt = null;
  }

  if (status === SUB_STATUS.PAID) {
    rest.dueAt = new Date(now + 30 * DAY_MS).toISOString();
    rest.graceEndsAt = null;
  } else if (status === SUB_STATUS.TRIAL) {
    // Reactivating into trial gives whatever trial time is genuinely left,
    // and at least one day so the restaurant is usable again.
    const remaining = Math.max(DAY_MS, new Date(rest.trialEndsAt).getTime() - now);
    rest.trialEndsAt = new Date(now + remaining).toISOString();
    rest.dueAt = rest.trialEndsAt;
    rest.graceEndsAt = null;
  } else if (status === SUB_STATUS.OVERDUE) {
    rest.graceEndsAt = new Date(now + GRACE_DAYS * DAY_MS).toISOString();
  }

  rest.subscriptionStatus = status;
  save();
  return rest;
}

// ---------------------------------------------------------------------------
// Persistence
// ---------------------------------------------------------------------------

function load() {
  try {
    if (fs.existsSync(DATA_FILE)) {
      const raw = JSON.parse(fs.readFileSync(DATA_FILE, 'utf8'));
      const merged = Object.assign(seed(), raw);
      merged.restaurants = (merged.restaurants || []).map(withSubscriptionDefaults);
      console.log(`[store] loaded ${merged.restaurants.length} restaurants from ${DATA_FILE}`);
      return merged;
    }
  } catch (err) {
    console.error(`[store] could not read ${DATA_FILE} (${err.message}) — seeding fresh`);
  }
  console.log('[store] no data file yet — seeding demo restaurants');
  return seed();
}

let state = load();

let saveTimer = null;
/** Debounced atomic write, so a burst of updates costs one disk write. */
function save() {
  if (saveTimer) return;
  saveTimer = setTimeout(() => {
    saveTimer = null;
    try {
      const tmp = `${DATA_FILE}.tmp`;
      fs.writeFileSync(tmp, JSON.stringify(state, null, 2));
      fs.renameSync(tmp, DATA_FILE);
    } catch (err) {
      console.error(`[store] save failed: ${err.message}`);
    }
  }, 200);
}

function nextOrderId() {
  state.counters.order += 1;
  return `VZ-ORD-${state.counters.order}`;
}

module.exports = {
  state,
  save,
  nextOrderId,
  isListed,
  sweepSubscriptions,
  subscriptionView,
  activatePaidPlan,
  setSubscriptionStatus,
  withSubscriptionDefaults,
  SUB_STATUS,
  ORDER_STATUS,
  TRIAL_DAYS,
  GRACE_DAYS,
  MONTHLY_PRICE,
  PLAN_NAME,
  DATA_FILE
};
