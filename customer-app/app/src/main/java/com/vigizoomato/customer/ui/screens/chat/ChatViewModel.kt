package com.vigizoomato.customer.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vigizoomato.customer.VigiZoomatoApp
import com.vigizoomato.customer.data.models.ChatMessage
import com.vigizoomato.customer.data.repository.AuthRepository
import com.vigizoomato.customer.data.repository.ChatRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatUiState(
    val subOrderId: String = "",
    val restaurantName: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = ""
)

class ChatViewModel(
    private val chatRepository: ChatRepository = VigiZoomatoApp.container.chatRepository,
    private val authRepository: AuthRepository = VigiZoomatoApp.container.authRepository
) : ViewModel() {

    private val _subOrderId = MutableStateFlow("")
    private val _restaurantName = MutableStateFlow("")
    private val _inputText = MutableStateFlow("")

    val uiState: StateFlow<ChatUiState> = combine(
        _subOrderId,
        _restaurantName,
        _inputText,
        chatRepository.messages
    ) { sId, rName, text, allMsgs ->
        val filtered = allMsgs.filter { it.subOrderId == sId }
        ChatUiState(
            subOrderId = sId,
            restaurantName = rName,
            messages = filtered,
            inputText = text
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatUiState()
    )

    fun initialize(subOrderId: String, restaurantName: String, orderId: String = "") {
        _subOrderId.value = subOrderId
        _restaurantName.value = restaurantName
        chatRepository.openThread(orderId, subOrderId, restaurantName)
    }

    override fun onCleared() {
        super.onCleared()
        chatRepository.closeThread()
    }

    fun onInputChanged(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isBlank()) return
        val sId = _subOrderId.value
        val rName = _restaurantName.value
        val userName = authRepository.currentUser.value?.name ?: "Customer"

        chatRepository.sendMessage(
            orderId = "",
            subOrderId = sId,
            restaurantName = rName,
            customerName = userName,
            text = text
        )
        _inputText.value = ""
    }
}
