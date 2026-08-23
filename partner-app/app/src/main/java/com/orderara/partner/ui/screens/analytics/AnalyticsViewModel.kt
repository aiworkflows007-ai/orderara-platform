package com.orderara.partner.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orderara.partner.data.models.PartnerDailyAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AnalyticsViewModel : ViewModel() {
    private val _analytics = MutableStateFlow(PartnerDailyAnalytics())
    val analytics: StateFlow<PartnerDailyAnalytics> = _analytics.asStateFlow()
}
