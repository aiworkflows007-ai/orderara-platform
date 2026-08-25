# OrderAra — Three-App Sync: Handoff Notes

**Date:** 2026-08-23
**Repo:** `/home/ashok/Projects/restaurant` (git remote `aiworkflows007-ai/orderara-platform`, branch `main`)
**Approved plan:** `/home/ashok/.claude/plans/floating-beaming-eich.md`

> **Audience note:** the repo owner (Ashok) is a non-coder. Keep explanations to him
> plain and visual — no code detail in what he reads. Implementation detail stays here.

---

## 1. The goal

Three products plus one backend must genuinely talk to each other:

| Product | Path | Tech |
|---|---|---|
| Customer app | `customer-app/` | Kotlin + Jetpack Compose (`com.vigizoomato.customer`) |
| Partner app (restaurants) | `partner-app/` | Kotlin + Jetpack Compose (`com.orderara.partner`) |
| Admin panel (subscriptions) | `admin-panel/` | React 19 + Vite + Tailwind 4 |
| Backend | `backend/` | Express 5 + socket.io, JSON-file persistence |

### The audit finding that started this work

Only **1 of 8** shared flows was really wired. Everything else looked connected
on screen but each app was reading its own built-in sample data. The Admin panel
made **zero** calls to the server. The server had **no concept of a subscription**
at all — the one thing the Admin panel exists to manage.

### The 8 flows (this is the test plan — test them one at a time, in order)

| # | Flow | Direction | Status |
|---|---|---|---|
| 1 | Restaurant signs up → gets listed | Partner → Customer + Admin | code done, **device test pending** |
| 2 | Menu items & out-of-stock | Partner → Customer | code done except 1 wiring gap (§5) |
| 3 | Store open/closed, radius, min order | Partner → Customer | code done, device test pending |
| 4 | Order placed → splits per restaurant | Customer → Partner + Admin | code done, device test pending |
| 5 | Order status lifecycle | Partner → Customer + Admin | code done, device test pending |
| 6 | Chat during an order | Customer ↔ Partner | code done except wiring gaps (§5) |
| 7 | Subscription / trial / suspension | Partner ↔ Admin → hides from Customer | code done, device test pending |
| 8 | Ratings after delivery | Customer → Partner + Admin | code done, device test pending |

### Decisions Ashok already made (do not re-litigate)

- Build and test **locally** first (phone by USB), deploy to VPS once at the end.
- **Instant socket push for orders**, 3–4s polling for everything else.
- Server **saves to a JSON file** so data survives restarts.
- Admin panel gets **full control** — suspending really hides a restaurant.

---

## 2. What is DONE and verified

### 2.1 Backend — rewritten (`backend/server.js`, new `backend/store.js`)

**`store.js`** owns all state and persists it to `backend/data.json` (debounced
atomic write). Seeds 4 demo restaurants + 16 menu items on first boot. Exports
subscription helpers: `isListed`, `sweepSubscriptions`, `subscriptionView`,
`activatePaidPlan`, `setSubscriptionStatus`.

**Bugs fixed from the audit:**
- **Sub-order ID collision** — was `SUB-01`/`SUB-02` per order, so two simultaneous
  customers collided and a restaurant's status update hit the wrong customer's order.
  Now `${orderId}-S${n}` with a monotonic order counter (`VZ-ORD-9843-S1`).
- **Express-5 crash route** — `POST /api/restaurants` used `app._router.handle`
  (removed in Express 5). Deleted.
- **In-memory only** — now file-backed.
- Added `X-HTTP-Method-Override` middleware because some Android builds refuse
  to send PATCH/DELETE over `HttpURLConnection`.

**Subscription lifecycle:** `ACTIVE_TRIAL → OVERDUE → SUSPENDED`, driven by
`dueAt` / `graceEndsAt`. Swept on boot and hourly. Tunable by env vars
`TRIAL_DAYS` (14), `GRACE_DAYS` (3), `MONTHLY_PRICE` (999).

