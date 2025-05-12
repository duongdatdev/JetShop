package com.shoppy.shop.models

data class MUser (
    var id: String? = null,
    var name: String? = null,
    var email: String? = null,
    var password: String? = null,
    var phone_no: String? = null,
    var address: String? = null,
    var profile_image: String? = null,
    var role: String = "USER", // Default role is regular user
    var shop_id: String? = null, // Shop ID for employees
    var position: String? = null, // Employee position
    var hire_date: Long? = null, // Employee hire date
    var salary: Double? = null, // Employee salary
    var status: String = "ACTIVE" // Employee status (ACTIVE, INACTIVE, ON_LEAVE)
){

    fun convertToMap(): MutableMap<String, Any>{
        return mutableMapOf(
            "user_id" to (this.id ?: ""),
            "name" to (this.name ?: ""),
            "email" to (this.email ?: ""),
            "password" to (this.password ?: ""),
            "phone_no" to (this.phone_no ?: ""),
            "address" to (this.address ?: ""),
            "profile_image" to (this.profile_image ?: ""),
            "role" to this.role,
            "shop_id" to (this.shop_id ?: ""),
            "position" to (this.position ?: ""),
            "hire_date" to (this.hire_date ?: System.currentTimeMillis()),
            "salary" to (this.salary ?: 0.0),
            "status" to this.status
        )
    }

    companion object {
        const val ROLE_USER = "USER"
        const val ROLE_ADMIN = "ADMIN"
        const val ROLE_STAFF = "STAFF"
        const val ROLE_EMPLOYEE = "EMPLOYEE"
        
        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_INACTIVE = "INACTIVE"
        const val STATUS_ON_LEAVE = "ON_LEAVE"
    }
}