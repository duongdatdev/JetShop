package com.shoppy.shop.screens.employee

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.components.BackButton
import com.shoppy.shop.components.PillButton
import com.shoppy.shop.components.TextBox2
import com.shoppy.shop.ui.theme.roboto
import com.shoppy.shop.R

@Composable
fun ShopSettings(
    navController: NavHostController,
    viewModel: EmployeeScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val shopName = remember { mutableStateOf(viewModel.shopName.value) }
    val shopAddress = remember { mutableStateOf("") }
    val shopPhone = remember { mutableStateOf("") }
    val shopEmail = remember { mutableStateOf("") }
    val shopLogo = remember { mutableStateOf("") }
    val shopDescription = remember { mutableStateOf("") }

    val isLoading = remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isLoading.value = true
            viewModel.uploadShopLogo(uri,
                success = { logoUrl ->
                    shopLogo.value = logoUrl
                    isLoading.value = false
                },
                error = { errorMsg ->
                    Toast.makeText(context, "Error: $errorMsg", Toast.LENGTH_SHORT).show()
                    isLoading.value = false
                }
            )
        }
    }
    LaunchedEffect(Unit) {
        viewModel.checkHasShop { hasShop ->
            if (hasShop) {
                // Shop exists, we can get data from viewModel or repository
                viewModel.getShopInfo { shop ->
                    shop?.let {
                        shopName.value = it.name ?: ""
                        shopAddress.value = it.address ?: ""
                        shopPhone.value = it.phone ?: ""
                        shopEmail.value = it.email ?: ""
                        shopDescription.value = it.description ?: ""
                        shopLogo.value = it.logo ?: ""
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            BackButton(navController = navController, topBarTitle = "Shop Settings", spacing = 40.dp)
        },
        backgroundColor = ShopKartUtils.offWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Update Shop Information",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = roboto
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Logo section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                if (shopLogo.value.isNotEmpty()) {
                    AsyncImage(
                        model = shopLogo.value,
                        contentDescription = "Shop Logo",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.Gray, CircleShape),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.default_shop_logo)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.default_shop_logo),
                        contentDescription = "Default Shop Logo",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.Gray, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { launcher.launch("image/*") },
                    border = BorderStroke(width = 1.dp, color = Color(ShopKartUtils.black.toInt())),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Text(
                        text = "Change Logo",
                        color = Color(ShopKartUtils.black.toInt())
                    )
                }
            }

            TextBox2(
                value = shopName.value,
                onChange = shopName,
                placeHolder = "Shop Name",
                keyBoardType = KeyboardType.Text,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            TextBox2(
                value = shopAddress.value,
                onChange = shopAddress,
                placeHolder = "Shop Address",
                keyBoardType = KeyboardType.Text,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            TextBox2(
                value = shopPhone.value,
                onChange = shopPhone,
                placeHolder = "Shop Phone",
                keyBoardType = KeyboardType.Phone,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            TextBox2(
                value = shopEmail.value,
                onChange = shopEmail,
                placeHolder = "Shop Email",
                keyBoardType = KeyboardType.Email,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            TextBox2(
                value = shopDescription.value,
                onChange = shopDescription,
                placeHolder = "Shop Description",
                keyBoardType = KeyboardType.Text,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading.value) {
                CircularProgressIndicator()
            } else {
                PillButton(
                    title = "Update Shop Information",
                    color = ShopKartUtils.black.toInt()
                ) {
                    if (shopName.value.isBlank()) {
                        Toast.makeText(context, "Shop name cannot be empty", Toast.LENGTH_SHORT).show()
                        return@PillButton
                    }

                    isLoading.value = true

                    viewModel.updateShopInfo(
                        name = shopName.value,
                        address = shopAddress.value,
                        phone = shopPhone.value,
                        email = shopEmail.value,
                        description = shopDescription.value,
                        success = {
                            isLoading.value = false
                            Toast.makeText(context, "Shop information updated successfully", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        error = { errorMessage ->
                            isLoading.value = false
                            Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                        },
                        logo = shopLogo.value
                    )
                }
            }
        }
    }
}