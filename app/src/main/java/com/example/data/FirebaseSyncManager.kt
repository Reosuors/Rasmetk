package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class FirebaseDrawing(
    val drawingIdString: String,
    val title: String,
    val description: String,
    val authorName: String,
    val imageBase64: String,
    val timestamp: Long,
    val isPublic: Boolean = true,
    val likesCount: Int = 0
)

object FirebaseSyncManager {
    private const val TAG = "FirebaseSyncManager"
    private const val BASE_URL = "https://rasmatak-bf4a5-default-rtdb.firebaseio.com"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    // Sanitizes emails for Firebase paths (replaces invalid path characters like '.', '$', '[', ']', '#', '/')
    private fun sanitizeKey(key: String): String {
        return key.trim()
            .lowercase()
            .replace(".", "_dot_")
            .replace("@", "_at_")
            .replace("#", "_hash_")
            .replace("$", "_dollar_")
            .replace("[", "_ob_")
            .replace("]", "_cb_")
            .replace("/", "_slash_")
    }

    /**
     * Encodes a bitmap to base64 string
     */
    fun encodeBitmapToBase64(bitmap: Bitmap): String {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
        val bytes = out.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    /**
     * Decodes a base64 string to bitmap
     */
    fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str.trim(), Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "decodeBase64ToBitmap: Error", e)
            null
        }
    }

    /**
     * Firebase Auth Signup with Email, Username, Password.
     */
    suspend fun signUpWithFirebase(email: String, username: String, pass: String): Result<String> = withContext(Dispatchers.IO) {
        if (email.isBlank() || username.isBlank() || pass.length < 5) {
            return@withContext Result.failure(Exception("الرجاء إدخال بيانات صحيحة (كلمة المرور يجب أن لا تقل عن 5 أحرف)"))
        }

        val safeEmail = sanitizeKey(email)
        val url = "$BASE_URL/users_auth/$safeEmail.json"

        try {
            // First check if user already exists
            val checkRequest = Request.Builder().url(url).get().build()
            client.newCall(checkRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    if (responseBody != "null" && responseBody.isNotBlank()) {
                        return@withContext Result.failure(Exception("هذا الحساب مسجّل بالفعل!"))
                    }
                }
            }

            // Create account
            val userJson = JSONObject().apply {
                put("email", email.trim())
                put("username", username.trim())
                put("password", pass) // In products this would be encrypted, keeping simple for seamless prototyping
            }

            val body = userJson.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val putRequest = Request.Builder().url(url).put(body).build()
            
            client.newCall(putRequest).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(username)
                } else {
                    Result.failure(Exception("فشل الاتصال بخادم Firebase: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "signUpWithFirebase error", e)
            Result.failure(Exception("خطأ في الاتصال بالشبكة: ${e.message}"))
        }
    }

    /**
     * Firebase Auth Login with Email & Password.
     */
    suspend fun loginWithFirebase(email: String, pass: String): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        val safeEmail = sanitizeKey(email)
        val url = "$BASE_URL/users_auth/$safeEmail.json"

        try {
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    if (responseBody == "null" || responseBody.isBlank()) {
                        return@withContext Result.failure(Exception("عذراً، هذا الحساب غير موجود!"))
                    }

                    val userObj = JSONObject(responseBody)
                    val dbPassword = userObj.optString("password", "")
                    val dbUsername = userObj.optString("username", "رسام مبدع")

                    if (dbPassword == pass) {
                        Result.success(Pair(dbUsername, email.trim()))
                    } else {
                        Result.failure(Exception("كلمة المرور غير صحيحة، يرجى المحاولة ثانية."))
                    }
                } else {
                    Result.failure(Exception("خطأ في الاتصال بقاعدة بيانات Firebase: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("فشل تسجيل الدخول. خطأ بالاتصال: ${e.message}"))
        }
    }

    /**
     * Saves a painting to the current logged-in user's private gallery node in Firebase.
     */
    suspend fun saveToPrivateCloudGallery(email: String, drawing: FirebaseDrawing): Result<Boolean> = withContext(Dispatchers.IO) {
        val safeEmail = sanitizeKey(email)
        val safeId = sanitizeKey(drawing.drawingIdString)
        val url = "$BASE_URL/users_drawings/$safeEmail/$safeId.json"

        try {
            val drawingJson = JSONObject().apply {
                put("drawingIdString", drawing.drawingIdString)
                put("title", drawing.title)
                put("description", drawing.description)
                put("authorName", drawing.authorName)
                put("imageBase64", drawing.imageBase64)
                put("timestamp", drawing.timestamp)
                put("isPublic", false)
                put("likesCount", drawing.likesCount)
            }

            val body = drawingJson.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val putRequest = Request.Builder().url(url).put(body).build()

            client.newCall(putRequest).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("فشل الرفع للمعرض الخاص: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("عطل في الاتصال: ${e.message}"))
        }
    }

    /**
     * Publishes a drawing publicly for sharing (under central node share code).
     */
    suspend fun publishPublicShare(drawing: FirebaseDrawing): Result<String> = withContext(Dispatchers.IO) {
        val safeId = sanitizeKey(drawing.drawingIdString)
        val url = "$BASE_URL/public_shares/$safeId.json"

        try {
            val drawingJson = JSONObject().apply {
                put("drawingIdString", drawing.drawingIdString)
                put("title", drawing.title)
                put("description", drawing.description)
                put("authorName", drawing.authorName)
                put("imageBase64", drawing.imageBase64)
                put("timestamp", drawing.timestamp)
                put("isPublic", true)
                put("likesCount", drawing.likesCount)
            }

            val body = drawingJson.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val putRequest = Request.Builder().url(url).put(body).build()

            client.newCall(putRequest).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(drawing.drawingIdString)
                } else {
                    Result.failure(Exception("فشل نشر كود المشاركة: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("عطل بالاتصال: ${e.message}"))
        }
    }

    /**
     * Fetches a shared drawing by its code.
     */
    suspend fun fetchSharedDrawingByCode(code: String): Result<FirebaseDrawing> = withContext(Dispatchers.IO) {
        val safeId = sanitizeKey(code)
        val url = "$BASE_URL/public_shares/$safeId.json"

        try {
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: ""
                    if (bodyStr == "null" || bodyStr.isBlank()) {
                        // Fallback: search in users private drawings just in case, but usually must be public
                        return@withContext Result.failure(Exception("عذراً، لم يتم العثور على لوحة مطابقة لهذا الرمز!"))
                    }

                    val obj = JSONObject(bodyStr)
                    val firebaseDrawing = FirebaseDrawing(
                        drawingIdString = obj.optString("drawingIdString", code),
                        title = obj.optString("title", "رسمة shared"),
                        description = obj.optString("description", ""),
                        authorName = obj.optString("authorName", "رسام مجهول"),
                        imageBase64 = obj.optString("imageBase64", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        isPublic = obj.optBoolean("isPublic", true),
                        likesCount = obj.optInt("likesCount", 0)
                    )
                    Result.success(firebaseDrawing)
                } else {
                    Result.failure(Exception("فشل في استرداد لوحة المشاركة: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطأ في الاتصال بالانترنت: ${e.message}"))
        }
    }

    /**
     * Fetches all drawings from the current user's private gallery.
     */
    suspend fun fetchPrivateDrawings(email: String): Result<List<FirebaseDrawing>> = withContext(Dispatchers.IO) {
        val safeEmail = sanitizeKey(email)
        val url = "$BASE_URL/users_drawings/$safeEmail.json"

        try {
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    if (responseBody == "null" || responseBody.isBlank()) {
                        return@withContext Result.success(emptyList())
                    }

                    val drawingsList = mutableListOf<FirebaseDrawing>()
                    val rootObj = JSONObject(responseBody)
                    val keys = rootObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val obj = rootObj.getJSONObject(key)
                        drawingsList.add(
                            FirebaseDrawing(
                                drawingIdString = obj.optString("drawingIdString", ""),
                                title = obj.optString("title", ""),
                                description = obj.optString("description", ""),
                                authorName = obj.optString("authorName", ""),
                                imageBase64 = obj.optString("imageBase64", ""),
                                timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                                isPublic = false,
                                likesCount = obj.optInt("likesCount", 0)
                            )
                        )
                    }
                    Result.success(drawingsList)
                } else {
                    Result.failure(Exception("فشل قراءة المعرض السحابي: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطأ في قراءة المعرض السحابي: ${e.message}"))
        }
    }
}
