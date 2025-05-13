package com.shoppy.shop.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.shoppy.shop.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import kotlin.coroutines.resume

object CloudinaryUtils {
    private var isInitialized = false
    private const val TAG = "CloudinaryUtils"

    // Setup Cloudinary configuration
    fun init(context: Context) {
        if (!isInitialized) {
            try {
                val config = mapOf(
                    "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
                    "api_key" to BuildConfig.CLOUDINARY_API_KEY,
                    "api_secret" to BuildConfig.CLOUDINARY_API_SECRET,
                    "secure" to true
                )
                MediaManager.init(context, config)
                isInitialized = true
                Log.d(TAG, "Cloudinary initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Cloudinary: ${e.message}", e)
            }
        }
    }

    suspend fun uploadImage(context: Context, imageUri: Uri): String? {
        return try {
            if (!isInitialized) init(context)

            val file = createTempFileFromUri(context, imageUri)
            if (file == null) {
                Log.e(TAG, "Failed to create temporary file from URI")
                return null
            }

            Log.d(TAG, "Starting Cloudinary upload for file: ${file.path}")

            suspendCancellableCoroutine<String?> { continuation ->
                val requestId = MediaManager.get().upload(file.path)
                    .unsigned("shop_unsigned_upload")
                    .option("public_id", "shop_app/${UUID.randomUUID()}")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            Log.d(TAG, "Upload started for request: $requestId")
                        }

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            Log.d(TAG, "Upload progress: $bytes/$totalBytes")
                        }

                        override fun onSuccess(requestId: String, resultData: Map<*, *>?) {
                            val url = resultData?.get("secure_url") as? String
                            Log.d(TAG, "Upload succeeded. URL: $url")
                            CoroutineScope(Dispatchers.IO).launch {
                                withContext(NonCancellable) {
                                    if (!continuation.isCompleted) {
                                        continuation.resume(url)
                                    }
                                }
                            }
                        }

                        override fun onError(requestId: String, error: ErrorInfo?) {
                            Log.e(TAG, "Upload error: ${error?.description}")
                            if (!continuation.isCompleted) {
                                continuation.resume(null)
                            }
                        }

                        override fun onReschedule(requestId: String, error: ErrorInfo?) {
                            Log.d(TAG, "Upload rescheduled: ${error?.description}")
                            // Don't resume here, let it retry
                        }
                    })
                    .dispatch()

                // Only cancel the actual request, not the continuation
                continuation.invokeOnCancellation {
                    MediaManager.get().cancelRequest(requestId)
                    Log.d(TAG, "Upload cancelled for request: $requestId")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during upload: ${e.message}", e)
            null
        }
    }

    private fun createTempFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream from URI")
                return null
            }

            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val tempFile = File(context.cacheDir, fileName)

            FileOutputStream(tempFile).use { outputStream ->
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                outputStream.flush()
            }
            inputStream.close()

            Log.d(TAG, "Created temp file: ${tempFile.path}")
            return tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Error creating temp file: ${e.message}", e)
            null
        }
    }
}