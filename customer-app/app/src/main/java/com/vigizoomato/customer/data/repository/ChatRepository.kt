package com.vigizoomato.customer.data.repository

import com.vigizoomato.customer.data.mock.MockDataProvider
import com.vigizoomato.customer.data.models.ChatMessage
import com.vigizoomato.customer.data.models.SenderType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class ChatRepository {

    private val _messages = MutableStateFlow(MockDataProvider.sampleChatMessages)
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    fun getMessagesForSubOrder(subOrderId: String): List<ChatMessage> {
        return _messages.value.filter { it.subOrderId == subOrderId }
    }

    fun sendMessage(orderId: String, subOrderId: String, restaurantName: String, customerName: String, text: String) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val newMsg = ChatMessage(
            id = "msg_${UUID.randomUUID().toString().take(8)}",
            orderId = orderId,
            subOrderId = subOrderId,
            restaurantName = restaurantName,
            senderType = SenderType.CUSTOMER,
            senderName = customerName,
            messageText = text,
            timestamp = timeFormat
        )
        _messages.value = _messages.value + newMsg
    }
}
