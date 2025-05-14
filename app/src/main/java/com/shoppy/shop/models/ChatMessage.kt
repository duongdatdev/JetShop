package com.shoppy.shop.models

/**
 * Represents a message in a chat conversation.
 *
 * @property content The text content of the message.
 * @property isFromUser Whether the message is from the user (true) or the system/AI (false).
 */
data class ChatMessage(
    val content: String,
    val isFromUser: Boolean
) 