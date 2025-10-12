package com.truonganim.admob.ads.native_ads

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

/**
 * Native Ad Loader
 * Handles loading native ads for a specific position
 */
class NativeAdLoader(
    private val context: Context,
    private val position: NativeAdPosition
) {
    private var adLoader: AdLoader? = null
    private var isLoading = false
    
    /**
     * Load native ad
     */
    fun loadAd(
        onAdLoaded: (NativeAd) -> Unit,
        onAdFailedToLoad: (String) -> Unit
    ) {
        if (isLoading) {
            println("⚠️ Native ad for ${position.name} is already loading")
            return
        }
        
        isLoading = true
        println("🔄 Loading Native Ad for position: ${position.name}")
        
        val adOptions = NativeAdOptions.Builder()
            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
            .build()
        
        adLoader = AdLoader.Builder(context, position.adUnitId)
            .forNativeAd { nativeAd ->
                println("✅ Native Ad loaded successfully for position: ${position.name}")
                isLoading = false
                onAdLoaded(nativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    println("❌ Native Ad failed to load for ${position.name}: ${error.message}")
                    isLoading = false
                    onAdFailedToLoad(error.message)
                }
                
                override fun onAdClicked() {
                    println("👆 Native Ad clicked for position: ${position.name}")
                }
                
                override fun onAdImpression() {
                    println("👁️ Native Ad impression for position: ${position.name}")
                }
            })
            .withNativeAdOptions(adOptions)
            .build()
        
        adLoader?.loadAd(AdRequest.Builder().build())
    }
    
    /**
     * Check if ad is currently loading
     */
    fun isLoading(): Boolean {
        return isLoading
    }
    
    /**
     * Destroy loader
     */
    fun destroy() {
        adLoader = null
        isLoading = false
    }
}

