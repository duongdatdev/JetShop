package com.shoppy.shop.screens.admin.rolemanagement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.shoppy.shop.ui.theme.roboto

@Composable
fun RoleManagementScreen(
    navController: NavHostController,
    viewModel: RoleManagementViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val users = viewModel.users.collectAsState().value
    var showRoleDialog by remember { mutableStateOf<MUser?>(null) }

    Scaffold(
        topBar = { BackButton(navController = navController, topBarTitle = "Manage Roles") },
        backgroundColor = ShopKartUtils.offWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "User Role Management",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = roboto
                ),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(users) { user ->
                    UserRoleCard(
                        user = user,
                        onRoleClick = { showRoleDialog = user }
                    )
                }
            }
        }

        // Role selection dialog
        showRoleDialog?.let { user ->
            AlertDialog(
                onDismissRequest = { showRoleDialog = null },
                title = { Text("Change Role for ${user.name}") },
                text = {
                    Column {
                        RoleOption(
                            role = MUser.ROLE_USER,
                            currentRole = user.role,
                            onClick = {
                                viewModel.updateUserRole(user.email!!, MUser.ROLE_USER)
                                showRoleDialog = null
                            }
                        )
                        RoleOption(
                            role = MUser.ROLE_STAFF,
                            currentRole = user.role,
                            onClick = {
                                viewModel.updateUserRole(user.email!!, MUser.ROLE_STAFF)
                                showRoleDialog = null
                            }
                        )
                        RoleOption(
                            role = MUser.ROLE_ADMIN,
                            currentRole = user.role,
                            onClick = {
                                viewModel.updateUserRole(user.email!!, MUser.ROLE_ADMIN)
                                showRoleDialog = null
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRoleDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun UserRoleCard(
    user: MUser,
    onRoleClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = user.name ?: "Unknown",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = roboto
                    )
                )
                Text(
                    text = user.email ?: "",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontFamily = roboto
                    )
                )
            }
            TextButton(onClick = onRoleClick) {
                Text(
                    text = user.role,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = roboto
                    )
                )
            }
        }
    }
}

@Composable
fun RoleOption(
    role: String,
    currentRole: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = role == currentRole,
            onClick = onClick
        )
        Text(
            text = role,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}