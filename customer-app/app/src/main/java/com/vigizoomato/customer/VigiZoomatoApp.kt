package com.vigizoomato.customer

import android.app.Application
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
        container = AppContainer()
    }
}
