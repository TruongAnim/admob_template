package com.truonganim.admob.ads.native_ads

import com.truonganim.admob.BuildConfig
import com.truonganim.admob.R

/**
 * Native Ad Position Enum
 * Defines different positions where native ads can be displayed
 * Each position has its own ad unit ID and layout
 */
enum class NativeAdPosition(
    val adUnitId: String,
    val layoutResId: Int
) {
    /**
     * Native ad at the bottom of Language Selection screen (initial)
     * Uses medium layout with image
     */
    LANGUAGE_SCREEN(
        adUnitId = BuildConfig.NATIVE_AD_LANGUAGE_SCREEN,
        layoutResId = R.layout.native_ad_medium
    ),

    /**
     * Native ad at the bottom of Language Selection screen (after language selected)
     * Uses medium layout with image
     */
    LANGUAGE_SCREEN_2(
        adUnitId = BuildConfig.NATIVE_AD_LANGUAGE_SCREEN_2,
        layoutResId = R.layout.native_ad_medium
    ),

    /**
     * Native ad on Home screen
     * Uses medium layout
     */
    HOME_SCREEN(
        adUnitId = BuildConfig.NATIVE_AD_HOME_SCREEN,
        layoutResId = R.layout.native_ad_medium
    ),
    
    /**
     * Native ad in list items
     * Uses small layout without image
     */
    LIST_ITEM(
        adUnitId = BuildConfig.NATIVE_AD_LIST_ITEM,
        layoutResId = R.layout.native_ad_small
    );
    
    companion object {
        /**
         * Get position by name
         */
        fun fromName(name: String): NativeAdPosition? {
            return entries.find { it.name == name }
        }
    }
}

