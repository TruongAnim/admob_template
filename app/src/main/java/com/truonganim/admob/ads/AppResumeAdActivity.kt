package com.truonganim.admob.ads

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.truonganim.admob.data.AppOpenAdConfig
import com.truonganim.admob.firebase.RemoteConfigKeys
import com.truonganim.admob.ui.base.BaseActivity
import com.truonganim.admob.ui.theme.AdMobBaseTheme

/**
 * App Resume Ad Activity
 * Shows a loading screen while loading and displaying app open ad
 */
class AppResumeAdActivity : BaseActivity() {

    private lateinit var config: AppOpenAdConfig
    private lateinit var adManager: AppOpenAdManager
    private val handler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Load config from Remote Config
        loadConfig()

        // Check if feature is enabled
        if (!config.enabled) {
            finish()
            return
        }

        // Initialize ad manager
        adManager = AppOpenAdManager(this, config)

        setContent {
            AdMobBaseTheme {
                AppResumeAdScreen(
                    config = config
                )
            }
        }

        // Start loading ad
        loadAndShowAd()
    }

    private fun loadConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configJson = remoteConfig.getString(RemoteConfigKeys.APP_OPEN_AD_CONFIG)
        
        config = if (configJson.isNotEmpty()) {
            AppOpenAdConfig.fromJson(configJson)
        } else {
            // Use default config for testing
            AppOpenAdConfig.getDefault()
        }
    }

    private fun loadAndShowAd() {
        // Set timeout
        timeoutRunnable = Runnable {
            if (!isFinishing) {
                finish()
            }
        }
        handler.postDelayed(timeoutRunnable!!, config.timeoutSeconds * 1000L)

        // Load ad
        adManager.loadAd(
            onAdLoaded = {
                // Ad loaded, show it
                showAd()
            },
            onAdFailedToLoad = { error ->
                // Failed to load, finish activity
                cancelTimeout()
                finish()
            }
        )
    }

    private fun showAd() {
        adManager.showAdIfAvailable(
            activity = this,
            onAdDismissed = {
                // Ad dismissed, finish activity
                cancelTimeout()
                finish()
            },
            onAdFailedToShow = { error ->
                // Failed to show, finish activity
                cancelTimeout()
                finish()
            }
        )
    }

    private fun cancelTimeout() {
        timeoutRunnable?.let { handler.removeCallbacks(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelTimeout()
        adManager.clear()
    }
}

@Composable
private fun AppResumeAdScreen(
    config: AppOpenAdConfig
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
                // Welcome message
                Text(
                    text = "Welcome back!",
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
                    text = "Loading...",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = if (config.backgroundImageUrl.isNotEmpty()) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onBackground
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

