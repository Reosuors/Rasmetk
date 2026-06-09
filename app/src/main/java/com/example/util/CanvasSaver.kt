package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object CanvasSaver {

    /**
     * Saves a Bitmap to the public pictures gallery.
     * Returns the Uri of the saved image, or null if it fails.
     */
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, title: String): Uri? {
        val filename = "Rasmatak_${System.currentTimeMillis()}.png"
        var fos: OutputStream? = null
        var imageUri: Uri? = null

        try {
            val resolver = context.contentResolver
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/رسمتك")
                }
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    fos = resolver.openOutputStream(imageUri)
                }
            } else {
                // Older Android: Save directly into Pictures directory
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(imagesDir, "رسمتك")
                if (!appDir.exists()) appDir.mkdirs()
                val imageFile = File(appDir, filename)
                fos = FileOutputStream(imageFile)
                
                // Add to scanner so it appears in gallery
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                }
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }

            if (fos != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
                return imageUri
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    /**
     * Saves a Bitmap to app's cache directory and returns a secure FileProvider Uri.
     * Useful for direct instant sharing.
     */
    fun getShareableUriFromBitmap(context: Context, bitmap: Bitmap, title: String): Uri? {
        try {
            val cachePath = File(context.cacheDir, "shared_drawings")
            if (!cachePath.exists()) cachePath.mkdirs()
            val file = File(cachePath, "resmatak_share_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            // Returns FileProvider URI (Authority should match AndroidManifest)
            val authority = "${context.packageName}.fileprovider"
            return FileProvider.getUriForFile(context, authority, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Triggers the native Android Sharesheet with the given image URI.
     */
    fun shareDrawing(context: Context, imageUri: Uri, title: String, idString: String?) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, imageUri)
            
            val shareText = if (!idString.isNullOrEmpty()) {
                "شاهد رسمتي الرائعة '${title}' على تطبيق رسمتك! يمكنك البحث عنها باستخدام المعرّف (ID): ${idString}"
            } else {
                "شاهد رسمتي الرائعة '${title}' على تطبيق رسمتك!"
            }
            
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "image/png"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "مشاركة الرسمة عبر"))
    }
}
