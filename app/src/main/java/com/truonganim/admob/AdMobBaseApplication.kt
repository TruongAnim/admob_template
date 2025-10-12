package com.truonganim.admob

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.truonganim.admob.ads.native_ads.NativeAdManager
import com.truonganim.admob.ads.native_ads.NativeAdPosition

/**
 * Application class
 * Initialize Firebase, AdMob and other app-wide components
 */
class AdMobBaseApplication : Application() {

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
    }

    /**
     * Preload native ads for common positions
     */
    private fun preloadNativeAds() {
        val adManager = NativeAdManager.getInstance()

        // Preload language screen ad
        adManager.preloadAd(this, NativeAdPosition.LANGUAGE_SCREEN)

        // Preload other ads as needed
        // adManager.preloadAd(this, NativeAdPosition.HOME_SCREEN)
    }
}

