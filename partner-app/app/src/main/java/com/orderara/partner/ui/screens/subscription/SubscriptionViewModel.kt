package com.orderara.partner.ui.screens.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orderara.partner.OrderAraPartnerApp
import com.orderara.partner.data.models.SubscriptionInfo
import kotlinx.coroutines.flow.StateFlow

class SubscriptionViewModel : ViewModel() {
    private val subRepo = OrderAraPartnerApp.instance.subscriptionRepository
    val subscription: StateFlow<SubscriptionInfo> = subRepo.subscription

    fun activatePaidPlan() {
        subRepo.activateMonthlyPaidPlan()
    }
}
