package com.shoppy.shop.screens.employee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.components.BackButton
import com.shoppy.shop.components.ProfileRowComp
import com.shoppy.shop.navigation.BottomNavScreens
import com.shoppy.shop.screens.employee.EmployeeScreenViewModel

@Composable
fun EmployeeScreen(navController: NavController,
                viewModel: EmployeeScreenViewModel = hiltViewModel()) {
    
    Scaffold(topBar = { BackButton(navController = navController, topBarTitle = "Employee") }, backgroundColor = ShopKartUtils.offWhite) { innerPadding ->
        
        Column(modifier = Modifier
            .padding(innerPadding)
            .padding(20.dp)
            .fillMaxSize(), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
            
            Surface(modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
                shape = RoundedCornerShape(14.dp)) {

                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    ProfileRowComp(
                        title = "Add/Remove Brand"
                    ) { navController.navigate(BottomNavScreens.AddRemoveBrandEmpl.route) }

                    Divider()

                    ProfileRowComp(
                        title = "Add Product/Slider",
                    ) { navController.navigate(BottomNavScreens.AddProductSliderEmpl.route) }
                }
            }

            Surface(modifier = Modifier
                        .fillMaxWidth()
                        .height(192.dp)
                        .padding(top = 10.dp),
                        shape = RoundedCornerShape(14.dp)) {

                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    ProfileRowComp(
                        title = "Ordered Items",
                    ) { navController.navigate(BottomNavScreens.OrderedItemsEmp.route) }

                    Divider()

                    ProfileRowComp(
                        title = "On The Way Items",
                    ) { navController.navigate(BottomNavScreens.OnTheWayItemsEmp.route) }

                    Divider()

                    ProfileRowComp(
                        title = "Delivered Items",
                    ) { navController.navigate(BottomNavScreens.DeliveredItemsEmp.route) }
                }
            }
        }
    }
}



//@Preview
//@Composable
//fun Prev(){
//    EmployeeScreen(navController = rememberNavController())
//}