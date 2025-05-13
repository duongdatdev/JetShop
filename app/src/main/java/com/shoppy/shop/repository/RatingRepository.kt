package com.shoppy.shop.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.shoppy.shop.data.DataOrException
import com.shoppy.shop.models.MRating
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RatingRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val ratingsCollection = firestore.collection("Ratings")
    private val productsCollection = firestore.collection("AllProducts")

    suspend fun addRating(rating: MRating): Result<String> {
        return try {
            val docRef = ratingsCollection.document()
            rating.rating_id = docRef.id
            docRef.set(rating.convertToMap()).await()
            
            // Update the product's average rating
            updateProductRating(rating.product_id!!)
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRatingsByProductId(productId: String): DataOrException<List<MRating>, Boolean, Exception> {
        val dataOrException = DataOrException<List<MRating>, Boolean, Exception>(
            listOf(), true, Exception("")
        )
        
        try {
            val result = ratingsCollection
                .whereEqualTo("product_id", productId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val ratings = result.documents.mapNotNull { document ->
                document.toObject(MRating::class.java)
            }
            
            dataOrException.data = ratings
            dataOrException.loading = false
        } catch (e: Exception) {
            dataOrException.e = e
            dataOrException.loading = false
        }
        
        return dataOrException
    }

    suspend fun hasUserRatedProduct(userId: String, productId: String): Boolean {
        return try {
            val result = ratingsCollection
                .whereEqualTo("user_id", userId)
                .whereEqualTo("product_id", productId)
                .get()
                .await()
            
            !result.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun updateProductRating(productId: String) {
        try {
            val ratings = ratingsCollection
                .whereEqualTo("product_id", productId)
                .get()
                .await()
                .documents.mapNotNull { it.toObject(MRating::class.java) }
            
            if (ratings.isNotEmpty()) {
                val totalRating = ratings.sumOf { it.rating_value ?: 0 }
                val averageRating = totalRating.toFloat() / ratings.size
                
                productsCollection.document(productId).update(
                    mapOf(
                        "average_rating" to averageRating,
                        "rating_count" to ratings.size
                    )
                ).await()
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
} 