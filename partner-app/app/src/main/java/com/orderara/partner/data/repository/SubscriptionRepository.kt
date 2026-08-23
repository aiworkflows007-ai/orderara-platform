package com.orderara.partner.data.repository

import com.orderara.partner.data.models.SubscriptionInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SubscriptionRepository {
    private val _subscription = MutableStateFlow(SubscriptionInfo())
    val subscription: StateFlow<SubscriptionInfo> = _subscription.asStateFlow()

    fun activateMonthlyPaidPlan() {
        _subscription.update { current ->
            current.copy(
                isTrialActive = false,
                trialDaysRemaining = 0,
                status = "ACTIVE_PAID",
                nextBillingDate = "22 Sept 2026"
            )
        }
    }
}
