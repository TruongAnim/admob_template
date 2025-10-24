package com.truonganim.admob.ads.native_ads

import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.truonganim.admob.R

/**
 * Native Ad View Composable
 * Displays a native ad using AndroidView
 * Observes StateFlow from NativeAdManager to automatically update when ad is loaded
 * Shows skeleton loading while ad is being loaded
 * Returns nothing if position is disabled in remote config
 */
@Composable
fun NativeAdView(
    position: NativeAdPosition,
    modifier: Modifier = Modifier
) {
    // Check if position is enabled in remote config
    if (!NativeAdHelper.isPositionEnabled(position)) {
        println("🚫 NativeAdView for ${position.name} is disabled in remote config - not showing")
        return
    }

    val context = LocalContext.current
    val adManager = remember { NativeAdManager.getInstance() }

    // Observe StateFlow from manager
    val nativeAd by adManager.getAdStateFlow(position).collectAsState()

    // Trigger load if not already loaded/loading
    DisposableEffect(position) {
        println("🎯 NativeAdView for ${position.name} is observing ad state")
        adManager.loadAd(context, position)

        onDispose {
            // Don't destroy the ad here, let the manager handle it
        }
    }

    // Display ad if loaded, otherwise show skeleton
    if (nativeAd != null) {
        println("✅ Displaying native ad for position: ${position.name}")
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { ctx ->
                val adView = LayoutInflater.from(ctx).inflate(
                    position.layoutResId,
                    null,
                    false
                ) as com.google.android.gms.ads.nativead.NativeAdView

                populateNativeAdView(nativeAd!!, adView)
                adView
            }
        )
    } else {
        println("⏳ Showing skeleton for position: ${position.name}")
        NativeAdSkeleton(modifier = modifier)
    }
}

/**
 * Populate native ad view with ad data
 */
private fun populateNativeAdView(
    nativeAd: NativeAd,
    adView: com.google.android.gms.ads.nativead.NativeAdView
) {
    // Set headline
    adView.findViewById<TextView>(R.id.ad_headline)?.let {
        it.text = nativeAd.headline
        adView.headlineView = it
    }
    
    // Set body
    adView.findViewById<TextView>(R.id.ad_body)?.let {
        it.text = nativeAd.body
        adView.bodyView = it
    }
    
    // Set call to action
    adView.findViewById<Button>(R.id.ad_call_to_action)?.let {
        it.text = nativeAd.callToAction
        adView.callToActionView = it
    }
    
    // Set icon
    adView.findViewById<ImageView>(R.id.ad_icon)?.let {
        nativeAd.icon?.let { icon ->
            it.setImageDrawable(icon.drawable)
        }
        adView.iconView = it
    }
    
    // Set media view (for medium layout)
    adView.findViewById<MediaView>(R.id.ad_media)?.let {
        adView.mediaView = it
    }
    
    // Set advertiser (for medium layout)
    adView.findViewById<TextView>(R.id.ad_advertiser)?.let {
        it.text = nativeAd.advertiser
        adView.advertiserView = it
    }
    
    // Register the native ad
    adView.setNativeAd(nativeAd)
}

/**
 * Preload native ad for a position
 * Call this early to cache the ad
 */
@Composable
fun PreloadNativeAd(position: NativeAdPosition) {
    val context = LocalContext.current
    val adManager = remember { NativeAdManager.getInstance() }

    DisposableEffect(position) {
        adManager.preloadAd(context, position)
        onDispose { }
    }
}

