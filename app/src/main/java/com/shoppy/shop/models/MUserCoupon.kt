//package com.shoppy.shop.models
//
//import android.os.Parcelable
//import com.google.firebase.Timestamp
//import kotlinx.parcelize.Parcelize
//import kotlinx.parcelize.TypeParceler
//import kotlinx.parcelize.WriteWith
//import com.shoppy.shop.utils.TimestampParceler
//import com.shoppy.shop.utils.NullableTimestampParceler
//
//@TypeParceler<Timestamp, TimestampParceler>()
//@TypeParceler<Timestamp?, NullableTimestampParceler>()
//@Parcelize
//data class MUserCoupon(
//    val id: String = "",
//    val user_id: String = "",
//    val coupon_id: String = "",
//    val claimed_at: @WriteWith<TimestampParceler> Timestamp = Timestamp.now(),
//    val is_used: Boolean = false,
//    val used_at: @WriteWith<NullableTimestampParceler> Timestamp? = null,
//    val order_id: String = "" // order where the coupon was used
//) : Parcelable {
//    fun convertToMap(): MutableMap<String, Any?> {
//        return mutableMapOf(
//            "id" to id,
//            "user_id" to user_id,
//            "coupon_id" to coupon_id,
//            "claimed_at" to claimed_at,
//            "is_used" to is_used,
//            "used_at" to used_at,
//            "order_id" to order_id
//        )
//    }
//}