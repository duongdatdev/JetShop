package com.shoppy.shop.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.ai.AIViewModel
import com.shoppy.shop.ai.AiResponseState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class ChatMessage(
    val content: String,
    val isFromUser: Boolean
)

// Companion object to store position across recompositions and screen rotations
private object AIButtonPosition {
    // Default values will be overridden
    var hasInitialized = false
    var offsetXRatio = 0.85f // Ratio of screen width
    var offsetYRatio = 0.85f // Ratio of screen height
}

@Composable
fun AIAssistantFloatingButton(
    viewModel: AIViewModel,
    productInfo: String,
    reviews: String
) {
    var showDialog by remember { mutableStateOf(false) }
    
    // Add offset state for dragging - use rememberSaveable to persist across recompositions
    var offsetX by rememberSaveable { mutableStateOf(0f) }
    var offsetY by rememberSaveable { mutableStateOf(0f) }
    
    // Get screen dimensions to constrain dragging
    val configuration = LocalConfiguration.current
    val screenWidth = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    
    // Button size in pixels
    val buttonSizePx = with(LocalDensity.current) { 56.dp.toPx() }
    
    // Set initial position (only once)
    LaunchedEffect(configuration) {
        if (!AIButtonPosition.hasInitialized) {
            // First time initialization with default position at bottom right
            AIButtonPosition.offsetXRatio = 0.85f
            AIButtonPosition.offsetYRatio = 0.85f
            AIButtonPosition.hasInitialized = true
        }
        
        // Calculate position from ratios (allows proper positioning after rotation)
        offsetX = screenWidth * AIButtonPosition.offsetXRatio - buttonSizePx / 2
        offsetY = screenHeight * AIButtonPosition.offsetYRatio - buttonSizePx / 2
        
        // Ensure button stays within bounds
        offsetX = offsetX.coerceIn(0f, screenWidth - buttonSizePx)
        offsetY = offsetY.coerceIn(0f, screenHeight - buttonSizePx)
    }
    
    // Draggable floating button
    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    
                    // Calculate new position
                    val newOffsetX = offsetX + dragAmount.x
                    val newOffsetY = offsetY + dragAmount.y
                    
                    // Constrain the button to stay within screen bounds with padding
                    offsetX = newOffsetX.coerceIn(0f, screenWidth - buttonSizePx)
                    offsetY = newOffsetY.coerceIn(0f, screenHeight - buttonSizePx)
                    
                    // Save position as ratios for persistence across rotations
                    AIButtonPosition.offsetXRatio = (offsetX + buttonSizePx / 2) / screenWidth
                    AIButtonPosition.offsetYRatio = (offsetY + buttonSizePx / 2) / screenHeight
                }
            }
    ) {
        FloatingActionButton(
            onClick = { showDialog = true },
            backgroundColor = ShopKartUtils.blue,
            contentColor = Color.White,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "AI Product Assistant",
                modifier = Modifier.size(24.dp)
            )
        }
    }
    
    AnimatedVisibility(
        visible = showDialog,
        enter = fadeIn() + scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = fadeOut() + scaleOut()
    ) {
        AIAssistantDialog(
            viewModel = viewModel,
            productInfo = productInfo,
            reviews = reviews,
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun AIAssistantDialog(
    viewModel: AIViewModel,
    productInfo: String,
    reviews: String,
    onDismiss: () -> Unit
) {
    val aiState by viewModel.aiResponse.collectAsState()
    var userQuestion by remember { mutableStateOf("") }
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Improved welcome message
    LaunchedEffect(Unit) {
        val productName = productInfo.lines().firstOrNull()?.substringAfter("Product: ")?.trim() ?: "this product"
        chatMessages.add(ChatMessage(
            "Hello! I'm your shopping assistant. I can help answer questions about $productName based on product details and customer reviews. How can I assist you today?",
            false
        ))
    }
    
    // Suggestion chips
    val suggestionChips = remember {
        listOf(
            "Is it worth buying?",
            "Key features?", 
            "Quality concerns?",
            "Sizing information?"
        )
    }
    
    // Track AI response state changes
    LaunchedEffect(aiState) {
        when (val state = aiState) {
            is AiResponseState.Success -> {
                chatMessages.add(ChatMessage(state.response, false))
                // Scroll to bottom
                coroutineScope.launch {
                    listState.animateScrollToItem(chatMessages.size - 1)
                }
            }
            else -> { /* No action needed for other states */ }
        }
    }
    
    Dialog(
        onDismissRequest = {
            viewModel.resetState()
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Product Assistant",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopKartUtils.blue
                        )
                    )
                    
                    IconButton(
                        onClick = {
                            viewModel.resetState()
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // Chat message list
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    elevation = 2.dp,
                    backgroundColor = Color(0xFFF5F5F5)
                ) {
                    Box(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        if (aiState is AiResponseState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(32.dp)
                                    .align(Alignment.Center),
                                color = ShopKartUtils.blue
                            )
                        }
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            state = listState,
                            reverseLayout = false,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(chatMessages) { message ->
                                MessageBubble(message)
                            }
                            
                            // Show suggestion chips only after the welcome message
                            if (chatMessages.size == 1) {
                                item {
                                    SuggestionChips(
                                        suggestions = suggestionChips,
                                        onSuggestionClick = { suggestion ->
                                            chatMessages.add(ChatMessage(suggestion, true))
                                            viewModel.getAIResponse(suggestion, productInfo, reviews)
                                            
                                            // Scroll to bottom
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(chatMessages.size)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Input area
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = userQuestion,
                        onValueChange = { userQuestion = it },
                        placeholder = { Text("Ask about this product...") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = ShopKartUtils.blue,
                            cursorColor = ShopKartUtils.blue
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.size(8.dp))
                    
                    IconButton(
                        onClick = {
                            if (userQuestion.isNotBlank() && aiState !is AiResponseState.Loading) {
                                chatMessages.add(ChatMessage(userQuestion, true))
                                viewModel.getAIResponse(userQuestion, productInfo, reviews)
                                userQuestion = ""
                                
                                // Scroll to bottom
                                coroutineScope.launch {
                                    listState.animateScrollToItem(chatMessages.size - 1)
                                }
                            }
                        },
                        enabled = userQuestion.isNotBlank() && aiState !is AiResponseState.Loading,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ShopKartUtils.blue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send question",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionChips(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            "Try asking:",
            style = TextStyle(
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        suggestions.forEach { suggestion ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = ShopKartUtils.blue.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable { onSuggestionClick(suggestion) }
            ) {
                Text(
                    text = suggestion,
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = ShopKartUtils.blue
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val backgroundColor = if (message.isFromUser) 
        ShopKartUtils.blue else Color.White
    val textColor = if (message.isFromUser) Color.White else Color.Black
    val isUserMessage = message.isFromUser
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (!isUserMessage) 4.dp else 16.dp,
                bottomEnd = if (isUserMessage) 4.dp else 16.dp
            ),
            backgroundColor = backgroundColor,
            elevation = 2.dp,
            modifier = Modifier
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = message.content,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = textColor
                ),
                modifier = Modifier.padding(12.dp)
            )
        }
    }
} 