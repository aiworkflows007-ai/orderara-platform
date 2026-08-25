package com.vigizoomato.customer

import android.app.Application
import com.vigizoomato.customer.data.network.ApiConfig
import com.vigizoomato.customer.data.network.RealtimeClient
import com.vigizoomato.customer.data.repository.*

class AppContainer {
    val restaurantRepository: RestaurantRepository by lazy { RestaurantRepository() }
    val cartRepository: CartRepository by lazy { CartRepository() }
    val orderRepository: OrderRepository by lazy { OrderRepository() }
    val authRepository: AuthRepository by lazy { AuthRepository() }
    val chatRepository: ChatRepository by lazy { ChatRepository() }
}

class VigiZoomatoApp : Application() {
    companion object {
        lateinit var container: AppContainer
            private set
    }

    override fun onCreate() {
        super.onCreate()
        // Connect first: repositories register their live-push handlers on
        // construction, and the socket must exist before they do.
        RealtimeClient.connect(ApiConfig.CUSTOMER_ID)
        container = AppContainer()
        // Touch the repositories that poll so they start syncing at launch.
        container.restaurantRepository
        container.orderRepository
    }
}
