package com.shoppy.shop.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shoppy.shop.models.NotificationData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    // Collection reference
    private val notificationsCollection = firestore.collection("notifications")
    
    // Get current user ID
    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""
    
    // Get notifications for current user as a Flow
    fun getNotificationsFlow(): Flow<List<NotificationData>> = callbackFlow {
        if (currentUserId.isEmpty()) {
            trySend(emptyList())
            return@callbackFlow
        }
        
        val listenerRegistration = notificationsCollection
            .whereEqualTo("userId", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }
                
                val notifications = snapshot?.documents?.mapNotNull {
                    it.toObject(NotificationData::class.java)
                } ?: emptyList()
                
                trySend(notifications)
            }
            
        // Close the listener when the flow is cancelled
        awaitClose { listenerRegistration.remove() }
    }
    
    // Get unread notifications count
    fun getUnreadNotificationsFlow(): Flow<Int> = callbackFlow {
        if (currentUserId.isEmpty()) {
            trySend(0)
            return@callbackFlow
        }
        
        val listenerRegistration = notificationsCollection
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }
                
                val count = snapshot?.documents?.size ?: 0
                trySend(count)
            }
            
        // Close the listener when the flow is cancelled
        awaitClose { listenerRegistration.remove() }
    }
    
    // Mark notification as read
    suspend fun markAsRead(notificationId: String) {
        notificationsCollection.document(notificationId)
            .update("isRead", true)
    }
    
    // Mark all notifications as read
    suspend fun markAllAsRead() {
        if (currentUserId.isEmpty()) return
        
        // Get all unread notifications for current user
        val unreadNotifications = notificationsCollection
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("isRead", false)
            .get()
            .await()
        
        // Create a batch update
        val batch = firestore.batch()
        unreadNotifications.documents.forEach { document ->
            batch.update(document.reference, "isRead", true)
        }
        
        // Commit the batch
        batch.commit()
    }
} 