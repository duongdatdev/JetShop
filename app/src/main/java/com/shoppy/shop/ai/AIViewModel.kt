package com.shoppy.shop.ai

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AIViewModel : ViewModel() {
    private val repository = AIRepository()

    private val _aiResponse = MutableStateFlow<AiResponseState>(AiResponseState.Idle)
    val aiResponse: StateFlow<AiResponseState> = _aiResponse.asStateFlow()

    fun getAIResponse(userMessage: String, productInfo: String = "", reviews: String = "") {
        _aiResponse.value = AiResponseState.Loading
        
        viewModelScope.launch {
            try {
                Log.d("AIViewModel", "Fetching AI response for message: $userMessage")
//                Log.d("AIViewModel", "Product Info: $productInfo")
//                Log.d("AIViewModel", "Reviews: $reviews")
                val result = if (productInfo.isNotBlank() && reviews.isNotBlank()) {
                    Log.d("AIViewModel", "Fetching detailed AI response")
                    repository.getAIResponse(userMessage, productInfo, reviews)
                } else {
                    repository.getSimpleAIResponse(userMessage)
                }
                
                result.fold(
                    onSuccess = { response ->
                        _aiResponse.value = AiResponseState.Success(response)
                    },
                    onFailure = { error ->
                        _aiResponse.value = AiResponseState.Error(error.message ?: "Unknown error")
                    }
                )
            } catch (e: Exception) {
                _aiResponse.value = AiResponseState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun resetState() {
        _aiResponse.value = AiResponseState.Idle
    }
}

sealed class AiResponseState {
    data object Idle : AiResponseState()
    data object Loading : AiResponseState()
    data class Success(val response: String) : AiResponseState()
    data class Error(val message: String) : AiResponseState()
} 