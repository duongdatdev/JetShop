package com.shoppy.shop.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.shoppy.shop.data.SuccessOrError
import com.shoppy.shop.models.MUser
import com.shoppy.shop.models.SignInResultData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {

    private val mAuth: FirebaseAuth = Firebase.auth

    private val _state = MutableStateFlow(SuccessOrError())
    val state = _state.asStateFlow()

    private suspend fun retrieveUserRole(email: String): String {
        val docSnapshot = FirebaseFirestore.getInstance().collection("Users").document(email).get().await()
        return docSnapshot.getString("role") ?: MUser.ROLE_USER
    }

    fun loginUser(
        email: String, password: String,
        except: (String) -> Unit = {message ->},
        nav: () -> Unit = {}
    ) {
        viewModelScope.launch {

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    nav()
                }
                .addOnFailureListener {
                    except(it.message.toString())
                }
        }
    }

    fun onSignInResult(resultData: SignInResultData) {
        _state.update { it.copy(
            isSuccess = resultData.data != null,
            error = resultData.errorMessage
        ) }
    }

    //Only used for Google Login
    fun addUserToDB() {
        viewModelScope.launch {
            val currentUser = mAuth.currentUser
            val fb = FirebaseFirestore.getInstance().collection("Users").document(currentUser?.email!!)

            var image: String? = ""
            var phone_no: String? = ""
            var address: String? = ""
            var role = MUser.ROLE_USER

            fb.get().addOnSuccessListener { docSnap ->
                if (docSnap.exists()) {
                    phone_no = docSnap.data?.getValue("phone_no").toString()
                    address = docSnap.data?.getValue("address").toString()
                    image = docSnap.data?.getValue("profile_image").toString()
                    role = docSnap.getString("role") ?: MUser.ROLE_USER
                }
            }.await()

            delay(800)

            val user = MUser(
                id = currentUser.uid,
                name = currentUser.displayName,
                email = currentUser.email,
                password = "Google SignIn",
                phone_no = phone_no?.ifEmpty { "" },
                address = address?.ifEmpty { "" },
                profile_image = image?.ifEmpty { currentUser.photoUrl.toString() },
                role = role
            ).convertToMap()

            fb.set(user)
        }
    }

    fun forgotPassword(email: String, success:() -> Unit, newPassword: String, error:(String) -> Unit){

        viewModelScope.launch {

            mAuth.sendPasswordResetEmail(email).addOnSuccessListener { success()

                FirebaseFirestore.getInstance().collection("Users").document(email).update("password",newPassword)

            }.addOnFailureListener{ e -> error(e.message.toString()) }
        }
    }
}