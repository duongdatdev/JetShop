package com.shoppy.shop.repository

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
    private val shopsCollection = firestore.collection("shops")
    private val usersCollection = firestore.collection("users")

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

    suspend fun getShopById(shopId: String): Result<MShop> {
        return try {
            val document = shopsCollection.document(shopId).get().await()
            if (document.exists()) {
                val shop = document.toObject(MShop::class.java)
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
            // First get the employee to get their shop_id
            val employeeDoc = usersCollection.document(employeeId).get().await()
            if (!employeeDoc.exists()) {
                return Result.failure(Exception("Employee not found"))
            }
            
            val employee = employeeDoc.toObject(MUser::class.java)
            if (employee?.shop_id == null) {
                return Result.failure(Exception("Employee not assigned to any shop"))
            }
            
            // Then get the shop using the shop_id
            val shopDoc = shopsCollection.document(employee.shop_id!!).get().await()
            if (!shopDoc.exists()) {
                return Result.failure(Exception("Shop not found"))
            }
            
            val shop = shopDoc.toObject(MShop::class.java)
            Result.success(shop!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateShopForEmployee(employeeId: String, shop: MShop): Result<Unit> {
        return try {
            // First verify the employee exists and has access to this shop
            val employeeDoc = usersCollection.document(employeeId).get().await()
            if (!employeeDoc.exists()) {
                return Result.failure(Exception("Employee not found"))
            }
            
            val employee = employeeDoc.toObject(MUser::class.java)
            if (employee?.shop_id != shop.id) {
                return Result.failure(Exception("Employee not authorized to update this shop"))
            }
            
            // Update the shop
            shop.updated_at = System.currentTimeMillis()
            shopsCollection.document(shop.id!!)
                .update(shop.convertToMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun initializeShopFromEmployee(employeeId: String): Result<String> {
        return try {
            // Get employee information
            val employeeDoc = usersCollection.document(employeeId).get().await()
            if (!employeeDoc.exists()) {
                return Result.failure(Exception("Employee not found"))
            }
            
            val employee = employeeDoc.toObject(MUser::class.java)
            if (employee == null) {
                return Result.failure(Exception("Failed to parse employee data"))
            }
            
            // Create new shop with employee's information
            val shop = MShop(
                name = "${employee.name}'s Store",
                address = employee.address,
                phone = employee.phone_no,
                email = employee.email,
                owner_id = employeeId
            )
            
            // Create the shop
            val shopId = createShop(shop).getOrThrow()
            
            // Update employee with shop_id
            usersCollection.document(employeeId)
                .update("shop_id", shopId)
                .await()
            
            Result.success(shopId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 