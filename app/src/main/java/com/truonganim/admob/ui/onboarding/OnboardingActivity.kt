package com.truonganim.admob.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.truonganim.admob.MainActivity
import com.truonganim.admob.datastore.PreferencesManager
import com.truonganim.admob.ui.base.BaseActivity
import com.truonganim.admob.ui.theme.AdMobBaseTheme
import kotlinx.coroutines.launch

/**
 * Onboarding Activity
 * Displays onboarding flow with 4 pages
 */
class OnboardingActivity : BaseActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AdMobBaseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OnboardingScreen(
                        onGetStarted = {
                            navigateToLanguageScreen()
                        }
                    )
                }
            }
        }
    }
    
    /**
     * Navigate to Main screen and mark onboarding as completed
     */
    private fun navigateToLanguageScreen() {
        lifecycleScope.launch {
            // Mark onboarding as completed
            val preferencesManager = PreferencesManager.getInstance(this@OnboardingActivity)
            preferencesManager.setOnboardingCompleted(true)
            println("✅ Onboarding completed!")

            // Navigate to Main screen
            val intent = Intent(this@OnboardingActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    
    /**
     * Disable back button
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing - prevent going back
    }
}

