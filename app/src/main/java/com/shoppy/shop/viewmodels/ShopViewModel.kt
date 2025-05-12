package com.shoppy.shop.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoppy.shop.models.MShop
import com.shoppy.shop.repository.ShopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val shopRepository: ShopRepository
) : ViewModel() {

    private val _shopState = MutableStateFlow<ShopState>(ShopState.Idle)
    val shopState: StateFlow<ShopState> = _shopState.asStateFlow()

    private val _currentShop = MutableStateFlow<MShop?>(null)
    val currentShop: StateFlow<MShop?> = _currentShop.asStateFlow()

    fun createShop(shop: MShop) {
        viewModelScope.launch {
            _shopState.value = ShopState.Loading
            try {
                val result = shopRepository.createShop(shop)
                result.fold(
                    onSuccess = { shopId ->
                        shop.id = shopId
                        _currentShop.value = shop
                        _shopState.value = ShopState.Success("Shop created successfully")
                    },
                    onFailure = { error ->
                        _shopState.value = ShopState.Error(error.message ?: "Failed to create shop")
                    }
                )
            } catch (e: Exception) {
                _shopState.value = ShopState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun updateShop(shop: MShop) {
        viewModelScope.launch {
            _shopState.value = ShopState.Loading
            try {
                val result = shopRepository.updateShop(shop)
                result.fold(
                    onSuccess = {
                        _currentShop.value = shop
                        _shopState.value = ShopState.Success("Shop updated successfully")
                    },
                    onFailure = { error ->
                        _shopState.value = ShopState.Error(error.message ?: "Failed to update shop")
                    }
                )
            } catch (e: Exception) {
                _shopState.value = ShopState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun getShopById(shopId: String) {
        viewModelScope.launch {
            _shopState.value = ShopState.Loading
            try {
                val result = shopRepository.getShopById(shopId)
                result.fold(
                    onSuccess = { shop ->
                        _currentShop.value = shop
                        _shopState.value = ShopState.Success("Shop retrieved successfully")
                    },
                    onFailure = { error ->
                        _shopState.value = ShopState.Error(error.message ?: "Failed to get shop")
                    }
                )
            } catch (e: Exception) {
                _shopState.value = ShopState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun getShopsByOwnerId(ownerId: String) {
        viewModelScope.launch {
            _shopState.value = ShopState.Loading
            try {
                val result = shopRepository.getShopsByOwnerId(ownerId)
                result.fold(
                    onSuccess = { shops ->
                        _shopState.value = ShopState.ShopsLoaded(shops)
                    },
                    onFailure = { error ->
                        _shopState.value = ShopState.Error(error.message ?: "Failed to get shops")
                    }
                )
            } catch (e: Exception) {
                _shopState.value = ShopState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun deleteShop(shopId: String) {
        viewModelScope.launch {
            _shopState.value = ShopState.Loading
            try {
                val result = shopRepository.deleteShop(shopId)
                result.fold(
                    onSuccess = {
                        _currentShop.value = null
                        _shopState.value = ShopState.Success("Shop deleted successfully")
                    },
                    onFailure = { error ->
                        _shopState.value = ShopState.Error(error.message ?: "Failed to delete shop")
                    }
                )
            } catch (e: Exception) {
                _shopState.value = ShopState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    sealed class ShopState {
        object Idle : ShopState()
        object Loading : ShopState()
        data class Success(val message: String) : ShopState()
        data class Error(val message: String) : ShopState()
        data class ShopsLoaded(val shops: List<MShop>) : ShopState()
    }
} 