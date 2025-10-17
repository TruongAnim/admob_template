package com.truonganim.admob.ads

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.truonganim.admob.data.AdGateConfig
import com.truonganim.admob.firebase.RemoteConfigKeys
import com.truonganim.admob.ui.theme.AdMobBaseTheme
import kotlinx.coroutines.launch

/**
 * Ad Gate Activity
 * Shows a loading screen while loading and displaying interstitial ad
 */
class AdGateActivity : ComponentActivity() {

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
        // Set timeout
        timeoutRunnable = Runnable {
            if (!isFinishing) {
                // Timeout reached
                if (isRequired) {
                    // Required ad failed to load
                    finishWithResult(RESULT_AD_FAILED, false)
                } else {
                    // Optional ad failed, let user continue
                    finishWithResult(RESULT_AD_SKIPPED, false)
                }
            }
        }
        handler.postDelayed(timeoutRunnable!!, config.timeoutSeconds * 1000L)

        // Check if ad is available
        if (adManager.isAdAvailable()) {
            // Ad is ready, show it
            showAdNow()
        } else {
            // Ad not ready, try to load
            adManager.loadAd(
                onAdLoaded = {
                    // Ad loaded, show it
                    showAdNow()
                },
                onAdFailedToLoad = { error ->
                    // Failed to load
                    cancelTimeout()
                    if (isRequired) {
                        finishWithResult(RESULT_AD_FAILED, false)
                    } else {
                        finishWithResult(RESULT_AD_SKIPPED, false)
                    }
                }
            )
        }
    }

    private fun showAdNow() {
        adManager.showAdIfAvailable(
            activity = this,
            onAdDismissed = { adWasShown ->
                // Ad dismissed
                cancelTimeout()
                
                if (adWasShown) {
                    // Record ad shown time
                    lifecycleScope.launch {
                        intervalTracker.recordAdShown()
                    }
                    finishWithResult(RESULT_AD_SHOWN, true)
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

