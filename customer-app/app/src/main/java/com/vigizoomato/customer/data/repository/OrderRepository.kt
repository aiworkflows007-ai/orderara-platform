package com.vigizoomato.customer.data.repository

import com.vigizoomato.customer.data.models.Address
import com.vigizoomato.customer.data.models.CartItem
import com.vigizoomato.customer.data.models.MenuItem
import com.vigizoomato.customer.data.models.Order
import com.vigizoomato.customer.data.models.OrderStatus
import com.vigizoomato.customer.data.models.StatusTimestamp
import com.vigizoomato.customer.data.models.SubOrder
import com.vigizoomato.customer.data.network.ApiClient
import com.vigizoomato.customer.data.network.ApiConfig
import com.vigizoomato.customer.data.network.RealtimeClient
import com.vigizoomato.customer.data.network.objects
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * The customer's orders, owned by the server.
 *
 * Checkout sends the cart up, where it is split into one sub-order per
 * restaurant. Each restaurant then drives its own sub-order's status, and those
 * changes come back here — by live push first, with polling as a safety net.
 */
class OrderRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _isPlacingOrder = MutableStateFlow(false)
    val isPlacingOrder: StateFlow<Boolean> = _isPlacingOrder.asStateFlow()

    /** Address is not stored server-side, so keep the one used at checkout. */
    private var lastKnownAddress: Address? = null

    init {
        RealtimeClient.on("order:status") { payload ->
            val subOrderId = payload.optString("subOrderId")
            val status = payload.optString("status").toOrderStatus()
            val driver = payload.optString("driverName")
            _orders.value = _orders.value.map { order ->
                order.copy(subOrders = order.subOrders.map { sub ->
                    if (sub.subOrderId != subOrderId) sub
                    else sub.copy(
                        status = status,
                        driverName = driver.ifBlank { sub.driverName },
                        statusHistory = sub.statusHistory + StatusTimestamp(
                            status = status,
                            timeFormatted = nowClock(),
                            note = "Updated by ${sub.restaurantName}"
                        )
                    )
                })
            }
        }

        scope.launch {
            while (isActive) {
                fetchOrders()
                delay(ApiConfig.POLL_INTERVAL_MS)
            }
        }
    }

    fun refresh() {
        scope.launch { fetchOrders() }
    }

    private fun fetchOrders() {
        val res = ApiClient.get("/api/orders/customer/${ApiConfig.CUSTOMER_ID}")
        if (res.isSuccess) {
            _orders.value = res.dataArray.objects().map { it.toOrder(lastKnownAddress) }
        }
    }

    fun getOrderById(orderId: String): Order? = _orders.value.find { it.id == orderId }

    /**
     * Sends the cart to the server, which splits it per restaurant and rings
     * each Partner app. Fails loudly when a restaurant closed, went below its
     * minimum, or had its listing suspended between browsing and checkout.
     */
    fun placeOrder(
        cartSummary: CartSummary,
        deliveryAddress: Address,
        paymentMethod: String,
        deliveryInstructions: String,
        customerName: String,
        customerPhone: String,
        onResult: (Order?, String?) -> Unit
    ) {
        _isPlacingOrder.value = true
        lastKnownAddress = deliveryAddress

        scope.launch {
            val payload = JSONObject().apply {
                put("userId", ApiConfig.CUSTOMER_ID)
                put("userName", customerName)
                put("userPhone", customerPhone)
                put("deliveryAddress", deliveryAddress.fullAddress)
                put("deliveryInstructions", deliveryInstructions)
                put("paymentMethod", paymentMethod)
                put("taxesAndFees", cartSummary.taxesAndPackaging)
                put("subOrders", JSONArray().apply {
                    cartSummary.groups.forEach { group ->
                        put(JSONObject().apply {
                            put("restaurantId", group.restaurantId)
                            put("restaurantName", group.restaurantName)
                            put("subTotal", group.subTotal)
                            put("deliveryFee", group.deliveryFee)
                            put("discount", group.discount)
                            put("specialInstructions", deliveryInstructions)
                            put("items", JSONArray().apply {
                                group.items.forEach { cartItem ->
                                    put(JSONObject().apply {
                                        put("menuItemId", cartItem.menuItem.id)
                                        put("name", cartItem.menuItem.name)
                                        put("description", cartItem.menuItem.description)
                                        put("imageUrl", cartItem.menuItem.imageUrl)
                                        put("isVeg", cartItem.menuItem.isVeg)
                                        put("price", cartItem.menuItem.price)
                                        put("quantity", cartItem.quantity)
                                        put("totalPrice", cartItem.totalPrice)
                                        put("specialNotes", cartItem.specialInstructions)
                                    })
                                }
                            })
                        })
                    }
                })
            }

            val res = ApiClient.post("/api/orders", payload)
            _isPlacingOrder.value = false

            if (res.isSuccess) {
                val order = res.data!!.toOrder(deliveryAddress)
                _orders.value = listOf(order) + _orders.value.filterNot { it.id == order.id }
                withContext(Dispatchers.Main) { onResult(order, null) }
            } else {
                val message = if (res.code == -1) {
                    "Could not reach OrderAra. Check your connection and try again."
                } else {
                    res.message
                }
                withContext(Dispatchers.Main) { onResult(null, message) }
            }
        }
    }

    fun rateSubOrder(orderId: String, subOrderId: String, ratingScore: Double) {
        _orders.value = _orders.value.map { order ->
            if (order.id != orderId) order
            else order.copy(subOrders = order.subOrders.map { sub ->
                if (sub.subOrderId == subOrderId) sub.copy(isRated = true, ratingScore = ratingScore) else sub
            })
        }
    }
}

