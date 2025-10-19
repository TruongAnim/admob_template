package com.truonganim.admob.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.truonganim.admob.wallpaper.AppWallpaperManager
import kotlinx.coroutines.launch

/**
 * Set Random Wallpaper Button
 * A gradient button that downloads images and sets them as random live wallpaper
 * Shows loading overlay during download process
 * 
 * @param imageUrls List of image URLs to download
 * @param text Button text (default: "SET RANDOM WALLPAPER")
 * @param intervalSeconds Interval between wallpaper changes in seconds (default: 15)
 * @param gradientColors Gradient colors for the button
 * @param modifier Modifier for the button
 * @param height Button height
 * @param cornerRadius Button corner radius
 */
@Composable
fun SetRandomWallpaperButton(
    imageUrls: List<String>,
    modifier: Modifier = Modifier,
    text: String = "SET RANDOM WALLPAPER",
    intervalSeconds: Int = 15,
    gradientColors: List<Color> = GradientPresets.Aurora,
    height: Dp = 56.dp,
    cornerRadius: Dp = 28.dp
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val wallpaperManager = AppWallpaperManager.getInstance(context)
    val loadingManager = AppLoadingOverlayManager.getInstance()
    
    GradientButton(
        text = text,
        onClick = {
            if (imageUrls.isEmpty()) {
                return@GradientButton
            }
            
            coroutineScope.launch {
                try {
                    // Show loading overlay
                    loadingManager.show("Preparing wallpaper...")
                    
                    // Download images with progress
                    val success = wallpaperManager.setupRandomWallpaper(
                        imageUrls = imageUrls,
                        intervalSeconds = intervalSeconds,
                        onProgress = { current, total ->
                            // Update loading message with progress
                            loadingManager.updateMessage("Downloading images $current/$total")
                        }
                    )
                    
                    // Check if user cancelled during download
                    if (loadingManager.isCancelled()) {
                        // User cancelled, don't proceed
                        return@launch
                    }
                    
                    // Hide loading overlay
                    loadingManager.hide()
                    
                    // If download successful, launch wallpaper picker
                    if (success) {
                        wallpaperManager.launchWallpaperPicker()
                    }
                } catch (e: Exception) {
                    // Hide loading on error
                    loadingManager.hide()
                }
            }
        },
        icon = Icons.Default.Wallpaper,
        enabled = imageUrls.isNotEmpty(),
        gradientColors = gradientColors,
        modifier = modifier,
        height = height,
        cornerRadius = cornerRadius
    )
}

