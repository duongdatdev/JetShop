package com.shoppy.shop.screens.myorderdetails

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.shoppy.shop.R
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.components.BackButton
import com.shoppy.shop.components.PillButton
import com.shoppy.shop.components.ProgressBox
import com.shoppy.shop.components.RatingStars
import com.shoppy.shop.components.RatingSubmissionForm
import com.shoppy.shop.models.MRating
import com.shoppy.shop.navigation.BottomNavScreens
import com.shoppy.shop.ui.theme.roboto
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyOrderDetailsScreen(navController: NavController,
                         status:String,
                         product_title: String,
                         product_url: String,
                         product_price: Int,
                         quantity: Int,
                         payment_method: String,
                         order_id: String,
                         order_date: String,
                         product_id: String,
                         viewModel: MyOrderDetailsViewModel = hiltViewModel()) {

    val name = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }
    val address = remember { mutableStateOf("") }
    
    // Track if the user can rate this product
    val canUserRate = remember { mutableStateOf(false) }
    val isOrderDelivered = status == "Delivered"
    
    // Store the user's rating for this product if they've already rated it
    val userRating = remember { mutableStateOf<MRating?>(null) }
    
    // Check if user can rate this product and get their existing rating if any
    LaunchedEffect(Unit) {
        if (isOrderDelivered) {
            // Check if user can rate
            viewModel.checkIfUserCanRateProduct(product_id) { canRate ->
                canUserRate.value = canRate
            }
            
            // Get user's existing rating for this product if they've already rated
            viewModel.getUserRatingForProduct(product_id) { rating ->
                userRating.value = rating
            }
        }
    }

    viewModel.getAddressNamePhone(
        name = { name.value = it },
        phone = { phone.value = it },
        address = { address.value = it })

    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BackButton(
                navController = navController,
                spacing = 60.dp,
                topBarTitle = "Order Details"
            )
        }, modifier = Modifier
            .fillMaxSize(),
        backgroundColor = ShopKartUtils.offWhite
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            // Alert Dialog to cancel order
            if (openDialog.value) {
                AlertDialog(
                    onDismissRequest = { openDialog.value = false },
                    title = { Text(text = "Cancel Order?", style = TextStyle(fontWeight = FontWeight.Bold)) },
                    text = { Text(text = "Are you sure you want to cancel this order?", style = TextStyle(fontWeight = FontWeight.Normal)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.cancelOrder(product_title)
                                openDialog.value = false
                                Toast.makeText(context, "Order Cancelled", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red, contentColor = Color.White)
                        ) {
                            Text(text = "Yes", style = TextStyle(fontWeight = FontWeight.Bold))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { openDialog.value = false },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray, contentColor = Color.Black)
                        ) {
                            Text(text = "No", style = TextStyle(fontWeight = FontWeight.Bold))
                        }
                    }
                )
            }

            //Product Image Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(start = 20.dp, end = 20.dp),
                shape = RoundedCornerShape(12.dp)
            ) {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {

                    AsyncImage(
                        model = product_url,
                        contentDescription = product_title,
                        modifier = Modifier
                            .size(150.dp)
                            .padding(top = 8.dp),
                        contentScale = ContentScale.Fit,
                        placeholder = painterResource(id = R.drawable.placeholder)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
//                        .height(60.dp)
                            .padding(top = 8.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = status,
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = roboto
                            )
                        )

                        //Changing logo as per delivery status
                        val logo = when (status) {
                            "On The Way" -> R.drawable.on_the_way
                            "Delivered" -> R.drawable.delivered
                            "Cancelled" -> R.drawable.cancel
                            else -> R.drawable.ordered
                        }

                        val tint =  if (status == "Cancelled") Color.Red else if (status == "Delivered") Color(0xFFCDDC39) else if (status == "On The Way") ShopKartUtils.blue else Color.Black

                        Icon(
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .size(25.dp),
                            painter = painterResource(id = logo),
                            contentDescription = "Delivery Status",
                            tint = tint
                        )
                    }
                    Text(
                        text = product_title,
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = roboto
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {

                        Text(
                            text = "Qty: $quantity",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = roboto
                            )
                        )

                        Text(
                            text = "₫${ DecimalFormat("#,###").format(product_price.toDouble())}",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = roboto
                            )
                        )
                    }
                }
            }

            //If order status is ordered then show cancel button
            AnimatedVisibility(visible = status == "Ordered") {
                PillButton(
                    title = "Cancel Order",
                    color = Color.Red.toArgb(),
                    onClick = { openDialog.value = true }
                )
            }

            //Order Address Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp),
                shape = RoundedCornerShape(12.dp)
            ) {

                Column(
                    modifier = Modifier
                        .padding(15.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.Start
                ) {

                    Text(
                        text = "Order Details",
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = roboto
                        ),
                        color = Color.Black.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Payment Method: $payment_method",
                        modifier = Modifier.padding(bottom = 10.dp),
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = roboto
                        )
                    )
                    Text(
                        text = "Order Date: $order_date",
                        modifier = Modifier.padding(bottom = 10.dp),
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = roboto
                        )
                    )
                    Text(
                        text = "Order ID: $order_id",
                        modifier = Modifier.padding(bottom = 10.dp),
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = roboto
                        )
                    )
                }

            }

            //Shipping Address Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp),
                shape = RoundedCornerShape(12.dp)
            ) {

                Column(
                    modifier = Modifier
                        .padding(15.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.Start
                ) {

                    Text(
                        text = "Shipping Address",
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = roboto
                        ),
                        color = Color.Black.copy(alpha = 0.5f)
                    )

                    Text(
                        text = "Name: ${name.value}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = roboto
                        )
                    )

                    Text(
                        text = "Address: ${address.value}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = roboto
                        )
                    )

                    Text(
                        text = "Phone no: ${phone.value}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = roboto
                        )
                    )
                }

            }

            //Price Details Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp),
                shape = RoundedCornerShape(12.dp)
            ) {

                Column(
                    modifier = Modifier
                        .padding(15.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.Start
                ) {

                    Text(
                        text = "Price Details",
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = roboto
                        ),
                        color = Color.Black.copy(alpha = 0.5f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Price",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = roboto
                            )
                        )

                        Text(
                            text = "₫${ DecimalFormat("#,###").format((product_price - 280).toDouble())}",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = roboto
                            )
                        )

                    }

                    Divider(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Total Payment",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = roboto
                            )
                        )

                        Text(
                            text = "₫${ DecimalFormat("#,###").format(product_price.toDouble())}",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = roboto
                            )
                        )

                    }
                }
            }
            
            // Rating section - only show for delivered orders
            if (isOrderDelivered) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Show rating form if user can rate
                if (canUserRate.value) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Rate This Product",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = roboto
                                ),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            RatingSubmissionForm(
                                productId = product_id,
                                onRatingSubmitted = {
                                    Toast.makeText(context, "Thank you for your rating!", Toast.LENGTH_SHORT).show()
                                    canUserRate.value = false
                                    
                                    // Get the newly submitted rating
                                    viewModel.getUserRatingForProduct(product_id) { rating ->
                                        userRating.value = rating
                                    }
                                }
                            )
                        }
                    }
                } 
                // Show existing rating if user has already rated
                else if (userRating.value != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Your Rating",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = roboto
                                ),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                userRating.value?.let { rating ->
                                    RatingStars(
                                        rating = rating.rating_value?.toFloat() ?: 0f,
                                        starSize = 24,
                                        showRatingText = true
                                    )
                                }
                            }
                            
                            userRating.value?.comment?.let { comment ->
                                if (comment.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.LightGray.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = comment,
                                            style = TextStyle(
                                                fontSize = 14.sp,
                                                fontFamily = roboto
                                            ),
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }
                            
                            // Show when the rating was submitted
                            userRating.value?.timestamp?.let { timestamp ->
                                val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                val formattedDate = dateFormatter.format(Date(timestamp))
                                
                                Text(
                                    text = "Submitted on $formattedDate",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        fontFamily = roboto
                                    ),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Add some space at the bottom
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}