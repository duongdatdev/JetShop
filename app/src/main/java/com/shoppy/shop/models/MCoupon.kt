//package com.shoppy.shop.models
//
//import android.os.Parcelable
//import com.google.firebase.Timestamp
//import kotlinx.parcelize.Parcelize
//import kotlinx.parcelize.TypeParceler
//import kotlinx.parcelize.WriteWith
//import com.shoppy.shop.utils.TimestampParceler
//
//@TypeParceler<Timestamp, TimestampParceler>()
//@Parcelize
//data class MCoupon(
//    val coupon_id: String = "",
//    val code: String = "",
//    val description: String = "",
//    val discount_type: String = DISCOUNT_TYPE_PERCENTAGE, // "PERCENTAGE" or "FIXED"
//    val discount_value: Double = 0.0, // percentage (e.g., 10.0 for 10%) or fixed amount
//    val minimum_order_value: Double = 0.0, // minimum order value required to use the coupon
//    val created_at: @WriteWith<TimestampParceler> Timestamp = Timestamp.now(),
//    val expires_at: @WriteWith<TimestampParceler> Timestamp = Timestamp.now(),
//    val is_active: Boolean = true,
//    val created_by: String = "" // admin who created the coupon
//) : Parcelable {
//    fun convertToMap(): MutableMap<String, Any> {
//        return mutableMapOf(
//            "coupon_id" to coupon_id,
//            "code" to code,
//            "description" to description,
//            "discount_type" to discount_type,
//            "discount_value" to discount_value,
//            "minimum_order_value" to minimum_order_value,
//            "created_at" to created_at,
//            "expires_at" to expires_at,
//            "is_active" to is_active,
//            "created_by" to created_by
//        )
//    }
//
//    fun isValid(): Boolean {
//        val now = Timestamp.now()
//        return is_active && now.compareTo(expires_at) <= 0
//    }
//
//    companion object {
//        const val DISCOUNT_TYPE_PERCENTAGE = "PERCENTAGE"
//        const val DISCOUNT_TYPE_FIXED = "FIXED"
//    }
//}