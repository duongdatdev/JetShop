package com.shoppy.shop.utils

import android.content.Context
import android.net.Uri
import com.shoppy.shop.BuildConfig
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImgBBUtils {
    private const val IMGBB_API_KEY = "fdadc7ab7894870105d54aaab2240ed2"
    private const val IMGBB_BASE_URL = "https://api.imgbb.com/1/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(IMGBB_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val imgBBService = retrofit.create(ImgBBService::class.java)

//    suspend fun uploadImage(context: Context, imageUri: Uri): String? {
//        try {
//            val file = createTempFileFromUri(context, imageUri)
//            val imageRequestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
//            val keyRequestBody = IMGBB_API_KEY.toRequestBody("text/plain".toMediaTypeOrNull())
//
//            val response = imgBBService.uploadImage(
//                key = keyRequestBody,
//                image = MultipartBody.Part.createFormData("image", file.name, imageRequestBody)
//            )
//            return response.data?.url
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return null
//        }
//    }

    private fun createTempFileFromUri(context: Context, uri: Uri): File {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File.createTempFile("img_", ".jpg", context.cacheDir)
        FileOutputStream(file).use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
        return file
    }
}

interface ImgBBService {
    @retrofit2.http.Multipart
    @retrofit2.http.POST("upload")
    suspend fun uploadImage(
        @retrofit2.http.Part("key") key: okhttp3.RequestBody,
        @retrofit2.http.Part image: MultipartBody.Part
    ): ImgBBResponse
}

data class ImgBBResponse(
    val data: ImgBBData? = null,
    val success: Boolean = false,
    val status: Int = 0
)

data class ImgBBData(
    val url: String? = null
) 