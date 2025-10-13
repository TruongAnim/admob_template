package com.truonganim.admob.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Image utility functions for downloading, saving, and sharing images
 */
object ImageUtils {
    
    /**
     * Download image from URL and save to gallery
     */
    suspend fun saveImageToGallery(
        context: Context,
        imageUrl: String,
        fileName: String = "IMG_${System.currentTimeMillis()}.jpg"
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Download image using Coil
            val bitmap = downloadImage(context, imageUrl) ?: return@withContext false
            
            // Save to gallery
            val saved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveImageToGalleryQ(context, bitmap, fileName)
            } else {
                saveImageToGalleryLegacy(context, bitmap, fileName)
            }
            
            if (saved) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
                }
            }
            
            saved
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            false
        }
    }
    
    /**
     * Download image from URL and share
     */
    suspend fun shareImage(
        context: Context,
        imageUrl: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Download image using Coil
            val bitmap = downloadImage(context, imageUrl) ?: return@withContext false
            
            // Save to cache directory
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "share_image_${System.currentTimeMillis()}.jpg")
            
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            
            // Get URI using FileProvider
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            // Create share intent
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, contentUri)
                type = "image/jpeg"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            withContext(Dispatchers.Main) {
                context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to share image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            false
        }
    }
    
    /**
     * Download image from URL using Coil
     */
    private suspend fun downloadImage(context: Context, imageUrl: String): Bitmap? {
        return try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false) // Disable hardware bitmaps for saving
                .build()
            
            val result = loader.execute(request)
            if (result is SuccessResult) {
                (result.drawable as? BitmapDrawable)?.bitmap
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Save image to gallery for Android Q and above
     */
    private fun saveImageToGalleryQ(
        context: Context,
        bitmap: Bitmap,
        fileName: String
    ): Boolean {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        
        return uri?.let {
            resolver.openOutputStream(it)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            true
        } ?: false
    }
    
    /**
     * Save image to gallery for Android P and below
     */
    @Suppress("DEPRECATION")
    private fun saveImageToGalleryLegacy(
        context: Context,
        bitmap: Bitmap,
        fileName: String
    ): Boolean {
        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image = File(imagesDir, fileName)
        
        return try {
            FileOutputStream(image).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            
            // Notify gallery
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DATA, image.absolutePath)
            }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

