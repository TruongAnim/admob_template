package com.truonganim.admob.lifecycle

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.truonganim.admob.ads.AppResumeAdActivity
import com.truonganim.admob.data.AppOpenAdConfig
import com.truonganim.admob.firebase.RemoteConfigKeys
import com.truonganim.admob.ui.splash.SplashActivity

/**
 * App Lifecycle Observer
 * Monitors app lifecycle and shows app open ad when app comes to foreground
 */
class AppLifecycleObserver(
    private val application: Application
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private var currentActivity: Activity? = null
    private var isAppInForeground = false
    private var isFirstLaunch = true

    companion object {
        private const val TAG = "AppLifecycleObserver"
        
        // Activities that should not trigger app open ad
        private val EXCLUDED_ACTIVITIES = setOf(
            SplashActivity::class.java.name,
            AppResumeAdActivity::class.java.name
        )
    }

    init {
        // Register lifecycle observer
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        
        // Register activity lifecycle callbacks
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d(TAG, "App moved to foreground")
        
        // Skip first launch (app just started)
        if (isFirstLaunch) {
            isFirstLaunch = false
            isAppInForeground = true
            return
        }
        
        // App came back to foreground, show app open ad
        if (!isAppInForeground) {
            isAppInForeground = true
            showAppOpenAd()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d(TAG, "App moved to background")
        isAppInForeground = false
    }

    private fun showAppOpenAd() {
        val activity = currentActivity ?: return
        
        // Check if current activity is excluded
        if (EXCLUDED_ACTIVITIES.contains(activity::class.java.name)) {
            Log.d(TAG, "Current activity is excluded from app open ad")
            return
        }
        
        // Check if feature is enabled
        val config = getConfig()
        if (!config.enabled) {
            Log.d(TAG, "App open ad is disabled")
            return
        }
        
        // Start AppResumeAdActivity
        Log.d(TAG, "Starting AppResumeAdActivity")
        val intent = Intent(activity, AppResumeAdActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intent)
    }

    private fun getConfig(): AppOpenAdConfig {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configJson = remoteConfig.getString(RemoteConfigKeys.APP_OPEN_AD_CONFIG)
        
        return if (configJson.isNotEmpty()) {
            AppOpenAdConfig.fromJson(configJson)
        } else {
            AppOpenAdConfig.getDefault()
        }
    }

    // Activity Lifecycle Callbacks
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}

