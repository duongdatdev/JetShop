package com.shoppy.shop.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shoppy.shop.components.AIMessageScreen
import com.shoppy.shop.screens.ForgotPasswordScreen
import com.shoppy.shop.screens.NotificationScreen
import com.shoppy.shop.screens.SplashScreen
import com.shoppy.shop.screens.login.LoginScreen2
import com.shoppy.shop.screens.mainscreenholder.MainScreenHolder
import com.shoppy.shop.screens.register.RegisterScreen

@Composable
fun ShopKartNavigation(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = NavScreens.SplashScreen.name){
        composable(NavScreens.SplashScreen.name) {
            SplashScreen(navController = navController)
        }

        composable(NavScreens.LoginScreen.name){
            LoginScreen2(navController = navController)
        }

        composable(NavScreens.RegisterScreen.name){
            RegisterScreen(navController = navController)
        }

        composable(NavScreens.MainScreenHolder.name){
            MainScreenHolder(navController = navController)
        }

        composable(NavScreens.ForgotPasswordScreen.name) {
            ForgotPasswordScreen(navHostController = navController)
        }
        
        composable(NavScreens.NotificationScreen.name) {
            NotificationScreen(navController = navController)
        }
        
        composable(NavScreens.AIMessageScreen.name) {
            AIMessageScreen()
        }
    }
}