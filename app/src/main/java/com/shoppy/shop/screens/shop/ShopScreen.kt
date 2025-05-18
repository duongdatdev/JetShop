package com.shoppy.shop.screens.shop

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.components.BackButton
import com.shoppy.shop.models.MProducts
import com.shoppy.shop.components.ProductCard
import com.shoppy.shop.utils.Resource

@Composable
fun ShopScreen(
    navController: NavHostController,
    shopId: String,
    shopName: String,
    viewModel: ShopViewModel = hiltViewModel()
) {
    val productsState = viewModel.productsByShop.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(shopId) {
        viewModel.getProductsByShop(shopId)
    }

    Scaffold(
        topBar = {
            BackButton(
                navController = navController,
                topBarTitle = shopName,
                spacing = 35.dp
            )
        },
        backgroundColor = ShopKartUtils.offWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                productsState.value is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                productsState.value.data.isNullOrEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No products found for this shop")
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(count = productsState.value.data?.size ?: 0) { index ->
                            val product = productsState.value.data?.get(index) ?: return@items
                            ProductCard(
                                product = product,
                                onProductClick = {
                                    navController.navigate("details/${product.product_url.toString().encodeForUrl()}/${product.product_title?.encodeForUrl()}/${product.product_description?.encodeForUrl()}/${product.product_price}/${product.stock}/${product.category?.encodeForUrl()}/${product.product_id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun String.encodeForUrl(): String {
    return java.net.URLEncoder.encode(this, "UTF-8")
}

@Composable
fun ProductCard(product: MProducts, onProductClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onProductClick),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            AsyncImage(
                model = product.product_url,
                contentDescription = product.product_title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = product.product_title ?: "",
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Display shop name
                Text(
                    text = "Shop: ${product.shop_name ?: ""}",
                    style = MaterialTheme.typography.caption,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Gray
                )

                Text(
                    text = "đ${product.product_price}",
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}