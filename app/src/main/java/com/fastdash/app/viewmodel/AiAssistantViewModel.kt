package com.fastdash.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fastdash.app.data.repository.AiRepository
import com.fastdash.app.data.repository.AiRepositoryResult
import com.fastdash.app.ui.ai.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

class AiAssistantViewModel(
    private val repository: AiRepository
) : ViewModel() {

    private val nextMessageId = AtomicLong(1L)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun onInputChange(text: String) {
        if (text.length <= 500) {
            _inputText.value = text
        }
    }

    fun sendMessage() {
        sendMessage(_inputText.value)
    }

    fun sendQuickPrompt(prompt: String) {
        sendMessage(prompt)
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun sendMessage(rawText: String) {
        val message = rawText.trim()
        if (message.isBlank() || _isLoading.value) return

        if (message.length > 500) {
            _errorMessage.value = "Tin nhắn không được vượt quá 500 ký tự."
            return
        }

        val userMessage = ChatMessage(
            id = nextMessageId.getAndIncrement(),
            content = message,
            isUser = true
        )
        _messages.value = _messages.value + userMessage
        _inputText.value = ""
        _isLoading.value = true

        viewModelScope.launch {
            when (val result = repository.chat(message)) {
                is AiRepositoryResult.Success -> {
                    val aiResponse = result.data
                    _messages.value = _messages.value + ChatMessage(
                        id = nextMessageId.getAndIncrement(),
                        content = aiResponse.reply,
                        isUser = false,
                        suggestedProducts = aiResponse.suggestedProducts,
                        intent = aiResponse.intent
                    )
                }
                is AiRepositoryResult.Error -> {
                    _errorMessage.value = result.message
                    _messages.value = _messages.value + ChatMessage(
                        id = nextMessageId.getAndIncrement(),
                        content = "Xin lỗi, hiện tại AI chưa thể phản hồi. Vui lòng thử lại sau.",
                        isUser = false
                    )
                }
            }
            _isLoading.value = false
        }
    }
}
