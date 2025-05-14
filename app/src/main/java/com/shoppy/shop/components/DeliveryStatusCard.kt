package com.shoppy.shop.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.models.MOrder
import com.shoppy.shop.ui.theme.roboto
import java.text.DecimalFormat

//Used in Admin and Employee Screen
@Composable
fun DeliveryStatusCard(
    ordered: MOrder,
    buttonTitle: String, 
    navHostController: NavHostController, 
    buttonClick:() -> Unit = { },
    onCancelClick:() -> Unit = { }
) {

    //If button text is "Item Delivered" button is disabled else enabled
    val isEnabled = when(buttonTitle){
        "Item Delivered" -> false
        else -> true
    }

    Surface(modifier = Modifier
        .fillMaxWidth()
        .height(if (ordered.delivery_status == "Ordered") 150.dp else 100.dp)
        .padding(10.dp),
        shape = RoundedCornerShape(12.dp)
    ) {

        Column(modifier = Modifier.fillMaxSize().padding(5.dp)) {
            Row(modifier = Modifier.fillMaxWidth().weight(1f), 
                verticalAlignment = Alignment.CenterVertically, 
                horizontalArrangement = Arrangement.SpaceBetween) {

                AsyncImage(model = ordered.product_url, contentDescription = ordered.product_title)

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(100.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {

                    Text(
                        text = ordered.product_title!!,
                        style = TextStyle(fontWeight = FontWeight.Bold, fontFamily = roboto)
                    )
                    Text(
                        text = "₫${ DecimalFormat("#,###,###").format(ordered.product_price?.toDouble())}",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontFamily = roboto)
                    )
                }

                PillButton(
                    title = buttonTitle,
                    color = ShopKartUtils.black.toInt(),
                    textSize = 12,
                    modifier = Modifier
                        .height(50.dp)
                        .width(160.dp),
                    enabled = isEnabled
                ) {
                    Log.d("DeliveryStatusCard", "Button clicked: $buttonTitle for product: ${ordered.product_title}, User ID: ${ordered.user_id}")
                    buttonClick.invoke()
                }
            }
            
            if (ordered.delivery_status == "Ordered") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PillButton(
                        title = "Cancel Order",
                        color = Color.Red.toArgb(),
                        textSize = 12,
                        modifier = Modifier
                            .height(40.dp)
                            .width(160.dp)
                    ) {
                        onCancelClick.invoke()
                    }
                }
            }
        }
    }
}