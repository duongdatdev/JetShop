package com.shoppy.shop.models

data class MProducts(
    var product_url: Any? = null,
    var product_title: String? = null,
    var product_price: Int? = null,
    var product_description: String? = null,
    var stock: Int? = null,
    var category: String? = null,
    var product_id: String? = null,
    val shop_id: String? = null,
    val shop_name: String? = null,
    var average_rating: Float? = 0f,
    var rating_count: Int? = 0
) {

    fun convertToMap(): MutableMap<String,Any>{

        return mutableMapOf(
            "product_url" to this.product_url!!,

            "product_title" to this.product_title!!,

            "product_price" to this.product_price!!,

            "product_description" to this.product_description!!,

            "stock" to this.stock!!,

            "category" to this.category!!,

            "product_id" to this.product_id!!,
            "shop_id" to this.shop_id!!,
            "shop_name" to this.shop_name!!,
            "average_rating" to (this.average_rating ?: 0f),
            "rating_count" to (this.rating_count ?: 0)
        )
    }
}