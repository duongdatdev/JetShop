package com.shoppy.shop.screens.admin.rolemanagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.shoppy.shop.models.MUser
import com.shoppy.shop.utils.UserRoleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class RoleManagementViewModel @Inject constructor() : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _users = MutableStateFlow<List<MUser>>(emptyList())
    val users: StateFlow<List<MUser>> = _users

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("Users").get().await()
                val userList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(MUser::class.java)
                }
                _users.value = userList
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateUserRole(email: String, newRole: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                UserRoleManager.setUserRole(email, newRole)
                loadUsers()
                onSuccess()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}