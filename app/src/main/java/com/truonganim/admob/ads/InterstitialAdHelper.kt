package com.truonganim.admob.ads

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Interstitial Ad Helper
 * Handles loading and showing interstitial ads
 */
class InterstitialAdHelper(
    private val adUnitId: String
) {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    
    /**
     * Load interstitial ad
     */
    fun loadAd(
        activity: Activity,
        onAdLoaded: () -> Unit,
        onAdFailedToLoad: (String) -> Unit
    ) {
        if (isLoading) {
            println("⚠️ Interstitial ad is already loading")
            return
        }
        
        if (interstitialAd != null) {
            println("✅ Interstitial ad already loaded")
            onAdLoaded()
            return
        }
        
        isLoading = true
        println("🔄 Loading Interstitial Ad...")
        
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(
            activity,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    println("✅ Interstitial Ad loaded successfully!")
                    interstitialAd = ad
                    isLoading = false
                    onAdLoaded()
                }
                
                override fun onAdFailedToLoad(error: LoadAdError) {
                    println("❌ Interstitial Ad failed to load: ${error.message}")
                    interstitialAd = null
                    isLoading = false
                    onAdFailedToLoad(error.message)
                }
            }
        )
    }
    
    /**
     * Show interstitial ad
     */
    fun showAd(
        activity: Activity,
        onAdShown: () -> Unit,
        onAdDismissed: () -> Unit,
        onAdFailed: (String) -> Unit
    ) {
        val ad = interstitialAd
        
        if (ad == null) {
            println("❌ Interstitial ad is not ready yet")
            onAdFailed("Ad not loaded")
            return
        }
        
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                println("📺 Interstitial Ad showed full screen content")
                onAdShown()
            }
            
            override fun onAdDismissedFullScreenContent() {
                println("✅ Interstitial Ad dismissed")
                interstitialAd = null // Ad can only be shown once
                onAdDismissed()
            }
            
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                println("❌ Interstitial Ad failed to show: ${error.message}")
                interstitialAd = null
                onAdFailed(error.message)
            }
        }
        
        ad.show(activity)
    }
    
    /**
     * Check if ad is loaded and ready to show
     */
    fun isAdReady(): Boolean {
        return interstitialAd != null
    }
    
    /**
     * Destroy ad
     */
    fun destroy() {
        interstitialAd = null
        isLoading = false
    }
}

