package com.truonganim.admob.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.truonganim.admob.data.AppOpenAdConfig
import com.truonganim.admob.utils.AdLogger

/**
 * App Open Ad Manager
 * Manages loading and showing app open ads
 */
class AppOpenAdManager(
    private val context: Context,
    private val config: AppOpenAdConfig
) {
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    
    companion object {
        private const val TAG = "AppOpenAdManager"
    }
    
    /**
     * Load app open ad
     */
    fun loadAd(onAdLoaded: () -> Unit, onAdFailedToLoad: (String) -> Unit) {
        if (isLoadingAd || isAdAvailable()) {
            return
        }
        
        if (config.adUnitId.isEmpty()) {
            onAdFailedToLoad("Ad unit ID is empty")
            return
        }
        
        isLoadingAd = true
        val request = AdRequest.Builder().build()
        
        AppOpenAd.load(
            context,
            config.adUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    AdLogger.i(TAG, "App open ad loaded successfully")
                    appOpenAd = ad
                    isLoadingAd = false
                    onAdLoaded()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    AdLogger.e(TAG, "App open ad failed to load: ${loadAdError.message}")
                    isLoadingAd = false
                    onAdFailedToLoad(loadAdError.message)
                }
            }
        )
    }
    
    /**
     * Show app open ad
     */
    fun showAdIfAvailable(
        activity: Activity,
        onAdDismissed: () -> Unit,
        onAdFailedToShow: (String) -> Unit
    ) {
        if (!isAdAvailable()) {
            AdLogger.i(TAG, "App open ad is not available")
            onAdFailedToShow("Ad not available")
            return
        }
        
        if (isShowingAd) {
            AdLogger.i(TAG, "App open ad is already showing")
            return
        }
        
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                AdLogger.i(TAG, "App open ad dismissed")
                appOpenAd = null
                isShowingAd = false
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                AdLogger.e(TAG, "App open ad failed to show: ${adError.message}")
                appOpenAd = null
                isShowingAd = false
                onAdFailedToShow(adError.message)
            }

            override fun onAdShowedFullScreenContent() {
                AdLogger.i(TAG, "App open ad showed")
                isShowingAd = true
            }
        }
        
        appOpenAd?.show(activity)
    }
    
    /**
     * Check if ad is available
     */
    fun isAdAvailable(): Boolean {
        return appOpenAd != null
    }
    
    /**
     * Clear ad
     */
    fun clear() {
        appOpenAd = null
        isLoadingAd = false
        isShowingAd = false
    }
}

