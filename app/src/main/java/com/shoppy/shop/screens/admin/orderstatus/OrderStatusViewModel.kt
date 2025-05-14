package com.shoppy.shop.screens.admin.orderstatus

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.shoppy.shop.data.DataOrException
import com.shoppy.shop.models.MOrder
import com.shoppy.shop.models.PushNotificationData
import com.shoppy.shop.network.NotificationApi
import com.shoppy.shop.repository.FireOrderStatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderStatusViewModel @Inject constructor(private val fireOrderStatusRepository: FireOrderStatusRepository,private val notificationApiInterface: NotificationApi): ViewModel() {

    val fireStatus: MutableState<DataOrException<List<MOrder>, Boolean, Exception>> = mutableStateOf(DataOrException(listOf(), false, Exception("")))

    val db = FirebaseFirestore.getInstance()

    init {
        getOrderStatusFromFB()
    }

    private fun getOrderStatusFromFB(){
        viewModelScope.launch {

            fireStatus.value = fireOrderStatusRepository.getOrderStatusFromFB()
        }
    }

    fun markOnTheWay(userId: String,product_title: String,success:() -> Unit){
        viewModelScope.launch {
            db.collection("Orders").document(userId + product_title).update("delivery_status","On The Way").addOnSuccessListener {
                success()
            }
        }
    }

    fun markDelivered(userId: String, product_title: String, success: () -> Unit, error: (String) -> Unit = {}) {
        Log.d("OrderStatus", "markDelivered called: userId=$userId, product_title=$product_title")
        viewModelScope.launch {
            Log.d("OrderStatus", "markDelivered called: userId=$userId, product_title=$product_title")
            val docRef = db.collection("Orders").document(userId + product_title)
            Log.d("OrderStatus", "Updating document: ${docRef.path}")
            Log.d("OrderStatus", "Document ID: ${userId + product_title}")
            
            // First check if the document exists
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        Log.d("OrderStatus", "Document exists, current data: ${document.data}")
                        
                        // Now attempt to update
                        docRef.update("delivery_status", "Delivered")
                            .addOnSuccessListener {
                                Log.d("OrderStatus", "Document updated successfully")
                                success()
                            }
                            .addOnFailureListener { e ->
                                Log.e("OrderStatus", "Error marking delivered: ${e.message}", e)
                                error(e.message ?: "Failed to update order status")
                            }
                    } else {
                        Log.e("OrderStatus", "Document does not exist!")
                        error("Document not found")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("OrderStatus", "Error checking document: ${e.message}")
                    error("Failed to check document: ${e.message}")
                }
        }
    }

    fun markCancelled(userId: String, product_title: String, success:() -> Unit){
        viewModelScope.launch {
            db.collection("Orders").document(userId + product_title).update("delivery_status","Cancelled").addOnSuccessListener {
                success()
            }
        }
    }

    fun sendNotification(notification: PushNotificationData){
        viewModelScope.launch {
            try {
                val response = notificationApiInterface.postNotification(notification)
            }catch (e: Exception){

            }
        }
    }
}