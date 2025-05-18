//package com.shoppy.shop.repository
//
//import com.google.firebase.Timestamp
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.Query
//import com.shoppy.shop.data.DataOrException
//import com.shoppy.shop.models.MCoupon
//import com.shoppy.shop.models.MUserCoupon
//import kotlinx.coroutines.channels.awaitClose
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.callbackFlow
//import kotlinx.coroutines.tasks.await
//import java.util.UUID
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class CouponRepository @Inject constructor(
//    private val firestore: FirebaseFirestore,
//    private val auth: FirebaseAuth
//) {
//    // Collection references
//    private val couponsCollection = firestore.collection("Coupons")
//    private val userCouponsCollection = firestore.collection("UserCoupons")
//
//    // Get current user ID
//    private val currentUserId: String
//        get() = auth.currentUser?.uid ?: ""
//
//    // Get all active coupons as a Flow
//    fun getActiveCouponsFlow(): Flow<List<MCoupon>> = callbackFlow {
//        val listenerRegistration = couponsCollection
//            .whereEqualTo("is_active", true)
//            .orderBy("created_at", Query.Direction.DESCENDING)
//            .addSnapshotListener { snapshot, error ->
//                if (error != null) {
//                    // Handle error
//                    trySend(emptyList())
//                    return@addSnapshotListener
//                }
//
//                val coupons = snapshot?.documents?.mapNotNull {
//                    it.toObject(MCoupon::class.java)
//                } ?: emptyList()
//
//                // Filter out expired coupons
//                val now = Timestamp.now()
//                val validCoupons = coupons.filter { coupon ->
//                    now.compareTo(coupon.expires_at) <= 0
//                }
//
//                trySend(validCoupons)
//            }
//
//        // Close the listener when the flow is cancelled
//        awaitClose { listenerRegistration.remove() }
//    }
//
//    // Get user's claimed coupons as a Flow
//    fun getUserCouponsFlow(): Flow<List<MUserCoupon>> = callbackFlow {
//        if (currentUserId.isEmpty()) {
//            trySend(emptyList())
//            return@callbackFlow
//        }
//
//        val listenerRegistration = userCouponsCollection
//            .whereEqualTo("user_id", currentUserId)
//            .orderBy("claimed_at", Query.Direction.DESCENDING)
//            .addSnapshotListener { snapshot, error ->
//                if (error != null) {
//                    // Handle error
//                    trySend(emptyList())
//                    return@addSnapshotListener
//                }
//
//                val userCoupons = snapshot?.documents?.mapNotNull {
//                    it.toObject(MUserCoupon::class.java)
//                } ?: emptyList()
//
//                trySend(userCoupons)
//            }
//
//        // Close the listener when the flow is cancelled
//        awaitClose { listenerRegistration.remove() }
//    }
//
//    // Get user's available (claimed but not used) coupons with full details
//    suspend fun getAvailableUserCoupons(): List<Pair<MUserCoupon, MCoupon>> {
//        if (currentUserId.isEmpty()) {
//            return emptyList()
//        }
//
//        try {
//            val userCouponsSnapshot = userCouponsCollection
//                .whereEqualTo("user_id", currentUserId)
//                .whereEqualTo("is_used", false)
//                .get()
//                .await()
//
//            val userCoupons = userCouponsSnapshot.documents.mapNotNull {
//                it.toObject(MUserCoupon::class.java)
//            }
//
//            val result = mutableListOf<Pair<MUserCoupon, MCoupon>>()
//
//            for (userCoupon in userCoupons) {
//                val couponSnapshot = couponsCollection
//                    .document(userCoupon.coupon_id)
//                    .get()
//                    .await()
//
//                val coupon = couponSnapshot.toObject(MCoupon::class.java)
//                if (coupon != null && coupon.isValid()) {
//                    result.add(Pair(userCoupon, coupon))
//                }
//            }
//
//            return result
//        } catch (e: Exception) {
//            // Handle exceptions
//            return emptyList()
//        }
//    }
//
//    // Claim a coupon
//    suspend fun claimCoupon(couponId: String): Result<Boolean> {
//        if (currentUserId.isEmpty()) {
//            return Result.failure(Exception("User not authenticated"))
//        }
//
//        return try {
//            // Check if coupon exists and is valid
//            val couponSnapshot = couponsCollection.document(couponId).get().await()
//            val coupon = couponSnapshot.toObject(MCoupon::class.java)
//                ?: return Result.failure(Exception("Coupon not found"))
//
//            if (!coupon.isValid()) {
//                return Result.failure(Exception("Coupon has expired or is not active"))
//            }
//
//            // Check if user has already claimed this coupon
//            val existingClaim = userCouponsCollection
//                .whereEqualTo("user_id", currentUserId)
//                .whereEqualTo("coupon_id", couponId)
//                .get()
//                .await()
//
//            if (!existingClaim.isEmpty) {
//                return Result.failure(Exception("You have already claimed this coupon"))
//            }
//
//            // Create new user coupon
//            val userCouponId = UUID.randomUUID().toString()
//            val userCoupon = MUserCoupon(
//                id = userCouponId,
//                user_id = currentUserId,
//                coupon_id = couponId,
//                claimed_at = Timestamp.now()
//            )
//
//            // Save to Firestore
//            userCouponsCollection.document(userCouponId).set(userCoupon.convertToMap()).await()
//
//            Result.success(true)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    // Validate and apply a coupon code
//    suspend fun validateCouponCode(code: String, orderTotal: Double): Result<MCoupon> {
//        try {
//            // Find coupon by code
//            val couponSnapshot = couponsCollection
//                .whereEqualTo("code", code)
//                .whereEqualTo("is_active", true)
//                .get()
//                .await()
//
//            if (couponSnapshot.isEmpty) {
//                return Result.failure(Exception("Invalid coupon code"))
//            }
//
//            val coupon = couponSnapshot.documents.first().toObject(MCoupon::class.java)
//                ?: return Result.failure(Exception("Error retrieving coupon"))
//
//            // Check if coupon is valid
//            if (!coupon.isValid()) {
//                return Result.failure(Exception("This coupon has expired"))
//            }
//
//            // Check minimum order value
//            if (orderTotal < coupon.minimum_order_value) {
//                return Result.failure(
//                    Exception("This coupon requires a minimum order value of ₫${String.format("%.0f", coupon.minimum_order_value)}")
//                )
//            }
//
//            return Result.success(coupon)
//        } catch (e: Exception) {
//            return Result.failure(e)
//        }
//    }
//
//    // Mark a coupon as used
//    suspend fun markCouponAsUsed(userCouponId: String, orderId: String): Result<Boolean> {
//        if (currentUserId.isEmpty()) {
//            return Result.failure(Exception("User not authenticated"))
//        }
//
//        return try {
//            val userCouponRef = userCouponsCollection.document(userCouponId)
//            val userCouponSnapshot = userCouponRef.get().await()
//
//            if (!userCouponSnapshot.exists()) {
//                return Result.failure(Exception("Coupon not found"))
//            }
//
//            val userCoupon = userCouponSnapshot.toObject(MUserCoupon::class.java)
//                ?: return Result.failure(Exception("Error retrieving coupon"))
//
//            if (userCoupon.user_id != currentUserId) {
//                return Result.failure(Exception("Not authorized to use this coupon"))
//            }
//
//            if (userCoupon.is_used) {
//                return Result.failure(Exception("This coupon has already been used"))
//            }
//
//            // Update the user coupon with used info
//            userCouponRef.update(
//                mapOf(
//                    "is_used" to true,
//                    "used_at" to Timestamp.now(),
//                    "order_id" to orderId
//                )
//            ).await()
//
//            Result.success(true)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    // Create a new coupon (Admin only)
//    suspend fun createCoupon(coupon: MCoupon): Result<String> {
//        return try {
//            val couponId = UUID.randomUUID().toString()
//            val newCoupon = coupon.copy(coupon_id = couponId, created_by = currentUserId)
//
//            couponsCollection.document(couponId).set(newCoupon.convertToMap()).await()
//
//            Result.success(couponId)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    // Calculate discount amount based on coupon and order total
//    fun calculateDiscount(coupon: MCoupon, orderTotal: Double): Double {
//        return when (coupon.discount_type) {
//            MCoupon.DISCOUNT_TYPE_PERCENTAGE -> {
//                (orderTotal * coupon.discount_value / 100).coerceAtMost(orderTotal)
//            }
//            MCoupon.DISCOUNT_TYPE_FIXED -> {
//                coupon.discount_value.coerceAtMost(orderTotal)
//            }
//            else -> 0.0
//        }
//    }
//}