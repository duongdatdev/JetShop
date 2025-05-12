package com.shoppy.shop.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.shoppy.shop.R
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.components.BackButton
import com.shoppy.shop.navigation.BottomNavScreens
import com.shoppy.shop.ui.theme.roboto

@Composable
fun AdminScreen(
    navController: NavHostController,
    viewModel: AdminScreenViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = { BackButton(navController = navController, topBarTitle = "Quản trị") },
        backgroundColor = ShopKartUtils.offWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- PHẦN QUẢN LÝ ---
            SectionCard(title = "Quản lý") {
                ProfileRowComp("Quản lý danh mục") {
                    navController.navigate(BottomNavScreens.ManageCategories.route)
                }
                ProfileRowComp("Thêm/Xoá thương hiệu") {
                    navController.navigate(BottomNavScreens.AddRemoveBrandAdmin.route)
                }
                ProfileRowComp("Thêm nhân viên") {
                    navController.navigate(BottomNavScreens.AddEmployee.route)
                }
                ProfileRowComp("Thêm sản phẩm / slider") {
                    navController.navigate(BottomNavScreens.AddProductSliderAdmin.route)
                }
                ProfileRowComp("Chấm công nhân viên") {
                    navController.navigate(BottomNavScreens.EmployeeAttendance.route)
                }
                ProfileRowComp("Phân quyền người dùng") {
                    navController.navigate(BottomNavScreens.RoleManagement.route)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- PHẦN ĐƠN HÀNG ---
            SectionCard(title = "Đơn hàng") {
                ProfileRowComp("Đơn đã đặt") {
                    navController.navigate(BottomNavScreens.OrderedItems.route)
                }
                ProfileRowComp("Đang giao") {
                    navController.navigate(BottomNavScreens.OnTheWayItems.route)
                }
                ProfileRowComp("Đã giao") {
                    navController.navigate(BottomNavScreens.DeliveredItems.route)
                }
            }
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(start = 20.dp, bottom = 8.dp),
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
            )
            content()
        }
    }
}

@Composable
fun ProfileRowComp(title: String, onClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, fontFamily = roboto)
            )
            Icon(
                painter = painterResource(id = R.drawable.arrow_forward),
                contentDescription = "Chuyển tiếp",
                tint = Color.Gray
            )
        }
        Divider()
    }
}
