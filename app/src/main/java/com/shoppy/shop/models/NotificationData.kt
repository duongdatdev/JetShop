package com.shoppy.shop.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class NotificationData(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false,
    val userId: String = "",
    val imageUrl: String? = null
)
