package com.shoppy.shop.models

data class MCategory(
    val category_id: String? = null,
    val category_name: String? = null,
    val category_description: String? = null,
    val created_at: Long = System.currentTimeMillis()
) {
    fun convertToMap(): MutableMap<String, Any> {
        return mutableMapOf(
            "category_id" to (category_id ?: ""),
            "category_name" to (category_name ?: ""),
            "category_description" to (category_description ?: ""),
            "created_at" to created_at
        )
    }
} 