package com.shoppy.shop.repository

import android.util.Log
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
            // Clear the exception if successful
            dataOrException.e = null
        } catch (e: Exception) {
            dataOrException.e = e
            dataOrException.loading = false
            // Ensure we have an empty list instead of null
            dataOrException.data = emptyList()
        }
        
        return dataOrException
    }

    suspend fun getAllRatings(): DataOrException<List<MRating>, Boolean, Exception> {
        val dataOrException = DataOrException<List<MRating>, Boolean, Exception>(
            listOf(), true, Exception("")
        )

        try {
            val result = ratingsCollection
                .get()
                .await()

            val ratings = result.documents.mapNotNull { document ->
                val rating = document.toObject(MRating::class.java)
                Log.d("RatingRepository", "Rating: ${document.id} -> ${rating?.product_id}, ${rating?.rating_value}, ${rating?.user_id}")
                rating
            }

            Log.d("RatingRepository", "Total Ratings Found: ${ratings.size}")
            dataOrException.data = ratings
            dataOrException.loading = false
            dataOrException.e = null
        } catch (e: Exception) {
            Log.e("RatingRepository", "Error fetching all ratings", e)
            dataOrException.e = e
            dataOrException.loading = false
            dataOrException.data = emptyList()
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