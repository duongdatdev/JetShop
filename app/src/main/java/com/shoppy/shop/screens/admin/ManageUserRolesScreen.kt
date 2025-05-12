package com.shoppy.shop.screens.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.components.BackButton
import com.shoppy.shop.models.MUser
import com.shoppy.shop.screens.admin.rolemanagement.RoleManagementViewModel
import com.shoppy.shop.ui.theme.roboto

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ManageUserRolesScreen(
    navController: NavHostController,
    viewModel: RoleManagementViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val users = viewModel.users.collectAsState().value
    var expandedUser by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { BackButton(navController = navController, topBarTitle = "Manage User Roles") },
        backgroundColor = ShopKartUtils.offWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "User Role Management",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = roboto
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(users) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = user.name ?: "Unknown User",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = roboto
                                )
                            )
                            Text(
                                text = user.email ?: "",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontFamily = roboto
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            ExposedDropdownMenuBox(
                                expanded = expandedUser == user.email,
                                onExpandedChange = { expandedUser = if (expandedUser == user.email) null else user.email }
                            ) {
                                OutlinedTextField(
                                    value = user.role,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Current Role") },
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Select Role") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                ExposedDropdownMenu(
                                    expanded = expandedUser == user.email,
                                    onDismissRequest = { expandedUser = null }
                                ) {
                                    DropdownMenuItem(
                                        onClick = {
                                            viewModel.updateUserRole(user.email!!, MUser.ROLE_USER) {
                                                Toast.makeText(context, "Role updated to User", Toast.LENGTH_SHORT).show()
                                            }
                                            expandedUser = null
                                        }
                                    ) {
                                        Text("User")
                                    }
                                    DropdownMenuItem(
                                        onClick = {
                                            viewModel.updateUserRole(user.email!!, MUser.ROLE_STAFF) {
                                                Toast.makeText(context, "Role updated to Staff", Toast.LENGTH_SHORT).show()
                                            }
                                            expandedUser = null
                                        }
                                    ) {
                                        Text("Staff")
                                    }
                                    DropdownMenuItem(
                                        onClick = {
                                            viewModel.updateUserRole(user.email!!, MUser.ROLE_ADMIN) {
                                                Toast.makeText(context, "Role updated to Admin", Toast.LENGTH_SHORT).show()
                                            }
                                            expandedUser = null
                                        }
                                    ) {
                                        Text("Admin")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 