package com.shoppy.shop.network

import com.shoppy.shop.ShopKartUtils
import com.shoppy.shop.models.PushNotificationData
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationApi {

//    @Singleton
    @Headers("Authorization: key = ${ShopKartUtils.SERVER_KEY}", "Content-Type: ${ShopKartUtils.CONTENT_TYPE}")
    @POST("fcm/send")
    suspend fun postNotification( @Body notification: PushNotificationData): Response<ResponseBody>
}