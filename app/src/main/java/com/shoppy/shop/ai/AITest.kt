package com.shoppy.shop.ai

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log

/**
 * A simple utility class to test the AI integration.
 * This is not meant for production use, just for testing.
 */
object AITest {
    private const val TAG = "AITest"
    private val repository = AIRepository()

    /**
     * Tests a simple AI response.
     * Logs the result to Logcat.
     */
    fun testAI() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    repository.getSimpleAIResponse("Tell me about shopping online")
                }
                
                result.fold(
                    onSuccess = { response ->
                        Log.d(TAG, "AI Response: $response")
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error getting AI response", error)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception in AI test", e)
            }
        }
    }

    /**
     * Tests an AI response with product information.
     * Logs the result to Logcat.
     */
    fun testProductAI() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val productInfo = """
                    Name: Ultra Comfy Running Shoes
                    Price: $89.99
                    Brand: SportFlex
                    Category: Footwear
                    Description: Lightweight running shoes with advanced cushioning technology for maximum comfort during long runs. Features breathable mesh upper and durable rubber outsole.
                """.trimIndent()

                val reviews = """
                    User1: Great shoes, very comfortable for long runs. Highly recommend!
                    User2: The cushioning is amazing, feels like walking on clouds.
                    User3: Sizing runs a bit small, had to exchange for a half size larger.
                    User4: Good quality but the color is a bit different from what's shown in pictures.
                """.trimIndent()

                val result = withContext(Dispatchers.IO) {
                    repository.getAIResponse(
                        "Are these shoes good for marathon training?", 
                        productInfo, 
                        reviews
                    )
                }
                
                result.fold(
                    onSuccess = { response ->
                        Log.d(TAG, "Product AI Response: $response")
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error getting product AI response", error)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception in product AI test", e)
            }
        }
    }
} 