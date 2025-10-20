package com.truonganim.admob.wallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Wallpaper Manager
 * Manages wallpaper setup and configuration
 */
class AppWallpaperManager(private val context: Context) {
    
    companion object {
        private const val TAG = "AppWallpaperManager"
        
        @Volatile
        private var instance: AppWallpaperManager? = null
        
        fun getInstance(context: Context): AppWallpaperManager {
            return instance ?: synchronized(this) {
                instance ?: AppWallpaperManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    private val imageDownloader = ImageDownloader(context)
    private val preferences = WallpaperPreferences.getInstance(context)
    
    /**
     * Setup random wallpaper
     * Downloads images and saves paths to preferences
     * 
     * @param imageUrls List of image URLs to download
     * @param intervalSeconds Interval between wallpaper changes
     * @param onProgress Progress callback (current, total)
     * @return True if setup successful
     */
    suspend fun setupRandomWallpaper(
        imageUrls: List<String>,
        intervalSeconds: Int = 15,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Setting up random wallpaper with ${imageUrls.size} images")
            
            // Download images
            val localPaths = imageDownloader.downloadImages(imageUrls, onProgress)
            
            if (localPaths.isEmpty()) {
                Log.e(TAG, "No images downloaded")
                return@withContext false
            }
            
            Log.d(TAG, "Downloaded ${localPaths.size} images")
            
            // Save to preferences
            preferences.saveImagePaths(localPaths)
            preferences.saveInterval(intervalSeconds)
            
            Log.d(TAG, "Random wallpaper setup complete")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up random wallpaper", e)
            false
        }
    }
    
    /**
     * Launch wallpaper picker to set live wallpaper
     */
    fun launchWallpaperPicker() {
        try {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(context, RandomWallpaperService::class.java)
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            
            Log.d(TAG, "Launched wallpaper picker")
        } catch (e: Exception) {
            Log.e(TAG, "Error launching wallpaper picker", e)
            
            // Fallback to general wallpaper settings
            try {
                val intent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e2: Exception) {
                Log.e(TAG, "Error launching fallback wallpaper picker", e2)
            }
        }
    }
    
    /**
     * Check if images are already downloaded
     */
    fun areImagesDownloaded(imageUrls: List<String>): Boolean {
        return imageUrls.all { imageDownloader.isImageCached(it) }
    }
    
    /**
     * Get number of cached images
     */
    fun getCachedImageCount(): Int {
        return preferences.getImagePaths().size
    }
    
    /**
     * Clear all wallpaper data
     */
    fun clearWallpaperData() {
        preferences.clear()
        imageDownloader.clearCache()
        Log.d(TAG, "Wallpaper data cleared")
    }
}

