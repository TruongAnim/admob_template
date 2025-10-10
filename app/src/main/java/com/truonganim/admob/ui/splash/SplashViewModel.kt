package com.truonganim.admob.ui.splash

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truonganim.admob.BuildConfig
import com.truonganim.admob.ads.AdType
import com.truonganim.admob.ads.AppOpenAdHelper
import com.truonganim.admob.ads.InterstitialAdHelper
import com.truonganim.admob.firebase.RemoteConfigHelper
import com.truonganim.admob.firebase.RemoteConfigKeys
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Splash Screen
 * Manages loading progress and completion state
 * Fetches Firebase Remote Config and loads ads during splash
 */
class SplashViewModel : ViewModel() {

    private val remoteConfigHelper = RemoteConfigHelper.getInstance()

    private var interstitialAdHelper: InterstitialAdHelper? = null
    private var appOpenAdHelper: AppOpenAdHelper? = null
    private var currentAdType: AdType = AdType.NONE
    
    private val _loadingProgress = MutableStateFlow(0)
    val loadingProgress: StateFlow<Int> = _loadingProgress.asStateFlow()

    private val _isLoadingComplete = MutableStateFlow(false)
    val isLoadingComplete: StateFlow<Boolean> = _isLoadingComplete.asStateFlow()

    private val _shouldShowAd = MutableStateFlow(false)
    val shouldShowAd: StateFlow<Boolean> = _shouldShowAd.asStateFlow()

    fun startLoading(activity: Activity) {
        loadRemoteConfigAndAd(activity)
    }
    
    /**
     * Load Remote Config and Ad based on config
     */
    private fun loadRemoteConfigAndAd(activity: Activity) {
        viewModelScope.launch {
            // Phase 1: Initial loading (0-30%)
            for (progress in 0..30 step 5) {
                _loadingProgress.value = progress
                delay(50)
            }

            // Phase 2: Fetch Remote Config (30-50%)
            println("🔄 Fetching Firebase Remote Config...")
            val fetchSuccess = remoteConfigHelper.fetchAndActivate()

            if (fetchSuccess) {
                println("✅ Remote Config fetched successfully!")
            } else {
                println("⚠️ Using default Remote Config values")
            }

            _loadingProgress.value = 50

            // Phase 3: Check ad type and load ad (50-90%)
            val adTypeString = remoteConfigHelper.getString(RemoteConfigKeys.SPLASH_AD_TYPE)
            currentAdType = AdType.fromString(adTypeString)

            println("🎯 Splash Ad Type: ${currentAdType.value}")

            when (currentAdType) {
                AdType.INTERSTITIAL -> loadInterstitialAd(activity)
                AdType.APP_OPEN -> loadAppOpenAd(activity)
                AdType.NONE -> {
                    println("⏭️ No ad to load")
                    _loadingProgress.value = 90
                    completeLoading()
                }
            }
        }
    }

    /**
     * Load Interstitial Ad
     */
    private fun loadInterstitialAd(activity: Activity) {
        interstitialAdHelper = InterstitialAdHelper(BuildConfig.SPLASH_INTERSTITIAL_AD_ID)

        interstitialAdHelper?.loadAd(
            activity = activity,
            onAdLoaded = {
                _loadingProgress.value = 90
                completeLoading()
            },
            onAdFailedToLoad = { error ->
                println("⚠️ Failed to load interstitial ad, continuing anyway: $error")
                _loadingProgress.value = 90
                completeLoading()
            }
        )
    }

    /**
     * Load App Open Ad
     */
    private fun loadAppOpenAd(activity: Activity) {
        appOpenAdHelper = AppOpenAdHelper(BuildConfig.SPLASH_APP_OPEN_AD_ID)

        appOpenAdHelper?.loadAd(
            activity = activity,
            onAdLoaded = {
                _loadingProgress.value = 90
                completeLoading()
            },
            onAdFailedToLoad = { error ->
                println("⚠️ Failed to load app open ad, continuing anyway: $error")
                _loadingProgress.value = 90
                completeLoading()
            }
        )
    }

    /**
     * Complete loading animation
     */
    private fun completeLoading() {
        viewModelScope.launch {
            // Phase 4: Complete loading (90-100%)
            for (progress in 90..100 step 5) {
                _loadingProgress.value = progress
                delay(50)
            }

            // Test Remote Config
            testRemoteConfig()

            // Mark loading as complete and trigger ad show
            _isLoadingComplete.value = true
            _shouldShowAd.value = true
            println("✅ Splash loading completed successfully!")
        }
    }

    /**
     * Show ad based on type
     */
    fun showAd(
        activity: Activity,
        onAdClosed: () -> Unit
    ) {
        when (currentAdType) {
            AdType.INTERSTITIAL -> {
                interstitialAdHelper?.showAd(
                    activity = activity,
                    onAdShown = {
                        println("📺 Interstitial ad is showing")
                    },
                    onAdDismissed = {
                        println("✅ Interstitial ad dismissed, navigating to main screen")
                        onAdClosed()
                    },
                    onAdFailed = { error ->
                        println("❌ Failed to show interstitial ad: $error")
                        onAdClosed()
                    }
                )
            }
            AdType.APP_OPEN -> {
                appOpenAdHelper?.showAd(
                    activity = activity,
                    onAdShown = {
                        println("📺 App Open ad is showing")
                    },
                    onAdDismissed = {
                        println("✅ App Open ad dismissed, navigating to main screen")
                        onAdClosed()
                    },
                    onAdFailed = { error ->
                        println("❌ Failed to show app open ad: $error")
                        onAdClosed()
                    }
                )
            }
            AdType.NONE -> {
                println("⏭️ No ad to show, navigating to main screen")
                onAdClosed()
            }
        }
    }

    /**
     * Test Remote Config by printing values
     */
    private fun testRemoteConfig() {
        println("\n" + "=".repeat(50))
        println("🧪 Testing Firebase Remote Config")
        println("=".repeat(50))

        val testMessage = remoteConfigHelper.getString(RemoteConfigKeys.TEST_MESSAGE)
        val isFeatureEnabled = remoteConfigHelper.getBoolean(RemoteConfigKeys.IS_FEATURE_ENABLED)

        println("📝 Test Message: $testMessage")
        println("🎯 Feature Enabled: $isFeatureEnabled")

        // Print all configs
        remoteConfigHelper.printAllConfigs()

        println("=".repeat(50) + "\n")
    }

    override fun onCleared() {
        super.onCleared()
        interstitialAdHelper?.destroy()
        appOpenAdHelper?.destroy()
    }
}

