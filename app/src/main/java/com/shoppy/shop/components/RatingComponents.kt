package com.shoppy.shop.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.models.MRating
import com.shoppy.shop.ui.theme.roboto
import com.shoppy.shop.viewmodels.RatingViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RatingStars(
    rating: Float,
    modifier: Modifier = Modifier,
    starSize: Int = 16,
    starColor: Color = Color(0xFFFFC107),
    showRatingText: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Full stars
        repeat(rating.toInt()) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Rating Star",
                tint = starColor,
                modifier = Modifier.size(starSize.dp)
            )
        }
        
        // Half star (if applicable)
        if (rating - rating.toInt() >= 0.5f) {
            // TODO: Implement half star if needed. For now, we'll round up.
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Rating Star",
                tint = starColor,
                modifier = Modifier.size(starSize.dp)
            )
        }
        
        // Empty stars
        repeat(5 - kotlin.math.ceil(rating).toInt()) {
            Icon(
                imageVector = Icons.Outlined.Star,
                contentDescription = "Empty Star",
                tint = starColor,
                modifier = Modifier.size(starSize.dp)
            )
        }
        
        if (showRatingText) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = String.format("%.1f", rating),
                style = TextStyle(
                    fontSize = (starSize - 2).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = roboto
                )
            )
        }
    }
}

@Composable
fun RatingSubmissionForm(
    productId: String,
    viewModel: RatingViewModel = hiltViewModel(),
    onRatingSubmitted: () -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp,
        color = ShopKartUtils.offWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Rate This Product",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = roboto
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..5) {
                    Icon(
                        imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Star $i",
                        tint = if (i <= rating) Color(0xFFFFC107) else Color.Gray,
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { rating = i }
                            .padding(4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Your Review (Optional)",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = roboto
                ),
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                placeholder = { Text("Share your experience with this product...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = ShopKartUtils.blue,
                    unfocusedBorderColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (rating > 0) {
                        viewModel.submitRating(
                            productId = productId,
                            rating = rating,
                            comment = comment,
                            onSuccess = {
                                Toast.makeText(context, "Thank you for your review!", Toast.LENGTH_SHORT).show()
                                onRatingSubmitted()
                            },
                            onError = { error ->
                                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "Please select a rating", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.align(Alignment.End),
                enabled = rating > 0,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = ShopKartUtils.blue,
                    contentColor = Color.White,
                    disabledBackgroundColor = Color.Gray,
                    disabledContentColor = Color.White
                )
            ) {
                Text("Submit Review")
            }
        }
    }
}

@Composable
fun RatingsList(
    ratings: List<MRating>,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ShopKartUtils.blue)
        }
    } else if (ratings.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No reviews yet",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = roboto,
                    color = Color.Gray
                )
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // Use regular Column instead of LazyColumn to ensure all items are visible
            ratings.forEach { rating ->
                RatingItem(rating = rating)
                Divider(
                    color = Color.LightGray,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun RatingItem(rating: MRating) {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val formattedDate = remember(rating.timestamp) {
        rating.timestamp?.let { dateFormatter.format(Date(it)) } ?: ""
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User avatar (placeholder circle)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ShopKartUtils.blue.copy(alpha = 0.2f))
                    .border(1.dp, ShopKartUtils.blue.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rating.user_name?.firstOrNull()?.toString() ?: "A",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopKartUtils.blue
                    )
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = rating.user_name ?: "Anonymous",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = roboto
                    )
                )
                
                RatingStars(
                    rating = rating.rating_value?.toFloat() ?: 0f,
                    starSize = 14,
                    showRatingText = false
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = formattedDate,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontFamily = roboto
                )
            )
        }
        
        if (!rating.comment.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 52.dp, end = 8.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.LightGray.copy(alpha = 0.2f),
                elevation = 0.dp
            ) {
                Text(
                    text = rating.comment!!,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = roboto
                    ),
                    modifier = Modifier.padding(8.dp),
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun RatingsSummary(
    averageRating: Float,
    ratingCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = String.format("%.1f", averageRating),
            style = TextStyle(
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = roboto
            )
        )
        
        RatingStars(
            rating = averageRating,
            starSize = 20,
            showRatingText = false
        )
        
        Text(
            text = "Based on $ratingCount ${if (ratingCount == 1) "review" else "reviews"}",
            style = TextStyle(
                fontSize = 14.sp,
                color = Color.Gray,
                fontFamily = roboto
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Divider(
            color = Color.LightGray,
            thickness = 1.dp,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
} 