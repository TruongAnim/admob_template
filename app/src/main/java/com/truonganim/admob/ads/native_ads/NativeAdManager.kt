package com.truonganim.admob.ads.native_ads

import android.content.Context
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Native Ad Manager
 * Singleton class to manage all native ads in the app
 * Handles preloading, caching, and retrieving native ads
 * Uses StateFlow to notify observers when ads are loaded
 */
class NativeAdManager private constructor() {

    // StateFlow for each position to notify observers
    private val adStateFlows = ConcurrentHashMap<NativeAdPosition, MutableStateFlow<NativeAd?>>()

    // Loaders for each position
    private val loaders = ConcurrentHashMap<NativeAdPosition, NativeAdLoader>()
    
    /**
     * Get StateFlow for a specific position
     * Views can observe this to get notified when ad is loaded
     */
    fun getAdStateFlow(position: NativeAdPosition): StateFlow<NativeAd?> {
        return adStateFlows.getOrPut(position) {
            MutableStateFlow(null)
        }.asStateFlow()
    }

    /**
     * Preload ad for a specific position
     * This should be called early (e.g., in Application.onCreate or Activity.onCreate)
     */
    fun preloadAd(context: Context, position: NativeAdPosition) {
        // Get or create StateFlow for this position
        val stateFlow = adStateFlows.getOrPut(position) {
            MutableStateFlow(null)
        }

        // Don't reload if already loaded
        if (stateFlow.value != null) {
            println("✅ Native ad for ${position.name} already loaded")
            return
        }

        val loader = loaders.getOrPut(position) {
            NativeAdLoader(context.applicationContext, position)
        }

        loader.loadAd(
            onAdLoaded = { nativeAd ->
                stateFlow.value = nativeAd
                println("💾 Native ad loaded and emitted for position: ${position.name}")
            },
            onAdFailedToLoad = { error ->
                println("⚠️ Failed to preload native ad for ${position.name}: $error")
            }
        )
    }
    
    /**
     * Load ad for a position (if not already loaded)
     * This will trigger the StateFlow to emit the ad when loaded
     */
    fun loadAd(context: Context, position: NativeAdPosition) {
        // Get or create StateFlow for this position
        val stateFlow = adStateFlows.getOrPut(position) {
            MutableStateFlow(null)
        }

        // Don't reload if already loaded
        if (stateFlow.value != null) {
            println("✅ Native ad for ${position.name} already loaded")
            return
        }

        // Check if already loading
        val loader = loaders.getOrPut(position) {
            NativeAdLoader(context.applicationContext, position)
        }

        if (loader.isLoading()) {
            println("⏳ Native ad for ${position.name} is already loading, waiting...")
            return
        }

        // Load new ad
        loader.loadAd(
            onAdLoaded = { nativeAd ->
                stateFlow.value = nativeAd
                println("💾 Native ad loaded and emitted for position: ${position.name}")
            },
            onAdFailedToLoad = { error ->
                println("❌ Failed to load native ad for ${position.name}: $error")
            }
        )
    }
    
    /**
     * Clear ad for a specific position
     */
    fun clearAd(position: NativeAdPosition) {
        adStateFlows[position]?.value?.destroy()
        adStateFlows[position]?.value = null
        println("🗑️ Cleared native ad for position: ${position.name}")
    }

    /**
     * Clear all ads
     */
    fun clearAllAds() {
        adStateFlows.values.forEach { stateFlow ->
            stateFlow.value?.destroy()
            stateFlow.value = null
        }
        println("🗑️ Cleared all native ads")
    }

    /**
     * Destroy all loaders and clear all ads
     */
    fun destroy() {
        loaders.values.forEach { it.destroy() }
        loaders.clear()
        clearAllAds()
        adStateFlows.clear()
    }
    
    companion object {
        @Volatile
        private var instance: NativeAdManager? = null
        
        fun getInstance(): NativeAdManager {
            return instance ?: synchronized(this) {
                instance ?: NativeAdManager().also { instance = it }
            }
        }
    }
}

