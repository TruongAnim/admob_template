package com.truonganim.admob.utils

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Helper class for setting wallpapers
 */
object WallpaperHelper {

    /**
     * Configuration: Choose how to set wallpaper
     * - AUTO: Set wallpaper automatically without user interaction
     * - INTENT: Open system wallpaper picker for user to choose crop/position
     */
    enum class SetWallpaperMethod {
        AUTO,
        INTENT
    }

    /**
     * Default method for setting wallpaper
     * Change this to switch between AUTO and INTENT methods
     */
    var DEFAULT_METHOD: SetWallpaperMethod = SetWallpaperMethod.INTENT

    /**
     * Wallpaper types
     */
    enum class WallpaperType {
        HOME_SCREEN,
        LOCK_SCREEN,
        BOTH
    }
    
    /**
     * Set wallpaper from URL using the configured method
     */
    suspend fun setWallpaperFromUrl(
        context: Context,
        imageUrl: String,
        wallpaperType: WallpaperType = WallpaperType.BOTH,
        method: SetWallpaperMethod = DEFAULT_METHOD
    ): Boolean = withContext(Dispatchers.IO) {
        return@withContext when (method) {
            SetWallpaperMethod.AUTO -> setWallpaperAuto(context, imageUrl, wallpaperType)
            SetWallpaperMethod.INTENT -> setWallpaperWithIntent(context, imageUrl)
        }
    }

    /**
     * Set wallpaper automatically without user interaction
     */
    private suspend fun setWallpaperAuto(
        context: Context,
        imageUrl: String,
        wallpaperType: WallpaperType = WallpaperType.BOTH
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Download image
            val bitmap = downloadImage(context, imageUrl) ?: return@withContext false
            
            // Set wallpaper
            val wallpaperManager = WallpaperManager.getInstance(context)
            
            when (wallpaperType) {
                WallpaperType.HOME_SCREEN -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(
                            bitmap,
                            null,
                            true,
                            WallpaperManager.FLAG_SYSTEM
                        )
                    } else {
                        wallpaperManager.setBitmap(bitmap)
                    }
                    showSuccessMessage(context, "Home screen wallpaper set successfully")
                }
                
                WallpaperType.LOCK_SCREEN -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(
                            bitmap,
                            null,
                            true,
                            WallpaperManager.FLAG_LOCK
                        )
                        showSuccessMessage(context, "Lock screen wallpaper set successfully")
                    } else {
                        // Lock screen wallpaper not supported on older versions
                        wallpaperManager.setBitmap(bitmap)
                        showSuccessMessage(context, "Wallpaper set successfully")
                    }
                }
                
                WallpaperType.BOTH -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(
                            bitmap,
                            null,
                            true,
                            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                        )
                        showSuccessMessage(context, "Wallpaper set for both screens")
                    } else {
                        wallpaperManager.setBitmap(bitmap)
                        showSuccessMessage(context, "Wallpaper set successfully")
                    }
                }
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Failed to set wallpaper: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            false
        }
    }
    
    /**
     * Set wallpaper using system Intent (opens wallpaper picker)
     * This allows user to crop and position the wallpaper
     */
    private suspend fun setWallpaperWithIntent(
        context: Context,
        imageUrl: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Download image
            val bitmap = downloadImage(context, imageUrl) ?: return@withContext false

            // Save to cache directory
            val cachePath = File(context.cacheDir, "wallpapers")
            cachePath.mkdirs()
            val file = File(cachePath, "wallpaper_${System.currentTimeMillis()}.jpg")

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            // Get URI using FileProvider
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            // Create wallpaper intent
            val intent = Intent(Intent.ACTION_ATTACH_DATA).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                setDataAndType(contentUri, "image/*")
                putExtra("mimeType", "image/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            withContext(Dispatchers.Main) {
                try {
                    context.startActivity(Intent.createChooser(intent, "Set as wallpaper"))
                } catch (e: Exception) {
                    // If ACTION_ATTACH_DATA doesn't work, try WallpaperManager intent
                    val wallpaperIntent = Intent(WallpaperManager.ACTION_CROP_AND_SET_WALLPAPER).apply {
                        setDataAndType(contentUri, "image/*")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(wallpaperIntent)
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Failed to open wallpaper picker: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            false
        }
    }
    
    /**
     * Download image from URL
     */
    private suspend fun downloadImage(context: Context, imageUrl: String): Bitmap? {
        return try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
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
     * Show success message
     */
    private suspend fun showSuccessMessage(context: Context, message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Check if device supports separate lock screen wallpaper
     */
    fun supportsLockScreenWallpaper(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    }
}

