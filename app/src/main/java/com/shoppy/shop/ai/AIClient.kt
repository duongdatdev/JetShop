package com.shoppy.shop.ai

import com.azure.ai.inference.ChatCompletionsClient
import com.azure.ai.inference.ChatCompletionsClientBuilder
import com.azure.core.credential.AzureKeyCredential
import com.shoppy.shop.BuildConfig

object AIClient {
    private val key = AzureKeyCredential(BuildConfig.API_AI_TOKEN)
    private const val ENDPOINT = "https://models.github.ai/inference"

    val chatClient: ChatCompletionsClient by lazy {
        ChatCompletionsClientBuilder()
            .credential(key)
            .endpoint(ENDPOINT)
            .buildClient()
    }
}

// A simple mock client to replace the Azure implementation
class DummyAIClient {
    fun getChatCompletions(deploymentId: String, options: Any): DummyChatResponse {
        return DummyChatResponse()
    }
}

// A simple mock response
class DummyChatResponse {
    fun getChoices(): List<DummyChoice> {
        return listOf(DummyChoice())
    }
}

class DummyChoice {
    fun getMessage(): DummyMessage {
        return DummyMessage()
    }
}

class DummyMessage {
    fun getContent(): String {
        return "I'm an AI assistant. Based on the product information and reviews, I can tell you that this is a good product with positive reviews."
    }
} 