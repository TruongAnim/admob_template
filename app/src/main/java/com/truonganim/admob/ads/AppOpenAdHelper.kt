package com.truonganim.admob.ads

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

/**
 * App Open Ad Helper
 * Handles loading and showing app open ads
 */
class AppOpenAdHelper(
    private val adUnitId: String
) {
    private var appOpenAd: AppOpenAd? = null
    private var isLoading = false
    
    /**
     * Load app open ad
     */
    fun loadAd(
        activity: Activity,
        onAdLoaded: () -> Unit,
        onAdFailedToLoad: (String) -> Unit
    ) {
        if (isLoading) {
            println("⚠️ App Open ad is already loading")
            return
        }
        
        if (appOpenAd != null) {
            println("✅ App Open ad already loaded")
            onAdLoaded()
            return
        }
        
        isLoading = true
        println("🔄 Loading App Open Ad...")
        
        val adRequest = AdRequest.Builder().build()
        
        AppOpenAd.load(
            activity,
            adUnitId,
            adRequest,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    println("✅ App Open Ad loaded successfully!")
                    appOpenAd = ad
                    isLoading = false
                    onAdLoaded()
                }
                
                override fun onAdFailedToLoad(error: LoadAdError) {
                    println("❌ App Open Ad failed to load: ${error.message}")
                    appOpenAd = null
                    isLoading = false
                    onAdFailedToLoad(error.message)
                }
            }
        )
    }
    
    /**
     * Show app open ad
     */
    fun showAd(
        activity: Activity,
        onAdShown: () -> Unit,
        onAdDismissed: () -> Unit,
        onAdFailed: (String) -> Unit
    ) {
        val ad = appOpenAd
        
        if (ad == null) {
            println("❌ App Open ad is not ready yet")
            onAdFailed("Ad not loaded")
            return
        }
        
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                println("📺 App Open Ad showed full screen content")
                onAdShown()
            }
            
            override fun onAdDismissedFullScreenContent() {
                println("✅ App Open Ad dismissed")
                appOpenAd = null // Ad can only be shown once
                onAdDismissed()
            }
            
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                println("❌ App Open Ad failed to show: ${error.message}")
                appOpenAd = null
                onAdFailed(error.message)
            }
        }
        
        ad.show(activity)
    }
    
    /**
     * Check if ad is loaded and ready to show
     */
    fun isAdReady(): Boolean {
        return appOpenAd != null
    }
    
    /**
     * Destroy ad
     */
    fun destroy() {
        appOpenAd = null
        isLoading = false
    }
}

