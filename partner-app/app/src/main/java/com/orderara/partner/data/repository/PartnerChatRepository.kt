package com.orderara.partner.data.repository

import com.orderara.partner.data.models.PartnerChatMessage
import com.orderara.partner.data.network.ApiClient
import com.orderara.partner.data.network.ApiConfig
import com.orderara.partner.data.network.RealtimeClient
import com.orderara.partner.data.network.objects
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

/** Chat with the customer, one thread per sub-order. */
class PartnerChatRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollJob: Job? = null
    private var activeSubOrderId: String? = null
    private var realtimeBound = false

    private val _messages = MutableStateFlow<List<PartnerChatMessage>>(emptyList())
    val messages: StateFlow<List<PartnerChatMessage>> = _messages.asStateFlow()

    /** Called when the chat screen opens for one order. */
    fun openThread(subOrderId: String) {
        if (activeSubOrderId == subOrderId && pollJob?.isActive == true) return
        activeSubOrderId = subOrderId
        _messages.value = emptyList()
        pollJob?.cancel()

        if (!realtimeBound) {
            realtimeBound = true
            RealtimeClient.on("chat:new") { payload ->
                if (payload.optString("subOrderId") == activeSubOrderId) {
                    val msg = payload.toChatMessage()
                    if (_messages.value.none { it.id == msg.id }) {
                        _messages.value = _messages.value + msg
                    }
                }
            }
        }

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

    private fun fetch(subOrderId: String) {
        val res = ApiClient.get("/api/chat/$subOrderId")
        if (res.isSuccess && subOrderId == activeSubOrderId) {
            _messages.value = res.dataArray.objects().map { it.toChatMessage() }
        }
    }

    fun sendMessage(subOrderId: String, text: String, senderName: String = "Restaurant Staff") {
        if (text.isBlank()) return
        scope.launch {
            val res = ApiClient.post("/api/chat/$subOrderId", JSONObject().apply {
                put("senderName", senderName)
                put("isFromCustomer", false)
                put("text", text)
            })
            if (res.isSuccess) {
                res.data?.toChatMessage()?.let { msg ->
                    if (_messages.value.none { it.id == msg.id }) {
                        _messages.value = _messages.value + msg
                    }
                }
            }
        }
    }
}

internal fun JSONObject.toChatMessage(): PartnerChatMessage = PartnerChatMessage(
    id = optString("id"),
    subOrderId = optString("subOrderId"),
    senderName = optString("senderName"),
    isFromCustomer = optBoolean("isFromCustomer", false),
    text = optString("text"),
    timestamp = optString("timestamp")
)
