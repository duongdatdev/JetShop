package com.shoppy.shop.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.shoppy.shop.navigation.BottomNavScreens.AddProductSliderEmpl
import com.shoppy.shop.screens.AboutScreen
import com.shoppy.shop.screens.NotificationScreen
import com.shoppy.shop.screens.admin.AddEmployee
import com.shoppy.shop.screens.admin.AddProductSliderAdmin
import com.shoppy.shop.screens.admin.AddRemoveBrandAdmin
import com.shoppy.shop.screens.admin.AdminScreen
import com.shoppy.shop.screens.admin.AdminScreenViewModel
import com.shoppy.shop.screens.admin.EmployeeAttendance
import com.shoppy.shop.screens.admin.orderstatus.DeliveredItems
import com.shoppy.shop.screens.admin.orderstatus.OnTheWayItems
import com.shoppy.shop.screens.admin.orderstatus.OrderedItems
import com.shoppy.shop.screens.cart.CartScreen
import com.shoppy.shop.screens.cart.CartScreenViewModel
import com.shoppy.shop.screens.checkout.OrderConfirmationScreen
import com.shoppy.shop.screens.checkout.OrderSuccessScreen
import com.shoppy.shop.screens.checkout.address.AddressScreen
import com.shoppy.shop.screens.checkout.address.EditAddressScreen
import com.shoppy.shop.screens.checkout.ordersummary.OrderSummaryScreen
import com.shoppy.shop.screens.checkout.payment.PaymentScreen
import com.shoppy.shop.screens.details.DetailsScreen
import com.shoppy.shop.screens.details.DetailsScreenViewModel
import com.shoppy.shop.screens.employee.AddRemoveBrandEmpl
import com.shoppy.shop.screens.employee.EmployeeScreen
import com.shoppy.shop.screens.employee.orderstatus.DeliveredItemsEmp
import com.shoppy.shop.screens.employee.orderstatus.OnTheWayItemsEmp
import com.shoppy.shop.screens.employee.orderstatus.OrderedItemsEmp
import com.shoppy.shop.screens.home.HomeScreen
import com.shoppy.shop.screens.home.HomeViewModel
import com.shoppy.shop.screens.myorderdetails.MyOrderDetailsScreen
import com.shoppy.shop.screens.myprofile.MyProfileScreen
import com.shoppy.shop.screens.orders.OrdersScreen
import com.shoppy.shop.screens.profile.ProfileScreen
import com.shoppy.shop.screens.search.SearchScreen
import com.shoppy.shop.screens.admin.ManageCategoriesScreen
import com.shoppy.shop.screens.admin.rolemanagement.RoleManagementScreen
import com.shoppy.shop.screens.employee.AddProductSliderEmpl
import com.shoppy.shop.screens.employee.ShopSettings
import com.shoppy.shop.screens.shop.ShopScreen


