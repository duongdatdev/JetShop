package com.shoppy.shop.ai

import com.azure.ai.inference.models.ChatRequestMessage
import com.azure.ai.inference.models.ChatRequestSystemMessage
import com.azure.ai.inference.models.ChatRequestUserMessage
import com.azure.ai.inference.models.ChatCompletionsOptions

object ChatModels {

    fun systemMessage(content: String = "You are a helpful AI assistant for ShopKart e-commerce app. Answer customer questions about products based on product information and reviews. Be concise and accurate."): ChatRequestSystemMessage {
        return ChatRequestSystemMessage(content)
    }

    fun userMessage(content: String): ChatRequestUserMessage =
        ChatRequestUserMessage(content)

    /** Wrap into options */
    fun makeOptions(messages: List<ChatRequestMessage>): ChatCompletionsOptions {
        return ChatCompletionsOptions(messages)
            .setModel("openai/gpt-4o")
    }
} 