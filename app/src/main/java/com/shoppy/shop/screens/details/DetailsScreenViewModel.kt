package com.shoppy.shop.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shoppy.shop.models.MCart
import kotlinx.coroutines.launch

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
                        // Fetch shop information using shop ID
                        db.collection("Shops").document(shopId)
                            .get()
                            .addOnSuccessListener { shopDoc ->
                                if (shopDoc != null && shopDoc.exists()) {
                                    val shopInfo = ShopInfo(
                                        id = shopId,
                                        name = shopDoc.getString("name") ?: "",
                                        logo = shopDoc.getString("logo")
                                    )
                                    callback(shopInfo)
                                } else {
                                    callback(ShopInfo()) // Empty shop info
                                }
                            }
                            .addOnFailureListener {
                                callback(ShopInfo()) // Empty shop info
                            }
                    } else {
                        callback(ShopInfo()) // Empty shop info
                    }
                } else {
                    callback(ShopInfo()) // Empty shop info
                }
            }
            .addOnFailureListener {
                callback(ShopInfo()) // Empty shop info
            }
    }

    //Delete Product from AllProducts and category using their Category Name and Product ID
    fun deleteProduct(category: String, productId: String){
        viewModelScope.launch {
            db.collection(category).document(productId).delete()
            db.collection("AllProducts").document(productId).delete()
        }
    }
}