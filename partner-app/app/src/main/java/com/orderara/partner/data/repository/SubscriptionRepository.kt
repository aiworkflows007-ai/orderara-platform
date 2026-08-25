package com.orderara.partner.data.repository

import com.orderara.partner.data.models.InvoiceRecord
import com.orderara.partner.data.models.SubscriptionInfo
import com.orderara.partner.data.network.ApiClient
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
 * The restaurant's subscription, owned by the server.
 *
 * The same record drives the Admin panel — when the platform owner suspends
 * this restaurant, that shows up here and the listing disappears from the
 * Customer app.
 */
class SubscriptionRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var syncJob: Job? = null
    private var restaurantId: String? = null
    private var realtimeBound = false

    private val _subscription = MutableStateFlow(SubscriptionInfo())
    val subscription: StateFlow<SubscriptionInfo> = _subscription.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun start(restaurantId: String) {
        if (this.restaurantId == restaurantId && syncJob?.isActive == true) return
        this.restaurantId = restaurantId
        syncJob?.cancel()

        if (!realtimeBound) {
            realtimeBound = true
            // Admin suspend/reactivate reaches this phone instantly.
            RealtimeClient.on("subscription:updated") { payload ->
                _subscription.value = payload.toSubscriptionInfo()
            }
        }

        // Billing changes are rare, so a slower poll is plenty.
        syncJob = scope.launch {
            while (isActive) {
                fetch(restaurantId)
                delay(15_000)
            }
        }
    }

    fun stop() {
        syncJob?.cancel()
        restaurantId = null
        _subscription.value = SubscriptionInfo()
    }

    fun refresh() {
        restaurantId?.let { id -> scope.launch { fetch(id) } }
    }

    private fun fetch(restaurantId: String) {
        val res = ApiClient.get("/api/partner/subscription/$restaurantId")
        if (res.isSuccess) {
            res.data?.let { _subscription.value = it.toSubscriptionInfo() }
            _error.value = null
        }
    }

    fun activateMonthlyPaidPlan(onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        val id = restaurantId
        if (id == null) {
            onResult(false, "Restaurant not registered yet")
            return
        }
        scope.launch {
            val res = ApiClient.post("/api/partner/subscription/$id/activate")
            if (res.isSuccess) {
                res.data?.let { _subscription.value = it.toSubscriptionInfo() }
                _error.value = null
            } else {
                _error.value = res.message
            }
            withContext(Dispatchers.Main) {
                onResult(res.isSuccess, res.message.takeIf { !res.isSuccess })
            }
        }
    }

    fun clearError() { _error.value = null }
}

private val isoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}
private val friendlyDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

/** "2026-09-05T…" -> "05 Sep 2026" */
internal fun String?.toFriendlyDate(): String {
    if (this.isNullOrBlank()) return ""
    return runCatching { friendlyDate.format(isoDate.parse(take(19))!!) }.getOrDefault(this)
}

internal fun JSONObject.toSubscriptionInfo(): SubscriptionInfo = SubscriptionInfo(
    restaurantId = optString("restaurantId"),
    planName = optString("planName", "Restaurant Unlimited Partner Plan"),
    priceMonthly = optDouble("priceMonthly", 999.0),
    isTrialActive = optBoolean("isTrialActive", false),
    trialDaysRemaining = optInt("trialDaysRemaining", 0),
    trialTotalDays = optInt("trialTotalDays", 14),
    daysUntilDue = optInt("daysUntilDue", 0),
    status = optString("status", "ACTIVE_TRIAL"),
    nextBillingDate = optString("nextBillingDate").toFriendlyDate(),
    graceEndsAt = optString("graceEndsAt").takeIf { it.isNotBlank() && it != "null" }?.toFriendlyDate(),
    suspendedReason = optString("suspendedReason").takeIf { it.isNotBlank() && it != "null" },
    invoices = optJSONArray("invoices").objects().map {
        InvoiceRecord(
            id = it.optString("id"),
            title = it.optString("title"),
            amount = it.optString("amount"),
            status = it.optString("status"),
            date = it.optString("date").toFriendlyDate()
        )
    }
)
