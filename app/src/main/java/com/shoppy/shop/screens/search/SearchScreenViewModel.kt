package com.shoppy.shop.screens.search

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoppy.shop.data.DataOrException
import com.shoppy.shop.models.MProducts
import com.shoppy.shop.repository.FireSearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchScreenViewModel @Inject constructor(private val fireSearchRepository: FireSearchRepository): ViewModel() {

    val fireSearch: MutableState<DataOrException<List<MProducts>, Boolean, Exception>> = mutableStateOf(DataOrException(listOf(), true, Exception("")))


    init {
        getSearchResultFromFB()
    }

    private fun getSearchResultFromFB(){

        viewModelScope.launch {

            fireSearch.value = fireSearchRepository.getSearchResultFromFirebase()

            if (fireSearch.value.data.isNullOrEmpty()) fireSearch.value.loading = false
        }
    }
}