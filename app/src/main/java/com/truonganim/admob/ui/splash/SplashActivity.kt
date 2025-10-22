package com.truonganim.admob.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.truonganim.admob.MainActivity
import com.truonganim.admob.datastore.PreferencesManager
import com.truonganim.admob.ui.language.LanguageActivity
import com.truonganim.admob.ui.onboarding.OnboardingActivity
import com.truonganim.admob.ui.theme.AdMobBaseTheme
import kotlinx.coroutines.launch

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
     * Show ad (if any) and navigate based on onboarding status
     */
    private fun showAdAndNavigate() {
        viewModel.showAd(
            activity = this,
            onAdClosed = {
                checkOnboardingAndNavigate()
            }
        )
    }

    /**
     * Check onboarding status and navigate accordingly
     * - If onboarding completed: Navigate to MainActivity
     * - If onboarding not completed: Navigate to LanguageActivity
     */
    private fun checkOnboardingAndNavigate() {
        lifecycleScope.launch {
            val preferencesManager = PreferencesManager.getInstance(this@SplashActivity)
            val isOnboardingCompleted = preferencesManager.isOnboardingCompletedSync()

            if (isOnboardingCompleted && false) {
                println("✅ Onboarding already completed, navigating to MainActivity")
                navigateToMain()
            } else {
                println("⚠️ Onboarding not completed, navigating to LanguageActivity")
                navigateToLanguage()
            }
        }
    }

    /**
     * Navigate to Language Activity
     */
    private fun navigateToLanguage() {
        val intent = Intent(this, LanguageActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Navigate to Main Activity
     */
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Disable back button during splash screen
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing - prevent user from going back during splash
    }
}

