//package com.shoppy.shop.components
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.expandVertically
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.animation.shrinkVertically
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.Card
//import androidx.compose.material.CircularProgressIndicator
//import androidx.compose.material.Icon
//import androidx.compose.material.MaterialTheme
//import androidx.compose.material.Snackbar
//import androidx.compose.material.Text
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Check
//import androidx.compose.material.icons.filled.Clear
//import androidx.compose.material.icons.filled.Info
//import androidx.compose.material.icons.outlined.Info
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.toArgb
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import coil.compose.AsyncImage
//import com.google.firebase.Timestamp
//import com.shoppy.shop.models.MCoupon
//import com.shoppy.shop.ui.theme.roboto
//import kotlinx.coroutines.launch
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//
//@Composable
//fun CouponSection(
//    coupons: List<MCoupon>,
//    onClaimCoupon: (MCoupon) -> Unit,
//    isLoading: Boolean = false,
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp)
//    ) {
//        Text(
//            text = "Special Offers & Discounts",
//            style = TextStyle(
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold,
//                fontFamily = roboto
//            ),
//            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
//        )
//
//        if (isLoading) {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(150.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator(color = Color.Black)
//            }
//        } else if (coupons.isEmpty()) {
//            EmptyCouponsMessage()
//        } else {
//            LazyRow(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 8.dp),
//                horizontalArrangement = Arrangement.spacedBy(12.dp),
//            ) {
//                item { Spacer(modifier = Modifier.width(4.dp)) }
//                items(coupons) { coupon ->
//                    CouponCard(
//                        coupon = coupon,
//                        onClaimCoupon = onClaimCoupon
//                    )
//                }
//                item { Spacer(modifier = Modifier.width(4.dp)) }
//            }
//        }
//    }
//}
//
//@Composable
//fun CouponCard(
//    coupon: MCoupon,
//    onClaimCoupon: (MCoupon) -> Unit
//) {
//    val scope = rememberCoroutineScope()
//    var isClaimingCoupon by remember { mutableStateOf(false) }
//    var showSuccess by remember { mutableStateOf(false) }
//
//    Card(
//        modifier = Modifier
//            .width(280.dp)
//            .height(150.dp)
//            .clickable(enabled = !isClaimingCoupon && !showSuccess) {
//                if (!isClaimingCoupon) {
//                    isClaimingCoupon = true
//                    scope.launch {
//                        onClaimCoupon(coupon)
//                        isClaimingCoupon = false
//                        showSuccess = true
//                        // Reset success message after a delay
//                        kotlinx.coroutines.delay(2000)
//                        showSuccess = false
//                    }
//                }
//            },
//        shape = RoundedCornerShape(12.dp),
//        elevation = 4.dp,
//        backgroundColor = Color.White
//    ) {
//        Box {
//            // Main content with gradient background
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(
//                        brush = Brush.linearGradient(
//                            colors = listOf(
//                                Color(0xFF2E2E2E),
//                                Color(0xFF3D3D3D)
//                            )
//                        )
//                    )
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    // Left section with coupon details
//                    Column(
//                        modifier = Modifier
//                            .weight(0.7f)
//                            .padding(end = 8.dp)
//                    ) {
//                        // Discount value
//                        Text(
//                            text = getDiscountText(coupon),
//                            style = TextStyle(
//                                fontSize = 20.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color.White,
//                                fontFamily = roboto
//                            )
//                        )
//
//                        Spacer(modifier = Modifier.height(4.dp))
//
//                        // Description
//                        Text(
//                            text = coupon.description,
//                            style = TextStyle(
//                                fontSize = 14.sp,
//                                color = Color.White.copy(alpha = 0.9f),
//                                fontFamily = roboto
//                            ),
//                            maxLines = 2,
//                            overflow = TextOverflow.Ellipsis
//                        )
//
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        // Minimum order value
//                        if (coupon.minimum_order_value > 0) {
//                            Text(
//                                text = "Min. order: ₫${String.format("%.0f", coupon.minimum_order_value)}",
//                                style = TextStyle(
//                                    fontSize = 12.sp,
//                                    color = Color.White.copy(alpha = 0.7f),
//                                    fontFamily = roboto
//                                )
//                            )
//                        }
//
//                        Spacer(modifier = Modifier.height(4.dp))
//
//                        // Expiration
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Icon(
//                                imageVector = Icons.Outlined.Info,
//                                contentDescription = null,
//                                tint = Color.White.copy(alpha = 0.7f),
//                                modifier = Modifier.size(14.dp)
//                            )
//                            Spacer(modifier = Modifier.width(4.dp))
//                            Text(
//                                text = "Expires: ${formatDate(coupon.expires_at)}",
//                                style = TextStyle(
//                                    fontSize = 12.sp,
//                                    color = Color.White.copy(alpha = 0.7f),
//                                    fontFamily = roboto
//                                )
//                            )
//                        }
//                    }
//
//                    // Right section with icon/button
//                    Box(
//                        modifier = Modifier
//                            .weight(0.3f)
//                            .align(Alignment.CenterVertically),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        when {
//                            isClaimingCoupon -> {
//                                CircularProgressIndicator(
//                                    modifier = Modifier.size(28.dp),
//                                    color = Color.White,
//                                    strokeWidth = 2.dp
//                                )
//                            }
//                            showSuccess -> {
//                                Box(
//                                    modifier = Modifier
//                                        .size(40.dp)
//                                        .background(Color.Green, CircleShape),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Icon(
//                                        imageVector = Icons.Default.Check,
//                                        contentDescription = "Claimed",
//                                        tint = Color.White
//                                    )
//                                }
//                            }
//                            else -> {
//                                PillButton(
//                                    title = "CLAIM",
//                                    color = MaterialTheme.colors.primary.toArgb(),
//                                    modifier = Modifier.padding(horizontal = 0.dp),
//
//                                ) {}
//                            }
//                        }
//                    }
//                }
//            }
//
//            // Code strip at the bottom
//            Box(
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .fillMaxWidth()
//                    .background(Color(0xFFFFCC00))
//                    .padding(vertical = 8.dp, horizontal = 16.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "CODE: ${coupon.code}",
//                    style = TextStyle(
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.Black,
//                        fontFamily = roboto,
//                        letterSpacing = 1.sp
//                    )
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun EmptyCouponsMessage() {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(150.dp)
//            .padding(horizontal = 16.dp)
//            .clip(RoundedCornerShape(12.dp))
//            .background(Color(0xFFF5F5F5))
//            .border(
//                width = 1.dp,
//                color = Color(0xFFE0E0E0),
//                shape = RoundedCornerShape(12.dp)
//            ),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Icon(
//                imageVector = Icons.Default.Info,
//                contentDescription = null,
//                tint = Color.Gray,
//                modifier = Modifier.size(36.dp)
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = "No active promotions right now",
//                style = TextStyle(
//                    fontSize = 16.sp,
//                    color = Color.Gray,
//                    fontFamily = roboto,
//                    textAlign = TextAlign.Center
//                ),
//                modifier = Modifier.padding(horizontal = 16.dp)
//            )
//            Spacer(modifier = Modifier.height(4.dp))
//            Text(
//                text = "Check back later for special offers",
//                style = TextStyle(
//                    fontSize = 14.sp,
//                    color = Color.Gray.copy(alpha = 0.7f),
//                    fontFamily = roboto,
//                    textAlign = TextAlign.Center
//                ),
//                modifier = Modifier.padding(horizontal = 16.dp)
//            )
//        }
//    }
//}
//
//private fun getDiscountText(coupon: MCoupon): String {
//    return when (coupon.discount_type) {
//        MCoupon.DISCOUNT_TYPE_PERCENTAGE -> "${coupon.discount_value.toInt()}% OFF"
//        MCoupon.DISCOUNT_TYPE_FIXED -> "₫${String.format("%.0f", coupon.discount_value)} OFF"
//        else -> "SPECIAL OFFER"
//    }
//}
//
//private fun formatDate(timestamp: Timestamp): String {
//    val date = timestamp.toDate()
//    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
//    return dateFormat.format(date)
//}