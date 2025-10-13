package com.truonganim.admob.utils

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.widget.Toast
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Helper class for setting wallpapers
 */
object WallpaperHelper {
    
    /**
     * Wallpaper types
     */
    enum class WallpaperType {
        HOME_SCREEN,
        LOCK_SCREEN,
        BOTH
    }
    
    /**
     * Set wallpaper from URL
     */
    suspend fun setWallpaperFromUrl(
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
     * Show wallpaper type selection dialog and set wallpaper
     */
    suspend fun setWallpaperWithDialog(
        context: Context,
        imageUrl: String,
        onTypeSelected: (WallpaperType) -> Unit
    ) {
        // This will be called from the UI to show a dialog
        // For now, we'll just set both
        setWallpaperFromUrl(context, imageUrl, WallpaperType.BOTH)
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

