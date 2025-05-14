package com.shoppy.shop.screens.checkout

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.components.PillButton
import com.shoppy.shop.models.MCart
import com.shoppy.shop.navigation.BottomNavScreens
import com.shoppy.shop.screens.checkout.ordersummary.OrderSummaryScreenViewModel
import com.shoppy.shop.ui.theme.roboto
import java.text.DecimalFormat

@Composable
fun OrderConfirmationScreen(
    navController: NavHostController,
    totalAmount: Int,
    paymentMethod: String,
    buyNowId: String? = null,
    viewModel: OrderSummaryScreenViewModel = hiltViewModel()
) {
    val isBuyNow = buyNowId != null
    val cartItems = remember { mutableStateOf<List<MCart>>(emptyList()) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current
    val deliveryStatus = "Ordered"

    // Fetch cart items or buy now item
    LaunchedEffect(Unit) {
        if (isBuyNow && buyNowId != null) {
            viewModel.getBuyNowItem(buyNowId)
        } else {
            viewModel.getCartItems()
        }
    }

    // Get cart items or buy now item
    if (isBuyNow && buyNowId != null) {
        val buyNowItemState = viewModel.buyNowItem.value
        if (!buyNowItemState.loading!! && buyNowItemState.data != null) {
            cartItems.value = listOf(buyNowItemState.data!!)
        }
    } else {
        if (!viewModel.fireSummary.value.data.isNullOrEmpty()) {
            cartItems.value = viewModel.fireSummary.value.data!!.toList().filter { mCart ->
                mCart.user_id == userId
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Order Confirmation", style = TextStyle(color = Color.White, fontFamily = roboto)) },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable { navController.popBackStack() },
                        tint = Color.White
                    )
                },
                backgroundColor = Color(ShopKartUtils.black)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ShopKartUtils.offWhite)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Order Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Order Summary",
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = roboto
                        )
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Item Details
                    cartItems.value.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.product_title} (x${item.item_count})",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontFamily = roboto
                                )
                            )
                            Text(
                                text = "₫${DecimalFormat("#,###").format(item.product_price!! * item.item_count!!)}",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontFamily = roboto
                                )
                            )
                        }
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Delivery Fee
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Delivery Fee (₫100 per item)",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = roboto
                            )
                        )
                        Text(
                            text = "₫${100 * cartItems.value.size}",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = roboto
                            )
                        )
                    }
                    
                    // GST
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "GST (18%)",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = roboto
                            )
                        )
                        Text(
                            text = "₫180",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = roboto
                            )
                        )
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Total
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Amount",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = roboto
                            )
                        )
                        Text(
                            text = "₫${DecimalFormat("#,###").format(totalAmount)}",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = roboto
                            )
                        )
                    }
                }
            }
            
            // Payment Method Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Payment Method",
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = roboto
                        )
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.Green
                        )
                        Text(
                            text = paymentMethod,
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontFamily = roboto,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
            
            // Shipping Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Shipping Information",
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = roboto
                        )
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // User's shipping address would be displayed here
                    // For now, we'll show placeholder text
                    Text(
                        text = "Your items will be delivered to your default address.",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = roboto
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Estimated delivery: 3-5 business days",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = roboto,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // Space for the bottom bar
        }
        
        // Bottom Bar with Confirm Button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PillButton(
                        title = "Confirm Order",
                        color = ShopKartUtils.black.toInt()
                    ) {
                        // Process the order based on whether it's buy now or cart items
                        if (isBuyNow && buyNowId != null) {
                            viewModel.uploadBuyNowItemToOrders(
                                buyNowId = buyNowId,
                                paymentMethod = paymentMethod,
                                deliveryStatus = deliveryStatus
                            ) {
                                navController.navigate(BottomNavScreens.OrderSuccessScreen.route) {
                                    popUpTo(id = navController.graph.findStartDestination().id)
                                }
                            }
                        } else {
                            viewModel.uploadToOrdersAndDeleteCart(
                                itemsList = cartItems.value,
                                paymentMethod = paymentMethod,
                                deliveryStatus = deliveryStatus
                            ) {
                                navController.navigate(BottomNavScreens.OrderSuccessScreen.route) {
                                    popUpTo(id = navController.graph.findStartDestination().id)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun OrderConfirmationScreenPreview() {
    OrderConfirmationScreen(
        navController = rememberNavController(),
        totalAmount = 1500,
        paymentMethod = "Cash On Delivery"
    )
} 