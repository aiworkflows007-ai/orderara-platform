package com.orderara.partner

import android.app.Application
import com.orderara.partner.data.network.PartnerSession
import com.orderara.partner.data.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OrderAraPartnerApp : Application() {
    lateinit var orderRepository: PartnerOrderRepository
        private set
    lateinit var menuRepository: PartnerMenuRepository
        private set
    lateinit var authRepository: PartnerAuthRepository
        private set
    lateinit var subscriptionRepository: SubscriptionRepository
        private set
    lateinit var chatRepository: PartnerChatRepository
        private set
    lateinit var analyticsRepository: PartnerAnalyticsRepository
        private set

    private val appScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        instance = this

        PartnerSession.init(this)

        orderRepository = PartnerOrderRepository()
        menuRepository = PartnerMenuRepository()
        authRepository = PartnerAuthRepository()
        subscriptionRepository = SubscriptionRepository()
        chatRepository = PartnerChatRepository()
        analyticsRepository = PartnerAnalyticsRepository()

        // Everything in this app is keyed on which restaurant this phone is.
        // When that becomes known (saved session, or fresh registration), point
        // every repository at it; when the owner logs out, shut them down.
        appScope.launch {
            authRepository.restaurantId.collectLatest { id ->
                if (id.isNullOrBlank()) {
                    orderRepository.stop()
                    menuRepository.stop()
                    subscriptionRepository.stop()
                    analyticsRepository.stop()
                } else {
                    orderRepository.start(id)
                    menuRepository.start(id)
                    subscriptionRepository.start(id)
                    analyticsRepository.start(id)
                }
            }
        }

        authRepository.bootstrap()
    }

    companion object {
        lateinit var instance: OrderAraPartnerApp
            private set
    }
}
