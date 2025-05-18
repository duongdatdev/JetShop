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
    onItemSelected: (BottomNavScreens) -> Unit,
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val isAdmin = remember { mutableStateOf(false) }
    val isStaff = remember { mutableStateOf(false) }

    val unreadNotificationsCount by notificationViewModel.unreadNotificationsCount.collectAsState()

    LaunchedEffect(Unit) {
        isAdmin.value = UserRoleManager.isAdmin()
        isStaff.value = UserRoleManager.isStaff()
    }

    val padding = if (isAdmin.value || isStaff.value) 80.dp else 40.dp
    val items = if (isAdmin.value || isStaff.value) BottomNavScreens.ItemsAdmin.list else BottomNavScreens.Items.list
    val navBackStackEntry by navHostController.currentBackStackEntryAsState()
    val currentScreen = navBackStackEntry?.destination

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(start = padding, end = padding, top = 10.dp, bottom = 10.dp),
        shape = RoundedCornerShape(30.dp),
        color = ShopKartUtils.darkBlue,
        elevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentScreen?.hierarchy?.any { it.route == item.route } == true
                val showBadge = item.route == BottomNavScreens.Home.route && unreadNotificationsCount > 0

                BottomNavBarItem(
                    item = item,
                    isSelected = isSelected,
                    showBadge = showBadge,
                    badgeCount = unreadNotificationsCount
                ) {
                    onItemSelected(item)
                    navHostController.navigate(item.route) {
                        popUpTo(navHostController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBarItem(
    item: BottomNavScreens,
    isSelected: Boolean,
    showBadge: Boolean = false,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    val iconColor = if (isSelected) Color.White else Color.Gray
    val textColor = if (isSelected) Color.White else Color.Transparent

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = item.icon!!),
                contentDescription = item.title,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )

            if (showBadge && badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .offset(x = 10.dp, y = (-8).dp)
                        .clip(CircleShape)
                        .background(Color.Red),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(
            text = item.title,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
