package com.orderara.partner.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orderara.partner.OrderAraPartnerApp
import com.orderara.partner.data.models.PartnerChatMessage
import kotlinx.coroutines.flow.*

data class ChatUiState(
    val messages: List<PartnerChatMessage> = emptyList(),
    val inputText: String = ""
)

class PartnerChatViewModel : ViewModel() {
    private val chatRepo = OrderAraPartnerApp.instance.chatRepository
    private val _inputText = MutableStateFlow("")

    val uiState: StateFlow<ChatUiState> = combine(
        chatRepo.messages,
        _inputText
    ) { messages, text ->
        ChatUiState(messages = messages, inputText = text)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChatUiState())

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun sendMessage(subOrderId: String) {
        val text = _inputText.value.trim()
        if (text.isNotEmpty()) {
            chatRepo.sendMessage(subOrderId, text, senderName = "Restaurant Staff")
            _inputText.value = ""
        }
    }
}
