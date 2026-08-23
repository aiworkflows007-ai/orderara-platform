# OrderAra — Master Plan
### Multi-Restaurant Food Ordering & Delivery Platform (Android)

---

## 1. Product Vision

OrderAra is a single Android platform connecting **many independent restaurants** to customers, where:
- Customers browse multiple restaurants, order (even from several at once), pay online, and track order status.
- Restaurants get their own management app to run their menu, orders, and business — and pay a **flat monthly subscription** to be listed (no per-order commission).
- Delivery is handled by **each restaurant's own staff** — OrderAra does not manage riders.

Three products come out of this plan:
| Product | Platform | Users |
|---|---|---|
| **OrderAra Customer App** | Android (Kotlin) | End customers |
| **OrderAra Partner App** | Android (Kotlin) | Restaurant owners/staff |
| **OrderAra Admin Panel** | Web dashboard | You (platform owner) |

---

## 2. Confirmed Business Model

- **Delivery**: Restaurant's own staff. App only shows status updates (Preparing → Out for Delivery → Delivered) — no live GPS rider tracking in v1.
- **Payments**: Online only (card/UPI/wallet). No Cash on Delivery.
- **Revenue model**: Flat monthly subscription per restaurant, unlimited orders, no commission. Includes a free trial period; auto-suspend listing if payment lapses.
- **Onboarding**: Fully automated self-signup for restaurants (no manual approval bottleneck), but visible to you via the Admin Panel for oversight.
- **Cart model**: Customers *can* order from multiple restaurants in a single checkout. Behind the scenes this splits into **one independent sub-order per restaurant** (separate prep/status/delivery), but one combined payment.
- **Coupons/offers**: Created and managed by each restaurant individually (not platform-wide, in v1).
- **Delivery area**: Each restaurant sets its own delivery radius.
- **Minimum order value**: Set individually by each restaurant.
- **Language**: English only at launch.
- **Branding**: Bold, vibrant, playful visual style (Swiggy-like energy) — bright accent colors, high-contrast CTAs, friendly iconography.

---

## 3. System Architecture (High Level)

```
                    ┌─────────────────────┐
                    │   Admin Web Panel    │  (You: approvals, subscriptions, disputes)
                    └──────────┬───────────┘
                               │
                    ┌──────────▼───────────┐
                    │   Backend / API       │
                    │  (orders, payments,   │
                    │   auth, notifications)│
                    └──┬────────────────┬───┘
                       │                │
          ┌────────────▼───┐    ┌───────▼────────────┐
          │ Customer App    │    │ Restaurant Partner  │
          │ (Android)       │    │ App (Android)        │
          └─────────────────┘    └───────────────────────┘
```

**Backend responsibilities:**
- Restaurant & menu data
- Order creation, splitting into sub-orders, status sync
- Payment processing + subscription billing
- Push notifications
- Ratings/reviews storage
- Coupon validation (per restaurant)

---

## 4. Customer App — Feature List

### Core
- Sign up / login (phone + OTP recommended, or email)
- Home feed: nearby restaurants (by delivery radius), search & filters (cuisine, price, rating)
- Restaurant page: menu, ratings/reviews, min order value, delivery radius indicator, current offers
- **Multi-restaurant cart**: add items from different restaurants; checkout splits into per-restaurant sub-orders automatically, single combined payment
- Order tracking per sub-order (status stepper: Placed → Accepted → Preparing → Out for Delivery → Delivered)
- Order history & reorder
- Favorites (restaurants/dishes)
- Ratings & reviews (post-delivery, per restaurant)
- In-app text chat with restaurant (per active order)
- Push notifications: order status, offers, promotions
- Coupon code entry at checkout (validated per restaurant)
- Payment: card/UPI/wallet via payment gateway (e.g., Razorpay/Stripe — to be decided in tech setup)

### Nice-to-have (Phase 2, not MVP)
- Live map / delivery ETA
- Loyalty points
- Group ordering / split bill

---

## 5. Restaurant Partner App — Feature List

### Core
- Self-signup: business details, menu upload, delivery radius, min order value, bank/payout details
- Free trial activation on signup; subscription payment screen; auto-suspend if unpaid past trial
- **Menu & inventory management**: add/edit/delete items, categories, photos, mark items out-of-stock in real time
- **Order dashboard**: incoming orders (accept/reject), status updates (Preparing → Out for Delivery → Delivered)
- **Staff accounts with roles**: Owner (full access), Manager (orders + menu), Kitchen Staff (orders only, view-only menu)
- **Sales analytics & reports**: daily/weekly/monthly revenue, best-selling items, order volume trends
- **Payout/invoice history**: subscription payment records, receipts
- Coupon/offer creation & management (restaurant-controlled)
- In-app text chat with customers (per active order)
- Push notifications: new order alerts, subscription reminders

