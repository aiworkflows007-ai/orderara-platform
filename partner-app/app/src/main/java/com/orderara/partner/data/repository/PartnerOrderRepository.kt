package com.orderara.partner.data.repository

import com.orderara.partner.data.models.IncomingSubOrder
import com.orderara.partner.data.models.OrderItemRecord
import com.orderara.partner.data.models.PartnerOrderStatus
import com.orderara.partner.data.network.ApiClient
import com.orderara.partner.data.network.ApiConfig
import com.orderara.partner.data.network.RealtimeClient
import com.orderara.partner.data.network.objects
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Incoming sub-orders for this restaurant.
 *
 * New orders arrive by live push the moment a customer checks out; a slower
 * poll runs alongside it as a safety net if the socket drops.
 */
class PartnerOrderRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var syncJob: Job? = null
    private var restaurantId: String? = null
    private var realtimeBound = false

    private val _orders = MutableStateFlow<List<IncomingSubOrder>>(emptyList())
    val orders: StateFlow<List<IncomingSubOrder>> = _orders.asStateFlow()

    /** Set when a brand-new order lands, so the UI can alert the kitchen. */
    private val _newOrderAlert = MutableStateFlow<String?>(null)
    val newOrderAlert: StateFlow<String?> = _newOrderAlert.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun start(restaurantId: String) {
        if (this.restaurantId == restaurantId && syncJob?.isActive == true) return
        this.restaurantId = restaurantId
        syncJob?.cancel()

        if (!realtimeBound) {
            realtimeBound = true
            RealtimeClient.on("order:new") { payload ->
                val sub = payload.optJSONObject("subOrder") ?: return@on
                val incoming = sub.toIncomingSubOrder(
                    parentOrderId = payload.optString("orderId"),
                    customerName = payload.optString("customerName"),
                    customerPhone = payload.optString("customerPhone"),
                    deliveryAddress = payload.optString("deliveryAddress")
                )
                if (_orders.value.none { it.subOrderId == incoming.subOrderId }) {
                    _orders.value = listOf(incoming) + _orders.value
                    _newOrderAlert.value = incoming.subOrderId
                }
            }
            RealtimeClient.on("order:status") { payload ->
                val subOrderId = payload.optString("subOrderId")
                val status = PartnerOrderStatus.fromApi(payload.optString("status"))
                _orders.value = _orders.value.map {
                    if (it.subOrderId == subOrderId) it.copy(status = status) else it
                }
            }
        }

        syncJob = scope.launch {
            while (isActive) {
                fetch(restaurantId)
                delay(ApiConfig.POLL_INTERVAL_MS)
            }
        }
    }

    fun stop() {
        syncJob?.cancel()
        restaurantId = null
        _orders.value = emptyList()
    }

    fun refresh() {
        restaurantId?.let { id -> scope.launch { fetch(id) } }
    }

    fun consumeNewOrderAlert() { _newOrderAlert.value = null }

    private fun fetch(restaurantId: String) {
        val res = ApiClient.get("/api/partner/orders/$restaurantId")
        if (res.isSuccess) {
            _orders.value = res.dataArray.objects().map { it.toIncomingSubOrder() }
            _error.value = null
        }
    }

    fun acceptOrder(subOrderId: String, prepMinutes: Int = 20, onResult: (Boolean) -> Unit = {}) =
        push(subOrderId, PartnerOrderStatus.ACCEPTED, prepMinutes = prepMinutes, onResult = onResult)

    fun rejectOrder(subOrderId: String, onResult: (Boolean) -> Unit = {}) =
        push(subOrderId, PartnerOrderStatus.REJECTED, onResult = onResult)

    fun updateOrderStatus(
        subOrderId: String,
        newStatus: PartnerOrderStatus,
        assignedRider: String? = null,
        onResult: (Boolean) -> Unit = {}
    ) = push(subOrderId, newStatus, rider = assignedRider, onResult = onResult)

    /** Sends the status to the server; the Customer app's tracker follows within seconds. */
    private fun push(
        subOrderId: String,
        status: PartnerOrderStatus,
        rider: String? = null,
        prepMinutes: Int? = null,
        onResult: (Boolean) -> Unit = {}
    ) {
        val previous = _orders.value
        _orders.value = previous.map {
            if (it.subOrderId == subOrderId) {
                it.copy(
                    status = status,
                    assignedRiderName = rider ?: it.assignedRiderName,
                    estimatedPrepMinutes = prepMinutes ?: it.estimatedPrepMinutes
                )
            } else it
        }

        scope.launch {
            val res = ApiClient.patch("/api/orders/sub-order/$subOrderId/status", JSONObject().apply {
                put("status", status.name)
                rider?.let { put("driverName", it) }
                prepMinutes?.let { put("estimatedPrepMinutes", it) }
            })
            if (!res.isSuccess) {
                _orders.value = previous
                _error.value = res.message
            }
            withContext(Dispatchers.Main) { onResult(res.isSuccess) }
        }
    }

    fun getOrderById(subOrderId: String): IncomingSubOrder? =
        _orders.value.find { it.subOrderId == subOrderId }

    fun clearError() { _error.value = null }
}

private val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}
private val clockFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

/** "2026-08-23T04:31:42.947Z" -> "10:01" in the phone's own timezone. */
internal fun String?.toLocalClockTime(): String {
    if (this.isNullOrBlank()) return ""
    return runCatching { clockFormat.format(isoParser.parse(take(19))!!) }.getOrDefault("")
}

internal fun JSONObject.toIncomingSubOrder(
    parentOrderId: String? = null,
    customerName: String? = null,
    customerPhone: String? = null,
    deliveryAddress: String? = null
): IncomingSubOrder = IncomingSubOrder(
    subOrderId = optString("subOrderId"),
    parentOrderId = parentOrderId ?: optString("parentOrderId"),
    customerName = customerName ?: optString("customerName", "Customer"),
    customerPhone = customerPhone ?: optString("customerPhone"),
    deliveryAddress = deliveryAddress ?: optString("deliveryAddress"),
    items = optJSONArray("items").objects().map {
        OrderItemRecord(
            menuItemId = it.optString("menuItemId"),
            name = it.optString("name"),
            price = it.optDouble("price", 0.0),
            quantity = it.optInt("quantity", 1),
            isVeg = it.optBoolean("isVeg", true),
            imageUrl = it.optString("imageUrl"),
            specialNotes = it.optString("specialNotes")
        )
    },
    subTotal = optDouble("subTotal", 0.0),
    deliveryFee = optDouble("deliveryFee", 0.0),
    discount = optDouble("discount", 0.0),
    status = PartnerOrderStatus.fromApi(optString("status")),
    orderTime = optString("createdAt").toLocalClockTime(),
    estimatedPrepMinutes = optInt("estimatedPrepMinutes", 20),
    assignedRiderName = optString("driverName"),
    specialInstructions = optString("specialInstructions"),
    paymentStatus = optString("paymentStatus", "PAID")
)
