package com.vigizoomato.customer.data.network

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

/**
 * Live push channel to the backend.
 *
 * The customer app joins its own room, so it receives order status changes and
 * chat messages the moment a restaurant makes them, without waiting for a poll.
 */
object RealtimeClient {

    private const val TAG = "RealtimeClient"
    private var socket: Socket? = null
    private val handlers = mutableMapOf<String, MutableList<(JSONObject) -> Unit>>()

    fun connect(customerId: String) {
        if (socket?.connected() == true) return
        try {
            val opts = IO.Options().apply {
                reconnection = true
                reconnectionDelay = 2000
                transports = arrayOf("websocket")
            }
            socket = IO.socket(ApiConfig.WS_ENDPOINT, opts).apply {
                on(Socket.EVENT_CONNECT) {
                    emit("join", JSONObject().apply {
                        put("role", "customer")
                        put("userId", customerId)
                    })
                    Log.d(TAG, "connected, joined as customer $customerId")
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

    /** Also listen on a specific sub-order's chat thread. */
    fun watchSubOrder(subOrderId: String) {
        socket?.emit("watch:suborder", JSONObject().apply { put("subOrderId", subOrderId) })
    }

    fun on(event: String, handler: (JSONObject) -> Unit) {
        handlers.getOrPut(event) { mutableListOf() }.add(handler)
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
        handlers.clear()
    }

    private val LISTENED_EVENTS = listOf(
        "order:status",
        "chat:new",
        "restaurants:updated",
        "menu:stock",
        "menu:updated",
        "restaurant:settings"
    )
}