**Socket rooms** (this is what makes an order ring only the right phone):
- `restaurant:<id>` — partner joins on connect
- `customer:<id>` — customer joins on connect
- `admin` — admin panel joins
- `suborder:<id>` — chat thread
- Clients join with `socket.emit('join', {role, restaurantId|userId})`.
- Events: `order:new`, `order:status`, `chat:new`, `subscription:updated`,
  `menu:updated`, `menu:stock`, `restaurant:new`, `restaurants:updated`,
  `restaurant:settings`, `review:new`.

**Endpoint map:**
```
GET    /health
GET    /api/restaurants                              (customer feed — suspended EXCLUDED)
GET    /api/restaurants/:id                          (restaurant + real menu + reviews)
POST   /api/partner/register
GET    /api/partner/profile/:id                      (restaurant + menu + subscription + orders)
GET    /api/partner/menu/:restaurantId
POST   /api/partner/menu
PATCH  /api/partner/menu/:itemId
PATCH  /api/partner/menu/:itemId/toggle-stock
DELETE /api/partner/menu/:itemId
PATCH  /api/partner/restaurant/:id/settings
GET    /api/partner/orders/:restaurantId
GET    /api/partner/analytics/:restaurantId          (computed from real orders)
GET    /api/partner/subscription/:restaurantId
POST   /api/partner/subscription/:restaurantId/activate
POST   /api/orders                                   (validates open/suspended/min-order BEFORE creating)
GET    /api/orders  |  /api/orders/:orderId  |  /api/orders/customer/:userId
PATCH  /api/orders/sub-order/:subOrderId/status
GET/POST /api/chat/:subOrderId
GET    /api/reviews/:restaurantId   |  POST /api/reviews
GET    /api/admin/restaurants                        (ALL, including suspended)
PATCH  /api/admin/restaurants/:id/subscription
GET    /api/admin/stats
```

**Verified passing:**
- `/tmp/claude-1000/-home-ashok/c6181a4d-288b-4e5a-8927-dbe451faa209/scratchpad/smoke.sh`
  → **51/51 API checks pass** (covers all 8 flows end-to-end at the API level,
  including suspension hiding the restaurant and blocking orders).
- `…/scratchpad/socket_test.js` → **10/10 socket-room checks pass**
  (proves the owning restaurant gets the order and the *other* restaurant does not).
- Persistence verified: killed and restarted the server; restaurants, orders,
  chat and reviews all survived.

> Both scripts have been copied into **`backend/test/`** (`smoke.sh`, `socket_test.js`).
> Run with the backend up on port 8080:
> `bash backend/test/smoke.sh` and `node backend/test/socket_test.js`.
> `smoke.sh` registers a `rest_test` restaurant, so run it against a throwaway
> `data.json` (delete the file first for a clean run).

### 2.2 Both Android apps — shared network layer

Added to `customer-app/` and `partner-app/`:
- `gradle/libs.versions.toml`: `socketio = "2.1.1"`, `socketio-client` library entry
- `app/build.gradle.kts`: `implementation(libs.socketio.client) { exclude(group="org.json", module="json") }`
- `data/network/ApiConfig.kt` — **`USE_LOCAL_BACKEND` toggle**, poll intervals,
  `CUSTOMER_ID = "user_101"` (customer app only)
- `data/network/ApiClient.kt` — blocking GET/POST/PATCH/DELETE returning a
  `Response(code, body)` with `isSuccess` / `data` / `dataArray` / `message`.
  Must be called from `Dispatchers.IO`.
- `data/network/RealtimeClient.kt` — socket.io singleton, joins the right room,
  fans events out to registered handlers.

### 2.3 Partner app — identity is now real

- **`data/network/PartnerSession.kt` (new)** — SharedPreferences store for
  `restaurantId` / `ownerName` / `ownerPhone`. **This is the fix for the biggest
  bug**: a freshly registered restaurant used to keep showing Royal Biryani
  House's menu and orders because `restaurantId` was hardcoded to `rest_1` and
  forgotten on restart.
- `PartnerAuthRepository` — rewritten. Exposes `restaurantId: StateFlow<String?>`;
  `bootstrap()` restores the saved session on launch; `registerNewRestaurant(...)`
  is async with an `onResult(success, message)` callback and only succeeds once
  the **server** confirms; store settings and UPI go through PATCH.
