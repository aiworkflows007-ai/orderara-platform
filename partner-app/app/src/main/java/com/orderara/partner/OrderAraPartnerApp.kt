package com.orderara.partner

import android.app.Application
import com.orderara.partner.data.repository.*

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

    override fun onCreate() {
        super.onCreate()
        instance = this
        orderRepository = PartnerOrderRepository()
        menuRepository = PartnerMenuRepository()
        authRepository = PartnerAuthRepository()
        subscriptionRepository = SubscriptionRepository()
        chatRepository = PartnerChatRepository()
    }

    companion object {
        lateinit var instance: OrderAraPartnerApp
            private set
    }
}
