package com.truonganim.admob.wallpaper

import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import java.io.File
import kotlin.random.Random

/**
 * Random Wallpaper Service
 * Live wallpaper that changes randomly from a list of images
 */
class RandomWallpaperService : WallpaperService() {
    
    companion object {
        private const val TAG = "RandomWallpaperService"
    }
    
    override fun onCreateEngine(): Engine {
        return RandomWallpaperEngine()
    }
    
    inner class RandomWallpaperEngine : Engine() {
        
        private val handler = Handler(Looper.getMainLooper())
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var imagePaths: List<String> = emptyList()
        private var intervalSeconds: Int = 15
        private var currentImageIndex: Int = -1
        
        private val updateRunnable = object : Runnable {
            override fun run() {
                drawNextWallpaper()
                handler.postDelayed(this, intervalSeconds * 1000L)
            }
        }
        
        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            Log.d(TAG, "Engine created")
            
            // Load settings
            loadSettings()
        }
        
        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            
            if (visible) {
                Log.d(TAG, "Wallpaper visible, starting updates")
                drawNextWallpaper()
                handler.postDelayed(updateRunnable, intervalSeconds * 1000L)
            } else {
                Log.d(TAG, "Wallpaper hidden, stopping updates")
                handler.removeCallbacks(updateRunnable)
            }
        }
        
        override fun onSurfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            Log.d(TAG, "Surface changed: ${width}x${height}")
            drawNextWallpaper()
        }
        
        override fun onDestroy() {
            super.onDestroy()
            Log.d(TAG, "Engine destroyed")
            handler.removeCallbacks(updateRunnable)
        }
        
        /**
         * Load settings from preferences
         */
        private fun loadSettings() {
            val prefs = WallpaperPreferences.getInstance(applicationContext)
            imagePaths = prefs.getImagePaths()
            intervalSeconds = prefs.getInterval()
            
            Log.d(TAG, "Loaded ${imagePaths.size} images, interval: ${intervalSeconds}s")
        }
        
        /**
         * Draw next wallpaper
         */
        private fun drawNextWallpaper() {
            if (imagePaths.isEmpty()) {
                Log.w(TAG, "No images available")
                return
            }
            
            // Get next random image
            val nextIndex = Random.nextInt(imagePaths.size)
            currentImageIndex = nextIndex
            
            val imagePath = imagePaths[currentImageIndex]
            Log.d(TAG, "Drawing wallpaper: $imagePath")
            
            drawWallpaper(imagePath)
        }
        
        /**
         * Draw wallpaper from file path
         */
        private fun drawWallpaper(imagePath: String) {
            val holder = surfaceHolder ?: return
            
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    // Load bitmap
                    val file = File(imagePath)
                    if (!file.exists()) {
                        Log.e(TAG, "Image file not found: $imagePath")
                        return
                    }
                    
                    val bitmap = BitmapFactory.decodeFile(imagePath)
                    if (bitmap == null) {
                        Log.e(TAG, "Failed to decode image: $imagePath")
                        return
                    }
                    
                    // Calculate scaling to fill screen
                    val canvasWidth = canvas.width.toFloat()
                    val canvasHeight = canvas.height.toFloat()
                    val bitmapWidth = bitmap.width.toFloat()
                    val bitmapHeight = bitmap.height.toFloat()
                    
                    val scaleX = canvasWidth / bitmapWidth
                    val scaleY = canvasHeight / bitmapHeight
                    val scale = maxOf(scaleX, scaleY) // Fill screen
                    
                    val scaledWidth = bitmapWidth * scale
                    val scaledHeight = bitmapHeight * scale
                    
                    val left = (canvasWidth - scaledWidth) / 2
                    val top = (canvasHeight - scaledHeight) / 2
                    
                    // Clear canvas
                    canvas.drawColor(android.graphics.Color.BLACK)
                    
                    // Draw bitmap
                    val destRect = android.graphics.RectF(
                        left,
                        top,
                        left + scaledWidth,
                        top + scaledHeight
                    )
                    
                    canvas.drawBitmap(bitmap, null, destRect, paint)
                    
                    bitmap.recycle()
                    
                    Log.d(TAG, "Wallpaper drawn successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error drawing wallpaper", e)
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }
    }
}

