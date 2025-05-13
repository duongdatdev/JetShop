package com.shoppy.shop.models

data class MRating(
    var rating_id: String? = null,
    var product_id: String? = null,
    var user_id: String? = null,
    var user_name: String? = null,
    var rating_value: Int? = null, // 1-5 stars
    var comment: String? = null,
    var timestamp: Long? = null
) {
    fun convertToMap(): MutableMap<String, Any> {
        return mutableMapOf(
            "rating_id" to (this.rating_id ?: ""),
            "product_id" to (this.product_id ?: ""),
            "user_id" to (this.user_id ?: ""),
            "user_name" to (this.user_name ?: ""),
            "rating_value" to (this.rating_value ?: 0),
            "comment" to (this.comment ?: ""),
            "timestamp" to (this.timestamp ?: 0)
        )
    }
} 