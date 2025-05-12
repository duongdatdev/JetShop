package com.shoppy.shop.screens.admin

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.components.BackButton
import com.shoppy.shop.components.ShopKartAppBar2
import com.shoppy.shop.models.MCategory
import com.shoppy.shop.navigation.BottomNavScreens
import com.shoppy.shop.ui.theme.roboto
import java.util.*

@Composable
fun ManageCategoriesScreen(
    navController: NavHostController,
    viewModel: AdminScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryName by remember { mutableStateOf("") }
    var categoryDescription by remember { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            BackButton(navController = navController, topBarTitle = "Manage Categories")
        },
        backgroundColor = ShopKartUtils.offWhite,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (viewModel.categories.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No categories yet. Add your first category!",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Gray,
                            fontFamily = roboto
                        )
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(viewModel.categories) { category ->
                        CategoryItem(
                            category = category,
                            onDelete = { showDeleteConfirmation = it }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add New Category") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = categoryName,
                            onValueChange = { categoryName = it },
                            label = { Text("Category Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = categoryDescription,
                            onValueChange = { categoryDescription = it },
                            label = { Text("Category Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (categoryName.isNotBlank()) {
                                viewModel.addCategory(
                                    MCategory(
                                        category_id = UUID.randomUUID().toString(),
                                        category_name = categoryName,
                                        category_description = categoryDescription
                                    )
                                )
                                categoryName = ""
                                categoryDescription = ""
                                showAddDialog = false
                                Toast.makeText(context, "Category added successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Please enter category name", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showDeleteConfirmation != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = null },
                title = { Text("Delete Category") },
                text = { Text("Are you sure you want to delete this category? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmation?.let { categoryId ->
                                viewModel.deleteCategory(categoryId)
                                Toast.makeText(context, "Category deleted successfully", Toast.LENGTH_SHORT).show()
                            }
                            showDeleteConfirmation = null
                        }
                    ) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun CategoryItem(
    category: MCategory,
    onDelete: (String) -> Unit
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.category_name ?: "",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = roboto
                )
                if (!category.category_description.isNullOrBlank()) {
                    Text(
                        text = category.category_description,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontFamily = roboto,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = { onDelete(category.category_id ?: "") }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Category",
                    tint = Color.Red
                )
            }
        }
    }
} 