package com.truonganim.admob.ads.native_ads

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.truonganim.admob.data.NativeAdConfig
import com.truonganim.admob.firebase.RemoteConfigKeys

/**
 * Native Ad Helper
 * Utility functions for native ads
 */
object NativeAdHelper {
    
    /**
     * Check if a native ad position is enabled in remote config
     * 
     * @param position The native ad position to check
     * @return true if the position is enabled, false otherwise
     */
    fun isPositionEnabled(position: NativeAdPosition): Boolean {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configJson = remoteConfig.getString(RemoteConfigKeys.NATIVE_ADS_CONFIG)

        val config = if (configJson.isNotEmpty()) {
            NativeAdConfig.fromJson(configJson)
        } else {
            NativeAdConfig.getDefault()
        }

        return when (position) {
            NativeAdPosition.LANGUAGE_SCREEN -> config.languageScreen
            NativeAdPosition.LANGUAGE_SCREEN_2 -> config.languageScreen2
            NativeAdPosition.ONBOARDING_PAGE_1 -> config.onboardingPage1
            NativeAdPosition.ONBOARDING_PAGE_3 -> config.onboardingPage3
            NativeAdPosition.HOME_SCREEN -> config.homeScreen
            NativeAdPosition.LIST_ITEM -> config.listItem
        }
    }
}

