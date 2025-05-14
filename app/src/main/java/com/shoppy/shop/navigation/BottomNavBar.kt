package com.shoppy.shop.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.auth.FirebaseAuth
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.utils.UserRoleManager
import com.shoppy.shop.viewmodels.NotificationViewModel

@Composable
fun BottomNavBar(
    navHostController: NavHostController,
    onItemSelected:(BottomNavScreens) -> Unit,
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {

    val isAdmin = remember { mutableStateOf(false) }
    val isStaff = remember { mutableStateOf(false) }
    
    // Get unread notifications count
    val unreadNotificationsCount by notificationViewModel.unreadNotificationsCount.collectAsState()

    LaunchedEffect(Unit) {
        isAdmin.value = UserRoleManager.isAdmin()
        isStaff.value = UserRoleManager.isStaff()
    }

    //If Admin or Staff Account is logged in change BottomNav Bar padding to 80.dp else 40.dp
    val padding = if (isAdmin.value || isStaff.value) 80.dp else 40.dp

    val items = if (isAdmin.value || isStaff.value) BottomNavScreens.ItemsAdmin.list else BottomNavScreens.Items.list

    val navBackStackEntry by navHostController.currentBackStackEntryAsState()

    val currentScreen = navBackStackEntry?.destination

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .padding(start = padding, end = padding, top = 35.dp, bottom = 10.dp),
        shape = RoundedCornerShape(40.dp),
        color = ShopKartUtils.darkBlue,
    ) {

        Row(
            modifier = Modifier
                .background(Color.Transparent)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {

            items.forEach { item ->

                //Checking which screen is selected
                val isSelected = currentScreen?.hierarchy?.any { it.route == item.route } == true
                
                // Show notification badge only on the home tab
                val showBadge = item.route == BottomNavScreens.Home.route && unreadNotificationsCount > 0

                BottomNavBarItems(
                    item = item, 
                    isSelected = isSelected,
                    showBadge = showBadge,
                    badgeCount = unreadNotificationsCount
                ) {
                    onItemSelected(item)
                    navHostController.navigate(item.route) {
                        popUpTo(navHostController.graph.findStartDestination().id){saveState = true}

                        //Avoid multiple copies of same destination when selecting again
                        launchSingleTop = true

                        //Restore state when re selecting a previously selected item
//                        restoreState = true
                    }
                }

            }
        }
    }
}

@Composable
fun BottomNavBarItems(
    item: BottomNavScreens,
    isSelected: Boolean,
    showBadge: Boolean = false,
    badgeCount: Int = 0,
    onClick: () -> Unit = {}
) {
    val contentColor = if (isSelected) Color.White else Color.Gray

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onClick)
                .width(55.dp)
        ) {
            Column(
                modifier = Modifier.padding(top = 12.dp, bottom = 12.dp, start = 15.dp, end = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = item.icon!!), 
                    contentDescription = item.title,
                    tint = contentColor
                )
            }
            
            // Notification Badge
            if (showBadge && badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .offset(x = 20.dp, y = (-2).dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

//           AnimatedVisibility(visible = isSelected) {
//
//                    Text(text = item.title,
//                        color = contentColor,
//                    modifier = Modifier.padding(bottom = 10.dp),
//                    style = TextStyle(fontWeight = FontWeight.Bold)
//                    )
//            }
    }
}