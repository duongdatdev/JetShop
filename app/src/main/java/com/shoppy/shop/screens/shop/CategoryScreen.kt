package com.shoppy.shop.screens.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.components.BackButton
import com.shoppy.shop.components.ProductCard
import com.shoppy.shop.models.MCategory
import com.shoppy.shop.models.MProducts
import com.shoppy.shop.ui.theme.roboto

@Composable
fun CategoryScreen(
    navController: NavHostController,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val categories = viewModel.categories
    val isLoadingCategories = viewModel.isLoadingCategories.collectAsState()
    val selectedCategory = viewModel.selectedCategory.collectAsState()
    val categoryProducts = viewModel.categoryProducts.collectAsState()
    val isLoadingProducts = viewModel.isLoadingProducts.collectAsState()
    
    LaunchedEffect(key1 = true) {
        viewModel.loadCategories()
    }
    
    Scaffold(
        topBar = {
            BackButton(
                navController = navController,
                topBarTitle = if (selectedCategory.value == null) "Categories" else selectedCategory.value?.category_name ?: "",
                spacing = 35.dp
            )
        },
        backgroundColor = ShopKartUtils.offWhite
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedCategory.value == null) {
                // Show categories list
                if (isLoadingCategories.value) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (categories.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No categories available")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(categories) { category ->
                            CategoryItem(category = category) {
                                viewModel.selectCategory(category)
                            }
                        }
                    }
                }
            } else {
                // Show products for selected category
                if (isLoadingProducts.value) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Category header
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .shadow(4.dp, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            backgroundColor = Color.White,
                            elevation = 4.dp
                        ) {
                            Text(
                                text = selectedCategory.value?.category_name ?: "",
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = roboto
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        // Products grid
                        if (categoryProducts.value.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No products available in this category",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = Color.Gray,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(count = categoryProducts.value.size) { index ->
                                    val product = categoryProducts.value[index]
                                    ProductItemCard(
                                        product = product,
                                        onClick = {
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
    }
}

@Composable
fun CategoryItem(category: MCategory, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.category_name ?: "",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = roboto
                    )
                )
                category.category_description?.let {
                    if (it.isNotEmpty()) {
                        Text(
                            text = it,
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Color.Gray
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "View category",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun ProductItemCard(product: MProducts, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color.LightGray)
            ) {
                coil.compose.AsyncImage(
                    model = product.product_url,
                    contentDescription = product.product_title,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = product.product_title ?: "",
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "đ${product.product_price}",
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary
                )
                
                if (product.shop_name != null) {
                    Text(
                        text = "Shop: ${product.shop_name}",
                        style = MaterialTheme.typography.caption,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

private fun String.encodeForUrl(): String {
    return java.net.URLEncoder.encode(this, "UTF-8")
} 