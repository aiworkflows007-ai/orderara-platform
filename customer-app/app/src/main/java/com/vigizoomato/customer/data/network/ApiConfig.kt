package com.vigizoomato.customer.data.network

object ApiConfig {

    /**
     * Flip this to true while testing against a backend running on the laptop.
     * With the phone plugged in over USB, run:
     *     adb reverse tcp:8080 tcp:8080
     * so that "localhost" on the phone reaches the laptop's server.
     */
    private const val USE_LOCAL_BACKEND = true

    private const val LOCAL_URL = "http://127.0.0.1:8080"
    private const val PRODUCTION_URL = "https://restaurant.ai-workflows.cloud"

    val BASE_URL: String get() = if (USE_LOCAL_BACKEND) LOCAL_URL else PRODUCTION_URL
    val API_ENDPOINT: String get() = "$BASE_URL/api"
    val WS_ENDPOINT: String get() = BASE_URL

    /** How often screens re-check the server for changes made in the Partner app. */
    const val POLL_INTERVAL_MS = 4000L
    const val CHAT_POLL_INTERVAL_MS = 3000L

    /** Identity of the signed-in customer — the key that ties orders together. */
    const val CUSTOMER_ID = "user_101"
}
