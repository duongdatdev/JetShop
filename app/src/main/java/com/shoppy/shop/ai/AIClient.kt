package com.shoppy.shop.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.shoppy.shop.BuildConfig

object AIClient {
    private val apiKey = BuildConfig.API_AI_TOKEN
    
    val chatModel: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = apiKey
        )
    }
}

// These dummy classes can be removed now that we're using Gemini
// If there are references to them elsewhere, keep them until those references are updated
class DummyAIClient {
    fun getChatCompletions(deploymentId: String, options: Any): DummyChatResponse {
        return DummyChatResponse()
    }
}

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