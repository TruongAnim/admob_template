package com.truonganim.admob.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.truonganim.admob.MainActivity
import com.truonganim.admob.ui.theme.AdMobBaseTheme

/**
 * Splash Activity
 * Entry point of the app, displays splash screen with loading animation
 * Loads and shows ads based on Remote Config
 * Prevents back button press during loading
 */
class SplashActivity : ComponentActivity() {

    private lateinit var viewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this)[SplashViewModel::class.java]

        setContent {
            AdMobBaseTheme {
                SplashScreen(
                    viewModel = viewModel,
                    onAdReadyToShow = {
                        showAdAndNavigate()
                    }
                )
            }
        }
    }

    /**
     * Show ad (if any) and navigate to MainActivity
     */
    private fun showAdAndNavigate() {
        viewModel.showAd(
            activity = this,
            onAdClosed = {
                navigateToMain()
            }
        )
    }

    /**
     * Navigate to MainActivity
     */
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close splash activity
    }

    /**
     * Disable back button during splash screen
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing - prevent user from going back during splash
    }
}

