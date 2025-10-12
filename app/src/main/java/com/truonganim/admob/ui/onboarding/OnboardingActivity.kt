package com.truonganim.admob.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.truonganim.admob.ui.language.LanguageActivity
import com.truonganim.admob.ui.theme.AdMobBaseTheme

/**
 * Onboarding Activity
 * Displays onboarding flow with 4 pages
 */
class OnboardingActivity : ComponentActivity() {
    
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
     * Navigate to Language Selection screen
     */
    private fun navigateToLanguageScreen() {
        val intent = Intent(this, LanguageActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    /**
     * Disable back button
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing - prevent going back
    }
}

