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
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

data class SupabaseDrawing(
    val drawingIdString: String,
    val title: String,
    val description: String,
    val authorName: String,
    val imageBase64: String,
    val timestamp: Long,
    val isPublic: Boolean = true,
    val likesCount: Int = 0
)

object SupabaseSyncManager {
    private const val TAG = "SupabaseSyncManager"
    
    private var baseUrl: String = "https://jjanuivrwsqvfqzvrwmu.supabase.co"
    private var anonKey: String = "sb_publishable_yjyu8Q0o_TSRM6rDOZ8BTw_MfgGHyG3"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    /**
     * Dynamically initialize Supabase with custom credentials
     */
    fun initialize(context: Context, url: String, key: String) {
        baseUrl = url.trim().removeSuffix("/")
        anonKey = key.trim()
        Log.i(TAG, "Initialized Supabase with BaseURL: $baseUrl")
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
     * Supabase GoTrue Auth Sign Up.
     */
    suspend fun signUpWithSupabase(email: String, username: String, pass: String): Result<String> = withContext(Dispatchers.IO) {
        if (email.isBlank() || username.isBlank() || pass.length < 6) {
            return@withContext Result.failure(Exception("الرجاء إدخال بيانات صحيحة (كلمة المرور يجب أن لا تقل عن 6 أحرف في Supabase)"))
        }

        val url = "$baseUrl/auth/v1/signup"
        try {
            val userMetadata = JSONObject().apply {
                put("username", username.trim())
            }
            val requestJson = JSONObject().apply {
                put("email", email.trim())
                put("password", pass)
                put("data", userMetadata)
            }

            val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(url)
                .header("apikey", anonKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Result.success(username)
                } else {
                    val errMsg = try {
                        JSONObject(bodyStr).optString("msg", response.message)
                    } catch (e: Exception) {
                        bodyStr.ifBlank { "رمز الخطأ: ${response.code}" }
                    }
                    Result.failure(Exception("فشل التسجيل في Supabase: $errMsg"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "signUpWithSupabase error", e)
            Result.failure(Exception("خطأ في الاتصال بالشبكة: ${e.message}"))
        }
    }

    /**
     * Supabase GoTrue Auth Login.
     */
    suspend fun loginWithSupabase(email: String, pass: String): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        val url = "$baseUrl/auth/v1/token?grant_type=password"
        try {
            val requestJson = JSONObject().apply {
                put("email", email.trim())
                put("password", pass)
            }

            val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(url)
                .header("apikey", anonKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val rootObj = JSONObject(bodyStr)
                    val userObj = rootObj.optJSONObject("user")
                    val userMetadata = userObj?.optJSONObject("user_metadata")
                    val dbUsername = userMetadata?.optString("username", "رسام سحابي") ?: "رسام سحابي"
                    val dbEmail = userObj?.optString("email", email) ?: email
                    Result.success(Pair(dbUsername, dbEmail))
                } else {
                    val errMsg = try {
                        JSONObject(bodyStr).optString("error_description", JSONObject(bodyStr).optString("msg", response.message))
                    } catch (e: Exception) {
                        bodyStr.ifBlank { "رمز الخطأ: ${response.code}" }
                    }
                    Result.failure(Exception("فشل تسجيل الدخول: $errMsg"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "loginWithSupabase error", e)
            Result.failure(Exception("فشل تسجيل الدخول. خطأ بالاتصال: ${e.message}"))
        }
    }

    /**
     * Saves or updates a painting to the database table 'drawings' in Supabase.
     */
    suspend fun saveToPrivateCloudGallery(email: String, drawing: SupabaseDrawing): Result<Boolean> = withContext(Dispatchers.IO) {
        val url = "$baseUrl/rest/v1/drawings"
        try {
            val drawingJson = JSONObject().apply {
                put("drawing_id", drawing.drawingIdString)
                put("title", drawing.title)
                put("description", drawing.description)
                put("author_name", drawing.authorName)
                put("image_base64", drawing.imageBase64)
                put("timestamp", drawing.timestamp)
                put("is_public", false)
                put("likes_count", drawing.likesCount)
                put("email", email.trim().lowercase())
            }

            val body = drawingJson.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(url)
                .header("apikey", anonKey)
                .header("Authorization", "Bearer $anonKey")
                .header("Content-Type", "application/json")
                .header("Prefer", "resolution=merge-duplicates")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    val hint = if (response.code == 404 || bodyStr.contains("relation")) {
                        "\n(ملاحظة: تأكد من إنشاء جدول 'drawings' في Supabase)"
                    } else ""
                    Result.failure(Exception("فشل الحفظ في السحاب: ${response.code} $bodyStr$hint"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "saveToPrivateCloudGallery error", e)
            Result.failure(Exception("عطل في الاتصال: ${e.message}"))
        }
    }

    /**
     * Publishes a drawing publicly for sharing (under table 'drawings' with is_public = true).
     */
    suspend fun publishPublicShare(drawing: SupabaseDrawing): Result<String> = withContext(Dispatchers.IO) {
        val url = "$baseUrl/rest/v1/drawings"
        try {
            val drawingJson = JSONObject().apply {
                put("drawing_id", drawing.drawingIdString)
                put("title", drawing.title)
                put("description", drawing.description)
                put("author_name", drawing.authorName)
                put("image_base64", drawing.imageBase64)
                put("timestamp", drawing.timestamp)
                put("is_public", true)
                put("likes_count", drawing.likesCount)
            }

            val body = drawingJson.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(url)
                .header("apikey", anonKey)
                .header("Authorization", "Bearer $anonKey")
                .header("Content-Type", "application/json")
                .header("Prefer", "resolution=merge-duplicates")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Result.success(drawing.drawingIdString)
                } else {
                    val hint = if (response.code == 404 || bodyStr.contains("relation")) {
                        "\n(ملاحظة: تأكد من إنشاء جدول 'drawings' في Supabase)"
                    } else ""
                    Result.failure(Exception("فشل النشر والمشاركة: ${response.code} $bodyStr$hint"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "publishPublicShare error", e)
            Result.failure(Exception("عطل بالاتصال: ${e.message}"))
        }
    }

    /**
     * Fetches a shared drawing by its code.
     */
    suspend fun fetchSharedDrawingByCode(code: String): Result<SupabaseDrawing> = withContext(Dispatchers.IO) {
        val url = "$baseUrl/rest/v1/drawings?drawing_id=eq.${code.trim()}&is_public=eq.true&select=*"
        try {
            val request = Request.Builder()
                .url(url)
                .header("apikey", anonKey)
                .header("Authorization", "Bearer $anonKey")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val arr = JSONArray(bodyStr)
                    if (arr.length() == 0) {
                        return@withContext Result.failure(Exception("عذراً، لم يتم العثور على لوحة مطابقة لهذا الرمز!"))
                    }

                    val obj = arr.getJSONObject(0)
                    val supabaseDrawing = SupabaseDrawing(
                        drawingIdString = obj.optString("drawing_id", code),
                        title = obj.optString("title", "رسمة shared"),
                        description = obj.optString("description", ""),
                        authorName = obj.optString("author_name", "رسام مجهول"),
                        imageBase64 = obj.optString("image_base64", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        isPublic = obj.optBoolean("is_public", true),
                        likesCount = obj.optInt("likes_count", 0)
                    )
                    Result.success(supabaseDrawing)
                } else {
                    Result.failure(Exception("فشل استيراد رسمة المشاركة: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchSharedDrawingByCode error", e)
            Result.failure(Exception("خطأ في الاتصال بالانترنت: ${e.message}"))
        }
    }

    /**
     * Fetches all drawings from the current user's profile in Supabase table.
     */
    suspend fun fetchPrivateDrawings(email: String): Result<List<SupabaseDrawing>> = withContext(Dispatchers.IO) {
        val url = "$baseUrl/rest/v1/drawings?email=eq.${email.trim().lowercase()}&select=*"
        try {
            val request = Request.Builder()
                .url(url)
                .header("apikey", anonKey)
                .header("Authorization", "Bearer $anonKey")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val drawingsList = mutableListOf<SupabaseDrawing>()
                    val arr = JSONArray(bodyStr)
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        drawingsList.add(
                            SupabaseDrawing(
                                drawingIdString = obj.optString("drawing_id", ""),
                                title = obj.optString("title", ""),
                                description = obj.optString("description", ""),
                                authorName = obj.optString("author_name", ""),
                                imageBase64 = obj.optString("image_base64", ""),
                                timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                                isPublic = obj.optBoolean("is_public", false),
                                likesCount = obj.optInt("likes_count", 0)
                            )
                        )
                    }
                    Result.success(drawingsList)
                } else {
                    val hint = if (response.code == 404 || bodyStr.contains("relation")) {
                        "\n(ملاحظة: تأكد من إنشاء جدول 'drawings' في Supabase)"
                    } else ""
                    Result.failure(Exception("فشل قراءة المعرض: ${response.code}$hint"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchPrivateDrawings error", e)
            Result.failure(Exception("خطأ في قراءة المعرض السحابي: ${e.message}"))
        }
    }
}
