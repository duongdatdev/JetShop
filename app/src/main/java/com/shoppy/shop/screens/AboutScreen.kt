package com.shoppy.shop.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.shoppy.shop.R
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.components.BackButton
import com.shoppy.shop.ui.theme.roboto

@Composable
fun AboutScreen(navController: NavController){

    val uriHandler = LocalUriHandler.current
    Scaffold(topBar = { BackButton(navController = navController, topBarTitle = "About")}, backgroundColor = ShopKartUtils.offWhite, modifier = Modifier
        .fillMaxSize()) { innerPadding ->

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
            
            Box(modifier = Modifier
                .height(200.dp)
                .width(150.dp)
                .fillMaxSize()){
                Image(painter = painterResource(id = R.drawable.rengoku), contentDescription = "About Image")
            }
        }
    }
}

@Preview
@Composable
fun Prev(){
    AboutScreen(navController = rememberNavController())
}