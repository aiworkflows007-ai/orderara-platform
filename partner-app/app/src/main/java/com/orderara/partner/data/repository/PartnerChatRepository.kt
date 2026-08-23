package com.orderara.partner.data.repository

import com.orderara.partner.data.mock.PartnerMockData
import com.orderara.partner.data.models.PartnerChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.*

class PartnerChatRepository {
    private val _messages = MutableStateFlow<List<PartnerChatMessage>>(PartnerMockData.initialChatMessages.toList())
    val messages: StateFlow<List<PartnerChatMessage>> = _messages.asStateFlow()

    fun sendMessage(subOrderId: String, text: String, senderName: String = "Restaurant Staff") {
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val newMsg = PartnerChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            subOrderId = subOrderId,
            senderName = senderName,
            isFromCustomer = false,
            text = text,
            timestamp = time
        )
        _messages.update { it + newMsg }
    }
}
