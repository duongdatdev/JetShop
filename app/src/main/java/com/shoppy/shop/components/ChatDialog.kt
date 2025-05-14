package com.shoppy.shop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.models.ChatMessage

@Composable
fun ChatDialog(
    onDismiss: () -> Unit,
    onSendMessage: (String) -> Unit,
    messages: List<ChatMessage>
) {
    var messageText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Chat messages
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    reverseLayout = true
                ) {
                    items(messages.reversed()) { message ->
                        ChatMessageItem(message)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Message input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        placeholder = { Text("Type a message...") },
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.White
                        )
                    )
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                onSendMessage(messageText)
                                messageText = ""
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = ShopKartUtils.blue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(message: ChatMessage) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .padding(4.dp),
            backgroundColor = if (message.isFromUser) ShopKartUtils.blue else Color.LightGray,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(8.dp),
                color = if (message.isFromUser) Color.White else Color.Black
            )
        }
    }
} 