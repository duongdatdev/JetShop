package com.shoppy.shop.screens.employee

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shoppy.shop.models.MBrand
import com.shoppy.shop.models.MCategory
import com.shoppy.shop.models.MProducts
import com.shoppy.shop.models.MShop
import com.shoppy.shop.models.MSliders
import com.shoppy.shop.repository.ShopRepository
import com.shoppy.shop.utils.CloudinaryUtils
import com.shoppy.shop.utils.ImgBBUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EmployeeScreenViewModel @Inject constructor(
    private val context: Context,
    private val shopRepository: ShopRepository
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val mAuth = FirebaseAuth.getInstance()

    val categories = mutableStateListOf<MCategory>()
    val shopId = mutableStateOf("")
    val shopName = mutableStateOf("")
    val hasShop = mutableStateOf(false)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadCategories()
        loadEmployeeShopInfo()
    }

    private fun loadEmployeeShopInfo() {
        val currentUserId = mAuth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val shopResult = shopRepository.getShopByEmployeeId(currentUserId)
                if (shopResult.isSuccess) {
                    val shop = shopResult.getOrNull()
                    shopId.value = shop?.id ?: ""
                    shopName.value = shop?.name ?: ""
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading shop info: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    fun uploadSliderToStorageGetUrl(selectedImageUris: Uri?, taskDone: () -> Unit = {}) {
        viewModelScope.launch {
            if (selectedImageUris != null) {
                val imageUrl = CloudinaryUtils.uploadImage(context, selectedImageUris)
                if (imageUrl != null) {
                    val sliders = MSliders(slider_image = imageUrl).convertToMap()
                    db.collection("Sliders").add(sliders)
                    taskDone()
                } else {
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun uploadBrand(
        selectedImageUri: Uri?,
        brandName: String,
        taskDone: () -> Unit
    ) {
        viewModelScope.launch {
            if (selectedImageUri != null) {
                val imageUrl = CloudinaryUtils.uploadImage(context, selectedImageUri)
                if (imageUrl != null) {
                    val brands = MBrand(
                        logo = imageUrl,
                        brand_name = brandName
                    ).convertToMap()

                    db.collection("Brands").document(brandName).set(brands)
                    taskDone()
                } else {
                    Toast.makeText(context, "Failed to upload brand image", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    fun removeBrand(brandName: String) {
        viewModelScope.launch {
            db.collection("Brands").document(brandName).delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Brand removed successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        "Failed to remove brand: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
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
        shopId: String? = this.shopId.value,
        shopName: String? = this.shopName.value,
        taskDone: () -> Unit
    ) {
        viewModelScope.launch {
            if (selectedImageUri != null) {
                val productId = UUID.randomUUID().toString()
                val imageUrl = CloudinaryUtils.uploadImage(context, selectedImageUri)

                if (imageUrl != null) {
                    val products = MProducts(
                        product_url = imageUrl,
                        product_title = title,
                        product_price = price.toInt(),
                        product_description = desc,
                        stock = stock.toInt(),
                        category = category,
                        product_id = productId,
                        shop_id = shopId,
                        shop_name = shopName
                    ).convertToMap()

                    db.collection(category).document(productId).set(products)

                    // Do not upload to AllProducts if selected category is BestSeller
                    if (category != "BestSeller") {
                        db.collection("AllProducts").document(productId).set(products)
                    }

                    taskDone()
                } else {
                    Toast.makeText(context, "Failed to upload product image", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                db.collection("Categories")
                    .orderBy("created_at", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { documents ->
                        categories.clear()
                        documents.forEach { doc ->
                            doc.toObject(MCategory::class.java)?.let { category ->
                                categories.add(category)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            "Error loading categories: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error loading categories: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun checkHasShop(callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val employeeId = mAuth.currentUser?.uid ?: return@launch
                val shopResult = shopRepository.getShopByEmployeeId(employeeId)

                if (shopResult.isSuccess) {
                    val shop = shopResult.getOrNull()
                    if (shop != null) {
                        hasShop.value = true
                        shopId.value = shop.id ?: ""
                        shopName.value = shop.name ?: ""
                        callback(true)
                    } else {
                        hasShop.value = false
                        callback(false)
                    }
                } else {
                    hasShop.value = false
                    callback(false)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error checking shop: ${e.message}", Toast.LENGTH_SHORT).show()
                hasShop.value = false
                callback(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getShopInfo(callback: (MShop?) -> Unit) {
        viewModelScope.launch {
            // Using the current user ID from mAuth which is private within the ViewModel
            shopRepository.getShopByEmployeeId(mAuth.currentUser?.uid ?: "")
                .onSuccess { shop ->
                    callback(shop)
                }
                .onFailure {
                    callback(null)
                }
        }
    }

    fun createShop() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val employeeId = mAuth.currentUser?.uid ?: return@launch
                Log.d("EmployeeScreenViewModel", "Creating shop for employee ID: $employeeId")
                val result = shopRepository.initializeShopFromEmployee(employeeId)

                Log.d("EmployeeScreenViewModel", "Result: $result")

                if (result.isSuccess) {
                    val newShopId = result.getOrNull()
                    if (newShopId != null) {
                        // Fetch the created shop to get name and other details
                        val shopResult = shopRepository.getShopById(newShopId)
                        if (shopResult.isSuccess) {
                            val shop = shopResult.getOrNull()
                            if (shop != null) {
                                hasShop.value = true
                                shopId.value = shop.id ?: ""
                                shopName.value = shop.name ?: ""
                                Toast.makeText(context, "Shop created successfully", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "Failed to create shop", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error creating shop: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateShopInfo(
        name: String,
        address: String,
        phone: String,
        email: String,
        description: String,
        logo: String,
        success: () -> Unit,
        error: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val currentUserId = mAuth.currentUser?.uid ?: throw Exception("User not logged in")

                val shopResult = shopRepository.getShopByEmployeeId(currentUserId)
                if (shopResult.isSuccess) {
                    val shop = shopResult.getOrNull() ?: throw Exception("Shop not found")

                    shop.name = name
                    shop.address = address
                    shop.phone = phone
                    shop.email = email
                    shop.description = description
                    shop.logo = logo
                    shop.updated_at = System.currentTimeMillis()

                    val updateResult = shopRepository.updateShopForEmployee(currentUserId, shop)
                    if (updateResult.isSuccess) {
                        shopName.value = name
                        success()
                    } else {
                        error(updateResult.exceptionOrNull()?.message ?: "Unknown error")
                    }
                } else {
                    error(shopResult.exceptionOrNull()?.message ?: "Shop not found")
                }
            } catch (e: Exception) {
                error(e.message ?: "Unknown error")
            }
        }
    }

    fun uploadShopLogo(
        imageUri: Uri,
        success: (String) -> Unit,
        error: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val imageUrl = CloudinaryUtils.uploadImage(context, imageUri)
                Log.d("EmployeeScreenViewModel", "Uploaded image URL: $imageUrl")
                if (imageUrl != null) {
                    success(imageUrl)
                } else {
                    error("Failed to upload shop logo")
                }
            } catch (e: Exception) {
                error(e.message ?: "Unknown error during upload")
            }
        }
    }


}