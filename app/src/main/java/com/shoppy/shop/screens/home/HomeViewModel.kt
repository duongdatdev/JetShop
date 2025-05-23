package com.shoppy.shop.screens.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shoppy.shop.data.DataOrException
import com.shoppy.shop.models.MBrand
import com.shoppy.shop.models.MCategory
import com.shoppy.shop.models.MProducts
import com.shoppy.shop.models.MSliders
import com.shoppy.shop.repository.FireRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.text.clear
import kotlin.text.get
import kotlin.text.set

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fireRepositorySlider: FireRepository.FireRepositorySliders,
    private val fireRepositoryBrand: FireRepository.FireRepositoryBrands,
    private val fireRepository: FireRepository.FireRepositoryBestSeller,

    ) : ViewModel() {

    //data with wrapper DataOrException
    val fireDataBrand: MutableState<DataOrException<List<MBrand>, Boolean, Exception>> =
        mutableStateOf(DataOrException(listOf(), true, Exception("")))
    val fireDataSlider: MutableStateFlow<DataOrException<List<MSliders>, Boolean, Exception>> =
        MutableStateFlow(DataOrException(null, true, Exception("")))
    val fireDataBS: MutableState<DataOrException<List<MProducts>, Boolean, Exception>> =
        mutableStateOf(DataOrException(listOf(), true, Exception("")))

    val categories = mutableStateListOf<MCategory>()
    val categoryProducts = mutableStateMapOf<String, List<MProducts>>()

    // Admin metrics
    val totalOrders = mutableStateOf(0)
    val totalProducts = mutableStateOf(0)
    val totalUsers = mutableStateOf(0)
    val isLoadingMetrics = mutableStateOf(false)

    private val _isLoadingCategories = mutableStateOf(false)
    val isLoadingCategories = _isLoadingCategories

    //Pull to Refresh
    private val _isLoading = mutableStateOf(false)
    val isLoading = _isLoading

    private val mAuth = FirebaseAuth.getInstance()

    val currentUser = mAuth.currentUser

    init {
        getBrandsFromFB()
        getSlidersFromFB()
        getBestSellerFromFB()
        getCategories()
        // Fetch admin metrics
        getAdminMetrics()
//        delete()
    }

    private fun getAdminMetrics() {
        viewModelScope.launch {
            isLoadingMetrics.value = true
            
            // Get total orders
            FirebaseFirestore.getInstance().collection("Orders")
                .get()
                .addOnSuccessListener { documents ->
                    totalOrders.value = documents.size()
                }
                
            // Get total products
            FirebaseFirestore.getInstance().collection("AllProducts")
                .get()
                .addOnSuccessListener { documents ->
                    totalProducts.value = documents.size()
                }
                
            // Get total users
            FirebaseFirestore.getInstance().collection("Users")
                .get()
                .addOnSuccessListener { documents ->
                    totalUsers.value = documents.size()
                    isLoadingMetrics.value = false
                }
                .addOnFailureListener {
                    isLoadingMetrics.value = false
                }
        }
    }

    fun getUserNameAndImage(profile_image: (String?) -> Unit, user: (String?) -> Unit) {
        val email = mAuth.currentUser?.email

        // Check if user is logged in and has an email
        if (email == null) {
            // Handle case when no user is logged in
            user("")
            profile_image("")
            return
        }

        // Now that we've confirmed email is not null
        if (email.contains("employee.")) {
            user("")
            profile_image("")
        } else {
            FirebaseFirestore.getInstance().collection("Users").document(email).get()
                .addOnSuccessListener { document ->
                    user(document.data?.getValue("name")?.toString() ?: "")
                    profile_image(document.data?.getValue("profile_image")?.toString() ?: "")
                }
                .addOnFailureListener {
                    // Handle potential failure
                    user("")
                    profile_image("")
                }
        }
    }

    //Getting Sliders From Firebase
    private fun getBrandsFromFB() {

        viewModelScope.launch {
            fireDataBrand.value.loading = true
            fireDataBrand.value = fireRepositoryBrand.getBrandsFromFB()

            if (!fireDataBrand.value.data.isNullOrEmpty()) fireDataBrand.value.loading = false

        }
    }

    private fun getSlidersFromFB() {

        viewModelScope.launch {
            fireDataSlider.update { fireRepositorySlider.getSlidersFromFB() }
        }
    }

    //Getting Products from Firebase
    private fun getBestSellerFromFB() {

        viewModelScope.launch {
            fireDataBS.value.loading = true
            fireDataBS.value = fireRepository.getBestSellerFromFB()

            if (!fireDataBS.value.data.isNullOrEmpty()) fireDataBS.value.loading = false

        }
    }

    private fun getCategories() {
        viewModelScope.launch {
            _isLoadingCategories.value = true
            FirebaseFirestore.getInstance().collection("Categories")
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    categories.clear()
                    documents.forEach { doc ->
                        doc.toObject(MCategory::class.java)?.let { category ->
                            categories.add(category)
                            getCategoryProducts(category.category_name ?: "")
                        }
                    }
                    _isLoadingCategories.value = false
                }
                .addOnFailureListener {
                    _isLoadingCategories.value = false
                }
        }
    }

    // Add function to get products by category
    private fun getCategoryProducts(categoryName: String) {
        viewModelScope.launch {
            FirebaseFirestore.getInstance().collection(categoryName)
                .get()
                .addOnSuccessListener { documents ->
                    val products = documents.mapNotNull { doc ->
                        doc.toObject(MProducts::class.java)
                    }
                    categoryProducts[categoryName] = products
                }
        }
    }

    // Update pull to refresh to include categories
    fun pullToRefresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _isLoadingCategories.value = true
            fireDataSlider.value.loading = true
            isLoadingMetrics.value = true

            fireDataSlider.value = fireRepositorySlider.getSlidersFromFB()
            fireDataBrand.value = fireRepositoryBrand.getBrandsFromFB()
            fireDataBS.value = fireRepository.getBestSellerFromFB()
            getCategories()
            getAdminMetrics()

            _isLoading.value = false
            fireDataSlider.value.loading = false
        }
    }
}