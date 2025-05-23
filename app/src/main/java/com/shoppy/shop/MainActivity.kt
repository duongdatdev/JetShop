package com.shoppy.shop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.shoppy.shop.navigation.ShopKartNavigation
import com.shoppy.shop.ui.theme.ShopKartTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShopKartTheme {
                ShopKartApp()
            }
        }
    }
}

@Composable
fun ShopKartApp(){
    ShopKartNavigation()
}