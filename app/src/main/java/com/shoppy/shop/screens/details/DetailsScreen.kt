package com.shoppy.shop.screens.details

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.shoppy.shop.R
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.components.BackButton
import com.shoppy.shop.components.PillButton
import com.shoppy.shop.navigation.BottomNavScreens
import com.shoppy.shop.ui.theme.roboto
import java.text.DecimalFormat
import com.shoppy.shop.utils.UserRoleManager
import com.shoppy.shop.components.RatingStars
import com.shoppy.shop.components.RatingSubmissionForm
import com.shoppy.shop.components.RatingsList
import com.shoppy.shop.components.RatingsSummary
import com.shoppy.shop.viewmodels.RatingViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.text.format
import kotlin.text.toInt

@Composable
fun DetailsScreen(
    navController: NavController, viewModel: DetailsScreenViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    imageUrl:Any?,
    productTitle: String = "",
    productDescription: String = "",
    productPrice: Int = 0,
    stock: Int = 0,
    category: String = "",
    productId: String = "",
) {

    val urlState = remember { mutableStateOf(imageUrl) }
    val titleState = remember { mutableStateOf(productTitle) }
    val descriptionState = remember { mutableStateOf(productDescription) }
    val priceState = remember { mutableStateOf(productPrice) }

    val shopId = remember { mutableStateOf("") }
    val shopName = remember { mutableStateOf("") }
    val shopLogo = remember { mutableStateOf("") }

    val context = LocalContext.current
    val isAdmin = remember { mutableStateOf(false) }
    val isStaff = remember { mutableStateOf(false) }

    // Add rating viewModel
    val ratingViewModel: RatingViewModel = hiltViewModel()
    val ratingsState = ratingViewModel.ratings.collectAsState()
    
    // Track if the user can rate
    val canUserRate = remember { mutableStateOf(false) }
    
    // Get product details with ratings
    LaunchedEffect(Unit) {
        isAdmin.value = UserRoleManager.isAdmin()
        isStaff.value = UserRoleManager.isStaff()

        viewModel.getShopInfoForProduct(productId) { shop ->
            shopId.value = shop.id ?: ""
            shopName.value = shop.name ?: ""
            shopLogo.value = shop.logo.toString()
        }
        
        // Get product details to show ratings
        viewModel.getProductDetails(productId)
        
        // Get ratings for this product
        ratingViewModel.getRatingsByProductId(productId)
        
        // Check if user can rate this product
        viewModel.checkIfUserCanRateProduct(productId) { canRate ->
            canUserRate.value = canRate
        }
    }

    val height = LocalConfiguration.current.screenHeightDp
    val width = LocalConfiguration.current.screenWidthDp

    val buttonTitle = when(stock){
        0 -> "Out Of Stock"
        else -> "Add to cart"
    }

    val textColors = when(buttonTitle){
        "Out Of Stock" -> Color.Black
        else -> Color.White
    }

    Scaffold(
        topBar = {
            //Back Button
            BackButton(navController = navController, category = category, productId = productId, topBarTitle = "Details", spacing = 60.dp)
        },
        bottomBar = { AddToCart(isAdmin,isStaff,email = "", buTitle = buttonTitle,viewModel = viewModel, url = urlState.value, description = descriptionState.value, title = titleState.value, price = priceState.value, stock = stock, category = category, productId = productId, textColor = textColors,navController = navController) },
        modifier = Modifier
            .width(width.dp)
            .height(height.dp),
        backgroundColor = ShopKartUtils.offWhite
    ) { innerPadding ->

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(start = 15.dp, end = 15.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Surface(
                modifier = Modifier
                    .height(350.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),

            ) {

                AsyncImage(
                    model = imageUrl, contentDescription = productTitle,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.placeholder),
                    modifier = Modifier.padding(5.dp).clip(shape = RoundedCornerShape(20.dp)),
                )
            }

            Spacer(modifier = Modifier.padding(top = 10.dp))
            //Shop Info Card
            if (shopId.value.isNotEmpty() && shopName.value.isNotEmpty()) {
                ShopInfoCard(
                    shopId = shopId.value,
                    shopName = shopName.value,
                    shopLogo = shopLogo.value,
                    navController = navController as NavHostController
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp, end = 10.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {

                Text(
                    text = productTitle,
                    style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, fontFamily = roboto),
                    modifier = Modifier.padding(top = 22.dp))
                
                // Display rating if available
                val productDetailsState = viewModel.productDetails.collectAsState()
                val product = productDetailsState.value
                if (product != null && (product.rating_count ?: 0) > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        RatingStars(
                            rating = product.average_rating ?: 0f,
                            starSize = 16
                        )
                        Text(
                            text = "(${product.rating_count} ${if ((product.rating_count ?: 0) == 1) "review" else "reviews"})",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Color.Gray,
                                fontFamily = roboto
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                Text(
                    text = "Description : ",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = roboto),
                    modifier = Modifier.padding(top = 12.dp))

                Text(
                    text = productDescription,
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, fontFamily = roboto, color = Color.Black.copy(alpha = 0.5f)),
//                    maxLines = 8, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.wrapContentHeight())
                
                // Ratings Section
                Text(
                    text = "Reviews",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = roboto),
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
                
                // Show ratings summary and list
                if (product != null && (product.rating_count ?: 0) > 0) {
                    RatingsSummary(
                        averageRating = product.average_rating ?: 0f,
                        ratingCount = product.rating_count ?: 0
                    )
                }
                
                // Show the ratings list
                RatingsList(
                    ratings = ratingsState.value.data ?: emptyList(),
                    isLoading = ratingsState.value.loading ?: false
                )
                
                // Show rating submission form if user can rate
                if (canUserRate.value) {
                    Spacer(modifier = Modifier.height(16.dp))
                    RatingSubmissionForm(
                        productId = productId,
                        viewModel = ratingViewModel,
                        onRatingSubmitted = {
                            // Refresh ratings
                            ratingViewModel.getRatingsByProductId(productId)
                            // Update user can rate status
                            canUserRate.value = false
                            // Refresh product details to get updated average rating
                            viewModel.getProductDetails(productId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ShopInfoCard(
    shopId: String,
    shopName: String,
    shopLogo: String? = null,
    navController: NavHostController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                navController.navigate("${BottomNavScreens.Shop.route}/$shopId/$shopName")
            },
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shop logo
            AsyncImage(
                model = shopLogo ?: R.drawable.default_shop_logo,
                contentDescription = "Shop logo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Shop info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shopName,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "View shop",
                    style = MaterialTheme.typography.caption,
                    color = Color(ShopKartUtils.black.toInt()).copy(alpha = 0.6f)
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Go to shop",
                tint = Color(ShopKartUtils.black.toInt())
            )
        }
    }
}

@Composable
fun AddToCart(isAdmin:  MutableState<Boolean>, isStaff:  MutableState<Boolean>,email: String,buTitle: String,viewModel: DetailsScreenViewModel,url: Any?,description: String,title: String,price: Int,stock: Int,category: String,productId: String,textColor: Color,navController: NavController) {

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    //Hide Add To Cart Option if Admin or Employee is logged in
    if (isAdmin.value || isStaff.value) {
        Box {}
    } else {
        Surface(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 25.dp),
            color = Color.Transparent
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                // Price display
                Text(
                    text = "Price: ₫${DecimalFormat("#,###").format(price.toDouble())}*",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = roboto
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                // Buttons in a row with equal weight
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Buy Now button
                    PillButton(
                        title = "Buy Now",
                        color = ShopKartUtils.blue.toArgb(),
                        textColor = Color.White,
                        shape = 16.dp,
                        modifier = Modifier.weight(1f),
                        enabled = stock > 0
                    ) {
                        if (stock > 0) {
                            val buyNowId = viewModel.buyNowProduct(
                                url = url,
                                title = title,
                                description = description,
                                price = price,
                                stock = stock,
                                category = category,
                                productId = productId
                            )

                            val navHostController = navController as? NavHostController
                                ?: throw IllegalStateException("navController must be NavHostController")
                            navHostController.navigate("${BottomNavScreens.AddressScreen.route}?buyNowId=$buyNowId")

                            haptic.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                            Toast.makeText(context, "Proceeding to checkout", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // Add to Cart button
                    PillButton(
                        title = buTitle,
                        color = ShopKartUtils.black.toInt(),
                        textColor = textColor,
                        shape = 16.dp,
                        modifier = Modifier.weight(1f),
                        enabled = stock > 0
                    ) {
                        viewModel.uploadCartToFirebase(
                            url = url,
                            title = title,
                            description = description,
                            price = price,
                            stock = stock,
                            category = category,
                            productId = productId
                        )

                        haptic.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                        Toast.makeText(context, "Item added to cart", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

//@Preview
//@Composable
//fun Pre(){
//    val navController = rememberNavController()
//    DetailsScreen(navController = navController,"","MacBook Pro","abcdefghasdfghjklertnmdfm","2,00,000")
//}