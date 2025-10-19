package com.truonganim.admob.wallpaper

import android.content.Context
import android.content.SharedPreferences

/**
 * Wallpaper Preferences
 * Manages wallpaper settings in SharedPreferences
 */
class WallpaperPreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val PREFS_NAME = "wallpaper_prefs"
        private const val KEY_IMAGE_PATHS = "image_paths"
        private const val KEY_INTERVAL_SECONDS = "interval_seconds"
        private const val DEFAULT_INTERVAL_SECONDS = 15
        
        @Volatile
        private var instance: WallpaperPreferences? = null
        
        fun getInstance(context: Context): WallpaperPreferences {
            return instance ?: synchronized(this) {
                instance ?: WallpaperPreferences(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    /**
     * Save image paths
     */
    fun saveImagePaths(paths: List<String>) {
        prefs.edit()
            .putStringSet(KEY_IMAGE_PATHS, paths.toSet())
            .apply()
    }
    
    /**
     * Get image paths
     */
    fun getImagePaths(): List<String> {
        return prefs.getStringSet(KEY_IMAGE_PATHS, emptySet())?.toList() ?: emptyList()
    }
    
    /**
     * Save interval in seconds
     */
    fun saveInterval(seconds: Int) {
        prefs.edit()
            .putInt(KEY_INTERVAL_SECONDS, seconds)
            .apply()
    }
    
    /**
     * Get interval in seconds
     */
    fun getInterval(): Int {
        return prefs.getInt(KEY_INTERVAL_SECONDS, DEFAULT_INTERVAL_SECONDS)
    }
    
    /**
     * Clear all wallpaper data
     */
    fun clear() {
        prefs.edit().clear().apply()
    }
}

