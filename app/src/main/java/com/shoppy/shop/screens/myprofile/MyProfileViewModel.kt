package com.shoppy.shop.screens.myprofile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shoppy.shop.utils.CloudinaryUtils
import com.shoppy.shop.utils.ImgBBUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyProfileViewModel @Inject constructor(
    private val context: Context
) : ViewModel() {

    val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance().collection("Users").document(currentUser?.email!!)

    fun getMyProfile(profileImage:(String?) -> Unit,name:(String) -> Unit,email:(String) -> Unit,phone:(String) -> Unit,address:(String) -> Unit){
        viewModelScope.launch {
            db.get().addOnSuccessListener { doc ->
                profileImage(doc.data?.getValue("profile_image").toString())
                name(doc.data?.getValue("name").toString())
                email(doc.data?.getValue("email").toString())
                phone(doc.data?.getValue("phone_no").toString())
                address(doc.data?.getValue("address").toString())
            }
        }
    }

    fun updateProfileImage(imageUrl: Uri?,name: String,email: String = "",phone: String,address: String){
        viewModelScope.launch {
            if (imageUrl != null) {
                val uploadedImageUrl = CloudinaryUtils.uploadImage(context, imageUrl)
                if (uploadedImageUrl != null) {
                    db.update("profile_image", uploadedImageUrl)
                }
            }

            //Updating user values
            db.update("name", name)
            db.update("phone_no", phone)
            db.update("address", address)
        }
    }

    fun removeProfilePhoto(successRemovePic: () -> Unit){
        db.update("profile_image","").addOnSuccessListener { successRemovePic.invoke() }
    }
}