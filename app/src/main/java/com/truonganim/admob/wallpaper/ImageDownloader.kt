package com.truonganim.admob.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Image Downloader
 * Downloads and caches images to internal storage
 */
class ImageDownloader(private val context: Context) {
    
    companion object {
        private const val TAG = "ImageDownloader"
        private const val WALLPAPER_DIR = "wallpapers"
    }
    
    private val imageLoader = ImageLoader(context)
    
    /**
     * Get wallpaper directory
     */
    private fun getWallpaperDir(): File {
        val dir = File(context.filesDir, WALLPAPER_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    /**
     * Get file name from URL
     */
    private fun getFileNameFromUrl(url: String): String {
        // Use hash of URL as filename to avoid special characters
        return url.hashCode().toString() + ".jpg"
    }
    
    /**
     * Check if image already exists
     */
    fun isImageCached(url: String): Boolean {
        val fileName = getFileNameFromUrl(url)
        val file = File(getWallpaperDir(), fileName)
        return file.exists()
    }
    
    /**
     * Get cached image path
     */
    fun getCachedImagePath(url: String): String? {
        val fileName = getFileNameFromUrl(url)
        val file = File(getWallpaperDir(), fileName)
        return if (file.exists()) file.absolutePath else null
    }
    
    /**
     * Download image from URL and save to internal storage
     * Returns the local file path
     */
    suspend fun downloadImage(url: String): String? = withContext(Dispatchers.IO) {
        try {
            // Check if already cached
            getCachedImagePath(url)?.let {
                Log.d(TAG, "Image already cached: $url")
                return@withContext it
            }
            
            Log.d(TAG, "Downloading image: $url")
            
            // Load image using Coil
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false) // Disable hardware bitmaps for file saving
                .build()
            
            val result = imageLoader.execute(request)
            
            if (result is SuccessResult) {
                val bitmap = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                
                if (bitmap != null) {
                    // Save to file
                    val fileName = getFileNameFromUrl(url)
                    val file = File(getWallpaperDir(), fileName)
                    
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    
                    Log.d(TAG, "Image saved: ${file.absolutePath}")
                    return@withContext file.absolutePath
                }
            }
            
            Log.e(TAG, "Failed to download image: $url")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading image: $url", e)
            null
        }
    }
    
    /**
     * Download multiple images
     * Returns list of successful local file paths
     */
    suspend fun downloadImages(
        urls: List<String>,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): List<String> = withContext(Dispatchers.IO) {
        val paths = mutableListOf<String>()
        
        urls.forEachIndexed { index, url ->
            onProgress(index + 1, urls.size)
            
            downloadImage(url)?.let { path ->
                paths.add(path)
            }
        }
        
        paths
    }
    
    /**
     * Clear all cached wallpapers
     */
    fun clearCache() {
        val dir = getWallpaperDir()
        dir.listFiles()?.forEach { it.delete() }
        Log.d(TAG, "Cache cleared")
    }
    
    /**
     * Get cache size in bytes
     */
    fun getCacheSize(): Long {
        val dir = getWallpaperDir()
        return dir.listFiles()?.sumOf { it.length() } ?: 0L
    }
}