- `PartnerMenuRepository`, `PartnerOrderRepository`, `SubscriptionRepository`,
  `PartnerChatRepository` — all rewritten server-backed with optimistic updates
  and rollback on failure. Each has `start(restaurantId)` / `stop()`.
- **`PartnerAnalyticsRepository` (new)** — real figures from the server.
- `OrderAraPartnerApp` — collects `authRepository.restaurantId` and starts/stops
  every other repository when it changes. Calls `PartnerSession.init(this)` first.
- `PartnerModels.kt` — `OrderItemRecord` flattened to match server JSON;
  `PartnerOrderStatus.CANCELLED` added plus `fromApi()`; `SubscriptionInfo`
  extended with real status fields + `headline`; `RestaurantProfile.cuisineTypes`.
- `PartnerMockData.kt` — trimmed to a placeholder profile + cuisine options only.
- `OrderCard.kt` — `item.menuItem.name` → `item.name`; CANCELLED branches added.
- `NavGraph.kt` — start destination is Onboarding when no restaurant is registered.
- `RestaurantOnboardingViewModel` — waits for the server, surfaces errors.

### 2.4 Customer app — server-backed

- `RestaurantRepository` — rewritten. Polls `/api/restaurants` every 4s, reacts to
  `menu:stock` / `menu:updated` / `restaurants:updated` / `restaurant:settings`.
  **`refreshMenu(restaurantId)` fetches the REAL menu** — the old code invented
  two fake placeholder dishes client-side. Adds `submitReview(...)`.
- `OrderRepository` — rewritten. `placeOrder(...)` POSTs to the server and reports
  failure (closed / suspended / below minimum) through an `onResult(order, error)`
  callback. Polls `/api/orders/customer/user_101`, reacts to `order:status`.
- `ChatRepository` — rewritten with `openThread(orderId, subOrderId, restaurantName)`
  / `closeThread()`, 3s poll + `chat:new` push.
- `VigiZoomatoApp` — connects `RealtimeClient` **before** building `AppContainer`
  (repositories register socket handlers in their constructors).
- `MockDataProvider` — trimmed 559 → 94 lines (only `sampleUser` + `sampleCoupons`).
- `CheckoutViewModel` — async place-order, `errorMessage` in UI state.
- `CheckoutScreen` — snackbar shows the server's rejection reason.
- `ReviewViewModel` — posts to the server.
- `OrderTrackingViewModel` + `SubOrderStatusStepper` — removed the "⚡ Demo Mode /
  Advance status" button. Customers must not drive their own order status; the
  restaurant does.

### 2.5 Admin panel — was 100% fake, now live

- **`src/api.js` (new)** — `api.*` fetch helpers, `getSocket()`, `useLiveData(loader, events, interval)`
  hook (poll + socket-triggered refetch), shared formatters and status style maps.
- **`src/components/StateBlocks.jsx` (new)** — Loading / Error / Empty.
  **Deliberate rule: never fall back to fake numbers when the server is unreachable.**
- `SubscriptionsHub.jsx` — real MRR/trials/overdue; **Suspend / Reactivate / Mark paid
  buttons actually call the server**.
- `RestaurantsManagement.jsx` — real directory with filters + search.
- `LiveOrdersMonitor.jsx` — real parent orders with their split sub-orders.
- `DashboardOverview.jsx` — real tiles, "needs attention", latest sub-orders.
- `PlatformSettings.jsx` — shows the billing policy the server actually enforces
  (read-only, sourced from `/api/admin/stats`); the fake platform-coupon manager
  was removed (coupons are per-restaurant per the master plan).
- `Header.jsx` — honest live/disconnected socket indicator (replaced the
  hardcoded "Device Connected: 00196654C005228" badge). Fake bell removed.
- **`src/mockData.js` DELETED.** No references remain.

### 2.6 Build status

| Target | Command | Result |
|---|---|---|
| Backend | `node backend/server.js` | runs, 61 automated checks pass |
| Partner app | `cd partner-app && ./gradlew :app:compileDebugKotlin` | **BUILD SUCCESSFUL** |
| Customer app | `cd customer-app && ./gradlew :app:compileDebugKotlin` | **BUILD SUCCESSFUL** |
| Admin panel | `cd admin-panel && npx vite build` | **built in 1.47s** |

