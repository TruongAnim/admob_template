package com.truonganim.admob.ads

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.truonganim.admob.BuildConfig
import com.truonganim.admob.R
import com.truonganim.admob.data.AdGateConfig
import com.truonganim.admob.firebase.RemoteConfigKeys
import com.truonganim.admob.ui.base.BaseActivity
import com.truonganim.admob.ui.theme.AdMobBaseTheme
import com.truonganim.admob.utils.AdLogger
import kotlinx.coroutines.launch

/**
 * Ad Gate Activity
 * Shows a loading screen while loading and displaying interstitial ad
 */
class AdGateActivity : BaseActivity() {

    private lateinit var config: AdGateConfig
    private lateinit var adManager: InterstitialAdManager
    private lateinit var intervalTracker: AdIntervalTracker
    private val handler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null
    private var isRequired = false

    companion object {
        private const val TAG = "AdGateActivity"
        
        const val EXTRA_IS_REQUIRED = "extra_is_required"
        const val EXTRA_AD_SHOWN = "extra_ad_shown"
        
        const val RESULT_AD_SHOWN = 100
        const val RESULT_AD_FAILED = 101
        const val RESULT_AD_SKIPPED = 102
        
        /**
         * Create intent to start Ad Gate
         * @param isRequired If true, user must watch ad. If false, respects ad interval
         */
        fun createIntent(context: Context, isRequired: Boolean = false): Intent {
            return Intent(context, AdGateActivity::class.java).apply {
                putExtra(EXTRA_IS_REQUIRED, isRequired)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get parameters
        isRequired = intent.getBooleanExtra(EXTRA_IS_REQUIRED, false)

        // Load config
        loadConfig()

        // Initialize managers
        adManager = InterstitialAdManager.getInstance(this)
        intervalTracker = AdIntervalTracker.getInstance(this)

        // Set native ad enabled based on config
        adManager.setNativeAdEnabled(config.nativeAdEnabled)

        setContent {
            AdMobBaseTheme {
                AdGateScreen(
                    config = config
                )
            }
        }

        // Check and show ad
        checkAndShowAd()
    }

    private fun loadConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configJson = remoteConfig.getString(RemoteConfigKeys.AD_GATE_CONFIG)
        
        config = if (configJson.isNotEmpty()) {
            AdGateConfig.fromJson(configJson)
        } else {
            AdGateConfig.getDefault()
        }
    }

    private fun checkAndShowAd() {
        // Check if feature is enabled
        if (!config.enabled) {
            finishWithResult(RESULT_AD_SKIPPED, false)
            return
        }

        // If not required, check interval
        if (!isRequired) {
            lifecycleScope.launch {
                val shouldShow = intervalTracker.shouldShowAd(config.adIntervalSeconds)
                if (!shouldShow) {
                    // Interval not reached, skip ad
                    finishWithResult(RESULT_AD_SKIPPED, false)
                    return@launch
                }
                
                // Interval reached, show ad
                showAd()
            }
        } else {
            // Required, show ad immediately
            showAd()
        }
    }

    private fun showAd() {
        // Check if ad is available
        if (adManager.isInterstitialAdAvailable()) {
            // Ad is ready, show it immediately
            AdLogger.i(TAG, "Ad available, showing immediately")
            showAdNow()
        } else {
            if(!isRequired) {
                AdLogger.w(TAG, "Ad not available, close with RESULT_AD_SKIPPED")
                finishWithResult(RESULT_AD_SKIPPED, false)
                return
            }
            // Ad not available, wait 2 seconds then close
            AdLogger.w(TAG, "Ad not available, waiting ${config.timeoutSeconds} second then close with RESULT_AD_FAILED")

            timeoutRunnable = Runnable {
                if (!isFinishing) {
                    // Optional ad not available, let user continue
                    finishWithResult(RESULT_AD_SKIPPED, false)
                }
            }
            handler.postDelayed(timeoutRunnable!!, config.timeoutSeconds * 1000L) // 2 seconds
        }
    }

    private fun showAdNow() {
        adManager.showInterstitialAdIfAvailable(
            activity = this,
            onAdDismissed = { adWasShown ->
                // Ad dismissed
                cancelTimeout()

                if (adWasShown) {
                    // Record ad shown time
                    lifecycleScope.launch {
                        intervalTracker.recordAdShown()
                    }

                    // Check if native ad is available
                    showNativeAdOrFinish()
                } else {
                    if (isRequired) {
                        finishWithResult(RESULT_AD_FAILED, false)
                    } else {
                        finishWithResult(RESULT_AD_SKIPPED, false)
                    }
                }
            },
            onAdFailedToShow = { error ->
                // Failed to show
                cancelTimeout()
                if (isRequired) {
                    finishWithResult(RESULT_AD_FAILED, false)
                } else {
                    finishWithResult(RESULT_AD_SKIPPED, false)
                }
            }
        )
    }

    private fun showNativeAdOrFinish() {
        val nativeAd = adManager.getNativeAd()

        if (nativeAd != null && config.nativeAdEnabled) {
            // Native ad is ready, show it
            AdLogger.i(TAG, "Showing native ad")
            setContent {
                AdMobBaseTheme {
                    NativeAdFullScreen(
                        nativeAd = nativeAd,
                        config = config,
                        onClose = {
                            // Mark native ad as shown
                            adManager.markNativeAdShown()
                            finishWithResult(RESULT_AD_SHOWN, true)
                        }
                    )
                }
            }
        } else {
            // Native ad not available or disabled, finish
            AdLogger.i(TAG, "Native ad not available or disabled, finishing")
            finishWithResult(RESULT_AD_SHOWN, true)
        }
    }

    private fun cancelTimeout() {
        timeoutRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun finishWithResult(resultCode: Int, adShown: Boolean) {
        val resultIntent = Intent().apply {
            putExtra(EXTRA_AD_SHOWN, adShown)
        }
        setResult(resultCode, resultIntent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelTimeout()
    }
}

@Composable
private fun AdGateScreen(
    config: AdGateConfig
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background Image
            if (config.backgroundImageUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(config.backgroundImageUrl),
                    contentDescription = "Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark overlay
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {}
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Unlocking message
                Text(
                    text = "Unlocking Feature",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (config.backgroundImageUrl.isNotEmpty()) Color.White else MaterialTheme.colorScheme.onBackground
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Loading indicator
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = if (config.backgroundImageUrl.isNotEmpty()) Color.White else MaterialTheme.colorScheme.primary,
                    strokeWidth = 6.dp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Loading text
                Text(
                    text = "Please wait...",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = if (config.backgroundImageUrl.isNotEmpty()) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onBackground
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun NativeAdFullScreen(
    nativeAd: NativeAd,
    config: AdGateConfig,
    onClose: () -> Unit
) {
    var showCloseButton by remember { mutableStateOf(false) }

    // Show close button after delay
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(config.nativeAdCloseDelaySeconds * 1000L)
        showCloseButton = true
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Native Ad View
        AndroidView(
            factory = { context ->
                val adView = android.view.LayoutInflater.from(context)
                    .inflate(R.layout.native_ad_fullscreen, null) as NativeAdView

                // Populate ad view
                populateNativeAdView(nativeAd, adView)

                adView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Close button
        if (showCloseButton) {
            val alignment = if (config.nativeAdClosePosition == "top_left") {
                Alignment.TopStart
            } else {
                Alignment.TopEnd
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(alignment)
                    .padding(16.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = CircleShape
                    )
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
    // Set ad assets
    adView.headlineView = adView.findViewById(R.id.ad_headline)
    adView.bodyView = adView.findViewById(R.id.ad_body)
    adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
    adView.iconView = adView.findViewById(R.id.ad_app_icon)
    adView.mediaView = adView.findViewById(R.id.ad_media)

    // Populate views
    (adView.headlineView as? android.widget.TextView)?.text = nativeAd.headline
    (adView.bodyView as? android.widget.TextView)?.text = nativeAd.body
    (adView.callToActionView as? android.widget.Button)?.text = nativeAd.callToAction

    nativeAd.icon?.let { icon ->
        (adView.iconView as? android.widget.ImageView)?.setImageDrawable(icon.drawable)
        adView.iconView?.visibility = android.view.View.VISIBLE
    } ?: run {
        adView.iconView?.visibility = android.view.View.GONE
    }

    adView.mediaView?.let { mediaView ->
        nativeAd.mediaContent?.let { mediaContent ->
            mediaView.setMediaContent(mediaContent)
        }
    }

    // Set native ad
    adView.setNativeAd(nativeAd)
}