---

## 6. Admin Web Panel — Feature List (You, the Platform Owner)

- Restaurant directory: view all signed-up restaurants, status (active/trial/suspended)
- Subscription monitoring: who's paid, who's in trial, who's overdue — with manual override to suspend/reactivate
- Dispute resolution: view flagged orders/complaints between customers and restaurants
- Basic platform-wide analytics: total restaurants, total orders, revenue from subscriptions
- Ability to deactivate a restaurant/customer account for policy violations

*(This is intentionally lightweight for v1 — not a full BI suite.)*

---

## 7. Order Flow (Multi-Restaurant Cart)

1. Customer adds items from Restaurant A and Restaurant B to cart.
2. At checkout, system checks each restaurant's minimum order value independently.
3. One payment is charged for the full cart total.
4. System creates **two sub-orders** (one to Restaurant A, one to Restaurant B), each with its own status lifecycle.
5. Each restaurant's Partner App receives its own sub-order to accept/prepare/deliver independently.
6. Customer App shows two separate tracking cards under one "Order" screen.
7. Customer rates each restaurant separately after delivery.

---

## 8. Subscription & Billing Logic

- Restaurant signs up → **free trial period** (length TBD, e.g., 14 or 30 days — needs your decision).
- During trial: fully listed and functional.
- Trial ends → must have active payment method on file → auto-charged monthly.
- Missed payment → grace flag → after grace period, **listing auto-suspended** (invisible to customers) until payment resolves.
- All subscription events logged and visible in Admin Panel.

**Open decision needed from you:** exact trial length, grace period length, and monthly price point.

---

## 9. Data Model (High-Level Entities)

- **User** (customer): id, name, phone, addresses, payment methods
- **Restaurant**: id, name, location, delivery radius, min order value, subscription status, staff list
- **MenuItem**: id, restaurant_id, name, price, category, availability
- **Order**: id, customer_id, total amount, payment status, list of sub-orders
- **SubOrder**: id, order_id, restaurant_id, items, status, timestamps
- **Coupon**: id, restaurant_id, code, discount rules, validity
- **Review**: id, customer_id, restaurant_id, sub_order_id, rating, comment
- **StaffAccount**: id, restaurant_id, role (owner/manager/kitchen), login credentials
- **SubscriptionRecord**: id, restaurant_id, plan, status, next billing date

---

## 10. Recommended Tech Stack

| Layer | Choice | Why |
|---|---|---|
| Customer & Partner Apps | **Kotlin (native Android)** | Confirmed — best performance, full Android API access |
| Admin Panel | Web app (React or similar) | Lightweight dashboard, accessible from any browser |
| Backend | REST API (e.g., Node.js/Spring Boot/Django — to be decided) | Powers both apps + admin panel |
| Database | PostgreSQL (relational — orders, restaurants, subscriptions have clear relations) | Strong consistency for orders/payments |
| Payments | Razorpay / Stripe (region-dependent) | Handles online payments + recurring subscription billing |
| Push Notifications | Firebase Cloud Messaging (FCM) | Standard for Android |
| Chat | Firebase Realtime DB / a lightweight chat service | Simple in-app text chat, no need for heavy infra at v1 scale |
| Hosting | Cloud provider (AWS/GCP/Azure) | Scalable as you grow beyond one city |

---

## 11. Suggested Build Phases

**Phase 1 — MVP (single city launch)**
- Customer App: browse, single + multi-restaurant cart, checkout, order tracking, ratings
- Partner App: signup, menu management, order dashboard, staff roles, basic analytics
- Admin Panel: restaurant list, subscription status, suspend/reactivate
- Payments: online only, subscription billing with trial

**Phase 2**
- In-app chat
- Coupon system refinement
- Deeper analytics for restaurants
- Expansion to more cities

**Phase 3**
- iOS versions (if desired later)
- Loyalty/rewards
- Live delivery ETA improvements

---

## 12. Still Open — Decisions Needed From You

1. Exact subscription price and trial length (e.g., 14-day free trial, then ₹X/month).
2. Payment gateway preference (Razorpay, Stripe, PayU, etc. — depends on your country).
3. App name — Confirmed as **OrderAra**.
4. OTP-based login vs email/password for customers and restaurant staff.
5. Grace period length before auto-suspension on missed payment.

---

*This document is the blueprint for development. Next step: once you confirm the open decisions in Section 12, we can move into wireframing the actual screens (Customer App, Partner App, Admin Panel) and then into a detailed technical spec for development.*
