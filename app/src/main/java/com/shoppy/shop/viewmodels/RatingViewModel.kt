package com.shoppy.shop.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shoppy.shop.data.DataOrException
import com.shoppy.shop.models.MRating
import com.shoppy.shop.repository.RatingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RatingViewModel @Inject constructor(
    private val ratingRepository: RatingRepository
) : ViewModel() {

    private val _ratings = MutableStateFlow<DataOrException<List<MRating>, Boolean, Exception>>(
        DataOrException(listOf(), true, Exception(""))
    )
    val ratings: StateFlow<DataOrException<List<MRating>, Boolean, Exception>> = _ratings.asStateFlow()
    
    val canUserRate = mutableStateOf(false)
    
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    
    fun getRatingsByProductId(productId: String) {
        viewModelScope.launch {
            _ratings.value = DataOrException(listOf(), true, Exception(""))
            _ratings.value = ratingRepository.getRatingsByProductId(productId)
            
            // Check if current user can rate this product
            val userId = auth.currentUser?.uid
            if (userId != null) {
                canUserRate.value = !ratingRepository.hasUserRatedProduct(userId, productId)
                
                // Also check if user has purchased and received the product
                checkIfUserCanRateProduct(userId, productId)
            }
        }
    }
    
    private fun checkIfUserCanRateProduct(userId: String, productId: String) {
        viewModelScope.launch {
            // Query orders to see if this user has purchased and received this product
            db.collection("Orders")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener { documents ->
                    val hasDeliveredOrder = documents.any { document ->
                        val order = document.toObject(com.shoppy.shop.models.MOrder::class.java)
                        order.product_id == productId && order.delivery_status == "Delivered"
                    }
                    
                    // User can rate only if they have a delivered order and haven't rated yet
                    canUserRate.value = hasDeliveredOrder && canUserRate.value
                }
        }
    }
    
    fun submitRating(productId: String, rating: Int, comment: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                onError("User not authenticated")
                return@launch
            }
            
            try {
                val newRating = MRating(
                    product_id = productId,
                    user_id = currentUser.uid,
                    user_name = currentUser.displayName ?: "Anonymous",
                    rating_value = rating,
                    comment = comment,
                    timestamp = System.currentTimeMillis()
                )
                
                val result = ratingRepository.addRating(newRating)
                if (result.isSuccess) {
                    onSuccess()
                    // Refresh ratings for this product
                    getRatingsByProductId(productId)
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Failed to submit rating")
                }
            } catch (e: Exception) {
                onError(e.message ?: "An error occurred")
            }
        }
    }
} 