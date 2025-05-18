package com.shoppy.shop.screens.shop

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shoppy.shop.models.MCategory
import com.shoppy.shop.models.MProducts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    // Categories
    val categories = mutableStateListOf<MCategory>()
    private val _isLoadingCategories = MutableStateFlow(false)
    val isLoadingCategories: StateFlow<Boolean> = _isLoadingCategories

    // Selected category
    private val _selectedCategory = MutableStateFlow<MCategory?>(null)
    val selectedCategory: StateFlow<MCategory?> = _selectedCategory

    // Products for selected category
    private val _categoryProducts = MutableStateFlow<List<MProducts>>(emptyList())
    val categoryProducts: StateFlow<List<MProducts>> = _categoryProducts
    
    private val _isLoadingProducts = MutableStateFlow(false)
    val isLoadingProducts: StateFlow<Boolean> = _isLoadingProducts

    fun loadCategories() {
        viewModelScope.launch {
            _isLoadingCategories.value = true
            categories.clear()
            
            try {
                firestore.collection("Categories")
                    .orderBy("created_at", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { documents ->
                        documents.forEach { doc ->
                            doc.toObject(MCategory::class.java)?.let { category ->
                                categories.add(category)
                            }
                        }
                        _isLoadingCategories.value = false
                    }
                    .addOnFailureListener {
                        _isLoadingCategories.value = false
                    }
            } catch (e: Exception) {
                _isLoadingCategories.value = false
            }
        }
    }

    fun selectCategory(category: MCategory) {
        _selectedCategory.value = category
        loadProductsByCategory(category.category_name ?: "")
    }

    private fun loadProductsByCategory(categoryName: String) {
        viewModelScope.launch {
            _isLoadingProducts.value = true
            
            try {
                // First try to get products from the category-named collection
                firestore.collection(categoryName)
                    .get()
                    .addOnSuccessListener { documents ->
                        val products = documents.mapNotNull { doc ->
                            doc.toObject(MProducts::class.java)
                        }
                        
                        if (products.isNotEmpty()) {
                            _categoryProducts.value = products
                            _isLoadingProducts.value = false
                        } else {
                            // If no products found in category collection, try AllProducts with category filter
                            firestore.collection("AllProducts")
                                .whereEqualTo("category", categoryName)
                                .get()
                                .addOnSuccessListener { allProductsDocs ->
                                    val allProductsList = allProductsDocs.mapNotNull { doc ->
                                        doc.toObject(MProducts::class.java)
                                    }
                                    _categoryProducts.value = allProductsList
                                    _isLoadingProducts.value = false
                                }
                                .addOnFailureListener {
                                    _categoryProducts.value = emptyList()
                                    _isLoadingProducts.value = false
                                }
                        }
                    }
                    .addOnFailureListener {
                        _categoryProducts.value = emptyList()
                        _isLoadingProducts.value = false
                    }
            } catch (e: Exception) {
                _categoryProducts.value = emptyList()
                _isLoadingProducts.value = false
            }
        }
    }

    // Clear selected category to return to categories list
    fun clearSelectedCategory() {
        _selectedCategory.value = null
        _categoryProducts.value = emptyList()
    }
} 