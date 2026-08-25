package com.orderara.partner.ui.screens.analytics

import androidx.lifecycle.ViewModel
import com.orderara.partner.OrderAraPartnerApp
import com.orderara.partner.data.models.PartnerDailyAnalytics
import kotlinx.coroutines.flow.StateFlow

class AnalyticsViewModel : ViewModel() {
    val analytics: StateFlow<PartnerDailyAnalytics> =
        OrderAraPartnerApp.instance.analyticsRepository.analytics

    fun refresh() {
        OrderAraPartnerApp.instance.analyticsRepository.refresh()
    }
}
