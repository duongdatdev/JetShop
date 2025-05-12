package com.shoppy.shop.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azure.ai.openai.models.ChatCompletions
import com.azure.ai.openai.models.ChatCompletionsOptions
import com.azure.ai.openai.models.ChatRequestMessage
import com.azure.ai.openai.models.ChatRequestUserMessage
import com.azure.ai.openai.models.ChatRequestAssistantMessage
import com.shoppy.shop.ai.AIClient
import com.shoppy.shop.components.ChatMessage
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
            val userMessage = ChatMessage(message = message, isUser = true)
            _messages.update { currentMessages ->
                currentMessages + userMessage
            }
            chatHistory.add(userMessage)

            // Show loading state
            _isLoading.value = true

            try {
                withContext(Dispatchers.IO) {
                    val chatMessages = chatHistory.map { msg ->
                        if (msg.isUser) {
                            ChatRequestUserMessage(msg.message)
                        } else {
                            ChatRequestAssistantMessage(msg.message)
                        }
                    }

                    val options = ChatCompletionsOptions(chatMessages)
                        .setMaxTokens(800)
                        .setTemperature(0.7f.toDouble())
                        .setTopP(0.95f.toDouble())

                    val response = AIClient.chatClient.getChatCompletions("gpt-3.5-turbo", options)
                    
                    response.choices.firstOrNull()?.message?.content?.let { content ->
                        val assistantMessage = ChatMessage(message = content, isUser = false)
                        chatHistory.add(assistantMessage)
                        _messages.update { currentMessages ->
                            currentMessages + assistantMessage
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle exception
                _messages.update { currentMessages ->
                    currentMessages + ChatMessage(
                        message = "An error occurred. Please try again later.",
                        isUser = false
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}