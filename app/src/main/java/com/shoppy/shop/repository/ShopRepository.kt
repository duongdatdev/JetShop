package com.shoppy.shop.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.shoppy.shop.models.MShop
import com.shoppy.shop.models.MUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val shopsCollection = firestore.collection("Shops")
    private val usersCollection = firestore.collection("Users")

    suspend fun createShop(shop: MShop): Result<String> {
        return try {
            val docRef = shopsCollection.document()
            shop.id = docRef.id
            docRef.set(shop.convertToMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateShop(shop: MShop): Result<Unit> {
        return try {
            shop.updated_at = System.currentTimeMillis()
            shopsCollection.document(shop.id!!)
                .update(shop.convertToMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getUserDocumentByUserId(userId: String) =
        usersCollection.whereEqualTo("user_id", userId).get().await().documents.firstOrNull()


    suspend fun getShopById(shopId: String): Result<MShop> {
        return try {
            val document = shopsCollection.document(shopId).get().await()
            if (document.exists()) {
                val shop = document.toObject(MShop::class.java)
                shop?.id = document.id
                Result.success(shop!!)
            } else {
                Result.failure(Exception("Shop not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getShopsByOwnerId(ownerId: String): Result<List<MShop>> {
        return try {
            val snapshot = shopsCollection
                .whereEqualTo("owner_id", ownerId)
                .get()
                .await()
            
            val shops = snapshot.documents.mapNotNull { it.toObject(MShop::class.java) }
            Result.success(shops)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteShop(shopId: String): Result<Unit> {
        return try {
            shopsCollection.document(shopId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getShopByEmployeeId(employeeId: String): Result<MShop> {
        return try {
            val userDoc = getUserDocumentByUserId(employeeId)
                ?: return Result.failure(Exception("Employee not found"))

            val employee = userDoc.toObject(MUser::class.java)
            val shopId = employee?.shop_id
                ?: return Result.failure(Exception("Employee not assigned to any shop"))

            val shopDoc = shopsCollection.document(shopId).get().await()
            if (!shopDoc.exists()) return Result.failure(Exception("Shop not found"))

            val shop = shopDoc.toObject(MShop::class.java)
            shop?.id = shopDoc.id
            Result.success(shop!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun updateShopForEmployee(employeeId: String, shop: MShop): Result<Unit> {
        return try {
            val userDoc = getUserDocumentByUserId(employeeId)
                ?: return Result.failure(Exception("Employee not found"))

            val employee = userDoc.toObject(MUser::class.java)
            Log.d("ShopRepository", employee?.shop_id + "Shop: $shop.id")
            if (employee?.shop_id != shop.id) {
                return Result.failure(Exception("Employee not authorized to update this shop"))
            }

            shop.updated_at = System.currentTimeMillis()
            shopsCollection.document(shop.id!!).update(shop.convertToMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun initializeShopFromEmployee(employeeId: String): Result<String> {
        return try {
            val userDoc = getUserDocumentByUserId(employeeId)
                ?: return Result.failure(Exception("Employee not found"))

            val employee = userDoc.toObject(MUser::class.java)
                ?: return Result.failure(Exception("Failed to parse employee data"))

            val shop = MShop(
                name = "${employee.name}'s Store",
                address = employee.address,
                phone = employee.phone_no,
                email = employee.email,
                owner_id = employeeId
            )

            val shopId = createShop(shop).getOrThrow()

            usersCollection.document(userDoc.id)
                .update("shop_id", shopId)
                .await()

            Result.success(shopId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

} 