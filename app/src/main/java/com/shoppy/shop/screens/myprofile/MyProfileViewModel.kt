package com.shoppy.shop.screens.myprofile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shoppy.shop.utils.CloudinaryUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MyProfileViewModel @Inject constructor(
    private val context: Context
) : ViewModel() {

    val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance().collection("Users").document(currentUser?.email!!)

    fun getMyProfile(
        profileImage: (String?) -> Unit,
        name: (String) -> Unit,
        email: (String) -> Unit,
        phone: (String) -> Unit,
        address: (String) -> Unit
    ) {
        viewModelScope.launch {
            db.get().addOnSuccessListener { doc ->
                profileImage(doc.data?.get("profile_image").toString())
                name(doc.data?.get("name").toString())
                email(doc.data?.get("email").toString())
                phone(doc.data?.get("phone_no").toString())
                address(doc.data?.get("address").toString())
            }
        }
    }

    fun updateProfileImage(imageUrl: Uri?, name: String, email: String = "", phone: String, address: String) {
        viewModelScope.launch {
            try {
                var uploadedImageUrl: String? = null

                if (imageUrl != null) {
                    uploadedImageUrl = withContext(Dispatchers.IO) {
                        CloudinaryUtils.uploadImage(context, imageUrl)
                    }
                    Log.d("ProfileUpdate", "Uploaded image URL: $uploadedImageUrl")
                }

                val updateData = mutableMapOf<String, Any>(
                    "name" to name,
                    "phone_no" to phone,
                    "address" to address
                )

                if (!uploadedImageUrl.isNullOrEmpty()) {
                    updateData["profile_image"] = uploadedImageUrl
                }

                db.update(updateData)
                    .addOnSuccessListener { Log.d("ProfileUpdate", "Profile updated successfully") }
                    .addOnFailureListener { e -> Log.e("ProfileUpdate", "Failed to update profile", e) }

            } catch (e: Exception) {
                Log.e("ProfileUpdate", "Exception in updateProfileImage: ${e.message}", e)
            }
        }
    }

    fun removeProfilePhoto(successRemovePic: () -> Unit) {
        db.update("profile_image", "")
            .addOnSuccessListener { successRemovePic.invoke() }
            .addOnFailureListener { e -> Log.e("ProfileUpdate", "Failed to remove profile image", e) }
    }
}
