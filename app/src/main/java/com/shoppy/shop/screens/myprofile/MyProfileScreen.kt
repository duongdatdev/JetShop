package com.shoppy.shop.screens.myprofile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.shoppy.shop.R
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.components.BackButton
import com.shoppy.shop.components.PillButton
import com.shoppy.shop.components.TextBox3
import com.shoppy.shop.ui.theme.roboto

@Composable
fun MyProfileScreen(navController: NavController, viewModel: MyProfileViewModel = hiltViewModel()) {
    val context = LocalContext.current

    // States
    val urlState = remember { mutableStateOf<Uri?>(null) }
    val fbUrl = remember { mutableStateOf<String?>("") }
    val nameState = remember { mutableStateOf("") }
    val emailState = remember { mutableStateOf("") }
    val phoneState = remember { mutableStateOf("") }
    val addressState = remember { mutableStateOf("") }
    val errorState = remember { mutableStateOf("") }
    val scaffoldState = rememberScaffoldState()

    // Only call once
    LaunchedEffect(true) {
        viewModel.getMyProfile(
            profileImage = { fbUrl.value = it },
            name = { nameState.value = it },
            email = { emailState.value = it },
            phone = { phoneState.value = it },
            address = { addressState.value = it }
        )
    }

    val launchGallery = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> urlState.value = uri }
    )

    Scaffold(
        topBar = { BackButton(navController = navController, topBarTitle = "My Profile") },
        scaffoldState = scaffoldState,
        modifier = Modifier.fillMaxSize(),
        backgroundColor = ShopKartUtils.offWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(15.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Surface(
                modifier = Modifier
                    .size(200.dp)
                    .clickable {
                        launchGallery.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                shape = CircleShape
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (urlState.value != null) {
                        AsyncImage(
                            model = urlState.value,
                            contentDescription = "Selected Profile Picture",
                            placeholder = painterResource(id = R.drawable.dummy_profile),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (!fbUrl.value.isNullOrEmpty()) {
                        AsyncImage(
                            model = fbUrl.value,
                            contentDescription = "Firebase Profile Picture",
                            placeholder = painterResource(id = R.drawable.dummy_profile),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_gallery),
                            contentDescription = "Select Image",
                            colorFilter = ColorFilter.tint(Color.Black),
                            contentScale = ContentScale.Inside,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }

            PillButton(
                title = "Remove Profile Photo",
                color = Color.Black.toArgb(),
                modifier = Modifier
                    .width(220.dp)
                    .height(50.dp)
                    .padding(top = 10.dp),
                textSize = 15
            ) {
                viewModel.removeProfilePhoto {
                    Toast.makeText(context, "Profile pic removed", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 15.dp)
                    .height(250.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextBox3(value = nameState.value, label = "Name", onChange = nameState, modifier = Modifier.width(350.dp))
                    TextBox3(value = phoneState.value, label = "Phone no", onChange = phoneState, modifier = Modifier.width(350.dp), keyBoardType = KeyboardType.Number)
                    TextBox3(value = addressState.value, label = "Address", onChange = addressState, modifier = Modifier.width(350.dp), imeAction = ImeAction.Done)
                }
            }

            Text(
                text = errorState.value,
                style = TextStyle(fontWeight = FontWeight.Bold, fontFamily = roboto, color = Color.Red),
                modifier = Modifier.padding(bottom = 15.dp)
            )

            PillButton(title = "Update Profile", color = Color.Black.toArgb()) {
                when {
                    nameState.value.isBlank() || phoneState.value.isBlank() || addressState.value.isBlank() -> {
                        errorState.value = "Fields cannot be empty"
                    }

                    phoneState.value.length != 10 -> {
                        errorState.value = "Enter a valid number"
                    }

                    else -> {
                        errorState.value = ""
                        viewModel.updateProfileImage(
                            imageUrl = urlState.value,
                            name = nameState.value,
                            phone = phoneState.value,
                            address = addressState.value
                        )
                        Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}
