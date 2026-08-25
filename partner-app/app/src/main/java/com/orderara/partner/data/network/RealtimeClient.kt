package com.orderara.partner.data.network

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

/**
 * Live push channel to the backend.
 *
 * The Partner app joins the room for its own restaurant, so a customer order
 * rings this phone the instant it is placed — and no other restaurant's phone.
 */
object RealtimeClient {

    private const val TAG = "RealtimeClient"
    private var socket: Socket? = null
    private var joinedRestaurantId: String? = null
    private val handlers = mutableMapOf<String, MutableList<(JSONObject) -> Unit>>()

    fun connect(restaurantId: String) {
        if (socket?.connected() == true && joinedRestaurantId == restaurantId) return
        if (socket != null && joinedRestaurantId != restaurantId) disconnect()

        joinedRestaurantId = restaurantId
        try {
            val opts = IO.Options().apply {
                reconnection = true
                reconnectionDelay = 2000
                transports = arrayOf("websocket")
            }
            socket = IO.socket(ApiConfig.WS_ENDPOINT, opts).apply {
                on(Socket.EVENT_CONNECT) {
                    emit("join", JSONObject().apply {
                        put("role", "partner")
                        put("restaurantId", restaurantId)
                    })
                    Log.d(TAG, "connected, joined restaurant $restaurantId")
                }
                LISTENED_EVENTS.forEach { event ->
                    on(event) { args ->
                        (args.firstOrNull() as? JSONObject)?.let { payload ->
                            handlers[event]?.forEach { handler ->
                                runCatching { handler(payload) }
                                    .onFailure { Log.e(TAG, "handler for $event failed: ${it.message}") }
                            }
                        }
                    }
                }
                connect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "could not connect: ${e.message}")
        }
    }

    fun on(event: String, handler: (JSONObject) -> Unit) {
        handlers.getOrPut(event) { mutableListOf() }.add(handler)
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
        joinedRestaurantId = null
        handlers.clear()
    }

    private val LISTENED_EVENTS = listOf(
        "order:new",
        "order:status",
        "chat:new",
        "subscription:updated",
        "menu:updated"
    )
}
