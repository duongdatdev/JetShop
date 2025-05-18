package com.shoppy.shop.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.shoppy.shop.R
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.components.LoadingComp
import com.shoppy.shop.components.ProductCard
import com.shoppy.shop.components.ShopKartAppBar2
import com.shoppy.shop.components.SliderItem
import com.shoppy.shop.models.MBrand
import com.shoppy.shop.models.MCategory
import com.shoppy.shop.models.MProducts
import com.shoppy.shop.models.MSliders
import com.shoppy.shop.navigation.BottomNavScreens
import com.shoppy.shop.ui.theme.roboto
import com.shoppy.shop.utils.UserRoleManager
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel(),
    onAddToCart: (product: MProducts) -> Unit
) {
    val userNameState = remember { mutableStateOf<String?>("") }
    val imageState = remember { mutableStateOf<String?>("") }
    val isDataLoaded = remember { mutableStateOf(false) }
    val isAdmin = remember { mutableStateOf(false) }
    val isStaff = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isAdmin.value = UserRoleManager.isAdmin()
        isStaff.value = UserRoleManager.isStaff()
    }

    // Get username and profile image
    LaunchedEffect(key1 = Unit) {
        viewModel.getUserNameAndImage(
            profile_image = { imageState.value = it }
        ) {
            userNameState.value = it
            isDataLoaded.value = true
        }
    }

    // Handle brand data
    val brandList = if (!viewModel.fireDataBrand.value.data.isNullOrEmpty()) {
        viewModel.fireDataBrand.value.data!!.toList()
    } else emptyList()

    // Handle slider data
    val sliderData = viewModel.fireDataSlider.collectAsStateWithLifecycle().value
    val slidersList = sliderData.data?.toList()

    // Handle product data by category
    val bestSellerProducts = if (!viewModel.fireDataBS.value.data.isNullOrEmpty()) {
        viewModel.fireDataBS.value.data!!.toList()
    } else emptyList()

    // Pull-to-refresh state
    val refreshing = viewModel.isLoading
    val refreshState = rememberPullRefreshState(
        refreshing = refreshing.value,
        onRefresh = { viewModel.pullToRefresh() }
    )

    Scaffold(
        topBar = {
            ShopKartAppBar2(
                userName = userNameState.value,
                profile_url = imageState.value,
                navHostController = navController
            ) {
                navController.navigate(BottomNavScreens.SearchScreen.route)
            }
        },
        backgroundColor = ShopKartUtils.offWhite
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .pullRefresh(state = refreshState),
            contentAlignment = Alignment.TopCenter
        ) {
            // Main content
            AnimatedVisibility(
                visible = !refreshing.value,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Handle loading, error, and content states
                    when {
                        sliderData.loading ?: false -> {
                            LoadingComp()
                        }
                        sliderData.e != null -> {
                            ErrorSection(message = "Could not load data. Please try again.")
                        }
                        slidersList.isNullOrEmpty() -> {
                            EmptyStateSection(
                                isAdmin = isAdmin.value || isStaff.value
                            )
                        }
                        else -> {
                            // Show admin metrics if user is admin
                            if (isAdmin.value) {
                                AdminMetricsSection(viewModel = viewModel)
                            }

                            // Content when data is available
                            ContentSection(
                                slidersList = slidersList,
                                brandList = brandList,
                                bestSellerProducts = bestSellerProducts,
                                categories = viewModel.categories,
                                categoryProducts = viewModel.categoryProducts,
                                navController = navController,
                                onAddToCart = onAddToCart
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }

            // Pull-to-refresh indicator
            PullRefreshIndicator(
                refreshing = refreshing.value,
                state = refreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = Color.White,
                contentColor = Color.Black
            )
        }
    }
}

@Composable
fun AdminMetricsSection(viewModel: HomeViewModel) {
    val totalOrders by viewModel.totalOrders
    val totalProducts by viewModel.totalProducts
    val totalUsers by viewModel.totalUsers
    val isLoading by viewModel.isLoadingMetrics

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Dashboard Metrics",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = roboto
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ShopKartUtils.darkBlue)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricItem(
                        icon = R.drawable.ic_orders,
                        title = "Orders",
                        value = totalOrders.toString(),
                        color = Color(0xFF3F51B5)
                    )

                    MetricItem(
                        icon = R.drawable.ic_cart,
                        title = "Products",
                        value = totalProducts.toString(),
                        color = Color(0xFF4CAF50)
                    )

                    MetricItem(
                        icon = R.drawable.ic_profile,
                        title = "Users",
                        value = totalUsers.toString(),
                        color = Color(0xFFFF9800)
                    )
                }
            }
        }
    }
}