`.gitignore` now excludes `backend/data.json` so a deploy never clobbers live
server data. **Nothing has been committed yet — the working tree is dirty.**

---

## 3. One unverified edit (session was interrupted here)

The last tool call was interrupted. It did two things; **the file edit landed, the
compile did not run.**

- **Applied:** `partner-app/.../ui/screens/onboarding/RestaurantOnboardingScreen.kt`
  (~line 690) — the submit button is now `enabled = !state.isSubmitting` and shows
  a spinner + "Registering…" while the server call is in flight. Verified by
  `git diff`; all needed imports are already present via wildcard imports
  (`androidx.compose.foundation.layout.*`, `material3.*`, `Alignment`, `unit.dp`).
- **NOT run:** `cd partner-app && ./gradlew :app:compileDebugKotlin`.

**Do this first:** run that compile to confirm the tree still builds. It is
expected to pass; if it does not, the edit is small and self-contained enough to
revert with `git checkout -- <that file>` without losing anything else.

---

## 4. Nothing has been tested on the real device yet

**This is the biggest outstanding item.** All 61 passing checks are backend-only.
Neither app has been installed or run against the local backend even once.

### Test setup

```bash
# 1. backend
cd /home/ashok/Projects/restaurant/backend && node server.js      # port 8080

# 2. let the USB phone reach the laptop's server
adb reverse tcp:8080 tcp:8080                                     # device 00196654C005228

# 3. admin panel — either
cd admin-panel && npm run dev                                     # http://localhost:5173
#    or just open http://localhost:8080 (backend serves admin-panel/dist)

# 4. install both apps
cd partner-app  && ./gradlew installDebug
cd customer-app && ./gradlew installDebug
```

Both manifests already have `usesCleartextTraffic="true"`, required for
`http://localhost`.

**Ashok has no browser automation available this session** (he declined the Chrome
extension — do not suggest it again). Screenshots must come from the device via
`adb exec-out screencap -p > shot.png`, and he checks the admin panel manually.

### Per-flow acceptance tests

1. **Listing** — register "Test Kitchen" in the Partner app → appears in the
   Customer feed within 4s and in the Admin directory.
2. **Menu** — add "Masala Chai ₹40" in Partner → visible on that restaurant's page
   in Customer. Toggle out-of-stock → greys out for the customer.
3. **Settings** — set store Closed → Customer shows closed and refuses to add items.
4. **Orders** — checkout from two restaurants at once → two order cards land on the
   correct Partner phones; Admin shows one parent order with two sub-orders.
5. **Status** — tap Accept in Partner → Customer tracking stepper advances within
   seconds. Advance both sub-orders independently to prove they don't interfere.
6. **Chat** — message from Customer appears in Partner; reply comes back.
7. **Subscription** — Admin suspends → restaurant vanishes from Customer within 4s
   and orders are refused; reactivate → returns. Partner "activate paid plan"
   raises Admin's revenue figure.
8. **Ratings** — rate a delivered order → new average in Customer feed, Partner
   analytics and Admin directory.

---

## 5. Known wiring gaps (found by grep, must fix before flows 2 and 6 pass)

These compile fine but the screens never call the new repository methods:

| Gap | File | Fix |
|---|---|---|
| **Customer never loads a real menu** | `customer-app/.../ui/screens/restaurant/RestaurantDetailViewModel.kt` | its `loadRestaurant(id)` (or equivalent setter for `_restaurantId`) must call `restaurantRepository.refreshMenu(id)`. **Flow 2 cannot pass without this.** |
| **Customer chat never opens a thread** | `customer-app/.../ui/screens/chat/ChatViewModel.kt` | the function that sets `_subOrderId` must call `chatRepository.openThread(orderId, subOrderId, restaurantName)`; add `onCleared { closeThread() }`. **Flow 6.** |
| **Partner chat never opens a thread** | `partner-app/.../ui/screens/chat/` (`PartnerChatScreen.kt` + its view model) | must call `chatRepository.openThread(subOrderId)` / `closeThread()`. **Flow 6.** |
| **Partner analytics shows zeros** | `partner-app/.../ui/screens/analytics/AnalyticsViewModel.kt` | currently `MutableStateFlow(PartnerDailyAnalytics())` — replace with `OrderAraPartnerApp.instance.analyticsRepository.analytics`. **Flow 8 verification.** |
| Partner new-order alert unused | `partner-app/.../ui/screens/orders/PartnerOrdersScreen.kt` | `orderRepository.newOrderAlert` exists but nothing consumes it; wire a sound/snackbar then `consumeNewOrderAlert()`. Nice-to-have for Flow 4. |

