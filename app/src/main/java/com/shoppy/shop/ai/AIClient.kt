package com.shoppy.shop.ai

import com.azure.ai.openai.OpenAIClient
import com.azure.ai.openai.OpenAIClientBuilder
import com.azure.core.credential.AzureKeyCredential
import com.shoppy.shop.BuildConfig

object AIClient {
    private val key = AzureKeyCredential(BuildConfig.API_AI_TOKEN)
    private const val ENDPOINT = "https://models.github.ai/inference"

    val chatClient: OpenAIClient by lazy {
        OpenAIClientBuilder()
            .credential(key)
            .endpoint(ENDPOINT)
            .buildClient()
    }
} 