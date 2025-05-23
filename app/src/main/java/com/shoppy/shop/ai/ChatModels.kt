//package com.shoppy.shop.ai
//
//import com.azure.ai.inference.models.ChatRequestMessage
//import com.azure.ai.inference.models.ChatRequestSystemMessage
//import com.azure.ai.inference.models.ChatRequestUserMessage
//import com.azure.ai.inference.models.ChatCompletionsOptions
//
//object ChatModels {
//
//    fun systemMessage(content: String = "You are a helpful AI assistant for ShopKart e-commerce app. Answer customer questions about products based on product information and reviews. Be concise and accurate."): ChatRequestSystemMessage {
//        return ChatRequestSystemMessage(content)
//    }
//
//    fun userMessage(content: String): ChatRequestUserMessage =
//        ChatRequestUserMessage(content)
//
//    /** Wrap into options */
//    fun makeOptions(messages: List<ChatRequestMessage>): ChatCompletionsOptions {
//        return ChatCompletionsOptions(messages)
//            .setModel("openai/gpt-4o-mini")
//    }
//}
package com.shoppy.shop.ai

import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.generationConfig

object ChatModels {
    fun createSystemContent(content: String = "You are a helpful AI assistant for ShopKart e-commerce app. Answer customer questions about products based on product information and reviews. Be concise and accurate."): Content {
        return Content.Builder()
            .text(content)
            .build()
    }

    fun createUserContent(content: String): Content {
        return Content.Builder()
            .text(content)
            .build()
    }

    fun createGenerationConfig() = generationConfig {
        temperature = 0.7f
        topK = 40
        topP = 0.95f
        maxOutputTokens = 1000
    }
}