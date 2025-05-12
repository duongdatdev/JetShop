package com.shoppy.shop.models

data class MShop(
    var id: String? = null,
    var name: String? = null,
    var address: String? = null,
    var phone: String? = null,
    var email: String? = null,
    var description: String? = null,
    var logo: String? = null,
    var owner_id: String? = null,
    var created_at: Long = System.currentTimeMillis(),
    var updated_at: Long = System.currentTimeMillis()
) {
    fun convertToMap(): MutableMap<String, Any> {
        return mutableMapOf(
            "shop_id" to (this.id ?: ""),
            "name" to (this.name ?: ""),
            "address" to (this.address ?: ""),
            "phone" to (this.phone ?: ""),
            "email" to (this.email ?: ""),
            "description" to (this.description ?: ""),
            "logo" to (this.logo ?: ""),
            "owner_id" to (this.owner_id ?: ""),
            "created_at" to this.created_at,
            "updated_at" to this.updated_at
        )
    }
} 