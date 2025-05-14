package com.shoppy.shop.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shoppy.shop.ai.AIViewModel
import com.shoppy.shop.ai.AiResponseState
import com.shoppy.shop.models.ChatMessage

@Composable
fun AIMessageScreen(
    productName: String = "Example Product",
    productInfo: String = "",
    reviews: String = "",
    viewModel: AIViewModel = viewModel()
) {
    val aiResponseState = viewModel.aiResponse.collectAsState().value
    var userMessage by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(listOf<ChatMessage>()) }
    
    LaunchedEffect(aiResponseState) {
        when (aiResponseState) {
            is AiResponseState.Success -> {
                chatMessages = chatMessages + ChatMessage(
                    content = aiResponseState.response,
                    isFromUser = false
                )
            }
            is AiResponseState.Error -> {
                chatMessages = chatMessages + ChatMessage(
                    content = "Error: ${aiResponseState.message}",
                    isFromUser = false
                )
            }
            else -> { /* No action needed */ }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "AI Assistant - $productName") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Chat messages area
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(chatMessages.size) { index ->
                    val message = chatMessages[index]
                    MessageBubble(message = message)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                item {
                    if (aiResponseState is AiResponseState.Loading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
            
            // Message input area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userMessage,
                    onValueChange = { userMessage = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask about the product...") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            sendMessage(userMessage, chatMessages, viewModel, productInfo, reviews) { newMessages ->
                                chatMessages = newMessages
                                userMessage = ""
                            }
                        }
                    )
                )
                
                IconButton(
                    onClick = {
                        sendMessage(userMessage, chatMessages, viewModel, productInfo, reviews) { newMessages ->
                            chatMessages = newMessages
                            userMessage = ""
                        }
                    },
                    enabled = userMessage.isNotBlank() && aiResponseState !is AiResponseState.Loading
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}

private fun sendMessage(
    message: String, 
    currentMessages: List<ChatMessage>, 
    viewModel: AIViewModel,
    productInfo: String,
    reviews: String,
    onMessageSent: (List<ChatMessage>) -> Unit
) {
    if (message.isBlank()) return
    
    // Add user message to chat
    val newMessages = currentMessages + ChatMessage(
        content = message,
        isFromUser = true
    )
    onMessageSent(newMessages)
    
    // Get AI response
    viewModel.getAIResponse(message, productInfo, reviews)
}

@Composable
fun MessageBubble(message: ChatMessage) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            backgroundColor = if (message.isFromUser) Color(0xFF1976D2) else Color(0xFFE0E0E0),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = if (message.isFromUser) Color.White else Color.Black,
                fontSize = 16.sp
            )
        }
    }
} 