package com.shoppy.shop.screens.checkout.ordersummary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.components.BackButton
import com.shoppy.shop.components.OrderSummaryCard
import com.shoppy.shop.components.PillButton
import com.shoppy.shop.components.ProgressBox
import com.shoppy.shop.models.MCart
import com.shoppy.shop.navigation.BottomNavScreens
import com.shoppy.shop.ui.theme.roboto
import java.text.DecimalFormat

@Composable
fun OrderSummaryScreen(navController: NavHostController, viewModel: OrderSummaryScreenViewModel = hiltViewModel(), buyNowId: String? = null) {

    var cartList = emptyList<MCart>()
    val isBuyNow = buyNowId != null
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val totalAmount = remember { mutableStateOf(0) }
    
    // Handle Buy Now mode by fetching the specific item
    if (isBuyNow && buyNowId != null) {
        viewModel.getBuyNowItem(buyNowId)
        
        // If we have a Buy Now item, convert it to a list with one item
        val buyNowItemState = viewModel.buyNowItem.value
        if (!buyNowItemState.loading!! && buyNowItemState.data != null) {
            cartList = listOf(buyNowItemState.data!!)
            totalAmount.value = buyNowItemState.data!!.product_price!! * (buyNowItemState.data!!.item_count ?: 1)
        }
    } else {
        // Regular cart flow
        if (!viewModel.fireSummary.value.data.isNullOrEmpty()){
            cartList = viewModel.fireSummary.value.data!!.toList().filter { mCart ->
                mCart.user_id == userId
            }
        }
    }
    
    val gstPrice = totalAmount.value + (100 * cartList.size) + 180 // Rs180 is GST price i.e 18%

    val constraints = ConstraintSet {
        val progressCard = createRefFor(id = "ProgressCard")
        val itemDetailsCard = createRefFor(id = "ItemsDetailsCard")
        val itemsLazyList = createRefFor(id = "ItemsLazyList")

        constrain(progressCard){
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(itemDetailsCard.top)
            width = Dimension.wrapContent
            height = Dimension.wrapContent
        }

        constrain(itemDetailsCard){
            top.linkTo(progressCard.bottom, margin = 20.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(itemsLazyList.top)
            width = Dimension.wrapContent
            height = Dimension.wrapContent
        }

        constrain(itemsLazyList){
            top.linkTo(itemDetailsCard.bottom, margin = 20.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
            width = Dimension.wrapContent
            height = Dimension.fillToConstraints
        }
    }

    Scaffold(topBar = { BackButton(navController = navController, topBarTitle = "Order Summary", spacing = 60.dp) },
        backgroundColor = ShopKartUtils.offWhite, bottomBar = { SummaryBottomBar(totalAmount = totalAmount.value, navController = navController, buyNowId = buyNowId) }) { innerPadding ->

        ConstraintLayout(constraintSet = constraints, modifier = Modifier.padding(innerPadding).fillMaxSize(), animateChanges = true) {

            //Progress Indicator 1-2-3
            Surface(modifier = Modifier
                .layoutId("ProgressCard")
                .fillMaxWidth()
                .height(80.dp),
                elevation = 2.dp,
                color = Color.White) {

                Row(modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {

                    ProgressBox(number = "1", title = "Address", color = ShopKartUtils.blue)
                    Divider(modifier = Modifier
                        .height(2.dp)
                        .width(40.dp))
                    ProgressBox(number = "2", title = "Order Summary",color = ShopKartUtils.blue)
                    Divider(modifier = Modifier
                        .height(2.dp)
                        .width(40.dp))
                    ProgressBox(number = "3", title = "Payment",color = Color.Gray)
                }
            }


            //Items Count,Items Price... Card
            Card(modifier = Modifier
                .layoutId("ItemsDetailsCard")
                .padding(start = 20.dp, end = 20.dp)
                .fillMaxWidth(),
                elevation = 0.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.Start, modifier = Modifier
                    .background(Color.White)
                    .padding(start = 35.dp, end = 15.dp)) {

                    RowComp(title = "Items Count:", price = "${cartList.size}", space = 100.dp)
                    RowComp(title = "Items Price:", price = "₫${ DecimalFormat("#,###").format(totalAmount.value.toDouble()) }", space = 100.dp)
                    RowComp(title = "Delivery Fee:", price = "₫${ DecimalFormat("#,###").format((100 * cartList.size).toDouble()) }", space = 90.dp)
                    RowComp(title = "GST:", price = "18%", space = 160.dp)

                    //(totalAmount.value + 100 * cartList.size) Adding 100rs for each item in the list
                    RowComp(title = "Total Price:", price = "₫${ DecimalFormat("#,###").format((gstPrice).toDouble()) }", space = 110.dp)
                }
            }

            OrderSummaryCard(cardList = cartList, viewModel = viewModel, modifier = Modifier.layoutId("ItemsLazyList")){ price ->
                if (!isBuyNow) {
                    totalAmount.value = price
                }
            }
        }
    }
}

@Composable
fun SummaryBottomBar(totalAmount: Int, navController: NavController, buyNowId: String? = null){
    val finalPrice = totalAmount + 280 // 100 delivery + 180 GST
    
    Surface(modifier = Modifier
        .fillMaxWidth()
        .height(120.dp)) {

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {

                Icon(imageVector = Icons.Rounded.Info, contentDescription = "Note")
                Text(text = "Note: 100đ fee is applied for all the items in the cart", modifier = Modifier.padding(start = 5.dp) ,style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, fontFamily = roboto), color = Color.Black.copy(alpha = 0.5f))
            }

            PillButton(title = "Continue", color = ShopKartUtils.black.toInt()){ 
                // If buy now, pass the ID to the payment screen
                if (buyNowId != null) {
                    navController.navigate("${BottomNavScreens.PaymentScreen.route}/${finalPrice}?buyNowId=$buyNowId") 
                } else {
                    navController.navigate("${BottomNavScreens.PaymentScreen.route}/${finalPrice}")
                }
            }
        }
    }
}

@Composable
fun RowComp(title: String,price:String,space: Dp,modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {

        Text(text = title, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = roboto))

        Spacer(modifier = Modifier.width(space))

        Text(text = price, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold), fontFamily = roboto)
    }
}