private val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}
private val clockFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
private val dayFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

private fun nowClock(): String = clockFormat.format(java.util.Date())

private fun String?.toClock(): String {
    if (this.isNullOrBlank()) return ""
    return runCatching { clockFormat.format(isoParser.parse(take(19))!!) }.getOrDefault("")
}

private fun String?.toDayStamp(): String {
    if (this.isNullOrBlank()) return ""
    return runCatching { dayFormat.format(isoParser.parse(take(19))!!) }.getOrDefault(this)
}

internal fun String?.toOrderStatus(): OrderStatus =
    OrderStatus.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }
        ?: if (this.equals("REJECTED", ignoreCase = true)) OrderStatus.CANCELLED else OrderStatus.PLACED

/** Rebuilds the app's Order model from the server's JSON. */
internal fun JSONObject.toOrder(address: Address?): Order {
    val orderId = optString("orderId")
    val fallbackAddress = address ?: Address(
        id = "addr_remote",
        title = "Delivery address",
        street = optString("deliveryAddress"),
        city = "",
        pincode = ""
    )

    val subOrders = optJSONArray("subOrders").objects().map { so ->
        val restaurantId = so.optString("restaurantId")
        val restaurantName = so.optString("restaurantName")
        SubOrder(
            subOrderId = so.optString("subOrderId"),
            orderId = orderId,
            restaurantId = restaurantId,
            restaurantName = restaurantName,
            restaurantPhone = so.optString("restaurantPhone"),
            items = so.optJSONArray("items").objects().map { item ->
                CartItem(
                    menuItem = MenuItem(
                        id = item.optString("menuItemId"),
                        restaurantId = restaurantId,
                        name = item.optString("name"),
                        description = item.optString("description"),
                        price = item.optDouble("price", 0.0),
                        category = "",
                        imageUrl = item.optString("imageUrl"),
                        isVeg = item.optBoolean("isVeg", true)
                    ),
                    restaurantId = restaurantId,
                    restaurantName = restaurantName,
                    restaurantMinOrder = 0.0,
                    quantity = item.optInt("quantity", 1),
                    specialInstructions = item.optString("specialNotes")
                )
            },
            subTotal = so.optDouble("subTotal", 0.0),
            deliveryFee = so.optDouble("deliveryFee", 0.0),
            discount = so.optDouble("discount", 0.0),
            status = so.optString("status").toOrderStatus(),
            statusHistory = so.optJSONArray("statusHistory").objects().map { h ->
                StatusTimestamp(
                    status = h.optString("status").toOrderStatus(),
                    timeFormatted = h.optString("at").toClock(),
                    note = h.optString("note")
                )
            },
            estimatedDeliveryMinutes = so.optInt("estimatedDeliveryMinutes", 30),
            driverName = so.optString("driverName"),
            driverPhone = so.optString("driverPhone"),
            isRated = so.optBoolean("isRated", false),
            ratingScore = so.optDouble("ratingScore", 0.0)
        )
    }

    return Order(
        id = orderId,
        customerId = optString("userId"),
        subOrders = subOrders,
        itemsTotal = optDouble("itemsTotal", 0.0),
        totalDeliveryFee = optDouble("totalDeliveryFee", 0.0),
        taxesAndPlatformFee = optDouble("taxesAndFees", 0.0),
        totalDiscount = optDouble("totalDiscount", 0.0),
        grandTotal = optDouble("totalPaid", 0.0),
        paymentStatus = optString("paymentStatus", "PAID"),
        paymentMethod = optString("paymentMethod"),
        transactionId = optString("transactionId"),
        deliveryAddress = fallbackAddress,
        deliveryInstructions = optString("deliveryInstructions"),
        createdAt = optString("createdAt").toDayStamp()
    )
}
