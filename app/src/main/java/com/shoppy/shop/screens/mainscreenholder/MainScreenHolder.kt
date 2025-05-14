package com.shoppy.shop.screens.mainscreenholder

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.identity.Identity
import com.shoppy.shop.components.ChatDialog
import com.shoppy.shop.navigation.BottomNavBar
import com.shoppy.shop.navigation.BottomNavScreens
import com.shoppy.shop.navigation.BottomNavigation
import com.shoppy.shop.navigation.NavScreens
import com.shoppy.shop.viewmodels.ChatViewModel

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainScreenHolder(
    navController: NavController,
    viewModel: MainScreenViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    chatViewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val navHostController = rememberNavController()
    val navBackStackEntry by navHostController.currentBackStackEntryAsState()
    val currentScreen = remember { mutableStateOf<BottomNavScreens>(BottomNavScreens.Home) }
    val emailState = remember { mutableStateOf("") }
    val context = LocalContext.current

    // Chat dialog state
    var showChatDialog by remember { mutableStateOf(false) }
    val messages by chatViewModel.messages.collectAsState()
    val isLoading by chatViewModel.isLoading.collectAsState()

//    viewModel.checkAdminAndEmployee { email ->
//        if (email != null) emailState.value = email
//    }

    val showBottomBar = when (navBackStackEntry?.destination?.route) {
        BottomNavScreens.Home.route,
        BottomNavScreens.Orders.route,
        BottomNavScreens.Cart.route,
        BottomNavScreens.Profile.route -> true
        else -> false
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                BottomNavBar(
                    navHostController = navHostController,
                    onItemSelected = {
                        currentScreen.value = it
                    }
                )
            }
        },
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = { navController.navigate(NavScreens.AIMessageScreen.name) },
//                backgroundColor = MaterialTheme.colors.primary
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Send,
//                    contentDescription = "AI Assistant",
//                    tint = MaterialTheme.colors.onPrimary
//                )
//            }
//        }
    ) {
        BottomNavigation(
            navController = navHostController,
            email = emailState.value
        ) {
            viewModel.signOut(
                navController = navController,
                oneTapClient = Identity.getSignInClient(context)
            )
        }

//        if (showChatDialog) {
//            ChatDialog(
//                onDismiss = { showChatDialog = false },
//                onSendMessage = { message ->
//                    chatViewModel.sendMessage(message)
//                },
//                messages = messages
//            )
//        }
    }
}
