package com.truonganim.admob.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.truonganim.admob.BuildConfig

/**
 * Interstitial Ad Manager
 * Singleton manager for main app interstitial ads
 */
class InterstitialAdManager private constructor(
    private val context: Context
) {
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    
    companion object {
        private const val TAG = "InterstitialAdManager"
        
        // Test Interstitial Ad ID
        private const val AD_UNIT_ID = BuildConfig.MAIN_INTERSTITIAL_AD_ID
        
        @Volatile
        private var instance: InterstitialAdManager? = null
        
        fun getInstance(context: Context): InterstitialAdManager {
            return instance ?: synchronized(this) {
                instance ?: InterstitialAdManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    /**
     * Load interstitial ad
     */
    fun loadAd(onAdLoaded: (() -> Unit)? = null, onAdFailedToLoad: ((String) -> Unit)? = null) {
        if (isLoadingAd || isAdAvailable()) {
            Log.d(TAG, "Ad is already loading or available")
            return
        }
        
        isLoadingAd = true
        val request = AdRequest.Builder().build()
        
        InterstitialAd.load(
            context,
            AD_UNIT_ID,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully")
                    interstitialAd = ad
                    isLoadingAd = false
                    onAdLoaded?.invoke()
                }
                
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                    interstitialAd = null
                    isLoadingAd = false
                    onAdFailedToLoad?.invoke(loadAdError.message)
                }
            }
        )
    }
    
    /**
     * Show interstitial ad
     */
    fun showAdIfAvailable(
        activity: Activity,
        onAdDismissed: (Boolean) -> Unit, // Boolean = ad was shown successfully
        onAdFailedToShow: (String) -> Unit
    ) {
        if (!isAdAvailable()) {
            Log.d(TAG, "Interstitial ad is not available")
            onAdFailedToShow("Ad not available")
            return
        }
        
        if (isShowingAd) {
            Log.d(TAG, "Interstitial ad is already showing")
            return
        }
        
        var adWasShown = false
        
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial ad dismissed")
                interstitialAd = null
                isShowingAd = false
                
                // Reload ad for next time
                loadAd()
                
                onAdDismissed(adWasShown)
            }
            
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Interstitial ad failed to show: ${adError.message}")
                interstitialAd = null
                isShowingAd = false
                
                // Reload ad for next time
                loadAd()
                
                onAdFailedToShow(adError.message)
            }
            
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial ad showed")
                isShowingAd = true
                adWasShown = true
            }
        }
        
        interstitialAd?.show(activity)
    }
    
    /**
     * Check if ad is available
     */
    fun isAdAvailable(): Boolean {
        return interstitialAd != null
    }
    
    /**
     * Clear ad
     */
    fun clear() {
        interstitialAd = null
        isLoadingAd = false
        isShowingAd = false
    }
}

