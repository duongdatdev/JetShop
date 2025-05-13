package com.shoppy.shop.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shoppy.shop.models.MCart
import com.shoppy.shop.models.MProducts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ShopInfo(
    val id: String? = null,
    val name: String? = null,
    val logo: String? = null
)

class DetailsScreenViewModel :ViewModel(){

    private val db = FirebaseFirestore.getInstance()

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val timeStamp = System.currentTimeMillis().toString()
    val emailId = FirebaseAuth.getInstance().currentUser?.email
    
    private val _productDetails = MutableStateFlow<MProducts?>(null)
    val productDetails: StateFlow<MProducts?> = _productDetails.asStateFlow()

    fun uploadCartToFirebase(url: Any?,title: String?,description: String?,price: Int?,stock: Int?,category: String?,productId: String?){

        viewModelScope.launch {
           val cart = MCart(
                timestamp = timeStamp,
                item_count = 1,
                user_id = userId,
                product_url = url,
                product_title = title,
                product_description = description,
                product_price = price,
                stock = stock,
                category = category,
               product_id = productId
            ).convertToMap()

            db.collection("Cart").document(userId + title).set(cart)
        }
    }
    
    fun getProductDetails(productId: String) {
        viewModelScope.launch {
            try {
                val document = db.collection("AllProducts").document(productId).get().await()
                if (document.exists()) {
                    val product = document.toObject(MProducts::class.java)
                    _productDetails.value = product
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getShopInfoForProduct(productId: String, callback: (ShopInfo) -> Unit) {
        // Use Firestore to get the product document
        val db = FirebaseFirestore.getInstance()

        db.collection("AllProducts").document(productId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Get shop ID from the product document
                    val shopId = document.getString("shop_id") ?: ""
                    
                    if (shopId.isNotEmpty()) {
                        // Now get the shop details using the shop ID
                        db.collection("Shops").document(shopId)
                            .get()
                            .addOnSuccessListener { shopDocument ->
                                if (shopDocument != null && shopDocument.exists()) {
                                    val shopInfo = ShopInfo(
                                        id = shopId,
                                        name = shopDocument.getString("name") ?: "",
                                        logo = shopDocument.getString("logo") ?: ""
                                    )
                                    callback(shopInfo)
                                }
                            }
                    }
                }
            }
    }

    fun checkIfUserCanRateProduct(productId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (userId == null) {
                callback(false)
                return@launch
            }
            
            try {
                // Check if the user has already rated this product
                val hasRated = db.collection("Ratings")
                    .whereEqualTo("user_id", userId)
                    .whereEqualTo("product_id", productId)
                    .get()
                    .await()
                    .documents.isNotEmpty()
                
                if (hasRated) {
                    callback(false)
                    return@launch
                }
                
                // Check if the user has ordered and received this product
                val orders = db.collection("Orders")
                    .whereEqualTo("user_id", userId)
                    .whereEqualTo("product_id", productId)
                    .whereEqualTo("delivery_status", "Delivered")
                    .get()
                    .await()
                    .documents
                
                callback(orders.isNotEmpty())
            } catch (e: Exception) {
                callback(false)
            }
        }
    }

    //Delete Product from AllProducts and category using their Category Name and Product ID
    fun deleteProduct(category: String, productId: String){
        viewModelScope.launch {
            db.collection(category).document(productId).delete()
            db.collection("AllProducts").document(productId).delete()
        }
    }
    
    // Function to directly purchase a product without going through the cart
    fun buyNowProduct(url: Any?, title: String?, description: String?, price: Int?, stock: Int?, category: String?, productId: String?): String {
        // Create a temporary document ID for this buy now purchase
        val buyNowId = "buynow_${userId}_${System.currentTimeMillis()}"
        
        viewModelScope.launch {
            val cart = MCart(
                timestamp = timeStamp,
                item_count = 1,
                user_id = userId,
                product_url = url,
                product_title = title,
                product_description = description,
                product_price = price,
                stock = stock,
                category = category,
                product_id = productId
            ).convertToMap()

            // Store in a special BuyNow collection instead of Cart
            db.collection("BuyNow").document(buyNowId).set(cart)
        }
        
        return buyNowId
    }
}