//package com.shoppy.shop.ai
//
//import com.azure.ai.inference.models.ChatCompletions
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//
//class AIRepository {
//    private val client = AIClient.chatClient
//
//    suspend fun getAIResponse(userMessage: String, productInfo: String, reviews: String): Result<String> = withContext(Dispatchers.IO) {
//        try {
//            // Create custom system prompt including product info and reviews
//            val systemPrompt = """
//                You are a helpful AI shopping assistant for ShopKart e-commerce app.
//                Answer customer questions about the following product using only this specific information:
//
//                PRODUCT INFORMATION:
//                $productInfo
//
//                CUSTOMER REVIEWS:
//                $reviews
//
//                Guidelines:
//                - Be concise, friendly, and helpful
//                - Focus only on information from the product details and reviews provided
//                - If asked about features or details not mentioned in the provided information, politely explain you don't have that specific information
//                - Do not make up features or specifications
//                - If there are no reviews, mention that when relevant
//                - Help customers make informed purchase decisions based on the information available
//            """.trimIndent()
//
//            // Create message sequence: system + user
//            val systemMsg = ChatModels.systemMessage(systemPrompt)
//            val userMsg = ChatModels.userMessage(userMessage)
//            val options = ChatModels.makeOptions(listOf(systemMsg, userMsg))
//
//            // Call API synchronously
//            val response: ChatCompletions = client.complete(options)
//            val reply = response.choices[0]
//                .message
//                .content
//                .trim()
//
//            Result.success(reply)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//
//    }
//
//    suspend fun getSimpleAIResponse(userMessage: String): Result<String> = withContext(Dispatchers.IO) {
//        try {
//            // Create system message for general assistance
//            val systemMsg = ChatModels.systemMessage(
//                """
//                You are a helpful shopping assistant for ShopKart e-commerce app.
//                Please provide brief, helpful responses to general shopping questions.
//                For specific product questions, you'll need the customer to view a product page first.
//                """
//            )
//            val userMsg = ChatModels.userMessage(userMessage)
//            val options = ChatModels.makeOptions(listOf(systemMsg, userMsg))
//
//            // Call API synchronously
//            val response: ChatCompletions = client.complete(options)
//            val reply = response.choices[0]
//                .message
//                .content
//                .trim()
//
//            Result.success(reply)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//}

package com.shoppy.shop.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AIRepository {
    private val client = AIClient.chatModel

    suspend fun getAIResponse(userMessage: String, productInfo: String, reviews: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                You are a helpful shopping assistant for e-commerce app.
                
                Product Information:
                $productInfo
                
                Product Reviews:
                $reviews
                
                Customer Question: $userMessage
                
                Please provide a helpful, concise response based on the product information and reviews.
            """.trimIndent()

            val response = client.generateContent(prompt)
            Log.d("AIResponse", "Response: ${response.text}")
            val reply = response.text?.trim() ?: "Sorry, I couldn't generate a response."

            Result.success(reply)
        } catch (e: Exception) {
            Log.d("AIResponse", "Error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getSimpleAIResponse(userMessage: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                You are a helpful shopping assistant for ShopKart e-commerce app.
                Please provide brief, helpful responses to general shopping questions.
                For specific product questions, you'll need the customer to view a product page first.
                
                Customer Question: $userMessage
            """.trimIndent()

            val response = client.generateContent(prompt)
            val reply = response.text?.trim() ?: "Sorry, I couldn't generate a response."

            Result.success(reply)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}