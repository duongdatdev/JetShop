package com.shoppy.shop.screens.employee.orderstatus

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.shoppy.shop.data.DataOrException
import com.shoppy.shop.models.MOrder
import com.shoppy.shop.repository.FireOrderStatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderStatusEmpViewModel @Inject constructor(private val fireOrderStatusRepository: FireOrderStatusRepository): ViewModel() {

    val fireStatus: MutableState<DataOrException<List<MOrder>, Boolean, Exception>> = mutableStateOf(
        DataOrException(listOf(), false, Exception(""))
    )

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

    fun markDelivered(userId: String, product_title: String, success:() -> Unit, error: (String) -> Unit = {}){
        viewModelScope.launch {
            val docRef = db.collection("Orders").document(userId + product_title)
            
            // Log the document path for debugging
            android.util.Log.d("OrderStatusEmp", "Updating document: ${docRef.path} for user $userId and product $product_title")
            android.util.Log.d("OrderStatusEmp", "Document ID: ${userId + product_title}")
            
            // First check if the document exists
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        android.util.Log.d("OrderStatusEmp", "Document exists, current data: ${document.data}")
                        
                        // Now attempt to update
                        docRef.update("delivery_status", "Delivered")
                            .addOnSuccessListener {
                                android.util.Log.d("OrderStatusEmp", "Successfully marked as delivered")
                                success()
                            }
                            .addOnFailureListener { e ->
                                android.util.Log.e("OrderStatusEmp", "Error marking as delivered: ${e.message}")
                                error(e.message ?: "Failed to update order status")
                            }
                    } else {
                        android.util.Log.e("OrderStatusEmp", "Document does not exist!")
                        error("Document not found")
                    }
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("OrderStatusEmp", "Error checking document: ${e.message}")
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
}