@Composable
fun MetricItem(
    icon: Int,
    title: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            style = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = roboto
            )
        )
        
        Text(
            text = title,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = roboto,
                color = Color.Gray
            )
        )
    }
}

@Composable
fun ContentSection(
    slidersList: List<MSliders>,
    brandList: List<MBrand>,
    bestSellerProducts: List<MProducts>,
    categories: List<MCategory>,
    categoryProducts: Map<String, List<MProducts>>,
    navController: NavHostController,
    onAddToCart: (MProducts) -> Unit
) {
    // Slider section
    SliderItem(slidersList = slidersList)

    // Brands section
    SectionHeader(title = "Popular Brands")
    BrandsList(brands = brandList)

    // Best seller section
//    SectionHeader(title = "Best Sellers")
//    ProductSection(
//        products = bestSellerProducts,
//        navController = navController,
//        onAddToCart = onAddToCart,
//        emptyMessage = "No best sellers available"
//    )

    // Categories divider
    CategoryDivider(navController = navController)

    // Categories and their products
    CategoriesSection(
        categories = categories,
        categoryProducts = categoryProducts,
        navController = navController,
        onAddToCart = onAddToCart
    )
}

@Composable
fun CategoriesSection(
    categories: List<MCategory>,
    categoryProducts: Map<String, List<MProducts>>,
    navController: NavHostController,
    onAddToCart: (MProducts) -> Unit
) {
    if (categories.isEmpty()) {
        Text(
            text = "No categories available",
            style = TextStyle(
                fontSize = 14.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = Color.Gray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center
        )
    } else {
        categories.forEach { category ->
            category.category_name?.let { categoryName ->
                CategorySectionHeader(
                    title = categoryName,
                    category = category,
                    navController = navController
                )

                val products = categoryProducts[categoryName] ?: emptyList()
                ProductSection(
                    products = products,
                    navController = navController,
                    onAddToCart = onAddToCart,
                    emptyMessage = "No products in this category"
                )

                Spacer(modifier = Modifier.height(16.dp))
                Divider(
                    modifier = Modifier
                        .width(300.dp)
                        .padding(vertical = 8.dp),

                    thickness = 0.5.dp
                )
            }
        }
    }
}

@Composable
fun CategorySectionHeader(title: String, category: MCategory, navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = roboto
            )
        )
        
        TextButton(
            onClick = { 
                // Navigate to category screen with the selected category ID
                navController.navigate(BottomNavScreens.Categories.route)
            }
        ) {
            Text(
                text = "See All",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.primary,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = roboto
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp, vertical = 16.dp)
    )
}

@Composable
fun CategoryDivider(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Divider(
            modifier = Modifier
                .width(300.dp)
                .padding(top = 24.dp, bottom = 16.dp)
                .align(Alignment.CenterHorizontally),
            thickness = 1.dp
        )

        Card(
            modifier = Modifier
                .padding(horizontal = 30.dp, vertical = 8.dp)
                .height(60.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            backgroundColor = Color.White,
            elevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Categories",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = roboto
                    )
                )
                
                TextButton(
                    onClick = { 
                        navController.navigate(BottomNavScreens.Categories.route)
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "View All",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ProductSection(
    products: List<MProducts>,
    navController: NavHostController,
    onAddToCart: (MProducts) -> Unit,
    emptyMessage: String
) {
    if (products.isEmpty()) {
        Text(
            text = emptyMessage,
            style = TextStyle(
                fontSize = 14.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = Color.Gray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center
        )
    } else {
        ProductCard(
            cardItem = products,
            navController = navController,
            onAddToCartClick = onAddToCart
        )
    }
}

@Composable
fun EmptyStateSection(isAdmin: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = 16.dp),
            tint = Color.Gray
        )

        Text(
            text = if (isAdmin) "Add some slider images" else "No products available",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = roboto,
                textAlign = TextAlign.Center
            )
        )

        if (isAdmin) {
            Text(
                text = "Go to admin panel to add content",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun ErrorSection(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = 16.dp),
            tint = Color.Red
        )

        Text(
            text = message,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Red
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BrandsList(brands: List<MBrand>) {
    if (brands.isEmpty()) {
        Text(
            text = "No brands available",
            style = TextStyle(
                fontSize = 14.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = Color.Gray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center
        )
    } else {
        LazyRow(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(brands) { brand ->
                BrandCard(brand = brand)
            }
        }
    }
}

@Composable
fun BrandCard(brand: MBrand) {
    Card(
        modifier = Modifier
            .height(60.dp)
            .width(90.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color.White
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = brand.logo,
                contentDescription = brand.brand_name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize(0.8f),
                placeholder = painterResource(id = R.drawable.placeholder)
            )
        }
    }
}