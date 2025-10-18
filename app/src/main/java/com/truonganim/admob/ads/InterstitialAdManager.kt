package com.truonganim.admob.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.truonganim.admob.BuildConfig
import com.truonganim.admob.data.AdGateConfig
import com.truonganim.admob.firebase.RemoteConfigKeys
import com.truonganim.admob.utils.AdLogger
import java.util.Timer
import java.util.TimerTask

/**
 * Interstitial Ad Manager
 * Singleton manager for main app interstitial ads and companion native ads
 * Uses timer-based loading strategy (load every 30 seconds)
 */
class InterstitialAdManager private constructor(
    private val context: Context
) {
    private var interstitialAd: InterstitialAd? = null
    private var nativeAd: NativeAd? = null
    private var isLoadingInterstitialAd = false
    private var isLoadingNativeAd = false
    private var isShowingAd = false

    // Config flag
    private var nativeAdEnabled = true

    // Timer for periodic ad loading
    private var adLoadTimer: Timer? = null
    private var isTimerRunning = false

    // Handler for main thread
    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        private const val TAG = "InterstitialAdManager"

        // Ad Unit IDs
        private const val INTERSTITIAL_AD_UNIT_ID = BuildConfig.MAIN_INTERSTITIAL_AD_ID
        private const val NATIVE_AD_UNIT_ID = BuildConfig.NATIVE_AD_AFTER_INTERESTIAL

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
     * Set whether native ad is enabled
     */
    fun setNativeAdEnabled(enabled: Boolean) {
        nativeAdEnabled = enabled
    }

    /**
     * Load AdGateConfig from Remote Config
     */
    private fun loadAdGateConfig(): AdGateConfig {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configJson = remoteConfig.getString(RemoteConfigKeys.AD_GATE_CONFIG)

        return if (configJson.isNotEmpty()) {
            AdGateConfig.fromJson(configJson)
        } else {
            AdGateConfig.getDefault()
        }
    }

    /**
     * Start timer to load ads periodically
     * Interval is from AdGateConfig.adIntervalSeconds
     * Call this when entering HomeScreen
     */
    fun startAdLoadTimer() {
        if (isTimerRunning) {
            AdLogger.d(TAG, "Ad load timer already running")
            return
        }

        // Load config to get interval
        val config = loadAdGateConfig()
        val intervalMs = config.adIntervalSeconds * 1000L

        AdLogger.i(TAG, "Starting ad load timer (interval: ${intervalMs}ms = ${config.adIntervalSeconds}s)")
        isTimerRunning = true

        // Schedule periodic loading
        adLoadTimer = Timer()
        adLoadTimer?.schedule(object : TimerTask() {
            override fun run() {
                // Post to main thread
                mainHandler.post {
                    loadAds()
                }
            }
        }, intervalMs * 2 / 3, intervalMs)
    }

    /**
     * Stop timer
     */
    fun stopAdLoadTimer() {
        if (!isTimerRunning) {
            return
        }

        AdLogger.i(TAG, "Stopping ad load timer")
        adLoadTimer?.cancel()
        adLoadTimer = null
        isTimerRunning = false
    }

    /**
     * Load both interstitial and native ads
     * Called by timer every 30 seconds
     */
    private fun loadAds() {
        AdLogger.d(TAG, "Timer triggered: Loading ads...")

        // Load interstitial ad
        loadInterstitialAd()

        // Load native ad if enabled
        if (nativeAdEnabled) {
            loadNativeAd()
        }
    }

    /**
     * Load interstitial ad
     * No retry on failure - only load via timer
     */
    private fun loadInterstitialAd() {
        // Don't reload if already loaded
        if (interstitialAd != null) {
            AdLogger.d(TAG, "Interstitial ad already loaded, skipping")
            return
        }

        if (isLoadingInterstitialAd) {
            AdLogger.d(TAG, "Interstitial ad is already loading, skipping")
            return
        }

        isLoadingInterstitialAd = true
        AdLogger.i(TAG, "Loading interstitial ad...")

        val request = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    AdLogger.i(TAG, "Interstitial ad loaded successfully")
                    interstitialAd = ad
                    isLoadingInterstitialAd = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    AdLogger.e(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                    interstitialAd = null
                    isLoadingInterstitialAd = false
                    // No retry - wait for next timer event
                }
            }
        )
    }

    /**
     * Load native ad
     * No retry on failure - only load via timer
     */
    private fun loadNativeAd() {
        // Don't reload if already loaded
        if (nativeAd != null) {
            AdLogger.d(TAG, "Native ad already loaded, skipping")
            return
        }

        if (isLoadingNativeAd) {
            AdLogger.d(TAG, "Native ad is already loading, skipping")
            return
        }

        isLoadingNativeAd = true
        AdLogger.i(TAG, "Loading native ad...")

        val adLoader = AdLoader.Builder(context, NATIVE_AD_UNIT_ID)
            .forNativeAd { ad ->
                AdLogger.i(TAG, "Native ad loaded successfully")
                nativeAd = ad
                isLoadingNativeAd = false
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    AdLogger.e(TAG, "Native ad failed to load: ${loadAdError.message}")
                    nativeAd = null
                    isLoadingNativeAd = false
                    // No retry - wait for next timer event
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }
    
    /**
     * Show interstitial ad
     * No auto-reload - only load via timer
     */
    fun showInterstitialAdIfAvailable(
        activity: Activity,
        onAdDismissed: (Boolean) -> Unit, // Boolean = ad was shown successfully
        onAdFailedToShow: (String) -> Unit
    ) {
        if (interstitialAd == null) {
            AdLogger.w(TAG, "Interstitial ad is not available")
            onAdFailedToShow("Ad not available")
            return
        }

        if (isShowingAd) {
            AdLogger.w(TAG, "Interstitial ad is already showing")
            return
        }

        var adWasShown = false

        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                AdLogger.i(TAG, "Interstitial ad dismissed")
                interstitialAd = null
                isShowingAd = false

                // No auto-reload - wait for timer

                onAdDismissed(adWasShown)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                AdLogger.e(TAG, "Interstitial ad failed to show: ${adError.message}")
                interstitialAd = null
                isShowingAd = false

                // No auto-reload - wait for timer

                onAdFailedToShow(adError.message)
            }

            override fun onAdShowedFullScreenContent() {
                AdLogger.i(TAG, "Interstitial ad showed")
                isShowingAd = true
                adWasShown = true
            }
        }

        interstitialAd?.show(activity)
    }

    /**
     * Get native ad if available
     */
    fun getNativeAd(): NativeAd? {
        return nativeAd
    }

    /**
     * Mark native ad as shown
     * No auto-reload - wait for timer
     */
    fun markNativeAdShown() {
        AdLogger.i(TAG, "Native ad marked as shown")
        nativeAd?.destroy()
        nativeAd = null

        // No auto-reload - wait for timer
    }

    /**
     * Check if interstitial ad is available
     */
    fun isInterstitialAdAvailable(): Boolean {
        return interstitialAd != null
    }

    /**
     * Check if native ad is available
     */
    fun isNativeAdAvailable(): Boolean {
        return nativeAd != null
    }

    /**
     * Clear all ads and stop timer
     */
    fun clear() {
        AdLogger.i(TAG, "Clearing all ads and stopping timer")
        stopAdLoadTimer()
        interstitialAd = null
        nativeAd?.destroy()
        nativeAd = null
        isLoadingInterstitialAd = false
        isLoadingNativeAd = false
        isShowingAd = false
    }
}

