package com.shoppy.shop.utils

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shoppy.shop.models.NotificationData
import kotlinx.coroutines.tasks.await
import java.util.UUID

object NotificationUtils {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * Create a test notification for the current user
     */
    suspend fun createTestNotification(title: String, message: String) {
        val userId = auth.currentUser?.uid ?: return
        
        val notification = NotificationData(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            timestamp = Timestamp.now(),
            isRead = false,
            userId = userId
        )
        
        firestore.collection("notifications")
            .document(notification.id)
            .set(notification)
            .await()
    }
    
    /**
     * Create multiple test notifications for the current user
     */
    suspend fun createMultipleTestNotifications(count: Int) {
        val userId = auth.currentUser?.uid ?: return
        
        for (i in 1..count) {
            val notification = NotificationData(
                id = UUID.randomUUID().toString(),
                title = "Test Notification #$i",
                message = "This is a test notification message #$i to test the notification system.",
                timestamp = Timestamp.now(),
                isRead = false,
                userId = userId
            )
            
            firestore.collection("notifications")
                .document(notification.id)
                .set(notification)
                .await()
        }
    }
    
    /**
     * Create a notification for a new order
     */
    suspend fun createOrderNotification(orderId: String, productName: String) {
        val userId = auth.currentUser?.uid ?: return
        
        val notification = NotificationData(
            id = UUID.randomUUID().toString(),
            title = "Order Placed Successfully",
            message = "Your order for $productName has been placed successfully. Order ID: $orderId",
            timestamp = Timestamp.now(),
            isRead = false,
            userId = userId
        )
        
        firestore.collection("notifications")
            .document(notification.id)
            .set(notification)
            .await()
    }
    
    /**
     * Create a notification for order status update
     */
    suspend fun createOrderStatusNotification(orderId: String, status: String) {
        val userId = auth.currentUser?.uid ?: return
        
        val notification = NotificationData(
            id = UUID.randomUUID().toString(),
            title = "Order Status Updated",
            message = "Your order (ID: $orderId) status has been updated to: $status",
            timestamp = Timestamp.now(),
            isRead = false,
            userId = userId
        )
        
        firestore.collection("notifications")
            .document(notification.id)
            .set(notification)
            .await()
    }
} 