//BottomNavScreens.Home.route
@Composable
fun BottomNavigation(
    navController: NavHostController,
    email: String,
    signOut: () -> Unit,
) {
    NavHost(navController = navController, startDestination = BottomNavScreens.Home.route) {
        composable(BottomNavScreens.Home.route) {
            val viewModel = hiltViewModel<HomeViewModel>()
            val viewModelDetails = hiltViewModel<DetailsScreenViewModel>()

            val context = LocalContext.current
            val haptic = LocalHapticFeedback.current

            HomeScreen(navController = navController, viewModel) { product ->
                //Uploading Item to Firebase Cart
                viewModelDetails.uploadCartToFirebase(
                    url = product.product_url,
                    title = product.product_title,
                    description = product.product_description,
                    price = product.product_price,
                    stock = product.stock,
                    category = product.category,
                    productId = product.product_id
                )

                //Haptic Feedback
                haptic.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                Toast.makeText(context, "Item added to cart", Toast.LENGTH_SHORT).show()
            }
        }

        composable(BottomNavScreens.Orders.route) {
            OrdersScreen(navController = navController)
        }

        composable(BottomNavScreens.Cart.route) {
            val viewModel = hiltViewModel<CartScreenViewModel>()
            CartScreen(navController = navController, viewModel)
        }

        composable(BottomNavScreens.Profile.route) {
            ProfileScreen(
                navController = navController,
                email = email,
            ) {
                signOut()
            }
        }

        composable(BottomNavScreens.MyProfile.route) {
            MyProfileScreen(navController = navController)
        }

        composable(BottomNavScreens.About.route) {
            AboutScreen(navController = navController)
        }

        val detailsScreen = BottomNavScreens.Details.route
        composable("$detailsScreen/{imageUrl}/{productTitle}/{productDescription}/{productPrice}/{stock}/{category}/{productId}",
            arguments = listOf(
                navArgument("imageUrl") {
                    type = NavType.StringType
                },
                navArgument("productTitle") {
                    type = NavType.StringType
                },
                navArgument("productDescription") {
                    type = NavType.StringType
                },

                navArgument("productPrice") {
                    type = NavType.IntType
                },

                navArgument("stock") {
                    type = NavType.IntType
                },

                navArgument("category") {
                    type = NavType.StringType
                },

                navArgument("productId") {
                    type = NavType.StringType
                }
            )) { backstack ->
            val imageUrl = backstack.arguments?.getString("imageUrl")
            val productTitle = backstack.arguments?.getString("productTitle")
            val productDescription = backstack.arguments?.getString("productDescription")
            val productPrice = backstack.arguments?.getInt("productPrice")
            val stock = backstack.arguments?.getInt("stock")
            val category = backstack.arguments?.getString("category")
            val productId = backstack.arguments?.getString("productId")
            DetailsScreen(
                navController = navController,
                imageUrl = imageUrl.toString(),
                productTitle = productTitle.toString(),
                productDescription = productDescription.toString(),
                productPrice = productPrice!!,
                stock = stock!!,
                category = category!!,
                productId = productId!!,
            )
        }

        composable(
            route = BottomNavScreens.MyOrderDetails.route + "/{status}/{product_title}/{product_url}/{product_price}/{quantity}/{payment_method}/{order_id}/{order_date}/{product_id}",
            arguments = listOf(

                navArgument("status") {
                    type = NavType.StringType
                },

                navArgument("product_title") {
                    type = NavType.StringType
                },

                navArgument("product_url") {
                    type = NavType.StringType
                },

                navArgument("product_price") {
                    type = NavType.IntType
                },

                navArgument("quantity") {
                    type = NavType.IntType
                },

                navArgument("payment_method") {
                    type = NavType.StringType
                },

                navArgument("order_id") {
                    type = NavType.StringType
                },

                navArgument("order_date") {
                    type = NavType.StringType
                },
                
                navArgument("product_id") {
                    type = NavType.StringType
                },
            )
        ) { bacStack ->
            val status = bacStack.arguments?.getString("status")
            val productTitle = bacStack.arguments?.getString("product_title")
            val productUrl = bacStack.arguments?.getString("product_url")
            val productPrice = bacStack.arguments?.getInt("product_price")
            val quantity = bacStack.arguments?.getInt("quantity")
            val paymentMethod = bacStack.arguments?.getString("payment_method")
            val orderId = bacStack.arguments?.getString("order_id")
            val orderDate = bacStack.arguments?.getString("order_date")
            val productId = bacStack.arguments?.getString("product_id")
            MyOrderDetailsScreen(
                navController = navController,
                status = status!!,
                product_title = productTitle!!,
                product_url = productUrl!!,
                product_price = productPrice!!,
                quantity = quantity!!,
                payment_method = paymentMethod!!,
                order_id = orderId!!,
                order_date = orderDate!!,
                product_id = productId!!
            )
        }

        composable(
            route = "${BottomNavScreens.AddressScreen.route}?buyNowId={buyNowId}",
            arguments = listOf(
                navArgument("buyNowId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val buyNowId = backStackEntry.arguments?.getString("buyNowId")
            AddressScreen(navController = navController, buyNowId = buyNowId)
        }

        composable(BottomNavScreens.EditAddressScreen.route) {
            EditAddressScreen(navController = navController)
        }

        composable(
            route = "${BottomNavScreens.OrderSummaryScreen.route}?buyNowId={buyNowId}",
            arguments = listOf(
                navArgument("buyNowId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val buyNowId = backStackEntry.arguments?.getString("buyNowId")
            OrderSummaryScreen(navController = navController, buyNowId = buyNowId)
        }

        val paymentScreen = BottomNavScreens.PaymentScreen.route
        composable(
            route = "$paymentScreen/{totalAmount}?buyNowId={buyNowId}", 
            arguments = listOf(
                navArgument("totalAmount") {
                    type = NavType.IntType
                },
                navArgument("buyNowId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStack ->
            val totalAmount = backStack.arguments?.getInt("totalAmount") ?: 0
            val buyNowId = backStack.arguments?.getString("buyNowId")
            PaymentScreen(navController = navController, totalAmount = totalAmount, buyNowId = buyNowId)
        }

        val confirmationScreen = BottomNavScreens.OrderConfirmationScreen.route
        composable(
            route = "$confirmationScreen/{totalAmount}/{paymentMethod}?buyNowId={buyNowId}",
            arguments = listOf(
                navArgument("totalAmount") {
                    type = NavType.IntType
                },
                navArgument("paymentMethod") {
                    type = NavType.StringType
                },
                navArgument("buyNowId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStack ->
            val totalAmount = backStack.arguments?.getInt("totalAmount") ?: 0
            val paymentMethod = backStack.arguments?.getString("paymentMethod") ?: ""
            val buyNowId = backStack.arguments?.getString("buyNowId")
            OrderConfirmationScreen(
                navController = navController, 
                totalAmount = totalAmount, 
                paymentMethod = paymentMethod,
                buyNowId = buyNowId
            )
        }

        composable(BottomNavScreens.OrderSuccessScreen.route) {
            OrderSuccessScreen(navController = navController)
        }

        composable(BottomNavScreens.SearchScreen.route) {
            SearchScreen(navController = navController)
        }

        composable(BottomNavScreens.AdminScreen.route) {
            AdminScreen(navController = navController)
        }

        composable(BottomNavScreens.EmployeeScreen.route) {
            EmployeeScreen(navController = navController)
        }

        composable(BottomNavScreens.AddRemoveBrandAdmin.route) {
            AddRemoveBrandAdmin(navHostController = navController)
        }

        composable(BottomNavScreens.AddProductSliderAdmin.route) {
            AddProductSliderAdmin(navHostController = navController)
        }

        composable(BottomNavScreens.EmployeeAttendance.route) {
            val viewModel = hiltViewModel<AdminScreenViewModel>()
            EmployeeAttendance(navController = navController, viewModel)
        }

        composable(BottomNavScreens.AddRemoveBrandEmpl.route) {
            AddRemoveBrandEmpl(navHostController = navController)
        }

        composable(BottomNavScreens.AddProductSliderEmpl.route) {
            AddProductSliderEmpl(navHostController = navController)
        }

        composable(BottomNavScreens.AddEmployee.route) {
            AddEmployee(navHostController = navController)
        }

        composable(BottomNavScreens.OrderedItems.route) {
            OrderedItems(navHostController = navController)
        }

        composable(BottomNavScreens.OnTheWayItems.route) {
            OnTheWayItems(navHostController = navController)
        }

        composable(BottomNavScreens.DeliveredItems.route) {
            DeliveredItems(navHostController = navController)
        }

        composable(BottomNavScreens.OrderedItemsEmp.route) {
            OrderedItemsEmp(navHostController = navController)
        }

        composable(BottomNavScreens.OnTheWayItemsEmp.route) {
            OnTheWayItemsEmp(navHostController = navController)
        }

        composable(BottomNavScreens.DeliveredItemsEmp.route) {
            DeliveredItemsEmp(navHostController = navController)
        }

        composable(BottomNavScreens.ManageCategories.route) {
            ManageCategoriesScreen(navController = navController)
        }

        composable(BottomNavScreens.RoleManagement.route) {
            RoleManagementScreen(navController = navController)
        }
        composable(BottomNavScreens.ShopSettings.route) {
            ShopSettings(navController = navController)
        }

        val shopRoute = BottomNavScreens.Shop.route
        composable("$shopRoute/{shopId}/{shopName}",
            arguments = listOf(
                navArgument("shopId") { type = NavType.StringType },
                navArgument("shopName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getString("shopId") ?: ""
            val shopName = backStackEntry.arguments?.getString("shopName") ?: ""
            ShopScreen(navController = navController, shopId = shopId, shopName = shopName)
        }

        // Add notification screen route
        composable(BottomNavScreens.Notifications.route) {
            NotificationScreen(navController = navController)
        }

    }
}