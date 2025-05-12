package com.shoppy.shop.screens.employee

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.components.BackButton
import com.shoppy.shop.components.PillButton
import com.shoppy.shop.components.ProfileRowComp
import com.shoppy.shop.components.TextBox2
import com.shoppy.shop.navigation.BottomNavScreens
import com.shoppy.shop.screens.employee.EmployeeScreenViewModel
import com.shoppy.shop.ui.theme.roboto

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun EmployeeScreen(
    navController: NavController,
    viewModel: EmployeeScreenViewModel = hiltViewModel()
) {
    val hasShop = remember { mutableStateOf<Boolean?>(null) }
    val isLoading = viewModel.isLoading.value

    LaunchedEffect(Unit) {
        viewModel.checkHasShop { result ->
            if (!result) {
                // No shop exists, automatically create one using user info
                viewModel.createShop()
            }
            // Always set to true as we're automatically creating a shop
            hasShop.value = true
        }
    }

    when {
        hasShop.value == null || isLoading -> {
            // Loading state
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        else -> {
            // Original employee screen with shop management option
            Scaffold(
                topBar = { BackButton(navController = navController, topBarTitle = "Employee") },
                backgroundColor = ShopKartUtils.offWhite
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(20.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ProfileRowComp(
                                title = "Add/Remove Brand"
                            ) { navController.navigate(BottomNavScreens.AddRemoveBrandEmpl.route) }

                            Divider()

                            ProfileRowComp(
                                title = "Add Product/Slider",
                            ) { navController.navigate(BottomNavScreens.AddProductSliderEmpl.route) }
                        }
                    }

                    // Shop Management Section
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(top = 10.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        ProfileRowComp(
                            title = "Manage Shop (${viewModel.shopName.value})",
                        ) { navController.navigate(BottomNavScreens.ShopSettings.route) }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(192.dp)
                            .padding(top = 10.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ProfileRowComp(
                                title = "Ordered Items",
                            ) { navController.navigate(BottomNavScreens.OrderedItemsEmp.route) }

                            Divider()

                            ProfileRowComp(
                                title = "On The Way Items",
                            ) { navController.navigate(BottomNavScreens.OnTheWayItemsEmp.route) }

                            Divider()

                            ProfileRowComp(
                                title = "Delivered Items",
                            ) { navController.navigate(BottomNavScreens.DeliveredItemsEmp.route) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateShopScreen(
    navController: NavHostController,
    viewModel: EmployeeScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val shopName = remember { mutableStateOf("") }
    val shopAddress = remember { mutableStateOf("") }
    val shopPhone = remember { mutableStateOf("") }
    val shopEmail = remember { mutableStateOf("") }
    val shopDescription = remember { mutableStateOf("") }

    val isLoading = remember { mutableStateOf(false) }

    Scaffold(
        topBar = { BackButton(navController = navController, topBarTitle = "Create Shop") },
        backgroundColor = ShopKartUtils.offWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Create Your Shop",
                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = roboto),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            TextBox2(
                value = shopName.value,
                onChange = shopName,
                placeHolder = "Shop Name",
                keyBoardType = KeyboardType.Text
            )

            TextBox2(
                value = shopAddress.value,
                onChange = shopAddress,
                placeHolder = "Shop Address",
                keyBoardType = KeyboardType.Text
            )

            TextBox2(
                value = shopPhone.value,
                onChange = shopPhone,
                placeHolder = "Shop Phone",
                keyBoardType = KeyboardType.Phone
            )

            TextBox2(
                value = shopEmail.value,
                onChange = shopEmail,
                placeHolder = "Shop Email",
                keyBoardType = KeyboardType.Email
            )

            TextBox2(
                value = shopDescription.value,
                onChange = shopDescription,
                placeHolder = "Shop Description",
                keyBoardType = KeyboardType.Text
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading.value) {
                CircularProgressIndicator()
            } else {
                PillButton(
                    title = "Create Shop",
                    color = ShopKartUtils.black.toInt()
                ) {
                    when {
                        shopName.value.isBlank() -> {
                            Toast.makeText(context, "Please enter shop name", Toast.LENGTH_SHORT).show()
                        }
                        shopAddress.value.isBlank() -> {
                            Toast.makeText(context, "Please enter shop address", Toast.LENGTH_SHORT).show()
                        }
                        shopPhone.value.isBlank() -> {
                            Toast.makeText(context, "Please enter shop phone", Toast.LENGTH_SHORT).show()
                        }
                        shopEmail.value.isBlank() -> {
                            Toast.makeText(context, "Please enter shop email", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            isLoading.value = true
                            viewModel.createShop()
                        }
                    }
                }
            }
        }
    }
}


//@Preview
//@Composable
//fun Prev(){
//    EmployeeScreen(navController = rememberNavController())
//}