Also unreviewed (compiled, behaviour not checked): `SettingsScreen`,
`SubscriptionScreen`, `MenuManagementScreen` in the Partner app.

---

## 6. Deploy (last step — task #12)

1. Flip **`USE_LOCAL_BACKEND = false`** in BOTH:
   - `customer-app/app/src/main/java/com/vigizoomato/customer/data/network/ApiConfig.kt:11`
   - `partner-app/app/src/main/java/com/orderara/partner/data/network/ApiConfig.kt:11`
2. Production URL is `https://restaurant.ai-workflows.cloud` → resolves to
   **147.93.108.231** (the same Hostinger VPS as `mirchmasala` in `~/.ssh/config`).
   SSH (22) and HTTPS (443) were both reachable when tested, but **an `ssh` command
   to that host was blocked by the permission classifier** — ask Ashok to run
   deploy commands himself with the `! <command>` prefix, or to approve the rule.
3. Deployment is Docker: `Dockerfile` + `docker-compose.yml` (host port 3009 →
   container 3000, external network `the-perfect-mart_default`). There is **no
   GitHub Actions workflow in this repo** — deploys are manual.
4. `backend/data.json` is gitignored — the server keeps its own live copy. Make
   sure the container has a writable volume for it or data resets on every rebuild.
   **This is currently NOT configured in `docker-compose.yml` — needs a volume mount.**
5. Re-run flows 1, 4, 5 and 7 against production.

---

## 7. Environment gotchas that cost time

- **The shell is zsh, not bash.** `grep --include=*.kt` fails with
  `(eval):1: no matches found`. Quote globs or use `-r` with explicit paths.
- **`pgrep -f "node server.js"` matches the shell's own command line**, so
  `kill $(pgrep -f …)` kills the tool's shell (exit code 144). Kill by explicit
  PID after inspecting `pgrep -af`, or use a distinctive pattern.
- `socket.io-client` for node scripts exists only in
  `admin-panel/node_modules`, not `backend/node_modules`.
- Gradle needs network for the new socket.io dependency — `--offline` fails.
- `local.properties` with `sdk.dir=/home/ashok/Android/Sdk` was created in both
  app modules (gitignored).

---

## 8. Task list state

```
#1  [completed]   Backend: persistence, unique IDs, subscriptions
#2  [completed]   Shared network layer in both Android apps
#3  [completed]   Admin panel api.js + live data wiring
#4  [in_progress] Flow 1: restaurant appears in Customer feed + Admin directory
#5  [pending]     Flow 2: menu / out-of-stock sync
#6  [pending]     Flow 3: store settings
#7  [pending]     Flow 4: order placement + split
#8  [pending]     Flow 5: order status lifecycle
#9  [pending]     Flow 6: chat
#10 [pending]     Flow 7: subscription + suspension enforcement
#11 [pending]     Flow 8: ratings
#12 [pending]     Deploy to VPS and re-verify live
```

**Recommended order for whoever picks this up:**
1. Run the partner-app compile from §3 to confirm the tree builds.
2. Close the §5 wiring gaps (small, but they block flows 2, 6 and 8).
3. Install both apps on the device and walk flows 1 → 8 one at a time,
   screenshotting each result for Ashok before moving on to the next.
4. Commit (nothing is committed yet), then deploy per §6.

**Do not** widen scope beyond these 8 flows. Coupons, staff roles, loyalty and
live GPS tracking are explicitly Phase 2 in `VigiZoomato_Master_Plan.md`.
