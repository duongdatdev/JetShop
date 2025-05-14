package com.shoppy.shop.screens.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.shoppy.shop.R
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.components.ProfileCards
import com.shoppy.shop.navigation.BottomNavScreens
import com.shoppy.shop.navigation.NavScreens
import com.shoppy.shop.utils.NotificationUtils
import com.shoppy.shop.utils.UserRoleManager
import kotlinx.coroutines.launch

//252.dp
@Composable
fun ProfileScreen(navController: NavController,
                  email: String,
                  signOut: () -> Unit) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isAdmin = remember { mutableStateOf(false) }
    val isStaff = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isAdmin.value = UserRoleManager.isAdmin()
        isStaff.value = UserRoleManager.isStaff()
    }

    val hasNotificationPermission = remember {
        //Checking if Android 13+ or not if not assigning true directly
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            mutableStateOf(
                ContextCompat.checkSelfPermission(context,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
        }else{
            mutableStateOf(true)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(), onResult = { isGranted ->
        if (isGranted){
            hasNotificationPermission.value = true
        }else{
//                hasNotificationPermission.value = false
            Toast.makeText(context,"Permission Denied", Toast.LENGTH_SHORT).show()
        }
    })

    val isButtonEnabled = remember {
        mutableStateOf(true)
    }

    if (hasNotificationPermission.value) isButtonEnabled.value = false

//        if (!hasNotificationPermission.value){
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permissionLauncher.launch(
//                Manifest.permission.POST_NOTIFICATIONS)
//        }
//    }

    // Calculate surface height based on role and Android version
    val baseHeight = if (isAdmin.value) 252.dp else if (isStaff.value) 192.dp else 250.dp
    // Add height for new notification buttons
    val additionalHeight = 120.dp
    // Add height for permission toggle on Android 13+
    val permissionToggleHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 60.dp else 0.dp
    
    val surfaceHeight = baseHeight + additionalHeight + permissionToggleHeight

    val openDialog = remember { mutableStateOf(false) }

    Scaffold(backgroundColor = ShopKartUtils.offWhite) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(start = 20.dp, end = 20.dp, top = 50.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(surfaceHeight)
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(20.dp)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    if (isAdmin.value) {
                        ProfileCards(
                            title = "Admin",
                            leadingIcon = R.drawable.ic_admin,
                            tint = Color(0xFFFF5722),
                            space = 200.dp
                        ) {
                            navController.navigate(BottomNavScreens.AdminScreen.route)
                        }
                        Divider()
                    } else {
                        Box {}
                    }

                    //Show employee option for both admin and staff
                    if (isStaff.value) {
                        ProfileCards(
                            title = "Employee",
                            leadingIcon = R.drawable.ic_admin,
                            tint = Color(0xFFFF22E2),
                            space = 175.dp
                        ) {
                            navController.navigate(BottomNavScreens.EmployeeScreen.route)
                        }
                        Divider()
                    } else {
                        Box {}
                    }

                    if (!isStaff.value){

                        ProfileCards(
                            title = "My Profile",
                            leadingIcon = R.drawable.ic_profile,
                            tint = Color(0xFFBFCF1A)
                        ) {
                            navController.navigate(BottomNavScreens.MyProfile.route)
                        }
                        Divider()
                    }else{
                        Box{}
                    }

                    // Add test notifications button
                    ProfileCards(
                        title = "Notifications",
                        leadingIcon = R.drawable.ic_info,
                        tint = Color(0xFF3F51B5),
                        space = 165.dp
                    ) {
                        // Navigate to notifications screen
                        navController.navigate(BottomNavScreens.Notifications.route)
                    }
                    
                    Divider()
                    
                    // Add test notifications button
//                    ProfileCards(
//                        title = "Create Test Notification",
//                        leadingIcon = R.drawable.ic_info,
//                        tint = Color(0xFFFFC107),
//                        space = 115.dp
//                    ) {
//                        // Create a test notification
//                        scope.launch {
//                            NotificationUtils.createTestNotification(
//                                "Test Notification",
//                                "This is a test notification message created from the profile screen."
//                            )
//                            Toast.makeText(context, "Test notification created", Toast.LENGTH_SHORT).show()
//                        }
//                    }
                    
                    Divider()

                    ProfileCards(
                        title = "Log Out",
                        leadingIcon = R.drawable.ic_logout,
                        tint = Color.Red.copy(0.5f),
                        space = 190.dp
                    ) {
                        openDialog.value = true
                    }

                    Divider()

                    ProfileCards(
                        title = "About",
                        leadingIcon = R.drawable.ic_info,
                        tint = Color.Blue.copy(0.5f),
                        space = 205.dp
                    ) {
                        navController.navigate(BottomNavScreens.About.route)
                    }

                    Divider()

                    // Notification permission toggle button - only for Android 13+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ProfileCards(
                            title = "Notification Permission",
                            leadingIcon = R.drawable.notification,
                            tint = Color(0xFFD5EC08),
                            space = 122.dp,
                            isChecked = hasNotificationPermission,
                            showButton = true,
                            isButtonEnabled = isButtonEnabled.value
                        ) {
                            if (!hasNotificationPermission.value) 
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

            }
        }
    }

    //Calling Alert Dialog
    ShopKartDialog(openDialog = openDialog,
        onTap = { signOut.invoke() },
        context = context,
        navController = navController,
        title = "Log Out",
        subTitle = "Are You Sure, you want to Log Out?",
        button1 = "Log Out",
        button2 = "Cancel",
        toast = "Logged Out")

    // After


}

@Composable
fun ShopKartDialog(
    openDialog: MutableState<Boolean>,
    onTap: () -> Unit,
    context: Context,
    navController: NavController,
    title: String,
    subTitle: String,
    button1: String,
    button2: String,
    toast: String
) {
    if (openDialog.value) {
        androidx.compose.material.AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = { androidx.compose.material.Text(text = title) },
            text = { androidx.compose.material.Text(text = subTitle) },
            confirmButton = {
                androidx.compose.material.TextButton(onClick = {
                    onTap()
                    Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                    openDialog.value = false
                }) {
                    androidx.compose.material.Text(text = button1)
                }
            },
            dismissButton = {
                androidx.compose.material.TextButton(onClick = { openDialog.value = false }) {
                    androidx.compose.material.Text(text = button2)
                }
            }
        )
    }
}