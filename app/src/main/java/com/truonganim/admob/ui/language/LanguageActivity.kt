package com.truonganim.admob.ui.language

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.truonganim.admob.ui.onboarding.OnboardingActivity
import com.truonganim.admob.ui.theme.AdMobBaseTheme

/**
 * Language Selection Activity
 * Allows user to select their preferred language
 */
class LanguageActivity : ComponentActivity() {

    private lateinit var viewModel: LanguageViewModel
    private var isFromSettings = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check if opened from Settings
        isFromSettings = intent.getBooleanExtra(EXTRA_FROM_SETTINGS, false)

        viewModel = ViewModelProvider(this)[LanguageViewModel::class.java]

        setContent {
            AdMobBaseTheme {
                LanguageScreen(
                    viewModel = viewModel,
                    showNativeAd = !isFromSettings,
                    showBackButton = isFromSettings,
                    onBackClick = {
                        finish()
                    },
                    onLanguageConfirmed = {
                        if (isFromSettings) {
                            // Close and let MainActivity recreate itself
                            // The locale change will be applied automatically
                            setResult(RESULT_OK)
                            finish()
                        } else {
                            // Navigate to onboarding (first time flow)
                            navigateToOnboarding()
                        }
                    }
                )
            }
        }
    }

    /**
     * Navigate to Onboarding after language is confirmed
     */
    private fun navigateToOnboarding() {
        val intent = Intent(this, OnboardingActivity::class.java)
        startActivity(intent)
        finish() // Close language activity
    }

    /**
     * Handle back button
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isFromSettings) {
            // Allow back if from settings
            super.onBackPressed()
        }
        // Otherwise do nothing - prevent user from going back during first time flow
    }

    companion object {
        private const val EXTRA_FROM_SETTINGS = "extra_from_settings"

        /**
         * Create intent to open LanguageActivity from Settings
         */
        fun createIntent(context: Context, fromSettings: Boolean = false): Intent {
            return Intent(context, LanguageActivity::class.java).apply {
                putExtra(EXTRA_FROM_SETTINGS, fromSettings)
            }
        }
    }
}

