package com.shoppy.shop.screens.myorderdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shoppy.shop.models.MRating
import com.shoppy.shop.repository.RatingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyOrderDetailsViewModel @Inject constructor(
    private val ratingRepository: RatingRepository
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    fun getAddressNamePhone(name: (String) -> Unit, phone: (String) -> Unit, address: (String) -> Unit) {
        viewModelScope.launch {
            db.collection("Users").document(currentUser?.email!!).get().addOnSuccessListener { address ->
                address(address.data?.getValue("address").toString())
                name(address.data?.getValue("name").toString())
                phone(address.data?.getValue("phone_no").toString())
            }
        }
    }

    fun cancelOrder(product_title: String) {
        viewModelScope.launch {
            db.collection("Orders").document(currentUser?.uid + product_title).update("delivery_status", "Cancelled")
        }
    }
    
    fun checkIfUserCanRateProduct(productId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (currentUser?.uid == null) {
                callback(false)
                return@launch
            }
            
            try {
                // Check if the user has already rated this product
                val canRate = !ratingRepository.hasUserRatedProduct(currentUser.uid, productId)
                callback(canRate)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }
    
    fun submitRating(productId: String, rating: Int, comment: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (currentUser == null) {
                onError("User not authenticated")
                return@launch
            }
            
            try {
                val newRating = MRating(
                    product_id = productId,
                    user_id = currentUser.uid,
                    user_name = currentUser.displayName ?: currentUser.email?.substringBefore('@') ?: "Anonymous",
                    rating_value = rating,
                    comment = comment,
                    timestamp = System.currentTimeMillis()
                )
                
                val result = ratingRepository.addRating(newRating)
                if (result.isSuccess) {
                    onSuccess()
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Failed to submit rating")
                }
            } catch (e: Exception) {
                onError(e.message ?: "An error occurred")
            }
        }
    }
}