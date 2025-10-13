package com.truonganim.admob.config

import com.truonganim.admob.utils.WallpaperHelper

/**
 * Application configuration constants
 */
object AppConfig {
    
    /**
     * Wallpaper Settings
     */
    object Wallpaper {
        /**
         * Method for setting wallpaper
         * 
         * Options:
         * - SetWallpaperMethod.AUTO: 
         *   Automatically set wallpaper without user interaction.
         *   Faster, but user cannot choose crop/position.
         * 
         * - SetWallpaperMethod.INTENT: 
         *   Open system wallpaper picker for user to choose crop/position.
         *   Slower, but gives user more control.
         * 
         * Default: INTENT (recommended for better UX)
         */
        var SET_WALLPAPER_METHOD: WallpaperHelper.SetWallpaperMethod = 
            WallpaperHelper.SetWallpaperMethod.INTENT
        
        /**
         * Default wallpaper type when using AUTO method
         * 
         * Options:
         * - WallpaperType.HOME_SCREEN: Only home screen
         * - WallpaperType.LOCK_SCREEN: Only lock screen (Android 7+)
         * - WallpaperType.BOTH: Both home and lock screen
         * 
         * Default: BOTH
         */
        var DEFAULT_WALLPAPER_TYPE: WallpaperHelper.WallpaperType = 
            WallpaperHelper.WallpaperType.BOTH
    }
    
    /**
     * Ad Settings
     */
    object Ads {
        // TODO: Add ad configuration here
        // Example:
        // const val SHOW_AD_ON_CHARACTER_UNLOCK = true
        // const val AD_FREQUENCY = 3 // Show ad every 3 actions
    }
    
    /**
     * UI Settings
     */
    object UI {
        // TODO: Add UI configuration here
        // Example:
        // const val GRID_COLUMNS_PORTRAIT = 2
        // const val GRID_COLUMNS_LANDSCAPE = 3
    }
}

