package com.truonganim.admob

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.truonganim.admob.ads.native_ads.NativeAdManager
import com.truonganim.admob.ads.native_ads.NativeAdPosition
import com.truonganim.admob.lifecycle.AppLifecycleObserver

/**
 * Application class
 * Initialize Firebase, AdMob and other app-wide components
 */
class AdMobBaseApplication : Application() {

    private lateinit var appLifecycleObserver: AppLifecycleObserver

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
}

