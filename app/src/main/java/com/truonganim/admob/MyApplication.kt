package com.truonganim.admob

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.truonganim.admob.ads.native_ads.NativeAdManager
import com.truonganim.admob.ads.native_ads.NativeAdPosition
import com.truonganim.admob.billing.BillingRepository
import com.truonganim.admob.billing.PremiumPreferencesManager
import com.truonganim.admob.lifecycle.AppLifecycleObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application class
 * Initialize Firebase, AdMob and other app-wide components
 */
class MyApplication : Application() {

    private lateinit var appLifecycleObserver: AppLifecycleObserver
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        println("🔥 Firebase initialized successfully!")

        // Initialize AdMob
        MobileAds.initialize(this) {
            println("📱 AdMob initialized successfully!")

            // Preload native ads after AdMob is initialized
            preloadNativeAds()
        }

        // Initialize app lifecycle observer for app open ads
        appLifecycleObserver = AppLifecycleObserver(this)
        println("👁️ App lifecycle observer initialized!")

        // Register activity lifecycle callbacks to hide navigation bar
        registerActivityLifecycleCallbacks(navigationBarHider)
        println("🎨 Navigation bar hider registered!")

        // Initialize billing and sync premium status
        initializeBilling()
    }

    /**
     * Preload native ads for common positions
     */
    private fun preloadNativeAds() {
        val adManager = NativeAdManager.getInstance()

        // Preload onboarding screen ads
        adManager.preloadAd(this, NativeAdPosition.ONBOARDING_PAGE_1)
        adManager.preloadAd(this, NativeAdPosition.ONBOARDING_PAGE_3)

        // Preload language screen ads
        adManager.preloadAd(this, NativeAdPosition.LANGUAGE_SCREEN)
        adManager.preloadAd(this, NativeAdPosition.LANGUAGE_SCREEN_2)

        // Preload other ads as needed
        // adManager.preloadAd(this, NativeAdPosition.HOME_SCREEN)
    }

    /**
     * Initialize billing and sync premium status
     * This will check with Google Play if user has active subscription
     */
    private fun initializeBilling() {
        applicationScope.launch {
            try {
                // Initialize premium preferences
                val premiumPrefs = PremiumPreferencesManager.getInstance(this@MyApplication)
                premiumPrefs.loadPremiumStatus()
                println("💎 Premium preferences loaded")

                // Initialize billing repository (will auto-connect)
                val billingRepository = BillingRepository.getInstance(this@MyApplication)

                // Sync purchases with Google Play
                billingRepository.syncPurchases()
                println("💎 Billing sync initiated")
            } catch (e: Exception) {
                println("❌ Failed to initialize billing: ${e.message}")
            }
        }
    }

    /**
     * Activity lifecycle callbacks to hide navigation bar for all activities
     */
    private val navigationBarHider = object : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            hideNavigationBar(activity)
        }

        override fun onActivityResumed(activity: Activity) {
            hideNavigationBar(activity)
        }

        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }

    /**
     * Hide navigation bar for an activity
     * Uses modern WindowInsetsController for Android 11+ and legacy method for older versions
     */
    private fun hideNavigationBar(activity: Activity) {
        val window = activity.window
        val decorView = window.decorView

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+) - Use WindowInsetsController
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            controller?.let {
                it.hide(WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Android 10 and below - Use legacy flags
            @Suppress("DEPRECATION")
            decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            )
        }
    }
}

