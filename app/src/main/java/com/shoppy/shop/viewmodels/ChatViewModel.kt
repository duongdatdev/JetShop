package com.shoppy.shop.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azure.ai.inference.models.ChatCompletions
import com.shoppy.shop.ai.AIClient
import com.shoppy.shop.ai.ChatModels
import com.shoppy.shop.models.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val chatHistory = mutableListOf<ChatMessage>()

    fun sendMessage(message: String) {
        viewModelScope.launch {
            // Add user message to the list
            val userMessage = ChatMessage(content = message, isFromUser = true)
            _messages.update { currentMessages ->
                currentMessages + userMessage
            }
            chatHistory.add(userMessage)

            // Show loading state
            _isLoading.value = true

            try {
                withContext(Dispatchers.IO) {

                    val systemMsg = ChatModels.systemMessage()
                    val userMsg   = ChatModels.userMessage(userMessage.toString())
                    val options   = ChatModels.makeOptions(listOf(systemMsg, userMsg))

                    // Gọi API đồng bộ
                    val response: ChatCompletions = AIClient.chatClient.complete(options)

                    val content = response.choice
                        .message
                        .content
                        .trim()
                    val assistantMessage = ChatMessage(content = content, isFromUser = false)
                    chatHistory.add(assistantMessage)
                    _messages.update { currentMessages ->
                        currentMessages + assistantMessage
                    }
                }
            } catch (e: Exception) {
                // Handle exception
                _messages.update { currentMessages ->
                    currentMessages + ChatMessage(
                        content = "An error occurred. Please try again later.",
                        isFromUser = false
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}