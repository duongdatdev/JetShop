package com.shoppy.shop.screens.employee

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.shoppy.shop.models.MBrand
import com.shoppy.shop.models.MProducts
import com.shoppy.shop.models.MSliders
import com.shoppy.shop.models.MCategory
import com.shoppy.shop.utils.ImgBBUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EmployeeScreenViewModel @Inject constructor(
    private val context: Context
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    val categories = mutableStateListOf<MCategory>()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            db.collection("Categories")
                .get()
                .addOnSuccessListener { documents ->
                    categories.clear()
                    documents.forEach { doc ->
                        doc.toObject(MCategory::class.java)?.let { category ->
                            categories.add(category)
                        }
                    }
                }
        }
    }

    fun uploadSliderToStorageGetUrl(selectedImageUris: Uri?, taskDone: () -> Unit = {}) {
        viewModelScope.launch {
            if (selectedImageUris != null) {
                val imageUrl = ImgBBUtils.uploadImage(context, selectedImageUris)
                if (imageUrl != null) {
                    val sliders = MSliders(slider_image = imageUrl).convertToMap()
                    db.collection("Sliders").add(sliders)
                    taskDone()
                }
            }
        }
    }

    fun uploadProductToStorageGetUrl(
        selectedImageUri: Uri?,
        title: String,
        price: String,
        desc: String,
        stock: String,
        category: String,
        shopId: String,
        shopName: String,
        taskDone: () -> Unit
    ) {
        viewModelScope.launch {
            if (selectedImageUri != null) {
                val productId = UUID.randomUUID().toString()
                val imageUrl = ImgBBUtils.uploadImage(context, selectedImageUri)
                
                if (imageUrl != null) {
                    val products = MProducts(
                        product_url = imageUrl,
                        product_title = title,
                        product_price = price.toInt(),
                        product_description = desc,
                        stock = stock.toInt(),
                        category = category,
                        shop_id = shopId,
                        shop_name = shopName,
                        product_id = productId
                    ).convertToMap()

                    db.collection(category).document(productId).set(products)
                    db.collection("Shops/$shopId/Products").document(productId)

                    //Do not upload to AllProducts if selected category is BestSeller
                    if (category != "BestSeller") {
                        db.collection("AllProducts").document(productId).set(products)
                    }
                }
            }
            taskDone()
        }
    }

    fun uploadBrand(selectedImageUri: Uri?,
                    brandName: String,
                    taskDone: () -> Unit) {
        viewModelScope.launch {
            if (selectedImageUri != null) {
                val imageUrl = ImgBBUtils.uploadImage(context, selectedImageUri)
                if (imageUrl != null) {
                    val brands = MBrand(
                        logo = imageUrl,
                        brand_name = brandName
                    ).convertToMap()

                    db.collection("Brands").document(brandName).set(brands)
                }
            }
            taskDone()
        }
    }

    fun removeBrand(brandName: String) {
        viewModelScope.launch {
            db.collection("Brands").document(brandName).delete()
        }
    }
}