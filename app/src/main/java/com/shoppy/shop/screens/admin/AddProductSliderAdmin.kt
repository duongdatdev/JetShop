package com.shoppy.shop.screens.admin

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.components.BackButton
import com.shoppy.shop.components.GalleryLaunchComp
import com.shoppy.shop.components.PillButton
import com.shoppy.shop.components.SelectedImageItem
import com.shoppy.shop.components.TextBox2
import com.shoppy.shop.models.MCategory
import com.shoppy.shop.ui.theme.roboto

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddProductSliderAdmin(
    navHostController: NavHostController,
    viewModel: AdminScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val selectedSliderImageUri = remember { mutableStateOf<Uri?>(null) }
    val selectedProductImageUri = remember { mutableStateOf<Uri?>(null) }

    val launchGallerySlider = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedSliderImageUri.value = uri })

    val launchGalleryProduct = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedProductImageUri.value = uri })

    val productTitle = remember { mutableStateOf("") }
    val productPrice = remember { mutableStateOf("") }
    val productDescription = remember { mutableStateOf("") }
    val stock = remember { mutableStateOf("") }

    val selectedCategory = remember { mutableStateOf<MCategory?>(null) }
    val expanded = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BackButton(
                navController = navHostController,
                topBarTitle = "Add Product/Slider",
                spacing = 30.dp
            )
        },
        modifier = Modifier.fillMaxSize(),
        backgroundColor = ShopKartUtils.offWhite
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.padding(top = 16.dp))

            Text(
                text = "Upload Slider",
                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = roboto)
            )

            GalleryLaunchComp(title = "Select Slider", color = Color.Black.copy(0.1f)) {
                launchGallerySlider.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            if (selectedSliderImageUri.value != null) {
                SelectedImageItem(uris = selectedSliderImageUri.value)
            }

            PillButton(
                title = "Post Slider",
                color = ShopKartUtils.black.toInt(),
                modifier = Modifier.padding(top = 10.dp, bottom = 20.dp)
            ) {
                if (selectedSliderImageUri.value != null) {
                    viewModel.uploadSliderToStorageGetUrl(selectedSliderImageUri.value) {
                        selectedSliderImageUri.value = null
                        navHostController.popBackStack()
                        Toast.makeText(context, "Slider Uploaded", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Select an Image", Toast.LENGTH_SHORT).show()
                }
            }

            Divider()

            Text(
                text = "Upload Product",
                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = roboto),
                modifier = Modifier.padding(top = 20.dp)
            )

            GalleryLaunchComp(title = "Select Product Image", color = Color.Black.copy(0.1f)) {
                launchGalleryProduct.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            if (selectedProductImageUri.value != null) {
                SelectedImageItem(uris = selectedProductImageUri.value)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dropdown for Category Selection
            ExposedDropdownMenuBox(
                expanded = expanded.value,
                onExpandedChange = { expanded.value = !expanded.value }
            ) {
                OutlinedTextField(
                    value = selectedCategory.value?.category_name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false }
                ) {
                    viewModel.categories.forEach { category ->
                        DropdownMenuItem(onClick = {
                            selectedCategory.value = category
                            expanded.value = false
                        }) {
                            Text(category.category_name ?: "", fontFamily = roboto)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextBox2(value = productTitle.value, onChange = productTitle, placeHolder = "Title", keyBoardType = KeyboardType.Text)
            TextBox2(value = productPrice.value, onChange = productPrice, placeHolder = "Price", keyBoardType = KeyboardType.Number)
            TextBox2(value = productDescription.value, onChange = productDescription, placeHolder = "Description", keyBoardType = KeyboardType.Text)
            TextBox2(value = stock.value, onChange = stock, placeHolder = "Stock", keyBoardType = KeyboardType.Number)

            PillButton(
                title = "Post Product",
                color = ShopKartUtils.black.toInt(),
                modifier = Modifier.padding(vertical = 25.dp)
            ) {
                when {
                    selectedProductImageUri.value == null -> Toast.makeText(context, "Please select a product image", Toast.LENGTH_SHORT).show()
                    selectedCategory.value == null -> Toast.makeText(context, "Please select a category", Toast.LENGTH_SHORT).show()
                    productTitle.value.isBlank() -> Toast.makeText(context, "Please enter product title", Toast.LENGTH_SHORT).show()
                    productPrice.value.isBlank() -> Toast.makeText(context, "Please enter product price", Toast.LENGTH_SHORT).show()
                    productDescription.value.isBlank() -> Toast.makeText(context, "Please enter product description", Toast.LENGTH_SHORT).show()
                    stock.value.isBlank() -> Toast.makeText(context, "Please enter stock quantity", Toast.LENGTH_SHORT).show()
                    else -> {
                        viewModel.uploadProductToStorageGetUrl(
                            selectedImageUri = selectedProductImageUri.value,
                            title = productTitle.value,
                            price = productPrice.value,
                            desc = productDescription.value,
                            stock = stock.value,
                            category = selectedCategory.value?.category_name ?: "",
                        ) {
                            Toast.makeText(context, "Product added successfully", Toast.LENGTH_SHORT).show()
                            navHostController.popBackStack()
                        }
                    }
                }
            }
        }
    }
}
