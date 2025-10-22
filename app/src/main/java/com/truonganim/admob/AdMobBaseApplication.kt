package com.truonganim.admob

import android.app.Application
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
class AdMobBaseApplication : Application() {

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
                val premiumPrefs = PremiumPreferencesManager.getInstance(this@AdMobBaseApplication)
                premiumPrefs.loadPremiumStatus()
                println("💎 Premium preferences loaded")

                // Initialize billing repository (will auto-connect)
                val billingRepository = BillingRepository.getInstance(this@AdMobBaseApplication)

                // Sync purchases with Google Play
                billingRepository.syncPurchases()
                println("💎 Billing sync initiated")
            } catch (e: Exception) {
                println("❌ Failed to initialize billing: ${e.message}")
            }
        }
    }
}

