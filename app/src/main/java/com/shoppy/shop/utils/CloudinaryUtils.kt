package com.shoppy.shop.utils

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.shoppy.shop.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import kotlin.coroutines.resume

object CloudinaryUtils {
    // Flag to track if MediaManager is initialized
    private var isInitialized = false

    // Setup Cloudinary configuration
    fun init(context: Context) {
        if (!isInitialized) {
            val config = mapOf(
                "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
                "api_key" to BuildConfig.CLOUDINARY_API_KEY,
                "api_secret" to BuildConfig.CLOUDINARY_API_SECRET,
                "secure" to true
            )
            MediaManager.init(context, config)
            isInitialized = true
        }
    }

    suspend fun uploadImage(context: Context, imageUri: Uri): String? {
        return try {
            if (!isInitialized) init(context)

            val file = createTempFileFromUri(context, imageUri)
            val requestId = UUID.randomUUID().toString()

            suspendCancellableCoroutine<String?> { continuation ->
                MediaManager.get().upload(file.path)
                    .unsigned("shop_unsigned_upload") // Replace with your actual preset name
                    .option("public_id", "shop_app/${UUID.randomUUID()}")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {}

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                        override fun onSuccess(requestId: String, resultData: Map<*, *>?) {
                            val url = resultData?.get("secure_url") as? String
                            continuation.resume(url)
                        }

                        override fun onError(requestId: String, error: ErrorInfo?) {
                            continuation.resume(null)
                        }

                        override fun onReschedule(requestId: String, error: ErrorInfo?) {
                            continuation.resume(null)
                        }
                    })
                    .dispatch()

                continuation.invokeOnCancellation {
                    MediaManager.get().cancelRequest(requestId)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun createTempFileFromUri(context: Context, uri: Uri): File {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File.createTempFile("img_", ".jpg", context.cacheDir)
        FileOutputStream(file).use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
        return file
    }
}