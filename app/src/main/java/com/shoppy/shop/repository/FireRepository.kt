package com.shoppy.shop.repository

import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.shoppy.shop.data.DataOrException
import com.shoppy.shop.models.MBrand
import com.shoppy.shop.models.MCategory
import com.shoppy.shop.models.MProducts
import com.shoppy.shop.models.MSliders
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FireRepository {

    class  FireRepositorySliders @Inject constructor(private val querySlider: Query) {

        suspend fun getSlidersFromFB(): DataOrException<List<MSliders>, Boolean, Exception> {

            val dataOrException = DataOrException<List<MSliders>, Boolean, Exception>()

            try {
                dataOrException.loading = true

                dataOrException.data =
                    querySlider.get().await().documents.map { documentSnapshot ->

                        documentSnapshot.toObject(MSliders::class.java)!!

                    }

                dataOrException.loading = false

            } catch (ex: FirebaseFirestoreException) {
                dataOrException.e = ex
            }

            return dataOrException

        }
    }

    class FireRepositoryBestSeller @Inject constructor(private val queryProduct: Query) {

        suspend fun getBestSellerFromFB(): DataOrException<List<MProducts>, Boolean, Exception> {

            val dataOrException = DataOrException<List<MProducts>, Boolean, Exception>()

            try {
                dataOrException.loading = true

                dataOrException.data =
                    queryProduct.get().await().documents.map { documentSnapshot ->

                        documentSnapshot.toObject(MProducts::class.java)!!

                    }

                if (!dataOrException.data.isNullOrEmpty()) dataOrException.loading = false

            } catch (ex: FirebaseFirestoreException) {
                dataOrException.e = ex
            }

            return dataOrException

        }
    }

    class  FireRepositoryMobilePhones @Inject constructor(private val queryProduct: Query) {

        suspend fun getMobilePhonesFromFB(): DataOrException<List<MProducts>, Boolean, Exception> {

            val dataOrException = DataOrException<List<MProducts>, Boolean, Exception>()

            try {
                dataOrException.loading = true

                dataOrException.data =
                    queryProduct.get().await().documents.map { documentSnapshot ->

                        documentSnapshot.toObject(MProducts::class.java)!!

                    }

                if (!dataOrException.data.isNullOrEmpty()) dataOrException.loading = false

            } catch (ex: FirebaseFirestoreException) {
                dataOrException.e = ex
            }

            return dataOrException

        }
    }

    class  FireRepositoryTv @Inject constructor(private val queryProduct: Query) {

        suspend fun getTvFromFB(): DataOrException<List<MProducts>, Boolean, Exception> {

            val dataOrException = DataOrException<List<MProducts>, Boolean, Exception>()

            try {
                dataOrException.loading = true

                dataOrException.data =
                    queryProduct.get().await().documents.map { documentSnapshot ->

                        documentSnapshot.toObject(MProducts::class.java)!!

                    }

                if (!dataOrException.data.isNullOrEmpty()) dataOrException.loading = false

            } catch (ex: FirebaseFirestoreException) {
                dataOrException.e = ex
            }

            return dataOrException

        }
    }

    class  FireRepositoryEarphones @Inject constructor(private val queryProduct: Query) {

        suspend fun getEarphonesFromFB(): DataOrException<List<MProducts>, Boolean, Exception> {

            val dataOrException = DataOrException<List<MProducts>, Boolean, Exception>()

            try {
                dataOrException.loading = true

                dataOrException.data =
                    queryProduct.get().await().documents.map { documentSnapshot ->

                        documentSnapshot.toObject(MProducts::class.java)!!

                    }

                if (!dataOrException.data.isNullOrEmpty()) dataOrException.loading = false

            } catch (ex: FirebaseFirestoreException) {
                dataOrException.e = ex
            }

            return dataOrException

        }
    }

    class  FireRepositoryBrands @Inject constructor(private val queryBrand: Query) {

        suspend fun getBrandsFromFB(): DataOrException<List<MBrand>, Boolean, Exception> {

            val dataOrException = DataOrException<List<MBrand>, Boolean, Exception>()

            try {
                dataOrException.loading = true

                dataOrException.data =
                    queryBrand.get().await().documents.map { documentSnapshot ->

                        documentSnapshot.toObject(MBrand::class.java)!!

                    }

                if (!dataOrException.data.isNullOrEmpty()) dataOrException.loading = false

            } catch (ex: FirebaseFirestoreException) {
                dataOrException.e = ex
            }

            return dataOrException

        }
    }

    class FireRepositoryCategories @Inject constructor(private val queryCategory: Query) {
        suspend fun getCategoriesFromFB(): DataOrException<List<MCategory>, Boolean, Exception> {
            val dataOrException = DataOrException<List<MCategory>, Boolean, Exception>()

            try {
                dataOrException.loading = true
                dataOrException.data = queryCategory.get().await().documents.map { documentSnapshot ->
                    documentSnapshot.toObject(MCategory::class.java)!!
                }
                if (!dataOrException.data.isNullOrEmpty()) dataOrException.loading = false
            } catch (ex: FirebaseFirestoreException) {
                dataOrException.e = ex
            }

            return dataOrException
        }
    }
}