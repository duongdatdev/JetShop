package com.shoppy.shop.screens.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.shoppy.shop.models.MProducts
import com.shoppy.shop.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _productsByShop = MutableStateFlow<Resource<List<MProducts>>>(Resource.Loading())
    val productsByShop: StateFlow<Resource<List<MProducts>>> = _productsByShop

    fun getProductsByShop(shopId: String) {
        viewModelScope.launch {
            try {
                _productsByShop.value = Resource.Loading()

                val productsSnapshot = firestore.collection("products")
                    .whereEqualTo("shop_id", shopId)
                    .get()
                    .await()

                val productsList = productsSnapshot.documents.mapNotNull { document ->
                    document.toObject(MProducts::class.java)?.apply {
                        product_id = document.id
                    }
                }

                _productsByShop.value = Resource.Success(productsList)
            } catch (e: Exception) {
                _productsByShop.value = Resource.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}