package com.shoppy.shop.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.network.NotificationApi
//import com.shoppy.shop.repository.CouponRepository
import com.shoppy.shop.repository.FireAttendanceRepository
import com.shoppy.shop.repository.FireCartRepository
import com.shoppy.shop.repository.FireOrderRepository
import com.shoppy.shop.repository.FireOrderStatusRepository
import com.shoppy.shop.repository.FireRepository
import com.shoppy.shop.repository.FireSearchRepository
import com.shoppy.shop.repository.NotificationRepository
import com.shoppy.shop.repository.RatingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Singleton
    @Provides
    fun providesFireRepositoryBrand()
    = FireRepository.FireRepositoryBrands(queryBrand = FirebaseFirestore.getInstance().collection("Brands"))

    @Singleton
    @Provides
    fun providesFireRepositorySlider()
    = FireRepository.FireRepositorySliders(querySlider = FirebaseFirestore.getInstance().collection("Sliders"))

    @Singleton
    @Provides
    fun providesFireRepositoryBestSeller()
    = FireRepository.FireRepositoryBestSeller(queryProduct = FirebaseFirestore.getInstance().collection("BestSeller"))

    @Singleton
    @Provides
    fun providesFireRepositoryMobilePhones()
    = FireRepository.FireRepositoryMobilePhones(queryProduct = FirebaseFirestore.getInstance().collection("MobilePhones"))

    @Singleton
    @Provides
    fun providesFireRepositoryTv()
    = FireRepository.FireRepositoryTv(queryProduct = FirebaseFirestore.getInstance().collection("Tv"))

    @Singleton
    @Provides
    fun providesFireRepositoryEarphones()
    = FireRepository.FireRepositoryEarphones(queryProduct = FirebaseFirestore.getInstance().collection("Earphones"))

    @Singleton
    @Provides
    fun providesFireRepositoryCategories()
    = FireRepository.FireRepositoryCategories(queryCategory = FirebaseFirestore.getInstance().collection("Categories"))

    @Singleton
    @Provides
    fun providesGetCartFromFireBase()
    = FireCartRepository(
        queryCart = FirebaseFirestore.getInstance().collection("Cart")
            //sorting cart to display newest items first
        .orderBy("timestamp",Query.Direction.DESCENDING)
    )

    @Singleton
    @Provides
    fun providesGetOrdersFromFirebase()
    = FireOrderRepository(queryOrder = FirebaseFirestore.getInstance().collection("Orders")
        //sorting cart to display newest items first
        .orderBy("timestamp",Query.Direction.DESCENDING))

    @Singleton
    @Provides
    fun providesGetSearchResultFromFirebase()
    = FireSearchRepository(querySearch = FirebaseFirestore.getInstance().collection("AllProducts"))

    @Singleton
    @Provides
    fun providesGetOrderStatusFromFirebase()
    = FireOrderStatusRepository(queryStatus = FirebaseFirestore.getInstance().collection("Orders"))

    @Singleton
    @Provides
    fun providesGetEmployeeAttendanceFromFB()
    = FireAttendanceRepository(queryAttendance = FirebaseFirestore.getInstance().collection("Attendance"))

    @Singleton
    @Provides
    fun providesRatingRepository(): RatingRepository {
        return RatingRepository(FirebaseFirestore.getInstance())
    }

    @Singleton
    @Provides
    fun providesNotificationRepository(firestore: FirebaseFirestore, auth: FirebaseAuth): NotificationRepository {
        return NotificationRepository(firestore, auth)
    }
    
//    @Singleton
//    @Provides
//    fun providesCouponRepository(firestore: FirebaseFirestore, auth: FirebaseAuth): CouponRepository {
//        return CouponRepository(firestore, auth)
//    }

    //Notification API
    @Singleton
    @Provides
    fun providesPostNotification(): NotificationApi {
        return Retrofit.Builder()
            .baseUrl(ShopKartUtils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NotificationApi::class.java)
    }

    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    
    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}
