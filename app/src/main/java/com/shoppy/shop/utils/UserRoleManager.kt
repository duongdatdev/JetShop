package com.shoppy.shop.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.shoppy.shop.models.MUser
import kotlinx.coroutines.tasks.await

object UserRoleManager {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getCurrentUserRole(): String {
        val currentUser = auth.currentUser ?: return ""
        return getUserRole(currentUser.email!!)
    }

    suspend fun getUserRole(email: String): String {
        try {
            val docSnapshot = db.collection("Users").document(email).get().await()
            return docSnapshot.getString("role") ?: MUser.ROLE_USER
        } catch (e: Exception) {
            return MUser.ROLE_USER
        }
    }

    suspend fun isAdmin(): Boolean {
        return getCurrentUserRole() == MUser.ROLE_ADMIN
    }

    suspend fun isStaff(): Boolean {
        return getCurrentUserRole() == MUser.ROLE_STAFF
    }

    suspend fun setUserRole(email: String, role: String) {
        db.collection("Users").document(email).update("role", role)
    }
}