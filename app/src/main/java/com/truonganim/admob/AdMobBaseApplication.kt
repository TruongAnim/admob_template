package com.truonganim.admob

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp

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
        }
    }
}

