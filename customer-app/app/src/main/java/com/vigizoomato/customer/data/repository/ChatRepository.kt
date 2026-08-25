package com.vigizoomato.customer.data.repository

import com.vigizoomato.customer.data.models.ChatMessage
import com.vigizoomato.customer.data.models.SenderType
import com.vigizoomato.customer.data.network.ApiClient
import com.vigizoomato.customer.data.network.ApiConfig
import com.vigizoomato.customer.data.network.RealtimeClient
import com.vigizoomato.customer.data.network.objects
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject

/** Chat with a restaurant about one sub-order. */
class ChatRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollJob: Job? = null
    private var activeSubOrderId: String? = null
    private var activeRestaurantName: String = ""
    private var activeOrderId: String = ""

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    init {
        RealtimeClient.on("chat:new") { payload ->
            if (payload.optString("subOrderId") == activeSubOrderId) {
                val msg = payload.toChatMessage(activeOrderId, activeRestaurantName)
                if (_messages.value.none { it.id == msg.id }) {
                    _messages.value = _messages.value + msg
                }
            }
        }
    }

    /** Called when the chat screen opens. */
    fun openThread(orderId: String, subOrderId: String, restaurantName: String) {
        if (activeSubOrderId == subOrderId && pollJob?.isActive == true) return
        activeSubOrderId = subOrderId
        activeOrderId = orderId
        activeRestaurantName = restaurantName
        _messages.value = emptyList()
        pollJob?.cancel()

        RealtimeClient.watchSubOrder(subOrderId)
        pollJob = scope.launch {
            while (isActive) {
                fetch(subOrderId)
                delay(ApiConfig.CHAT_POLL_INTERVAL_MS)
            }
        }
    }

    fun closeThread() {
        pollJob?.cancel()
        activeSubOrderId = null
    }

    fun getMessagesForSubOrder(subOrderId: String): List<ChatMessage> =
        _messages.value.filter { it.subOrderId == subOrderId }

    private fun fetch(subOrderId: String) {
        val res = ApiClient.get("/api/chat/$subOrderId")
        if (res.isSuccess && subOrderId == activeSubOrderId) {
            _messages.value = res.dataArray.objects().map { it.toChatMessage(activeOrderId, activeRestaurantName) }
        }
    }

    fun sendMessage(orderId: String, subOrderId: String, restaurantName: String, customerName: String, text: String) {
        if (text.isBlank()) return
        scope.launch {
            val res = ApiClient.post("/api/chat/$subOrderId", JSONObject().apply {
                put("senderName", customerName)
                put("isFromCustomer", true)
                put("text", text)
            })
            if (res.isSuccess) {
                res.data?.toChatMessage(orderId, restaurantName)?.let { msg ->
                    if (_messages.value.none { it.id == msg.id }) {
                        _messages.value = _messages.value + msg
                    }
                }
            }
        }
    }
}

internal fun JSONObject.toChatMessage(orderId: String, restaurantName: String): ChatMessage {
    val fromCustomer = optBoolean("isFromCustomer", false)
    return ChatMessage(
        id = optString("id"),
        orderId = orderId,
        subOrderId = optString("subOrderId"),
        restaurantName = restaurantName,
        senderType = if (fromCustomer) SenderType.CUSTOMER else SenderType.RESTAURANT_STAFF,
        senderName = optString("senderName"),
        messageText = optString("text"),
        timestamp = optString("timestamp")
    )
}
