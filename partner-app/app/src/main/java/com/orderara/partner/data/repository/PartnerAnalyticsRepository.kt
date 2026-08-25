package com.orderara.partner.data.repository

import com.orderara.partner.data.models.PartnerDailyAnalytics
import com.orderara.partner.data.network.ApiClient
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

/** Sales figures computed by the server from this restaurant's real orders. */
class PartnerAnalyticsRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var syncJob: Job? = null
    private var restaurantId: String? = null

    private val _analytics = MutableStateFlow(PartnerDailyAnalytics())
    val analytics: StateFlow<PartnerDailyAnalytics> = _analytics.asStateFlow()

    fun start(restaurantId: String) {
        if (this.restaurantId == restaurantId && syncJob?.isActive == true) return
        this.restaurantId = restaurantId
        syncJob?.cancel()
        syncJob = scope.launch {
            while (isActive) {
                fetch(restaurantId)
                delay(10_000)
            }
        }
    }

    fun stop() {
        syncJob?.cancel()
        restaurantId = null
        _analytics.value = PartnerDailyAnalytics()
    }

    fun refresh() {
        restaurantId?.let { id -> scope.launch { fetch(id) } }
    }

    private fun fetch(restaurantId: String) {
        val res = ApiClient.get("/api/partner/analytics/$restaurantId")
        val data = res.data ?: return
        if (!res.isSuccess) return

        _analytics.value = PartnerDailyAnalytics(
            todayRevenue = data.optDouble("todayRevenue", 0.0),
            todayOrdersCount = data.optInt("todayOrdersCount", 0),
            lifetimeOrdersCount = data.optInt("lifetimeOrdersCount", 0),
            lifetimeRevenue = data.optDouble("lifetimeRevenue", 0.0),
            avgPrepTimeMinutes = data.optInt("avgPrepTimeMinutes", 20),
            topSellingItems = data.optJSONArray("topSellingItems").objects()
                .map { it.optString("name") to it.optInt("count") },
            weeklyRevenueTrend = data.optJSONArray("weeklyRevenueTrend").objects()
                .map { it.optString("day") to it.optDouble("revenue", 0.0) }
        )
    }
}
